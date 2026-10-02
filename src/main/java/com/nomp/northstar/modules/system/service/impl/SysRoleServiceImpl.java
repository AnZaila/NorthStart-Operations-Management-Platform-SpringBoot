package com.nomp.northstar.modules.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.modules.system.domain.SysPermission;
import com.nomp.northstar.modules.system.domain.SysRole;
import com.nomp.northstar.modules.system.domain.SysRoleDept;
import com.nomp.northstar.modules.system.domain.SysRolePermission;
import com.nomp.northstar.modules.system.domain.SysUserRole;
import com.nomp.northstar.modules.system.mapper.SysPermissionMapper;
import com.nomp.northstar.modules.system.mapper.SysRoleDeptMapper;
import com.nomp.northstar.modules.system.mapper.SysRoleMapper;
import com.nomp.northstar.modules.system.mapper.SysRolePermissionMapper;
import com.nomp.northstar.modules.system.mapper.SysUserRoleMapper;
import com.nomp.northstar.modules.system.model.dto.SysRoleGrantDTO;
import com.nomp.northstar.modules.system.model.dto.SysRoleSaveDTO;
import com.nomp.northstar.modules.system.model.vo.SysRoleVO;
import com.nomp.northstar.modules.system.model.vo.SysUserVO;
import com.nomp.northstar.modules.system.service.ISysRoleService;
import com.nomp.northstar.modules.org.domain.SysDept;
import com.nomp.northstar.modules.org.mapper.SysDeptMapper;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.mapper.SysUserMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements ISysRoleService {
  private final SysRoleMapper roleMapper;
  private final SysRolePermissionMapper rolePermissionMapper;
  private final SysPermissionMapper permissionMapper;
  private final SysRoleDeptMapper roleDeptMapper;
  private final SysUserRoleMapper userRoleMapper;
  private final SysUserMapper userMapper;
  private final SysDeptMapper deptMapper;

  @Override
  public List<SysRoleVO> listAll() {
    return roleMapper.selectList(Wrappers.<SysRole>lambdaQuery().orderByAsc(SysRole::getSortNo)).stream()
        .map(this::toVo)
        .toList();
  }

  @Override
  public SysRoleVO detail(Long id) {
    return toVo(require(id));
  }

  @Override
  @Transactional
  public Long create(SysRoleSaveDTO dto) {
    assertCodeUnique(dto.getCode(), null);
    SysRole role = new SysRole();
    fill(role, dto);
    role.setBuiltin(0);
    roleMapper.insert(role);
    replaceDepts(role.getId(), dto.getDeptIds());
    return role.getId();
  }

  @Override
  @Transactional
  public void update(Long id, SysRoleSaveDTO dto) {
    SysRole role = require(id);
    if (Integer.valueOf(1).equals(role.getBuiltin()) && !role.getCode().equals(dto.getCode())) {
      throw BizException.conflict("内置角色编码不可修改");
    }
    assertCodeUnique(dto.getCode(), id);
    fill(role, dto);
    if (Integer.valueOf(1).equals(role.getBuiltin())) {
      role.setCode(require(id).getCode());
    }
    roleMapper.updateById(role);
    replaceDepts(id, dto.getDeptIds());
  }

  @Override
  @Transactional
  public void remove(Long id) {
    SysRole role = require(id);
    if (Integer.valueOf(1).equals(role.getBuiltin())) {
      throw BizException.conflict("内置角色不可删除");
    }
    Long count = userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, id));
    if (count != null && count > 0) {
      throw BizException.conflict("角色仍有成员，无法删除");
    }
    roleMapper.deleteById(id);
    rolePermissionMapper.delete(Wrappers.<SysRolePermission>lambdaQuery().eq(SysRolePermission::getRoleId, id));
    roleDeptMapper.delete(Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, id));
  }

  @Override
  @Transactional
  public void grant(Long id, SysRoleGrantDTO dto) {
    SysRole role = require(id);
    if ("super_admin".equals(role.getCode())) {
      throw BizException.conflict("超级管理员权限不可收窄");
    }
    if (StrUtil.isNotBlank(dto.getDataScope())) {
      role.setDataScope(dto.getDataScope());
      roleMapper.updateById(role);
    }
    rolePermissionMapper.delete(Wrappers.<SysRolePermission>lambdaQuery().eq(SysRolePermission::getRoleId, id));
    if (dto.getPermissionCodes() != null && !dto.getPermissionCodes().isEmpty()) {
      List<SysPermission> perms = permissionMapper.selectList(
          Wrappers.<SysPermission>lambdaQuery().in(SysPermission::getCode, dto.getPermissionCodes()));
      for (SysPermission perm : perms) {
        SysRolePermission rel = new SysRolePermission();
        rel.setRoleId(id);
        rel.setPermissionId(perm.getId());
        rolePermissionMapper.insert(rel);
      }
    }
    replaceDepts(id, dto.getDeptIds());
  }

  @Override
  @Transactional
  public Long copy(Long id) {
    SysRoleVO source = detail(id);
    SysRoleSaveDTO dto = new SysRoleSaveDTO();
    dto.setCode(uniqueCopyCode(source.getCode()));
    dto.setName(source.getName() + " 副本");
    dto.setDescription(source.getDescription());
    dto.setDataScope(source.getDataScope());
    dto.setStatus("active");
    dto.setSortNo(source.getSortNo() == null ? 0 : source.getSortNo() + 1);
    dto.setDeptIds(source.getDeptIds());
    Long newId = create(dto);
    SysRoleGrantDTO grant = new SysRoleGrantDTO();
    grant.setPermissionCodes(source.getPermissionCodes());
    grant.setDataScope(source.getDataScope());
    grant.setDeptIds(source.getDeptIds());
    grant(newId, grant);
    return newId;
  }

  @Override
  public List<SysUserVO> members(Long id) {
    require(id);
    List<Long> userIds = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, id))
        .stream()
        .map(SysUserRole::getUserId)
        .toList();
    if (userIds.isEmpty()) {
      return List.of();
    }
    return userMapper.selectBatchIds(userIds).stream().map(this::toMember).toList();
  }

  private String uniqueCopyCode(String code) {
    String base = code + "_copy";
    String next = base;
    int i = 1;
    while (roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getCode, next)) > 0) {
      next = base + i++;
    }
    return next;
  }

  private SysUserVO toMember(SysUser user) {
    SysUserVO vo = new SysUserVO();
    vo.setId(user.getId());
    vo.setUsername(user.getUsername());
    vo.setDisplayName(user.getDisplayName());
    vo.setStatus(user.getStatus());
    vo.setDeptId(user.getDeptId());
    if (user.getDeptId() != null) {
      SysDept dept = deptMapper.selectById(user.getDeptId());
      vo.setDeptName(dept == null ? null : dept.getName());
    }
    return vo;
  }

  private void fill(SysRole role, SysRoleSaveDTO dto) {
    role.setCode(dto.getCode());
    role.setName(dto.getName());
    role.setDescription(dto.getDescription());
    role.setDataScope(StrUtil.blankToDefault(dto.getDataScope(), "SELF"));
    role.setStatus(StrUtil.blankToDefault(dto.getStatus(), "active"));
    role.setSortNo(dto.getSortNo() == null ? 0 : dto.getSortNo());
  }

  private void replaceDepts(Long roleId, List<Long> deptIds) {
    roleDeptMapper.delete(Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, roleId));
    if (deptIds == null) {
      return;
    }
    for (Long deptId : deptIds) {
      SysRoleDept rel = new SysRoleDept();
      rel.setRoleId(roleId);
      rel.setDeptId(deptId);
      roleDeptMapper.insert(rel);
    }
  }

  private void assertCodeUnique(String code, Long excludeId) {
    Long count = roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery()
        .eq(SysRole::getCode, code)
        .ne(excludeId != null, SysRole::getId, excludeId));
    if (count != null && count > 0) {
      throw BizException.conflict("角色编码已存在");
    }
  }

  private SysRole require(Long id) {
    SysRole role = roleMapper.selectById(id);
    if (role == null) {
      throw BizException.notFound("角色不存在");
    }
    return role;
  }

  private SysRoleVO toVo(SysRole role) {
    SysRoleVO vo = new SysRoleVO();
    vo.setId(role.getId());
    vo.setCode(role.getCode());
    vo.setName(role.getName());
    vo.setDescription(role.getDescription());
    vo.setBuiltin(Integer.valueOf(1).equals(role.getBuiltin()));
    vo.setDataScope(role.getDataScope());
    vo.setStatus(role.getStatus());
    vo.setSortNo(role.getSortNo());
    vo.setCreateTime(role.getCreateTime());
    vo.setUserCount(userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, role.getId())));
    List<Long> pids = rolePermissionMapper.selectList(
            Wrappers.<SysRolePermission>lambdaQuery().eq(SysRolePermission::getRoleId, role.getId()))
        .stream()
        .map(SysRolePermission::getPermissionId)
        .toList();
    if (!pids.isEmpty()) {
      vo.setPermissionCodes(permissionMapper.selectBatchIds(pids).stream().map(SysPermission::getCode).collect(Collectors.toList()));
    }
    vo.setDeptIds(roleDeptMapper.selectList(Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, role.getId()))
        .stream()
        .map(SysRoleDept::getDeptId)
        .toList());
    return vo;
  }
}
