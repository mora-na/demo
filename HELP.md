# 项目帮助（中文）

本文档聚焦“配置与运维细节”，与 `README.md`（快速上手与整体介绍）互补。

## 认证与登录

- 令牌使用：仅支持 `Authorization: Bearer <token>` 或 `X-Auth-Token`。
- JWT 配置：
    - `auth.jwt.secret` / `auth.jwt.ttl-seconds`
- 密码策略：
    - `auth.password.mode`（plain/md5/bcrypt/sm3）
    - `auth.password.transport-mode`（plain/base64/aes-gcm/sm2）
    - `auth.password.transport-key`（AES-GCM Base64 密钥）
    - `auth.password.strong-check-enabled`
- 登录失败限制（滑动窗口）：
    - `auth.login-limit.enabled`
    - `auth.login-limit.max-errors`
    - `auth.login-limit.window-seconds`（统计窗口）
    - `auth.login-limit.lock-seconds`（锁定时长）
    - `auth.login-limit.key-mode`（user/ip/ip-user）
      说明：每次失败刷新窗口 TTL，达到阈值后锁定。

## 验证码

- 生成与校验基于 Redis。
- 主要配置：
    - `auth.captcha.width` / `auth.captcha.height`
    - `auth.captcha.code-length`
    - `auth.captcha.thickness`
    - `auth.captcha.rotate-min` / `auth.captcha.rotate-max`
    - `auth.captcha.shear-x-min` / `auth.captcha.shear-x-max`
    - `auth.captcha.shear-y-min` / `auth.captcha.shear-y-max`
    - `auth.captcha.font-resources`
    - `auth.captcha.expire-seconds`

## 权限与菜单

- 权限校验由权限拦截器控制：`security.permission.*`
- 菜单权限通过 `sys_menu.permission` 汇入权限判断。

## 定时任务（Quartz）

- 持久化调度配置：`spring.quartz.*`
- 任务处理器需要实现 `JobHandler`，并作为 Spring Bean 注册。
- 执行记录存储于 `sys_job_log`，详情日志使用 `log_detail` 字段。

## 执行日志自动收集（Job Log Collect）

- 开关与范围：
    - `job.log.collect.enabled`
    - `job.log.collect.scope`（MDC | THREAD）
- 收集级别与长度：
    - `job.log.collect.min-level`
    - `job.log.collect.max-length`
- 异步合并：
    - `job.log.collect.merge-delay-millis`
    - `job.log.collect.max-hold-millis`
- 线程上下文兜底：
    - `job.log.collect.inherit-thread-context`
- 说明：
    - 默认按 MDC 识别执行上下文，并自动收集同线程日志。
    - `@Async` 会自动透传 MDC。
    - 自建线程池建议使用 `MdcUtils.wrapExecutorService`，保证稳定收集。

## 通知（SSE 推送）

- 推送配置：`notice.sse.*`
    - 心跳频率：`notice.sse.heartbeat-interval-millis`
    - 断线判定：`notice.sse.heartbeat-timeout-millis`
    - 最新列表推送条数：`notice.sse.latest-limit`

## 许可证与合规

已生成依赖许可证报告（见 `target/`）：

- 后端（Maven）：`target/site/aggregate-third-party-report.html`
- 前端（node_modules 扫描）：`target/licenses/frontend-licenses.json`
- 前端汇总：`target/licenses/frontend-licenses-summary.txt`

重新生成：

```bash
./mvnw -q -DskipTests org.codehaus.mojo:license-maven-plugin:2.4.0:aggregate-third-party-report
python3 scripts/licenses_scan_frontend.py  # 或使用你自己的扫描脚本
```

风险提示（仅作初步提醒）：

- 后端报告中包含 **EPL 2.0 / LGPL 2.1 / GPL2 w/ CPE** 等许可条目，请在商用发布前做法务确认。
- 前端依赖以 MIT/ISC/BSD/Apache 为主，风险相对较低，但仍建议以报告为准。

## 数据范围

- 开关与默认范围：`security.data-scope.enabled` / `security.data-scope.default-type`
- 表字段映射：`sys_data_scope_rule`（如映射 `dept_id`）
- 详细说明文档：`docs/DATA_SCOPE.md`

## 安全防护

- SQL 防护：`security.sql-guard.*`
- XSS 过滤：`security.xss.*`
- 限流：`security.rate-limit.*`
- 重复提交：`security.duplicate-submit.*`
- 排除路径会与 `security.common.exclude-paths` 合并。

## 配置加密（Jasypt）

- 使用 `ENC(...)` 包裹敏感配置。
- 启动时通过 `JASYPT_ENCRYPTOR_PASSWORD` 提供加解密口令。

## 运行与环境

- 配置入口：`src/main/resources/application.yml` + `application-dev.yml`
- 默认 profile：`dev`（可用 `SPRING_PROFILES_ACTIVE` 覆盖）
- 数据库脚本：`sql/mysql.sql`、`sql/postgresql.sql`
- Druid 监控：`spring.datasource.druid.stat-view-servlet.*`

## 常见问题

- 登录失败次数过多：检查 `auth.login-limit.*` 与 Redis 状态。
- 验证码无效：确认 Redis 可用与验证码是否过期。
- Token 无效/过期：检查 `auth.jwt.ttl-seconds` 与时钟偏差。

## 常用命令

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw -DskipTests package
```
