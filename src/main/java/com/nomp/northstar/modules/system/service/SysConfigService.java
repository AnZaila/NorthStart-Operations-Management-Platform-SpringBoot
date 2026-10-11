package com.nomp.northstar.modules.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.constant.ConfigKeys;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.config.NorthstarProperties;
import com.nomp.northstar.modules.auth.model.SecurityPolicyVO;
import com.nomp.northstar.modules.system.domain.SysConfig;
import com.nomp.northstar.modules.system.mapper.SysConfigMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysConfigService {
  private final SysConfigMapper configMapper;
  private final NorthstarProperties properties;
  private final Map<String, String> cache = new ConcurrentHashMap<>();
  private volatile boolean loaded;

  public synchronized void reload() {
    Map<String, String> next = new ConcurrentHashMap<>();
    for (SysConfig row : configMapper.selectList(Wrappers.<SysConfig>lambdaQuery())) {
      if (StrUtil.isNotBlank(row.getConfigKey())) {
        next.put(row.getConfigKey(), StrUtil.nullToEmpty(row.getConfigValue()));
      }
    }
    cache.clear();
    cache.putAll(next);
    loaded = true;
  }

  public List<SysConfig> list() {
    return configMapper.selectList(Wrappers.<SysConfig>lambdaQuery().orderByAsc(SysConfig::getConfigKey));
  }

  @Transactional
  public void save(List<SysConfig> configs) {
    if (configs == null || configs.isEmpty()) {
      throw BizException.validation("没有可保存的参数");
    }
    Map<String, String> pending = new LinkedHashMap<>();
    for (SysConfig incoming : configs) {
      if (incoming.getId() == null) {
        continue;
      }
      SysConfig existing = configMapper.selectById(incoming.getId());
      if (existing == null) {
        throw BizException.notFound("参数不存在");
      }
      pending.put(existing.getConfigKey(), StrUtil.trim(incoming.getConfigValue()));
    }
    int minLen = parseInt(ConfigKeys.PASSWORD_MIN_LENGTH, pending.get(ConfigKeys.PASSWORD_MIN_LENGTH),
        properties.getSecurity().getPasswordMinLength(), 4, 32);
    int maxLen = parseInt(ConfigKeys.PASSWORD_MAX_LENGTH, pending.get(ConfigKeys.PASSWORD_MAX_LENGTH),
        properties.getSecurity().getPasswordMaxLength(), 8, 64);
    if (maxLen < minLen) {
      throw BizException.validation("密码最大长度不能小于最小长度");
    }
    parseInt(ConfigKeys.PASSWORD_HISTORY, pending.get(ConfigKeys.PASSWORD_HISTORY),
        properties.getSecurity().getPasswordHistory(), 0, 20);
    parseInt(ConfigKeys.LOGIN_MAX_FAILURES, pending.get(ConfigKeys.LOGIN_MAX_FAILURES),
        properties.getSecurity().getMaxLoginFailures(), 1, 20);
    parseInt(ConfigKeys.LOGIN_LOCK_MINUTES, pending.get(ConfigKeys.LOGIN_LOCK_MINUTES),
        properties.getSecurity().getLockMinutes(), 1, 1440);
    parseInt(ConfigKeys.LOGIN_CAPTCHA_AFTER, pending.get(ConfigKeys.LOGIN_CAPTCHA_AFTER),
        properties.getSecurity().getCaptchaAfterFailures(), 0, 20);
    parseInt(ConfigKeys.LOGIN_REFRESH_TTL_DAYS, pending.get(ConfigKeys.LOGIN_REFRESH_TTL_DAYS),
        properties.getSecurity().getRefreshTtlDays(), 1, 30);

    for (SysConfig incoming : configs) {
      if (incoming.getId() == null) {
        continue;
      }
      SysConfig existing = configMapper.selectById(incoming.getId());
      existing.setConfigValue(pending.get(existing.getConfigKey()));
      configMapper.updateById(existing);
    }
    reload();
  }

  public SecurityPolicyVO policy() {
    SecurityPolicyVO vo = new SecurityPolicyVO();
    vo.setPasswordMinLength(passwordMinLength());
    vo.setPasswordMaxLength(passwordMaxLength());
    vo.setPasswordHistory(passwordHistory());
    return vo;
  }

  public int passwordMinLength() {
    return getInt(ConfigKeys.PASSWORD_MIN_LENGTH, properties.getSecurity().getPasswordMinLength(), 4, 32);
  }

  public int passwordMaxLength() {
    int min = passwordMinLength();
    return Math.max(min, getInt(ConfigKeys.PASSWORD_MAX_LENGTH, properties.getSecurity().getPasswordMaxLength(), 8, 64));
  }

  public int passwordHistory() {
    return getInt(ConfigKeys.PASSWORD_HISTORY, properties.getSecurity().getPasswordHistory(), 0, 20);
  }

  public int maxLoginFailures() {
    return getInt(ConfigKeys.LOGIN_MAX_FAILURES, properties.getSecurity().getMaxLoginFailures(), 1, 20);
  }

  public int lockMinutes() {
    return getInt(ConfigKeys.LOGIN_LOCK_MINUTES, properties.getSecurity().getLockMinutes(), 1, 1440);
  }

  public int captchaAfterFailures() {
    return getInt(ConfigKeys.LOGIN_CAPTCHA_AFTER, properties.getSecurity().getCaptchaAfterFailures(), 0, 20);
  }

  public int refreshTtlDays() {
    return getInt(ConfigKeys.LOGIN_REFRESH_TTL_DAYS, properties.getSecurity().getRefreshTtlDays(), 1, 30);
  }

  private int getInt(String key, int fallback, int min, int max) {
    if (!loaded) {
      reload();
    }
    return parseInt(key, cache.get(key), fallback, min, max, false);
  }

  private int parseInt(String key, String raw, int fallback, int min, int max) {
    return parseInt(key, raw, fallback, min, max, true);
  }

  private int parseInt(String key, String raw, int fallback, int min, int max, boolean strict) {
    if (StrUtil.isBlank(raw)) {
      return fallback;
    }
    int value;
    try {
      value = Integer.parseInt(raw.trim());
    } catch (NumberFormatException ex) {
      if (strict) {
        throw BizException.validation(label(key) + " 必须是整数");
      }
      return fallback;
    }
    if (value < min || value > max) {
      if (strict) {
        throw BizException.validation(label(key) + " 需在 " + min + "–" + max + " 之间");
      }
      return fallback;
    }
    return value;
  }

  private String label(String key) {
    return switch (key) {
      case ConfigKeys.PASSWORD_MIN_LENGTH -> "密码最小长度";
      case ConfigKeys.PASSWORD_MAX_LENGTH -> "密码最大长度";
      case ConfigKeys.PASSWORD_HISTORY -> "密码历史次数";
      case ConfigKeys.LOGIN_MAX_FAILURES -> "登录失败锁定阈值";
      case ConfigKeys.LOGIN_LOCK_MINUTES -> "锁定时长";
      case ConfigKeys.LOGIN_CAPTCHA_AFTER -> "验证码出现阈值";
      case ConfigKeys.LOGIN_REFRESH_TTL_DAYS -> "刷新令牌有效天数";
      default -> key;
    };
  }
}
