package com.nomp.northstar.common.utils;

import cn.hutool.crypto.digest.BCrypt;
import java.util.regex.Pattern;

public final class PasswordUtils {
  private static final Pattern UPPER = Pattern.compile("[A-Z]");
  private static final Pattern LOWER = Pattern.compile("[a-z]");
  private static final Pattern DIGIT = Pattern.compile("\\d");
  private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

  private PasswordUtils() {}

  public static String hash(String raw) {
    return BCrypt.hashpw(raw);
  }

  public static boolean matches(String raw, String hashed) {
    return hashed != null && BCrypt.checkpw(raw, hashed);
  }

  public static String validate(String raw, int min, int max) {
    if (raw == null || raw.length() < min || raw.length() > max) {
      return "密码长度需为 " + min + "–" + max + " 位";
    }
    if (!UPPER.matcher(raw).find() || !LOWER.matcher(raw).find() || !DIGIT.matcher(raw).find() || !SPECIAL.matcher(raw).find()) {
      return "密码需包含大小写字母、数字和特殊字符";
    }
    return null;
  }
}
