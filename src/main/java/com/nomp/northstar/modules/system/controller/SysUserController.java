package com.nomp.northstar.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.system.model.dto.SysUserSaveDTO;
import com.nomp.northstar.modules.system.model.query.SysUserQuery;
import com.nomp.northstar.modules.system.model.vo.SysUserVO;
import com.nomp.northstar.modules.system.service.ISysUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
public class SysUserController {
  private final ISysUserService userService;

  @GetMapping
  @SaCheckPermission(Perms.USER_VIEW)
  public R<PageResult<SysUserVO>> page(SysUserQuery query) {
    return R.ok(userService.page(query));
  }

  @GetMapping("/{id}")
  @SaCheckPermission(Perms.USER_VIEW)
  public R<SysUserVO> detail(@PathVariable Long id) {
    return R.ok(userService.detail(id));
  }

  @PostMapping
  @SaCheckPermission(Perms.USER_CREATE)
  @OperLog(module = "用户", action = "新建", resource = "user")
  public R<Map<String, Long>> create(@Valid @RequestBody SysUserSaveDTO dto) {
    return R.ok(Map.of("id", userService.create(dto)));
  }

  @PatchMapping("/{id}")
  @SaCheckPermission(Perms.USER_UPDATE)
  @OperLog(module = "用户", action = "编辑", resource = "user")
  public R<Void> update(@PathVariable Long id, @Valid @RequestBody SysUserSaveDTO dto) {
    userService.update(id, dto);
    return R.ok();
  }

  @DeleteMapping("/{id}")
  @SaCheckPermission(Perms.USER_DELETE)
  @OperLog(module = "用户", action = "删除", resource = "user")
  public R<Void> remove(@PathVariable Long id) {
    userService.remove(id);
    return R.ok();
  }

  @PostMapping("/{id}/disable")
  @SaCheckPermission(Perms.USER_DISABLE)
  @OperLog(module = "用户", action = "停用", resource = "user")
  public R<Void> disable(@PathVariable Long id) {
    userService.changeStatus(id, "frozen");
    return R.ok();
  }

  @PostMapping("/{id}/enable")
  @SaCheckPermission(Perms.USER_DISABLE)
  @OperLog(module = "用户", action = "启用", resource = "user")
  public R<Void> enable(@PathVariable Long id) {
    userService.changeStatus(id, "active");
    return R.ok();
  }

  @PostMapping("/{id}/unlock")
  @SaCheckPermission(Perms.USER_UNLOCK)
  @OperLog(module = "用户", action = "解锁", resource = "user")
  public R<Void> unlock(@PathVariable Long id) {
    userService.unlock(id);
    return R.ok();
  }

  @PostMapping("/{id}/reset-password")
  @SaCheckPermission(Perms.USER_RESET)
  @OperLog(module = "用户", action = "重置密码", resource = "user")
  public R<Map<String, String>> reset(@PathVariable Long id) {
    return R.ok(Map.of("temporaryPassword", userService.resetPassword(id)));
  }

  @PutMapping("/{id}/roles")
  @SaCheckPermission(Perms.USER_GRANT)
  @OperLog(module = "用户", action = "分配角色", resource = "user")
  public R<Void> roles(@PathVariable Long id, @RequestBody RoleIds body) {
    userService.grantRoles(id, body.getRoleIds());
    return R.ok();
  }

  @Data
  public static class RoleIds {
    private List<Long> roleIds;
  }
}
