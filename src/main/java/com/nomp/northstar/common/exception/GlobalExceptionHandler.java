package com.nomp.northstar.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.nomp.northstar.common.constant.ErrorCode;
import com.nomp.northstar.common.core.R;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BizException.class)
  public ResponseEntity<R<Object>> handleBiz(BizException ex) {
    return ResponseEntity.status(ex.getStatus()).body(R.fail(ex.getCode(), ex.getMessage(), ex.getData()));
  }

  @ExceptionHandler(NotLoginException.class)
  public ResponseEntity<R<Void>> handleNotLogin(NotLoginException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(R.fail(ErrorCode.UNAUTHORIZED, "未登录或登录已过期"));
  }

  @ExceptionHandler(NotPermissionException.class)
  public ResponseEntity<R<Void>> handleNotPerm(NotPermissionException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(R.fail(ErrorCode.FORBIDDEN, "没有权限执行该操作"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<R<Object>> handleValid(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(R.fail(ErrorCode.VALIDATION, "参数校验失败", Map.of("errors", errors)));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<R<Void>> handle404(NoResourceFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(R.fail(ErrorCode.NOT_FOUND, "资源不存在"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<R<Void>> handleOther(Exception ex) {
    log.error("未处理异常", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(R.fail(ErrorCode.SERVER, "服务暂时不可用"));
  }
}
