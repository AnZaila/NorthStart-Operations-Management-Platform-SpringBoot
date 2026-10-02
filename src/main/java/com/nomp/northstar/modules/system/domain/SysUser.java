package com.nomp.northstar.modules.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nomp.northstar.common.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
  private String username;
  private String password;
  private String displayName;
  private String email;
  private String phone;
  private String avatar;
  private String status;
  private Long deptId;
  private Long postId;
  private Integer mustChangePwd;
  private LocalDateTime passwordChangedAt;
  private Integer loginFailCount;
  private LocalDateTime lockedUntil;
  private LocalDateTime lastLoginAt;
  private String lastLoginIp;
  private String remark;
}
