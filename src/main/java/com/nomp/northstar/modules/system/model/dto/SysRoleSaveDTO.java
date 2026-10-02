package com.nomp.northstar.modules.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SysRoleSaveDTO {
  @NotBlank(message = "请输入角色编码")
  private String code;
  @NotBlank(message = "请输入角色名称")
  private String name;
  private String description;
  private String dataScope;
  private String status;
  private Integer sortNo;
  private List<Long> deptIds = new ArrayList<>();
}
