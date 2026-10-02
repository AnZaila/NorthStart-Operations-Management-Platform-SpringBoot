package com.nomp.northstar.modules.dashboard.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.common.security.LoginUser;
import com.nomp.northstar.modules.audit.domain.SysLoginLog;
import com.nomp.northstar.modules.audit.mapper.SysLoginLogMapper;
import com.nomp.northstar.modules.dashboard.model.vo.DashboardVO;
import com.nomp.northstar.modules.dashboard.model.vo.DashboardVO.DualChart;
import com.nomp.northstar.modules.dashboard.model.vo.DashboardVO.Metric;
import com.nomp.northstar.modules.dashboard.model.vo.DashboardVO.NamedValue;
import com.nomp.northstar.modules.dashboard.model.vo.DashboardVO.Task;
import com.nomp.northstar.modules.notify.domain.SysNotice;
import com.nomp.northstar.modules.notify.mapper.SysNoticeMapper;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.mapper.SysUserMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {
  private final SysUserMapper userMapper;
  private final SysLoginLogMapper loginLogMapper;
  private final SysNoticeMapper noticeMapper;

  public DashboardVO overview() {
    LoginUser user = LoginHelper.get();
    DashboardVO vo = new DashboardVO();
    vo.setDisplayName(user.getDisplayName());
    long unread = noticeMapper.selectCount(Wrappers.<SysNotice>lambdaQuery()
        .eq(SysNotice::getUserId, user.getUserId())
        .eq(SysNotice::getReadFlag, 0));
    vo.setTodoCount(unread);
    vo.setPendingApproval(userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getStatus, "pending")));
    LocalDateTime start = LocalDate.now().atStartOfDay();
    vo.setTodayVisits(loginLogMapper.selectCount(Wrappers.<SysLoginLog>lambdaQuery()
        .eq(SysLoginLog::getSuccess, 1)
        .ge(SysLoginLog::getCreateTime, start)));
    long active = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getStatus, "active"));
    long total = Math.max(userMapper.selectCount(null), 1);
    vo.setOnlineRate(String.format("%.1f%%", active * 100.0 / total));

    List<Metric> metrics = new ArrayList<>();
    metrics.add(metric("本周新增用户", String.valueOf(userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
        .ge(SysUser::getCreateTime, LocalDate.now().minusDays(7).atStartOfDay()))), "+0", "up", Perms.DASHBOARD_VIEW));
    metrics.add(metric("待审核账号", String.valueOf(vo.getPendingApproval()), "待处理", "down", Perms.USER_VIEW));
    metrics.add(metric("启用账号", String.valueOf(active), "运行中", "up", Perms.DASHBOARD_VIEW));
    metrics.add(metric("未读通知", String.valueOf(unread), "待查看", "down", Perms.DASHBOARD_VIEW));
    vo.setMetrics(metrics.stream().filter(item -> item.getPermission() == null || user.isSuperAdmin() || user.getPermissions().contains(item.getPermission())).toList());

    List<Task> tasks = new ArrayList<>();
    if (vo.getPendingApproval() > 0 && (user.isSuperAdmin() || user.getPermissions().contains(Perms.USER_VIEW))) {
      Task task = new Task();
      task.setTitle("审核待开通账号");
      task.setDesc("当前有 " + vo.getPendingApproval() + " 个账号待启用。");
      task.setTag("高");
      task.setType("danger");
      tasks.add(task);
    }
    if (unread > 0) {
      Task task = new Task();
      task.setTitle("处理未读通知");
      task.setDesc("你有 " + unread + " 条未读消息。");
      task.setTag("中");
      task.setType("warning");
      tasks.add(task);
    }
    vo.setTasks(tasks);
    fillCharts(vo);
    return vo;
  }

  private void fillCharts(DashboardVO vo) {
    List<String> days = new ArrayList<>();
    List<Long> values = new ArrayList<>();
    List<Long> ok = new ArrayList<>();
    List<Long> fail = new ArrayList<>();
    for (int i = 6; i >= 0; i--) {
      LocalDate day = LocalDate.now().minusDays(i);
      days.add(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.CHINA));
      LocalDateTime from = day.atStartOfDay();
      LocalDateTime to = day.plusDays(1).atStartOfDay();
      long success = loginLogMapper.selectCount(Wrappers.<SysLoginLog>lambdaQuery()
          .eq(SysLoginLog::getSuccess, 1).ge(SysLoginLog::getCreateTime, from).lt(SysLoginLog::getCreateTime, to));
      long failed = loginLogMapper.selectCount(Wrappers.<SysLoginLog>lambdaQuery()
          .eq(SysLoginLog::getSuccess, 0).ge(SysLoginLog::getCreateTime, from).lt(SysLoginLog::getCreateTime, to));
      values.add(success);
      ok.add(success);
      fail.add(failed);
    }
    vo.getTraffic().setDays(days);
    vo.getTraffic().setValues(values);
    DualChart orders = new DualChart();
    orders.setDays(days);
    orders.setCompleted(ok);
    orders.setPending(fail);
    vo.setOrders(orders);
    vo.setSources(List.of(
        named("直接访问", 42),
        named("搜索引擎", 28),
        named("内容推荐", 18),
        named("外部链接", 12)));
  }

  private Metric metric(String label, String value, String trend, String type, String permission) {
    Metric metric = new Metric();
    metric.setLabel(label);
    metric.setValue(value);
    metric.setTrend(trend);
    metric.setTrendType(type);
    metric.setPermission(permission);
    return metric;
  }

  private NamedValue named(String name, long value) {
    NamedValue item = new NamedValue();
    item.setName(name);
    item.setValue(value);
    return item;
  }
}
