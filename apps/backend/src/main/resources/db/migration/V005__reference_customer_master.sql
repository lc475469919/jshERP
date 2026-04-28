CREATE TABLE IF NOT EXISTS yz_md_customer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_code VARCHAR(64) NOT NULL,
  customer_name VARCHAR(128) NOT NULL,
  category_id BIGINT NULL,
  contact_name VARCHAR(64) NULL,
  mobile VARCHAR(32) NULL,
  telephone VARCHAR(64) NULL,
  email VARCHAR(128) NULL,
  country VARCHAR(64) NULL,
  address VARCHAR(255) NULL,
  salesperson_name VARCHAR(64) NULL,
  logistics_company VARCHAR(128) NULL,
  linked_supplier VARCHAR(128) NULL,
  prepaid_amount DECIMAL(18,4) NOT NULL DEFAULT 0,
  member_level VARCHAR(64) NULL,
  self_order_enabled TINYINT NOT NULL DEFAULT 0,
  login_account VARCHAR(64) NULL,
  credit_limit DECIMAL(18,4) NOT NULL DEFAULT 0,
  invoice_enabled TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  tags VARCHAR(255) NULL,
  follow_count INT NOT NULL DEFAULT 0,
  total_deal_amount DECIMAL(18,4) NOT NULL DEFAULT 0,
  last_deal_date VARCHAR(64) NULL,
  remark VARCHAR(255) NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_yz_md_customer_code (customer_code),
  KEY idx_yz_md_customer_category (category_id),
  KEY idx_yz_md_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息';

INSERT INTO yz_md_business_category (id, category_type, parent_id, category_code, category_name, sort_order, sale_enabled, purchase_enabled) VALUES
(3, 'CUS', 0, 'CUS001', '默认客户类别', 1, 1, 1)
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);
