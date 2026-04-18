-- Lightweight production module for jshERP.

CREATE TABLE IF NOT EXISTS `jsh_production_bom` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bom_no` varchar(64) DEFAULT NULL COMMENT 'BOM编号',
  `name` varchar(100) NOT NULL COMMENT 'BOM名称',
  `material_id` bigint DEFAULT NULL COMMENT '成品商品id',
  `material_extend_id` bigint DEFAULT NULL COMMENT '成品扩展id',
  `material_name` varchar(100) DEFAULT NULL COMMENT '成品名称',
  `material_unit` varchar(20) DEFAULT NULL COMMENT '成品单位',
  `output_quantity` decimal(24,6) DEFAULT 1.000000 COMMENT '标准产出数量',
  `enabled` bit(1) DEFAULT b'1' COMMENT '是否启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  `delete_flag` varchar(1) DEFAULT '0' COMMENT '删除标记，0未删除，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_production_bom_tenant` (`tenant_id`, `delete_flag`),
  KEY `idx_production_bom_material` (`material_id`, `material_extend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='生产BOM';

CREATE TABLE IF NOT EXISTS `jsh_production_bom_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bom_id` bigint NOT NULL COMMENT 'BOM主表id',
  `material_id` bigint DEFAULT NULL COMMENT '原料商品id',
  `material_extend_id` bigint DEFAULT NULL COMMENT '原料扩展id',
  `material_name` varchar(100) DEFAULT NULL COMMENT '原料名称',
  `material_unit` varchar(20) DEFAULT NULL COMMENT '原料单位',
  `quantity` decimal(24,6) DEFAULT 1.000000 COMMENT '标准用量',
  `loss_rate` decimal(24,6) DEFAULT 0.000000 COMMENT '损耗率百分比',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `sort` int DEFAULT NULL COMMENT '排序',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  `delete_flag` varchar(1) DEFAULT '0' COMMENT '删除标记，0未删除，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_production_bom_item_bom` (`bom_id`, `delete_flag`),
  KEY `idx_production_bom_item_material` (`material_id`, `material_extend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='生产BOM明细';

CREATE TABLE IF NOT EXISTS `jsh_production_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) DEFAULT NULL COMMENT '生产任务编号',
  `bom_id` bigint DEFAULT NULL COMMENT 'BOM主表id',
  `material_id` bigint DEFAULT NULL COMMENT '成品商品id',
  `material_extend_id` bigint DEFAULT NULL COMMENT '成品扩展id',
  `material_name` varchar(100) DEFAULT NULL COMMENT '成品名称',
  `material_unit` varchar(20) DEFAULT NULL COMMENT '成品单位',
  `plan_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '计划生产数量',
  `finished_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '已完工数量',
  `plan_start_date` datetime DEFAULT NULL COMMENT '计划开工日期',
  `plan_finish_date` datetime DEFAULT NULL COMMENT '计划完工日期',
  `status` varchar(20) DEFAULT '草稿' COMMENT '状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  `delete_flag` varchar(1) DEFAULT '0' COMMENT '删除标记，0未删除，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_production_order_tenant` (`tenant_id`, `delete_flag`, `status`),
  KEY `idx_production_order_bom` (`bom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='生产任务';

CREATE TABLE IF NOT EXISTS `jsh_production_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '生产任务id',
  `bom_item_id` bigint DEFAULT NULL COMMENT 'BOM明细id',
  `material_id` bigint DEFAULT NULL COMMENT '原料商品id',
  `material_extend_id` bigint DEFAULT NULL COMMENT '原料扩展id',
  `material_name` varchar(100) DEFAULT NULL COMMENT '原料名称',
  `material_unit` varchar(20) DEFAULT NULL COMMENT '原料单位',
  `planned_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '计划用料数量',
  `issued_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '已领料数量',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `sort` int DEFAULT NULL COMMENT '排序',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  `delete_flag` varchar(1) DEFAULT '0' COMMENT '删除标记，0未删除，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_production_order_item_order` (`order_id`, `delete_flag`),
  KEY `idx_production_order_item_material` (`material_id`, `material_extend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='生产任务用料';

CREATE TABLE IF NOT EXISTS `jsh_production_material_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '生产任务id',
  `order_item_id` bigint DEFAULT NULL COMMENT '生产任务用料id',
  `material_id` bigint DEFAULT NULL COMMENT '原料商品id',
  `material_extend_id` bigint DEFAULT NULL COMMENT '原料扩展id',
  `material_name` varchar(100) DEFAULT NULL COMMENT '原料名称',
  `material_unit` varchar(20) DEFAULT NULL COMMENT '原料单位',
  `quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '登记用量',
  `record_time` datetime DEFAULT NULL COMMENT '登记时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  `delete_flag` varchar(1) DEFAULT '0' COMMENT '删除标记，0未删除，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_production_material_record_order` (`order_id`, `delete_flag`),
  KEY `idx_production_material_record_item` (`order_item_id`, `delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用料登记';

CREATE TABLE IF NOT EXISTS `jsh_production_process` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `process_no` varchar(64) DEFAULT NULL COMMENT '工序编号',
  `name` varchar(100) NOT NULL COMMENT '工序名称',
  `wage_type` varchar(20) DEFAULT NULL COMMENT '计价方式',
  `unit_price` decimal(24,6) DEFAULT 0.000000 COMMENT '工序单价',
  `enabled` bit(1) DEFAULT b'1' COMMENT '是否启用',
  `sort` int DEFAULT NULL COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  `delete_flag` varchar(1) DEFAULT '0' COMMENT '删除标记，0未删除，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_production_process_tenant` (`tenant_id`, `delete_flag`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工序管理';

CREATE TABLE IF NOT EXISTS `jsh_production_process_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '生产任务id',
  `process_id` bigint NOT NULL COMMENT '工序id',
  `process_name` varchar(100) DEFAULT NULL COMMENT '工序名称',
  `worker_name` varchar(100) DEFAULT NULL COMMENT '汇报人',
  `good_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '合格数量',
  `defect_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '不良数量',
  `scrap_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '报废数量',
  `report_time` datetime DEFAULT NULL COMMENT '汇报时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  `delete_flag` varchar(1) DEFAULT '0' COMMENT '删除标记，0未删除，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_production_process_report_order` (`order_id`, `delete_flag`),
  KEY `idx_production_process_report_process` (`process_id`, `delete_flag`),
  KEY `idx_production_process_report_tenant` (`tenant_id`, `delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工序汇报';

CREATE TABLE IF NOT EXISTS `jsh_production_quality_inspection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '生产任务id',
  `inspector_name` varchar(100) DEFAULT NULL COMMENT '检验员',
  `good_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '合格数量',
  `defect_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '不良数量',
  `scrap_quantity` decimal(24,6) DEFAULT 0.000000 COMMENT '报废数量',
  `defect_item` varchar(100) DEFAULT NULL COMMENT '不良品项',
  `inspect_time` datetime DEFAULT NULL COMMENT '检验时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  `delete_flag` varchar(1) DEFAULT '0' COMMENT '删除标记，0未删除，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_production_quality_order` (`order_id`, `delete_flag`),
  KEY `idx_production_quality_tenant` (`tenant_id`, `delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='生产质检';

INSERT INTO `jsh_function` (`id`, `number`, `name`, `parent_number`, `url`, `component`, `state`, `sort`, `enabled`, `type`, `push_btn`, `icon`, `delete_flag`)
SELECT 270, '0901', '生产管理', '0', '/production', '/layouts/TabLayout', b'0', '0410', b'1', '电脑版', '', 'tool', '0'
WHERE NOT EXISTS (SELECT 1 FROM `jsh_function` WHERE `number` = '0901' AND ifnull(`delete_flag`, '0') != '1');

INSERT INTO `jsh_function` (`id`, `number`, `name`, `parent_number`, `url`, `component`, `state`, `sort`, `enabled`, `type`, `push_btn`, `icon`, `delete_flag`)
SELECT 271, '090101', 'BOM管理', '0901', '/production/bom', '/production/BomList', b'0', '0411', b'1', '电脑版', '1', 'profile', '0'
WHERE NOT EXISTS (SELECT 1 FROM `jsh_function` WHERE `number` = '090101' AND ifnull(`delete_flag`, '0') != '1');

INSERT INTO `jsh_function` (`id`, `number`, `name`, `parent_number`, `url`, `component`, `state`, `sort`, `enabled`, `type`, `push_btn`, `icon`, `delete_flag`)
SELECT 272, '090102', '生产任务', '0901', '/production/order', '/production/OrderList', b'0', '0412', b'1', '电脑版', '1', 'profile', '0'
WHERE NOT EXISTS (SELECT 1 FROM `jsh_function` WHERE `number` = '090102' AND ifnull(`delete_flag`, '0') != '1');

UPDATE `jsh_function`
SET `name` = '生产任务'
WHERE `number` = '090102' AND ifnull(`delete_flag`, '0') != '1';

INSERT INTO `jsh_function` (`id`, `number`, `name`, `parent_number`, `url`, `component`, `state`, `sort`, `enabled`, `type`, `push_btn`, `icon`, `delete_flag`)
SELECT 273, '090103', '生产领料', '0901', '/production/material_issue', '/production/MaterialIssueList', b'0', '0413', b'1', '电脑版', '1,2,3,7', 'profile', '0'
WHERE NOT EXISTS (SELECT 1 FROM `jsh_function` WHERE `number` = '090103' AND ifnull(`delete_flag`, '0') != '1');

INSERT INTO `jsh_function` (`id`, `number`, `name`, `parent_number`, `url`, `component`, `state`, `sort`, `enabled`, `type`, `push_btn`, `icon`, `delete_flag`)
SELECT 274, '090104', '成品入库', '0901', '/production/finished_in', '/production/FinishedInList', b'0', '0414', b'1', '电脑版', '1,2,3,7', 'profile', '0'
WHERE NOT EXISTS (SELECT 1 FROM `jsh_function` WHERE `number` = '090104' AND ifnull(`delete_flag`, '0') != '1');

INSERT INTO `jsh_function` (`id`, `number`, `name`, `parent_number`, `url`, `component`, `state`, `sort`, `enabled`, `type`, `push_btn`, `icon`, `delete_flag`)
SELECT 275, '090105', '用料登记', '0901', '/production/material_record', '/production/MaterialRecordList', b'0', '0415', b'1', '电脑版', '1', 'profile', '0'
WHERE NOT EXISTS (SELECT 1 FROM `jsh_function` WHERE `number` = '090105' AND ifnull(`delete_flag`, '0') != '1');

INSERT INTO `jsh_function` (`id`, `number`, `name`, `parent_number`, `url`, `component`, `state`, `sort`, `enabled`, `type`, `push_btn`, `icon`, `delete_flag`)
SELECT 276, '090106', '工序管理', '0901', '/production/process', '/production/ProcessList', b'0', '0416', b'1', '电脑版', '1', 'profile', '0'
WHERE NOT EXISTS (SELECT 1 FROM `jsh_function` WHERE `number` = '090106' AND ifnull(`delete_flag`, '0') != '1');

INSERT INTO `jsh_function` (`id`, `number`, `name`, `parent_number`, `url`, `component`, `state`, `sort`, `enabled`, `type`, `push_btn`, `icon`, `delete_flag`)
SELECT 277, '090107', '工序汇报', '0901', '/production/process_report', '/production/ProcessReportList', b'0', '0417', b'1', '电脑版', '1', 'profile', '0'
WHERE NOT EXISTS (SELECT 1 FROM `jsh_function` WHERE `number` = '090107' AND ifnull(`delete_flag`, '0') != '1');

INSERT INTO `jsh_function` (`id`, `number`, `name`, `parent_number`, `url`, `component`, `state`, `sort`, `enabled`, `type`, `push_btn`, `icon`, `delete_flag`)
SELECT 278, '090108', '生产质检', '0901', '/production/quality_inspection', '/production/QualityInspectionList', b'0', '0418', b'1', '电脑版', '1', 'profile', '0'
WHERE NOT EXISTS (SELECT 1 FROM `jsh_function` WHERE `number` = '090108' AND ifnull(`delete_flag`, '0') != '1');

UPDATE `jsh_user_business`
SET `value` = concat(
  `value`,
  if(`value` like '%[270]%', '', '[270]'),
  if(`value` like '%[271]%', '', '[271]'),
  if(`value` like '%[272]%', '', '[272]'),
  if(`value` like '%[273]%', '', '[273]'),
  if(`value` like '%[274]%', '', '[274]'),
  if(`value` like '%[275]%', '', '[275]'),
  if(`value` like '%[276]%', '', '[276]'),
  if(`value` like '%[277]%', '', '[277]'),
  if(`value` like '%[278]%', '', '[278]')
)
WHERE `type` = 'RoleFunctions'
  AND `value` IS NOT NULL
  AND `value` <> '';
