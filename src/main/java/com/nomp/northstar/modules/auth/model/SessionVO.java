package com.nomp.northstar.modules.auth.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SessionVO {
  private Long id;
  private Long userId;
  private String username;
  private String displayName;
  private String ip;
  private String userAgent;
  private LocalDateTime loginTime;
  private LocalDateTime expireTime;
  private boolean current;
}
