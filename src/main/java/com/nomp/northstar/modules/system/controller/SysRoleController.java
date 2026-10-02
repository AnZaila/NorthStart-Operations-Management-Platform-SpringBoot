package com.nomp.northstar.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.system.model.dto.SysRoleGrantDTO;
import com.nomp.northstar.modules.system.model.dto.SysRoleSaveDTO;
import com.nomp.northstar.modules.system.model.vo.SysRoleVO;
import com.nomp.northstar.modules.system.model.vo.SysUserVO;
import com.nomp.northstar.modules.system.service.ISysRoleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/roles")
@RequiredArgsConstructor
public class SysRoleController {
  private final ISysRoleService roleService;

  @GetMapping
  @SaCheckPermission(Perms.ROLE_VIEW)
  public R<List<SysRoleVO>> list() {
    return R.ok(roleService.listAll());
  }

  @GetMapping("/{id}")
  @SaCheckPermission(Perms.ROLE_VIEW)
  public R<SysRoleVO> detail(@PathVariable Long id) {
    return R.ok(roleService.detail(id));
  }

  @PostMapping
  @SaCheckPermission(Perms.ROLE_CREATE)
  @OperLog(module = "角色", action = "新建", resource = "role")
  public R<Map<String, Long>> create(@Valid @RequestBody SysRoleSaveDTO dto) {
    return R.ok(Map.of("id", roleService.create(dto)));
  }

  @PutMapping("/{id}")
  @SaCheckPermission(Perms.ROLE_UPDATE)
  @OperLog(module = "角色", action = "编辑", resource = "role")
  public R<Void> update(@PathVariable Long id, @Valid @RequestBody SysRoleSaveDTO dto) {
    roleService.update(id, dto);
    return R.ok();
  }

  @PutMapping("/{id}/permissions")
  @SaCheckPermission(Perms.ROLE_GRANT)
  @OperLog(module = "角色", action = "授权", resource = "role")
  public R<Void> grant(@PathVariable Long id, @RequestBody SysRoleGrantDTO dto) {
    roleService.grant(id, dto);
    return R.ok();
  }

  @DeleteMapping("/{id}")
  @SaCheckPermission(Perms.ROLE_DELETE)
  @OperLog(module = "角色", action = "删除", resource = "role")
  public R<Void> remove(@PathVariable Long id) {
    roleService.remove(id);
    return R.ok();
  }

  @PostMapping("/{id}/copy")
  @SaCheckPermission(Perms.ROLE_CREATE)
  @OperLog(module = "角色", action = "复制", resource = "role")
  public R<Map<String, Long>> copy(@PathVariable Long id) {
    return R.ok(Map.of("id", roleService.copy(id)));
  }

  @GetMapping("/{id}/users")
  @SaCheckPermission(Perms.ROLE_VIEW)
  public R<List<SysUserVO>> members(@PathVariable Long id) {
    return R.ok(roleService.members(id));
  }
}
