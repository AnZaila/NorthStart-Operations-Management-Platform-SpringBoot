package com.nomp.northstar.modules.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.constant.ErrorCode;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.common.security.LoginUser;
import com.nomp.northstar.common.utils.PasswordUtils;
import com.nomp.northstar.common.utils.ServletUtils;
import com.nomp.northstar.config.NorthstarProperties;
import com.nomp.northstar.modules.auth.model.CaptchaVO;
import com.nomp.northstar.modules.auth.model.ChangePasswordDTO;
import com.nomp.northstar.modules.auth.model.LoginDTO;
import com.nomp.northstar.modules.auth.model.MenuVO;
import com.nomp.northstar.modules.auth.model.ProfileUpdateDTO;
import com.nomp.northstar.modules.auth.model.ProfileVO;
import com.nomp.northstar.modules.auth.model.SessionVO;
import com.nomp.northstar.modules.auth.model.TokenVO;
import com.nomp.northstar.modules.audit.domain.SysLoginLog;
import com.nomp.northstar.modules.audit.mapper.SysLoginLogMapper;
import com.nomp.northstar.modules.org.domain.SysDept;
import com.nomp.northstar.modules.org.domain.SysPost;
import com.nomp.northstar.modules.org.mapper.SysDeptMapper;
import com.nomp.northstar.modules.org.mapper.SysPostMapper;
import com.nomp.northstar.modules.system.domain.SysMenu;
import com.nomp.northstar.modules.system.domain.SysPasswordHistory;
import com.nomp.northstar.modules.system.domain.SysRefreshToken;
import com.nomp.northstar.modules.system.domain.SysRole;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.domain.SysUserRole;
import com.nomp.northstar.modules.system.mapper.SysMenuMapper;
import com.nomp.northstar.modules.system.mapper.SysPasswordHistoryMapper;
import com.nomp.northstar.modules.system.mapper.SysRefreshTokenMapper;
import com.nomp.northstar.modules.system.mapper.SysRoleMapper;
import com.nomp.northstar.modules.system.mapper.SysUserMapper;
import com.nomp.northstar.modules.system.mapper.SysUserRoleMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final SysUserMapper userMapper;
  private final SysMenuMapper menuMapper;
  private final SysDeptMapper deptMapper;
  private final SysPostMapper postMapper;
  private final SysRoleMapper roleMapper;
  private final SysUserRoleMapper userRoleMapper;
  private final SysRefreshTokenMapper refreshTokenMapper;
  private final SysPasswordHistoryMapper passwordHistoryMapper;
  private final SysLoginLogMapper loginLogMapper;
  private final LoginUserAssembler assembler;
  private final NorthstarProperties properties;

  private record Captcha(String answer, LocalDateTime expireAt) {}

  private final Map<String, Captcha> captchaStore = new ConcurrentHashMap<>();

  public CaptchaVO captcha(boolean required) {
    int a = RandomUtil.randomInt(1, 9);
    int b = RandomUtil.randomInt(1, 9);
    CaptchaVO vo = new CaptchaVO();
    vo.setCaptchaId(IdUtil.fastSimpleUUID());
    vo.setQuestion(a + " + " + b + " = ?");
    vo.setRequired(required);
    captchaStore.put(vo.getCaptchaId(), new Captcha(String.valueOf(a + b), LocalDateTime.now().plusMinutes(5)));
    return vo;
  }

  @Transactional
  public TokenVO login(LoginDTO dto) {
    SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getUsername, dto.getUsername())
        .or()
        .eq(SysUser::getEmail, dto.getUsername()));
    var sec = properties.getSecurity();
    if (user != null && user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
      writeLoginLog(user, dto.getUsername(), false, "账号已锁定");
      throw BizException.conflict("账号已锁定，请稍后重试或联系管理员");
    }
    boolean needCaptcha = user != null && user.getLoginFailCount() != null && user.getLoginFailCount() >= sec.getCaptchaAfterFailures();
    if (needCaptcha) {
      verifyCaptcha(dto);
    }
    if (user == null) {
      writeLoginLog(null, dto.getUsername(), false, "用户不存在");
      throw failLogin(null, "用户名或密码错误", needCaptcha);
    }
    if (!PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
      int fails = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
      user.setLoginFailCount(fails);
      if (fails >= sec.getMaxLoginFailures()) {
        user.setLockedUntil(LocalDateTime.now().plusMinutes(sec.getLockMinutes()));
      }
      userMapper.updateById(user);
      writeLoginLog(user, dto.getUsername(), false, "密码错误");
      throw failLogin(user, "用户名或密码错误", fails >= sec.getCaptchaAfterFailures());
    }
    if ("frozen".equals(user.getStatus())) {
      writeLoginLog(user, dto.getUsername(), false, "账号已停用");
      throw BizException.forbidden("账号已停用");
    }
    if ("pending".equals(user.getStatus())) {
      writeLoginLog(user, dto.getUsername(), false, "账号待审核");
      throw BizException.forbidden("账号待审核，暂不能登录");
    }
    user.setLoginFailCount(0);
    user.setLockedUntil(null);
    user.setLastLoginAt(LocalDateTime.now());
    user.setLastLoginIp(ServletUtils.ip());
    userMapper.updateById(user);
    writeLoginLog(user, user.getUsername(), true, null);
    return issueTokens(user);
  }

  @Transactional
  public TokenVO refresh(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw BizException.unauthorized("登录已过期，请重新登录");
    }
    SysRefreshToken record = refreshTokenMapper.selectOne(
        Wrappers.<SysRefreshToken>lambdaQuery().eq(SysRefreshToken::getToken, refreshToken));
    if (record == null) {
      throw BizException.unauthorized("登录已过期，请重新登录");
    }
    if (Integer.valueOf(1).equals(record.getRevoked()) || record.getExpireTime().isBefore(LocalDateTime.now())) {
      refreshTokenMapper.update(null, Wrappers.<SysRefreshToken>lambdaUpdate()
          .eq(SysRefreshToken::getUserId, record.getUserId())
          .set(SysRefreshToken::getRevoked, 1));
      throw BizException.unauthorized("登录已过期，请重新登录");
    }
    SysUser user = userMapper.selectById(record.getUserId());
    if (user == null || !"active".equals(user.getStatus())) {
      record.setRevoked(1);
      refreshTokenMapper.updateById(record);
      throw BizException.unauthorized("账号不可用");
    }
    record.setRevoked(1);
    refreshTokenMapper.updateById(record);
    StpUtil.logout(user.getId());
    return issueTokens(user);
  }

  public void logout(String refreshToken) {
    if (StpUtil.isLogin()) {
      StpUtil.logout();
    }
    if (refreshToken != null && !refreshToken.isBlank()) {
      refreshTokenMapper.update(null, Wrappers.<SysRefreshToken>lambdaUpdate()
          .eq(SysRefreshToken::getToken, refreshToken)
          .set(SysRefreshToken::getRevoked, 1));
    }
  }

  public ProfileVO me() {
    LoginUser loginUser = LoginHelper.get();
    SysUser user = userMapper.selectById(loginUser.getUserId());
    ProfileVO vo = toProfile(user, loginUser);
    List<Long> roleIds = userRoleMapper.selectList(
            Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, user.getId()))
        .stream()
        .map(SysUserRole::getRoleId)
        .toList();
    if (!roleIds.isEmpty()) {
      vo.setRoleNames(roleMapper.selectBatchIds(roleIds).stream().map(SysRole::getName).toList());
    }
    return vo;
  }

  public List<MenuVO> menus() {
    LoginUser loginUser = LoginHelper.get();
    List<SysMenu> all = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
        .eq(SysMenu::getStatus, "active")
        .orderByAsc(SysMenu::getSortNo));
    List<SysMenu> allowed = all.stream()
        .filter(menu -> "BUTTON".equals(menu.getType())
            ? false
            : (menu.getPermission() == null
                || menu.getPermission().isBlank()
                || loginUser.isSuperAdmin()
                || loginUser.getPermissions().contains(menu.getPermission())))
        .toList();
    return buildMenuTree(allowed, 0L);
  }

  @Transactional
  public void changePassword(ChangePasswordDTO dto) {
    SysUser user = userMapper.selectById(LoginHelper.userId());
    if (!PasswordUtils.matches(dto.getOldPassword(), user.getPassword())) {
      throw BizException.validation("原密码不正确", Map.of("oldPassword", "原密码不正确"));
    }
    persistNewPassword(user, dto.getNewPassword());
    revokeUserSessions(user.getId());
  }

  public List<SessionVO> sessions(Long userId) {
    var wrapper = Wrappers.<SysRefreshToken>lambdaQuery()
        .eq(SysRefreshToken::getRevoked, 0)
        .gt(SysRefreshToken::getExpireTime, LocalDateTime.now())
        .orderByDesc(SysRefreshToken::getId);
    if (userId != null) {
      wrapper.eq(SysRefreshToken::getUserId, userId);
    }
    String currentRefresh = null;
    String device = StpUtil.isLogin() ? StpUtil.getLoginDevice() : null;
    return refreshTokenMapper.selectList(wrapper).stream().map(item -> toSession(item, device)).toList();
  }

  public void kickSession(Long id, boolean allowAny) {
    SysRefreshToken token = refreshTokenMapper.selectById(id);
    if (token == null) {
      throw BizException.notFound("会话不存在");
    }
    if (!allowAny && !Objects.equals(token.getUserId(), LoginHelper.userId())) {
      throw BizException.forbidden();
    }
    token.setRevoked(1);
    refreshTokenMapper.updateById(token);
    if (StrUtil.isNotBlank(token.getDevice())) {
      StpUtil.logout(token.getUserId(), token.getDevice());
    } else {
      StpUtil.logout(token.getUserId());
    }
  }

  public ProfileVO updateProfile(ProfileUpdateDTO dto) {
    SysUser user = userMapper.selectById(LoginHelper.userId());
    user.setDisplayName(dto.getDisplayName());
    user.setEmail(dto.getEmail());
    user.setPhone(dto.getPhone());
    user.setAvatar(dto.getAvatar());
    userMapper.updateById(user);
    LoginUser loginUser = assembler.assemble(user);
    LoginHelper.set(loginUser);
    return toProfile(user, loginUser);
  }

  public void persistNewPassword(SysUser user, String raw) {
    var sec = properties.getSecurity();
    String invalid = PasswordUtils.validate(raw, sec.getPasswordMinLength(), sec.getPasswordMaxLength());
    if (invalid != null) {
      throw BizException.validation(invalid, Map.of("newPassword", invalid));
    }
    if (PasswordUtils.matches(raw, user.getPassword())) {
      throw BizException.validation("新密码不能与当前密码相同", Map.of("newPassword", "新密码不能与当前密码相同"));
    }
    List<SysPasswordHistory> histories = passwordHistoryMapper.selectList(
        Wrappers.<SysPasswordHistory>lambdaQuery()
            .eq(SysPasswordHistory::getUserId, user.getId())
            .orderByDesc(SysPasswordHistory::getCreateTime)
            .last("limit " + sec.getPasswordHistory()));
    for (SysPasswordHistory history : histories) {
      if (PasswordUtils.matches(raw, history.getPassword())) {
        throw BizException.validation("新密码不能与最近使用过的密码重复", Map.of("newPassword", "新密码不能与最近使用过的密码重复"));
      }
    }
    SysPasswordHistory history = new SysPasswordHistory();
    history.setUserId(user.getId());
    history.setPassword(user.getPassword());
    history.setCreateTime(LocalDateTime.now());
    passwordHistoryMapper.insert(history);
    user.setPassword(PasswordUtils.hash(raw));
    user.setMustChangePwd(0);
    user.setPasswordChangedAt(LocalDateTime.now());
    userMapper.updateById(user);
  }

  public void revokeUserSessions(Long userId) {
    StpUtil.logout(userId);
    refreshTokenMapper.update(null, Wrappers.<SysRefreshToken>lambdaUpdate()
        .eq(SysRefreshToken::getUserId, userId)
        .set(SysRefreshToken::getRevoked, 1));
  }

  private TokenVO issueTokens(SysUser user) {
    LoginUser loginUser = assembler.assemble(user);
    String device = IdUtil.fastSimpleUUID();
    StpUtil.login(user.getId(), device);
    LoginHelper.set(loginUser);
    SysRefreshToken refresh = new SysRefreshToken();
    refresh.setUserId(user.getId());
    refresh.setToken(IdUtil.fastSimpleUUID());
    refresh.setExpireTime(LocalDateTime.now().plusDays(properties.getSecurity().getRefreshTtlDays()));
    refresh.setRevoked(0);
    refresh.setUserAgent(ServletUtils.userAgent());
    refresh.setIp(ServletUtils.ip());
    refresh.setDevice(device);
    refresh.setCreateTime(LocalDateTime.now());
    refreshTokenMapper.insert(refresh);
    TokenVO vo = new TokenVO();
    vo.setAccessToken(StpUtil.getTokenValue());
    vo.setRefreshToken(refresh.getToken());
    vo.setExpiresIn(1800);
    vo.setMustChangePassword(Integer.valueOf(1).equals(user.getMustChangePwd()));
    return vo;
  }

  private void verifyCaptcha(LoginDTO dto) {
    if (dto.getCaptchaId() == null || dto.getCaptchaCode() == null) {
      CaptchaVO captcha = captcha(true);
      throw new BizException(40022, org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "请输入验证码",
          Map.of("captchaRequired", true, "captchaId", captcha.getCaptchaId(), "question", captcha.getQuestion()));
    }
    Captcha captcha = captchaStore.remove(dto.getCaptchaId());
    if (captcha == null || captcha.expireAt().isBefore(LocalDateTime.now()) || !captcha.answer().equals(dto.getCaptchaCode().trim())) {
      CaptchaVO next = captcha(true);
      throw new BizException(40022, org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "验证码错误",
          Map.of("captchaRequired", true, "captchaId", next.getCaptchaId(), "question", next.getQuestion()));
    }
  }

  private BizException failLogin(SysUser user, String message, boolean captchaRequired) {
    if (captchaRequired) {
      CaptchaVO captcha = captcha(true);
      return new BizException(ErrorCode.BAD_REQUEST, org.springframework.http.HttpStatus.BAD_REQUEST, message,
          Map.of("captchaRequired", true, "captchaId", captcha.getCaptchaId(), "question", captcha.getQuestion()));
    }
    return BizException.badRequest(message);
  }

  private void writeLoginLog(SysUser user, String username, boolean success, String reason) {
    SysLoginLog log = new SysLoginLog();
    log.setUserId(user == null ? null : user.getId());
    log.setUsername(username);
    log.setSuccess(success ? 1 : 0);
    log.setIp(ServletUtils.ip());
    log.setUserAgent(ServletUtils.userAgent());
    log.setReason(reason);
    log.setCreateTime(LocalDateTime.now());
    loginLogMapper.insert(log);
  }

  private SessionVO toSession(SysRefreshToken token, String device) {
    SessionVO vo = new SessionVO();
    vo.setId(token.getId());
    vo.setUserId(token.getUserId());
    vo.setIp(token.getIp());
    vo.setUserAgent(token.getUserAgent());
    vo.setLoginTime(token.getCreateTime());
    vo.setExpireTime(token.getExpireTime());
    vo.setCurrent(StrUtil.isNotBlank(device) && device.equals(token.getDevice()));
    if (token.getUserId() != null) {
      SysUser user = userMapper.selectById(token.getUserId());
      if (user != null) {
        vo.setUsername(user.getUsername());
        vo.setDisplayName(user.getDisplayName());
      }
    }
    return vo;
  }

  private ProfileVO toProfile(SysUser user, LoginUser loginUser) {
    ProfileVO vo = new ProfileVO();
    vo.setId(user.getId());
    vo.setUsername(user.getUsername());
    vo.setDisplayName(user.getDisplayName());
    vo.setEmail(user.getEmail());
    vo.setPhone(user.getPhone());
    vo.setAvatar(user.getAvatar());
    vo.setStatus(user.getStatus());
    vo.setDeptId(user.getDeptId());
    vo.setPostId(user.getPostId());
    vo.setMustChangePassword(Integer.valueOf(1).equals(user.getMustChangePwd()));
    vo.setDataScope(loginUser.getDataScope());
    vo.setRoles(new ArrayList<>(loginUser.getRoles()));
    vo.setPermissions(new ArrayList<>(loginUser.getPermissions()));
    vo.setLastLoginAt(user.getLastLoginAt());
    if (user.getDeptId() != null) {
      SysDept dept = deptMapper.selectById(user.getDeptId());
      vo.setDeptName(dept == null ? null : dept.getName());
    }
    if (user.getPostId() != null) {
      SysPost post = postMapper.selectById(user.getPostId());
      vo.setPostName(post == null ? null : post.getName());
    }
    return vo;
  }

  private List<MenuVO> buildMenuTree(List<SysMenu> list, Long parentId) {
    return list.stream()
        .filter(item -> ObjectsEquals(parentId, item.getParentId()))
        .sorted(Comparator.comparing(item -> item.getSortNo() == null ? 0 : item.getSortNo()))
        .map(item -> {
          MenuVO vo = toMenu(item);
          List<MenuVO> children = buildMenuTree(list, item.getId());
          if ("DIR".equals(item.getType()) && children.isEmpty()) {
            return null;
          }
          vo.setChildren(children);
          return vo;
        })
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  private boolean ObjectsEquals(Long parentId, Long actual) {
    long left = parentId == null ? 0L : parentId;
    long right = actual == null ? 0L : actual;
    return left == right;
  }

  private MenuVO toMenu(SysMenu menu) {
    MenuVO vo = new MenuVO();
    vo.setId(menu.getId());
    vo.setParentId(menu.getParentId());
    vo.setType(menu.getType());
    vo.setName(menu.getName());
    vo.setPath(menu.getPath());
    vo.setComponent(menu.getComponent());
    vo.setIcon(menu.getIcon());
    vo.setPermission(menu.getPermission());
    vo.setVisible(Integer.valueOf(1).equals(menu.getVisible()));
    vo.setHidden(Integer.valueOf(1).equals(menu.getHidden()));
    vo.setSortNo(menu.getSortNo());
    return vo;
  }
}
