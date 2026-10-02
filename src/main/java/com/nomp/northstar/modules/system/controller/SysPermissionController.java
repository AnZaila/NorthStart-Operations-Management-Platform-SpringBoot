package com.nomp.northstar.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.modules.system.domain.SysPermission;
import com.nomp.northstar.modules.system.domain.SysRolePermission;
import com.nomp.northstar.modules.system.mapper.SysPermissionMapper;
import com.nomp.northstar.modules.system.mapper.SysRolePermissionMapper;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.Data;
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
@RequestMapping("/api/v1/system/permissions")
@RequiredArgsConstructor
public class SysPermissionController {
  private final SysPermissionMapper permissionMapper;
  private final SysRolePermissionMapper rolePermissionMapper;

  @GetMapping
  @SaCheckPermission(Perms.PERMISSION_VIEW)
  public R<List<SysPermission>> list() {
    return R.ok(permissionMapper.selectList(Wrappers.<SysPermission>lambdaQuery().orderByAsc(SysPermission::getCode)));
  }

  @PostMapping
  @SaCheckPermission(Perms.PERMISSION_CREATE)
  @OperLog(module = "权限点", action = "新建", resource = "permission")
  public R<Map<String, Long>> create(@RequestBody PermBody body) {
    if (permissionMapper.selectCount(Wrappers.<SysPermission>lambdaQuery().eq(SysPermission::getCode, body.getCode())) > 0) {
      throw BizException.conflict("权限编码已存在");
    }
    SysPermission permission = new SysPermission();
    permission.setCode(body.getCode());
    permission.setName(body.getName());
    permission.setType(StrUtil.blankToDefault(body.getType(), "API"));
    permissionMapper.insert(permission);
    return R.ok(Map.of("id", permission.getId()));
  }

  @PutMapping("/{id}")
  @SaCheckPermission(Perms.PERMISSION_UPDATE)
  public R<Void> update(@PathVariable Long id, @RequestBody PermBody body) {
    SysPermission permission = permissionMapper.selectById(id);
    if (permission == null) {
      throw BizException.notFound("权限点不存在");
    }
    permission.setName(body.getName());
    permission.setType(body.getType());
    permissionMapper.updateById(permission);
    return R.ok();
  }

  @DeleteMapping("/{id}")
  @SaCheckPermission(Perms.PERMISSION_DELETE)
  public R<Void> remove(@PathVariable Long id) {
    Long used = rolePermissionMapper.selectCount(Wrappers.<SysRolePermission>lambdaQuery().eq(SysRolePermission::getPermissionId, id));
    if (used != null && used > 0) {
      throw BizException.conflict("权限点已被角色使用，无法删除");
    }
    permissionMapper.deleteById(id);
    return R.ok();
  }

  @Data
  public static class PermBody {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String type;
  }
}
