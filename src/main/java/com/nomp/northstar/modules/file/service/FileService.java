package com.nomp.northstar.modules.file.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nomp.northstar.common.core.PageQuery;
import com.nomp.northstar.common.core.PageResult;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.common.security.LoginHelper;
import com.nomp.northstar.config.NorthstarProperties;
import com.nomp.northstar.modules.file.domain.SysFile;
import com.nomp.northstar.modules.file.mapper.SysFileMapper;
import com.nomp.northstar.modules.file.model.FileVO;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.mapper.SysUserMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {
  private static final Set<String> ALLOWED = Set.of(
      "jpg", "jpeg", "png", "gif", "webp", "pdf", "xlsx", "xls", "csv", "doc", "docx");

  private final SysFileMapper fileMapper;
  private final SysUserMapper userMapper;
  private final NorthstarProperties properties;

  public FileVO upload(MultipartFile file, String bizType) {
    if (file == null || file.isEmpty()) {
      throw BizException.validation("请选择文件");
    }
    String original = StrUtil.blankToDefault(file.getOriginalFilename(), "file");
    String ext = StrUtil.blankToDefault(FileUtil.extName(original), "bin").toLowerCase();
    if (!ALLOWED.contains(ext)) {
      throw BizException.validation("不支持的文件类型");
    }
    if ("avatar".equals(bizType) && !Set.of("jpg", "jpeg", "png", "gif", "webp").contains(ext)) {
      throw BizException.validation("头像仅支持图片");
    }
    String stored = IdUtil.fastSimpleUUID() + "." + ext;
    Path dir = Path.of(properties.getStorage().getDir()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(dir);
      Path target = dir.resolve(stored).normalize();
      if (!target.startsWith(dir)) {
        throw BizException.validation("非法文件名");
      }
      try (InputStream in = file.getInputStream()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException ex) {
      throw BizException.conflict("文件保存失败");
    }
    SysFile record = new SysFile();
    record.setOriginalName(original);
    record.setStoredName(stored);
    record.setContentType(file.getContentType());
    record.setSizeBytes(file.getSize());
    record.setBizType(StrUtil.blankToDefault(bizType, "common"));
    record.setUserId(LoginHelper.userId());
    record.setCreateTime(LocalDateTime.now());
    fileMapper.insert(record);
    return toVo(record);
  }

  public PageResult<FileVO> page(PageQuery query) {
    Page<SysFile> page = fileMapper.selectPage(
        new Page<>(query.current(), query.size()),
        Wrappers.<SysFile>lambdaQuery().orderByDesc(SysFile::getId));
    return PageResult.of(page.getRecords().stream().map(this::toVo).toList(), page.getTotal(), page.getCurrent(), page.getSize());
  }

  public ResponseEntity<Resource> raw(Long id) {
    SysFile file = require(id);
    Path path = Path.of(properties.getStorage().getDir()).toAbsolutePath().normalize().resolve(file.getStoredName());
    if (!Files.exists(path)) {
      throw BizException.notFound("文件不存在");
    }
    MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
    if (StrUtil.isNotBlank(file.getContentType())) {
      mediaType = MediaType.parseMediaType(file.getContentType());
    }
    return ResponseEntity.ok()
        .contentType(mediaType)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getOriginalName() + "\"")
        .body(new FileSystemResource(path));
  }

  public void remove(Long id) {
    SysFile file = require(id);
    Path path = Path.of(properties.getStorage().getDir()).toAbsolutePath().normalize().resolve(file.getStoredName());
    FileUtil.del(path.toFile());
    fileMapper.deleteById(id);
  }

  private SysFile require(Long id) {
    SysFile file = fileMapper.selectById(id);
    if (file == null) {
      throw BizException.notFound("文件不存在");
    }
    return file;
  }

  private FileVO toVo(SysFile file) {
    FileVO vo = new FileVO();
    vo.setId(file.getId());
    vo.setOriginalName(file.getOriginalName());
    vo.setUrl("/api/v1/files/raw/" + file.getId());
    vo.setContentType(file.getContentType());
    vo.setSizeBytes(file.getSizeBytes());
    vo.setBizType(file.getBizType());
    vo.setUserId(file.getUserId());
    vo.setCreateTime(file.getCreateTime());
    if (file.getUserId() != null) {
      SysUser user = userMapper.selectById(file.getUserId());
      vo.setUploader(user == null ? null : user.getDisplayName());
    }
    return vo;
  }
}
