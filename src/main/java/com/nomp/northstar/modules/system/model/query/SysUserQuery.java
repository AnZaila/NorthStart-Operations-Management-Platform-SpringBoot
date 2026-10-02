package com.nomp.northstar.modules.system.model.query;

import com.nomp.northstar.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQuery extends PageQuery {
  private String keyword;
  private String status;
  private Long roleId;
  private Long deptId;
}
