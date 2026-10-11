package com.nomp.northstar.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.system.domain.SysConfig;
import com.nomp.northstar.modules.system.service.SysConfigService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/configs")
@RequiredArgsConstructor
public class SysConfigController {
  private final SysConfigService configService;

  @GetMapping
  @SaCheckPermission(Perms.CONFIG_VIEW)
  public R<List<SysConfig>> list() {
    return R.ok(configService.list());
  }

  @PutMapping
  @SaCheckPermission(Perms.CONFIG_UPDATE)
  @OperLog(module = "参数", action = "更新", resource = "config")
  public R<Void> save(@RequestBody List<SysConfig> configs) {
    configService.save(configs);
    return R.ok();
  }
}
