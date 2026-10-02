package com.nomp.northstar.modules.dashboard.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.dashboard.model.vo.DashboardVO;
import com.nomp.northstar.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
  private final DashboardService dashboardService;

  @GetMapping("/overview")
  @SaCheckPermission(Perms.DASHBOARD_VIEW)
  public R<DashboardVO> overview() {
    return R.ok(dashboardService.overview());
  }
}
