package com.nomp.northstar.common.core;

import lombok.Data;

@Data
public class PageQuery {
  private Integer page = 1;
  private Integer pageSize = 20;

  public long current() {
    return page == null || page < 1 ? 1 : page;
  }

  public long size() {
    if (pageSize == null) {
      return 20;
    }
    return Math.min(Math.max(pageSize, 1), 100);
  }
}
