# Project Help (English)

This document focuses on configuration and operations details, complementing `README_EN.md` (quick start and overview).

## Authentication & Login

- Token header: `Authorization: Bearer <token>` or `X-Auth-Token` only.
- JWT:
    - `auth.jwt.secret` / `auth.jwt.ttl-seconds`
- Password policy:
    - `auth.password.mode` (plain/md5/bcrypt/sm3)
    - `auth.password.transport-mode` (plain/base64/aes-gcm/sm2)
    - `auth.password.transport-key` (AES-GCM Base64 key)
    - `auth.password.strong-check-enabled`
- Login failure limits (sliding window):
    - `auth.login-limit.enabled`
    - `auth.login-limit.max-errors`
    - `auth.login-limit.window-seconds` (counting window)
    - `auth.login-limit.lock-seconds` (lock duration)
    - `auth.login-limit.key-mode` (user/ip/ip-user)
      Note: the failure counter TTL is refreshed on each failure; lock kicks in when the threshold is reached.

## Captcha

- Captcha generation/verification is backed by Redis.
- Key configs:
    - `auth.captcha.width` / `auth.captcha.height`
    - `auth.captcha.code-length`
    - `auth.captcha.thickness`
    - `auth.captcha.rotate-min` / `auth.captcha.rotate-max`
    - `auth.captcha.shear-x-min` / `auth.captcha.shear-x-max`
    - `auth.captcha.shear-y-min` / `auth.captcha.shear-y-max`
    - `auth.captcha.font-resources`
    - `auth.captcha.expire-seconds`

## Permissions & Menus

- Permission checks are controlled by `security.permission.*`.
- Menu permissions are derived from `sys_menu.permission`.

## Scheduled Jobs (Quartz)

- Persistent scheduling: `spring.quartz.*`
- Job handlers must implement `JobHandler` and be registered as Spring beans.
- Execution logs are stored in `sys_job_log`, with detail logs in `log_detail`.

## Job Log Auto Collection

- Switches and scope:
    - `job.log.collect.enabled`
    - `job.log.collect.scope` (MDC | THREAD)
- Level and size:
    - `job.log.collect.min-level`
    - `job.log.collect.max-length`
- Delayed merge:
    - `job.log.collect.merge-delay-millis`
    - `job.log.collect.max-hold-millis`
- Thread-context fallback:
    - `job.log.collect.inherit-thread-context`
- Notes:
    - Logs are collected by MDC during job execution.
    - `@Async` automatically propagates MDC via TaskDecorator.
    - Custom executors should use `MdcUtils.wrapExecutorService` for stable collection.

## Notifications (SSE)

- Push config: `notice.sse.*`
    - Heartbeat interval: `notice.sse.heartbeat-interval-millis`
    - Offline timeout: `notice.sse.heartbeat-timeout-millis`
    - Latest list size: `notice.sse.latest-limit`

## Licenses & Compliance

Generated reports (under `target/`):

- Backend (Maven): `target/site/aggregate-third-party-report.html`
- Frontend (node_modules scan): `target/licenses/frontend-licenses.json`
- Frontend summary: `target/licenses/frontend-licenses-summary.txt`

Regenerate:

```bash
./mvnw -q -DskipTests org.codehaus.mojo:license-maven-plugin:2.4.0:aggregate-third-party-report
python3 scripts/licenses_scan_frontend.py
```

Risk hints (initial check only):

- Backend report includes **EPL 2.0 / LGPL 2.1 / GPL2 w/ CPE** entries; review before commercial release.
- Frontend is mostly MIT/ISC/BSD/Apache, but always verify against the report.

## Data Scope

- Toggle and default scope: `security.data-scope.enabled` / `security.data-scope.default-type`
- Table-to-column mapping: `sys_data_scope_rule` (e.g., map to `dept_id`).

## Security Protections

- SQL guard: `security.sql-guard.*`
- XSS filter: `security.xss.*`
- Rate limiting: `security.rate-limit.*`
- Duplicate-submit protection: `security.duplicate-submit.*`
- Exclude paths are merged with `security.common.exclude-paths`.

## Encrypted Configs (Jasypt)

- Wrap secrets with `ENC(...)`.
- Provide the password via `JASYPT_ENCRYPTOR_PASSWORD` at startup.

## Runtime & Environment

- Main configs: `src/main/resources/application.yml` + `application-dev.yml`
- Default profile: `dev` (override with `SPRING_PROFILES_ACTIVE`)
- DB scripts: `sql/mysql.sql`, `sql/postgresql.sql`
- Druid monitor: `spring.datasource.druid.stat-view-servlet.*`

## Troubleshooting

- Too many login failures: check `auth.login-limit.*` and Redis.
- Invalid captcha: verify Redis and captcha expiration.
- Invalid/expired token: check `auth.jwt.ttl-seconds` and system clock.

## Common Commands

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw -DskipTests package
```
