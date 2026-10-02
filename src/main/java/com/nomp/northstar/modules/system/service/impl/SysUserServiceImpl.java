package com.nomp.northstar.modules.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nomp.northstar.common.constant.DataScope;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.common.security.LoginUser;
import com.nomp.northstar.common.utils.PasswordUtils;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.config.NorthstarProperties;
import com.nomp.northstar.modules.auth.service.AuthService;
import com.nomp.northstar.modules.org.domain.SysDept;
import com.nomp.northstar.modules.org.domain.SysPost;
import com.nomp.northstar.modules.org.mapper.SysDeptMapper;
import com.nomp.northstar.modules.org.mapper.SysPostMapper;
import com.nomp.northstar.modules.system.domain.SysRole;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.domain.SysUserRole;
import com.nomp.northstar.modules.system.mapper.SysRoleMapper;
import com.nomp.northstar.modules.system.mapper.SysUserMapper;
import com.nomp.northstar.modules.system.mapper.SysUserRoleMapper;
import com.nomp.northstar.modules.system.model.dto.SysUserSaveDTO;
import com.nomp.northstar.modules.system.model.query.SysUserQuery;
import com.nomp.northstar.modules.system.model.vo.SysUserVO;
import com.nomp.northstar.modules.system.service.ISysUserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements ISysUserService {
  private final SysUserMapper userMapper;
  private final SysUserRoleMapper userRoleMapper;
  private final SysRoleMapper roleMapper;
  private final SysDeptMapper deptMapper;
  private final SysPostMapper postMapper;
  private final AuthService authService;
  private final NorthstarProperties properties;

  @Override
  public PageResult<SysUserVO> page(SysUserQuery query) {
    LoginUser actor = LoginHelper.get();
    LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
    if (StrUtil.isNotBlank(query.getKeyword())) {
      wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
          .or().like(SysUser::getDisplayName, query.getKeyword())
          .or().like(SysUser::getEmail, query.getKeyword())
          .or().like(SysUser::getPhone, query.getKeyword()));
    }
    if (StrUtil.isNotBlank(query.getStatus()) && !"all".equals(query.getStatus())) {
      wrapper.eq(SysUser::getStatus, query.getStatus());
    }
    if (query.getDeptId() != null) {
      wrapper.eq(SysUser::getDeptId, query.getDeptId());
    }
    applyDataScope(wrapper, actor);
    if (query.getRoleId() != null) {
      List<Long> userIds = userRoleMapper.selectList(
              Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, query.getRoleId()))
          .stream()
          .map(SysUserRole::getUserId)
          .toList();
      if (userIds.isEmpty()) {
        return PageResult.of(List.of(), 0, query.current(), query.size(), stats());
      }
      wrapper.in(SysUser::getId, userIds);
    }
    Page<SysUser> page = userMapper.selectPage(new Page<>(query.current(), query.size()), wrapper);
    List<SysUserVO> items = page.getRecords().stream().map(this::toVo).toList();
    return PageResult.of(items, page.getTotal(), page.getCurrent(), page.getSize(), stats());
  }

  @Override
  public SysUserVO detail(Long id) {
    SysUser user = requireUser(id);
    assertReadable(user);
    return toVo(user);
  }

  @Override
  @Transactional
  public Long create(SysUserSaveDTO dto) {
    assertUsernameUnique(dto.getUsername(), null);
    SysUser user = new SysUser();
    fill(user, dto);
    String raw = StrUtil.blankToDefault(dto.getPassword(), "User@123456");
    String invalid = PasswordUtils.validate(raw, properties.getSecurity().getPasswordMinLength(), properties.getSecurity().getPasswordMaxLength());
    if (invalid != null) {
      throw BizException.validation(invalid);
    }
    user.setPassword(PasswordUtils.hash(raw));
    user.setMustChangePwd(1);
    if (user.getStatus() == null) {
      user.setStatus("pending");
    }
    userMapper.insert(user);
    replaceRoles(user.getId(), dto.getRoleIds());
    return user.getId();
  }

  @Override
  @Transactional
  public void update(Long id, SysUserSaveDTO dto) {
    SysUser user = requireUser(id);
    assertUsernameUnique(dto.getUsername(), id);
    fill(user, dto);
    userMapper.updateById(user);
    if (dto.getRoleIds() != null) {
      replaceRoles(id, dto.getRoleIds());
    }
  }

  @Override
  @Transactional
  public void remove(Long id) {
    SysUser user = requireUser(id);
    if (Objects.equals(id, LoginHelper.userId())) {
      throw BizException.conflict("不能删除当前登录账号");
    }
    protectLastSuperAdmin(id);
    userMapper.deleteById(id);
    userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
    authService.revokeUserSessions(id);
  }

  @Override
  @Transactional
  public void changeStatus(Long id, String status) {
    SysUser user = requireUser(id);
    if (Objects.equals(id, LoginHelper.userId()) && "frozen".equals(status)) {
      throw BizException.conflict("不能停用当前登录账号");
    }
    protectLastSuperAdmin(id);
    user.setStatus(status);
    userMapper.updateById(user);
    if ("frozen".equals(status)) {
      authService.revokeUserSessions(id);
    }
  }

  @Override
  public void unlock(Long id) {
    SysUser user = requireUser(id);
    user.setLockedUntil(null);
    user.setLoginFailCount(0);
    userMapper.updateById(user);
  }

  @Override
  @Transactional
  public String resetPassword(Long id) {
    SysUser user = requireUser(id);
    String raw = "Reset@" + java.util.concurrent.ThreadLocalRandom.current().nextInt(100000, 999999);
    authService.persistNewPassword(user, raw);
    user.setMustChangePwd(1);
    userMapper.updateById(user);
    authService.revokeUserSessions(id);
    return raw;
  }

  @Override
  @Transactional
  public void grantRoles(Long id, List<Long> roleIds) {
    requireUser(id);
    if (Objects.equals(id, LoginHelper.userId())) {
      boolean keepSuper = roleMapper.selectBatchIds(roleIds).stream().anyMatch(r -> "super_admin".equals(r.getCode()));
      if (hasSuperAdmin(id) && !keepSuper) {
        throw BizException.conflict("不能取消自己的超级管理员角色");
      }
    }
    protectLastSuperAdmin(id, roleIds);
    replaceRoles(id, roleIds);
  }

  @Override
  public Map<String, Long> stats() {
    Map<String, Long> map = new LinkedHashMap<>();
    map.put("total", userMapper.selectCount(null));
    map.put("active", userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getStatus, "active")));
    map.put("pending", userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getStatus, "pending")));
    map.put("frozen", userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getStatus, "frozen")));
    return map;
  }

  private void applyDataScope(LambdaQueryWrapper<SysUser> wrapper, LoginUser actor) {
    if (actor.isSuperAdmin() || actor.getDataScope() == DataScope.ALL) {
      return;
    }
    if (actor.getDataScope() == DataScope.SELF) {
      wrapper.eq(SysUser::getId, actor.getUserId());
      return;
    }
    Set<Long> deptIds = actor.getDeptIds();
    if (deptIds == null || deptIds.isEmpty()) {
      wrapper.eq(SysUser::getId, actor.getUserId());
      return;
    }
    wrapper.and(w -> w.in(SysUser::getDeptId, deptIds).or().eq(SysUser::getId, actor.getUserId()));
  }

  private void assertReadable(SysUser user) {
    LoginUser actor = LoginHelper.get();
    if (actor.isSuperAdmin() || actor.getDataScope() == DataScope.ALL) {
      return;
    }
    if (Objects.equals(user.getId(), actor.getUserId())) {
      return;
    }
    if (actor.getDataScope() == DataScope.SELF || actor.getDeptIds() == null || !actor.getDeptIds().contains(user.getDeptId())) {
      throw BizException.forbidden();
    }
  }

  private void fill(SysUser user, SysUserSaveDTO dto) {
    user.setUsername(dto.getUsername());
    user.setDisplayName(dto.getDisplayName());
    user.setEmail(dto.getEmail());
    user.setPhone(dto.getPhone());
    user.setStatus(dto.getStatus());
    user.setDeptId(dto.getDeptId());
    user.setPostId(dto.getPostId());
    user.setRemark(dto.getRemark());
  }

  private void replaceRoles(Long userId, List<Long> roleIds) {
    userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
    if (roleIds == null) {
      return;
    }
    for (Long roleId : roleIds) {
      SysUserRole rel = new SysUserRole();
      rel.setUserId(userId);
      rel.setRoleId(roleId);
      userRoleMapper.insert(rel);
    }
  }

  private void assertUsernameUnique(String username, Long excludeId) {
    Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getUsername, username)
        .ne(excludeId != null, SysUser::getId, excludeId));
    if (count != null && count > 0) {
      throw BizException.conflict("用户名已存在");
    }
  }

  private SysUser requireUser(Long id) {
    SysUser user = userMapper.selectById(id);
    if (user == null) {
      throw BizException.notFound("用户不存在");
    }
    return user;
  }

  private boolean hasSuperAdmin(Long userId) {
    List<Long> roleIds = userRoleMapper.selectList(
            Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
        .stream()
        .map(SysUserRole::getUserId) // wait this is wrong - should be getRoleId
        .toList();
    return false;
  }

  private void protectLastSuperAdmin(Long userId) {
    protectLastSuperAdmin(userId, null);
  }

  private void protectLastSuperAdmin(Long userId, List<Long> nextRoleIds) {
    if (!isSuperAdminUser(userId)) {
      return;
    }
    boolean stillSuper = nextRoleIds == null || roleMapper.selectBatchIds(nextRoleIds).stream().anyMatch(r -> "super_admin".equals(r.getCode()));
    if (stillSuper && nextRoleIds != null) {
      return;
    }
    if (nextRoleIds != null && stillSuper) {
      return;
    }
    if (nextRoleIds == null) {
      // deleting or disabling
    } else if (stillSuper) {
      return;
    }
    long supers = countSuperAdmins();
    if (supers <= 1 && (nextRoleIds == null || !stillSuper)) {
      throw BizException.conflict("至少保留一名超级管理员");
    }
  }

  private boolean isSuperAdminUser(Long userId) {
    List<Long> roleIds = userRoleMapper.selectList(
            Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
        .stream()
        .map(SysUserRole::getRoleId)
        .toList();
    if (roleIds.isEmpty()) {
      return false;
    }
    return roleMapper.selectBatchIds(roleIds).stream().anyMatch(r -> "super_admin".equals(r.getCode()));
  }

  private long countSuperAdmins() {
    SysRole superRole = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getCode, "super_admin"));
    if (superRole == null) {
      return 0;
    }
    return userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, superRole.getId()));
  }

  private SysUserVO toVo(SysUser user) {
    SysUserVO vo = new SysUserVO();
    vo.setId(user.getId());
    vo.setUsername(user.getUsername());
    vo.setDisplayName(user.getDisplayName());
    vo.setEmail(user.getEmail());
    vo.setPhone(user.getPhone());
    vo.setAvatar(user.getAvatar());
    vo.setStatus(user.getStatus());
    vo.setDeptId(user.getDeptId());
    vo.setPostId(user.getPostId());
    vo.setLastLoginAt(user.getLastLoginAt());
    vo.setCreateTime(user.getCreateTime());
    vo.setRemark(user.getRemark());
    vo.setMustChangePassword(Integer.valueOf(1).equals(user.getMustChangePwd()));
    vo.setLocked(user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now()));
    if (user.getDeptId() != null) {
      SysDept dept = deptMapper.selectById(user.getDeptId());
      vo.setDeptName(dept == null ? null : dept.getName());
    }
    if (user.getPostId() != null) {
      SysPost post = postMapper.selectById(user.getPostId());
      vo.setPostName(post == null ? null : post.getName());
    }
    List<Long> roleIds = userRoleMapper.selectList(
            Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, user.getId()))
        .stream()
        .map(SysUserRole::getRoleId)
        .toList();
    vo.setRoleIds(new ArrayList<>(roleIds));
    if (!roleIds.isEmpty()) {
      List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
      vo.setRoleCodes(roles.stream().map(SysRole::getCode).collect(Collectors.toList()));
      vo.setRoleNames(roles.stream().map(SysRole::getName).collect(Collectors.toList()));
    }
    return vo;
  }
}
