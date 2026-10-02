package com.nomp.northstar.modules.org.model.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysDeptVO {
  private Long id;
  private Long parentId;
  private String parentName;
  private String name;
  private String code;
  private Long leaderId;
  private String leaderName;
  private String phone;
  private String status;
  private Integer sortNo;
  private long memberCount;
  private String remark;
  private LocalDateTime createTime;
}
