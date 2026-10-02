package com.nomp.northstar.modules.system.model.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SysRoleGrantDTO {
  private List<String> permissionCodes = new ArrayList<>();
  private String dataScope;
  private List<Long> deptIds = new ArrayList<>();
}
