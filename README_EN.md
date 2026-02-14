# Demo System (Spring Boot)

A front-end/back-end separated sample system focused on authentication/authorization, system management, scheduled jobs, notifications, and data scope controls. Suitable as a starting point for admin platforms.

## Overview

- Provides common admin-platform capabilities with strong configurability and operational readiness.
- Backend uses Spring Boot + MyBatis-Plus; frontend uses Vue 3 + Element Plus.
- Built on clear permission models, data scope enforcement, and audit logging.

## Highlights

- Auth & Authorization: JWT, captcha login, permission interception (roles/permissions/menus).
- Login security: failure limits (sliding window) and lock strategy.
- Password security: transport AES-GCM/SM2, storage bcrypt/md5/sm3 (configurable).
- Data scope control: ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE.
- Security protections: SQL guard, XSS filter, rate limiting, duplicate-submit protection.
- Excel import/export: user batch import/export example.
- Scheduled jobs: Quartz persistent scheduling, handler management, execution logs.
- Notifications: publish/read states, SSE pushes unread counts and latest list.
- Dictionary module: dict types/data, front/back caching, automatic label translation in responses.
- Caching: Redis / in-memory / database options.

## Tech Stack

### Backend

- Spring Boot 2.7.x (current 2.7.12)
- MyBatis-Plus
- Quartz (JDBC persistence)
- Redis + Caffeine (multi-level caching)
- Druid + Dynamic Datasource
- Jasypt (config encryption)
- Logback
- PostgreSQL / MySQL (see `sql/`)

### Frontend (`demo-ui`)

- Vue 3 + Vite + TypeScript
- Pinia + Axios
- Element Plus
- vue-i18n
- lucide-vue-next

## Quick Start

1. Prepare JDK 8+, Redis, and a database (PostgreSQL or MySQL)
2. Initialize DB: run scripts under `sql/`
3. Configure `src/main/resources/application-dev.yml`
4. Run backend:

```bash
./mvnw spring-boot:run
```

Default profile is `dev` and can be overridden:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Run frontend:

```bash
cd demo-ui
npm install
npm run dev
```

## Layout

- `src/main/java` backend source
- `src/main/resources` configs and i18n
- `sql/` database scripts
- `demo-ui/` frontend

## Usage and Configuration

### Authentication & Login

- Token header: `Authorization: Bearer <token>` or `X-Auth-Token`.
- JWT config: `auth.jwt.secret`, `auth.jwt.ttl-seconds`.
- Password policy: `auth.password.mode`, `auth.password.transport-mode`, `auth.password.transport-key`.
- Login failure limits: `auth.login-limit.enabled`, `auth.login-limit.max-errors`, `auth.login-limit.window-seconds`, `auth.login-limit.lock-seconds`, `auth.login-limit.key-mode`.

### Captcha

- Backed by Redis.
- Configs: `auth.captcha.*`.

### Permissions & Menus

- Permission checks: `security.permission.*`.
- Menu permissions are derived from `sys_menu.permission`.

### Data Scope

- Toggle/default scope: `security.data-scope.enabled`, `security.data-scope.default-type`.
- Mapping table: `sys_data_scope_rule`.

### Dictionary (Dict)

- Tables: `sys_dict_type` and `sys_dict_data`, linked by `dict_type`.
- Public APIs (login required):
  - `GET /api/dict/data/{dictType}`
  - `GET /api/dict/data/batch?types=a,b`
  - `GET /api/dict/data/all`
- Admin APIs (admin only, `dict:*`):
  - `GET /api/sys/dict/type/list`
  - `POST /api/sys/dict/type`
  - `PUT /api/sys/dict/type/{id}`
  - `DELETE /api/sys/dict/type/{id}`
  - `GET /api/sys/dict/data/list`
  - `POST /api/sys/dict/data`
  - `PUT /api/sys/dict/data/{id}`
  - `DELETE /api/sys/dict/data/{id}`
  - `DELETE /api/sys/dict/cache/refresh`
- Cache: `dict.cache.seconds` (<=0 disables caching).
- Backend translation: annotate VO fields with `@DictLabel("sys_gender")` to add `xxxLabel`.

### Notifications (SSE)

- Config: `notice.sse.*`.

### Scheduled Jobs (Quartz)

- Persistent scheduling: `spring.quartz.*`.
- Handlers implement `JobHandler` and are Spring beans.
- Logs in `sys_job_log` with detail in `log_detail`.

### Job Log Auto Collection

- Switch/scope: `job.log.collect.enabled`, `job.log.collect.scope`.
- Level/size: `job.log.collect.min-level`, `job.log.collect.max-length`.
- Merge: `job.log.collect.merge-delay-millis`, `job.log.collect.max-hold-millis`.
- Thread context: `job.log.collect.inherit-thread-context`.

### Security Protections

- SQL guard: `security.sql-guard.*`.
- XSS filter: `security.xss.*`.
- Rate limiting: `security.rate-limit.*`.
- Duplicate-submit: `security.duplicate-submit.*`.
- Exclude paths are merged with `security.common.exclude-paths`.

### Filters and Interceptors Overview

- `TraceIdFilter`: injects `traceId` into MDC. General-purpose.
- `AuthTokenFilter`: validates token and sets `AuthContext`. Allowlist-based.
- `PermissionInterceptor`: checks `@RequireLogin` / `@RequirePermission`. Allowlist-based.
- `RateLimitFilter`: rate limiting. Allowlist-based.
- `DuplicateSubmitFilter`: duplicate-submit protection with `Idempotency-Key`. Allowlist-based.
- `XssFilter`: request param XSS sanitization. Allowlist-based.
- `XssRequestBodyAdvice`: request-body XSS sanitization. Allowlist-based.
- `DataScopeAspect`: reads `@DataScope` and writes to thread context. General-purpose.
- `DataScopeInnerInterceptor`: SQL rewrite for data scope. General-purpose.
- `PaginationInnerInterceptor`: pagination. General-purpose.
- `OptimisticLockerInnerInterceptor`: optimistic lock. General-purpose.
- `SqlGuardInnerInterceptor`: blocks multi-statements and full-table updates/deletes. Blacklist-style.

Impact on development:

- Public endpoints must be added to `auth.filter.exclude-paths` and `security.permission.exclude-paths`.
- High-frequency APIs may hit 429; tune rate limits or exclude paths.
- Write endpoints can be blocked for duplicate submits; use `Idempotency-Key` if needed.
- Rich-text input should be excluded from XSS or handled in business logic.
- Data scope requires `@DataScope` and correct `deptAlias` / `userAlias`.
- Full-table UPDATE/DELETE will be blocked by SQL guard.

### Encrypted Configs (Jasypt)

- Wrap secrets with `ENC(...)`.
- Provide the password via `JASYPT_ENCRYPTOR_PASSWORD`.

### Runtime & Environment

- Main configs: `src/main/resources/application.yml` + `application-dev.yml`.
- Default profile: `dev` (override with `SPRING_PROFILES_ACTIVE`).
- DB scripts: `sql/mysql.sql`, `sql/postgresql.sql`.
- Druid monitor: `spring.datasource.druid.stat-view-servlet.*`.

### Licenses & Compliance

- Backend report: `target/site/aggregate-third-party-report.html`.
- Frontend scan: `target/licenses/frontend-licenses.json`.
- Summary: `target/licenses/frontend-licenses-summary.txt`.

Regenerate:

```bash
./mvnw -q -DskipTests org.codehaus.mojo:license-maven-plugin:2.4.0:aggregate-third-party-report
python3 scripts/licenses_scan_frontend.py
```

### Troubleshooting

- Too many login failures: check `auth.login-limit.*` and Redis.
- Invalid captcha: verify Redis and expiration.
- Invalid/expired token: check `auth.jwt.ttl-seconds` and system time.

## Common Commands

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw -DskipTests package
```
