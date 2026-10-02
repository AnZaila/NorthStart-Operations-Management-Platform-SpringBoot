package com.nomp.northstar.modules.org.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nomp.northstar.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {
  private Long parentId;
  private String name;
  private String code;
  private Long leaderId;
  private String phone;
  private String status;
  private Integer sortNo;
  private String remark;
}
