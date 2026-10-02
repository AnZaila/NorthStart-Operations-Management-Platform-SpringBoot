package com.nomp.northstar.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.annotation.OperLog;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.core.R;
import com.nomp.northstar.common.exception.BizException;
import com.nomp.northstar.modules.system.domain.SysDictData;
import com.nomp.northstar.modules.system.domain.SysDictType;
import com.nomp.northstar.modules.system.mapper.SysDictDataMapper;
import com.nomp.northstar.modules.system.mapper.SysDictTypeMapper;
import java.util.List;
import java.util.Map;
import lombok.Data;
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
@RequestMapping("/api/v1/system/dicts")
@RequiredArgsConstructor
public class SysDictController {
  private final SysDictTypeMapper typeMapper;
  private final SysDictDataMapper dataMapper;

  @GetMapping
  @SaCheckPermission(Perms.DICT_VIEW)
  public R<List<SysDictType>> types() {
    return R.ok(typeMapper.selectList(Wrappers.<SysDictType>lambdaQuery().orderByAsc(SysDictType::getCode)));
  }

  @GetMapping("/{code}")
  public R<List<DictItemVO>> data(@PathVariable String code) {
    return R.ok(dataMapper.selectList(Wrappers.<SysDictData>lambdaQuery()
        .eq(SysDictData::getDictCode, code)
        .eq(SysDictData::getStatus, "active")
        .orderByAsc(SysDictData::getSortNo)).stream().map(this::toItem).toList());
  }

  @PostMapping
  @SaCheckPermission(Perms.DICT_CREATE)
  @OperLog(module = "字典", action = "新建类型", resource = "dict")
  public R<Map<String, Long>> createType(@RequestBody SysDictType body) {
    if (typeMapper.selectCount(Wrappers.<SysDictType>lambdaQuery().eq(SysDictType::getCode, body.getCode())) > 0) {
      throw BizException.conflict("字典编码已存在");
    }
    typeMapper.insert(body);
    return R.ok(Map.of("id", body.getId()));
  }

  @PutMapping("/{id}")
  @SaCheckPermission(Perms.DICT_UPDATE)
  public R<Void> updateType(@PathVariable Long id, @RequestBody SysDictType body) {
    body.setId(id);
    typeMapper.updateById(body);
    return R.ok();
  }

  @DeleteMapping("/{id}")
  @SaCheckPermission(Perms.DICT_DELETE)
  public R<Void> removeType(@PathVariable Long id) {
    SysDictType type = typeMapper.selectById(id);
    if (type != null) {
      dataMapper.delete(Wrappers.<SysDictData>lambdaQuery().eq(SysDictData::getDictCode, type.getCode()));
    }
    typeMapper.deleteById(id);
    return R.ok();
  }

  @PostMapping("/{code}/items")
  @SaCheckPermission(Perms.DICT_CREATE)
  public R<Map<String, Long>> createItem(@PathVariable String code, @RequestBody ItemBody body) {
    SysDictData item = new SysDictData();
    item.setDictCode(code);
    item.setLabel(body.getLabel());
    item.setDictValue(body.getValue());
    item.setSortNo(body.getSortNo() == null ? 0 : body.getSortNo());
    item.setStatus("active");
    dataMapper.insert(item);
    return R.ok(Map.of("id", item.getId()));
  }

  @DeleteMapping("/items/{id}")
  @SaCheckPermission(Perms.DICT_DELETE)
  public R<Void> removeItem(@PathVariable Long id) {
    dataMapper.deleteById(id);
    return R.ok();
  }

  @PutMapping("/items/{id}")
  @SaCheckPermission(Perms.DICT_UPDATE)
  public R<Void> updateItem(@PathVariable Long id, @RequestBody ItemBody body) {
    SysDictData item = dataMapper.selectById(id);
    if (item == null) {
      throw BizException.notFound("字典项不存在");
    }
    item.setLabel(body.getLabel());
    item.setDictValue(body.getValue());
    if (body.getSortNo() != null) {
      item.setSortNo(body.getSortNo());
    }
    dataMapper.updateById(item);
    return R.ok();
  }

  @Data
  public static class ItemBody {
    private String label;
    private String value;
    private Integer sortNo;
  }

  @Data
  public static class DictItemVO {
    private Long id;
    private String dictCode;
    private String label;
    private String value;
    private Integer sortNo;
    private String status;
  }

  private DictItemVO toItem(SysDictData item) {
    DictItemVO vo = new DictItemVO();
    vo.setId(item.getId());
    vo.setDictCode(item.getDictCode());
    vo.setLabel(item.getLabel());
    vo.setValue(item.getDictValue());
    vo.setSortNo(item.getSortNo());
    vo.setStatus(item.getStatus());
    return vo;
  }
}
