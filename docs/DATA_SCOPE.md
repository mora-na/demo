# 数据范围设计说明（3层架构）

本说明覆盖新的数据范围（Data Scope）设计与执行流程，包含三层优先级、映射配置、SQL 过滤规则以及特殊场景处理。

## 目标

- 以“角色默认 → 角色×菜单 → 用户覆盖”的三层模型表达数据范围。
- 同一层级多角色合并为**并集（最大范围）**。
- 缺省行为清晰、可预测；无配置时使用约定字段 `create_dept` / `create_by`。
- 读写解耦：写入时自动记录归属字段，读取时自动拼接过滤条件。

## 三层架构（纵向优先级）

优先级从高到低：

1. **Layer 3：用户级覆盖**（`sys_user_data_scope`）  
   - 只要存在用户级覆盖，则直接使用（最高优先级）。
2. **Layer 2：角色×菜单级**（`sys_role_menu.data_scope_type` + `sys_role_menu_dept`）  
   - 同一角色在不同菜单下可有不同数据范围。
3. **Layer 1：角色默认级**（`sys_role.data_scope_type`）  
   - 角色全局数据范围基线。

兜底规则：
- 用户没有任何角色 → 仅本人（SELF）。

## 横向合并（多角色并集）

当用户有多个角色时，在**同一层级**内取并集（最大范围）：

- 任一角色为 **ALL** → 直接放行全部数据。
- 其他范围合并为**可见部门集合 + 是否包含本人**。

## 数据范围类型

使用字符串枚举（全大写）：

- `ALL`：全部数据  
- `DEPT`：本部门  
- `DEPT_AND_CHILD`：本部门及子部门  
- `CUSTOM_DEPT` / `CUSTOM`：自定义部门  
- `SELF`：仅本人  
- `NONE`：无可见数据

## 数据模型

### Layer 1：角色默认范围
- 表：`sys_role`
- 字段：`data_scope_type`, `data_scope_value`（CUSTOM_DEPT 时存部门 ID 列表）

### Layer 2：角色×菜单范围
- 表：`sys_role_menu`
  - 字段：`data_scope_type`（覆盖角色默认）
- 表：`sys_role_menu_dept`
  - 角色 × 菜单 × 部门 的自定义部门集合

### Layer 3：用户覆盖
- 表：`sys_user_data_scope`
  - 字段：`scope_key`（菜单权限标识，或 `*` 表示全局覆盖）
  - `data_scope_type`, `data_scope_value`

### 字段映射配置（表 → 部门/用户字段）
表：`sys_data_scope_rule`

关键字段：
- `scope_key`：映射标识（通常为菜单权限标识，如 `biz:order:list`）
- `table_name`：目标表
- `table_alias`：表别名（可选）
- `dept_column`：部门字段名（可为 NULL）
- `user_column`：用户字段名（可为 NULL）
- `filter_type`：过滤方式（当前实现为追加 WHERE，其他值保留扩展）

无配置时默认字段：
- `dept_column = create_dept`
- `user_column = create_by`

## 登录时预加载（避免每次查库）

登录成功后装配并写入 `AuthUser`：

- `deptTreeIds`：当前部门及子部门集合  
- `roleDataScopes`：每个角色的默认范围 + 菜单级覆盖  
- `userScopeOverrides`：用户级覆盖（scope_key → 覆盖配置）

> 这样每次请求只需读取缓存中的登录上下文即可完成计算。

## 读取阶段（SELECT）过滤流程

1. **方法标注** `@DataScope` → 将 `scopeKey` 写入线程上下文  
2. **字段映射** 优先查 `sys_data_scope_rule`  
   - 无配置 → 使用默认字段 `create_dept` / `create_by`  
3. **计算最终范围**  
   - Layer3 → Layer2 → Layer1 → 兜底 SELF  
   - 多角色并集  
4. **SQL 拼接条件**  

示例：
```
WHERE (
    o.create_dept IN ( ...合并后的部门集合... )
    OR o.create_by = #{userId}
)
```

> 不加 `@DataScope` 则不做数据过滤（用于系统级操作/全量查询）。

## 特殊场景

### 仅按用户过滤（无部门概念）
若 `dept_column` 为 NULL：
- 只按 `user_column` 过滤  
- DEPT/DEPT_AND_CHILD 自动退化为 SELF

### 字段配置错误
- `dept_column` / `user_column` 指向不存在的列 → 数据库执行时抛错  
- 该错误不会被拦截吞掉，便于及时定位配置问题

### 多归属维度的业务表
用 `scope_key` 实现“同一表不同过滤维度”：

```
INSERT INTO sys_data_scope_rule (scope_key, table_name, table_alias, dept_column, user_column, remark) VALUES
('biz:ticket:created',  'biz_ticket', 't', 'create_dept',   'create_by',   '我创建的工单'),
('biz:ticket:assigned', 'biz_ticket', 't', 'assignee_dept', 'assignee_id', '我负责的工单');
```

## 写入阶段（自动归属字段）

所有业务表继承 `BaseEntity`：
- `create_by` / `create_dept` 自动填充
- `create_dept` 是数据归属部门（快照），不依赖后续部门变更

## 推荐落地规范

- 新建业务表：直接用 `create_by` / `create_dept`（零配置）
- 历史表/第三方表：在 `sys_data_scope_rule` 里补映射
- 仅按用户过滤的表：`dept_column` 设为 NULL

## 关键结论

- 数据范围以**用户覆盖 > 角色×菜单 > 角色默认**为准  
- 同层多角色合并为**并集**  
- 无配置时自动走默认字段，不需要额外维护  
