# 后端数据范围机制说明（详解）

本文档解释数据范围（Data Scope）在项目中的**完整生效链路**、**数据过滤细节**、**配置项**与**常见问题**，适用于排查“为什么数据被过滤/未过滤”的问题。

## 1. 生效链路（从登录到 SQL）

### 1.1 用户上下文加载

请求进入后，`AuthTokenFilter` 会解析 token 并加载数据库用户，然后调用 `DataScopeResolver` 计算最终数据范围：

- `AuthTokenFilter`：`src/main/java/com/example/demo/auth/web/AuthTokenFilter.java`
- `DataScopeResolver`：`src/main/java/com/example/demo/datascope/service/DataScopeResolver.java`

最终会把数据范围写入 `AuthUser`：

- `dataScopeType`
- `dataScopeValue`

> 注意：登录接口返回的 token 里有 `dataScopeType/value`，但**每次请求会再次根据角色/部门实时计算**并覆盖。

### 1.2 SQL 过滤生效

MyBatis-Plus 拦截器在 SQL 预编译前对 SQL 进行重写，注入数据范围条件：

- `MybatisPlusConfig`：注册 `DataScopeInnerInterceptor`
- `DataScopeInnerInterceptor`：在 `beforePrepare` 中重写 SQL

即 **SELECT / UPDATE / DELETE** 都会被过滤。

## 2. 数据范围如何计算（DataScopeResolver）

入口：`DataScopeResolver.resolve(SysUser user)`

角色/用户数据范围的规则（优先级从高到低）：

1. **任一角色=ALL** → `ALL`
2. **角色包含 DEPT / DEPT_AND_CHILD / CUSTOM_DEPT** → 合并部门 ID → `CUSTOM_DEPT`
3. **角色包含 SELF** → `SELF`
4. **角色包含 NONE 或可见范围为空** → `NONE`
5. **以上都没有** → 回退用户自身配置（`user.dataScopeType/value`），若为空则用默认值

组合规则说明：

- `DEPT_AND_CHILD`：通过部门树展开当前部门及子部门
- `DEPT`：仅当前部门
- `CUSTOM_DEPT`：角色配置的部门 ID 列表
- 最终统一落为 `CUSTOM_DEPT` 并写入 `dataScopeValue`（逗号分隔 ID 列表）

## 3. SQL 过滤细节（DataScopeInnerInterceptor）

### 3.1 过滤条件生成

对每个匹配的表，生成条件：

- `NONE` → `1 = 0`
- `SELF` → `{column} = userId`
- `DEPT` → `{column} = deptId`
- `CUSTOM/CUSTOM_DEPT/DEPT_AND_CHILD` → `{column} IN (dataScopeValue)`

多个表的条件会 **AND** 组合。

### 3.2 表与字段映射

拦截器仅对 **配置了数据范围字段**的表生效（表名匹配 + 字段名）：

- 规则来源：`DataScopeRuleProvider`
  - `db`：来自 `sys_data_scope_rule`
  - `config`：来自 `security.data-scope.table-column-map`

如果某表未配置映射，则 **不会增加数据范围条件**。

### 3.3 SQL 重写失败时

`DataScopeInnerInterceptor` 使用 JSqlParser 解析 SQL。解析失败会返回原 SQL，即：

> **解析失败 = 不过滤**

建议在复杂 SQL 场景下关注日志或逐步简化 SQL。

## 4. 配置项（application.yml）

```yaml
security:
  data-scope:
    enabled: true
    default-type: DEPT_AND_CHILD
    source: db                # db | config
    cache-seconds: 180
    # source=config 时生效
    table-column-map:
      sys_user: dept_id
      sys_dept: id
```

### source = db

使用数据库表 `sys_data_scope_rule`：

| 字段 | 说明 |
|------|------|
| table_name | 目标表名（小写匹配） |
| column_name | 数据范围字段名 |
| enabled | 1=启用 |

### source = config

使用 `table-column-map` 直接映射（适合小项目或无需动态管理）。

## 5. 常见问题与排查

### 5.1 数据未过滤

常见原因：

- `security.data-scope.enabled=false`
- `table-column-map` 没配置/`sys_data_scope_rule` 为空
- 用户最终 `dataScopeType=ALL`
- SQL 解析失败（JSqlParser 不支持）

### 5.2 数据全为空

可能原因：

- `dataScopeType=NONE`
- `deptId` 为空
- `dataScopeValue` 为空或非法
- 角色只配置了 DEPT/DEPT_AND_CHILD，但用户没有部门

### 5.3 多表查询过滤太严格

SQL 中多个表都配置了数据范围字段时，条件会 **AND** 组合，可能过窄。  
建议：

- 仅给“业务主表”配置数据范围字段
- 其他表不配置字段映射

## 6. 关键代码入口（索引）

- `AuthTokenFilter`：认证后写入用户数据范围  
  `src/main/java/com/example/demo/auth/web/AuthTokenFilter.java`
- `DataScopeResolver`：角色/部门范围计算  
  `src/main/java/com/example/demo/datascope/service/DataScopeResolver.java`
- `DataScopeInnerInterceptor`：SQL 过滤  
  `src/main/java/com/example/demo/common/mybatis/DataScopeInnerInterceptor.java`
- `DbDataScopeRuleProvider` / `ConfigDataScopeRuleProvider`：规则来源  
  `src/main/java/com/example/demo/common/mybatis/`

