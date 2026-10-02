package com.nomp.northstar.modules.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordDTO {
  @NotBlank(message = "请输入原密码")
  private String oldPassword;

  @NotBlank(message = "请输入新密码")
  private String newPassword;
}
