package com.nomp.northstar.modules.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_password_history")
public class SysPasswordHistory {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private String password;
  private LocalDateTime createTime;
}
