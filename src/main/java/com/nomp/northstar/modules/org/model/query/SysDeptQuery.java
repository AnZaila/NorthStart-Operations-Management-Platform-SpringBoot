package com.nomp.northstar.modules.org.model.query;

import com.nomp.northstar.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysDeptQuery extends PageQuery {
  private String keyword;
  private String status;
  private Long parentId;
}
