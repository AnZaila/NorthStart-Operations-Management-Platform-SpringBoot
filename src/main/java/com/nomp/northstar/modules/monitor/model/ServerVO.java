package com.nomp.northstar.modules.monitor.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ServerVO {
  private Os os = new Os();
  private Jvm jvm = new Jvm();
  private Memory memory = new Memory();
  private List<Disk> disks = new ArrayList<>();

  @Data
  public static class Os {
    private String name;
    private String arch;
    private String version;
    private int cpuCount;
    private double systemLoad;
    private double cpuPercent;
  }

  @Data
  public static class Jvm {
    private String name;
    private String version;
    private String vendor;
    private String startTime;
    private String uptime;
    private String home;
    private long pid;
  }

  @Data
  public static class Memory {
    private long heapUsed;
    private long heapMax;
    private long nonHeapUsed;
    private long physicalTotal;
    private long physicalFree;
  }

  @Data
  public static class Disk {
    private String path;
    private String type;
    private long total;
    private long usable;
  }
}
