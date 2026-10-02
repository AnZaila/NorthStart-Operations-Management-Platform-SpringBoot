package com.nomp.northstar.modules.org.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysDeptSaveDTO {
  private Long parentId;
  @NotBlank
  private String name;
  @NotBlank
  private String code;
  private Long leaderId;
  private String phone;
  private String status;
  private Integer sortNo;
  private String remark;
}
