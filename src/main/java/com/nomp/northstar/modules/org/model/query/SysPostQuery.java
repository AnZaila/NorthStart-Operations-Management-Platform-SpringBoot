package com.nomp.northstar.modules.org.model.query;

import com.nomp.northstar.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysPostQuery extends PageQuery {
  private String keyword;
  private String status;
  private String level;
  private Long deptId;
}
