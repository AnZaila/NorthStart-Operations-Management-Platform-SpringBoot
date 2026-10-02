CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(120) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  email VARCHAR(128),
  phone VARCHAR(32),
  avatar VARCHAR(512),
  status VARCHAR(16) NOT NULL DEFAULT 'pending',
  dept_id BIGINT,
  post_id BIGINT,
  must_change_pwd TINYINT NOT NULL DEFAULT 1,
  password_changed_at DATETIME,
  login_fail_count INT NOT NULL DEFAULT 0,
  locked_until DATETIME,
  last_login_at DATETIME,
  last_login_ip VARCHAR(64),
  remark VARCHAR(255),
  create_time DATETIME,
  update_time DATETIME,
  create_by BIGINT,
  update_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255),
  builtin TINYINT NOT NULL DEFAULT 0,
  data_scope VARCHAR(16) NOT NULL DEFAULT 'SELF',
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  sort_no INT NOT NULL DEFAULT 0,
  create_time DATETIME,
  update_time DATETIME,
  create_by BIGINT,
  update_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(128) NOT NULL,
  name VARCHAR(64) NOT NULL,
  type VARCHAR(16) NOT NULL,
  create_time DATETIME,
  update_time DATETIME,
  create_by BIGINT,
  update_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_role_dept (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT DEFAULT 0,
  type VARCHAR(16) NOT NULL,
  name VARCHAR(64) NOT NULL,
  path VARCHAR(128),
  component VARCHAR(128),
  icon VARCHAR(64),
  permission VARCHAR(128),
  visible TINYINT NOT NULL DEFAULT 1,
  hidden TINYINT NOT NULL DEFAULT 0,
  sort_no INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  create_time DATETIME,
  update_time DATETIME,
  create_by BIGINT,
  update_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_dept (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT DEFAULT 0,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL,
  leader_id BIGINT,
  phone VARCHAR(32),
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  sort_no INT NOT NULL DEFAULT 0,
  remark VARCHAR(255),
  create_time DATETIME,
  update_time DATETIME,
  create_by BIGINT,
  update_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dept_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL,
  level VARCHAR(16),
  headcount INT NOT NULL DEFAULT 1,
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  remark VARCHAR(255),
  create_time DATETIME,
  update_time DATETIME,
  create_by BIGINT,
  update_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  create_time DATETIME,
  update_time DATETIME,
  create_by BIGINT,
  update_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_dict_data (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  dict_value VARCHAR(64) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  create_time DATETIME,
  update_time DATETIME,
  create_by BIGINT,
  update_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(64) NOT NULL,
  config_value VARCHAR(1024),
  remark VARCHAR(255),
  create_time DATETIME,
  update_time DATETIME,
  create_by BIGINT,
  update_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_login_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  username VARCHAR(64),
  success TINYINT NOT NULL,
  ip VARCHAR(64),
  user_agent VARCHAR(512),
  reason VARCHAR(255),
  create_time DATETIME
);

CREATE TABLE IF NOT EXISTS sys_oper_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  username VARCHAR(64),
  module VARCHAR(64),
  action VARCHAR(64),
  resource VARCHAR(64),
  resource_id VARCHAR(64),
  request_id VARCHAR(64),
  ip VARCHAR(64),
  success TINYINT NOT NULL DEFAULT 1,
  duration_ms BIGINT,
  detail TEXT,
  create_time DATETIME
);

CREATE TABLE IF NOT EXISTS sys_refresh_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(64) NOT NULL,
  expire_time DATETIME NOT NULL,
  revoked TINYINT NOT NULL DEFAULT 0,
  user_agent VARCHAR(512),
  ip VARCHAR(64),
  create_time DATETIME
);

CREATE TABLE IF NOT EXISTS sys_password_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  password VARCHAR(120) NOT NULL,
  create_time DATETIME
);

CREATE TABLE IF NOT EXISTS sys_notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  content VARCHAR(512),
  type VARCHAR(32),
  read_flag TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME
);

CREATE TABLE IF NOT EXISTS sys_file (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  original_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(128) NOT NULL,
  content_type VARCHAR(128),
  size_bytes BIGINT NOT NULL DEFAULT 0,
  biz_type VARCHAR(32),
  user_id BIGINT,
  create_time DATETIME
);

ALTER TABLE sys_refresh_token ADD COLUMN device VARCHAR(64);
