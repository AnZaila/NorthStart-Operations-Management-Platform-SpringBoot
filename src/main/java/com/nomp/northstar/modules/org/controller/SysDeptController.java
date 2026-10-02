package com.nomp.northstar.modules.org.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.org.model.dto.SysDeptSaveDTO;
import com.nomp.northstar.modules.org.model.query.SysDeptQuery;
import com.nomp.northstar.modules.org.model.vo.SysDeptVO;
import com.nomp.northstar.modules.org.service.impl.SysDeptServiceImpl;
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
@RequestMapping("/api/v1/org/departments")
@RequiredArgsConstructor
public class SysDeptController {
  private final SysDeptServiceImpl deptService;

  @GetMapping
  @SaCheckPermission(Perms.DEPT_VIEW)
  public R<PageResult<SysDeptVO>> page(SysDeptQuery query) {
    return R.ok(deptService.page(query));
  }

  @GetMapping("/options")
  @SaCheckPermission(Perms.DEPT_VIEW)
  public R<List<SysDeptVO>> options() {
    return R.ok(deptService.listAll());
  }

  @PostMapping
  @SaCheckPermission(Perms.DEPT_CREATE)
  @OperLog(module = "部门", action = "新建", resource = "dept")
  public R<Map<String, Long>> create(@Valid @RequestBody SysDeptSaveDTO dto) {
    return R.ok(Map.of("id", deptService.create(dto)));
  }

  @PutMapping("/{id}")
  @SaCheckPermission(Perms.DEPT_UPDATE)
  @OperLog(module = "部门", action = "编辑", resource = "dept")
  public R<Void> update(@PathVariable Long id, @Valid @RequestBody SysDeptSaveDTO dto) {
    deptService.update(id, dto);
    return R.ok();
  }

  @PostMapping("/{id}/disable")
  @SaCheckPermission(Perms.DEPT_DISABLE)
  public R<Void> disable(@PathVariable Long id) {
    deptService.changeStatus(id, "frozen");
    return R.ok();
  }

  @PostMapping("/{id}/enable")
  @SaCheckPermission(Perms.DEPT_DISABLE)
  public R<Void> enable(@PathVariable Long id) {
    deptService.changeStatus(id, "active");
    return R.ok();
  }

  @DeleteMapping("/{id}")
  @SaCheckPermission(Perms.DEPT_DELETE)
  @OperLog(module = "部门", action = "删除", resource = "dept")
  public R<Void> remove(@PathVariable Long id) {
    deptService.remove(id);
    return R.ok();
  }
}
