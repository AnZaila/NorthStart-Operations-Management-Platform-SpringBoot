package com.nomp.northstar.modules.auth.model;

import lombok.Data;

@Data
public class TokenVO {
  private String accessToken;
  private String refreshToken;
  private long expiresIn;
  private boolean mustChangePassword;
}
