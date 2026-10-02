package com.nomp.northstar.modules.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.constant.DataScope;
import com.nomp.northstar.common.security.DataScopeHelper;
import com.nomp.northstar.common.security.LoginUser;
import com.nomp.northstar.modules.system.domain.SysPermission;
import com.nomp.northstar.modules.system.domain.SysRole;
import com.nomp.northstar.modules.system.domain.SysRoleDept;
import com.nomp.northstar.modules.system.domain.SysRolePermission;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.domain.SysUserRole;
import com.nomp.northstar.modules.system.mapper.SysPermissionMapper;
import com.nomp.northstar.modules.system.mapper.SysRoleDeptMapper;
import com.nomp.northstar.modules.system.mapper.SysRoleMapper;
import com.nomp.northstar.modules.system.mapper.SysRolePermissionMapper;
import com.nomp.northstar.modules.system.mapper.SysUserRoleMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginUserAssembler {
  private final SysUserRoleMapper userRoleMapper;
  private final SysRoleMapper roleMapper;
  private final SysRolePermissionMapper rolePermissionMapper;
  private final SysPermissionMapper permissionMapper;
  private final SysRoleDeptMapper roleDeptMapper;
  private final DataScopeHelper dataScopeHelper;

  public LoginUser assemble(SysUser user) {
    List<Long> roleIds = userRoleMapper.selectList(
            Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, user.getId()))
        .stream()
        .map(SysUserRole::getRoleId)
        .toList();
    List<SysRole> roles = roleIds.isEmpty()
        ? List.of()
        : roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
            .in(SysRole::getId, roleIds)
            .eq(SysRole::getStatus, "active"));

    boolean superAdmin = roles.stream().anyMatch(r -> "super_admin".equals(r.getCode()));
    Set<String> roleCodes = roles.stream().map(SysRole::getCode).collect(Collectors.toSet());
    Set<String> permissions = new HashSet<>();
    if (superAdmin) {
      permissions.addAll(permissionMapper.selectList(null).stream().map(SysPermission::getCode).toList());
    } else if (!roles.isEmpty()) {
      List<Long> pids = rolePermissionMapper.selectList(
              Wrappers.<SysRolePermission>lambdaQuery().in(SysRolePermission::getRoleId, roles.stream().map(SysRole::getId).toList()))
          .stream()
          .map(SysRolePermission::getPermissionId)
          .toList();
      if (!pids.isEmpty()) {
        permissions.addAll(permissionMapper.selectBatchIds(pids).stream().map(SysPermission::getCode).toList());
      }
    }

    DataScope effective = DataScope.SELF;
    Set<Long> deptIds = new HashSet<>();
    if (superAdmin || roles.stream().anyMatch(r -> DataScope.ALL.name().equals(r.getDataScope()))) {
      effective = DataScope.ALL;
    } else {
      boolean selfOnly = true;
      for (SysRole role : roles) {
        DataScope scope = DataScope.valueOf(role.getDataScope());
        if (scope == DataScope.SELF) {
          continue;
        }
        selfOnly = false;
        if (scope == DataScope.DEPT && user.getDeptId() != null) {
          deptIds.add(user.getDeptId());
        } else if (scope == DataScope.DEPT_TREE) {
          deptIds.addAll(dataScopeHelper.deptTreeIds(user.getDeptId()));
        } else if (scope == DataScope.CUSTOM) {
          deptIds.addAll(roleDeptMapper.selectList(
                  Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, role.getId()))
              .stream()
              .map(SysRoleDept::getDeptId)
              .filter(Objects::nonNull)
              .toList());
        }
      }
      effective = selfOnly ? DataScope.SELF : DataScope.CUSTOM;
    }

    LoginUser loginUser = new LoginUser();
    loginUser.setUserId(user.getId());
    loginUser.setUsername(user.getUsername());
    loginUser.setDisplayName(user.getDisplayName());
    loginUser.setDeptId(user.getDeptId());
    loginUser.setSuperAdmin(superAdmin);
    loginUser.setDataScope(effective);
    loginUser.setPermissions(permissions);
    loginUser.setRoles(roleCodes);
    loginUser.setDeptIds(deptIds);
    loginUser.setMustChangePwd(Integer.valueOf(1).equals(user.getMustChangePwd()));
    return loginUser;
  }
}
