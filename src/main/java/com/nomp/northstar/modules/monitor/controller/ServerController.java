package com.nomp.northstar.modules.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.monitor.model.ServerVO;
import com.nomp.northstar.modules.monitor.service.ServerMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/monitor/server")
@RequiredArgsConstructor
public class ServerController {
  private final ServerMonitorService serverMonitorService;

  @GetMapping
  @SaCheckPermission(Perms.MONITOR_SERVER)
  public R<ServerVO> snapshot() {
    return R.ok(serverMonitorService.snapshot());
  }
}
