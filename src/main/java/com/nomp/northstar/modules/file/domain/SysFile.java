package com.nomp.northstar.modules.file.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_file")
public class SysFile {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String originalName;
  private String storedName;
  private String contentType;
  private Long sizeBytes;
  private String bizType;
  private Long userId;
  private LocalDateTime createTime;
}
