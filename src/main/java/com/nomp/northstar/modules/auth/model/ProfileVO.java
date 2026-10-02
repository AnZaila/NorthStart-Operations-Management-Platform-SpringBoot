package com.nomp.northstar.modules.auth.model;

import com.nomp.northstar.common.constant.DataScope;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ProfileVO {
  private Long id;
  private String username;
  private String displayName;
  private String email;
  private String phone;
  private String avatar;
  private String status;
  private Long deptId;
  private String deptName;
  private Long postId;
  private String postName;
  private boolean mustChangePassword;
  private DataScope dataScope;
  private List<String> roles = new ArrayList<>();
  private List<String> roleNames = new ArrayList<>();
  private List<String> permissions = new ArrayList<>();
  private LocalDateTime lastLoginAt;
}
