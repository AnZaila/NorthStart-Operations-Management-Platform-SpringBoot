package com.nomp.northstar.modules.audit.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_oper_log")
public class SysOperLog {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private String username;
  private String module;
  private String action;
  private String resource;
  private String resourceId;
  private String requestId;
  private String ip;
  private Integer success;
  private Long durationMs;
  private String detail;
  private LocalDateTime createTime;
}
