package com.nomp.northstar.modules.org.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.org.model.dto.SysPostSaveDTO;
import com.nomp.northstar.modules.org.model.query.SysPostQuery;
import com.nomp.northstar.modules.org.model.vo.SysPostVO;
import com.nomp.northstar.modules.org.service.impl.SysPostServiceImpl;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/org/positions")
@RequiredArgsConstructor
public class SysPostController {
  private final SysPostServiceImpl postService;

  @GetMapping
  @SaCheckPermission(Perms.POST_VIEW)
  public R<PageResult<SysPostVO>> page(SysPostQuery query) {
    return R.ok(postService.page(query));
  }

  @GetMapping("/options")
  public R<List<SysPostVO>> options() {
    return R.ok(postService.options());
  }

  @PostMapping
  @SaCheckPermission(Perms.POST_CREATE)
  @OperLog(module = "岗位", action = "新建", resource = "position")
  public R<Map<String, Long>> create(@Valid @RequestBody SysPostSaveDTO dto) {
    return R.ok(Map.of("id", postService.create(dto)));
  }

  @PutMapping("/{id}")
  @SaCheckPermission(Perms.POST_UPDATE)
  @OperLog(module = "岗位", action = "编辑", resource = "position")
  public R<Void> update(@PathVariable Long id, @Valid @RequestBody SysPostSaveDTO dto) {
    postService.update(id, dto);
    return R.ok();
  }

  @PostMapping("/{id}/disable")
  @SaCheckPermission(Perms.POST_UPDATE)
  public R<Void> disable(@PathVariable Long id) {
    postService.changeStatus(id, "frozen");
    return R.ok();
  }

  @PostMapping("/{id}/enable")
  @SaCheckPermission(Perms.POST_UPDATE)
  public R<Void> enable(@PathVariable Long id) {
    postService.changeStatus(id, "active");
    return R.ok();
  }

  @DeleteMapping("/{id}")
  @SaCheckPermission(Perms.POST_DELETE)
  @OperLog(module = "岗位", action = "删除", resource = "position")
  public R<Void> remove(@PathVariable Long id) {
    postService.remove(id);
    return R.ok();
  }
}
