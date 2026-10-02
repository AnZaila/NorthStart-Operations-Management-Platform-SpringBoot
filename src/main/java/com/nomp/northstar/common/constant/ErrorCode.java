package com.nomp.northstar.common.constant;

public final class ErrorCode {
  public static final int OK = 0;
  public static final int UNAUTHORIZED = 40001;
  public static final int FORBIDDEN = 40003;
  public static final int NOT_FOUND = 40004;
  public static final int CONFLICT = 40009;
  public static final int VALIDATION = 40022;
  public static final int SERVER = 50000;

  private ErrorCode() {}
}
