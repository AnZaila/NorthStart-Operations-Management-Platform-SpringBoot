package com.nomp.northstar.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.system.domain.SysConfig;
import com.nomp.northstar.modules.system.mapper.SysConfigMapper;
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
  private final SysConfigMapper configMapper;

  @GetMapping
  @SaCheckPermission(Perms.CONFIG_VIEW)
  public R<List<SysConfig>> list() {
    return R.ok(configMapper.selectList(Wrappers.<SysConfig>lambdaQuery().orderByAsc(SysConfig::getConfigKey)));
  }

  @PutMapping
  @SaCheckPermission(Perms.CONFIG_UPDATE)
  @OperLog(module = "参数", action = "更新", resource = "config")
  public R<Void> save(@RequestBody List<SysConfig> configs) {
    for (SysConfig config : configs) {
      if (config.getId() == null) {
        configMapper.insert(config);
      } else {
        configMapper.updateById(config);
      }
    }
    return R.ok();
  }
}
