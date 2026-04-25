-- 员工、考勤与工资模块草案
-- 说明：本文件先作为单公司模式的设计草案，实际导入前需要结合现有数据库做字段和索引复核。

CREATE TABLE IF NOT EXISTS jsh_employee (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_no VARCHAR(64) NOT NULL COMMENT '员工编号',
  name VARCHAR(100) NOT NULL COMMENT '姓名',
  phone VARCHAR(50) DEFAULT NULL COMMENT '手机号',
  department_id BIGINT DEFAULT NULL COMMENT '部门ID',
  department_name VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
  position_name VARCHAR(100) DEFAULT NULL COMMENT '岗位',
  user_id BIGINT DEFAULT NULL COMMENT '关联系统用户ID',
  entry_date DATE DEFAULT NULL COMMENT '入职日期',
  leave_date DATE DEFAULT NULL COMMENT '离职日期',
  status VARCHAR(20) NOT NULL DEFAULT '在职' COMMENT '状态：在职、离职、停用',
  wage_type VARCHAR(20) NOT NULL DEFAULT '月薪' COMMENT '工资类型：月薪、计时、计件、混合',
  base_salary DECIMAL(24,6) DEFAULT 0 COMMENT '基础工资',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delete_flag VARCHAR(1) NOT NULL DEFAULT '0',
  UNIQUE KEY uk_employee_no (employee_no),
  KEY idx_employee_user (user_id),
  KEY idx_employee_status (status)
) COMMENT='员工档案';

CREATE TABLE IF NOT EXISTS jsh_attendance_shift (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shift_no VARCHAR(64) NOT NULL COMMENT '班次编号',
  name VARCHAR(100) NOT NULL COMMENT '班次名称',
  start_time TIME NOT NULL COMMENT '上班时间',
  end_time TIME NOT NULL COMMENT '下班时间',
  late_minutes INT NOT NULL DEFAULT 0 COMMENT '允许迟到分钟',
  early_leave_minutes INT NOT NULL DEFAULT 0 COMMENT '允许早退分钟',
  enabled BIT NOT NULL DEFAULT b'1',
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delete_flag VARCHAR(1) NOT NULL DEFAULT '0',
  UNIQUE KEY uk_shift_no (shift_no)
) COMMENT='考勤班次';

CREATE TABLE IF NOT EXISTS jsh_attendance_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_id BIGINT NOT NULL COMMENT '员工ID',
  employee_name VARCHAR(100) NOT NULL COMMENT '员工姓名',
  attendance_date DATE NOT NULL COMMENT '考勤日期',
  shift_id BIGINT DEFAULT NULL COMMENT '班次ID',
  clock_in_time DATETIME DEFAULT NULL COMMENT '上班打卡时间',
  clock_out_time DATETIME DEFAULT NULL COMMENT '下班打卡时间',
  clock_type VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '打卡类型：正常、外勤、补卡',
  location VARCHAR(255) DEFAULT NULL COMMENT '打卡地点',
  status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '状态：正常、迟到、早退、缺勤、请假、加班',
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delete_flag VARCHAR(1) NOT NULL DEFAULT '0',
  UNIQUE KEY uk_attendance_employee_date (employee_id, attendance_date),
  KEY idx_attendance_date (attendance_date),
  KEY idx_attendance_status (status)
) COMMENT='考勤打卡记录';

CREATE TABLE IF NOT EXISTS jsh_attendance_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  apply_no VARCHAR(64) NOT NULL COMMENT '申请编号',
  employee_id BIGINT NOT NULL COMMENT '员工ID',
  employee_name VARCHAR(100) NOT NULL COMMENT '员工姓名',
  apply_type VARCHAR(20) NOT NULL COMMENT '类型：请假、加班、调休、补卡',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  hours DECIMAL(24,6) DEFAULT 0 COMMENT '时长',
  reason VARCHAR(500) DEFAULT NULL COMMENT '原因',
  status VARCHAR(20) NOT NULL DEFAULT '待审批' COMMENT '状态：待审批、已通过、已驳回、已取消',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delete_flag VARCHAR(1) NOT NULL DEFAULT '0',
  UNIQUE KEY uk_attendance_apply_no (apply_no),
  KEY idx_attendance_apply_employee (employee_id),
  KEY idx_attendance_apply_status (status)
) COMMENT='请假加班申请';

CREATE TABLE IF NOT EXISTS jsh_piecework_wage (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  wage_no VARCHAR(64) NOT NULL COMMENT '计件单号',
  employee_id BIGINT NOT NULL COMMENT '员工ID',
  employee_name VARCHAR(100) NOT NULL COMMENT '员工姓名',
  production_task_id BIGINT DEFAULT NULL COMMENT '生产任务ID',
  production_task_no VARCHAR(64) DEFAULT NULL COMMENT '生产任务编号',
  process_id BIGINT DEFAULT NULL COMMENT '工序ID',
  process_name VARCHAR(100) DEFAULT NULL COMMENT '工序名称',
  report_id BIGINT DEFAULT NULL COMMENT '工序汇报ID',
  work_date DATE NOT NULL COMMENT '作业日期',
  good_quantity DECIMAL(24,6) DEFAULT 0 COMMENT '良品数',
  defect_quantity DECIMAL(24,6) DEFAULT 0 COMMENT '不良数',
  scrap_quantity DECIMAL(24,6) DEFAULT 0 COMMENT '报废数',
  unit_price DECIMAL(24,6) DEFAULT 0 COMMENT '计件单价',
  amount DECIMAL(24,6) DEFAULT 0 COMMENT '计件金额',
  status VARCHAR(20) NOT NULL DEFAULT '待结算' COMMENT '状态：待结算、已结算、已作废',
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delete_flag VARCHAR(1) NOT NULL DEFAULT '0',
  UNIQUE KEY uk_piecework_wage_no (wage_no),
  KEY idx_piecework_employee_date (employee_id, work_date),
  KEY idx_piecework_task (production_task_id),
  KEY idx_piecework_status (status)
) COMMENT='计件工资明细';

CREATE TABLE IF NOT EXISTS jsh_salary_sheet (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sheet_no VARCHAR(64) NOT NULL COMMENT '工资单号',
  salary_month VARCHAR(7) NOT NULL COMMENT '工资月份，格式YYYY-MM',
  employee_id BIGINT NOT NULL COMMENT '员工ID',
  employee_name VARCHAR(100) NOT NULL COMMENT '员工姓名',
  base_salary DECIMAL(24,6) DEFAULT 0 COMMENT '基本工资',
  attendance_salary DECIMAL(24,6) DEFAULT 0 COMMENT '计时/考勤工资',
  piecework_salary DECIMAL(24,6) DEFAULT 0 COMMENT '计件工资',
  overtime_salary DECIMAL(24,6) DEFAULT 0 COMMENT '加班工资',
  allowance_amount DECIMAL(24,6) DEFAULT 0 COMMENT '补贴',
  deduction_amount DECIMAL(24,6) DEFAULT 0 COMMENT '扣款',
  payable_amount DECIMAL(24,6) DEFAULT 0 COMMENT '应发工资',
  paid_amount DECIMAL(24,6) DEFAULT 0 COMMENT '实发工资',
  status VARCHAR(20) NOT NULL DEFAULT '草稿' COMMENT '状态：草稿、已确认、已发放、已作废',
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delete_flag VARCHAR(1) NOT NULL DEFAULT '0',
  UNIQUE KEY uk_salary_sheet_no (sheet_no),
  UNIQUE KEY uk_salary_month_employee (salary_month, employee_id),
  KEY idx_salary_status (status)
) COMMENT='员工工资单';

CREATE TABLE IF NOT EXISTS jsh_salary_payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  payment_no VARCHAR(64) NOT NULL COMMENT '发放单号',
  salary_sheet_id BIGINT NOT NULL COMMENT '工资单ID',
  employee_id BIGINT NOT NULL COMMENT '员工ID',
  employee_name VARCHAR(100) NOT NULL COMMENT '员工姓名',
  account_id BIGINT DEFAULT NULL COMMENT '付款账户ID',
  payment_time DATETIME NOT NULL COMMENT '发放时间',
  amount DECIMAL(24,6) NOT NULL DEFAULT 0 COMMENT '发放金额',
  status VARCHAR(20) NOT NULL DEFAULT '已发放' COMMENT '状态：已发放、已撤销',
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delete_flag VARCHAR(1) NOT NULL DEFAULT '0',
  UNIQUE KEY uk_salary_payment_no (payment_no),
  KEY idx_salary_payment_sheet (salary_sheet_id),
  KEY idx_salary_payment_employee (employee_id)
) COMMENT='工资发放记录';
