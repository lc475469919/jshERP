-- Baseline migration for yize-erp.
-- Business tables will be introduced step by step from the blueprint.

CREATE TABLE IF NOT EXISTS yz_sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  mobile VARCHAR(32) NULL,
  password_hash VARCHAR(128) NOT NULL,
  password_salt VARCHAR(64) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  last_login_at DATETIME NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_sys_user_account (account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS yz_sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS yz_sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_sys_user_role (user_id, role_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色';

CREATE TABLE IF NOT EXISTS yz_sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT NOT NULL DEFAULT 0,
  menu_type VARCHAR(16) NOT NULL,
  menu_name VARCHAR(64) NOT NULL,
  route_path VARCHAR(128) NULL,
  component_path VARCHAR(128) NULL,
  permission_code VARCHAR(128) NULL,
  icon VARCHAR(64) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  visible TINYINT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_yz_sys_menu_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单';

CREATE TABLE IF NOT EXISTS yz_sys_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_sys_role_menu (role_id, menu_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单权限';

CREATE TABLE IF NOT EXISTS yz_sys_dict_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_code VARCHAR(64) NOT NULL,
  dict_name VARCHAR(64) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_sys_dict_type_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

CREATE TABLE IF NOT EXISTS yz_sys_dict_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_type_id BIGINT NOT NULL,
  item_label VARCHAR(64) NOT NULL,
  item_value VARCHAR(64) NOT NULL,
  color VARCHAR(32) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_yz_sys_dict_item_type (dict_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项';

CREATE TABLE IF NOT EXISTS yz_sys_number_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  document_type VARCHAR(64) NOT NULL,
  document_name VARCHAR(64) NOT NULL,
  prefix VARCHAR(32) NOT NULL,
  date_pattern VARCHAR(32) NOT NULL DEFAULT 'yyyyMMdd',
  serial_length INT NOT NULL DEFAULT 4,
  current_serial INT NOT NULL DEFAULT 0,
  reset_daily TINYINT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_sys_number_rule_type (document_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据编号规则';

CREATE TABLE IF NOT EXISTS yz_sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  user_name VARCHAR(64) NULL,
  module_name VARCHAR(64) NOT NULL,
  operation_type VARCHAR(64) NOT NULL,
  business_no VARCHAR(64) NULL,
  request_result VARCHAR(32) NOT NULL,
  message VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

INSERT INTO yz_sys_user (id, account, name, mobile, password_hash, password_salt, status, remark)
VALUES (1, 'admin', '超级管理员', '13800000000', '4cfb93bd54342fb825ec1f957fd698394023558a7722cf7c736b319b26a1a79c', 'yz-admin-salt', 1, '初始化管理员，默认密码 admin123')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO yz_sys_role (id, role_code, role_name, status, remark)
VALUES (1, 'SUPER_ADMIN', '超级管理员', 1, '拥有全部权限')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO yz_sys_user_role (user_id, role_id)
VALUES (1, 1)
ON DUPLICATE KEY UPDATE deleted = 0;

INSERT INTO yz_sys_menu (id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_order, visible, status) VALUES
(1, 0, 'DIR', '系统管理', '/system', NULL, 'system', 'SettingOutlined', 10, 1, 1),
(2, 1, 'PAGE', '用户管理', '/system/users', 'system/UserList', 'system:user:view', 'UserOutlined', 11, 1, 1),
(3, 1, 'PAGE', '角色管理', '/system/roles', 'system/RoleList', 'system:role:view', 'TeamOutlined', 12, 1, 1),
(4, 1, 'PAGE', '菜单管理', '/system/menus', 'system/MenuList', 'system:menu:view', 'MenuOutlined', 13, 1, 1),
(5, 1, 'PAGE', '字典管理', '/system/dicts', 'system/DictList', 'system:dict:view', 'ProfileOutlined', 14, 1, 1),
(6, 1, 'PAGE', '编号规则', '/system/number-rules', 'system/NumberRuleList', 'system:number-rule:view', 'NumberOutlined', 15, 1, 1),
(7, 1, 'PAGE', '操作日志', '/system/logs', 'system/OperationLogList', 'system:log:view', 'FileSearchOutlined', 16, 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO yz_sys_role_menu (role_id, menu_id)
SELECT 1, id FROM yz_sys_menu
ON DUPLICATE KEY UPDATE deleted = 0;

INSERT INTO yz_sys_dict_type (id, dict_code, dict_name, status, remark) VALUES
(1, 'common_status', '通用状态', 1, '启用/停用'),
(2, 'menu_type', '菜单类型', 1, '目录/页面/按钮'),
(3, 'document_status', '单据状态', 1, '草稿/已提交/已审核/已作废')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO yz_sys_dict_item (dict_type_id, item_label, item_value, color, sort_order, status) VALUES
(1, '启用', '1', 'green', 1, 1),
(1, '停用', '0', 'red', 2, 1),
(2, '目录', 'DIR', 'blue', 1, 1),
(2, '页面', 'PAGE', 'purple', 2, 1),
(2, '按钮', 'BUTTON', 'orange', 3, 1),
(3, '草稿', 'DRAFT', 'default', 1, 1),
(3, '已提交', 'SUBMITTED', 'blue', 2, 1),
(3, '已审核', 'APPROVED', 'green', 3, 1),
(3, '已作废', 'VOIDED', 'red', 4, 1);

INSERT INTO yz_sys_number_rule (document_type, document_name, prefix, date_pattern, serial_length, current_serial, reset_daily, status) VALUES
('PURCHASE_ORDER', '采购订单', 'PO', 'yyyyMMdd', 4, 0, 1, 1),
('SALES_ORDER', '销售订单', 'SO', 'yyyyMMdd', 4, 0, 1, 1),
('PRODUCTION_TASK', '生产任务', 'MO', 'yyyyMMdd', 4, 0, 1, 1)
ON DUPLICATE KEY UPDATE document_name = VALUES(document_name);
