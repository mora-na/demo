# Project Help (English)

## Overview

This Spring Boot backend demo includes authentication, authorization, data-scope controls, organization departments, and menu permissions, plus several built-in security protections and utilities.

## Key Features

- Auth & authorization: JWT, captcha login, permission interception (roles/permissions/menus).
- Password security: AES-GCM password transport, bcrypt storage (one-way).
- Org & access model: user → dept → role → permission → menu.
- Data scope: role-based visibility (ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE).
- Security protections: SQL guard, XSS filter, rate limiting, duplicate-submit prevention.
- Excel import/export for user data.
- Optional encrypted configs via `ENC(...)` (Jasypt SM4).

## Notable Configs

- Auth & password
  - `auth.jwt.secret` / `auth.jwt.ttl-seconds`
  - `auth.password.mode` (default `bcrypt`)
  - `auth.password.transport-mode` (`plain`/`base64`/`aes-gcm`)
  - `auth.password.transport-key` (AES-GCM Base64 key)
  - `auth.password.default-password`
  - `auth.password.strong-check-enabled`
  - `auth.password.strong-min-length` / `auth.password.strong-pattern`

- Permissions & menus
  - Schemas in `SQL/mysql.sql` and `SQL/postgresql.sql`
  - Menu permissions from `sys_menu.permission` are included in permission checks

- Data scope
  - `security.data-scope.enabled` / `security.data-scope.default-type`
  - `sys_data_scope_rule` defines the table → column mapping for filtering

- Protections
  - `security.sql-guard.*`
  - `security.xss.*`
  - `security.rate-limit.*`
  - `security.duplicate-submit.*`

- Connections & monitoring
  - Druid monitor login: `spring.datasource.druid.stat-view-servlet.*`
  - Camunda admin user: `camunda.bpm.admin-user.*`
  - Jasypt encryptor: `jasypt.encryptor.bean` / `jasypt.encryptor.password`

## Notes

- Tokens are accepted via `Authorization: Bearer <token>` or `X-Auth-Token` only; `?token=` is not supported.
- `bcrypt` is one-way; DB passwords cannot be decrypted.
- Replace default passwords and avoid plaintext secrets in production.
- If `auth.password.transport-mode=aes-gcm`, `auth.password.transport-key` is required.
- Data scope filtering needs `sys_data_scope_rule` entries (e.g., map to `dept_id`).

## Common Commands

```bash
./mvnw test
./mvnw spring-boot:run
```
