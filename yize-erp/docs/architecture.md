# 技术架构

## 推荐技术栈

### 后端

- Java 21 或 Java 17
- Spring Boot 3
- MyBatis Plus
- MySQL 8
- Redis
- Sa-Token 或 Spring Security
- Knife4j/OpenAPI

### 管理后台

- Vue 3
- TypeScript
- Vite
- Ant Design Vue
- Pinia
- Axios

### 微信小程序

- 原生微信小程序
- TypeScript 可选
- 统一 API 请求封装

## 架构原则

- 单公司模式，不做租户隔离。
- 模块边界清晰，生产、库存、财务之间通过业务单据联动。
- 单据都有状态机，不靠散乱字段判断。
- 所有影响库存和财务的数据必须有来源单据。
- 工资数据要能追溯到考勤、工序汇报或手工调整。
- 报表数据优先从业务明细实时汇总，必要时再做汇总表。

## 建议服务结构

```text
backend/
  src/main/java/com/yize/erp/
    common/
    system/
    masterdata/
    purchase/
    sales/
    inventory/
    manufacturing/
    outsourcing/
    finance/
    hr/
    payroll/
    report/
    mobile/
```

## 数据库命名

- 表名：`yz_模块_名称`
- 主键：`id`
- 业务编号：`xxx_no`
- 删除标记：`deleted`
- 创建时间：`created_at`
- 更新时间：`updated_at`

示例：

- `yz_mfg_task`
- `yz_mfg_bom`
- `yz_mfg_material_issue`
- `yz_mfg_process_report`
- `yz_hr_employee`
- `yz_payroll_salary_sheet`
