package com.nomp.northstar.modules.auth.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateDTO {
  @NotBlank(message = "请输入姓名")
  private String displayName;

  @Email(message = "邮箱格式不正确")
  private String email;

  private String phone;
  private String avatar;
}
