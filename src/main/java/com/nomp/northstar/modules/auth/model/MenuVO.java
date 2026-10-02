package com.nomp.northstar.modules.auth.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MenuVO {
  private Long id;
  private Long parentId;
  private String type;
  private String name;
  private String path;
  private String component;
  private String icon;
  private String permission;
  private Boolean visible;
  private Boolean hidden;
  private Integer sortNo;
  private List<MenuVO> children = new ArrayList<>();
}
