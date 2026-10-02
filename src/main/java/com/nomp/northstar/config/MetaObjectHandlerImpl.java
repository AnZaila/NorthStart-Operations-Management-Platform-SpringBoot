package com.nomp.northstar.config;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class MetaObjectHandlerImpl implements MetaObjectHandler {
  @Override
  public void insertFill(MetaObject metaObject) {
    LocalDateTime now = LocalDateTime.now();
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
    this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId());
    this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId());
  }

  @Override
  public void updateFill(MetaObject metaObject) {
    this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId());
  }

  private Long currentUserId() {
    try {
      if (StpUtil.isLogin()) {
        return StpUtil.getLoginIdAsLong();
      }
    } catch (Exception ignored) {
      // 启动种子数据或匿名请求
    }
    return 0L;
  }
}
