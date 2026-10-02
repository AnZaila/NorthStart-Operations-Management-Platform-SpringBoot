package com.nomp.northstar.config;

import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.common.security.LoginUser;
import com.nomp.northstar.common.utils.ServletUtils;
import com.nomp.northstar.common.web.RequestIdHolder;
import com.nomp.northstar.modules.audit.domain.SysOperLog;
import com.nomp.northstar.modules.audit.mapper.SysOperLogMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {
  private final SysOperLogMapper operLogMapper;

  @Around("@annotation(operLog)")
  public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
    long start = System.currentTimeMillis();
    boolean success = true;
    try {
      return point.proceed();
    } catch (Throwable ex) {
      success = false;
      throw ex;
    } finally {
      SysOperLog log = new SysOperLog();
      LoginUser user = LoginHelper.getOrNull();
      if (user != null) {
        log.setUserId(user.getUserId());
        log.setUsername(user.getUsername());
      }
      log.setModule(operLog.module());
      log.setAction(operLog.action());
      log.setResource(operLog.resource());
      Object[] args = point.getArgs();
      if (args != null && args.length > 0 && args[0] instanceof Long) {
        log.setResourceId(String.valueOf(args[0]));
      }
      log.setRequestId(RequestIdHolder.get());
      log.setIp(ServletUtils.ip());
      log.setSuccess(success ? 1 : 0);
      log.setDurationMs(System.currentTimeMillis() - start);
      log.setCreateTime(LocalDateTime.now());
      operLogMapper.insert(log);
    }
  }
}
