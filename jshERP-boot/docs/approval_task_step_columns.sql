-- 补齐历史库的多级审批任务步骤字段。
-- 首次安装的新库在 jsh_erp.sql 中已经包含这些字段；旧库若早于多步骤审批版本，需要执行本脚本。

SET @schema_name = DATABASE();

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `jsh_approval_task` ADD COLUMN `current_step_no` int NULL DEFAULT 1 COMMENT ''当前审批步骤'' AFTER `approve_remark`',
    'SELECT ''jsh_approval_task.current_step_no already exists'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'jsh_approval_task'
    AND COLUMN_NAME = 'current_step_no'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `jsh_approval_task` ADD COLUMN `total_step` int NULL DEFAULT 1 COMMENT ''总审批步骤'' AFTER `current_step_no`',
    'SELECT ''jsh_approval_task.total_step already exists'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'jsh_approval_task'
    AND COLUMN_NAME = 'total_step'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
