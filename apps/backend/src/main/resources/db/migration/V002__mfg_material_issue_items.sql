CREATE TABLE yz_mfg_material_issue_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  issue_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  material_id BIGINT NOT NULL,
  material_name VARCHAR(150) NOT NULL,
  warehouse_id BIGINT,
  warehouse_name VARCHAR(100),
  plan_qty DECIMAL(24,6) NOT NULL DEFAULT 0,
  issue_qty DECIMAL(24,6) NOT NULL DEFAULT 0,
  unit_name VARCHAR(50),
  remark VARCHAR(500),
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_mfg_issue_item_issue (issue_id),
  KEY idx_mfg_issue_item_task (task_id),
  KEY idx_mfg_issue_item_material (material_id)
) COMMENT='生产领料明细';
