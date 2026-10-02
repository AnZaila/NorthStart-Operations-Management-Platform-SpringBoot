package com.nomp.northstar.modules.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SysUserSaveDTO {
  @NotBlank(message = "请输入用户名")
  private String username;
  @NotBlank(message = "请输入姓名")
  private String displayName;
  private String email;
  private String phone;
  private String status;
  private Long deptId;
  private Long postId;
  private String password;
  private String remark;
  private List<Long> roleIds = new ArrayList<>();
}
