package com.nomp.northstar.modules.notify.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nomp.northstar.common.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_announcement")
public class SysAnnouncement extends BaseEntity {
  private String title;
  private String content;
  private String type;
  private String status;
  private Long publisherId;
  private String publisherName;
  private LocalDateTime publishTime;
  private Integer recipientCount;
}
