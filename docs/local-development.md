# 本地开发

## 后端

```bash
cd /Users/mac/jshERP/yize-erp/apps/backend
mvn spring-boot:run
```

默认地址：

- API: `http://127.0.0.1:8088/api`
- 健康检查: `http://127.0.0.1:8088/api/system/health`

默认数据库：

- MySQL: `127.0.0.1:3306`
- Database: `yize_erp`
- Username: `root`
- Password: `root`

## 管理端

```bash
cd /Users/mac/jshERP/yize-erp/apps/admin-web
npm install
npm run dev
```

默认地址：

- `http://127.0.0.1:5178`

## 微信小程序

使用微信开发者工具导入：

```text
/Users/mac/jshERP/yize-erp/apps/miniprogram
```

本地调试时关闭合法域名校验，默认 API 地址在 `app.js` 中配置。

## 下一步

1. 初始化数据库迁移工具。
2. 实现登录和用户权限。
3. 实现基础资料。
4. 实现生产任务主单据。
5. 实现生产领料、用料登记、工序汇报、质检、成品入库闭环。
