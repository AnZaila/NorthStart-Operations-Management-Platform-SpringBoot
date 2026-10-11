package com.nomp.northstar.modules.notify.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nomp.northstar.common.core.PageQuery;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.modules.notify.domain.SysNotice;
import com.nomp.northstar.modules.notify.mapper.SysNoticeMapper;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ops/notifications")
@RequiredArgsConstructor
public class NoticeController {
  private final SysNoticeMapper noticeMapper;

  @GetMapping
  public R<PageResult<SysNotice>> page(Query query) {
    LambdaQueryWrapper<SysNotice> wrapper = Wrappers.<SysNotice>lambdaQuery()
        .eq(SysNotice::getUserId, LoginHelper.userId());
    if (query.getReadFlag() != null) {
      wrapper.eq(SysNotice::getReadFlag, query.getReadFlag());
    }
    if (StrUtil.isNotBlank(query.getKeyword())) {
      wrapper.and(w -> w.like(SysNotice::getTitle, query.getKeyword())
          .or()
          .like(SysNotice::getContent, query.getKeyword()));
    }
    wrapper.orderByDesc(SysNotice::getId);
    Page<SysNotice> page = noticeMapper.selectPage(new Page<>(query.current(), query.size()), wrapper);
    return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
  }

  @GetMapping("/unread-count")
  public R<Map<String, Long>> unread() {
    long count = noticeMapper.selectCount(Wrappers.<SysNotice>lambdaQuery()
        .eq(SysNotice::getUserId, LoginHelper.userId())
        .eq(SysNotice::getReadFlag, 0));
    return R.ok(Map.of("count", count));
  }

  @PostMapping("/{id}/read")
  public R<Void> read(@PathVariable Long id) {
    SysNotice notice = noticeMapper.selectById(id);
    if (notice != null && LoginHelper.userId().equals(notice.getUserId())) {
      notice.setReadFlag(1);
      noticeMapper.updateById(notice);
    }
    return R.ok();
  }

  @PostMapping("/read-all")
  public R<Void> readAll() {
    List<SysNotice> list = noticeMapper.selectList(Wrappers.<SysNotice>lambdaQuery()
        .eq(SysNotice::getUserId, LoginHelper.userId())
        .eq(SysNotice::getReadFlag, 0));
    for (SysNotice notice : list) {
      notice.setReadFlag(1);
      noticeMapper.updateById(notice);
    }
    return R.ok();
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class Query extends PageQuery {
    private String keyword;
    private Integer readFlag;
  }
}
