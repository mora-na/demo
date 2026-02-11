# Demo 系统（Spring Boot）

一个 Spring Boot 后端示例系统，内置认证、权限、数据范围控制、组织部门与菜单权限等能力，并提供若干安全防护与通用功能。

## 功能亮点

- 认证与鉴权：JWT、验证码登录、权限拦截（角色/权限/菜单）。
- 登录安全：登录失败次数限制（滑动窗口）与锁定策略。
- 密码安全：传输层可选 AES-GCM，持久化支持 bcrypt/md5/sm3（可配置）。
- 数据范围控制：按角色配置可见范围（ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE）。
- 安全防护：SQL 防护、XSS 过滤、限流、重复提交防护。
- Excel 导入导出：用户数据批量导入导出示例。

## 技术栈

- Spring Boot 2.7.x
- MyBatis-Plus + PageHelper
- Redis（验证码、令牌、限流等）
- PostgreSQL/MySQL（见 `sql/` 脚本）

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
