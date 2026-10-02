package com.nomp.northstar.modules.system.model.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SysRoleVO {
  private Long id;
  private String code;
  private String name;
  private String description;
  private boolean builtin;
  private String dataScope;
  private String status;
  private Integer sortNo;
  private List<String> permissionCodes = new ArrayList<>();
  private List<Long> deptIds = new ArrayList<>();
  private long userCount;
  private LocalDateTime createTime;
}
