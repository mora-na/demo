# Demo Backend (Spring Boot)

A Spring Boot backend demo with authentication, authorization, data-scope controls, organization/department modeling,
and menu permissions, plus built-in security protections and utilities.

## Highlights

- Auth & authorization: JWT, captcha login, permission interception (roles/permissions/menus).
- Login safety: failure limits with a sliding window and lock policy.
- Password security: optional AES-GCM transport, stored with bcrypt/md5/sm3 (configurable).
- Data scope: role-based visibility (ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE).
- Protections: SQL guard, XSS filter, rate limiting, duplicate-submit prevention.
- Excel import/export for user data.

## Tech Stack

- Spring Boot 2.7.x
- MyBatis-Plus + PageHelper
- Redis (captcha, token, rate limit, etc.)
- PostgreSQL/MySQL (see scripts in `sql/`)

## Quick Start

1. Prepare: JDK 8+, Redis, and a database (PostgreSQL or MySQL)
2. Initialize DB: run the scripts in `sql/`
3. Configure `src/main/resources/application-dev.yml`
    - DB connection and credentials
    - Redis connection
    - Auth/password policy
4. Run:

```bash
./mvnw spring-boot:run
```

The `dev` profile is active by default. Override it with:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

## Structure

- `src/main/java` backend source
- `src/main/resources` configs and i18n
- `sql/` database scripts
- `demo-ui/` frontend project (if needed)

## Docs

- Detailed configuration and operations: `HELP.md`
- English help: `HELP_EN.md`

## Common Commands

```bash
./mvnw test
./mvnw spring-boot:run
```
