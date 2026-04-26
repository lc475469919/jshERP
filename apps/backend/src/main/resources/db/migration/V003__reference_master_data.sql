CREATE TABLE IF NOT EXISTS yz_md_business_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_type VARCHAR(16) NOT NULL,
  parent_id BIGINT NOT NULL DEFAULT 0,
  category_code VARCHAR(64) NULL,
  category_name VARCHAR(64) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  sale_enabled TINYINT NOT NULL DEFAULT 1,
  purchase_enabled TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_yz_md_biz_category_type (category_type),
  KEY idx_yz_md_biz_category_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务类别树';

CREATE TABLE IF NOT EXISTS yz_md_product_attr (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  attr_type VARCHAR(16) NOT NULL,
  attr_name VARCHAR(64) NOT NULL,
  allow_decimal TINYINT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_yz_md_product_attr_type (attr_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性';

CREATE TABLE IF NOT EXISTS yz_md_product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_code VARCHAR(64) NOT NULL,
  product_name VARCHAR(128) NOT NULL,
  mnemonic_code VARCHAR(64) NULL,
  category_id BIGINT NULL,
  barcode VARCHAR(64) NULL,
  brand_id BIGINT NULL,
  specification VARCHAR(128) NULL,
  supplier_id BIGINT NULL,
  shelf_no VARCHAR(64) NULL,
  default_unit_id BIGINT NULL,
  purchase_price DECIMAL(18,4) NOT NULL DEFAULT 0,
  cost_price DECIMAL(18,4) NOT NULL DEFAULT 0,
  wholesale_price DECIMAL(18,4) NOT NULL DEFAULT 0,
  retail_price DECIMAL(18,4) NOT NULL DEFAULT 0,
  purchase_tax_rate DECIMAL(8,4) NOT NULL DEFAULT 0,
  sale_tax_rate DECIMAL(8,4) NOT NULL DEFAULT 0,
  min_stock DECIMAL(18,4) NOT NULL DEFAULT 0,
  max_stock DECIMAL(18,4) NOT NULL DEFAULT 0,
  color_names VARCHAR(255) NULL,
  serial_enabled TINYINT NOT NULL DEFAULT 0,
  batch_enabled TINYINT NOT NULL DEFAULT 0,
  expiry_enabled TINYINT NOT NULL DEFAULT 0,
  sale_enabled TINYINT NOT NULL DEFAULT 1,
  purchase_enabled TINYINT NOT NULL DEFAULT 1,
  self_made_enabled TINYINT NOT NULL DEFAULT 0,
  outsourcing_enabled TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_md_product_code (product_code),
  KEY idx_yz_md_product_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息';

CREATE TABLE IF NOT EXISTS yz_md_supplier (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  supplier_code VARCHAR(64) NOT NULL,
  supplier_name VARCHAR(128) NOT NULL,
  category_id BIGINT NULL,
  contact_name VARCHAR(64) NULL,
  mobile VARCHAR(32) NULL,
  email VARCHAR(128) NULL,
  telephone VARCHAR(64) NULL,
  province_code VARCHAR(32) NULL,
  city_code VARCHAR(32) NULL,
  district_code VARCHAR(32) NULL,
  address VARCHAR(255) NULL,
  prepaid_amount DECIMAL(18,4) NOT NULL DEFAULT 0,
  invoice_enabled TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_md_supplier_code (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商';

CREATE TABLE IF NOT EXISTS yz_md_project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(64) NOT NULL,
  project_name VARCHAR(128) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_md_project_code (project_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目信息';

CREATE TABLE IF NOT EXISTS yz_md_logistics_company (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  logistics_code VARCHAR(64) NOT NULL,
  logistics_name VARCHAR(128) NOT NULL,
  express_type VARCHAR(64) NULL,
  express_company_name VARCHAR(128) NULL,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_md_logistics_code (logistics_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流公司';

CREATE TABLE IF NOT EXISTS yz_md_sender (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  logistics_id BIGINT NOT NULL,
  sender_name VARCHAR(64) NOT NULL,
  phone VARCHAR(32) NULL,
  address VARCHAR(255) NULL,
  default_flag TINYINT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_yz_md_sender_logistics (logistics_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流寄件人';

INSERT INTO yz_sys_menu (id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_order, visible, status) VALUES
(200, 0, 'DIR', '基础资料', '/master-data', NULL, 'master-data', 'DatabaseOutlined', 20, 1, 1),
(201, 200, 'DIR', '商品管理', '/master-data/product', NULL, 'master-data:product', 'AppstoreOutlined', 21, 1, 1),
(202, 201, 'PAGE', '商品信息', '/master-data/products', 'masterdata/ProductList', 'master-data:product:view', 'BarcodeOutlined', 22, 1, 1),
(203, 201, 'PAGE', '商品属性', '/master-data/product-attrs', 'masterdata/ProductAttrList', 'master-data:product-attr:view', 'TagsOutlined', 23, 1, 1),
(204, 200, 'DIR', '信息管理', '/master-data/info', NULL, 'master-data:info', 'ProfileOutlined', 24, 1, 1),
(205, 204, 'PAGE', '供应商管理', '/master-data/suppliers', 'masterdata/SupplierList', 'master-data:supplier:view', 'ShopOutlined', 25, 1, 1),
(206, 204, 'PAGE', '项目信息', '/master-data/projects', 'masterdata/ProjectList', 'master-data:project:view', 'ProjectOutlined', 26, 1, 1),
(207, 204, 'PAGE', '物流公司', '/master-data/logistics', 'masterdata/LogisticsList', 'master-data:logistics:view', 'TruckOutlined', 27, 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO yz_sys_role_menu (role_id, menu_id)
SELECT 1, id FROM yz_sys_menu WHERE id >= 200 AND id < 300
ON DUPLICATE KEY UPDATE deleted = 0;

INSERT INTO yz_md_business_category (id, category_type, parent_id, category_code, category_name, sort_order, sale_enabled, purchase_enabled) VALUES
(1, 'COM', 0, 'COM001', '默认商品类别', 1, 1, 1),
(2, 'SUP', 0, 'SUP001', '默认供应商类别', 1, 1, 1)
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);

INSERT INTO yz_md_product_attr (id, attr_type, attr_name, allow_decimal, sort_order, remark) VALUES
(1, 'UNIT', '个', 0, 1, '默认计量单位'),
(2, 'COLOR', '默认颜色', 0, 1, NULL),
(3, 'BRAND', '默认品牌', 0, 1, NULL)
ON DUPLICATE KEY UPDATE attr_name = VALUES(attr_name);
