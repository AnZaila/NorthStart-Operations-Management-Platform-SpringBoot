package com.nomp.northstar.modules.org.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nomp.northstar.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class SysPost extends BaseEntity {
  private Long deptId;
  private String name;
  private String code;
  private String level;
  private Integer headcount;
  private String status;
  private String remark;
}
