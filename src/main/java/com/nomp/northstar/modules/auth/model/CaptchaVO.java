package com.nomp.northstar.modules.auth.model;

import lombok.Data;

@Data
public class CaptchaVO {
  private String captchaId;
  private String question;
  private boolean required;
}
