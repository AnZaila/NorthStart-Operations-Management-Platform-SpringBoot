package com.nomp.northstar.common.core;

import com.nomp.northstar.common.constant.ErrorCode;
import com.nomp.northstar.common.web.RequestIdHolder;
import lombok.Data;

@Data
public class R<T> {
  private int code;
  private String message;
  private T data;
  private String requestId;

  public static <T> R<T> ok(T data) {
    R<T> r = new R<>();
    r.code = ErrorCode.OK;
    r.message = "ok";
    r.data = data;
    r.requestId = RequestIdHolder.get();
    return r;
  }

  public static R<Void> ok() {
    return ok(null);
  }

  public static <T> R<T> fail(int code, String message) {
    return fail(code, message, null);
  }

  public static <T> R<T> fail(int code, String message, T data) {
    R<T> r = new R<>();
    r.code = code;
    r.message = message;
    r.data = data;
    r.requestId = RequestIdHolder.get();
    return r;
  }
}
