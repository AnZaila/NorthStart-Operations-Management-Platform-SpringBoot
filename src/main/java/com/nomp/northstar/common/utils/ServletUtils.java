package com.nomp.northstar.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class ServletUtils {
  private ServletUtils() {}

  public static HttpServletRequest request() {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attrs == null ? null : attrs.getRequest();
  }

  public static String ip() {
    HttpServletRequest request = request();
    if (request == null) {
      return "";
    }
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    } else {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }

  public static String userAgent() {
    HttpServletRequest request = request();
    return request == null ? "" : request.getHeader("User-Agent");
  }
}
