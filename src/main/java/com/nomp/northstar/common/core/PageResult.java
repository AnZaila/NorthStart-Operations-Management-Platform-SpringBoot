package com.nomp.northstar.common.core;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
  private List<T> items;
  private long total;
  private int page;
  private int pageSize;
  private Object stats;

  public static <T> PageResult<T> of(List<T> items, long total, long page, long pageSize) {
    return new PageResult<>(items, total, (int) page, (int) pageSize, null);
  }

  public static <T> PageResult<T> of(List<T> items, long total, long page, long pageSize, Object stats) {
    return new PageResult<>(items, total, (int) page, (int) pageSize, stats);
  }
}
