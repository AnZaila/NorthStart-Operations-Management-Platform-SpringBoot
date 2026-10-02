package com.nomp.northstar.common.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.constant.DataScope;
import com.nomp.northstar.modules.org.domain.SysDept;
import com.nomp.northstar.modules.org.mapper.SysDeptMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataScopeHelper {
  private final SysDeptMapper deptMapper;

  public Set<Long> deptTreeIds(Long deptId) {
    Set<Long> ids = new HashSet<>();
    if (deptId == null) {
      return ids;
    }
    ids.add(deptId);
    fillChildren(deptId, ids);
    return ids;
  }

  private void fillChildren(Long parentId, Set<Long> ids) {
    List<SysDept> children = deptMapper.selectList(Wrappers.<SysDept>lambdaQuery().eq(SysDept::getParentId, parentId));
    for (SysDept child : children) {
      if (ids.add(child.getId())) {
        fillChildren(child.getId(), ids);
      }
    }
  }

  public boolean allData(LoginUser user) {
    return user.isSuperAdmin() || user.getDataScope() == DataScope.ALL;
  }
}
