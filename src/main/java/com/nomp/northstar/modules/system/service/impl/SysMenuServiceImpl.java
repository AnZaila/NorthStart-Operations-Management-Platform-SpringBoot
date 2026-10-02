package com.nomp.northstar.modules.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.modules.auth.model.MenuVO;
import com.nomp.northstar.modules.system.domain.SysMenu;
import com.nomp.northstar.modules.system.domain.SysPermission;
import com.nomp.northstar.modules.system.mapper.SysMenuMapper;
import com.nomp.northstar.modules.system.mapper.SysPermissionMapper;
import com.nomp.northstar.modules.system.model.dto.SysMenuSaveDTO;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl {
  private final SysMenuMapper menuMapper;
  private final SysPermissionMapper permissionMapper;

  public List<MenuVO> tree() {
    List<SysMenu> all = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery().orderByAsc(SysMenu::getSortNo));
    return build(all, 0L);
  }

  @Transactional
  public Long create(SysMenuSaveDTO dto) {
    SysMenu menu = new SysMenu();
    fill(menu, dto);
    menuMapper.insert(menu);
    syncPermission(menu);
    return menu.getId();
  }

  @Transactional
  public void update(Long id, SysMenuSaveDTO dto) {
    SysMenu menu = require(id);
    fill(menu, dto);
    menuMapper.updateById(menu);
    syncPermission(menu);
  }

  @Transactional
  public void remove(Long id) {
    Long children = menuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, id));
    if (children != null && children > 0) {
      throw BizException.conflict("请先删除子菜单");
    }
    menuMapper.deleteById(id);
  }

  private void fill(SysMenu menu, SysMenuSaveDTO dto) {
    menu.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
    menu.setType(dto.getType());
    menu.setName(dto.getName());
    menu.setPath(dto.getPath());
    menu.setComponent(dto.getComponent());
    menu.setIcon(dto.getIcon());
    menu.setPermission(dto.getPermission());
    menu.setVisible(dto.getVisible() == null ? 1 : dto.getVisible());
    menu.setHidden(dto.getHidden() == null ? 0 : dto.getHidden());
    menu.setSortNo(dto.getSortNo() == null ? 0 : dto.getSortNo());
    menu.setStatus(StrUtil.blankToDefault(dto.getStatus(), "active"));
  }

  private void syncPermission(SysMenu menu) {
    if (StrUtil.isBlank(menu.getPermission())) {
      return;
    }
    Long count = permissionMapper.selectCount(Wrappers.<SysPermission>lambdaQuery().eq(SysPermission::getCode, menu.getPermission()));
    if (count != null && count > 0) {
      return;
    }
    SysPermission permission = new SysPermission();
    permission.setCode(menu.getPermission());
    permission.setName(menu.getName());
    permission.setType("BUTTON".equals(menu.getType()) ? "BUTTON" : "MENU");
    permissionMapper.insert(permission);
  }

  private SysMenu require(Long id) {
    SysMenu menu = menuMapper.selectById(id);
    if (menu == null) {
      throw BizException.notFound("菜单不存在");
    }
    return menu;
  }

  private List<MenuVO> build(List<SysMenu> list, Long parentId) {
    long pid = parentId == null ? 0L : parentId;
    return list.stream()
        .filter(item -> pid == (item.getParentId() == null ? 0L : item.getParentId()))
        .sorted(Comparator.comparing(item -> item.getSortNo() == null ? 0 : item.getSortNo()))
        .map(item -> {
          MenuVO vo = new MenuVO();
          vo.setId(item.getId());
          vo.setParentId(item.getParentId());
          vo.setType(item.getType());
          vo.setName(item.getName());
          vo.setPath(item.getPath());
          vo.setComponent(item.getComponent());
          vo.setIcon(item.getIcon());
          vo.setPermission(item.getPermission());
          vo.setVisible(Integer.valueOf(1).equals(item.getVisible()));
          vo.setHidden(Integer.valueOf(1).equals(item.getHidden()));
          vo.setSortNo(item.getSortNo());
          vo.setChildren(build(list, item.getId()));
          return vo;
        })
        .toList();
  }
}
