package com.nomp.northstar.modules.audit.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.DataScope;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.PageQuery;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.common.security.LoginUser;
import com.nomp.northstar.modules.audit.domain.SysLoginLog;
import com.nomp.northstar.modules.audit.domain.SysOperLog;
import com.nomp.northstar.modules.audit.mapper.SysLoginLogMapper;
import com.nomp.northstar.modules.audit.mapper.SysOperLogMapper;
import com.nomp.northstar.modules.auth.model.SessionVO;
import com.nomp.northstar.modules.auth.service.AuthService;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {
  private final SysLoginLogMapper loginLogMapper;
  private final SysOperLogMapper operLogMapper;
  private final AuthService authService;

  @GetMapping("/logins")
  @SaCheckPermission(Perms.AUDIT_LOGIN)
  public R<PageResult<SysLoginLog>> logins(LogQuery query) {
    LambdaQueryWrapper<SysLoginLog> wrapper = Wrappers.lambdaQuery();
    if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
      wrapper.like(SysLoginLog::getUsername, query.getKeyword());
    }
    if (query.getSuccess() != null) {
      wrapper.eq(SysLoginLog::getSuccess, query.getSuccess());
    }
    applySelf(wrapper);
    wrapper.orderByDesc(SysLoginLog::getId);
    Page<SysLoginLog> page = loginLogMapper.selectPage(new Page<>(query.current(), query.size()), wrapper);
    return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
  }

  @GetMapping("/operations")
  @SaCheckPermission(Perms.AUDIT_OPERATE)
  public R<PageResult<SysOperLog>> operations(LogQuery query) {
    LambdaQueryWrapper<SysOperLog> wrapper = Wrappers.lambdaQuery();
    if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
      wrapper.and(w -> w.like(SysOperLog::getUsername, query.getKeyword())
          .or().like(SysOperLog::getModule, query.getKeyword())
          .or().like(SysOperLog::getAction, query.getKeyword()));
    }
    applySelfOper(wrapper);
    wrapper.orderByDesc(SysOperLog::getId);
    Page<SysOperLog> page = operLogMapper.selectPage(new Page<>(query.current(), query.size()), wrapper);
    return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
  }

  @GetMapping("/online")
  @SaCheckPermission(Perms.AUDIT_ONLINE)
  public R<List<SessionVO>> online() {
    return R.ok(authService.sessions(null));
  }

  @PostMapping("/online/{id}/kick")
  @SaCheckPermission(Perms.AUDIT_ONLINE)
  @OperLog(module = "审计", action = "强制下线", resource = "session")
  public R<Void> kick(@PathVariable Long id) {
    authService.kickSession(id, true);
    return R.ok();
  }

  private void applySelf(LambdaQueryWrapper<SysLoginLog> wrapper) {
    LoginUser user = LoginHelper.get();
    if (user.isSuperAdmin() || user.getDataScope() == DataScope.ALL) {
      return;
    }
    if (user.getDataScope() == DataScope.SELF) {
      wrapper.eq(SysLoginLog::getUserId, user.getUserId());
    }
  }

  private void applySelfOper(LambdaQueryWrapper<SysOperLog> wrapper) {
    LoginUser user = LoginHelper.get();
    if (user.isSuperAdmin() || user.getDataScope() == DataScope.ALL) {
      return;
    }
    if (user.getDataScope() == DataScope.SELF) {
      wrapper.eq(SysOperLog::getUserId, user.getUserId());
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class LogQuery extends PageQuery {
    private String keyword;
    private Integer success;
  }
}
