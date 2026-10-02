package com.nomp.northstar.modules.notify.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_notice")
public class SysNotice {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private String title;
  private String content;
  private String type;
  private Integer readFlag;
  private LocalDateTime createTime;
}
