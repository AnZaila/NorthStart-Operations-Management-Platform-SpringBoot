package com.nomp.northstar.common.security;

import cn.dev33.satoken.stp.StpUtil;
import com.nomp.northstar.common.exception.BizException;

public final class LoginHelper {
  public static final String KEY = "loginUser";

  private LoginHelper() {}

  public static void set(LoginUser user) {
    StpUtil.getSession().set(KEY, user);
  }

  public static LoginUser get() {
    if (!StpUtil.isLogin()) {
      throw BizException.unauthorized("未登录或登录已过期");
    }
    Object value = StpUtil.getSession().get(KEY);
    if (value instanceof LoginUser) {
      return (LoginUser) value;
    }
    throw BizException.unauthorized("登录状态已失效，请重新登录");
  }

  public static LoginUser getOrNull() {
    if (!StpUtil.isLogin()) {
      return null;
    }
    Object value = StpUtil.getSession().get(KEY);
    return value instanceof LoginUser ? (LoginUser) value : null;
  }

  public static Long userId() {
    return get().getUserId();
  }
}
