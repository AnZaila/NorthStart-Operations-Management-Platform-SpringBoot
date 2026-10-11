package com.nomp.northstar.modules.notify.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nomp.northstar.common.core.PageQuery;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.common.security.LoginUser;
import com.nomp.northstar.modules.notify.domain.SysAnnouncement;
import com.nomp.northstar.modules.notify.domain.SysNotice;
import com.nomp.northstar.modules.notify.mapper.SysAnnouncementMapper;
import com.nomp.northstar.modules.notify.mapper.SysNoticeMapper;
import com.nomp.northstar.modules.notify.model.AnnouncementSaveDTO;
import com.nomp.northstar.modules.notify.model.AnnouncementVO;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.mapper.SysUserMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
  private final SysAnnouncementMapper announcementMapper;
  private final SysNoticeMapper noticeMapper;
  private final SysUserMapper userMapper;

  public PageResult<AnnouncementVO> page(Query query) {
    LambdaQueryWrapper<SysAnnouncement> wrapper = Wrappers.lambdaQuery();
    if (StrUtil.isNotBlank(query.getKeyword())) {
      wrapper.and(w -> w.like(SysAnnouncement::getTitle, query.getKeyword())
          .or()
          .like(SysAnnouncement::getContent, query.getKeyword()));
    }
    if (StrUtil.isNotBlank(query.getStatus()) && !"all".equals(query.getStatus())) {
      wrapper.eq(SysAnnouncement::getStatus, query.getStatus());
    }
    wrapper.orderByDesc(SysAnnouncement::getId);
    Page<SysAnnouncement> page =
        announcementMapper.selectPage(new Page<>(query.current(), query.size()), wrapper);
    return PageResult.of(page.getRecords().stream().map(this::toVo).toList(), page.getTotal(), page.getCurrent(), page.getSize());
  }

  @Transactional
  public Long create(AnnouncementSaveDTO dto) {
    SysAnnouncement item = new SysAnnouncement();
    fill(item, dto);
    item.setStatus("draft");
    item.setRecipientCount(0);
    announcementMapper.insert(item);
    return item.getId();
  }

  @Transactional
  public void update(Long id, AnnouncementSaveDTO dto) {
    SysAnnouncement item = require(id);
    if ("published".equals(item.getStatus())) {
      throw BizException.conflict("已发布的公告不能修改");
    }
    fill(item, dto);
    announcementMapper.updateById(item);
  }

  @Transactional
  public void publish(Long id) {
    SysAnnouncement item = require(id);
    if ("published".equals(item.getStatus())) {
      throw BizException.conflict("公告已发布");
    }
    LoginUser actor = LoginHelper.get();
    List<SysUser> users =
        userMapper.selectList(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getStatus, "active"));
    LocalDateTime now = LocalDateTime.now();
    for (SysUser user : users) {
      SysNotice notice = new SysNotice();
      notice.setUserId(user.getId());
      notice.setTitle(item.getTitle());
      notice.setContent(item.getContent());
      notice.setType(item.getType());
      notice.setReadFlag(0);
      notice.setCreateTime(now);
      noticeMapper.insert(notice);
    }
    item.setStatus("published");
    item.setPublisherId(actor.getUserId());
    item.setPublisherName(actor.getDisplayName());
    item.setPublishTime(now);
    item.setRecipientCount(users.size());
    announcementMapper.updateById(item);
  }

  @Transactional
  public void remove(Long id) {
    SysAnnouncement item = require(id);
    announcementMapper.deleteById(item.getId());
  }

  private void fill(SysAnnouncement item, AnnouncementSaveDTO dto) {
    item.setTitle(dto.getTitle().trim());
    item.setContent(StrUtil.blankToDefault(dto.getContent(), "").trim());
    item.setType(StrUtil.blankToDefault(dto.getType(), "system"));
  }

  private SysAnnouncement require(Long id) {
    SysAnnouncement item = announcementMapper.selectById(id);
    if (item == null) {
      throw BizException.notFound("公告不存在");
    }
    return item;
  }

  private AnnouncementVO toVo(SysAnnouncement item) {
    AnnouncementVO vo = new AnnouncementVO();
    vo.setId(item.getId());
    vo.setTitle(item.getTitle());
    vo.setContent(item.getContent());
    vo.setType(item.getType());
    vo.setStatus(item.getStatus());
    vo.setPublisherId(item.getPublisherId());
    vo.setPublisherName(item.getPublisherName());
    vo.setPublishTime(item.getPublishTime());
    vo.setRecipientCount(item.getRecipientCount());
    vo.setCreateTime(item.getCreateTime());
    return vo;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class Query extends PageQuery {
    private String keyword;
    private String status;
  }
}
