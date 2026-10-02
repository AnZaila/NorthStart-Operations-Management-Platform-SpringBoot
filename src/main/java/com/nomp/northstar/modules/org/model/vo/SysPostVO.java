package com.nomp.northstar.modules.org.model.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysPostVO {
  private Long id;
  private Long deptId;
  private String deptName;
  private String name;
  private String code;
  private String level;
  private Integer headcount;
  private long occupied;
  private String status;
  private String remark;
  private LocalDateTime createTime;
}
