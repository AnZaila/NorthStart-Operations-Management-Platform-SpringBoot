package com.nomp.northstar.modules.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nomp.northstar.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
  private Long parentId;
  private String type;
  private String name;
  private String path;
  private String component;
  private String icon;
  private String permission;
  private Integer visible;
  private Integer hidden;
  private Integer sortNo;
  private String status;
}
