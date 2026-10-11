package com.nomp.northstar.modules.auth.model;

import lombok.Data;

@Data
public class SecurityPolicyVO {
  private int passwordMinLength;
  private int passwordMaxLength;
  private int passwordHistory;
}
