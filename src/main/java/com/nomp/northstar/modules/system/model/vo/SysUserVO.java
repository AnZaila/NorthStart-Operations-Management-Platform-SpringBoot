package com.nomp.northstar.modules.system.model.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SysUserVO {
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
  private List<Long> roleIds = new ArrayList<>();
  private List<String> roleCodes = new ArrayList<>();
  private List<String> roleNames = new ArrayList<>();
  private LocalDateTime lastLoginAt;
  private LocalDateTime createTime;
  private String remark;
  private boolean mustChangePassword;
  private boolean locked;
}
