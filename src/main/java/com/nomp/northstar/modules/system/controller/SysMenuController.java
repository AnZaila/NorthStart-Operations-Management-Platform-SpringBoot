package com.nomp.northstar.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.auth.model.MenuVO;
import com.nomp.northstar.modules.system.model.dto.SysMenuSaveDTO;
import com.nomp.northstar.modules.system.service.impl.SysMenuServiceImpl;
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
@RequestMapping("/api/v1/system/menus")
@RequiredArgsConstructor
public class SysMenuController {
  private final SysMenuServiceImpl menuService;

  @GetMapping
  @SaCheckPermission(Perms.MENU_VIEW)
  public R<List<MenuVO>> tree() {
    return R.ok(menuService.tree());
  }

  @PostMapping
  @SaCheckPermission(Perms.MENU_CREATE)
  @OperLog(module = "菜单", action = "新建", resource = "menu")
  public R<Map<String, Long>> create(@Valid @RequestBody SysMenuSaveDTO dto) {
    return R.ok(Map.of("id", menuService.create(dto)));
  }

  @PutMapping("/{id}")
  @SaCheckPermission(Perms.MENU_UPDATE)
  @OperLog(module = "菜单", action = "编辑", resource = "menu")
  public R<Void> update(@PathVariable Long id, @Valid @RequestBody SysMenuSaveDTO dto) {
    menuService.update(id, dto);
    return R.ok();
  }

  @DeleteMapping("/{id}")
  @SaCheckPermission(Perms.MENU_DELETE)
  @OperLog(module = "菜单", action = "删除", resource = "menu")
  public R<Void> remove(@PathVariable Long id) {
    menuService.remove(id);
    return R.ok();
  }
}
