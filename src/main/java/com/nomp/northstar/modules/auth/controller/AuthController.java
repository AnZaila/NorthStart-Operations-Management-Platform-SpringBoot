package com.nomp.northstar.modules.auth.controller;

import com.nomp.northstar.common.core.R;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.modules.auth.model.CaptchaVO;
import com.nomp.northstar.modules.auth.model.ChangePasswordDTO;
import com.nomp.northstar.modules.auth.model.LoginDTO;
import com.nomp.northstar.modules.auth.model.MenuVO;
import com.nomp.northstar.modules.auth.model.ProfileUpdateDTO;
import com.nomp.northstar.modules.auth.model.ProfileVO;
import com.nomp.northstar.modules.auth.model.RefreshDTO;
import com.nomp.northstar.modules.auth.model.SessionVO;
import com.nomp.northstar.modules.auth.model.TokenVO;
import com.nomp.northstar.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @GetMapping("/captcha")
  public R<CaptchaVO> captcha() {
    return R.ok(authService.captcha(false));
  }

  @PostMapping("/login")
  public R<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
    return R.ok(authService.login(dto));
  }

  @PostMapping("/refresh")
  public R<TokenVO> refresh(@RequestBody RefreshDTO dto) {
    return R.ok(authService.refresh(dto.getRefreshToken()));
  }

  @PostMapping("/logout")
  public R<Void> logout(@RequestBody(required = false) RefreshDTO dto) {
    authService.logout(dto == null ? null : dto.getRefreshToken());
    return R.ok();
  }

  @GetMapping("/me")
  public R<ProfileVO> me() {
    return R.ok(authService.me());
  }

  @GetMapping("/menus")
  public R<List<MenuVO>> menus() {
    return R.ok(authService.menus());
  }

  @PostMapping("/password")
  public R<Void> password(@Valid @RequestBody ChangePasswordDTO dto) {
    authService.changePassword(dto);
    return R.ok();
  }

  @PatchMapping("/profile")
  public R<ProfileVO> profile(@Valid @RequestBody ProfileUpdateDTO dto) {
    return R.ok(authService.updateProfile(dto));
  }

  @GetMapping("/sessions")
  public R<List<SessionVO>> sessions() {
    return R.ok(authService.sessions(LoginHelper.userId()));
  }

  @PostMapping("/sessions/{id}/kick")
  public R<Void> kickMine(@PathVariable Long id) {
    authService.kickSession(id, false);
    return R.ok();
  }
}
