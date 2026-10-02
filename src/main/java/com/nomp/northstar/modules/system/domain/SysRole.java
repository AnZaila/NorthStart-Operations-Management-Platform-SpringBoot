package com.nomp.northstar.modules.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nomp.northstar.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
  private String code;
  private String name;
  private String description;
  private Integer builtin;
  private String dataScope;
  private String status;
  private Integer sortNo;
}
