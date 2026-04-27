SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'yz_md_product' AND column_name = 'image_url') = 0,
  'ALTER TABLE yz_md_product ADD COLUMN image_url VARCHAR(255) NULL AFTER brand_id',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'yz_md_product' AND column_name = 'detail_description') = 0,
  'ALTER TABLE yz_md_product ADD COLUMN detail_description TEXT NULL AFTER remark',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'yz_md_product' AND column_name = 'production_department') = 0,
  'ALTER TABLE yz_md_product ADD COLUMN production_department VARCHAR(128) NULL AFTER detail_description',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
