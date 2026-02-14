# Demo 系统（Spring Boot）

一个前后端分离的示例系统，聚焦“认证鉴权 + 系统管理 + 定时任务 + 通知推送 + 数据权限”等通用能力，适合作为中后台项目的起点。

## 总览

- 面向中后台的通用系统能力，强调可配置、可扩展与可运维。
- 后端以 Spring Boot + MyBatis-Plus 为核心，前端基于 Vue 3 + Element Plus。
- 以清晰的权限体系、数据范围与日志审计为基础，覆盖常见管理场景。

## 功能亮点

- 认证与鉴权：JWT、验证码登录、权限拦截（角色/权限/菜单）。
- 登录安全：登录失败次数限制（滑动窗口）与锁定策略。
- 密码安全：传输层可选 AES-GCM/SM2，持久化支持 bcrypt/md5/sm3（可配置）。
- 数据范围控制：按角色配置可见范围（ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE）。
- 安全防护：SQL 防护、XSS 过滤、限流、重复提交防护。
- Excel 导入导出：用户数据批量导入导出示例。
- 定时任务：Quartz 持久化调度、任务处理器管理、执行日志与详情查看。
- 系统通知：支持发布与阅读状态，SSE 实时推送未读数与列表。
- 字典管理：支持字典类型/数据项维护，前后端缓存，接口返回可自动翻译字典标签。
- 缓存体系：Redis / 本地内存 / 数据库多策略缓存配置。

## 技术栈

### 后端

- Spring Boot 2.7.x（当前 2.7.12）
- MyBatis-Plus
- Quartz（JDBC 持久化）
- Redis + Caffeine（多级缓存）
- Druid + Dynamic Datasource
- Jasypt（配置加密）
- Logback（日志）
- PostgreSQL / MySQL（见 `sql/`）

### 前端（`demo-ui`）

- Vue 3 + Vite + TypeScript
- Pinia + Axios
- Element Plus
- vue-i18n
- lucide-vue-next（图标）

## 快速开始

1. 准备环境：JDK 8+、Redis、数据库（PostgreSQL 或 MySQL）
2. 初始化数据库：执行 `sql/` 中对应脚本
3. 配置 `src/main/resources/application-dev.yml`
4. 运行服务：

```bash
./mvnw spring-boot:run
```

默认启用 `dev` 配置，可通过环境变量切换：

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

如需启动前端：

```bash
cd demo-ui
npm install
npm run dev
```

## 目录结构

- `src/main/java` 后端源码
- `src/main/resources` 配置与国际化资源
- `sql/` 数据库建表脚本
- `demo-ui/` 前端工程（如需）

## 使用说明与配置

### 认证与登录

- Token 头仅支持 `Authorization: Bearer <token>` 或 `X-Auth-Token`。
- JWT 配置：`auth.jwt.secret`、`auth.jwt.ttl-seconds`。
- 密码策略：`auth.password.mode`、`auth.password.transport-mode`、`auth.password.transport-key`。
- 登录失败限制（滑动窗口）：`auth.login-limit.enabled`、`auth.login-limit.max-errors`、`auth.login-limit.window-seconds`、`auth.login-limit.lock-seconds`、`auth.login-limit.key-mode`。

### 验证码

- Redis 生成与校验。
- 配置：`auth.captcha.*`。

### 权限与菜单

- 权限校验由 `security.permission.*` 控制。
- 菜单权限来源于 `sys_menu.permission`。

### 数据范围

- 开关与默认范围：`security.data-scope.enabled`、`security.data-scope.default-type`。
- 映射规则表：`sys_data_scope_rule`。

### 字典管理（Dict）

- 表结构：`sys_dict_type` 与 `sys_dict_data`，通过 `dict_type` 关联。
- 公开接口（登录用户可用）：
  - `GET /api/dict/data/{dictType}`
  - `GET /api/dict/data/batch?types=a,b`
  - `GET /api/dict/data/all`
- 管理接口（仅管理员，权限 `dict:*`）：
  - `GET /api/sys/dict/type/list`
  - `POST /api/sys/dict/type`
  - `PUT /api/sys/dict/type/{id}`
  - `DELETE /api/sys/dict/type/{id}`
  - `GET /api/sys/dict/data/list`
  - `POST /api/sys/dict/data`
  - `PUT /api/sys/dict/data/{id}`
  - `DELETE /api/sys/dict/data/{id}`
  - `DELETE /api/sys/dict/cache/refresh`
- 缓存配置：`dict.cache.seconds`（<=0 表示不缓存）。
- 后端翻译：VO 字段标注 `@DictLabel("sys_gender")` 自动追加 `xxxLabel` 字段。

### 通知（SSE 推送）

- 配置：`notice.sse.*`。

### 定时任务（Quartz）

- 持久化调度配置：`spring.quartz.*`。
- 任务处理器需实现 `JobHandler` 并注册为 Spring Bean。
- 记录存储：`sys_job_log`，详情日志使用 `log_detail` 字段。

### 执行日志自动收集（Job Log Collect）

- 开关与范围：`job.log.collect.enabled`、`job.log.collect.scope`。
- 级别与长度：`job.log.collect.min-level`、`job.log.collect.max-length`。
- 异步合并：`job.log.collect.merge-delay-millis`、`job.log.collect.max-hold-millis`。
- 线程上下文兜底：`job.log.collect.inherit-thread-context`。

### 安全防护

- SQL 防护：`security.sql-guard.*`。
- XSS 过滤：`security.xss.*`。
- 限流：`security.rate-limit.*`。
- 重复提交：`security.duplicate-submit.*`。
- 排除路径会与 `security.common.exclude-paths` 合并。

### 过滤器与拦截器总览

- `TraceIdFilter`：写入 `traceId` 到 MDC。通用型。
- `AuthTokenFilter`：认证过滤器，校验 Token 并写入 `AuthContext`。白名单式。
- `PermissionInterceptor`：权限拦截器，校验 `@RequireLogin` / `@RequirePermission`。白名单式。
- `RateLimitFilter`：限流。白名单式。
- `DuplicateSubmitFilter`：重复提交防护，支持 `Idempotency-Key`。白名单式。
- `XssFilter`：请求参数 XSS 转义。白名单式。
- `XssRequestBodyAdvice`：请求体 XSS 转义。白名单式。
- `DataScopeAspect`：解析 `@DataScope` 并写入上下文。通用型。
- `DataScopeInnerInterceptor`：SQL 注入数据权限条件。通用型。
- `PaginationInnerInterceptor`：分页。通用型。
- `OptimisticLockerInnerInterceptor`：乐观锁。通用型。
- `SqlGuardInnerInterceptor`：阻断多语句与全表 UPDATE/DELETE。黑名单式。

业务影响提示：

- 匿名接口需加入 `auth.filter.exclude-paths` 与 `security.permission.exclude-paths`。
- 高频接口可能触发 429，需要调整限流或加入排除路径。
- 写操作短时间重复会被拒绝，可用 `Idempotency-Key`。
- 富文本需从 XSS 过滤中排除或业务层处理。
- 数据权限查询需使用 `@DataScope` 并正确设置 `deptAlias` / `userAlias`。
- 全表 UPDATE/DELETE 会被 SQL 防护拦截。

### 配置加密（Jasypt）

- 使用 `ENC(...)` 包裹敏感配置。
- 启动时通过 `JASYPT_ENCRYPTOR_PASSWORD` 提供口令。

### 运行与环境

- 主配置：`src/main/resources/application.yml` 与 `application-dev.yml`。
- 默认 profile：`dev`，可用 `SPRING_PROFILES_ACTIVE` 覆盖。
- 数据库脚本：`sql/mysql.sql`、`sql/postgresql.sql`。
- Druid 监控：`spring.datasource.druid.stat-view-servlet.*`。

### 许可证与合规

- Maven 报告：`target/site/aggregate-third-party-report.html`。
- 前端扫描：`target/licenses/frontend-licenses.json`。
- 汇总文件：`target/licenses/frontend-licenses-summary.txt`。

重新生成：

```bash
./mvnw -q -DskipTests org.codehaus.mojo:license-maven-plugin:2.4.0:aggregate-third-party-report
python3 scripts/licenses_scan_frontend.py
```

### 常见问题

- 登录失败次数过多：检查 `auth.login-limit.*` 与 Redis。
- 验证码无效：确认 Redis 可用与验证码是否过期。
- Token 无效/过期：检查 `auth.jwt.ttl-seconds` 与时钟偏差。

## 常用命令

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw -DskipTests package
```
