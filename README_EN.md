# Demo System (Spring Boot)

A full-stack starter system focused on authentication, system management, scheduled jobs, and notifications. Designed as a solid baseline for admin/back‑office projects.

## Highlights

- Auth & authorization: JWT, captcha login, permission interception (roles/permissions/menus).
- Login safety: failure limits with a sliding window and lock policy.
- Password security: optional AES-GCM transport, stored with bcrypt/md5/sm3 (configurable).
- Data scope: role-based visibility (ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE).
- Protections: SQL guard, XSS filter, rate limiting, duplicate-submit prevention.
- Excel import/export for user data.
- Scheduled jobs: Quartz persistence, handler management, execution logs with detail view.
- System notices: publish & read states, SSE pushes unread counts and latest items.
- Cache strategies: Redis / in-memory / database cache options.

## Tech Stack

### Backend
- Spring Boot 2.7.x (current 2.7.12)
- MyBatis-Plus + PageHelper
- Quartz (JDBC persistence)
- Redis + Caffeine (multi-level cache)
- Druid + Dynamic Datasource
- Jasypt (config encryption)
- Logback (logging)
- PostgreSQL / MySQL (see `sql/`)

### Frontend (`demo-ui`)
- Vue 3 + Vite + TypeScript
- Pinia + Axios
- Element Plus
- vue-i18n
- lucide-vue-next (icons)

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

To start the frontend:

```bash
cd demo-ui
npm install
npm run dev
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
