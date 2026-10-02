package com.nomp.northstar.modules.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.PageQuery;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.modules.file.model.FileVO;
import com.nomp.northstar.modules.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
  private final FileService fileService;

  @PostMapping
  @OperLog(module = "文件", action = "上传", resource = "file")
  public R<FileVO> upload(@RequestParam("file") MultipartFile file,
                          @RequestParam(value = "bizType", required = false) String bizType) {
    return R.ok(fileService.upload(file, bizType));
  }

  @GetMapping
  @SaCheckPermission(Perms.FILE_VIEW)
  public R<PageResult<FileVO>> page(PageQuery query) {
    return R.ok(fileService.page(query));
  }

  @GetMapping("/raw/{id}")
  public ResponseEntity<Resource> raw(@PathVariable Long id) {
    return fileService.raw(id);
  }

  @DeleteMapping("/{id}")
  @SaCheckPermission(Perms.FILE_DELETE)
  @OperLog(module = "文件", action = "删除", resource = "file")
  public R<Void> remove(@PathVariable Long id) {
    fileService.remove(id);
    return R.ok();
  }
}
