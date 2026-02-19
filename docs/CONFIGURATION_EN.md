# Configuration Guide

This document is split from `README_EN.md` and centralizes all configuration references.

### Mail Notification Capability (`notify.mail` + `spring.mail`)

- Default implementation is under `src/main/java/com/example/demo/common/notify/mail`, reusable by modules like `auth`
  and `notice`.
- When enabled, SMTP is used via Spring Mail; when disabled, it falls back to a Noop sender (log only, no actual email).
- SMTP connection settings come from standard `spring.mail.*`; business switch/text settings come from `notify.mail.*`.

**`notify.mail` business settings**

| Key                          | Default  | Description                                                      |
|------------------------------|----------|------------------------------------------------------------------|
| `notify.mail.enabled`        | `false`  | Enables SMTP mail sending capability.                            |
| `notify.mail.from`           | ``       | Sender address. Falls back to `spring.mail.username` when empty. |
| `notify.mail.subject-prefix` | `[Demo]` | Subject prefix auto-prepended to outgoing emails.                |

**`spring.mail` SMTP settings (Spring standard)**

| Key                                                | Default      | Description                                           |
|----------------------------------------------------|--------------|-------------------------------------------------------|
| `spring.mail.host`                                 | ``           | SMTP host.                                            |
| `spring.mail.port`                                 | ``           | SMTP port.                                            |
| `spring.mail.username`                             | ``           | SMTP username (also used as default sender).          |
| `spring.mail.password`                             | ``           | SMTP password/app token.                              |
| `spring.mail.properties.mail.smtp.auth`            | `true/false` | Enables SMTP auth depending on provider requirements. |
| `spring.mail.properties.mail.smtp.starttls.enable` | `true/false` | Enables STARTTLS.                                     |
| `spring.mail.properties.mail.smtp.ssl.enable`      | `true/false` | Enables SSL.                                          |

### Auth Security Enhancements (`auth.security`)

- Bound in `src/main/java/com/example/demo/auth/config/AuthProperties.java`.
- Current capabilities:
    1. Login anomaly alert (async check after successful login, with email alert).
    2. Sensitive-operation email confirmation (send code + verify code + issue short-lived ticket).

**Login anomaly alert (`auth.security.login-anomaly`)**

| Key                                                   | Default  | Description                                                            |
|-------------------------------------------------------|----------|------------------------------------------------------------------------|
| `auth.security.login-anomaly.enabled`                 | `true`   | Enables login anomaly alerting.                                        |
| `auth.security.login-anomaly.notify-on-ip-change`     | `true`   | Triggers alerts on IP change.                                          |
| `auth.security.login-anomaly.notify-on-device-change` | `true`   | Triggers alerts on device fingerprint change (device type/OS/browser). |
| `auth.security.login-anomaly.mail-subject`            | `登录安全提醒` | Alert email subject (without `notify.mail.subject-prefix`).            |

**Sensitive-operation confirmation (`auth.security.operation-confirm`)**

| Key                                                       | Default     | Description                                                              |
|-----------------------------------------------------------|-------------|--------------------------------------------------------------------------|
| `auth.security.operation-confirm.enabled`                 | `true`      | Enables email-based operation confirmation.                              |
| `auth.security.operation-confirm.code-length`             | `6`         | Verification code length (internally clamped to 4~10).                   |
| `auth.security.operation-confirm.code-ttl-seconds`        | `300`       | Code TTL in seconds (minimum 60).                                        |
| `auth.security.operation-confirm.resend-interval-seconds` | `60`        | Resend cooldown in seconds (minimum 10).                                 |
| `auth.security.operation-confirm.max-verify-attempts`     | `5`         | Max verification failures per code.                                      |
| `auth.security.operation-confirm.ticket-ttl-seconds`      | `900`       | Issued ticket TTL in seconds after successful verification (minimum 60). |
| `auth.security.operation-confirm.mail-subject`            | `敏感操作确认验证码` | Confirmation email subject (without `notify.mail.subject-prefix`).       |

**Related APIs (login required)**

| API                                            | Description                                     |
|------------------------------------------------|-------------------------------------------------|
| `POST /auth/security/operation-confirm/send`   | Sends operation confirmation email code.        |
| `POST /auth/security/operation-confirm/verify` | Verifies code and returns a short-lived ticket. |

### Datascope Constants Override (`datascope.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/datascope/config/DataScopeConstants.java`.
- Override behavior by setting `datascope.constants.*` in configuration files; missing keys keep defaults.
- Recommendation: only override keys that differ by business/environment.

**Scope Group (global override identity)**

| Key | Default | Description |
|---|---|---|
| `datascope.constants.scope.global-scope-key` | `*` | Scope key treated as a global user-level override fallback for all permissions. |
| `datascope.constants.scope.global-scope-menu-name` | `全局覆盖` | Display name used in user data-scope list for global overrides. |
| `datascope.constants.scope.global-scope-permission` | `*` | Display permission value used in user data-scope list for global overrides. |

**Layer Group (source labels)**

| Key | Default | Description |
|---|---|---|
| `datascope.constants.layer.source-layer3` | `LAYER3` | Source label for Layer 3 (user override). |
| `datascope.constants.layer.source-layer2` | `LAYER2` | Source label for Layer 2 (role×menu override). |
| `datascope.constants.layer.source-layer1` | `LAYER1` | Source label for Layer 1 (role default). |

**Label Group (final scope labels)**

| Key | Default | Description |
|---|---|---|
| `datascope.constants.label.final-all` | `ALL` | Label used when final scope is full access. |
| `datascope.constants.label.final-none` | `NONE` | Label used when final scope has no access. |
| `datascope.constants.label.final-self` | `SELF` | Label used when final scope is self only. |
| `datascope.constants.label.final-dept` | `DEPT` | Label used when final scope is department-based. |
| `datascope.constants.label.final-dept-and-self` | `DEPT+SELF` | Label used when final scope combines department and self. |

**Rule Group (rule source and fallback columns)**

| Key | Default | Description |
|---|---|---|
| `datascope.constants.rule.source-default` | `DEFAULT` | Rule source marker when no mapping is found in `sys_data_scope_rule`. |
| `datascope.constants.rule.source-mapping` | `MAPPING` | Rule source marker when mapping is found in `sys_data_scope_rule`. |
| `datascope.constants.rule.default-dept-column` | `create_dept` | Fallback department column when no mapping exists. |
| `datascope.constants.rule.default-user-column` | `create_by` | Fallback user column when no mapping exists. |

**SQL Group (condition fragments)**

| Key | Default | Description |
|---|---|---|
| `datascope.constants.sql.no-filter-text` | `无过滤` | Human-readable SQL description when final scope is `ALL`. |
| `datascope.constants.sql.none-condition` | `1 = 0` | SQL condition used when final scope is `NONE`. |
| `datascope.constants.sql.in-operator` | ` IN ` | `IN` operator fragment (with spaces) for department conditions. |
| `datascope.constants.sql.equals-operator` | ` = ` | Equality operator fragment (with spaces) for self conditions. |
| `datascope.constants.sql.self-user-param` | `:userId` | User parameter placeholder used by self conditions. |
| `datascope.constants.sql.or-operator` | ` OR ` | Logical OR fragment (with spaces) to combine dept and self conditions. |
| `datascope.constants.sql.dot` | `.` | Separator between alias and column. |
| `datascope.constants.sql.left-bracket` | `(` | Left bracket used in composite SQL conditions. |
| `datascope.constants.sql.right-bracket` | `)` | Right bracket used in composite SQL conditions. |

**Status Group**

| Key | Default | Description |
|---|---|---|
| `datascope.constants.status.enabled` | `1` | Unified enabled value for datascope records/rules. |
| `datascope.constants.status.disabled` | `0` | Unified disabled value for datascope records/rules. |
| `datascope.constants.status.role-active` | `1` | Role status value considered active during scope resolving. |

**Controller Group (error codes)**

| Key | Default | Description |
|---|---|---|
| `datascope.constants.controller.bad-request-code` | `400` | Error code for request conflicts (e.g. duplicated scope key). |
| `datascope.constants.controller.not-found-code` | `404` | Error code for missing users or rules. |
| `datascope.constants.controller.internal-server-error-code` | `500` | Error code for update/delete failures. |

**Filter Group (filter-type bounds)**

| Key | Default | Description |
|---|---|---|
| `datascope.constants.filter.default-type` | `1` | Default filter type when request value is absent/invalid. |
| `datascope.constants.filter.type-min` | `1` | Minimum allowed filter type. |
| `datascope.constants.filter.type-max` | `3` | Maximum allowed filter type. |

**Parser Group**

| Key | Default | Description |
|---|---|---|
| `datascope.constants.parser.dept-id-separator` | `,` | Separator used to parse and join custom department ID strings. |

### Dept Constants Override (`dept.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/dept/config/DeptConstants.java`.
- Override behavior via `dept.constants.*` in config files; missing keys keep defaults.
- Recommendation: only override keys that are truly environment/business specific.

**Status Group**

| Key | Default | Description |
|---|---|---|
| `dept.constants.status.enabled` | `1` | Enabled status value. Used as fallback when create/update status is missing or invalid. |
| `dept.constants.status.disabled` | `0` | Disabled status value. Status update endpoint accepts only `enabled/disabled`. |

**Sort Group**

| Key | Default | Description |
|---|---|---|
| `dept.constants.sort.default-sort` | `0` | Default `sort` value when creating a department without explicit sort. |

**Controller Group (error codes)**

| Key | Default | Description |
|---|---|---|
| `dept.constants.controller.bad-request-code` | `400` | Error code for invalid input, duplicate code, or invalid parent checks. |
| `dept.constants.controller.not-found-code` | `404` | Error code when a department record is not found. |
| `dept.constants.controller.internal-server-error-code` | `500` | Error code for update/delete persistence failures. |

**Message Group (i18n keys)**

| Key | Default | Description |
|---|---|---|
| `dept.constants.message.dept-not-found` | `dept.not.found` | i18n key for missing department. |
| `dept.constants.message.dept-code-exists` | `dept.code.exists` | i18n key for duplicated department code. |
| `dept.constants.message.dept-parent-not-found` | `dept.parent.not.found` | i18n key for missing parent department. |
| `dept.constants.message.dept-parent-cannot-self` | `dept.parent.cannot.self` | i18n key when parent department equals self. |
| `dept.constants.message.common-update-failed` | `common.update.failed` | i18n key for generic update failure. |
| `dept.constants.message.common-status-invalid` | `common.status.invalid` | i18n key for invalid status value. |
| `dept.constants.message.common-status-update-failed` | `common.status.update.failed` | i18n key for status update failure. |
| `dept.constants.message.common-delete-failed` | `common.delete.failed` | i18n key for generic delete failure. |

### Dict Constants Override (`dict.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/dict/config/DictConstants.java`.
- Override behavior with `dict.constants.*` in config files; missing keys keep defaults.
- Recommendation: override only keys with real business differences to avoid confusion with dictionary data itself.

**Status Group**

| Key | Default | Description |
|---|---|---|
| `dict.constants.status.enabled` | `1` | Enabled status value for dict type/data records. |
| `dict.constants.status.disabled` | `0` | Disabled status value for dict type/data records. |

**Sort Group**

| Key | Default | Description |
|---|---|---|
| `dict.constants.sort.default-sort` | `0` | Default sort used when creating dict type/data without explicit `sort`. |

**Controller Group (error codes)**

| Key | Default | Description |
|---|---|---|
| `dict.constants.controller.bad-request-code` | `400` | Error code for invalid requests or duplicate dict type/data. |
| `dict.constants.controller.not-found-code` | `404` | Error code when dict type/data is not found. |
| `dict.constants.controller.internal-server-error-code` | `500` | Error code for persistence update/delete failures. |

**Message Group (i18n keys)**

| Key | Default | Description |
|---|---|---|
| `dict.constants.message.dict-type-exists` | `dict.type.exists` | i18n key for duplicated dict type. |
| `dict.constants.message.dict-type-not-found` | `dict.type.not.found` | i18n key for missing dict type. |
| `dict.constants.message.dict-data-exists` | `dict.data.exists` | i18n key for duplicated dict data. |
| `dict.constants.message.dict-data-not-found` | `dict.data.not.found` | i18n key for missing dict data. |
| `dict.constants.message.common-update-failed` | `common.update.failed` | i18n key for generic update failure. |
| `dict.constants.message.common-delete-failed` | `common.delete.failed` | i18n key for generic delete failure. |

**Public API Group**

| Key | Default | Description |
|---|---|---|
| `dict.constants.public-api.success-message` | `success` | Success message text in public dict endpoints. |
| `dict.constants.public-api.batch-type-separator` | `,` | Delimiter for `types` in `GET /api/dict/data/batch`. |

**Cache Group**

| Key | Default | Description |
|---|---|---|
| `dict.constants.cache.key-prefix` | `dict:data:` | Cache key prefix for per-type dict data (`prefix + dictType`). |
| `dict.constants.cache.all-key` | `dict:data:all` | Cache key for all enabled dict data. |
| `dict.constants.cache.ttl-seconds` | `600` | Dict cache TTL in seconds; `<=0` disables cache writes. |

**Serializer Group**

| Key | Default | Description |
|---|---|---|
| `dict.constants.serializer.label-field-suffix` | `Label` | Suffix appended by `@DictLabel` for translated label fields (e.g. `status` -> `statusLabel`). |

### Menu Constants Override (`menu.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/menu/config/MenuConstants.java`.
- Override behavior with `menu.constants.*`; missing keys keep defaults.
- Recommendation: override only keys with real business differences to avoid conflicts with menu-permission semantics.

**Status Group**

| Key | Default | Description |
|---|---|---|
| `menu.constants.status.enabled` | `1` | Enabled status value for menu records. |
| `menu.constants.status.disabled` | `0` | Disabled status value for menu records. |

**Sort Group**

| Key | Default | Description |
|---|---|---|
| `menu.constants.sort.default-sort` | `0` | Default sort used when creating menus without explicit `sort`. |

**Controller Group (error codes)**

| Key | Default | Description |
|---|---|---|
| `menu.constants.controller.bad-request-code` | `400` | Error code for invalid input, parent checks, and code conflicts. |
| `menu.constants.controller.not-found-code` | `404` | Error code when menu resource is not found. |
| `menu.constants.controller.internal-server-error-code` | `500` | Error code for persistence failures in update/status/delete flows. |

**Message Group (i18n keys)**

| Key | Default | Description |
|---|---|---|
| `menu.constants.message.menu-not-found` | `menu.not.found` | i18n key for missing menu. |
| `menu.constants.message.menu-code-exists` | `menu.code.exists` | i18n key for duplicated menu code. |
| `menu.constants.message.menu-parent-not-found` | `menu.parent.not.found` | i18n key for missing parent menu. |
| `menu.constants.message.menu-parent-cannot-self` | `menu.parent.cannot.self` | i18n key when parent menu equals self. |
| `menu.constants.message.common-update-failed` | `common.update.failed` | i18n key for generic update failure. |
| `menu.constants.message.common-status-invalid` | `common.status.invalid` | i18n key for invalid status value. |
| `menu.constants.message.common-status-update-failed` | `common.status.update.failed` | i18n key for status update failure. |
| `menu.constants.message.common-delete-failed` | `common.delete.failed` | i18n key for generic delete failure. |

### Notice Constants Override (`notice.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/notice/config/NoticeConstants.java`.
- Override behavior with `notice.constants.*`; missing keys keep defaults.
- Recommendation: override only keys with real business differences to avoid semantic mismatches with persisted scope values.

**Controller Group (error codes)**

| Key | Default | Description |
|---|---|---|
| `notice.constants.controller.bad-request-code` | `400` | Error code for invalid scope type, empty targets, and other request-level business validation failures. |
| `notice.constants.controller.unauthorized-code` | `401` | Error code for unauthenticated access to my-notice/unread/read/SSE endpoints. |
| `notice.constants.controller.not-found-code` | `404` | Error code when notice resource is missing. |
| `notice.constants.controller.internal-server-error-code` | `500` | Error code for persistence failure in delete operations. |

**Message Group (i18n keys)**

| Key | Default | Description |
|---|---|---|
| `notice.constants.message.notice-scope-invalid` | `notice.scope.invalid` | i18n key for invalid notice scope type. |
| `notice.constants.message.notice-scope-empty` | `notice.scope.empty` | i18n key when scope ids are required but empty. |
| `notice.constants.message.notice-recipients-empty` | `notice.recipients.empty` | i18n key when resolved recipient set is empty. |
| `notice.constants.message.notice-not-found` | `notice.not.found` | i18n key for missing notice. |
| `notice.constants.message.auth-permission-required` | `auth.permission.required` | i18n key when login is required. |
| `notice.constants.message.common-delete-failed` | `common.delete.failed` | i18n key for generic delete failure. |

**Page Group**

| Key | Default | Description |
|---|---|---|
| `notice.constants.page.default-page-num` | `1` | Default page number when paging object is null. |
| `notice.constants.page.default-page-size` | `10` | Default page size when paging object is null. |

**Scope Group**

| Key | Default | Description |
|---|---|---|
| `notice.constants.scope.all` | `ALL` | Scope token for broadcasting to all users. |
| `notice.constants.scope.dept` | `DEPT` | Scope token for department-targeted notices. |
| `notice.constants.scope.role` | `ROLE` | Scope token for role-targeted notices. |
| `notice.constants.scope.user` | `USER` | Scope token for user-targeted notices. |
| `notice.constants.scope.scope-value-separator` | `,` | Separator used when serializing `scopeIds` into `scope_value`. |
| `notice.constants.scope.empty-scope-type` | `` | Normalized value used when scope type is null. |

**Recipient / User / Common / Numeric Group**

| Key | Default | Description |
|---|---|---|
| `notice.constants.recipient.unread` | `0` | Recipient unread status value. |
| `notice.constants.recipient.read` | `1` | Recipient read status value. |
| `notice.constants.user.enabled-status` | `1` | Enabled status value for users eligible to receive notices. |
| `notice.constants.common.not-deleted-flag` | `0` | Logical not-deleted flag value. |
| `notice.constants.numeric.zero-long` | `0` | Long zero fallback for counts/unread defaults. |
| `notice.constants.numeric.zero-int` | `0` | Int zero fallback for update result defaults. |

**Stream Group (SSE)**

| Key | Default | Description |
|---|---|---|
| `notice.constants.stream.anonymous-emitter-timeout-millis` | `0` | SSE emitter timeout for anonymous users (milliseconds). |
| `notice.constants.stream.emitter-timeout-millis` | `0` | SSE emitter timeout for authenticated users (milliseconds). |
| `notice.constants.stream.heartbeat-interval-millis` | `30000` | Heartbeat interval in milliseconds; `<=0` disables heartbeat. |
| `notice.constants.stream.heartbeat-timeout-millis` | `90000` | Client-side disconnect timeout hint in milliseconds. |
| `notice.constants.stream.latest-limit` | `5` | Max cached latest notices included in init/push payload. |
| `notice.constants.stream.event-notice-name` | `notice` | SSE event name for notice/unread updates. |
| `notice.constants.stream.event-init-name` | `init` | SSE event name for initial payload. |
| `notice.constants.stream.event-ping-name` | `ping` | SSE event name for heartbeat. |
| `notice.constants.stream.heartbeat-thread-name` | `notice-sse-heartbeat` | Thread name used by heartbeat scheduler. |
| `notice.constants.stream.log-heartbeat-disabled` | `Notice SSE heartbeat disabled (interval={}ms).` | Log template when heartbeat is disabled. |
| `notice.constants.stream.log-push-failed` | `Failed to push notice to user {}, removing emitter.` | Log template for notice push failures. |
| `notice.constants.stream.log-push-update-failed` | `Failed to push notice update to user {}, removing emitter.` | Log template for unread update push failures. |
| `notice.constants.stream.log-init-failed` | `Failed to send init payload to user {}, removing emitter.` | Log template for init-event push failures. |
| `notice.constants.stream.log-heartbeat-failed` | `Heartbeat failed for user {}, removing emitter: {}` | Log template for heartbeat push failures. |

### Permission Constants Override (`permission.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/permission/config/PermissionConstants.java`.
- Override behavior with `permission.constants.*`; missing keys keep defaults.
- Recommendation: if you override Message-group keys, make sure matching i18n entries exist.

**Status Group**

| Key | Default | Description |
|---|---|---|
| `permission.constants.status.enabled` | `1` | Enabled status value for role/permission records. Used as fallback when create payload status is missing/invalid. |
| `permission.constants.status.disabled` | `0` | Disabled status value for role/permission records. Status update endpoints accept only `enabled/disabled`. |

**Controller Group (error codes)**

| Key | Default | Description |
|---|---|---|
| `permission.constants.controller.bad-request-code` | `400` | Error code for request validation failures (duplicate code, invalid status, missing role-menu relation, etc.). |
| `permission.constants.controller.not-found-code` | `404` | Error code when role/permission resources are missing. |
| `permission.constants.controller.internal-server-error-code` | `500` | Error code for persistence failures in update/delete/assignment flows. |

**Message Group (i18n keys)**

| Key | Default | Description |
|---|---|---|
| `permission.constants.message.permission-not-found` | `permission.not.found` | i18n key for missing permission. |
| `permission.constants.message.permission-code-exists` | `permission.code.exists` | i18n key for duplicated permission code. |
| `permission.constants.message.role-not-found` | `role.not.found` | i18n key for missing role. |
| `permission.constants.message.role-code-exists` | `role.code.exists` | i18n key for duplicated role code. |
| `permission.constants.message.menu-not-found` | `menu.not.found` | i18n key for missing menu. |
| `permission.constants.message.role-menu-not-found` | `role.menu.not.found` | i18n key for missing role-menu relation. |
| `permission.constants.message.role-permissions-assign-failed` | `role.permissions.assign.failed` | i18n key for role-permission assignment failure. |
| `permission.constants.message.role-menus-assign-failed` | `role.menus.assign.failed` | i18n key for role-menu assignment failure. |
| `permission.constants.message.common-update-failed` | `common.update.failed` | i18n key for generic update failure. |
| `permission.constants.message.common-status-invalid` | `common.status.invalid` | i18n key for invalid status value. |
| `permission.constants.message.common-status-update-failed` | `common.status.update.failed` | i18n key for status update failure. |
| `permission.constants.message.common-delete-failed` | `common.delete.failed` | i18n key for generic delete failure. |

**MenuDataScope Group**

| Key | Default | Description |
|---|---|---|
| `permission.constants.menu-data-scope.inherit` | `INHERIT` | Menu-level data-scope token meaning "inherit role default" (case-insensitive). When matched, menu-level override is cleared. |
| `permission.constants.menu-data-scope.default-type` | `DEFAULT` | Compatibility token for menu-level data-scope default value (case-insensitive), same behavior as `inherit`. |

### Post Constants Override (`post.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/post/config/PostConstants.java`.
- Override behavior with `post.constants.*`; missing keys keep defaults.
- Recommendation: if you override Message-group keys, make sure matching i18n entries exist.

**Status Group**

| Key | Default | Description |
|---|---|---|
| `post.constants.status.enabled` | `1` | Enabled status value for post records. Used as fallback when create payload status is missing/invalid. |
| `post.constants.status.disabled` | `0` | Disabled status value for post records. Status update endpoint accepts only `enabled/disabled`. |

**Sort Group**

| Key | Default | Description |
|---|---|---|
| `post.constants.sort.default-sort` | `0` | Default sort value used when creating posts without explicit `sort`. |

**Controller Group (error codes)**

| Key | Default | Description |
|---|---|---|
| `post.constants.controller.bad-request-code` | `400` | Error code for invalid input, duplicate post code, or missing department. |
| `post.constants.controller.not-found-code` | `404` | Error code when post resource is not found. |
| `post.constants.controller.internal-server-error-code` | `500` | Error code for update/status/delete persistence failures. |

**Message Group (i18n keys)**

| Key | Default | Description |
|---|---|---|
| `post.constants.message.post-not-found` | `post.not.found` | i18n key for missing post. |
| `post.constants.message.post-code-exists` | `post.code.exists` | i18n key for duplicated post code. |
| `post.constants.message.dept-not-found` | `dept.not.found` | i18n key for missing department. |
| `post.constants.message.common-update-failed` | `common.update.failed` | i18n key for generic update failure. |
| `post.constants.message.common-status-invalid` | `common.status.invalid` | i18n key for invalid status value. |
| `post.constants.message.common-status-update-failed` | `common.status.update.failed` | i18n key for status update failure. |
| `post.constants.message.common-delete-failed` | `common.delete.failed` | i18n key for generic delete failure. |

### User Constants Override (`user.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/user/config/UserConstants.java`.
- Override behavior with `user.constants.*`; missing keys keep defaults.
- Recommendation: if you override Message-group keys, make sure matching i18n entries exist.

**Status Group**

| Key | Default | Description |
|---|---|---|
| `user.constants.status.enabled` | `1` | Enabled status value for users. Used as fallback when create payload status is missing. |
| `user.constants.status.disabled` | `0` | Disabled status value for users. User status endpoint accepts only `enabled/disabled`. |
| `user.constants.status.data-scope-enabled` | `1` | Default enabled status for newly created user data-scope override records. |

**Controller Group (error codes)**

| Key | Default | Description |
|---|---|---|
| `user.constants.controller.bad-request-code` | `400` | Error code for invalid input, username conflict, invalid password, and data-scope conflicts. |
| `user.constants.controller.forbidden-code` | `403` | Error code for unauthorized batch-delete attempts on inaccessible users. |
| `user.constants.controller.not-found-code` | `404` | Error code when user or user data-scope records are missing. |
| `user.constants.controller.internal-server-error-code` | `500` | Error code for persistence failures in update/delete/reset-password/assignment flows. |

**Password Group**

| Key | Default | Description |
|---|---|---|
| `user.constants.password.min-length` | `6` | Minimum password length for user create and reset-password flows. |

**Page Group**

| Key | Default | Description |
|---|---|---|
| `user.constants.page.default-page-num` | `1` | Default page number when paging argument is null. |
| `user.constants.page.default-page-size` | `10` | Default page size when paging argument is null. |

**Scope Group**

| Key | Default | Description |
|---|---|---|
| `user.constants.scope.global-scope-key` | `*` | Global override `scopeKey` for user data scope. Blank values normalize to this key. |
| `user.constants.scope.global-scope-menu-name` | `全局覆盖` | Display menu name used for global override in user data-scope views. |
| `user.constants.scope.global-scope-permission` | `*` | Display permission value used for global override in user data-scope views. |

**Excel Group**

| Key | Default | Description |
|---|---|---|
| `user.constants.excel.export-file-prefix` | `用户信息` | Default filename prefix for user export files (timestamp appended). |

**Message Group (i18n keys)**

| Key | Default | Description |
|---|---|---|
| `user.constants.message.user-not-found` | `user.not.found` | i18n key for missing user. |
| `user.constants.message.user-username-exists` | `user.username.exists` | i18n key for duplicated username. |
| `user.constants.message.dept-not-found` | `dept.not.found` | i18n key for missing department. |
| `user.constants.message.user-password-empty` | `user.password.empty` | i18n key for empty password. |
| `user.constants.message.user-password-invalid` | `user.password.invalid` | i18n key for invalid decoded password payload. |
| `user.constants.message.user-password-length-invalid` | `user.password.length.invalid` | i18n key for invalid password length. |
| `user.constants.message.user-password-weak` | `user.password.weak` | i18n key for weak password. |
| `user.constants.message.user-password-reset-failed` | `user.password.reset.failed` | i18n key for reset-password failure. |
| `user.constants.message.user-roles-assign-failed` | `user.roles.assign.failed` | i18n key for role assignment failure. |
| `user.constants.message.user-posts-assign-failed` | `user.posts.assign.failed` | i18n key for post assignment failure. |
| `user.constants.message.user-data-scope-update-failed` | `user.data.scope.update.failed` | i18n key for user data-scope update failure. |
| `user.constants.message.data-scope-user-exists` | `data.scope.user.exists` | i18n key for duplicated user data-scope override record. |
| `user.constants.message.data-scope-user-not-found` | `data.scope.user.not.found` | i18n key for missing user data-scope override record. |
| `user.constants.message.common-update-failed` | `common.update.failed` | i18n key for generic update failure. |
| `user.constants.message.common-status-invalid` | `common.status.invalid` | i18n key for invalid status value. |
| `user.constants.message.common-status-update-failed` | `common.status.update.failed` | i18n key for status update failure. |
| `user.constants.message.common-delete-failed` | `common.delete.failed` | i18n key for generic delete failure. |
| `user.constants.message.auth-permission-denied` | `auth.permission.denied` | i18n key for permission denied. |
| `user.constants.message.user-import-success` | `user.import.success` | i18n key for user import success. |
| `user.constants.message.user-import-failed` | `user.import.failed` | i18n key for user import failure. |

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
- Cache: `dict.constants.cache.ttl-seconds` (<=0 disables caching).
- Backend translation: annotate VO fields with `@DictLabel("sys_gender")` to add `xxxLabel`.

### Notifications (SSE)

- Config: `notice.constants.stream.*`.

### Scheduled Jobs (Quartz)

- Persistent scheduling: `spring.quartz.*`.
- Handlers implement `JobHandler` and are Spring beans.
- Logs in `sys_job_log` with detail in `log_detail`.

### Job Log Auto Collection

- Switch/scope: `job.constants.log-collect.enabled`, `job.constants.log-collect.scope`.
- Level/size: `job.constants.log-collect.min-level`, `job.constants.log-collect.max-length`.
- Merge: `job.constants.log-collect.merge-delay-millis`, `job.constants.log-collect.max-hold-millis`.
- Thread context: `job.constants.log-collect.inherit-thread-context`.

### Job Constants Override (`job.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/job/config/JobConstants.java`.
- Override behavior with `job.constants.*`; missing keys keep defaults.
- Recommendation: only override keys with real business differences to avoid conflicts with existing persisted job data.

**Controller Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.controller.bad-request-code` | `400` | Error code for validation failures (cron/handler/misfire). |
| `job.constants.controller.not-found-code` | `404` | Error code for missing jobs or job logs. |
| `job.constants.controller.internal-server-error-code` | `500` | Error code for create/update/delete/run failures. |

**Message Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.message.job-not-found` | `job.not.found` | i18n key for missing job. |
| `job.constants.message.job-log-not-found` | `job.log.not.found` | i18n key for missing job log. |
| `job.constants.message.job-cron-invalid` | `job.cron.invalid` | i18n key for invalid cron expression. |
| `job.constants.message.job-handler-invalid` | `job.handler.invalid` | i18n key for invalid handler name. |
| `job.constants.message.job-misfire-invalid` | `job.misfire.invalid` | i18n key for invalid misfire policy. |
| `job.constants.message.job-create-failed` | `job.create.failed` | i18n key for job creation failure. |
| `job.constants.message.job-update-failed` | `job.update.failed` | i18n key for job update failure. |
| `job.constants.message.job-delete-failed` | `job.delete.failed` | i18n key for job delete failure. |
| `job.constants.message.job-status-update-failed` | `job.status.update.failed` | i18n key for status update failure. |
| `job.constants.message.job-run-failed` | `job.run.failed` | i18n key for manual run failure. |

**Page Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.page.default-page-num` | `1` | Default page number used when page object is null. |
| `job.constants.page.default-page-size` | `10` | Default page size used when page object is null. |

**Status Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.status.job-enabled` | `1` | Status value for enabled jobs. |
| `job.constants.status.job-disabled` | `0` | Status value for disabled jobs. |
| `job.constants.status.log-success` | `1` | Status value for successful execution logs. |
| `job.constants.status.log-failed` | `0` | Status value for failed execution logs. |

**Concurrent Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.concurrent.allow` | `1` | Flag meaning concurrent execution is allowed. |
| `job.constants.concurrent.disallow` | `0` | Flag meaning concurrent execution is disallowed. |

**Scheduler Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.scheduler.job-group` | `SYS_JOB` | Quartz `JobKey` group. |
| `job.constants.scheduler.trigger-group` | `SYS_JOB_TRIGGER` | Quartz `TriggerKey` group. |
| `job.constants.scheduler.job-key-prefix` | `JOB_` | Prefix for Quartz job keys (`JOB_ + jobId`). |
| `job.constants.scheduler.trigger-key-prefix` | `TRIGGER_` | Prefix for Quartz trigger keys (`TRIGGER_ + jobId`). |

**DataMap Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.data-map.job-id-key` | `jobId` | JobDataMap key for job id. |
| `job.constants.data-map.job-name-key` | `jobName` | JobDataMap key for job name. |
| `job.constants.data-map.handler-name-key` | `handlerName` | JobDataMap key for handler name. |
| `job.constants.data-map.cron-expression-key` | `cronExpression` | JobDataMap key for cron expression. |
| `job.constants.data-map.params-key` | `params` | JobDataMap key for params. |

**Execution Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.execution.execute-start-prefix` | `开始执行: ` | Prefix for execution start line in manual logs. |
| `job.constants.execution.params-prefix` | `参数: ` | Prefix for params line in manual logs. |
| `job.constants.execution.handler-not-found-message` | `handler not found` | Message persisted when handler is missing. |
| `job.constants.execution.handler-not-found-log-prefix` | `处理器不存在: ` | Manual-log prefix when handler is missing. |
| `job.constants.execution.execute-success-log` | `执行成功` | Manual-log text for successful execution. |
| `job.constants.execution.execute-error-prefix` | `执行异常: ` | Prefix for execution error manual logs. |
| `job.constants.execution.log-merge-separator` | `\n----\n` | Separator between manual logs and collected logs. |
| `job.constants.execution.message-max-length` | `500` | Max length for `sys_job_log.message`. |
| `job.constants.execution.log-detail-max-length` | `8000` | Max length for `sys_job_log.log_detail`. |

**Handler Demo Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.handler-demo.manual-log-start` | `手动记录定时任务日志` | Demo handler start log text. |
| `job.constants.handler-demo.manual-log-end` | `手动记录日志任务结束` | Demo handler end log text. |
| `job.constants.handler-demo.new-thread-log` | `new Thread 未显式透传也可收集日志` | Demo log text for plain thread branch. |
| `job.constants.handler-demo.async-thread-log` | `异步线程日志手动记录` | Demo log text for async wrapped thread branch. |
| `job.constants.handler-demo.plain-thread-name` | `job-log-plain` | Thread name for plain thread branch. |
| `job.constants.handler-demo.async-thread-name` | `job-log-demo` | Thread name for wrapped async thread branch. |
| `job.constants.handler-demo.raw-executor-pool-size` | `1` | Pool size for plain executor demo. |
| `job.constants.handler-demo.wrapped-executor-pool-size` | `1` | Pool size for wrapped executor demo. |
| `job.constants.handler-demo.schedule-delay-millis` | `100` | Delay for scheduled executor demo task. |

**Log Collect Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.log-collect.enabled` | `true` | Whether automatic job-log collection is enabled. |
| `job.constants.log-collect.scope` | `MDC` | Collection scope: `MDC` or `THREAD`. |
| `job.constants.log-collect.min-level` | `INFO` | Minimum log level to collect. |
| `job.constants.log-collect.max-length` | `65536` | Max collected log length per run (chars). |
| `job.constants.log-collect.merge-delay-millis` | `3000` | Delay before merge/update after run completion. |
| `job.constants.log-collect.max-hold-millis` | `60000` | Max in-memory hold time for a run buffer. |
| `job.constants.log-collect.inherit-thread-context` | `true` | Whether inheritable thread context fallback is enabled. |
| `job.constants.log-collect.mdc-key` | `jobLogId` | MDC key storing run id. |
| `job.constants.log-collect.thread-key` | `jobLogThread` | MDC key storing thread name (used when `scope=THREAD`). |
| `job.constants.log-collect.collector-thread-name` | `job-log-collector` | Background collector thread name. |
| `job.constants.log-collect.cleanup-initial-delay-millis` | `60000` | Initial delay before cleanup task starts. |
| `job.constants.log-collect.cleanup-interval-millis` | `60000` | Cleanup task fixed-rate interval. |

**Appender Group**

| Key | Default | Description |
|---|---|---|
| `job.constants.appender.appender-name` | `JOB_LOG_COLLECTOR` | Name of the appender attached to root logger. |
| `job.constants.appender.time-pattern` | `HH:mm:ss.SSS` | Time format used in collected lines. |
| `job.constants.appender.empty-message` | `` | Replacement text when log message is null. |
| `job.constants.appender.throwable-separator` | `\n` | Separator between base log line and throwable stack string. |

### Security Protections

- SQL guard: `security.sql-guard.*`.
- SQL guard extras: `block-with-clause`, `block-union`, `blocked-functions`, `allowed-tables`, `allowed-columns`.
- XSS filter: `security.xss.*`.
- Rate limiting: `security.rate-limit.*`.
- Duplicate-submit: `security.duplicate-submit.*`.
- Exclude paths are merged with `security.common.exclude-paths`.

### Dynamic API (core-http-extension)

**Entry and Management APIs**

- Runtime entry: `/ext/**` (matched by the dynamic API registry).
- Management APIs: `/dynamic-api/**` (CRUD, enable/disable, reload).
- Metadata APIs: `/dynamic-api/metadata/beans`, `/dynamic-api/metadata/rate-limit-policies`,
  `/dynamic-api/metadata/types`, `/dynamic-api/metadata/metrics`.
- Response header: `X-Dynamic-Api-Termination` is returned on timeout/cancel/reject.
- Response body: strategy `meta` or `errorDetails` is surfaced when provided.

**Types and Extensibility**

- Built-in types: `BEAN` / `SQL` / `HTTP`.
- Custom types: implement `com.example.demo.extension.api.executor.ExecuteStrategy` and register as a Spring bean; the
  UI type list is loaded from
  `/dynamic-api/metadata/types`.
- Custom configs are parsed by `ExecuteStrategy.parseConfig`; if not provided, raw `config` is passed through as a
  string.
- Optional ServiceLoader discovery: `dynamic.api.strategy.enable-service-loader=true`.
- Duplicate type policy: `dynamic.api.strategy.duplicate-type-policy=REPLACE|KEEP_FIRST|FAIL|PREFER_HIGHEST_VERSION`.

**SQL Type Restrictions (`type=SQL`)**

- Only `SELECT` is allowed (non-SELECT will be rejected).
- `security.sql-guard.block-multi-statement=true` blocks multi-statements.
- `security.sql-guard.block-cross-schema-join=true` blocks cross-schema JOINs and enforces `allowed-schemas`.
- `security.sql-guard.block-with-clause=true` blocks `WITH`.
- `security.sql-guard.block-union=true` blocks `UNION/UNION ALL`.
- `security.sql-guard.blocked-functions` configures a function blacklist (case-insensitive).
- `security.sql-guard.allowed-tables` / `security.sql-guard.allowed-columns` configure table/column whitelists.
- SQL rows are capped by `dynamic.api.constants.execute.sql-max-rows`.
- Named parameters are supported (e.g. `:name`), bound from request params.

**HTTP Forwarding Safety (`type=HTTP`)**

- Allowed schemes: `dynamic.api.constants.http.allowed-schemes` (default `http/https`).
- Allowed hosts (wildcards): `dynamic.api.constants.http.allowed-hosts` (empty = no restriction).
- Blocked hosts (wildcards): `dynamic.api.constants.http.blocked-hosts`.
- Allowed CIDRs: `dynamic.api.constants.http.allowed-cidrs` (empty = no restriction).
- Blocked CIDRs: `dynamic.api.constants.http.blocked-cidrs`.
- Block private/loopback: `dynamic.api.constants.http.block-private-network=false` (recommended in prod).
- Block on DNS failure: `dynamic.api.constants.http.block-unknown-host=true`.

**Request Context Headers**

- Request ID header: `dynamic.api.constants.http.request-id-header` (default `X-Request-Id`).
- Tenant ID header: `dynamic.api.constants.http.tenant-id-header` (default `X-Tenant-Id`).
- Trace ID header: `dynamic.api.constants.http.trace-id-header` (default `X-Trace-Id`).

**Datasource Mode Notes**

- `multi-datasource`: dynamic SQL runs on the dynamic datasource primary (default `system_rw`); it does not auto-route
  by module package.
- `single-datasource-multi-schema`: dynamic SQL uses `spring.datasource`.
- `single-datasource-single-schema`: startup overrides `security.sql-guard.allowed-schemas` to
  `app.datasource.single-schema-name`. Schema rewrite only applies to MyBatis, not dynamic SQL.

**Dynamic API Config (`dynamic.api.*`)**

| Key                                            | Default     | Description                                                         |
|------------------------------------------------|-------------|---------------------------------------------------------------------|
| `dynamic.api.global.enabled`                   | `true`      | Global switch.                                                      |
| `dynamic.api.executor.core-pool-size`          | `8`         | Executor core pool size.                                            |
| `dynamic.api.executor.max-pool-size`           | `16`        | Executor max pool size.                                             |
| `dynamic.api.executor.queue-capacity`          | `200`       | Executor queue capacity.                                            |
| `dynamic.api.executor.keep-alive-seconds`      | `60`        | Thread keep-alive seconds.                                          |
| `dynamic.api.executor.thread-name-prefix`      | `ext-exec-` | Thread name prefix.                                                 |
| `dynamic.api.executor.rejected-policy`         | `ABORT`     | Rejection policy: `ABORT`/`CALLER_RUNS`/`DISCARD`/`DISCARD_OLDEST`. |
| `dynamic.api.executors`                        | `{}`        | Custom executors (name -> config).                                  |
| `dynamic.api.executor-routes`                  | `[]`        | Executor routing rules (by api/type/path).                          |
| `dynamic.api.circuit-breaker.enabled`          | `false`     | Enable circuit breaker.                                             |
| `dynamic.api.circuit-breaker.window-seconds`   | `60`        | Window seconds.                                                     |
| `dynamic.api.circuit-breaker.minimum-calls`    | `20`        | Minimum calls.                                                      |
| `dynamic.api.circuit-breaker.failure-rate`     | `0.5`       | Failure rate threshold (0-1).                                       |
| `dynamic.api.circuit-breaker.open-duration-ms` | `30000`     | Open duration in ms.                                                |
| `dynamic.api.metrics.enabled`                  | `true`      | Enable dynamic API metrics.                                         |
| `dynamic.api.metrics.max-details`              | `200`       | Max metrics items.                                                  |
| `dynamic.api.rate-limit-policies`              | `[]`        | Rate limit policy list for dynamic APIs.                            |

**Executors (`dynamic.api.executors`)**

| Key                  | Default     | Description                                                         |
|----------------------|-------------|---------------------------------------------------------------------|
| `core-pool-size`     | `8`         | Core pool size.                                                     |
| `max-pool-size`      | `16`        | Max pool size.                                                      |
| `queue-capacity`     | `200`       | Queue capacity.                                                     |
| `keep-alive-seconds` | `60`        | Keep-alive seconds.                                                 |
| `thread-name-prefix` | `ext-exec-` | Thread name prefix.                                                 |
| `rejected-policy`    | `ABORT`     | Rejection policy: `ABORT`/`CALLER_RUNS`/`DISCARD`/`DISCARD_OLDEST`. |

**Executor Routes (`dynamic.api.executor-routes[]`)**

| Key           | Default | Description                    |
|---------------|---------|--------------------------------|
| `executor-id` | -       | Target executor id (required). |
| `type`        | -       | Dynamic API type (optional).   |
| `api-id`      | -       | Dynamic API id (optional).     |
| `path-prefix` | -       | Path prefix (optional).        |

- Routes are matched in order; first match wins.

**Rate Limit Policy Item (`dynamic.api.rate-limit-policies[]`)**

| Key              | Default | Description                           |
|------------------|---------|---------------------------------------|
| `id`             | -       | Policy id (required).                 |
| `name`           | -       | Policy name.                          |
| `window-seconds` | `0`     | Window size (seconds).                |
| `max-requests`   | `0`     | Max requests in the window.           |
| `key-mode`       | -       | Key mode (same as global rate limit). |
| `include-path`   | `true`  | Whether to include path in key.       |

**Constants Override (`dynamic.api.constants.*`)**

**Controller**

| Key                                                           | Default | Description               |
|---------------------------------------------------------------|---------|---------------------------|
| `dynamic.api.constants.controller.bad-request-code`           | `400`   | Bad request code.         |
| `dynamic.api.constants.controller.not-found-code`             | `404`   | Not found code.           |
| `dynamic.api.constants.controller.internal-server-error-code` | `500`   | Execution error code.     |
| `dynamic.api.constants.controller.service-unavailable-code`   | `503`   | Service unavailable code. |
| `dynamic.api.constants.controller.rate-limit-code`            | `429`   | Rate limit code.          |
| `dynamic.api.constants.controller.rejected-code`              | `429`   | Rejected execution code.  |

**Message**

| Key                                                  | Default                            | Description           |
|------------------------------------------------------|------------------------------------|-----------------------|
| `dynamic.api.constants.message.not-found`            | `dynamic.api.not.found`            | Not found.            |
| `dynamic.api.constants.message.global-disabled`      | `dynamic.api.global.disabled`      | Globally disabled.    |
| `dynamic.api.constants.message.path-invalid`         | `dynamic.api.path.invalid`         | Invalid path.         |
| `dynamic.api.constants.message.method-invalid`       | `dynamic.api.method.invalid`       | Invalid method.       |
| `dynamic.api.constants.message.type-invalid`         | `dynamic.api.type.invalid`         | Invalid type.         |
| `dynamic.api.constants.message.config-invalid`       | `dynamic.api.config.invalid`       | Invalid config.       |
| `dynamic.api.constants.message.bean-invalid`         | `dynamic.api.bean.invalid`         | Invalid bean config.  |
| `dynamic.api.constants.message.sql-invalid`          | `dynamic.api.sql.invalid`          | Invalid SQL config.   |
| `dynamic.api.constants.message.http-invalid`         | `dynamic.api.http.invalid`         | Invalid HTTP config.  |
| `dynamic.api.constants.message.create-failed`        | `dynamic.api.create.failed`        | Create failed.        |
| `dynamic.api.constants.message.update-failed`        | `dynamic.api.update.failed`        | Update failed.        |
| `dynamic.api.constants.message.delete-failed`        | `dynamic.api.delete.failed`        | Delete failed.        |
| `dynamic.api.constants.message.status-update-failed` | `dynamic.api.status.update.failed` | Status update failed. |
| `dynamic.api.constants.message.execute-failed`       | `dynamic.api.execute.failed`       | Execute failed.       |
| `dynamic.api.constants.message.timeout`              | `dynamic.api.timeout`              | Timeout.              |
| `dynamic.api.constants.message.rejected`             | `dynamic.api.rejected`             | Rejected/overloaded.  |
| `dynamic.api.constants.message.circuit-open`         | `dynamic.api.circuit.open`         | Circuit open.         |
| `dynamic.api.constants.message.response-too-large`   | `dynamic.api.response.too.large`   | Response too large.   |

**HTTP**

| Key                                                    | Default                     | Description                         |
|--------------------------------------------------------|-----------------------------|-------------------------------------|
| `dynamic.api.constants.http.ext-prefix`                | `/ext/`                     | Dynamic API prefix.                 |
| `dynamic.api.constants.http.error-path`                | `/error`                    | Error prefix (forbidden).           |
| `dynamic.api.constants.http.actuator-prefix`           | `/actuator`                 | Actuator prefix (forbidden).        |
| `dynamic.api.constants.http.supported-methods`         | `GET,POST,PUT,PATCH,DELETE` | Supported HTTP methods.             |
| `dynamic.api.constants.http.max-total-connections`     | `200`                       | HTTP max total connections.         |
| `dynamic.api.constants.http.max-connections-per-route` | `50`                        | HTTP max connections per route.     |
| `dynamic.api.constants.http.idle-evict-seconds`        | `30`                        | HTTP idle connection evict seconds. |
| `dynamic.api.constants.http.allowed-cidrs`             | `[]`                        | Allowed target CIDRs.               |
| `dynamic.api.constants.http.blocked-cidrs`             | `[]`                        | Blocked target CIDRs.               |

**Execute**

| Key                                                                  | Default                     | Description                               |
|----------------------------------------------------------------------|-----------------------------|-------------------------------------------|
| `dynamic.api.constants.execute.log-max-length`                       | `2000`                      | Max log length for dynamic API.           |
| `dynamic.api.constants.execute.default-timeout-ms`                   | `3000`                      | Default timeout in ms.                    |
| `dynamic.api.constants.execute.max-timeout-ms`                       | `60000`                     | Max timeout in ms, <=0 means no limit.    |
| `dynamic.api.constants.execute.cleanup-timeout-ms`                   | `1000`                      | Max cleanup callback time in ms.          |
| `dynamic.api.constants.execute.max-response-bytes`                   | `1048576`                   | Max response bytes (<=0 means unlimited). |
| `dynamic.api.constants.execute.sql-max-rows`                         | `500`                       | SQL max rows (<=0 means unlimited).       |
| `dynamic.api.constants.execute.sql-fetch-size`                       | `200`                       | SQL fetch size (<=0 means unset).         |
| `dynamic.api.constants.execute.masked-keys`                          | `password,token,secret,...` | Sensitive keys for log masking.           |
| `dynamic.api.constants.execute.cleanup-executor-core-pool-size`      | `1`                         | Cleanup executor core pool size.          |
| `dynamic.api.constants.execute.cleanup-executor-max-pool-size`       | `2`                         | Cleanup executor max pool size.           |
| `dynamic.api.constants.execute.cleanup-executor-queue-capacity`      | `200`                       | Cleanup executor queue capacity.          |
| `dynamic.api.constants.execute.cleanup-executor-keep-alive-seconds`  | `30`                        | Cleanup executor keep-alive seconds.      |
| `dynamic.api.constants.execute.cleanup-executor-thread-name-prefix`  | `ext-cleanup-`              | Cleanup executor thread name prefix.      |
| `dynamic.api.constants.execute.cleanup-scheduler-pool-size`          | `1`                         | Cleanup scheduler pool size.              |
| `dynamic.api.constants.execute.cleanup-scheduler-thread-name-prefix` | `ext-cleanup-scheduler-`    | Cleanup scheduler thread name prefix.     |

**Config Examples (`type` and `config`)**

```json
// BEAN
{"beanName":"orderSummaryHandler","paramMode":"AUTO","paramSchema":"{\"orderId\":\"string\"}"}
```

```json
// SQL
{"sql":"SELECT * FROM order.orders WHERE id = :id"}
```

```json
// HTTP
{"url":"https://internal/api","method":"POST","passHeaders":true,"passQuery":true,"headers":{"X-Token":"secret"}}
```

### Log Constants Override (`log.constants`)

- Defaults are centralized in `src/main/java/com/example/demo/log/config/LogConstants.java`.
- Override behavior with `log.constants.*`; missing keys keep defaults.
- For `List/Map` keys, use standard Spring Boot binding:
- `log.constants.aspect.default-exclude-params[0]=password`
- `log.constants.aspect.title-mappings.user=User Management`
- `log.constants.ip.headers[0]=X-Forwarded-For`

**Controller Group**

| Key | Default | Description |
|---|---|---|
| `log.constants.controller.bad-request-code` | `400` | HTTP code used for invalid-parameter cases. |
| `log.constants.controller.not-found-code` | `404` | HTTP code used when resource is not found. |
| `log.constants.controller.internal-server-error-code` | `500` | HTTP code used for internal execution failures. |

**Message Group**

| Key | Default | Description |
|---|---|---|
| `log.constants.message.common-delete-failed` | `common.delete.failed` | i18n key for delete-failed responses. |
| `log.constants.message.login-log-persist-failed` | `登录日志入库失败` | Log template for async login-log persistence failures. |
| `log.constants.message.oper-log-persist-failed` | `操作日志入库失败` | Log template for async operation-log persistence failures. |
| `log.constants.message.spel-parse-failed` | `解析操作日志SpEL失败: {}` | Log template for SpEL parsing failures (first placeholder is template text). |

**Page / Query / Status Group**

| Key | Default | Description |
|---|---|---|
| `log.constants.page.default-page-num` | `1` | Default page number when paging object is null. |
| `log.constants.page.default-page-size` | `10` | Default page size when paging object is null. |
| `log.constants.query.date-time-pattern` | `yyyy-MM-dd HH:mm:ss` | Default formatter pattern for query time parsing. |
| `log.constants.status.oper-success` | `1` | Status value for successful operation logs. |
| `log.constants.status.oper-failed` | `0` | Status value for failed operation logs. |

**HTTP Group**

| Key | Default | Description |
|---|---|---|
| `log.constants.http.get-method` | `GET` | GET method token. |
| `log.constants.http.options-method` | `OPTIONS` | OPTIONS method token. |
| `log.constants.http.post-method` | `POST` | POST method token. |
| `log.constants.http.put-method` | `PUT` | PUT method token. |
| `log.constants.http.patch-method` | `PATCH` | PATCH method token. |
| `log.constants.http.delete-method` | `DELETE` | DELETE method token. |
| `log.constants.http.permission-separator` | `:` | Separator used to extract module prefix from permission strings. |
| `log.constants.http.method-url-separator` | ` ` | Separator used by `method + separator + url` fallback operation text. |

**Aspect Group**

| Key | Default | Description |
|---|---|---|
| `log.constants.aspect.max-text-length` | `2000` | Truncation limit for params/result/error text. |
| `log.constants.aspect.default-exclude-params` | `password,oldPassword,newPassword,token` | Default sensitive fields when `excludeParams` is not explicitly set. |
| `log.constants.aspect.title-mappings.user` | `用户管理` | Example mapping entry from permission prefix to title (same pattern for other keys). |
| `log.constants.aspect.spel-pattern` | `#\{(.+?)}` | Regex used to match SpEL placeholders. |
| `log.constants.aspect.spel-null-literal` | `null` | Replacement text when SpEL result is null. |
| `log.constants.aspect.mask-value` | `******` | Replacement text for masked values. |
| `log.constants.aspect.spring-validation-package-prefix` | `org.springframework.validation.` | Ignored argument package prefix for Spring validation objects. |
| `log.constants.aspect.spring-multipart-package-prefix` | `org.springframework.web.multipart.` | Ignored argument package prefix for multipart objects. |

**IP Group**

| Key | Default | Description |
|---|---|---|
| `log.constants.ip.headers` | `X-Forwarded-For,X-Real-IP,Proxy-Client-IP,WL-Proxy-Client-IP,HTTP_CLIENT_IP,HTTP_X_FORWARDED_FOR` | Header chain used to resolve client IP. |
| `log.constants.ip.unknown-token` | `unknown` | Placeholder token that means header IP is invalid. |
| `log.constants.ip.multi-ip-separator` | `,` | Separator for multi-proxy IP list. |
| `log.constants.ip.internal-ip-text` | `内网IP` | Text returned for internal-network IPs. |
| `log.constants.ip.unknown-location-text` | `未知` | Text returned when location cannot be resolved. |
| `log.constants.ip.ipv4-segment-separator-regex` | `\.` | Regex separator used for IPv4 octet split. |
| `log.constants.ip.ipv4-loopback-prefix` | `127.` | IPv4 loopback prefix. |
| `log.constants.ip.ipv6-loopback-full` | `0:0:0:0:0:0:0:1` | Full IPv6 loopback address. |
| `log.constants.ip.ipv6-loopback-short` | `::1` | Short IPv6 loopback address. |
| `log.constants.ip.private-a-prefix` | `10.` | Class A private-network prefix. |
| `log.constants.ip.private-c-prefix` | `192.168.` | Class C private-network prefix. |
| `log.constants.ip.private-b-prefix` | `172.` | Class B private-network prefix. |
| `log.constants.ip.private-b-second-octet-min` | `16` | Min second octet for private Class B range. |
| `log.constants.ip.private-b-second-octet-max` | `31` | Max second octet for private Class B range. |

**User-Agent Group**

| Key | Default | Description |
|---|---|---|
| `log.constants.user-agent.unknown` | `Unknown` | Fallback name when value cannot be detected. |
| `log.constants.user-agent.pc` | `PC` | Default desktop device name. |
| `log.constants.user-agent.browser-edge-token` | `edg/` | Token for Edge browser detection. |
| `log.constants.user-agent.browser-chrome-token` | `chrome/` | Token for Chrome browser detection. |
| `log.constants.user-agent.browser-firefox-token` | `firefox/` | Token for Firefox browser detection. |
| `log.constants.user-agent.browser-safari-token` | `safari/` | Token for Safari browser detection. |
| `log.constants.user-agent.browser-ie-token` | `msie` | Token for legacy IE detection. |
| `log.constants.user-agent.browser-trident-token` | `trident/` | Token for Trident-based IE detection. |
| `log.constants.user-agent.browser-edge-name` | `Edge` | Browser name after Edge match. |
| `log.constants.user-agent.browser-chrome-name` | `Chrome` | Browser name after Chrome match. |
| `log.constants.user-agent.browser-firefox-name` | `Firefox` | Browser name after Firefox match. |
| `log.constants.user-agent.browser-safari-name` | `Safari` | Browser name after Safari match. |
| `log.constants.user-agent.browser-ie-name` | `IE` | Browser name after IE match. |
| `log.constants.user-agent.os-windows-token` | `windows` | Token for Windows detection. |
| `log.constants.user-agent.os-mac-token` | `mac os x` | Token for macOS detection. |
| `log.constants.user-agent.os-android-token` | `android` | Token for Android detection. |
| `log.constants.user-agent.os-iphone-token` | `iphone` | Token for iPhone detection. |
| `log.constants.user-agent.os-ipad-token` | `ipad` | Token for iPad detection. |
| `log.constants.user-agent.os-ios-token` | `ios` | Token for iOS detection. |
| `log.constants.user-agent.os-linux-token` | `linux` | Token for Linux detection. |
| `log.constants.user-agent.os-windows-name` | `Windows` | OS name after Windows match. |
| `log.constants.user-agent.os-mac-name` | `macOS` | OS name after macOS match. |
| `log.constants.user-agent.os-android-name` | `Android` | OS name after Android match. |
| `log.constants.user-agent.os-ios-name` | `iOS` | OS name after iOS match. |
| `log.constants.user-agent.os-linux-name` | `Linux` | OS name after Linux match. |
| `log.constants.user-agent.device-tablet-token` | `tablet` | Token for tablet detection. |
| `log.constants.user-agent.device-mobile-token` | `mobile` | Token for mobile-phone detection. |
| `log.constants.user-agent.device-tablet-name` | `Tablet` | Device type name after tablet match. |
| `log.constants.user-agent.device-mobile-name` | `Mobile` | Device type name after mobile match. |

### Common Constants Override (`common.constants`)

- Default values are centralized in `src/main/java/com/example/demo/common/config/CommonConstants.java`.
- To override defaults, define `common.constants.*` in config files. Unset keys keep class defaults.
- Recommendation: override only what you need; do not copy all keys blindly.

**Common Group**

| Key                                                                   | Default                          | Description                                                                                   |
|-----------------------------------------------------------------------|----------------------------------|-----------------------------------------------------------------------------------------------|
| `common.constants.http.json-content-type`                             | `application/json;charset=UTF-8` | `Content-Type` for JSON error responses in auth/permission/rate-limit/duplicate-submit flows. |
| `common.constants.http.forwarded-for-header`                          | `X-Forwarded-For`                | Primary proxy header for client IP extraction.                                                |
| `common.constants.http.real-ip-header`                                | `X-Real-IP`                      | Secondary proxy header for client IP extraction.                                              |
| `common.constants.http.multipart-prefix`                              | `multipart/`                     | `Content-Type` prefix used to identify multipart requests.                                    |
| `common.constants.http.idempotency-header-default`                    | `Idempotency-Key`                | Default idempotency header name when not explicitly configured.                               |
| `common.constants.trace.mdc-key`                                      | `traceId`                        | MDC key for request trace id.                                                                 |
| `common.constants.rate-limit.key-prefix`                              | `rl:`                            | Rate-limit cache key prefix.                                                                  |
| `common.constants.rate-limit.response-status`                         | `429`                            | HTTP status for rate-limit rejection.                                                         |
| `common.constants.rate-limit.message-key`                             | `common.rate.limit.exceeded`     | i18n message key for rate-limit rejection.                                                    |
| `common.constants.duplicate-submit.key-prefix`                        | `dup:`                           | Duplicate-submit cache key prefix.                                                            |
| `common.constants.duplicate-submit.response-status`                   | `409`                            | HTTP status for duplicate-submit rejection.                                                   |
| `common.constants.duplicate-submit.message-key`                       | `common.duplicate.submission`    | i18n message key for duplicate-submit rejection.                                              |
| `common.constants.duplicate-submit.key-idempotency-tag`               | `k`                              | Idempotency segment tag in duplicate-submit key composition.                                  |
| `common.constants.duplicate-submit.key-query-tag`                     | `q`                              | Query-string segment tag in duplicate-submit key composition.                                 |
| `common.constants.duplicate-submit.key-body-tag`                      | `b`                              | Body-hash segment tag in duplicate-submit key composition.                                    |
| `common.constants.permission.required-message-key`                    | `auth.permission.required`       | i18n message key when login is required.                                                      |
| `common.constants.permission.denied-message-key`                      | `auth.permission.denied`         | i18n message key when permission check fails.                                                 |
| `common.constants.mybatis.datasource-url-property`                    | `spring.datasource.url`          | Datasource URL property key used for DB type detection.                                       |
| `common.constants.mybatis.postgres-token`                             | `:postgresql:`                   | JDBC URL token for PostgreSQL detection.                                                      |
| `common.constants.mybatis.mysql-token`                                | `:mysql:`                        | JDBC URL token for MySQL detection.                                                           |
| `common.constants.mybatis.mariadb-token`                              | `:mariadb:`                      | JDBC URL token for MariaDB detection.                                                         |
| `common.constants.mybatis.oracle-token`                               | `:oracle:`                       | JDBC URL token for Oracle detection.                                                          |
| `common.constants.i18n.basename`                                      | `classpath:i18n/messages`        | i18n bundle basename.                                                                         |
| `common.constants.i18n.default-encoding`                              | `UTF-8`                          | i18n bundle default encoding.                                                                 |
| `common.constants.i18n.fallback-to-system-locale`                     | `false`                          | Whether i18n falls back to system locale.                                                     |
| `common.constants.i18n.use-code-as-default-message`                   | `true`                           | Whether message code itself is used when no translation exists.                               |
| `common.constants.i18n.default-locale-tag`                            | `zh-CN`                          | Default locale language tag.                                                                  |
| `common.constants.mdc.thread-name-prefix`                             | `mdc-thread-`                    | Default thread name prefix for MDC thread factory.                                            |
| `common.constants.cache.memory-cleanup-thread-prefix`                 | `cache-cleanup`                  | Memory-cache cleanup thread name prefix.                                                      |
| `common.constants.cache.db-cleanup-thread-prefix`                     | `cache-db-cleanup`               | DB-cache cleanup thread name prefix.                                                          |
| `common.constants.exception-handling.client-abort-window-millis`      | `60000`                          | Time window (ms) for tracking client disconnects.                                             |
| `common.constants.exception-handling.client-abort-warn-threshold`     | `20`                             | Emit a warning when disconnects reach this threshold within the window.                       |
| `common.constants.exception-handling.client-abort-message-max-length` | `200`                            | Max length of disconnect log message (<=0 means no truncation).                               |

### Auth Runtime Settings (`auth.password`)

- Controls password transport/strength plus first-login and expiration policies.
- Defaults are defined in `src/main/java/com/example/demo/auth/config/AuthProperties.java` and can be overridden via
  `auth.password.*`.

| Key                                         | Default | Description                                                                                                                                                    |
|---------------------------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `auth.password.force-change-on-first-login` | `true`  | Enables mandatory password change on first login. When enabled, newly created users and admin-reset users must change password before accessing business APIs. |
| `auth.password.expire-days`                 | `120`   | Password expiration window in days. `<=0` disables expiration policy; values `>0` require password update after the configured age.                            |

### Auth Constants Override (`auth.constants`)

- Default values are centralized in `src/main/java/com/example/demo/auth/config/AuthConstants.java`.
- To override defaults, define `auth.constants.*` in config files. Unset keys keep class defaults.
- Recommendation: override only what you need; do not copy all keys blindly.

**Auth Token / Filter**

| Key                                                          | Default                         | Description                                                          |
|--------------------------------------------------------------|---------------------------------|----------------------------------------------------------------------|
| `auth.constants.token.authorization-header`                  | `Authorization`                 | Primary auth token request header.                                   |
| `auth.constants.token.fallback-token-header`                 | `X-Auth-Token`                  | Fallback auth token request header.                                  |
| `auth.constants.token.query-token-parameter`                 | `token`                         | Query parameter name used for token passing.                         |
| `auth.constants.token.bearer-prefix`                         | `Bearer `                       | Bearer prefix (including trailing space).                            |
| `auth.constants.token.token-type`                            | `Bearer`                        | `tokenType` in login response payload.                               |
| `auth.constants.token.jwt-header-alg-key`                    | `alg`                           | JWT header algorithm key name.                                       |
| `auth.constants.token.jwt-header-type-key`                   | `typ`                           | JWT header type key name.                                            |
| `auth.constants.token.jwt-header-alg-value`                  | `HS256`                         | JWT header algorithm value.                                          |
| `auth.constants.token.jwt-header-type-value`                 | `JWT`                           | JWT header type value.                                               |
| `auth.constants.token.jwt-claim-subject`                     | `sub`                           | JWT subject claim key.                                               |
| `auth.constants.token.jwt-claim-user-id`                     | `uid`                           | JWT user-id claim key.                                               |
| `auth.constants.token.jwt-claim-issued-at`                   | `iat`                           | JWT issued-at claim key.                                             |
| `auth.constants.token.jwt-claim-expires-at`                  | `exp`                           | JWT expires-at claim key.                                            |
| `auth.constants.token.jwt-claim-jwt-id`                      | `jti`                           | JWT id claim key.                                                    |
| `auth.constants.token.sign-algorithm`                        | `HmacSHA256`                    | JWT signing algorithm.                                               |
| `auth.constants.token.store-key-prefix`                      | `auth:token:`                   | TokenStore cache key prefix.                                         |
| `auth.constants.filter.options-method`                       | `OPTIONS`                       | Method bypassed by `AuthTokenFilter` (preflight).                    |
| `auth.constants.filter.token-missing-message-key`            | `auth.token.missing`            | i18n key for missing token.                                          |
| `auth.constants.filter.token-invalid-message-key`            | `auth.token.invalid`            | i18n key for invalid token.                                          |
| `auth.constants.filter.user-invalid-message-key`             | `auth.user.invalid`             | i18n key for invalid user payload.                                   |
| `auth.constants.filter.user-not-found-message-key`           | `auth.user.not.found`           | i18n key for non-existing user.                                      |
| `auth.constants.filter.user-disabled-message-key`            | `auth.user.disabled`            | i18n key for disabled user.                                          |
| `auth.constants.filter.password-change-required-message-key` | `auth.password.change.required` | i18n key returned when password must be changed first.               |
| `auth.constants.filter.password-change-profile-path`         | `/auth/profile`                 | Profile path allowed during forced password-change state.            |
| `auth.constants.filter.password-change-logout-path`          | `/auth/logout`                  | Logout path allowed during forced password-change state.             |
| `auth.constants.filter.get-method`                           | `GET`                           | GET method token used by forced password-change allowlist matching.  |
| `auth.constants.filter.put-method`                           | `PUT`                           | PUT method token used by forced password-change allowlist matching.  |
| `auth.constants.filter.post-method`                          | `POST`                          | POST method token used by forced password-change allowlist matching. |

**Auth Captcha / Login Attempt**

| Key | Default | Description |
|---|---|---|
| `auth.constants.captcha.image-prefix` | `data:image/png;base64,` | Data URL prefix for captcha image payload. |
| `auth.constants.captcha.code-charset` | `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` | Captcha character set. |
| `auth.constants.captcha.png-format` | `png` | Captcha image output format. |
| `auth.constants.captcha.fallback-font-family` | `SansSerif` | Fallback font family when embedded fonts are unavailable. |
| `auth.constants.captcha.noise-dot-min-count` | `20` | Minimum number of captcha noise dots. |
| `auth.constants.captcha.noise-dot-density-divisor` | `150` | Dot-density divisor (`width*height/divisor`). |
| `auth.constants.captcha.font-min-size` | `18` | Minimum captcha font size. |
| `auth.constants.captcha.font-padding` | `10` | Vertical font padding used in captcha rendering. |
| `auth.constants.captcha.char-color-min` | `30` | Minimum random color value for captcha chars. |
| `auth.constants.captcha.char-color-max` | `160` | Maximum random color value for captcha chars. |
| `auth.constants.captcha.line-dot-color-min` | `120` | Minimum random color value for noise lines/dots. |
| `auth.constants.captcha.line-dot-color-max` | `200` | Maximum random color value for noise lines/dots. |
| `auth.constants.captcha.font-resource-classpath-prefix` | `classpath:` | Classpath prefix used by captcha font resources. |
| `auth.constants.captcha.store-key-prefix` | `auth:captcha:` | CaptchaStore cache key prefix. |
| `auth.constants.login-attempt.fail-key-prefix` | `auth:login:fail:` | Login-failure counter cache key prefix. |
| `auth.constants.login-attempt.lock-key-prefix` | `auth:login:lock:` | Login-lock cache key prefix. |
| `auth.constants.login-attempt.mode-ip` | `ip` | Mode token for IP-based login-limit identity. |
| `auth.constants.login-attempt.mode-ip-user` | `ip-user` | Mode token for IP+user login-limit identity. |
| `auth.constants.login-attempt.mode-user-ip` | `user-ip` | Mode token for user+IP login-limit identity. |
| `auth.constants.login-attempt.mode-fallback` | `user` | Default login-limit identity mode. |

**Auth Password / Controller**

| Key | Default | Description |
|---|---|---|
| `auth.constants.password.mode-fallback` | `plain` | Fallback password mode. |
| `auth.constants.password.mode-bcrypt` | `bcrypt` | Mode token for bcrypt password handling. |
| `auth.constants.password.mode-sm3` | `sm3` | Mode token for sm3 password handling. |
| `auth.constants.password.transport-mode-aes` | `aes` | Mode token for AES transport decryption. |
| `auth.constants.password.transport-mode-aes-gcm` | `aes-gcm` | Mode token for AES-GCM transport decryption. |
| `auth.constants.password.transport-mode-base64` | `base64` | Mode token for Base64 transport decoding. |
| `auth.constants.password.transport-mode-sm2` | `sm2` | Mode token for SM2 transport decryption. |
| `auth.constants.password.transport-split-delimiter` | `:` | Delimiter for AES transport payload (`iv:cipher`). |
| `auth.constants.password.transport-split-limit` | `2` | Split limit for AES transport payload parsing. |
| `auth.constants.password.aes-key-algorithm` | `AES` | AES key algorithm name. |
| `auth.constants.password.aes-transformation` | `AES/GCM/NoPadding` | AES transformation string. |
| `auth.constants.password.aes-gcm-tag-length-bits` | `128` | AES-GCM tag length (bits). |
| `auth.constants.profile.new-password-min-length` | `6` | Minimum new password length in profile update API. |
| `auth.constants.profile.user-agent-header` | `User-Agent` | Request header used to capture user agent in login logs. |
| `auth.constants.login-log.type-login` | `1` | Login log type code for login events. |
| `auth.constants.login-log.type-logout` | `2` | Login log type code for logout events. |
| `auth.constants.login-log.status-fail` | `0` | Login log status code for failure. |
| `auth.constants.login-log.status-success` | `1` | Login log status code for success. |
| `auth.constants.controller.bad-request-code` | `400` | HTTP code for bad request in auth controller flows. |
| `auth.constants.controller.unauthorized-code` | `401` | HTTP code for unauthorized in auth flows. |
| `auth.constants.controller.forbidden-code` | `403` | HTTP code for forbidden in auth filter flows. |
| `auth.constants.controller.not-found-code` | `404` | HTTP code for user/resource not found in auth flows. |
| `auth.constants.controller.too-many-requests-code` | `429` | HTTP code for lock/rate-limit in auth flows. |
| `auth.constants.controller.internal-server-error-code` | `500` | HTTP code for internal auth/profile update failure. |

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

- Public endpoints should be added to `security.common.exclude-paths`; for auth- or permission-only cases,
  use `auth.filter.additional-exclude-paths` and `security.permission.additional-exclude-paths`.
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
- Single-datasource example profile: `app/src/main/resources/application-single-datasource.yml` (recommended with
  `dev,single-datasource`).
- Three-mode example file: `app/src/main/resources/application-datasource-modes.example.yml`.
- Datasource mode key: `app.datasource.mode` (`multi-datasource` / `single-datasource-multi-schema` /
  `single-datasource-single-schema`).
- DB scripts:
  - Multi-module datasource: `sql/mysql-multi-datasource.sql`, `sql/postgresql-multi-datasource.sql`
  - Single datasource + multi schema: `sql/mysql-single-datasource-multi-schema.sql`,
    `sql/postgresql-single-datasource-multi-schema.sql`
  - Single datasource + single schema: `sql/mysql-single-datasource-single-schema.sql`,
    `sql/postgresql-single-datasource-single-schema.sql`
- Druid monitor: `spring.datasource.druid.stat-view-servlet.*`.
