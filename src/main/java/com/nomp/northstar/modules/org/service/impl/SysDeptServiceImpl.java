package com.nomp.northstar.modules.org.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nomp.northstar.common.constant.DataScope;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.common.security.LoginUser;
import com.nomp.northstar.modules.org.domain.SysDept;
import com.nomp.northstar.modules.org.mapper.SysDeptMapper;
import com.nomp.northstar.modules.org.model.dto.SysDeptSaveDTO;
import com.nomp.northstar.modules.org.model.query.SysDeptQuery;
import com.nomp.northstar.modules.org.model.vo.SysDeptVO;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.mapper.SysUserMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl {
  private final SysDeptMapper deptMapper;
  private final SysUserMapper userMapper;

  public PageResult<SysDeptVO> page(SysDeptQuery query) {
    LoginUser actor = LoginHelper.get();
    LambdaQueryWrapper<SysDept> wrapper = Wrappers.lambdaQuery();
    if (StrUtil.isNotBlank(query.getKeyword())) {
      wrapper.and(w -> w.like(SysDept::getName, query.getKeyword())
          .or().like(SysDept::getCode, query.getKeyword())
          .or().like(SysDept::getPhone, query.getKeyword()));
    }
    if (StrUtil.isNotBlank(query.getStatus()) && !"all".equals(query.getStatus())) {
      wrapper.eq(SysDept::getStatus, query.getStatus());
    }
    if (query.getParentId() != null) {
      wrapper.eq(SysDept::getParentId, query.getParentId());
    }
    if (!actor.isSuperAdmin() && actor.getDataScope() != DataScope.ALL) {
      if (actor.getDeptIds() == null || actor.getDeptIds().isEmpty()) {
        wrapper.eq(SysDept::getId, actor.getDeptId());
      } else {
        wrapper.in(SysDept::getId, actor.getDeptIds());
      }
    }
    wrapper.orderByAsc(SysDept::getSortNo).orderByDesc(SysDept::getId);
    Page<SysDept> page = deptMapper.selectPage(new Page<>(query.current(), query.size()), wrapper);
    return PageResult.of(page.getRecords().stream().map(this::toVo).toList(), page.getTotal(), page.getCurrent(), page.getSize(), stats());
  }

  public List<SysDeptVO> listAll() {
    return deptMapper.selectList(Wrappers.<SysDept>lambdaQuery().orderByAsc(SysDept::getSortNo)).stream().map(this::toVo).toList();
  }

  public Long create(SysDeptSaveDTO dto) {
    assertCode(dto.getCode(), null);
    SysDept dept = new SysDept();
    fill(dept, dto);
    deptMapper.insert(dept);
    return dept.getId();
  }

  public void update(Long id, SysDeptSaveDTO dto) {
    SysDept dept = require(id);
    assertCode(dto.getCode(), id);
    if (dto.getParentId() != null && dto.getParentId().equals(id)) {
      throw BizException.conflict("上级部门不能是自己");
    }
    fill(dept, dto);
    deptMapper.updateById(dept);
  }

  public void changeStatus(Long id, String status) {
    SysDept dept = require(id);
    if ("frozen".equals(status)) {
      Long members = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeptId, id).eq(SysUser::getStatus, "active"));
      if (members != null && members > 0) {
        throw BizException.conflict("部门仍有在职成员，无法停用");
      }
    }
    dept.setStatus(status);
    deptMapper.updateById(dept);
  }

  public void remove(Long id) {
    Long children = deptMapper.selectCount(Wrappers.<SysDept>lambdaQuery().eq(SysDept::getParentId, id));
    if (children != null && children > 0) {
      throw BizException.conflict("请先删除子部门");
    }
    Long members = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeptId, id));
    if (members != null && members > 0) {
      throw BizException.conflict("部门仍有人员，无法删除");
    }
    deptMapper.deleteById(id);
  }

  public Map<String, Long> stats() {
    Map<String, Long> map = new LinkedHashMap<>();
    map.put("total", deptMapper.selectCount(null));
    map.put("active", deptMapper.selectCount(Wrappers.<SysDept>lambdaQuery().eq(SysDept::getStatus, "active")));
    map.put("members", userMapper.selectCount(null));
    return map;
  }

  private void fill(SysDept dept, SysDeptSaveDTO dto) {
    dept.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
    dept.setName(dto.getName());
    dept.setCode(dto.getCode());
    dept.setLeaderId(dto.getLeaderId());
    dept.setPhone(dto.getPhone());
    dept.setStatus(StrUtil.blankToDefault(dto.getStatus(), "active"));
    dept.setSortNo(dto.getSortNo() == null ? 0 : dto.getSortNo());
    dept.setRemark(dto.getRemark());
  }

  private void assertCode(String code, Long excludeId) {
    Long count = deptMapper.selectCount(Wrappers.<SysDept>lambdaQuery().eq(SysDept::getCode, code).ne(excludeId != null, SysDept::getId, excludeId));
    if (count != null && count > 0) {
      throw BizException.conflict("部门编码已存在");
    }
  }

  private SysDept require(Long id) {
    SysDept dept = deptMapper.selectById(id);
    if (dept == null) {
      throw BizException.notFound("部门不存在");
    }
    return dept;
  }

  private SysDeptVO toVo(SysDept dept) {
    SysDeptVO vo = new SysDeptVO();
    vo.setId(dept.getId());
    vo.setParentId(dept.getParentId());
    vo.setName(dept.getName());
    vo.setCode(dept.getCode());
    vo.setLeaderId(dept.getLeaderId());
    vo.setPhone(dept.getPhone());
    vo.setStatus(dept.getStatus());
    vo.setSortNo(dept.getSortNo());
    vo.setRemark(dept.getRemark());
    vo.setCreateTime(dept.getCreateTime());
    vo.setMemberCount(userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeptId, dept.getId())));
    if (dept.getParentId() != null && dept.getParentId() > 0) {
      SysDept parent = deptMapper.selectById(dept.getParentId());
      vo.setParentName(parent == null ? null : parent.getName());
    }
    if (dept.getLeaderId() != null) {
      SysUser leader = userMapper.selectById(dept.getLeaderId());
      vo.setLeaderName(leader == null ? null : leader.getDisplayName());
    }
    return vo;
  }
}
