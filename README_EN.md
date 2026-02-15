# Demo System (Spring Boot)

A front-end/back-end separated sample system focused on authentication/authorization, system management, scheduled jobs, notifications, and data scope controls. Suitable as a starting point for admin platforms.

## Overview

- Provides common admin-platform capabilities with strong configurability and operational readiness.
- Backend uses Spring Boot + MyBatis-Plus; frontend uses Vue 3 + Element Plus.
- Built on clear permission models, data scope enforcement, and audit logging.

## Highlights

- Auth & Authorization: JWT, captcha login, permission interception (roles/permissions/menus).
- Login security: failure limits (sliding window) and lock strategy.
- Session security: server-side token revocation on logout (no need to wait for JWT natural expiration).
- Mail-backed security: login anomaly alerts (IP/device change) and sensitive-operation email confirmation (code +
  short-lived ticket).
- Password security: transport AES-GCM/SM2, storage bcrypt/sm3 (configurable), with first-login forced password
  change and password expiration policy.
- Unified API contract: backend returns `CommonResult` consistently; frontend handles `code/message/data` in one place.
- Constant governance: magic values are centralized in module-level `*Constants` with defaults and config overrides.
- Data scope control: ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE.
- Security protections: SQL guard, XSS filter, rate limiting, duplicate-submit protection.
- Excel import/export: user batch import/export example.
- Scheduled jobs: Quartz persistent scheduling, handler management, execution logs.
- Notifications: publish/read states, SSE pushes unread counts and latest list.
- Dictionary module: dict types/data, front/back caching, automatic label translation in responses.
- Caching: Redis / in-memory / database options.
- Frontend routing & permissions: Vue Router nested routes (History mode), menu-driven navigation, and permission-aware
  access.
- Internationalization: backend i18n messages + frontend `vue-i18n` for bilingual extensibility.

## Tech Stack

### Backend

- Spring Boot 2.7.x (current 2.7.12)
- MyBatis-Plus
- Spring Validation
- Quartz (JDBC persistence)
- Spring Mail (SMTP)
- Redis + Caffeine (multi-level caching)
- Druid + Dynamic Datasource
- Spring Security Crypto + BouncyCastle (password and cryptography support)
- Jasypt (config encryption)
- Logback
- PostgreSQL / MySQL (see `sql/`)

### Frontend (`demo-ui`)

- Vue 3 + Vite + TypeScript
- Vue Router (History mode)
- Pinia + Axios
- Element Plus
- vue-i18n
- sm-crypto (SM2/SM3 in browser)
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

## Module Capability Map

| Module       | Responsibility           | Key Capabilities                                                                                          |
|--------------|--------------------------|-----------------------------------------------------------------------------------------------------------|
| `auth`       | Authentication & session | Captcha login, JWT issue/verify, logout revocation, login security policy, email confirmation             |
| `user`       | User management          | User profile, status lifecycle, password updates, user-level data-scope overrides                         |
| `dept`       | Organization structure   | Department tree, hierarchy management, ownership baseline                                                 |
| `menu`       | Menu management          | Menu tree, route menu payloads, menu permission identifiers                                               |
| `permission` | Roles & permissions      | Role authorization, permission-point control, API access control                                          |
| `datascope`  | Data authorization       | 3-layer scope merge (role/menu/user), SQL data filtering                                                  |
| `dict`       | Dictionary management    | Dict type/data maintenance, caching, dict label mapping                                                   |
| `notice`     | Notification center      | Notification publish/read state, SSE real-time push                                                       |
| `job`        | Job scheduling           | Quartz job management, execution logs and result tracking                                                 |
| `log`        | Audit logging            | Login logs, operation logs, audit event persistence                                                       |
| `post`       | Position management      | Position maintenance, user-position association                                                           |
| `order`      | Business sample          | Order sample module (demonstrates CRUD + permission/data-scope integration)                               |
| `common`     | Shared infrastructure    | Unified response, exception handling, cache abstraction, mail sender abstraction, common utilities/config |
| `ai`         | AI extension entry       | AI capability integration and extensibility (enabled per business needs)                                  |

### Microservice Transition Modules (Phase 1)

- `gateway`: unified entry and canary forwarding, with default fallback to `app`.
- `auth-service`: auth-domain transition service (wrapper over existing `auth` module).
- `order-service`: order-domain transition service (wrapper over existing `order` module).
- `system-api`: cross-domain contract module used by `auth/order` to reduce direct `system` coupling.
- Migration guide: `docs/MICROSERVICE_TRANSITION.md`.

## Usage and Configuration

### Authentication & Login

- Token header: `Authorization: Bearer <token>` or `X-Auth-Token`.
- JWT config: `auth.jwt.secret`, `auth.jwt.ttl-seconds`.
- Password policy: `auth.password.mode`, `auth.password.transport-mode`, `auth.password.transport-key`.
    - `auth.password.mode` supports only `bcrypt` / `sm3` (`md5` has been removed).
- Password governance:
    - Force password change on first login: `auth.password.force-change-on-first-login` (default `true`).
    - Password expiration days: `auth.password.expire-days` (default `120`; disable when `<=0`).
- Login failure limits: `auth.login-limit.enabled`, `auth.login-limit.max-errors`, `auth.login-limit.window-seconds`, `auth.login-limit.lock-seconds`, `auth.login-limit.key-mode`.
- Security enhancements:
    - Login anomaly alert: `auth.security.login-anomaly.*`
    - Sensitive-operation email confirmation: `auth.security.operation-confirm.*`
    - Send code API: `POST /auth/security/operation-confirm/send`
    - Verify code API: `POST /auth/security/operation-confirm/verify`
- Logout invalidation:
    - API: `POST /auth/logout`
    - Behavior: current token is revoked server-side and becomes invalid immediately.

### Unified Response & Constants

- Unified response: except binary download endpoints, backend APIs return `CommonResult`.
- Constant governance: module magic values are centralized in `*Constants` and bound via `@ConfigurationProperties`.
- Full configurable keys and defaults: `docs/CONFIGURATION_EN.md`.

### Performance Optimizations

- Data-scope preloading on login: `deptTreeIds`, `roleDataScopes`, and `userScopeOverrides` are preloaded into auth
  context after login, so query-time scope resolution uses in-memory profile data instead of rebuilding scope profile on
  each query.
- Batch dict translation: before response serialization, dict types required by `@DictLabel` are collected and preloaded
  in batch; serialization then resolves labels from request-local maps to avoid row-by-row repeated lookups.
- SSE heartbeat keepalive: notice stream sends periodic heartbeat events to reduce idle disconnects on browsers/proxies,
  and exposes heartbeat settings in the init event for client-side timeout/reconnect logic.

### Mail-backed Security Scenarios

- Email is sent in these cases:
    - After successful login, anomaly detection triggers alert when IP/device fingerprint changes and policy matches.
    - Calling `POST /auth/security/operation-confirm/send` sends operation confirmation code by email.
- `POST /auth/security/operation-confirm/verify` only verifies code and issues a short-lived ticket, it does not send
  email.
- Enforcing confirmation on concrete business actions requires consuming the ticket (`consumeTicket`) in those business
  APIs.

### Mail Config (Minimal)

- Business switch/text: `notify.mail.*`
- SMTP connection: `spring.mail.*`
- Default `notify.mail.enabled=false` means no actual sending (Noop sender).
- Mail health check switch: `management.health.mail.enabled`
    - Defaults to `NOTIFY_MAIL_ENABLED`.
    - When mail sending is disabled, SMTP probing is disabled by default to avoid noisy `Mail health check failed`
      warnings.

### Captcha

- Backed by Redis.
- Configs: `auth.captcha.*`.

### Permissions & Menus

- Permission checks: `security.permission.*`.
- Menu permissions are derived from `sys_menu.permission`.

### Notice SSE Keepalive

- SSE endpoint: `GET /notices/stream`.
- Init event: `init`, includes `heartbeatIntervalMillis` and `heartbeatTimeoutMillis` for client timeout handling.
- Heartbeat event: `ping`, pushed by server at `notice.sse.heartbeat-interval-millis`.
- Key configs:
  - `notice.sse.heartbeat-interval-millis` (default `60000`)
  - `notice.sse.heartbeat-timeout-millis` (default `180000`)
  - `notice.sse.latest-limit` (default `5`)

### Data Scope

**Goals**
- Model data visibility with a three-layer design: role default → role×menu → user override.
- Merge multiple roles on the same layer as a union (max scope).
- Use default columns `create_dept` / `create_by` when no mapping is configured.
- Decouple write and read: write ownership fields, read-time SQL filtering.

**Three Layers (priority)**
- Layer 3: user overrides (`sys_user_data_scope`), highest priority.
- Layer 2: role×menu scope (`sys_role_menu` / `sys_role_menu_dept`).
- Layer 1: role default scope (`sys_role`).
- Fallback: no roles → `SELF`.

**Multi-role Merge**
- Merge within the same layer as a union.
- Any role with `ALL` grants full access.
- Others merge into “visible department set + include self”.

**Scope Types**
- `ALL`: all data
- `DEPT`: current department
- `DEPT_AND_CHILD`: department and children
- `CUSTOM_DEPT` / `CUSTOM`: custom departments
- `SELF`: only self
- `NONE`: no data

**Data Model**
- Role default: `sys_role.data_scope_type` / `data_scope_value`.
- Role×menu: `sys_role_menu.data_scope_type` overrides role default.
- Role×menu×dept: `sys_role_menu_dept` for custom departments.
- User override: `sys_user_data_scope` (`scope_key` can be `*` for global).
- Mapping: `sys_data_scope_rule` (`scope_key` → table/column/alias).
- Defaults: `dept_column = create_dept`, `user_column = create_by`.

**Preload on Login**
- `deptTreeIds`: current dept and descendants.
- `roleDataScopes`: role default + menu overrides.
- `userScopeOverrides`: user overrides (scope_key → config).
- Performance note: this profile is assembled once at login and stored in auth context; query-time filtering no longer
  rebuilds role/menu/override profile from DB, and only performs in-memory scope merge plus SQL predicate injection.

**Read-time Filtering (SELECT)**
- Annotate with `@DataScope` to set `scopeKey` in thread context.
- Resolve mapping via `sys_data_scope_rule`; fallback to default columns.
- Compute final scope (Layer3 → Layer2 → Layer1 → SELF).
- Append SQL filter, for example:
```
WHERE (
  o.create_dept IN (...dept ids...)
  OR o.create_by = #{userId}
)
```

**Special Cases**
- `dept_column` is NULL: filter by `user_column` only, DEPT scopes degrade to `SELF`.
- Wrong mapping: DB errors surface directly for quick diagnosis.
- Same table, multiple dimensions: use different `scope_key` values.

**Write-time Ownership**
- Business tables inherit `BaseEntity`, auto-fill `create_by` / `create_dept`.
- `create_dept` is a snapshot of ownership, not affected by later dept changes.

**Recommended Practice**
- New tables: use `create_by` / `create_dept` to avoid extra config.
- Legacy tables: add mappings in `sys_data_scope_rule`.
- User-only filtering: set `dept_column` to NULL.

**Key Takeaways**
- User override > role×menu > role default.
- Multi-role merge is a union.
- Defaults work without extra config.

### Configuration Docs

- All configuration references were moved to `docs/CONFIGURATION_EN.md`.
- Chinese version: `docs/CONFIGURATION.md`.

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
- `Mail health check failed`: check whether `management.health.mail.enabled` is enabled; if enabled, ensure
  `spring.mail.username/password` are fully configured.

## Common Commands

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw -DskipTests package
```
