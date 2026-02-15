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

## 常用命令

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw -DskipTests package
```
