# jshERP Project Context

This file is the handoff note for future Codex sessions. Keep it short and update it whenever the direction changes.

## Current Goal

Continue building the customized jshERP work around:

- approval workflow support
- WeChat mini program client
- lightweight production management: BOM, production task, production material issue, finished goods stock-in, and production-to-inventory document linkage
- single-company/local runtime setup

If the chat history is unavailable, read this file first, then inspect `git status` and recent commits.

## Repository Layout

- `jshERP-boot/`: Spring Boot backend.
- `jshERP-web/`: Vue 2 admin web frontend.
- `jshERP-miniprogram/`: WeChat mini program MVP.
- `/Users/mac/jshERP-runtime/`: local runtime data/logs, including MySQL runtime files.

## Current Git State

As of 2026-04-14, local `master` has unpushed work. Run this to get the current list:

```bash
git log --oneline origin/master..HEAD
```

Known unpushed work includes:

- `a82c26bd docs: record production module backlog`
- `ac229f72 chore(production): standardize task wording`
- `8abe6292 feat(web): add production management pages`
- `ad9c0205 feat: add lightweight production module skeleton`
- `c634623f fix: stabilize local backend startup`
- `f5322b49 fix: restore basic data queries and actions`
- `7fec8f51 docs: avoid stale commit count in context`
- `82f16787 docs: refresh project context commits`
- `5c756005 docs: add local development scripts`
- `d1657026 fix(web): remove unused sass chart stylesheet`
- `8f76d598 docs: add project context handoff`
- `8925a2e2 update`
- `ae07792c feat(platform): merge mini program settings into platform config`
- `3f12e196 chore(env): update local runtime configuration`
- `07abf4f5 refactor(account): switch to single-company model and add db migration scripts`
- `9aea73be feat(miniprogram): add mini program client and desktop config page`
- `0886089c feat(approval): add approval workflow service and admin pages`

## Verification

Last verified on 2026-04-18:

- Frontend: `cd /Users/mac/jshERP/jshERP-web && npm run build` passed.
- API smoke test passed for creating a temporary production task, saving linked `生产领料` and `成品入库` stock documents through `/depotHead/addDepotHeadAndDetail`, querying both by production task `linkNumber`, and deleting all temporary test data.
- Backend: `cd /Users/mac/jshERP/jshERP-boot && mvn test` passed.
- API smoke test passed for automatic production task finished-quantity/status sync from linked `成品入库`: add stock-in -> `finishedQuantity=1/status=已完工`; delete stock-in -> `finishedQuantity=0/status=已下达`; temporary test data was deleted.
- Web: `cd /Users/mac/jshERP/jshERP-web && npm run build` passed.
- API smoke test passed for first-pass `生产质检`: create a temporary production task, save/query/delete a quality inspection with good/defect/scrap quantities and defect item, then delete the temporary task.

Known non-blocking frontend warnings:

- asset size limit warnings

Latest basic-data fix on 2026-04-13:

- Supplier/customer list query errors were traced to `UserService.getRoleTypeByUserId`, where `userBusinessService` was not injected and empty role bindings were not handled safely.
- Mini program basic data now exposes delete actions for person, in/out item, unit, and material category.
- Mini program partner/customer/supplier list now exposes edit and delete actions.
- Rebuilt backend jar with `mvn package -DskipTests` after fixing local startup to prefer OpenJDK 8 and adding `allowPublicKeyRetrieval=true` to the MySQL URL.
- Re-verified: backend `mvn test`, mini program JS `node --check`, web `npm run build`, authenticated supplier/customer/person/in-out-item/unit queries, and material category tree with `id=0` passed.

Latest production module work on 2026-04-13:

- Added backend production BOM and production order skeleton: entities, MyBatis annotation mappers, `ProductionService`, and `ProductionController`.
- Added DB migration at `jshERP-boot/docs/production_module.sql` for `jsh_production_bom`, `jsh_production_bom_item`, `jsh_production_order`, and `jsh_production_order_item`.
- Added desktop Web production pages at `jshERP-web/src/views/production/BomList.vue` and `jshERP-web/src/views/production/OrderList.vue`; mini program production work is paused and no production entry is registered there.
- Added desktop menu entries for `生产管理`, `BOM管理`, and `生产任务` in `production_module.sql`, including existing-role permission updates.
- Verified: backend `mvn test`, backend `mvn package -DskipTests`, Web `npm run build`, mini program JS `node --check`, local migration import, authenticated menu query, authenticated production BOM list/save/delete, production order list/save/delete, and material calculation passed.
- Note: production insert SQL lets the tenant interceptor inject `tenant_id`; do not add `tenant_id` manually to those insert statements or MyBatis will generate duplicate columns.

Latest production module follow-up on 2026-04-14:

- Standardized user-facing production order wording to `生产任务` in the desktop task page, Swagger annotations, service logs, and `production_module.sql` comments/menu seed.
- Added an idempotent `jsh_function` menu name update for existing local databases that already imported the old `生产单` label.
- Verified: backend `mvn test` and Web `npm run build` passed; frontend still has the known non-blocking asset size warnings.
- Added desktop production entries for `生产领料` and `成品入库` by reusing the existing other-out/other-in bill engine with dedicated `subType` filters and menu records in `production_module.sql`.
- Verified: Web `npm run build` passed; frontend still has the known non-blocking asset size warnings.
- Added a first-pass `用料登记` backend table/API and desktop page; records adjust production task item `已领料` totals on create/edit/delete.
- Verified: backend `mvn test` and Web `npm run build` passed; frontend still has the known non-blocking asset size warnings.
- Added first-pass `工序管理` backend table/API and desktop page with process number, name, wage type, unit price, enabled flag, sort, and remark.
- Verified: backend `mvn test` and Web `npm run build` passed; frontend still has the known non-blocking asset size warnings.

Latest production module follow-up on 2026-04-18:

- Added first-pass `工序汇报` backend table/API and desktop page with production task, process, worker, good quantity, defect quantity, scrap quantity, report time, and remark.
- Added desktop menu seed for `工序汇报` in `production_module.sql`, including existing-role permission updates.
- Verified: backend `mvn test` and Web `npm run build` passed; frontend still has the known non-blocking asset size warnings.
- Linked production BOM and production task material fields to the existing system product/material selector, including finished goods and raw-material detail rows. `/material/findBySelect` now returns both material master id and material extend id for production association.
- Verified: backend `mvn test`, backend `mvn package -DskipTests`, and Web `npm run build` passed; frontend still has the known non-blocking asset size warnings.
- Linked `生产领料` and `成品入库` bill modals to production tasks by using the existing stock document `linkNumber` field. Production pages now expose the linked-task column by default and preserve normal other-in/other-out linkage behavior outside production mode.
- Verified: `git diff --check`, Web `npm run build`, and authenticated API smoke test for linked production issue/finished-in documents passed. Temporary test records were deleted.
- Added backend synchronization from linked `成品入库` documents to production task `finishedQuantity` and status. Stock-in add/update/delete now recalculates the task; `已关闭` is preserved, completed quantity reaches plan -> `已完工`, partial quantity -> `生产中`, and deletion back to zero from an auto status -> `已下达`.
- Verified: backend `mvn test`, backend `mvn package -DskipTests`, local backend restart, and authenticated API smoke test passed.
- Added first-pass `生产质检` backend table/API and desktop page with production task, inspector, good quantity, defect quantity, scrap quantity, defect item, inspection time, and remark.
- Added desktop menu seed for `生产质检` in `production_module.sql`, including existing-role permission updates.
- Verified: backend `mvn test`, backend `mvn package -DskipTests`, Web `npm run build`, local backend restart, and authenticated API smoke test passed.

Production module target list from the user:

- 自制业务：生产任务、生产领料、用料登记、工序汇报、生产质检、成品入库
- 委外业务：委外任务、物料出库、提请付款、成品质检、成品入库、委外付款情况
- 其他：工序管理、BOM管理、BOM模板、不良品项、计时工资、计件工资、工序汇报记录
- 生产报表：生产看板、生产成本统计、订单进度跟踪表、委外进度跟踪表、生产进度跟踪表、物料追踪表、缺料

Reference system notes from `https://sv.loafish.com/lfdp` manufacturing trial:

- Manufacturing trial login is exposed by the site as `13512733500 / 111111`; version selector value `4` is `制造业版`.
- Production menu paths include: `生产任务` `/inv/invScrw/list`, `生产领料` `/inv/scrw/jg/scllList`, `用料登记` `/inv/scrw/jg/yldjList`, `工序汇报` `/inv/scrw/jg/gxhbList`, `生产质检` `/inv/scrw/jg/sczjList`, `成品入库` `/inv/scrw/jg/scrkList`.
- Outsourcing menu paths include: `委外任务` `/inv/invScWwjg/list`, `成品质检` `/inv/wwrw/jg/cpzjList`, `成品入库` `/inv/wwrw/jg/wwrkList`, `委外付款情况` `/inv/invScWwjg/wwfkList`.
- Other/reference menus include: `工序管理`, `BOM管理`, `BOM模板`, `计时工资`, `计件工资`, and `工序汇报记录`.
- Report menus include: `生产看板`, `生产成本统计`, `生产进度跟踪表`, `委外进度跟踪表`, `物料追踪表`, `缺料追踪表`, plus `计时工资统计表` and `计件工资统计表`.
- `用料登记` has single-task and multi-task modes; its page says it calculates current material usage from the BOM and uses the formula `用料数=已领料数-已退数-废料数`.
- `工序汇报` targets tasks in `加工中`, captures process name, operator(s), good quantity, scrap quantity, remarks, and has finish/rework/detail actions plus piecework status.
- `生产质检` captures inspection date, inspector, good quantity, defect quantity, scrap quantity, defect item, quantity, and remarks.

The previous `Failed to resolve loader: sass-loader` warning was caused by a duplicate unused Sass file next to the active Less file:

- active: `jshERP-web/src/components/chart/chart.less`
- removed: `jshERP-web/src/components/chart/chart.scss`

## Local Run Notes

Use the project-local scripts first:

- start: `cd /Users/mac/jshERP && scripts/start-local.sh`
- stop: `cd /Users/mac/jshERP && scripts/stop-local.sh`
- details: `LOCAL_DEV.md`

The expected local URLs are:

- backend API: `http://localhost:9999/jshERP-boot`
- backend docs: `http://localhost:9999/jshERP-boot/doc.html`
- frontend dev server: `http://localhost:3000/`
- test user from backend log: `jsh`
- test password from backend log: `123456`

There was an older backend log error for `/jshERP-boot/user/info` missing request parameter `id`; re-check only if login/user-info behavior is the next task.

## Next Session Checklist

1. Read this file.
2. Run `git status --short --branch`.
3. If continuing implementation, inspect the latest local commit with `git show --stat HEAD`.
4. Ask the user which track to continue if it is not obvious:
   - mini program testing and API wiring
   - production module desktop UI expansion and inventory stock-in/stock-out integration
   - approval workflow behavior
   - pushing local commits to remote
   - local runtime/startup cleanup

## Update Rule

When important work is completed, update:

- `Current Goal`
- `Verification`
- `Next Session Checklist`

Then commit the context update with the related code change.
