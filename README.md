# Demo 系统（Spring Boot）

一个前后端分离的示例系统，聚焦“认证鉴权 + 系统管理 + 定时任务 + 通知推送 + 数据权限”等通用能力，适合作为中后台项目的起点。

## 总览

- 面向中后台的通用系统能力，强调可配置、可扩展与可运维。
- 后端以 Spring Boot + MyBatis-Plus 为核心，前端基于 Vue 3 + Element Plus。
- 以清晰的权限体系、数据范围与日志审计为基础，覆盖常见管理场景。

## 功能亮点

- 认证与鉴权：JWT、验证码登录、权限拦截（角色/权限/菜单）。
- 登录安全：登录失败次数限制（滑动窗口）与锁定策略。
- 会话安全：支持退出登录后服务端主动撤销 Token（无需等待 JWT 自然过期）。
- 邮件安全能力：支持异地/设备变更登录告警、敏感操作邮箱二次确认（验证码 + 短期票据）。
- 密码安全：传输层可选 AES-GCM/SM2，持久化支持 bcrypt/md5/sm3（可配置），并支持首次登录强制改密与密码过期策略。
- 统一接口契约：后端统一 `CommonResult` 返回结构，前端统一按 `code/message/data` 处理。
- 常量治理：模块魔法值收敛到各模块 `*Constants`，支持默认值与配置覆盖。
- 数据范围控制：按角色配置可见范围（ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE）。
- 安全防护：SQL 防护、XSS 过滤、限流、重复提交防护。
- Excel 导入导出：用户数据批量导入导出示例。
- 定时任务：Quartz 持久化调度、任务处理器管理、执行日志与详情查看。
- 系统通知：支持发布与阅读状态，SSE 实时推送未读数与列表。
- 字典管理：支持字典类型/数据项维护，前后端缓存，接口返回可自动翻译字典标签。
- 缓存体系：Redis / 本地内存 / 数据库多策略缓存配置。
- 前端路由与权限：采用 Vue Router（History 模式）嵌套路由，菜单点击路由跳转并保留权限控制。
- 国际化：后端 i18n 消息与前端 `vue-i18n` 配合，支持中英文文案扩展。

## 技术栈

### 后端

- Spring Boot 2.7.x（当前 2.7.12）
- MyBatis-Plus
- Spring Validation
- Quartz（JDBC 持久化）
- Spring Mail（SMTP）
- Redis + Caffeine（多级缓存）
- Druid + Dynamic Datasource
- Spring Security Crypto + BouncyCastle（密码与国密能力）
- Jasypt（配置加密）
- Logback（日志）
- PostgreSQL / MySQL（见 `sql/`）

### 前端（`demo-ui`）

- Vue 3 + Vite + TypeScript
- Vue Router（History）
- Pinia + Axios
- Element Plus
- vue-i18n
- sm-crypto（SM2/SM3 前端能力）
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

## 模块能力地图

| 模块           | 主要职责    | 关键能力                               |
|--------------|---------|------------------------------------|
| `auth`       | 认证与会话   | 验证码登录、JWT 签发与校验、登出撤销、登录安全策略、邮箱二次确认 |
| `user`       | 用户管理    | 用户资料、状态维护、密码修改、用户级数据范围覆盖           |
| `dept`       | 组织架构    | 部门树维护、上下级关系、数据归属基础                 |
| `menu`       | 菜单管理    | 菜单树、路由菜单数据、菜单权限标识                  |
| `permission` | 角色与权限   | 角色授权、权限点控制、接口访问控制                  |
| `datascope`  | 数据权限    | 角色/菜单/用户三级数据范围合并、SQL 数据过滤          |
| `dict`       | 字典管理    | 字典类型与数据维护、缓存、字典标签映射                |
| `notice`     | 通知中心    | 通知发布与阅读状态、SSE 实时推送                 |
| `job`        | 任务调度    | Quartz 任务管理、执行日志与结果追踪              |
| `log`        | 日志审计    | 登录日志、操作日志、审计事件落库                   |
| `post`       | 岗位管理    | 岗位信息维护、用户岗位关联                      |
| `order`      | 业务示例    | 订单示例模块（用于展示业务层 CRUD + 权限/数据范围接入）   |
| `common`     | 公共基础设施  | 统一返回、异常处理、缓存抽象、邮件发送抽象、工具与通用配置      |
| `ai`         | AI 扩展入口 | AI 相关能力接入与扩展（按业务启用）                |

## 使用说明与配置

### 认证与登录

- Token 头仅支持 `Authorization: Bearer <token>` 或 `X-Auth-Token`。
- JWT 配置：`auth.jwt.secret`、`auth.jwt.ttl-seconds`。
- 密码策略：`auth.password.mode`、`auth.password.transport-mode`、`auth.password.transport-key`。
- 密码治理：
    - 首次登录强制改密：`auth.password.force-change-on-first-login`（默认 `true`）。
    - 密码过期天数：`auth.password.expire-days`（默认 `120`，`<=0` 表示关闭过期策略）。
- 登录失败限制（滑动窗口）：`auth.login-limit.enabled`、`auth.login-limit.max-errors`、`auth.login-limit.window-seconds`、`auth.login-limit.lock-seconds`、`auth.login-limit.key-mode`。
- 安全增强：
    - 登录异常告警：`auth.security.login-anomaly.*`
    - 敏感操作邮箱二次确认：`auth.security.operation-confirm.*`
    - 发送接口：`POST /auth/security/operation-confirm/send`
    - 校验接口：`POST /auth/security/operation-confirm/verify`
- 登出失效：
    - 接口：`POST /auth/logout`
    - 行为：服务端撤销当前 Token，使其立即失效。

### 统一返回与常量治理

- 统一返回：除下载流等特殊场景外，后端接口统一返回 `CommonResult`，便于前端统一错误处理与消息展示。
- 常量治理：模块内部魔法值已收敛到各自 `*Constants`，通过 `@ConfigurationProperties` 支持配置覆盖。
- 详细可覆盖项与默认值见：`docs/CONFIGURATION.md`。

### 性能优化要点

- 数据权限计算前移：登录成功时预加载 `deptTreeIds`、`roleDataScopes`、`userScopeOverrides`
  到认证上下文，查询阶段直接基于上下文计算最终范围，避免每次查询重复装配数据权限画像。
- 字典翻译批量化：响应序列化前会批量收集 `@DictLabel` 需要的字典类型并预取，翻译阶段优先使用请求级上下文映射，减少逐条翻译带来的重复查询与重复遍历。
- SSE 心跳保活：通知流会按固定间隔推送心跳事件，降低代理层/浏览器空闲断连概率，并在初始化事件中下发心跳参数供前端断线重连判断。

### 邮件能力（安全场景）

- 当前会触发邮件发送的场景：
    - 登录成功后触发“登录环境异常检测”，当 IP 或设备指纹变化且满足策略时发送告警邮件。
    - 调用 `POST /auth/security/operation-confirm/send` 时发送敏感操作验证码邮件。
- `POST /auth/security/operation-confirm/verify` 只校验验证码并返回短期票据，不发送邮件。
- 当前“敏感业务接口强制二次确认”需要在具体业务接口中消费短期票据（`consumeTicket`）后才会真正生效。

### 邮件配置最小说明

- 业务开关与文案：`notify.mail.*`
- SMTP 连接参数：`spring.mail.*`
- 默认 `notify.mail.enabled=false`，即默认不发送邮件（Noop 实现）。
- 邮件健康检查开关：`management.health.mail.enabled`
    - 默认跟随 `NOTIFY_MAIL_ENABLED`。
    - 未启用邮件时默认不探测 SMTP，避免出现 `Mail health check failed` 的误告警。

### 验证码

- Redis 生成与校验。
- 配置：`auth.captcha.*`。

### 权限与菜单

- 权限校验由 `security.permission.*` 控制。
- 菜单权限来源于 `sys_menu.permission`。

### 通知 SSE 保活

- SSE 接口：`GET /notices/stream`。
- 初始化事件：`init`，包含 `heartbeatIntervalMillis`、`heartbeatTimeoutMillis`，用于前端设置心跳超时判定。
- 心跳事件：`ping`，由服务端按 `notice.sse.heartbeat-interval-millis` 周期推送。
- 关键配置：
  - `notice.sse.heartbeat-interval-millis`（默认 `60000`）
  - `notice.sse.heartbeat-timeout-millis`（默认 `180000`）
  - `notice.sse.latest-limit`（默认 `5`）

### 数据范围

**目标**
- 以“角色默认 → 角色×菜单 → 用户覆盖”的三层模型表达数据范围。
- 同层多角色合并为并集（最大范围）。
- 无配置时使用约定字段 `create_dept` / `create_by`。
- 读写解耦：写入记录归属字段，读取自动拼接过滤条件。

**三层架构（优先级）**
- Layer 3：用户级覆盖（`sys_user_data_scope`），优先级最高。
- Layer 2：角色×菜单级（`sys_role_menu` / `sys_role_menu_dept`）。
- Layer 1：角色默认级（`sys_role`）。
- 兜底：无角色 → `SELF`。

**多角色合并**
- 同层内取并集（最大范围）。
- 任一角色为 `ALL` → 全量放行。
- 其他范围合并为“可见部门集合 + 是否包含本人”。

**范围类型**
- `ALL`：全部数据
- `DEPT`：本部门
- `DEPT_AND_CHILD`：本部门及子部门
- `CUSTOM_DEPT` / `CUSTOM`：自定义部门
- `SELF`：仅本人
- `NONE`：无可见数据

**数据模型**
- 角色默认范围：`sys_role.data_scope_type` / `data_scope_value`。
- 角色×菜单范围：`sys_role_menu.data_scope_type` 覆盖角色默认。
- 角色×菜单×部门：`sys_role_menu_dept` 存自定义部门集合。
- 用户覆盖：`sys_user_data_scope`（`scope_key` 可为 `*` 表示全局）。
- 字段映射：`sys_data_scope_rule`（`scope_key` → 表/字段/别名）。
- 默认字段：`dept_column = create_dept`，`user_column = create_by`。

**登录预加载**
- `deptTreeIds`：当前部门及子部门集合。
- `roleDataScopes`：角色默认 + 菜单级范围。
- `userScopeOverrides`：用户级覆盖（`scope_key` → 覆盖配置）。

**读取过滤流程（SELECT）**
- 方法标注 `@DataScope`，写入 `scopeKey` 至线程上下文。
- 按 `sys_data_scope_rule` 获取字段映射，缺省使用默认字段。
- 计算最终范围（Layer3 → Layer2 → Layer1 → 兜底 SELF）。
- SQL 追加过滤条件（示例）：
```
WHERE (
  o.create_dept IN (...部门集合...)
  OR o.create_by = #{userId}
)
```

**特殊场景**
- `dept_column` 为空：仅按 `user_column` 过滤，部门范围退化为 `SELF`。
- 字段映射错误：数据库执行报错，便于快速定位配置问题。
- 同表多维度：用不同 `scope_key` 区分过滤维度。

**写入归属字段**
- 业务表继承 `BaseEntity`，自动填充 `create_by` / `create_dept`。
- `create_dept` 为数据归属快照，不依赖后续部门变更。

**落地规范**
- 新业务表优先使用 `create_by` / `create_dept`，零配置即可生效。
- 历史表/第三方表通过 `sys_data_scope_rule` 补充映射。
- 仅按用户过滤的表将 `dept_column` 设为 NULL。

**关键结论**
- 用户覆盖 > 角色×菜单 > 角色默认。
- 同层多角色并集。
- 无配置即使用默认字段。

### 配置文档

- 所有配置项说明已迁移到 `docs/CONFIGURATION.md`。
- English version: `docs/CONFIGURATION_EN.md`。

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
- 出现 `Mail health check failed`：检查 `management.health.mail.enabled` 是否被开启；若开启需同时完整配置
  `spring.mail.username/password`。

## 常用命令

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw -DskipTests package
```
