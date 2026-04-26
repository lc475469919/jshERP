# 本地开发

## 后端

```bash
cd /Users/mac/yize-erp/apps/backend
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

尚未创建管理端工程。创建后使用：

```bash
cd /Users/mac/yize-erp/apps/admin-web
npm install
npm run dev
```

默认地址：

- `http://127.0.0.1:5178`

## 微信小程序

尚未创建小程序工程。创建后使用微信开发者工具导入：

```text
/Users/mac/yize-erp/apps/miniprogram
```

本地调试时关闭合法域名校验，默认 API 地址在 `app.js` 中配置。

## 下一步

1. 创建管理端最小工程骨架。
2. 创建微信小程序最小工程骨架。
3. 实现登录和用户权限。
4. 实现基础资料。
5. 基础资料完成后再实现业务单据。
