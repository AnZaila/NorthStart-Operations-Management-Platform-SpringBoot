package com.nomp.northstar.bootstrap;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nomp.northstar.common.constant.Perms;
import com.nomp.northstar.common.utils.PasswordUtils;
import com.nomp.northstar.modules.notify.domain.SysNotice;
import com.nomp.northstar.modules.notify.mapper.SysNoticeMapper;
import com.nomp.northstar.modules.org.domain.SysDept;
import com.nomp.northstar.modules.org.domain.SysPost;
import com.nomp.northstar.modules.org.mapper.SysDeptMapper;
import com.nomp.northstar.modules.org.mapper.SysPostMapper;
import com.nomp.northstar.modules.system.domain.SysConfig;
import com.nomp.northstar.modules.system.domain.SysDictData;
import com.nomp.northstar.modules.system.domain.SysDictType;
import com.nomp.northstar.modules.system.domain.SysMenu;
import com.nomp.northstar.modules.system.domain.SysPermission;
import com.nomp.northstar.modules.system.domain.SysRole;
import com.nomp.northstar.modules.system.domain.SysRolePermission;
import com.nomp.northstar.modules.system.domain.SysUser;
import com.nomp.northstar.modules.system.domain.SysUserRole;
import com.nomp.northstar.modules.system.mapper.SysConfigMapper;
import com.nomp.northstar.modules.system.mapper.SysDictDataMapper;
import com.nomp.northstar.modules.system.mapper.SysDictTypeMapper;
import com.nomp.northstar.modules.system.mapper.SysMenuMapper;
import com.nomp.northstar.modules.system.mapper.SysPermissionMapper;
import com.nomp.northstar.modules.system.mapper.SysRoleMapper;
import com.nomp.northstar.modules.system.mapper.SysRolePermissionMapper;
import com.nomp.northstar.modules.system.mapper.SysUserMapper;
import com.nomp.northstar.modules.system.mapper.SysUserRoleMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
  private final SysUserMapper userMapper;
  private final SysRoleMapper roleMapper;
  private final SysPermissionMapper permissionMapper;
  private final SysRolePermissionMapper rolePermissionMapper;
  private final SysUserRoleMapper userRoleMapper;
  private final SysMenuMapper menuMapper;
  private final SysDeptMapper deptMapper;
  private final SysPostMapper postMapper;
  private final SysDictTypeMapper dictTypeMapper;
  private final SysDictDataMapper dictDataMapper;
  private final SysConfigMapper configMapper;
  private final SysNoticeMapper noticeMapper;

  @Override
  @Transactional
  public void run(String... args) {
    if (userMapper.selectCount(null) == 0) {
      log.info("初始化 Northstar 种子数据");
      seedDicts();
      seedConfigs();
      Map<String, Long> permIds = seedPermissions();
      seedMenus();
      Long hq = dept("总部", "HQ", 0L, null, "13800002000", 1);
      Long product = dept("产品部", "PRODUCT", hq, null, "13800002001", 2);
      Long operation = dept("运营部", "OPERATION", hq, null, "13800002002", 3);
      Long engineering = dept("技术部", "ENGINEERING", hq, null, "13800002003", 4);
      Long content = dept("内容部", "CONTENT", operation, null, "13800002004", 5);
      Long pm = post("产品经理", "PM", product, "P3", 3);
      Long ops = post("运营专员", "OPS", operation, "P2", 5);
      Long fe = post("前端工程师", "FE", engineering, "P3", 4);
      Long qa = post("测试工程师", "QA", engineering, "P2", 2);
      Long reviewerPost = post("内容审核", "REVIEW", content, "P2", 3);

      Long superRole = role("super_admin", "超级管理员", "拥有全部功能权限", 1, "ALL", 1);
      Long opsRole = role("ops_manager", "运营经理", "工单、公告、报表与部分组织查看", 1, "DEPT_TREE", 2);
      Long deptRole = role("dept_admin", "部门管理员", "本部门用户与岗位", 1, "DEPT_TREE", 3);
      Long reviewerRole = role("reviewer", "审核员", "账号与内容复核", 1, "SELF", 4);
      Long memberRole = role("member", "普通成员", "工作台与个人中心", 1, "SELF", 5);

      grant(superRole, permIds.keySet().stream().map(permIds::get).toList());
      grant(opsRole, codes(permIds, Perms.DASHBOARD_VIEW, Perms.USER_VIEW, Perms.DEPT_VIEW, Perms.POST_VIEW, Perms.DICT_VIEW, Perms.AUDIT_LOGIN));
      grant(deptRole, codes(permIds, Perms.DASHBOARD_VIEW, Perms.USER_VIEW, Perms.USER_CREATE, Perms.USER_UPDATE, Perms.USER_DISABLE,
          Perms.DEPT_VIEW, Perms.POST_VIEW, Perms.POST_CREATE, Perms.POST_UPDATE, Perms.DICT_VIEW));
      grant(reviewerRole, codes(permIds, Perms.DASHBOARD_VIEW, Perms.USER_VIEW, Perms.AUDIT_LOGIN));
      grant(memberRole, codes(permIds, Perms.DASHBOARD_VIEW));

      Long adminId = user("admin", "林知远", "lin@northstar.cn", "13800001001", "active", engineering, fe, "平台最高权限账号", false);
      Long opsId = user("ops", "周明", "zhou@northstar.cn", "13800001002", "active", operation, ops, "负责活动与数据运营", false);
      Long reviewerId = user("reviewer", "宋雨", "song@northstar.cn", "13800001003", "pending", content, reviewerPost, "待完成身份复核", true);
      Long memberId = user("member", "陈果", "chen@northstar.cn", "13800001004", "active", product, pm, "日常成员账号", false);
      Long frozenId = user("zhao", "赵琳", "zhao@northstar.cn", "13800001005", "frozen", operation, ops, "账号已冻结待处理", false);

      bind(adminId, superRole);
      bind(opsId, opsRole);
      bind(reviewerId, reviewerRole);
      bind(memberId, memberRole);
      bind(frozenId, memberRole);

      notice(adminId, "欢迎使用 Northstar", "会话、权限与组织管理已经接通真实接口。", "system");
      notice(adminId, "请妥善保管初始密码", "生产环境请立即修改默认密码。", "security");
      log.info("种子账号: admin / Admin@123456 ；member / Member@123456");
    }
    ensureExtras();
  }

  private Map<String, Long> seedPermissions() {
    List<String[]> rows = List.of(
        arr(Perms.DASHBOARD_VIEW, "工作台", "MENU"),
        arr(Perms.USER_VIEW, "用户查看", "MENU"),
        arr(Perms.USER_CREATE, "新建用户", "BUTTON"),
        arr(Perms.USER_UPDATE, "编辑用户", "BUTTON"),
        arr(Perms.USER_DELETE, "删除用户", "BUTTON"),
        arr(Perms.USER_DISABLE, "停用用户", "BUTTON"),
        arr(Perms.USER_UNLOCK, "解锁用户", "BUTTON"),
        arr(Perms.USER_RESET, "重置密码", "BUTTON"),
        arr(Perms.USER_GRANT, "分配角色", "BUTTON"),
        arr(Perms.USER_SENSITIVE, "查看敏感信息", "API"),
        arr(Perms.ROLE_VIEW, "角色查看", "MENU"),
        arr(Perms.ROLE_CREATE, "新建角色", "BUTTON"),
        arr(Perms.ROLE_UPDATE, "编辑角色", "BUTTON"),
        arr(Perms.ROLE_DELETE, "删除角色", "BUTTON"),
        arr(Perms.ROLE_GRANT, "角色授权", "BUTTON"),
        arr(Perms.MENU_VIEW, "菜单查看", "MENU"),
        arr(Perms.MENU_CREATE, "新建菜单", "BUTTON"),
        arr(Perms.MENU_UPDATE, "编辑菜单", "BUTTON"),
        arr(Perms.MENU_DELETE, "删除菜单", "BUTTON"),
        arr(Perms.PERMISSION_VIEW, "权限点查看", "MENU"),
        arr(Perms.PERMISSION_CREATE, "新建权限点", "BUTTON"),
        arr(Perms.PERMISSION_UPDATE, "编辑权限点", "BUTTON"),
        arr(Perms.PERMISSION_DELETE, "删除权限点", "BUTTON"),
        arr(Perms.DICT_VIEW, "字典查看", "MENU"),
        arr(Perms.DICT_CREATE, "新建字典", "BUTTON"),
        arr(Perms.DICT_UPDATE, "编辑字典", "BUTTON"),
        arr(Perms.DICT_DELETE, "删除字典", "BUTTON"),
        arr(Perms.CONFIG_VIEW, "参数查看", "MENU"),
        arr(Perms.CONFIG_UPDATE, "更新参数", "BUTTON"),
        arr(Perms.DEPT_VIEW, "部门查看", "MENU"),
        arr(Perms.DEPT_CREATE, "新建部门", "BUTTON"),
        arr(Perms.DEPT_UPDATE, "编辑部门", "BUTTON"),
        arr(Perms.DEPT_DELETE, "删除部门", "BUTTON"),
        arr(Perms.DEPT_DISABLE, "停用部门", "BUTTON"),
        arr(Perms.POST_VIEW, "岗位查看", "MENU"),
        arr(Perms.POST_CREATE, "新建岗位", "BUTTON"),
        arr(Perms.POST_UPDATE, "编辑岗位", "BUTTON"),
        arr(Perms.POST_DELETE, "删除岗位", "BUTTON"),
        arr(Perms.AUDIT_LOGIN, "登录日志", "MENU"),
        arr(Perms.AUDIT_OPERATE, "操作日志", "MENU"),
        arr(Perms.AUDIT_ONLINE, "在线用户", "MENU"),
        arr(Perms.FILE_VIEW, "文件查看", "MENU"),
        arr(Perms.FILE_DOWNLOAD, "文件下载", "BUTTON"),
        arr(Perms.FILE_DELETE, "文件删除", "BUTTON"),
        arr(Perms.LAB_VUE3, "平台实验室", "MENU"));
    java.util.HashMap<String, Long> ids = new java.util.HashMap<>();
    for (String[] row : rows) {
      SysPermission permission = new SysPermission();
      permission.setCode(row[0]);
      permission.setName(row[1]);
      permission.setType(row[2]);
      permissionMapper.insert(permission);
      ids.put(row[0], permission.getId());
    }
    return ids;
  }

  private void seedMenus() {
    Long dashboard = menu(0L, "MENU", "工作台", "/dashboard", "dashboard/DashBoardPage", "House", Perms.DASHBOARD_VIEW, 1, 0, 1);
    Long system = menu(0L, "DIR", "系统管理", "/system", null, "Setting", null, 1, 0, 2);
    Long user = menu(system, "MENU", "用户管理", "/system/user", "system/UserHome", "User", Perms.USER_VIEW, 1, 0, 1);
    button(user, "新建用户", Perms.USER_CREATE, 1);
    button(user, "编辑用户", Perms.USER_UPDATE, 2);
    button(user, "删除用户", Perms.USER_DELETE, 3);
    button(user, "停用用户", Perms.USER_DISABLE, 4);
    button(user, "解锁用户", Perms.USER_UNLOCK, 5);
    button(user, "重置密码", Perms.USER_RESET, 6);
    button(user, "分配角色", Perms.USER_GRANT, 7);
    Long role = menu(system, "MENU", "角色管理", "/system/role", "system/RoleHome", "UserFilled", Perms.ROLE_VIEW, 1, 0, 2);
    button(role, "新建角色", Perms.ROLE_CREATE, 1);
    button(role, "编辑角色", Perms.ROLE_UPDATE, 2);
    button(role, "删除角色", Perms.ROLE_DELETE, 3);
    button(role, "角色授权", Perms.ROLE_GRANT, 4);
    Long menus = menu(system, "MENU", "菜单管理", "/system/menu", "system/MenuHome", "Menu", Perms.MENU_VIEW, 1, 0, 3);
    button(menus, "新建菜单", Perms.MENU_CREATE, 1);
    button(menus, "编辑菜单", Perms.MENU_UPDATE, 2);
    button(menus, "删除菜单", Perms.MENU_DELETE, 3);
    menu(system, "MENU", "权限点", "/system/permission", "system/PermissionHome", "Key", Perms.PERMISSION_VIEW, 1, 0, 4);
    menu(system, "MENU", "数据字典", "/system/dict", "system/DictHome", "Collection", Perms.DICT_VIEW, 1, 0, 5);
    menu(system, "MENU", "参数配置", "/system/config", "system/ConfigHome", "Tools", Perms.CONFIG_VIEW, 1, 0, 6);
    Long file = menu(system, "MENU", "文件中心", "/system/file", "system/FileHome", "Paperclip", Perms.FILE_VIEW, 1, 0, 7);
    button(file, "删除文件", Perms.FILE_DELETE, 1);
    Long org = menu(0L, "DIR", "组织管理", "/organization", null, "OfficeBuilding", null, 1, 0, 3);
    Long dept = menu(org, "MENU", "部门管理", "/organization/department", "organization/DepartmentHome", "Connection", Perms.DEPT_VIEW, 1, 0, 1);
    button(dept, "新建部门", Perms.DEPT_CREATE, 1);
    button(dept, "编辑部门", Perms.DEPT_UPDATE, 2);
    button(dept, "删除部门", Perms.DEPT_DELETE, 3);
    button(dept, "停用部门", Perms.DEPT_DISABLE, 4);
    Long post = menu(org, "MENU", "岗位管理", "/organization/position", "organization/PositionHome", "Postcard", Perms.POST_VIEW, 1, 0, 2);
    button(post, "新建岗位", Perms.POST_CREATE, 1);
    button(post, "编辑岗位", Perms.POST_UPDATE, 2);
    button(post, "删除岗位", Perms.POST_DELETE, 3);
    Long audit = menu(0L, "DIR", "审计安全", "/audit", null, "Lock", null, 1, 0, 4);
    menu(audit, "MENU", "登录日志", "/audit/login", "audit/LoginLogHome", "Document", Perms.AUDIT_LOGIN, 1, 0, 1);
    menu(audit, "MENU", "操作日志", "/audit/operate", "audit/OperateLogHome", "Tickets", Perms.AUDIT_OPERATE, 1, 0, 2);
    menu(audit, "MENU", "在线用户", "/audit/online", "audit/OnlineUserHome", "Monitor", Perms.AUDIT_ONLINE, 1, 0, 3);
    menu(0L, "MENU", "个人中心", "/profile", "profile/ProfileHome", "User", null, 0, 1, 90);
    menu(0L, "MENU", "平台实验室", "/lab/vue3-learning", "learning/Vue3Learning", "Cpu", Perms.LAB_VUE3, 1, 0, 99);
  }

  private void seedDicts() {
    dictType("user_status", "用户状态");
    dictData("user_status", "启用", "active", 1);
    dictData("user_status", "停用", "frozen", 2);
    dictData("user_status", "待审核", "pending", 3);
    dictType("org_status", "组织状态");
    dictData("org_status", "启用", "active", 1);
    dictData("org_status", "停用", "frozen", 2);
    dictType("post_level", "职级");
    dictData("post_level", "P1", "P1", 1);
    dictData("post_level", "P2", "P2", 2);
    dictData("post_level", "P3", "P3", 3);
    dictData("post_level", "P4", "P4", 4);
  }

  private void seedConfigs() {
    config("password.min-length", "8", "密码最小长度");
    config("password.max-length", "32", "密码最大长度");
    config("login.max-failures", "5", "登录失败锁定阈值");
    config("login.lock-minutes", "15", "锁定时长（分钟）");
    config("login.captcha-after", "3", "失败几次后出现验证码");
  }

  private Long menu(Long parentId, String type, String name, String path, String component, String icon, String perm, int visible, int hidden, int sort) {
    SysMenu menu = new SysMenu();
    menu.setParentId(parentId);
    menu.setType(type);
    menu.setName(name);
    menu.setPath(path);
    menu.setComponent(component);
    menu.setIcon(icon);
    menu.setPermission(perm);
    menu.setVisible(visible);
    menu.setHidden(hidden);
    menu.setSortNo(sort);
    menu.setStatus("active");
    menuMapper.insert(menu);
    return menu.getId();
  }

  private void button(Long parentId, String name, String perm, int sort) {
    menu(parentId, "BUTTON", name, null, null, null, perm, 0, 0, sort);
  }

  private Long dept(String name, String code, Long parentId, Long leaderId, String phone, int sort) {
    SysDept dept = new SysDept();
    dept.setName(name);
    dept.setCode(code);
    dept.setParentId(parentId);
    dept.setLeaderId(leaderId);
    dept.setPhone(phone);
    dept.setStatus("active");
    dept.setSortNo(sort);
    deptMapper.insert(dept);
    return dept.getId();
  }

  private Long post(String name, String code, Long deptId, String level, int headcount) {
    SysPost post = new SysPost();
    post.setName(name);
    post.setCode(code);
    post.setDeptId(deptId);
    post.setLevel(level);
    post.setHeadcount(headcount);
    post.setStatus("active");
    postMapper.insert(post);
    return post.getId();
  }

  private Long role(String code, String name, String desc, int builtin, String dataScope, int sort) {
    SysRole role = new SysRole();
    role.setCode(code);
    role.setName(name);
    role.setDescription(desc);
    role.setBuiltin(builtin);
    role.setDataScope(dataScope);
    role.setStatus("active");
    role.setSortNo(sort);
    roleMapper.insert(role);
    return role.getId();
  }

  private void grant(Long roleId, List<Long> permissionIds) {
    for (Long permissionId : permissionIds) {
      if (permissionId == null) {
        continue;
      }
      SysRolePermission rel = new SysRolePermission();
      rel.setRoleId(roleId);
      rel.setPermissionId(permissionId);
      rolePermissionMapper.insert(rel);
    }
  }

  private Long user(String username, String name, String email, String phone, String status, Long deptId, Long postId, String remark, boolean mustChange) {
    SysUser user = new SysUser();
    user.setUsername(username);
    user.setPassword(PasswordUtils.hash(passwordOf(username)));
    user.setDisplayName(name);
    user.setEmail(email);
    user.setPhone(phone);
    user.setStatus(status);
    user.setDeptId(deptId);
    user.setPostId(postId);
    user.setRemark(remark);
    user.setMustChangePwd(mustChange ? 1 : 0);
    user.setLoginFailCount(0);
    user.setPasswordChangedAt(LocalDateTime.now());
    userMapper.insert(user);
    return user.getId();
  }

  private void ensureExtras() {
    ensurePermission(Perms.AUDIT_ONLINE, "在线用户", "MENU");
    ensurePermission(Perms.FILE_VIEW, "文件查看", "MENU");
    ensurePermission(Perms.FILE_DOWNLOAD, "文件下载", "BUTTON");
    ensurePermission(Perms.FILE_DELETE, "文件删除", "BUTTON");
    Long systemId = findMenuId("/system", "DIR");
    Long auditId = findMenuId("/audit", "DIR");
    if (systemId != null) {
      Long fileId = ensureMenu(systemId, "MENU", "文件中心", "/system/file", "system/FileHome", "Paperclip", Perms.FILE_VIEW, 1, 0, 7);
      if (fileId != null) {
        ensureButton(fileId, "删除文件", Perms.FILE_DELETE, 1);
      }
    }
    if (auditId != null) {
      ensureMenu(auditId, "MENU", "在线用户", "/audit/online", "audit/OnlineUserHome", "Monitor", Perms.AUDIT_ONLINE, 1, 0, 3);
    }
  }

  private void ensurePermission(String code, String name, String type) {
    Long count = permissionMapper.selectCount(Wrappers.<SysPermission>lambdaQuery().eq(SysPermission::getCode, code));
    if (count != null && count > 0) {
      return;
    }
    SysPermission permission = new SysPermission();
    permission.setCode(code);
    permission.setName(name);
    permission.setType(type);
    permissionMapper.insert(permission);
  }

  private Long findMenuId(String path, String type) {
    SysMenu menu = menuMapper.selectOne(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getPath, path).eq(SysMenu::getType, type).last("limit 1"));
    return menu == null ? null : menu.getId();
  }

  private Long ensureMenu(Long parentId, String type, String name, String path, String component, String icon, String perm, int visible, int hidden, int sort) {
    SysMenu exists = menuMapper.selectOne(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getPath, path).last("limit 1"));
    if (exists != null) {
      return exists.getId();
    }
    return menu(parentId, type, name, path, component, icon, perm, visible, hidden, sort);
  }

  private void ensureButton(Long parentId, String name, String perm, int sort) {
    Long count = menuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery()
        .eq(SysMenu::getParentId, parentId)
        .eq(SysMenu::getPermission, perm)
        .eq(SysMenu::getType, "BUTTON"));
    if (count != null && count > 0) {
      return;
    }
    button(parentId, name, perm, sort);
  }

  private String passwordOf(String username) {
    if ("admin".equals(username)) {
      return "Admin@123456";
    }
    if ("ops".equals(username)) {
      return "Ops@123456";
    }
    if ("reviewer".equals(username)) {
      return "Reviewer@123456";
    }
    return "Member@123456";
  }

  private void bind(Long userId, Long roleId) {
    SysUserRole rel = new SysUserRole();
    rel.setUserId(userId);
    rel.setRoleId(roleId);
    userRoleMapper.insert(rel);
  }

  private void dictType(String code, String name) {
    SysDictType type = new SysDictType();
    type.setCode(code);
    type.setName(name);
    type.setStatus("active");
    dictTypeMapper.insert(type);
  }

  private void dictData(String dictCode, String label, String value, int sort) {
    SysDictData data = new SysDictData();
    data.setDictCode(dictCode);
    data.setLabel(label);
    data.setDictValue(value);
    data.setSortNo(sort);
    data.setStatus("active");
    dictDataMapper.insert(data);
  }

  private void config(String key, String value, String remark) {
    SysConfig config = new SysConfig();
    config.setConfigKey(key);
    config.setConfigValue(value);
    config.setRemark(remark);
    configMapper.insert(config);
  }

  private void notice(Long userId, String title, String content, String type) {
    SysNotice notice = new SysNotice();
    notice.setUserId(userId);
    notice.setTitle(title);
    notice.setContent(content);
    notice.setType(type);
    notice.setReadFlag(0);
    notice.setCreateTime(LocalDateTime.now());
    noticeMapper.insert(notice);
  }

  private List<Long> codes(Map<String, Long> permIds, String... codes) {
    return java.util.Arrays.stream(codes).map(permIds::get).toList();
  }

  private String[] arr(String... values) {
    return values;
  }
}
