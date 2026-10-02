package com.nomp.northstar.modules.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysMenuSaveDTO {
  private Long parentId;
  @NotBlank
  private String type;
  @NotBlank
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
