package com.nomp.northstar.common.security;

import com.nomp.northstar.common.constant.DataScope;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
public class LoginUser implements Serializable {
  private Long userId;
  private String username;
  private String displayName;
  private Long deptId;
  private boolean superAdmin;
  private DataScope dataScope;
  private Set<String> permissions = new HashSet<>();
  private Set<String> roles = new HashSet<>();
  private Set<Long> deptIds = new HashSet<>();
  private boolean mustChangePwd;
}
