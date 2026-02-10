# 项目帮助（中文）

## 项目概览

本项目为 Spring Boot 后端示例系统，内置认证、权限、数据范围控制、组织部门与菜单权限等能力，并提供若干安全防护与通用功能。

## 特色功能

- 认证与鉴权：JWT 认证、验证码登录、权限拦截（角色/权限/菜单）。
- 密码安全：传输层可用 AES-GCM 密文，持久化使用 bcrypt（不可逆）。
- 组织与权限模型：用户-部门-角色-权限-菜单全链路模型。
- 数据范围控制：按角色配置可见范围（ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE）。
- 防护能力：SQL 防护、XSS 过滤、限流、重复提交防护。
- Excel 导入导出：用户数据批量导入导出示例。
- 可选配置加密：支持 `ENC(...)` 配置密文（Jasypt SM4）。

## 特色配置（关键项）

- 认证与密码
  - `auth.jwt.secret` / `auth.jwt.ttl-seconds`
  - `auth.password.mode`（默认 `bcrypt`）
  - `auth.password.transport-mode`（`plain`/`base64`/`aes-gcm`）
  - `auth.password.transport-key`（AES-GCM Base64 密钥）
  - `auth.password.default-password`（默认密码）
  - `auth.password.strong-check-enabled`（强密码校验开关）
  - `auth.password.strong-min-length` / `auth.password.strong-pattern`

- 权限与菜单
  - 角色/权限/菜单数据表见 `SQL/mysql.sql`、`SQL/postgresql.sql`
  - 菜单权限通过 `sys_menu.permission` 汇入权限判断

- 数据范围
  - `security.data-scope.enabled` / `security.data-scope.default-type`
  - `sys_data_scope_rule` 定义“表 → 数据范围字段”映射

- 防护配置
  - `security.sql-guard.*`
  - `security.xss.*`
  - `security.rate-limit.*`
  - `security.duplicate-submit.*`

- 连接与监控
  - Druid 监控登录配置：`spring.datasource.druid.stat-view-servlet.*`
  - Camunda 管理员：`camunda.bpm.admin-user.*`
  - Jasypt 加解密器：`jasypt.encryptor.bean` / `jasypt.encryptor.password`

## 注意事项

- 登录后只支持 `Authorization: Bearer <token>` 或 `X-Auth-Token`，不再支持 `?token=`。
- `bcrypt` 为不可逆哈希，数据库内密码不可解密。
- 生产环境务必替换默认密码、Druid/Camunda 默认账号密码，并避免明文配置。
- `auth.password.transport-mode=aes-gcm` 时必须提供 `auth.password.transport-key`。
- 数据范围要生效，需在 `sys_data_scope_rule` 中配置目标表与字段（如 `dept_id`）。

## 常用命令

```bash
./mvnw test
./mvnw spring-boot:run
```
