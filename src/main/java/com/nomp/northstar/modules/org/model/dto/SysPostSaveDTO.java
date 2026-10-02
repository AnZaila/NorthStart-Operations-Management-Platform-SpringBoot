package com.nomp.northstar.modules.org.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysPostSaveDTO {
  @NotNull
  private Long deptId;
  @NotBlank
  private String name;
  @NotBlank
  private String code;
  private String level;
  private Integer headcount;
  private String status;
  private String remark;
}
