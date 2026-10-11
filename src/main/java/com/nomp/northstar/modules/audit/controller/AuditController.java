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
import com.nomp.northstar.common.utils.ExcelWeb;
import com.nomp.northstar.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    LambdaQueryWrapper<SysLoginLog> wrapper = loginWrapper(query);
    Page<SysLoginLog> page = loginLogMapper.selectPage(new Page<>(query.current(), query.size()), wrapper);
    return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
  }

  @GetMapping("/operations")
  @SaCheckPermission(Perms.AUDIT_OPERATE)
  public R<PageResult<SysOperLog>> operations(LogQuery query) {
    LambdaQueryWrapper<SysOperLog> wrapper = operWrapper(query);
    Page<SysOperLog> page = operLogMapper.selectPage(new Page<>(query.current(), query.size()), wrapper);
    return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
  }

  @GetMapping("/logins/export")
  @SaCheckPermission(Perms.AUDIT_LOGIN)
  @OperLog(module = "审计", action = "导出登录日志", resource = "login-log")
  public void exportLogins(LogQuery query, HttpServletResponse response) throws IOException {
    LambdaQueryWrapper<SysLoginLog> wrapper = loginWrapper(query);
    List<SysLoginLog> logs = loginLogMapper.selectList(wrapper.last("limit 5000"));
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    List<List<Object>> rows = new ArrayList<>();
    for (SysLoginLog log : logs) {
      rows.add(List.of(
          log.getUsername() == null ? "" : log.getUsername(),
          Integer.valueOf(1).equals(log.getSuccess()) ? "成功" : "失败",
          log.getIp() == null ? "" : log.getIp(),
          log.getReason() == null ? "" : log.getReason(),
          log.getCreateTime() == null ? "" : fmt.format(log.getCreateTime())));
    }
    ExcelWeb.write(response, "登录日志.xlsx", List.of("账号", "结果", "IP", "说明", "时间"), rows);
  }

  @GetMapping("/operations/export")
  @SaCheckPermission(Perms.AUDIT_OPERATE)
  @OperLog(module = "审计", action = "导出操作日志", resource = "oper-log")
  public void exportOperations(LogQuery query, HttpServletResponse response) throws IOException {
    LambdaQueryWrapper<SysOperLog> wrapper = operWrapper(query);
    List<SysOperLog> logs = operLogMapper.selectList(wrapper.last("limit 5000"));
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    List<List<Object>> rows = new ArrayList<>();
    for (SysOperLog log : logs) {
      rows.add(List.of(
          log.getUsername() == null ? "" : log.getUsername(),
          log.getModule() == null ? "" : log.getModule(),
          log.getAction() == null ? "" : log.getAction(),
          log.getResource() == null ? "" : log.getResource(),
          log.getIp() == null ? "" : log.getIp(),
          Integer.valueOf(1).equals(log.getSuccess()) ? "成功" : "失败",
          log.getDurationMs() == null ? "" : log.getDurationMs(),
          log.getCreateTime() == null ? "" : fmt.format(log.getCreateTime())));
    }
    ExcelWeb.write(response, "操作日志.xlsx", List.of("操作人", "模块", "动作", "资源", "IP", "结果", "耗时(ms)", "时间"), rows);
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

  private LambdaQueryWrapper<SysLoginLog> loginWrapper(LogQuery query) {
    LambdaQueryWrapper<SysLoginLog> wrapper = Wrappers.lambdaQuery();
    if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
      wrapper.like(SysLoginLog::getUsername, query.getKeyword());
    }
    if (query.getSuccess() != null) {
      wrapper.eq(SysLoginLog::getSuccess, query.getSuccess());
    }
    applySelf(wrapper);
    wrapper.orderByDesc(SysLoginLog::getId);
    return wrapper;
  }

  private LambdaQueryWrapper<SysOperLog> operWrapper(LogQuery query) {
    LambdaQueryWrapper<SysOperLog> wrapper = Wrappers.lambdaQuery();
    if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
      wrapper.and(w -> w.like(SysOperLog::getUsername, query.getKeyword())
          .or().like(SysOperLog::getModule, query.getKeyword())
          .or().like(SysOperLog::getAction, query.getKeyword()));
    }
    applySelfOper(wrapper);
    wrapper.orderByDesc(SysOperLog::getId);
    return wrapper;
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
