package com.nomp.northstar.modules.notify.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnouncementSaveDTO {
  @NotBlank(message = "请输入标题")
  private String title;
  private String content;
  private String type;
}
