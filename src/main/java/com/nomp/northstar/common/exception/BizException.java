package com.nomp.northstar.common.exception;

import com.nomp.northstar.common.constant.ErrorCode;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BizException extends RuntimeException {
  private final int code;
  private final HttpStatus status;
  private final Object data;

  public BizException(int code, HttpStatus status, String message, Object data) {
    super(message);
    this.code = code;
    this.status = status;
    this.data = data;
  }

  public static BizException unauthorized(String message) {
    return new BizException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, message, null);
  }

  public static BizException forbidden() {
    return new BizException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "没有权限执行该操作", null);
  }

  public static BizException notFound(String message) {
    return new BizException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, message, null);
  }

  public static BizException conflict(String message) {
    return new BizException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, message, null);
  }

  public static BizException validation(String message) {
    return validation(message, null);
  }

  public static BizException validation(String message, Map<String, String> errors) {
    Object data = errors == null ? null : Map.of("errors", errors);
    return new BizException(ErrorCode.VALIDATION, HttpStatus.UNPROCESSABLE_ENTITY, message, data);
  }
}
