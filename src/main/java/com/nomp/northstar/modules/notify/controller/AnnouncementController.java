package com.nomp.northstar.modules.notify.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.notify.model.AnnouncementSaveDTO;
import com.nomp.northstar.modules.notify.model.AnnouncementVO;
import com.nomp.northstar.modules.notify.service.AnnouncementService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ops/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
  private final AnnouncementService announcementService;

  @GetMapping
  @SaCheckPermission(Perms.NOTICE_VIEW)
  public R<PageResult<AnnouncementVO>> page(AnnouncementService.Query query) {
    return R.ok(announcementService.page(query));
  }

  @PostMapping
  @SaCheckPermission(Perms.NOTICE_CREATE)
  @OperLog(module = "公告", action = "新建", resource = "announcement")
  public R<Map<String, Long>> create(@Valid @RequestBody AnnouncementSaveDTO dto) {
    return R.ok(Map.of("id", announcementService.create(dto)));
  }

  @PutMapping("/{id}")
  @SaCheckPermission(Perms.NOTICE_CREATE)
  @OperLog(module = "公告", action = "编辑", resource = "announcement")
  public R<Void> update(@PathVariable Long id, @Valid @RequestBody AnnouncementSaveDTO dto) {
    announcementService.update(id, dto);
    return R.ok();
  }

  @PostMapping("/{id}/publish")
  @SaCheckPermission(Perms.NOTICE_PUBLISH)
  @OperLog(module = "公告", action = "发布", resource = "announcement")
  public R<Void> publish(@PathVariable Long id) {
    announcementService.publish(id);
    return R.ok();
  }

  @DeleteMapping("/{id}")
  @SaCheckPermission(Perms.NOTICE_DELETE)
  @OperLog(module = "公告", action = "删除", resource = "announcement")
  public R<Void> remove(@PathVariable Long id) {
    announcementService.remove(id);
    return R.ok();
  }
}
