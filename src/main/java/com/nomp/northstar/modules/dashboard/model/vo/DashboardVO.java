package com.nomp.northstar.modules.dashboard.model.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DashboardVO {
  private String displayName;
  private long todoCount;
  private long pendingApproval;
  private long todayVisits;
  private String onlineRate;
  private List<Metric> metrics = new ArrayList<>();
  private List<Task> tasks = new ArrayList<>();
  private Chart traffic = new Chart();
  private List<NamedValue> sources = new ArrayList<>();
  private DualChart orders = new DualChart();

  @Data
  public static class Metric {
    private String label;
    private String value;
    private String trend;
    private String trendType;
    private String permission;
  }

  @Data
  public static class Task {
    private String title;
    private String desc;
    private String tag;
    private String type;
  }

  @Data
  public static class Chart {
    private List<String> days = new ArrayList<>();
    private List<Long> values = new ArrayList<>();
  }

  @Data
  public static class DualChart {
    private List<String> days = new ArrayList<>();
    private List<Long> completed = new ArrayList<>();
    private List<Long> pending = new ArrayList<>();
  }

  @Data
  public static class NamedValue {
    private String name;
    private long value;
  }
}
