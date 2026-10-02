package com.nomp.northstar.modules.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nomp.northstar.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {
  private String configKey;
  private String configValue;
  private String remark;
}
