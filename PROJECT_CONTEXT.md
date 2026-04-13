# jshERP Project Context

This file is the handoff note for future Codex sessions. Keep it short and update it whenever the direction changes.

## Current Goal

Continue building the customized jshERP work around:

- approval workflow support
- WeChat mini program client
- single-company/local runtime setup

If the chat history is unavailable, read this file first, then inspect `git status` and recent commits.

## Repository Layout

- `jshERP-boot/`: Spring Boot backend.
- `jshERP-web/`: Vue 2 admin web frontend.
- `jshERP-miniprogram/`: WeChat mini program MVP.
- `/Users/mac/jshERP-runtime/`: local runtime data/logs, including MySQL runtime files.

## Current Git State

As of 2026-04-13, local `master` has unpushed work. Run this to get the current list:

```bash
git log --oneline origin/master..HEAD
```

Known unpushed work includes:

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

Last verified on 2026-04-13:

- Backend: `cd /Users/mac/jshERP/jshERP-boot && mvn test` passed.
- Frontend: `cd /Users/mac/jshERP/jshERP-web && npm run build` passed after removing the duplicate unused `jshERP-web/src/components/chart/chart.scss`.

Known non-blocking frontend warnings:

- asset size limit warnings

Latest basic-data fix on 2026-04-13:

- Supplier/customer list query errors were traced to `UserService.getRoleTypeByUserId`, where `userBusinessService` was not injected and empty role bindings were not handled safely.
- Mini program basic data now exposes delete actions for person, in/out item, unit, and material category.
- Mini program partner/customer/supplier list now exposes edit and delete actions.
- Re-verified: backend `mvn test`, mini program JS `node --check`, and web `npm run build` passed.

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
   - approval workflow behavior
   - pushing local commits to remote
   - local runtime/startup cleanup

## Update Rule

When important work is completed, update:

- `Current Goal`
- `Verification`
- `Next Session Checklist`

Then commit the context update with the related code change.
