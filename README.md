# Demo 系统（Spring Boot）

一个前后端分离的示例系统，聚焦“认证鉴权 + 系统管理 + 定时任务 + 通知推送”等通用能力，适合作为中后台项目的起点。

## 功能亮点

- 认证与鉴权：JWT、验证码登录、权限拦截（角色/权限/菜单）。
- 登录安全：登录失败次数限制（滑动窗口）与锁定策略。
- 密码安全：传输层可选 AES-GCM，持久化支持 bcrypt/md5/sm3（可配置）。
- 数据范围控制：按角色配置可见范围（ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE）。
- 安全防护：SQL 防护、XSS 过滤、限流、重复提交防护。
- Excel 导入导出：用户数据批量导入导出示例。
- 定时任务：Quartz 持久化调度、任务处理器管理、执行日志与详情查看。
- 系统通知：支持发布与阅读状态，SSE 实时推送未读数与列表。
- 缓存体系：Redis / 本地内存 / 数据库多策略缓存配置。

## 技术栈

### 后端
- Spring Boot 2.7.x（当前 2.7.12）
- MyBatis-Plus + PageHelper
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
    - 数据库连接与账号密码
    - Redis 连接
    - 认证与密码策略
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

## 文档

- 详细配置与运维说明见 `HELP.md`
- English help: `HELP_EN.md`

## 常用命令

```bash
./mvnw test
./mvnw spring-boot:run
```
