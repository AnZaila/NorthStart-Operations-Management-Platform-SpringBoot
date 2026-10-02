package com.nomp.northstar.modules.file.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FileVO {
  private Long id;
  private String originalName;
  private String url;
  private String contentType;
  private Long sizeBytes;
  private String bizType;
  private Long userId;
  private String uploader;
  private LocalDateTime createTime;
}
