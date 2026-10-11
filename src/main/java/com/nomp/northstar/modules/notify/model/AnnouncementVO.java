package com.nomp.northstar.modules.notify.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AnnouncementVO {
  private Long id;
  private String title;
  private String content;
  private String type;
  private String status;
  private Long publisherId;
  private String publisherName;
  private LocalDateTime publishTime;
  private Integer recipientCount;
  private LocalDateTime createTime;
}
