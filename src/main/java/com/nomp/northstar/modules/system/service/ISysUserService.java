package com.nomp.northstar.modules.system.service;

import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.modules.system.model.dto.SysUserSaveDTO;
import com.nomp.northstar.modules.system.model.query.SysUserQuery;
import com.nomp.northstar.modules.system.model.vo.SysUserVO;
import java.util.List;
import java.util.Map;

public interface ISysUserService {
  PageResult<SysUserVO> page(SysUserQuery query);

  SysUserVO detail(Long id);

  Long create(SysUserSaveDTO dto);

  void update(Long id, SysUserSaveDTO dto);

  void remove(Long id);

  void changeStatus(Long id, String status);

  void unlock(Long id);

  String resetPassword(Long id);

  void grantRoles(Long id, List<Long> roleIds);

  Map<String, Long> stats();
}
