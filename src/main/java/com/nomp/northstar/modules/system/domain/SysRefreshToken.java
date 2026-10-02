package com.nomp.northstar.modules.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_refresh_token")
public class SysRefreshToken {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private String token;
  private LocalDateTime expireTime;
  private Integer revoked;
  private String userAgent;
  private String ip;
  private String device;
  private LocalDateTime createTime;
}
