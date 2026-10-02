package com.nomp.northstar.modules.notify.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.modules.notify.domain.SysNotice;
import com.nomp.northstar.modules.notify.mapper.SysNoticeMapper;
import java.util.List;
import java.util.Map;
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
  public R<List<SysNotice>> list() {
    return R.ok(noticeMapper.selectList(Wrappers.<SysNotice>lambdaQuery()
        .eq(SysNotice::getUserId, LoginHelper.userId())
        .orderByDesc(SysNotice::getId)
        .last("limit 20")));
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
}
