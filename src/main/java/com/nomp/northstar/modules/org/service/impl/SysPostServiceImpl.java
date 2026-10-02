package com.nomp.northstar.modules.org.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.modules.org.domain.SysDept;
import com.nomp.northstar.modules.org.domain.SysPost;
import com.nomp.northstar.modules.org.mapper.SysDeptMapper;
import com.nomp.northstar.modules.org.mapper.SysPostMapper;
import com.nomp.northstar.modules.org.model.dto.SysPostSaveDTO;
import com.nomp.northstar.modules.org.model.query.SysPostQuery;
import com.nomp.northstar.modules.org.model.vo.SysPostVO;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.mapper.SysUserMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysPostServiceImpl {
  private final SysPostMapper postMapper;
  private final SysDeptMapper deptMapper;
  private final SysUserMapper userMapper;

  public PageResult<SysPostVO> page(SysPostQuery query) {
    LambdaQueryWrapper<SysPost> wrapper = Wrappers.lambdaQuery();
    if (StrUtil.isNotBlank(query.getKeyword())) {
      wrapper.and(w -> w.like(SysPost::getName, query.getKeyword()).or().like(SysPost::getCode, query.getKeyword()));
    }
    if (StrUtil.isNotBlank(query.getStatus()) && !"all".equals(query.getStatus())) {
      wrapper.eq(SysPost::getStatus, query.getStatus());
    }
    if (StrUtil.isNotBlank(query.getLevel())) {
      wrapper.eq(SysPost::getLevel, query.getLevel());
    }
    if (query.getDeptId() != null) {
      wrapper.eq(SysPost::getDeptId, query.getDeptId());
    }
    wrapper.orderByDesc(SysPost::getId);
    Page<SysPost> page = postMapper.selectPage(new Page<>(query.current(), query.size()), wrapper);
    return PageResult.of(page.getRecords().stream().map(this::toVo).toList(), page.getTotal(), page.getCurrent(), page.getSize(), stats());
  }

  public List<SysPostVO> options() {
    return postMapper.selectList(Wrappers.<SysPost>lambdaQuery()
            .eq(SysPost::getStatus, "active")
            .orderByAsc(SysPost::getName))
        .stream()
        .map(this::toVo)
        .toList();
  }

  public Long create(SysPostSaveDTO dto) {
    assertCode(dto.getCode(), null);
    SysPost post = new SysPost();
    fill(post, dto);
    postMapper.insert(post);
    return post.getId();
  }

  public void update(Long id, SysPostSaveDTO dto) {
    SysPost post = require(id);
    assertCode(dto.getCode(), id);
    fill(post, dto);
    postMapper.updateById(post);
  }

  public void changeStatus(Long id, String status) {
    SysPost post = require(id);
    post.setStatus(status);
    postMapper.updateById(post);
  }

  public void remove(Long id) {
    Long used = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getPostId, id));
    if (used != null && used > 0) {
      throw BizException.conflict("岗位仍有在职人员，无法删除");
    }
    postMapper.deleteById(id);
  }

  public Map<String, Long> stats() {
    Map<String, Long> map = new LinkedHashMap<>();
    map.put("total", postMapper.selectCount(null));
    map.put("active", postMapper.selectCount(Wrappers.<SysPost>lambdaQuery().eq(SysPost::getStatus, "active")));
    map.put("headcount", postMapper.selectList(null).stream().mapToLong(p -> p.getHeadcount() == null ? 0 : p.getHeadcount()).sum());
    return map;
  }

  private void fill(SysPost post, SysPostSaveDTO dto) {
    post.setDeptId(dto.getDeptId());
    post.setName(dto.getName());
    post.setCode(dto.getCode());
    post.setLevel(dto.getLevel());
    post.setHeadcount(dto.getHeadcount() == null ? 1 : dto.getHeadcount());
    post.setStatus(StrUtil.blankToDefault(dto.getStatus(), "active"));
    post.setRemark(dto.getRemark());
  }

  private void assertCode(String code, Long excludeId) {
    Long count = postMapper.selectCount(Wrappers.<SysPost>lambdaQuery().eq(SysPost::getCode, code).ne(excludeId != null, SysPost::getId, excludeId));
    if (count != null && count > 0) {
      throw BizException.conflict("岗位编码已存在");
    }
  }

  private SysPost require(Long id) {
    SysPost post = postMapper.selectById(id);
    if (post == null) {
      throw BizException.notFound("岗位不存在");
    }
    return post;
  }

  private SysPostVO toVo(SysPost post) {
    SysPostVO vo = new SysPostVO();
    vo.setId(post.getId());
    vo.setDeptId(post.getDeptId());
    vo.setName(post.getName());
    vo.setCode(post.getCode());
    vo.setLevel(post.getLevel());
    vo.setHeadcount(post.getHeadcount());
    vo.setStatus(post.getStatus());
    vo.setRemark(post.getRemark());
    vo.setCreateTime(post.getCreateTime());
    vo.setOccupied(userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getPostId, post.getId()).eq(SysUser::getStatus, "active")));
    SysDept dept = deptMapper.selectById(post.getDeptId());
    vo.setDeptName(dept == null ? null : dept.getName());
    return vo;
  }
}
