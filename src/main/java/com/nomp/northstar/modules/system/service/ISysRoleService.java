package com.nomp.northstar.modules.system.service;

import com.nomp.northstar.modules.system.model.dto.SysRoleGrantDTO;
import com.nomp.northstar.modules.system.model.dto.SysRoleSaveDTO;
import com.nomp.northstar.modules.system.model.vo.SysRoleVO;
import com.nomp.northstar.modules.system.model.vo.SysUserVO;
import java.util.List;

public interface ISysRoleService {
  List<SysRoleVO> listAll();

  SysRoleVO detail(Long id);

  Long create(SysRoleSaveDTO dto);

  void update(Long id, SysRoleSaveDTO dto);

  void remove(Long id);

  void grant(Long id, SysRoleGrantDTO dto);

  Long copy(Long id);

  List<SysUserVO> members(Long id);
}
