package com.nomp.northstar.common.security;

import cn.dev33.satoken.stp.StpInterface;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StpInterfaceImpl implements StpInterface {
  @Override
  public List<String> getPermissionList(Object loginId, String loginType) {
    LoginUser user = LoginHelper.getOrNull();
    return user == null ? List.of() : new ArrayList<>(user.getPermissions());
  }

  @Override
  public List<String> getRoleList(Object loginId, String loginType) {
    LoginUser user = LoginHelper.getOrNull();
    return user == null ? List.of() : new ArrayList<>(user.getRoles());
  }
}
