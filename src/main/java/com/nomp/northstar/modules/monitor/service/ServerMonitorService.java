package com.nomp.northstar.modules.monitor.service;

import com.nomp.northstar.modules.monitor.model.ServerVO;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class ServerMonitorService {
  private static final DateTimeFormatter TIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  public ServerVO snapshot() {
    ServerVO vo = new ServerVO();
    OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

    vo.getOs().setName(os.getName());
    vo.getOs().setArch(os.getArch());
    vo.getOs().setVersion(os.getVersion());
    vo.getOs().setCpuCount(os.getAvailableProcessors());
    vo.getOs().setSystemLoad(round(Math.max(os.getSystemLoadAverage(), 0)));

    vo.getJvm().setName(runtime.getVmName());
    vo.getJvm().setVersion(runtime.getVmVersion());
    vo.getJvm().setVendor(runtime.getVmVendor());
    vo.getJvm().setStartTime(TIME.format(Instant.ofEpochMilli(runtime.getStartTime())));
    vo.getJvm().setUptime(formatDuration(runtime.getUptime()));
    vo.getJvm().setHome(System.getProperty("java.home"));
    vo.getJvm().setPid(runtime.getPid());

    vo.getMemory().setHeapUsed(memory.getHeapMemoryUsage().getUsed());
    vo.getMemory().setHeapMax(memory.getHeapMemoryUsage().getMax());
    vo.getMemory().setNonHeapUsed(memory.getNonHeapMemoryUsage().getUsed());
    if (os instanceof com.sun.management.OperatingSystemMXBean sunOs) {
      vo.getMemory().setPhysicalTotal(sunOs.getTotalMemorySize());
      vo.getMemory().setPhysicalFree(sunOs.getFreeMemorySize());
      vo.getOs().setCpuPercent(round(Math.max(sunOs.getCpuLoad(), 0) * 100));
    }

    for (FileStore store : FileSystems.getDefault().getFileStores()) {
      try {
        long total = store.getTotalSpace();
        if (total <= 0) {
          continue;
        }
        ServerVO.Disk disk = new ServerVO.Disk();
        disk.setPath(store.name() + " (" + store.toString() + ")");
        disk.setType(store.type());
        disk.setTotal(total);
        disk.setUsable(store.getUsableSpace());
        vo.getDisks().add(disk);
      } catch (Exception ignored) {
        // skip inaccessible volumes
      }
    }
    return vo;
  }

  private static double round(double value) {
    if (value < 0) {
      return 0;
    }
    return Math.round(value * 100.0) / 100.0;
  }

  private static String formatDuration(long millis) {
    Duration duration = Duration.ofMillis(millis);
    long days = duration.toDays();
    long hours = duration.toHours() % 24;
    long minutes = duration.toMinutes() % 60;
    return days + " 天 " + hours + " 小时 " + minutes + " 分钟";
  }
}
