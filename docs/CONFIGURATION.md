# 配置说明

本文档从 `README.md` 拆分，集中维护所有配置项说明。

### 邮件通知能力（notify.mail + spring.mail）

- 默认实现放在 `src/main/java/com/example/demo/common/notify/mail`，供 `auth/notice` 等模块复用。
- 开启后使用 Spring Mail（SMTP）发送；关闭时自动降级为 Noop 实现（只记录日志，不发送）。
- SMTP 连接参数使用 `spring.mail.*` 标准配置；业务开关与文案使用 `notify.mail.*`。

**notify.mail 业务配置**

| 配置键                          | 默认值      | 说明                                  |
|------------------------------|----------|-------------------------------------|
| `notify.mail.enabled`        | `false`  | 是否启用 SMTP 邮件发送能力。                   |
| `notify.mail.from`           | ``       | 发件人地址；为空时回退 `spring.mail.username`。 |
| `notify.mail.subject-prefix` | `[Demo]` | 邮件主题前缀（发送时自动拼接）。                    |

**spring.mail SMTP 配置（Spring 标准）**

| 配置键                                                | 默认值          | 说明                      |
|----------------------------------------------------|--------------|-------------------------|
| `spring.mail.host`                                 | ``           | SMTP 主机地址。              |
| `spring.mail.port`                                 | ``           | SMTP 端口。                |
| `spring.mail.username`                             | ``           | SMTP 用户名（同时可作为默认发件人）。   |
| `spring.mail.password`                             | ``           | SMTP 密码/授权码。            |
| `spring.mail.properties.mail.smtp.auth`            | `true/false` | 是否启用 SMTP 认证（按服务商要求配置）。 |
| `spring.mail.properties.mail.smtp.starttls.enable` | `true/false` | 是否启用 STARTTLS。          |
| `spring.mail.properties.mail.smtp.ssl.enable`      | `true/false` | 是否启用 SSL。               |

### 认证安全增强（auth.security）

- 配置绑定类：`src/main/java/com/example/demo/auth/config/AuthProperties.java`。
- 当前提供两类能力：
    1. 异地/设备变更登录告警（登录成功后异步检测并发邮件）。
    2. 敏感操作邮箱二次确认（发码 + 验码 + 签发短期票据）。

**登录异常告警（auth.security.login-anomaly）**

| 配置键                                                   | 默认值      | 说明                                       |
|-------------------------------------------------------|----------|------------------------------------------|
| `auth.security.login-anomaly.enabled`                 | `true`   | 是否启用异地/设备变更告警。                           |
| `auth.security.login-anomaly.notify-on-ip-change`     | `true`   | 是否在 IP 变化时告警。                            |
| `auth.security.login-anomaly.notify-on-device-change` | `true`   | 是否在设备指纹变化时告警（设备类型/系统/浏览器）。               |
| `auth.security.login-anomaly.mail-subject`            | `登录安全提醒` | 告警邮件主题（不含 `notify.mail.subject-prefix`）。 |

**敏感操作二次确认（auth.security.operation-confirm）**

| 配置键                                                       | 默认值         | 说明                                         |
|-----------------------------------------------------------|-------------|--------------------------------------------|
| `auth.security.operation-confirm.enabled`                 | `true`      | 是否启用邮箱二次确认能力。                              |
| `auth.security.operation-confirm.code-length`             | `6`         | 验证码长度（系统内部限制 4~10）。                        |
| `auth.security.operation-confirm.code-ttl-seconds`        | `300`       | 验证码有效期（秒，最小 60）。                           |
| `auth.security.operation-confirm.resend-interval-seconds` | `60`        | 发码冷却时间（秒，最小 10）。                           |
| `auth.security.operation-confirm.max-verify-attempts`     | `5`         | 单验证码最大校验失败次数。                              |
| `auth.security.operation-confirm.ticket-ttl-seconds`      | `900`       | 验证通过后票据有效期（秒，最小 60）。                       |
| `auth.security.operation-confirm.mail-subject`            | `敏感操作确认验证码` | 二次确认邮件主题（不含 `notify.mail.subject-prefix`）。 |

**相关接口（需登录）**

| 接口                                             | 说明            |
|------------------------------------------------|---------------|
| `POST /auth/security/operation-confirm/send`   | 发送敏感操作邮箱验证码。  |
| `POST /auth/security/operation-confirm/verify` | 校验验证码并返回短期票据。 |

### DataScope 模块常量覆盖（datascope.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/datascope/config/DataScopeConstants.java`。
- 需要覆盖默认行为时，在配置文件中使用 `datascope.constants.*` 即可；未配置的键自动使用默认值。
- 建议仅覆盖确有业务差异的键，减少跨环境维护成本。

**Scope 组（全局覆盖标识）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `datascope.constants.scope.global-scope-key` | `*` | 用户覆盖里的“全局规则”scopeKey；命中该键时会作为所有权限点的兜底覆盖。 |
| `datascope.constants.scope.global-scope-menu-name` | `全局覆盖` | 用户数据范围列表中，全局规则展示的菜单名称。 |
| `datascope.constants.scope.global-scope-permission` | `*` | 用户数据范围列表中，全局规则展示的权限标识。 |

**Layer 组（来源层级标签）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `datascope.constants.layer.source-layer3` | `LAYER3` | 返回结果中“用户级覆盖”层的来源标签。 |
| `datascope.constants.layer.source-layer2` | `LAYER2` | 返回结果中“角色×菜单”层的来源标签。 |
| `datascope.constants.layer.source-layer1` | `LAYER1` | 返回结果中“角色默认”层的来源标签。 |

**Label 组（最终范围标签）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `datascope.constants.label.final-all` | `ALL` | 最终结果为全量可见时的标签。 |
| `datascope.constants.label.final-none` | `NONE` | 最终结果为无可见数据时的标签。 |
| `datascope.constants.label.final-self` | `SELF` | 最终结果仅本人可见时的标签。 |
| `datascope.constants.label.final-dept` | `DEPT` | 最终结果为部门范围可见时的标签。 |
| `datascope.constants.label.final-dept-and-self` | `DEPT+SELF` | 最终结果同时包含“部门 + 本人”时的标签。 |

**Rule 组（规则来源与默认字段）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `datascope.constants.rule.source-default` | `DEFAULT` | 未命中 `sys_data_scope_rule` 时，规则来源标识。 |
| `datascope.constants.rule.source-mapping` | `MAPPING` | 命中 `sys_data_scope_rule` 时，规则来源标识。 |
| `datascope.constants.rule.default-dept-column` | `create_dept` | 未配置映射时默认部门列名。 |
| `datascope.constants.rule.default-user-column` | `create_by` | 未配置映射时默认用户列名。 |

**SQL 组（条件拼接文本）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `datascope.constants.sql.no-filter-text` | `无过滤` | 最终范围为 `ALL` 时返回的可读 SQL 说明文本。 |
| `datascope.constants.sql.none-condition` | `1 = 0` | 最终范围为 `NONE` 时返回的拒绝条件。 |
| `datascope.constants.sql.in-operator` | ` IN ` | 部门范围条件中 `IN` 操作符文本（含空格）。 |
| `datascope.constants.sql.equals-operator` | ` = ` | 本人范围条件中的等号操作符文本（含空格）。 |
| `datascope.constants.sql.self-user-param` | `:userId` | 本人范围条件中的用户参数占位符。 |
| `datascope.constants.sql.or-operator` | ` OR ` | 部门条件与本人条件合并时使用的逻辑操作符（含空格）。 |
| `datascope.constants.sql.dot` | `.` | 表别名与列名拼接分隔符。 |
| `datascope.constants.sql.left-bracket` | `(` | 复合条件左括号。 |
| `datascope.constants.sql.right-bracket` | `)` | 复合条件右括号。 |

**Status 组（启停状态值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `datascope.constants.status.enabled` | `1` | 启用态统一值（规则启用、覆盖启用等）。 |
| `datascope.constants.status.disabled` | `0` | 禁用态统一值（规则禁用、覆盖禁用等）。 |
| `datascope.constants.status.role-active` | `1` | 角色在数据范围计算中被视为有效的状态值。 |

**Controller 组（接口错误码）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `datascope.constants.controller.bad-request-code` | `400` | 新增/更新规则时参数冲突等错误返回码。 |
| `datascope.constants.controller.not-found-code` | `404` | 用户或规则不存在时的错误返回码。 |
| `datascope.constants.controller.internal-server-error-code` | `500` | 更新/删除失败时的错误返回码。 |

**Filter 组（过滤类型边界）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `datascope.constants.filter.default-type` | `1` | 过滤类型缺省值。 |
| `datascope.constants.filter.type-min` | `1` | 过滤类型允许的最小值。 |
| `datascope.constants.filter.type-max` | `3` | 过滤类型允许的最大值。 |

**Parser 组（解析分隔符）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `datascope.constants.parser.dept-id-separator` | `,` | 自定义部门 ID 串解析与拼接时使用的分隔符。 |

### Dept 模块常量覆盖（dept.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/dept/config/DeptConstants.java`。
- 如需覆盖默认行为，可在配置文件中使用 `dept.constants.*`；未配置项自动使用默认值。
- 建议仅覆盖确有差异的键，避免环境迁移时出现配置膨胀。

**Status 组（部门状态值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dept.constants.status.enabled` | `1` | 部门“启用”状态值。创建部门状态缺省或非法时会回退到该值。 |
| `dept.constants.status.disabled` | `0` | 部门“禁用”状态值。状态更新接口仅接受 `enabled/disabled` 两个值。 |

**Sort 组（默认排序）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dept.constants.sort.default-sort` | `0` | 创建部门时未传 `sort` 的默认排序值。 |

**Controller 组（错误码）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dept.constants.controller.bad-request-code` | `400` | 参数非法、编码冲突、上级部门校验失败等场景返回码。 |
| `dept.constants.controller.not-found-code` | `404` | 部门不存在场景返回码。 |
| `dept.constants.controller.internal-server-error-code` | `500` | 更新/删除失败场景返回码。 |

**Message 组（i18n 消息键）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dept.constants.message.dept-not-found` | `dept.not.found` | 部门不存在消息键。 |
| `dept.constants.message.dept-code-exists` | `dept.code.exists` | 部门编码重复消息键。 |
| `dept.constants.message.dept-parent-not-found` | `dept.parent.not.found` | 上级部门不存在消息键。 |
| `dept.constants.message.dept-parent-cannot-self` | `dept.parent.cannot.self` | 上级部门不可为自身消息键。 |
| `dept.constants.message.common-update-failed` | `common.update.failed` | 更新失败消息键。 |
| `dept.constants.message.common-status-invalid` | `common.status.invalid` | 状态值非法消息键。 |
| `dept.constants.message.common-status-update-failed` | `common.status.update.failed` | 状态更新失败消息键。 |
| `dept.constants.message.common-delete-failed` | `common.delete.failed` | 删除失败消息键。 |

### Dict 模块常量覆盖（dict.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/dict/config/DictConstants.java`。
- 可通过配置文件 `dict.constants.*` 覆盖默认行为；未配置项自动使用默认值。
- 建议仅覆盖确有业务差异的键，避免与数据库字典数据配置混淆。

**Status 组（字典状态值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dict.constants.status.enabled` | `1` | 字典类型/字典数据“启用”状态值。 |
| `dict.constants.status.disabled` | `0` | 字典类型/字典数据“禁用”状态值。 |

**Sort 组（默认排序）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dict.constants.sort.default-sort` | `0` | 新增字典类型或字典数据时，未传 `sort` 的默认排序值。 |

**Controller 组（错误码）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dict.constants.controller.bad-request-code` | `400` | 参数非法、字典类型/数据冲突等业务错误返回码。 |
| `dict.constants.controller.not-found-code` | `404` | 字典类型或字典数据不存在时返回码。 |
| `dict.constants.controller.internal-server-error-code` | `500` | 更新/删除持久化失败时返回码。 |

**Message 组（i18n 消息键）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dict.constants.message.dict-type-exists` | `dict.type.exists` | 字典类型重复消息键。 |
| `dict.constants.message.dict-type-not-found` | `dict.type.not.found` | 字典类型不存在消息键。 |
| `dict.constants.message.dict-data-exists` | `dict.data.exists` | 字典数据重复消息键。 |
| `dict.constants.message.dict-data-not-found` | `dict.data.not.found` | 字典数据不存在消息键。 |
| `dict.constants.message.common-update-failed` | `common.update.failed` | 通用更新失败消息键。 |
| `dict.constants.message.common-delete-failed` | `common.delete.failed` | 通用删除失败消息键。 |

**Public API 组（公开接口返回）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dict.constants.public-api.success-message` | `success` | 公开字典接口成功响应 message 文案。 |
| `dict.constants.public-api.batch-type-separator` | `,` | `GET /api/dict/data/batch` 中 `types` 参数分隔符。 |

**Cache 组（缓存键规则）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dict.constants.cache.key-prefix` | `dict:data:` | 按字典类型缓存时的 key 前缀（最终 key 为 `prefix + dictType`）。 |
| `dict.constants.cache.all-key` | `dict:data:all` | 全量字典缓存 key。 |
| `dict.constants.cache.ttl-seconds` | `600` | 字典缓存 TTL（秒），`<=0` 表示不缓存。 |

**Serializer 组（字典标签序列化）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `dict.constants.serializer.label-field-suffix` | `Label` | `@DictLabel` 自动追加标签字段时的后缀（如 `status` -> `statusLabel`）。 |

### Menu 模块常量覆盖（menu.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/menu/config/MenuConstants.java`。
- 可通过配置文件 `menu.constants.*` 覆盖默认行为；未配置项自动使用默认值。
- 建议仅覆盖确有业务差异的键，避免与前端菜单权限配置语义冲突。

**Status 组（菜单状态值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `menu.constants.status.enabled` | `1` | 菜单“启用”状态值。 |
| `menu.constants.status.disabled` | `0` | 菜单“禁用”状态值。 |

**Sort 组（默认排序）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `menu.constants.sort.default-sort` | `0` | 新增菜单时，未传 `sort` 的默认排序值。 |

**Controller 组（错误码）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `menu.constants.controller.bad-request-code` | `400` | 参数非法、上级菜单校验失败、编码冲突等业务错误返回码。 |
| `menu.constants.controller.not-found-code` | `404` | 菜单不存在时返回码。 |
| `menu.constants.controller.internal-server-error-code` | `500` | 更新状态/更新菜单/删除菜单持久化失败时返回码。 |

**Message 组（i18n 消息键）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `menu.constants.message.menu-not-found` | `menu.not.found` | 菜单不存在消息键。 |
| `menu.constants.message.menu-code-exists` | `menu.code.exists` | 菜单编码冲突消息键。 |
| `menu.constants.message.menu-parent-not-found` | `menu.parent.not.found` | 上级菜单不存在消息键。 |
| `menu.constants.message.menu-parent-cannot-self` | `menu.parent.cannot.self` | 上级菜单不可为自身消息键。 |
| `menu.constants.message.common-update-failed` | `common.update.failed` | 通用更新失败消息键。 |
| `menu.constants.message.common-status-invalid` | `common.status.invalid` | 状态值非法消息键。 |
| `menu.constants.message.common-status-update-failed` | `common.status.update.failed` | 状态更新失败消息键。 |
| `menu.constants.message.common-delete-failed` | `common.delete.failed` | 通用删除失败消息键。 |

### Notice 模块常量覆盖（notice.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/notice/config/NoticeConstants.java`。
- 可通过配置文件 `notice.constants.*` 覆盖默认行为；未配置项自动使用默认值。
- 建议仅覆盖确有业务差异的键，避免通知范围值与历史数据语义不一致。

**Controller 组（错误码）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `notice.constants.controller.bad-request-code` | `400` | 参数非法、通知范围不合法、目标接收人为空等业务错误返回码。 |
| `notice.constants.controller.unauthorized-code` | `401` | 未登录访问“我的通知/未读数/已读操作/SSE”时返回码。 |
| `notice.constants.controller.not-found-code` | `404` | 通知不存在时返回码。 |
| `notice.constants.controller.internal-server-error-code` | `500` | 删除通知持久化失败时返回码。 |

**Message 组（i18n 消息键）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `notice.constants.message.notice-scope-invalid` | `notice.scope.invalid` | 通知范围类型非法消息键。 |
| `notice.constants.message.notice-scope-empty` | `notice.scope.empty` | 指定范围下 scopeIds 为空消息键。 |
| `notice.constants.message.notice-recipients-empty` | `notice.recipients.empty` | 解析后的接收人为空消息键。 |
| `notice.constants.message.notice-not-found` | `notice.not.found` | 通知不存在消息键。 |
| `notice.constants.message.auth-permission-required` | `auth.permission.required` | 需要登录消息键。 |
| `notice.constants.message.common-delete-failed` | `common.delete.failed` | 删除失败消息键。 |

**Page 组（分页默认值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `notice.constants.page.default-page-num` | `1` | 分页对象为空时默认页码。 |
| `notice.constants.page.default-page-size` | `10` | 分页对象为空时默认页大小。 |

**Scope 组（通知范围）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `notice.constants.scope.all` | `ALL` | 全员通知范围标识。 |
| `notice.constants.scope.dept` | `DEPT` | 按部门通知范围标识。 |
| `notice.constants.scope.role` | `ROLE` | 按角色通知范围标识。 |
| `notice.constants.scope.user` | `USER` | 按用户通知范围标识。 |
| `notice.constants.scope.scope-value-separator` | `,` | `scopeIds` 序列化到 `scope_value` 时分隔符。 |
| `notice.constants.scope.empty-scope-type` | `` | `scopeType` 为空时的默认归一化值。 |

**Recipient / User / Common / Numeric 组**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `notice.constants.recipient.unread` | `0` | 接收记录未读状态值。 |
| `notice.constants.recipient.read` | `1` | 接收记录已读状态值。 |
| `notice.constants.user.enabled-status` | `1` | 可作为通知目标用户的启用状态值。 |
| `notice.constants.common.not-deleted-flag` | `0` | 逻辑未删除标记值。 |
| `notice.constants.numeric.zero-long` | `0` | Long 类型零值兜底（统计/未读数）。 |
| `notice.constants.numeric.zero-int` | `0` | Int 类型零值兜底（标记已读返回值等）。 |

**Stream 组（SSE 推送）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `notice.constants.stream.anonymous-emitter-timeout-millis` | `0` | 匿名用户 SSE 连接超时（毫秒）。 |
| `notice.constants.stream.emitter-timeout-millis` | `0` | 登录用户 SSE 连接超时（毫秒）。 |
| `notice.constants.stream.heartbeat-interval-millis` | `30000` | 心跳间隔（毫秒），`<=0` 表示禁用。 |
| `notice.constants.stream.heartbeat-timeout-millis` | `90000` | 前端断线判定超时（毫秒）。 |
| `notice.constants.stream.latest-limit` | `5` | SSE 初始化/推送携带的最新通知缓存长度。 |
| `notice.constants.stream.event-notice-name` | `notice` | 新通知或未读变化事件名。 |
| `notice.constants.stream.event-init-name` | `init` | 初始化事件名。 |
| `notice.constants.stream.event-ping-name` | `ping` | 心跳事件名。 |
| `notice.constants.stream.heartbeat-thread-name` | `notice-sse-heartbeat` | 心跳线程名。 |
| `notice.constants.stream.log-heartbeat-disabled` | `Notice SSE heartbeat disabled (interval={}ms).` | 心跳禁用日志模板。 |
| `notice.constants.stream.log-push-failed` | `Failed to push notice to user {}, removing emitter.` | 新通知推送失败日志模板。 |
| `notice.constants.stream.log-push-update-failed` | `Failed to push notice update to user {}, removing emitter.` | 未读数推送失败日志模板。 |
| `notice.constants.stream.log-init-failed` | `Failed to send init payload to user {}, removing emitter.` | 初始化推送失败日志模板。 |
| `notice.constants.stream.log-heartbeat-failed` | `Heartbeat failed for user {}, removing emitter: {}` | 心跳推送失败日志模板。 |

### Permission 模块常量覆盖（permission.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/permission/config/PermissionConstants.java`。
- 可通过配置文件 `permission.constants.*` 覆盖默认行为；未配置项自动使用默认值。
- 建议：若覆盖 Message 组键值，请确保对应 i18n 资源中存在同名文案键。

**Status 组（角色/权限状态值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `permission.constants.status.enabled` | `1` | 角色与权限“启用”状态值。创建角色/权限时状态缺省或非法会回退到该值。 |
| `permission.constants.status.disabled` | `0` | 角色与权限“禁用”状态值。状态更新接口仅接受 `enabled/disabled` 两个值。 |

**Controller 组（错误码）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `permission.constants.controller.bad-request-code` | `400` | 参数非法、编码冲突、角色菜单关系不存在等业务校验失败返回码。 |
| `permission.constants.controller.not-found-code` | `404` | 角色/权限资源不存在时返回码。 |
| `permission.constants.controller.internal-server-error-code` | `500` | 更新、删除、分配权限/菜单等持久化失败返回码。 |

**Message 组（i18n 消息键）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `permission.constants.message.permission-not-found` | `permission.not.found` | 权限不存在消息键。 |
| `permission.constants.message.permission-code-exists` | `permission.code.exists` | 权限编码冲突消息键。 |
| `permission.constants.message.role-not-found` | `role.not.found` | 角色不存在消息键。 |
| `permission.constants.message.role-code-exists` | `role.code.exists` | 角色编码冲突消息键。 |
| `permission.constants.message.menu-not-found` | `menu.not.found` | 菜单不存在消息键。 |
| `permission.constants.message.role-menu-not-found` | `role.menu.not.found` | 角色与菜单关联关系不存在消息键。 |
| `permission.constants.message.role-permissions-assign-failed` | `role.permissions.assign.failed` | 角色分配权限失败消息键。 |
| `permission.constants.message.role-menus-assign-failed` | `role.menus.assign.failed` | 角色分配菜单失败消息键。 |
| `permission.constants.message.common-update-failed` | `common.update.failed` | 通用更新失败消息键。 |
| `permission.constants.message.common-status-invalid` | `common.status.invalid` | 状态值非法消息键。 |
| `permission.constants.message.common-status-update-failed` | `common.status.update.failed` | 状态更新失败消息键。 |
| `permission.constants.message.common-delete-failed` | `common.delete.failed` | 通用删除失败消息键。 |

**MenuDataScope 组（角色菜单数据范围归一化）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `permission.constants.menu-data-scope.inherit` | `INHERIT` | 菜单级数据范围“继承角色默认值”标识（不区分大小写）。命中后会清空菜单级覆盖，回退到角色默认数据范围。 |
| `permission.constants.menu-data-scope.default-type` | `DEFAULT` | 菜单级数据范围“默认值”兼容标识（不区分大小写），行为与 `inherit` 一致。 |

### Post 模块常量覆盖（post.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/post/config/PostConstants.java`。
- 可通过配置文件 `post.constants.*` 覆盖默认行为；未配置项自动使用默认值。
- 建议：若覆盖 Message 组键值，请确保对应 i18n 资源中存在同名文案键。

**Status 组（岗位状态值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `post.constants.status.enabled` | `1` | 岗位“启用”状态值。创建岗位状态缺省或非法时会回退到该值。 |
| `post.constants.status.disabled` | `0` | 岗位“禁用”状态值。状态更新接口仅接受 `enabled/disabled` 两个值。 |

**Sort 组（默认排序）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `post.constants.sort.default-sort` | `0` | 创建岗位时未传 `sort` 的默认排序值。 |

**Controller 组（错误码）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `post.constants.controller.bad-request-code` | `400` | 参数非法、岗位编码冲突、部门不存在等场景返回码。 |
| `post.constants.controller.not-found-code` | `404` | 岗位不存在场景返回码。 |
| `post.constants.controller.internal-server-error-code` | `500` | 更新、状态变更、删除失败场景返回码。 |

**Message 组（i18n 消息键）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `post.constants.message.post-not-found` | `post.not.found` | 岗位不存在消息键。 |
| `post.constants.message.post-code-exists` | `post.code.exists` | 岗位编码冲突消息键。 |
| `post.constants.message.dept-not-found` | `dept.not.found` | 部门不存在消息键。 |
| `post.constants.message.common-update-failed` | `common.update.failed` | 通用更新失败消息键。 |
| `post.constants.message.common-status-invalid` | `common.status.invalid` | 状态值非法消息键。 |
| `post.constants.message.common-status-update-failed` | `common.status.update.failed` | 状态更新失败消息键。 |
| `post.constants.message.common-delete-failed` | `common.delete.failed` | 通用删除失败消息键。 |

### User 模块常量覆盖（user.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/user/config/UserConstants.java`。
- 可通过配置文件 `user.constants.*` 覆盖默认行为；未配置项自动使用默认值。
- 建议：若覆盖 Message 组键值，请确保对应 i18n 资源中存在同名文案键。

**Status 组（用户与数据范围状态值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `user.constants.status.enabled` | `1` | 用户启用状态值。创建用户状态缺省时会回退到该值。 |
| `user.constants.status.disabled` | `0` | 用户禁用状态值。用户状态更新接口仅接受 `enabled/disabled` 两个值。 |
| `user.constants.status.data-scope-enabled` | `1` | 新建用户数据范围覆盖记录时默认启用状态值。 |

**Controller 组（错误码）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `user.constants.controller.bad-request-code` | `400` | 参数非法、用户名冲突、密码不合法、数据范围冲突等场景返回码。 |
| `user.constants.controller.forbidden-code` | `403` | 批量删除用户时存在越权或不可见用户场景返回码。 |
| `user.constants.controller.not-found-code` | `404` | 用户或用户数据范围记录不存在场景返回码。 |
| `user.constants.controller.internal-server-error-code` | `500` | 更新、删除、重置密码、分配角色/岗位等持久化失败场景返回码。 |

**Password 组（密码校验）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `user.constants.password.min-length` | `6` | 用户创建与重置密码时最小长度校验值。 |

**Page 组（分页默认值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `user.constants.page.default-page-num` | `1` | 分页参数为空时默认页码。 |
| `user.constants.page.default-page-size` | `10` | 分页参数为空时默认页大小。 |

**Scope 组（用户数据范围全局覆盖标识）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `user.constants.scope.global-scope-key` | `*` | 用户数据范围中的全局覆盖 `scopeKey`。空值会归一化为该值。 |
| `user.constants.scope.global-scope-menu-name` | `全局覆盖` | 全局覆盖在用户数据范围详情中的菜单展示名。 |
| `user.constants.scope.global-scope-permission` | `*` | 全局覆盖在用户数据范围详情中的权限标识展示值。 |

**Excel 组（导入导出）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `user.constants.excel.export-file-prefix` | `用户信息` | 用户导出文件名默认前缀（后拼接时间戳）。 |

**Message 组（i18n 消息键）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `user.constants.message.user-not-found` | `user.not.found` | 用户不存在消息键。 |
| `user.constants.message.user-username-exists` | `user.username.exists` | 用户名重复消息键。 |
| `user.constants.message.dept-not-found` | `dept.not.found` | 部门不存在消息键。 |
| `user.constants.message.user-password-empty` | `user.password.empty` | 密码为空消息键。 |
| `user.constants.message.user-password-invalid` | `user.password.invalid` | 密码解密失败或格式非法消息键。 |
| `user.constants.message.user-password-length-invalid` | `user.password.length.invalid` | 密码长度非法消息键。 |
| `user.constants.message.user-password-weak` | `user.password.weak` | 密码强度不足消息键。 |
| `user.constants.message.user-password-reset-failed` | `user.password.reset.failed` | 重置密码失败消息键。 |
| `user.constants.message.user-roles-assign-failed` | `user.roles.assign.failed` | 分配角色失败消息键。 |
| `user.constants.message.user-posts-assign-failed` | `user.posts.assign.failed` | 分配岗位失败消息键。 |
| `user.constants.message.user-data-scope-update-failed` | `user.data.scope.update.failed` | 更新数据范围失败消息键。 |
| `user.constants.message.data-scope-user-exists` | `data.scope.user.exists` | 用户数据范围覆盖重复消息键。 |
| `user.constants.message.data-scope-user-not-found` | `data.scope.user.not.found` | 用户数据范围覆盖不存在消息键。 |
| `user.constants.message.common-update-failed` | `common.update.failed` | 通用更新失败消息键。 |
| `user.constants.message.common-status-invalid` | `common.status.invalid` | 状态值非法消息键。 |
| `user.constants.message.common-status-update-failed` | `common.status.update.failed` | 状态更新失败消息键。 |
| `user.constants.message.common-delete-failed` | `common.delete.failed` | 通用删除失败消息键。 |
| `user.constants.message.auth-permission-denied` | `auth.permission.denied` | 权限拒绝消息键。 |
| `user.constants.message.user-import-success` | `user.import.success` | 用户导入成功消息键。 |
| `user.constants.message.user-import-failed` | `user.import.failed` | 用户导入失败消息键。 |

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
- 缓存配置：`dict.constants.cache.ttl-seconds`（<=0 表示不缓存）。
- 后端翻译：VO 字段标注 `@DictLabel("sys_gender")` 自动追加 `xxxLabel` 字段。

### 通知（SSE 推送）

- 配置：`notice.constants.stream.*`。

### 定时任务（Quartz）

- 持久化调度配置：`spring.quartz.*`。
- 任务处理器需实现 `JobHandler` 并注册为 Spring Bean。
- 记录存储：`sys_job_log`，详情日志使用 `log_detail` 字段。

### 执行日志自动收集（Job Log Collect）

- 开关与范围：`job.constants.log-collect.enabled`、`job.constants.log-collect.scope`。
- 级别与长度：`job.constants.log-collect.min-level`、`job.constants.log-collect.max-length`。
- 异步合并：`job.constants.log-collect.merge-delay-millis`、`job.constants.log-collect.max-hold-millis`。
- 线程上下文兜底：`job.constants.log-collect.inherit-thread-context`。

### Job 模块常量覆盖（job.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/job/config/JobConstants.java`。
- 可通过配置文件 `job.constants.*` 覆盖默认行为；未配置项自动使用默认值。
- 建议：仅覆盖确有业务差异的项，避免调度键/状态值与历史数据不一致。

**Controller 组（接口错误码）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.controller.bad-request-code` | `400` | 参数非法、Cron/处理器/misfire 校验失败等错误码。 |
| `job.constants.controller.not-found-code` | `404` | 任务或任务日志不存在时错误码。 |
| `job.constants.controller.internal-server-error-code` | `500` | 创建/更新/删除/运行失败时错误码。 |

**Message 组（i18n 消息键）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.message.job-not-found` | `job.not.found` | 任务不存在消息键。 |
| `job.constants.message.job-log-not-found` | `job.log.not.found` | 任务日志不存在消息键。 |
| `job.constants.message.job-cron-invalid` | `job.cron.invalid` | Cron 表达式非法消息键。 |
| `job.constants.message.job-handler-invalid` | `job.handler.invalid` | 任务处理器非法消息键。 |
| `job.constants.message.job-misfire-invalid` | `job.misfire.invalid` | Misfire 策略非法消息键。 |
| `job.constants.message.job-create-failed` | `job.create.failed` | 创建任务失败消息键。 |
| `job.constants.message.job-update-failed` | `job.update.failed` | 更新任务失败消息键。 |
| `job.constants.message.job-delete-failed` | `job.delete.failed` | 删除任务失败消息键。 |
| `job.constants.message.job-status-update-failed` | `job.status.update.failed` | 更新任务状态失败消息键。 |
| `job.constants.message.job-run-failed` | `job.run.failed` | 手动执行任务失败消息键。 |

**Page 组（分页默认值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.page.default-page-num` | `1` | 查询分页对象为空时的默认页码。 |
| `job.constants.page.default-page-size` | `10` | 查询分页对象为空时的默认页大小。 |

**Status 组（任务/日志状态值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.status.job-enabled` | `1` | 任务启用状态值。 |
| `job.constants.status.job-disabled` | `0` | 任务禁用状态值。 |
| `job.constants.status.log-success` | `1` | 执行日志成功状态值。 |
| `job.constants.status.log-failed` | `0` | 执行日志失败状态值。 |

**Concurrent 组（并发开关值）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.concurrent.allow` | `1` | 允许并发执行标记值。 |
| `job.constants.concurrent.disallow` | `0` | 禁止并发执行标记值。 |

**Scheduler 组（Quartz 键规则）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.scheduler.job-group` | `SYS_JOB` | Quartz `JobKey` 分组名。 |
| `job.constants.scheduler.trigger-group` | `SYS_JOB_TRIGGER` | Quartz `TriggerKey` 分组名。 |
| `job.constants.scheduler.job-key-prefix` | `JOB_` | Quartz 任务键前缀（最终 `JOB_ + jobId`）。 |
| `job.constants.scheduler.trigger-key-prefix` | `TRIGGER_` | Quartz 触发器键前缀（最终 `TRIGGER_ + jobId`）。 |

**DataMap 组（Quartz JobDataMap 键）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.data-map.job-id-key` | `jobId` | 任务 ID 的 JobDataMap 键。 |
| `job.constants.data-map.job-name-key` | `jobName` | 任务名的 JobDataMap 键。 |
| `job.constants.data-map.handler-name-key` | `handlerName` | 处理器名的 JobDataMap 键。 |
| `job.constants.data-map.cron-expression-key` | `cronExpression` | Cron 表达式的 JobDataMap 键。 |
| `job.constants.data-map.params-key` | `params` | 参数字符串的 JobDataMap 键。 |

**Execution 组（执行日志拼装）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.execution.execute-start-prefix` | `开始执行: ` | 手动日志中执行开始前缀。 |
| `job.constants.execution.params-prefix` | `参数: ` | 手动日志中参数行前缀。 |
| `job.constants.execution.handler-not-found-message` | `handler not found` | 处理器缺失时写入 `message` 的文本。 |
| `job.constants.execution.handler-not-found-log-prefix` | `处理器不存在: ` | 处理器缺失时写入手动日志的前缀。 |
| `job.constants.execution.execute-success-log` | `执行成功` | 执行成功手动日志文本。 |
| `job.constants.execution.execute-error-prefix` | `执行异常: ` | 执行异常手动日志前缀。 |
| `job.constants.execution.log-merge-separator` | `\n----\n` | 手动日志与自动采集日志合并分隔符。 |
| `job.constants.execution.message-max-length` | `500` | `sys_job_log.message` 最大截断长度。 |
| `job.constants.execution.log-detail-max-length` | `8000` | `sys_job_log.log_detail` 最大截断长度。 |

**Handler Demo 组（示例处理器）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.handler-demo.manual-log-start` | `手动记录定时任务日志` | 示例处理器起始手动日志文本。 |
| `job.constants.handler-demo.manual-log-end` | `手动记录日志任务结束` | 示例处理器结束手动日志文本。 |
| `job.constants.handler-demo.new-thread-log` | `new Thread 未显式透传也可收集日志` | 示例中普通线程日志文本。 |
| `job.constants.handler-demo.async-thread-log` | `异步线程日志手动记录` | 示例中异步线程日志文本。 |
| `job.constants.handler-demo.plain-thread-name` | `job-log-plain` | 示例普通线程名称。 |
| `job.constants.handler-demo.async-thread-name` | `job-log-demo` | 示例异步线程名称。 |
| `job.constants.handler-demo.raw-executor-pool-size` | `1` | 示例未包装线程池大小。 |
| `job.constants.handler-demo.wrapped-executor-pool-size` | `1` | 示例包装线程池大小。 |
| `job.constants.handler-demo.schedule-delay-millis` | `100` | 示例延迟任务触发毫秒数。 |

**Log Collect 组（自动日志收集）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.log-collect.enabled` | `true` | 是否启用任务自动日志收集。 |
| `job.constants.log-collect.scope` | `MDC` | 收集范围：`MDC` 或 `THREAD`。 |
| `job.constants.log-collect.min-level` | `INFO` | 最低收集日志级别。 |
| `job.constants.log-collect.max-length` | `65536` | 单次执行自动日志最大长度（字符）。 |
| `job.constants.log-collect.merge-delay-millis` | `3000` | 延迟合并日志毫秒数。 |
| `job.constants.log-collect.max-hold-millis` | `60000` | 运行实例日志缓冲最大保留毫秒数。 |
| `job.constants.log-collect.inherit-thread-context` | `true` | 是否允许线程上下文兜底透传 runId。 |
| `job.constants.log-collect.mdc-key` | `jobLogId` | runId 在 MDC 的键名。 |
| `job.constants.log-collect.thread-key` | `jobLogThread` | 线程名在 MDC 的键名（`scope=THREAD` 生效）。 |
| `job.constants.log-collect.collector-thread-name` | `job-log-collector` | 收集器后台线程名。 |
| `job.constants.log-collect.cleanup-initial-delay-millis` | `60000` | 缓冲清理任务首次执行延迟。 |
| `job.constants.log-collect.cleanup-interval-millis` | `60000` | 缓冲清理任务执行周期。 |

**Appender 组（Logback 收集器）**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `job.constants.appender.appender-name` | `JOB_LOG_COLLECTOR` | 注入 Root Logger 的 appender 名称。 |
| `job.constants.appender.time-pattern` | `HH:mm:ss.SSS` | 收集日志时间格式。 |
| `job.constants.appender.empty-message` | `` | 日志消息为 null 时的替代文本。 |
| `job.constants.appender.throwable-separator` | `\n` | 普通日志与异常堆栈拼接分隔符。 |

### 安全防护

- SQL 防护：`security.sql-guard.*`。
- XSS 过滤：`security.xss.*`。
- 限流：`security.rate-limit.*`。
- 重复提交：`security.duplicate-submit.*`。
- 排除路径会与 `security.common.exclude-paths` 合并。

### Log 模块常量覆盖（log.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/log/config/LogConstants.java`。
- 可通过配置文件 `log.constants.*` 覆盖默认行为；未配置项自动使用默认值。
- `List/Map` 类型建议按 Spring Boot 标准方式覆盖：
- `log.constants.aspect.default-exclude-params[0]=password`
- `log.constants.aspect.title-mappings.user=用户管理`
- `log.constants.ip.headers[0]=X-Forwarded-For`

**Controller 组**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `log.constants.controller.bad-request-code` | `400` | 参数非法场景错误码。 |
| `log.constants.controller.not-found-code` | `404` | 资源不存在场景错误码。 |
| `log.constants.controller.internal-server-error-code` | `500` | 服务执行失败场景错误码。 |

**Message 组**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `log.constants.message.common-delete-failed` | `common.delete.failed` | 删除失败时使用的 i18n 消息键。 |
| `log.constants.message.login-log-persist-failed` | `登录日志入库失败` | 登录日志异步入库异常日志模板。 |
| `log.constants.message.oper-log-persist-failed` | `操作日志入库失败` | 操作日志异步入库异常日志模板。 |
| `log.constants.message.spel-parse-failed` | `解析操作日志SpEL失败: {}` | SpEL 解析失败日志模板（第一个占位符为模板字符串）。 |

**Page / Query / Status 组**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `log.constants.page.default-page-num` | `1` | 查询分页对象为空时默认页码。 |
| `log.constants.page.default-page-size` | `10` | 查询分页对象为空时默认页大小。 |
| `log.constants.query.date-time-pattern` | `yyyy-MM-dd HH:mm:ss` | 查询时间字符串默认解析格式。 |
| `log.constants.status.oper-success` | `1` | 操作日志成功状态值。 |
| `log.constants.status.oper-failed` | `0` | 操作日志失败状态值。 |

**HTTP 组**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `log.constants.http.get-method` | `GET` | GET 方法名。 |
| `log.constants.http.options-method` | `OPTIONS` | OPTIONS 方法名。 |
| `log.constants.http.post-method` | `POST` | POST 方法名。 |
| `log.constants.http.put-method` | `PUT` | PUT 方法名。 |
| `log.constants.http.patch-method` | `PATCH` | PATCH 方法名。 |
| `log.constants.http.delete-method` | `DELETE` | DELETE 方法名。 |
| `log.constants.http.permission-separator` | `:` | 权限字符串分隔符（用于提取模块前缀）。 |
| `log.constants.http.method-url-separator` | ` ` | 无权限标识时，`method + separator + url` 的拼接分隔符。 |

**Aspect 组**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `log.constants.aspect.max-text-length` | `2000` | 操作参数/结果/错误文本统一截断长度。 |
| `log.constants.aspect.default-exclude-params` | `password,oldPassword,newPassword,token` | 注解未显式声明 `excludeParams` 时使用的脱敏字段列表。 |
| `log.constants.aspect.title-mappings.user` | `用户管理` | 权限前缀到 title 的映射项示例（其余键同理）。 |
| `log.constants.aspect.spel-pattern` | `#\{(.+?)}` | SpEL 占位符匹配正则。 |
| `log.constants.aspect.spel-null-literal` | `null` | SpEL 表达式值为 null 时的替换文本。 |
| `log.constants.aspect.mask-value` | `******` | 脱敏替换值。 |
| `log.constants.aspect.spring-validation-package-prefix` | `org.springframework.validation.` | 参数过滤时忽略的 Spring Validation 包前缀。 |
| `log.constants.aspect.spring-multipart-package-prefix` | `org.springframework.web.multipart.` | 参数过滤时忽略的 Spring Multipart 包前缀。 |

**IP 组**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `log.constants.ip.headers` | `X-Forwarded-For,X-Real-IP,Proxy-Client-IP,WL-Proxy-Client-IP,HTTP_CLIENT_IP,HTTP_X_FORWARDED_FOR` | 解析客户端 IP 时依次检查的代理头列表。 |
| `log.constants.ip.unknown-token` | `unknown` | 代理头中表示无效 IP 的占位值。 |
| `log.constants.ip.multi-ip-separator` | `,` | 多级代理 IP 串分隔符。 |
| `log.constants.ip.internal-ip-text` | `内网IP` | 内网地址解析结果文本。 |
| `log.constants.ip.unknown-location-text` | `未知` | 外网地址无法解析时返回文本。 |
| `log.constants.ip.ipv4-segment-separator-regex` | `\.` | IPv4 分段正则分隔符。 |
| `log.constants.ip.ipv4-loopback-prefix` | `127.` | IPv4 回环前缀。 |
| `log.constants.ip.ipv6-loopback-full` | `0:0:0:0:0:0:0:1` | IPv6 完整回环地址。 |
| `log.constants.ip.ipv6-loopback-short` | `::1` | IPv6 简写回环地址。 |
| `log.constants.ip.private-a-prefix` | `10.` | A 类私网前缀。 |
| `log.constants.ip.private-c-prefix` | `192.168.` | C 类私网前缀。 |
| `log.constants.ip.private-b-prefix` | `172.` | B 类私网前缀。 |
| `log.constants.ip.private-b-second-octet-min` | `16` | B 类私网第二段最小值。 |
| `log.constants.ip.private-b-second-octet-max` | `31` | B 类私网第二段最大值。 |

**User-Agent 组**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `log.constants.user-agent.unknown` | `Unknown` | 无法识别时的默认名称。 |
| `log.constants.user-agent.pc` | `PC` | 桌面设备默认名称。 |
| `log.constants.user-agent.browser-edge-token` | `edg/` | Edge 浏览器关键字。 |
| `log.constants.user-agent.browser-chrome-token` | `chrome/` | Chrome 浏览器关键字。 |
| `log.constants.user-agent.browser-firefox-token` | `firefox/` | Firefox 浏览器关键字。 |
| `log.constants.user-agent.browser-safari-token` | `safari/` | Safari 浏览器关键字。 |
| `log.constants.user-agent.browser-ie-token` | `msie` | IE 浏览器关键字（旧版）。 |
| `log.constants.user-agent.browser-trident-token` | `trident/` | IE 浏览器关键字（Trident）。 |
| `log.constants.user-agent.browser-edge-name` | `Edge` | Edge 命中后的浏览器名称。 |
| `log.constants.user-agent.browser-chrome-name` | `Chrome` | Chrome 命中后的浏览器名称。 |
| `log.constants.user-agent.browser-firefox-name` | `Firefox` | Firefox 命中后的浏览器名称。 |
| `log.constants.user-agent.browser-safari-name` | `Safari` | Safari 命中后的浏览器名称。 |
| `log.constants.user-agent.browser-ie-name` | `IE` | IE 命中后的浏览器名称。 |
| `log.constants.user-agent.os-windows-token` | `windows` | Windows 关键字。 |
| `log.constants.user-agent.os-mac-token` | `mac os x` | macOS 关键字。 |
| `log.constants.user-agent.os-android-token` | `android` | Android 关键字。 |
| `log.constants.user-agent.os-iphone-token` | `iphone` | iPhone 关键字。 |
| `log.constants.user-agent.os-ipad-token` | `ipad` | iPad 关键字。 |
| `log.constants.user-agent.os-ios-token` | `ios` | iOS 关键字。 |
| `log.constants.user-agent.os-linux-token` | `linux` | Linux 关键字。 |
| `log.constants.user-agent.os-windows-name` | `Windows` | Windows 命中后的系统名称。 |
| `log.constants.user-agent.os-mac-name` | `macOS` | macOS 命中后的系统名称。 |
| `log.constants.user-agent.os-android-name` | `Android` | Android 命中后的系统名称。 |
| `log.constants.user-agent.os-ios-name` | `iOS` | iOS 命中后的系统名称。 |
| `log.constants.user-agent.os-linux-name` | `Linux` | Linux 命中后的系统名称。 |
| `log.constants.user-agent.device-tablet-token` | `tablet` | 平板设备关键字。 |
| `log.constants.user-agent.device-mobile-token` | `mobile` | 手机设备关键字。 |
| `log.constants.user-agent.device-tablet-name` | `Tablet` | 平板命中后的设备类型名称。 |
| `log.constants.user-agent.device-mobile-name` | `Mobile` | 手机命中后的设备类型名称。 |

### Common 模块常量覆盖（common.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/common/config/CommonConstants.java`。
- 如需覆盖默认值，可在配置文件中使用 `common.constants.*`，未配置项自动使用常量类默认值。
- 建议：只覆盖确有业务需要的键，不建议整段复制。

**Common 组**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `common.constants.http.json-content-type` | `application/json;charset=UTF-8` | 认证/权限/限流/重复提交等 JSON 错误响应的 `Content-Type`。 |
| `common.constants.http.forwarded-for-header` | `X-Forwarded-For` | 解析客户端 IP 时优先读取的代理头。 |
| `common.constants.http.real-ip-header` | `X-Real-IP` | 解析客户端 IP 时的第二优先代理头。 |
| `common.constants.http.multipart-prefix` | `multipart/` | 判断是否为 multipart 请求的 `Content-Type` 前缀。 |
| `common.constants.http.idempotency-header-default` | `Idempotency-Key` | 幂等键默认请求头名（未显式配置时使用）。 |
| `common.constants.trace.mdc-key` | `traceId` | TraceId 写入 MDC 的键名。 |
| `common.constants.rate-limit.key-prefix` | `rl:` | 限流缓存键前缀。 |
| `common.constants.rate-limit.response-status` | `429` | 限流命中时的 HTTP 状态码。 |
| `common.constants.rate-limit.message-key` | `common.rate.limit.exceeded` | 限流命中时的 i18n 消息键。 |
| `common.constants.duplicate-submit.key-prefix` | `dup:` | 重复提交缓存键前缀。 |
| `common.constants.duplicate-submit.response-status` | `409` | 重复提交命中时的 HTTP 状态码。 |
| `common.constants.duplicate-submit.message-key` | `common.duplicate.submission` | 重复提交命中时的 i18n 消息键。 |
| `common.constants.duplicate-submit.key-idempotency-tag` | `k` | 重复提交键中幂等键片段标签。 |
| `common.constants.duplicate-submit.key-query-tag` | `q` | 重复提交键中 Query 片段标签。 |
| `common.constants.duplicate-submit.key-body-tag` | `b` | 重复提交键中 Body 摘要片段标签。 |
| `common.constants.permission.required-message-key` | `auth.permission.required` | 未登录/需登录时的 i18n 消息键。 |
| `common.constants.permission.denied-message-key` | `auth.permission.denied` | 权限不足时的 i18n 消息键。 |
| `common.constants.mybatis.datasource-url-property` | `spring.datasource.url` | MyBatis 自动识别 DB 类型时读取的数据源 URL 属性键。 |
| `common.constants.mybatis.postgres-token` | `:postgresql:` | JDBC URL 中识别 PostgreSQL 的标识片段。 |
| `common.constants.mybatis.mysql-token` | `:mysql:` | JDBC URL 中识别 MySQL 的标识片段。 |
| `common.constants.mybatis.mariadb-token` | `:mariadb:` | JDBC URL 中识别 MariaDB 的标识片段。 |
| `common.constants.mybatis.oracle-token` | `:oracle:` | JDBC URL 中识别 Oracle 的标识片段。 |
| `common.constants.i18n.basename` | `classpath:i18n/messages` | i18n 资源基名。 |
| `common.constants.i18n.default-encoding` | `UTF-8` | i18n 资源默认编码。 |
| `common.constants.i18n.fallback-to-system-locale` | `false` | i18n 未命中时是否回退系统 Locale。 |
| `common.constants.i18n.use-code-as-default-message` | `true` | i18n 未命中时是否回退消息键本身。 |
| `common.constants.i18n.default-locale-tag` | `zh-CN` | 默认 Locale（语言标签）。 |
| `common.constants.mdc.thread-name-prefix` | `mdc-thread-` | MDC 线程工厂默认线程名前缀。 |
| `common.constants.cache.memory-cleanup-thread-prefix` | `cache-cleanup` | 内存缓存清理线程名前缀。 |
| `common.constants.cache.db-cleanup-thread-prefix` | `cache-db-cleanup` | 数据库缓存清理线程名前缀。 |

### Auth 业务配置（auth.password）

- 用于控制密码传输、强度、首次登录改密与过期策略。
- 默认值定义在 `src/main/java/com/example/demo/auth/config/AuthProperties.java`，可在配置文件通过 `auth.password.*` 覆盖。

| 配置键                                         | 默认值    | 说明                                               |
|---------------------------------------------|--------|--------------------------------------------------|
| `auth.password.force-change-on-first-login` | `true` | 是否启用首次登录强制修改密码。启用时，新建用户/管理员重置密码后需要先改密才能继续访问业务接口。 |
| `auth.password.expire-days`                 | `120`  | 密码过期天数。`<=0` 表示不启用过期策略；大于 0 时，超过该天数会强制先改密。       |

### Auth 模块常量覆盖（auth.constants）

- 默认值集中定义在 `src/main/java/com/example/demo/auth/config/AuthConstants.java`。
- 如需覆盖默认值，可在配置文件中使用 `auth.constants.*`，未配置项自动使用常量类默认值。
- 建议：只覆盖确有业务需要的键，不建议整段复制。

**Auth Token / Filter**

| 配置键                                                          | 默认值                             | 说明                       |
|--------------------------------------------------------------|---------------------------------|--------------------------|
| `auth.constants.token.authorization-header`                  | `Authorization`                 | 主令牌请求头名。                 |
| `auth.constants.token.fallback-token-header`                 | `X-Auth-Token`                  | 备用令牌请求头名。                |
| `auth.constants.token.query-token-parameter`                 | `token`                         | Query 参数传递令牌时的参数名。       |
| `auth.constants.token.bearer-prefix`                         | `Bearer `                       | Bearer 前缀（含空格）。          |
| `auth.constants.token.token-type`                            | `Bearer`                        | 登录响应中的 `tokenType`。      |
| `auth.constants.token.jwt-header-alg-key`                    | `alg`                           | JWT Header 算法字段名。        |
| `auth.constants.token.jwt-header-type-key`                   | `typ`                           | JWT Header 类型字段名。        |
| `auth.constants.token.jwt-header-alg-value`                  | `HS256`                         | JWT Header 算法字段值。        |
| `auth.constants.token.jwt-header-type-value`                 | `JWT`                           | JWT Header 类型字段值。        |
| `auth.constants.token.jwt-claim-subject`                     | `sub`                           | JWT Subject Claim 键名。    |
| `auth.constants.token.jwt-claim-user-id`                     | `uid`                           | JWT 用户 ID Claim 键名。      |
| `auth.constants.token.jwt-claim-issued-at`                   | `iat`                           | JWT 签发时间 Claim 键名。       |
| `auth.constants.token.jwt-claim-expires-at`                  | `exp`                           | JWT 过期时间 Claim 键名。       |
| `auth.constants.token.jwt-claim-jwt-id`                      | `jti`                           | JWT 唯一 ID Claim 键名。      |
| `auth.constants.token.sign-algorithm`                        | `HmacSHA256`                    | JWT 签名算法。                |
| `auth.constants.token.store-key-prefix`                      | `auth:token:`                   | TokenStore 缓存键前缀。        |
| `auth.constants.filter.options-method`                       | `OPTIONS`                       | AuthTokenFilter 放行的预检方法。 |
| `auth.constants.filter.token-missing-message-key`            | `auth.token.missing`            | 缺失令牌消息键。                 |
| `auth.constants.filter.token-invalid-message-key`            | `auth.token.invalid`            | 无效令牌消息键。                 |
| `auth.constants.filter.user-invalid-message-key`             | `auth.user.invalid`             | 用户信息无效消息键。               |
| `auth.constants.filter.user-not-found-message-key`           | `auth.user.not.found`           | 用户不存在消息键。                |
| `auth.constants.filter.user-disabled-message-key`            | `auth.user.disabled`            | 用户被禁用消息键。                |
| `auth.constants.filter.password-change-required-message-key` | `auth.password.change.required` | 强制改密时拦截返回的消息键。           |
| `auth.constants.filter.password-change-profile-path`         | `/auth/profile`                 | 强制改密状态下允许访问的个人信息接口路径。    |
| `auth.constants.filter.password-change-logout-path`          | `/auth/logout`                  | 强制改密状态下允许访问的登出接口路径。      |
| `auth.constants.filter.get-method`                           | `GET`                           | 强制改密白名单路径匹配的 GET 方法名。    |
| `auth.constants.filter.put-method`                           | `PUT`                           | 强制改密白名单路径匹配的 PUT 方法名。    |
| `auth.constants.filter.post-method`                          | `POST`                          | 强制改密白名单路径匹配的 POST 方法名。   |

**Auth Captcha / Login Attempt**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `auth.constants.captcha.image-prefix` | `data:image/png;base64,` | 验证码 Base64 Data URL 前缀。 |
| `auth.constants.captcha.code-charset` | `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` | 验证码字符集。 |
| `auth.constants.captcha.png-format` | `png` | 验证码输出图片格式。 |
| `auth.constants.captcha.fallback-font-family` | `SansSerif` | 未加载嵌入字体时的字体族。 |
| `auth.constants.captcha.noise-dot-min-count` | `20` | 验证码噪点最小数量。 |
| `auth.constants.captcha.noise-dot-density-divisor` | `150` | 噪点密度分母（`width*height/divisor`）。 |
| `auth.constants.captcha.font-min-size` | `18` | 验证码最小字体大小。 |
| `auth.constants.captcha.font-padding` | `10` | 验证码字体高度预留边距。 |
| `auth.constants.captcha.char-color-min` | `30` | 验证码字符颜色随机最小值。 |
| `auth.constants.captcha.char-color-max` | `160` | 验证码字符颜色随机最大值。 |
| `auth.constants.captcha.line-dot-color-min` | `120` | 干扰线/噪点颜色随机最小值。 |
| `auth.constants.captcha.line-dot-color-max` | `200` | 干扰线/噪点颜色随机最大值。 |
| `auth.constants.captcha.font-resource-classpath-prefix` | `classpath:` | 验证码字体 classpath 前缀。 |
| `auth.constants.captcha.store-key-prefix` | `auth:captcha:` | CaptchaStore 缓存键前缀。 |
| `auth.constants.login-attempt.fail-key-prefix` | `auth:login:fail:` | 登录失败计数缓存键前缀。 |
| `auth.constants.login-attempt.lock-key-prefix` | `auth:login:lock:` | 登录锁定缓存键前缀。 |
| `auth.constants.login-attempt.mode-ip` | `ip` | 登录失败统计：按 IP 模式标识。 |
| `auth.constants.login-attempt.mode-ip-user` | `ip-user` | 登录失败统计：IP+用户 模式标识。 |
| `auth.constants.login-attempt.mode-user-ip` | `user-ip` | 登录失败统计：用户+IP 模式标识。 |
| `auth.constants.login-attempt.mode-fallback` | `user` | 未配置时默认统计模式。 |

**Auth Password / Controller**

| 配置键 | 默认值 | 说明 |
|---|---|---|
| `auth.constants.password.mode-fallback` | `plain` | 密码模式兜底值。 |
| `auth.constants.password.mode-bcrypt` | `bcrypt` | bcrypt 模式标识。 |
| `auth.constants.password.mode-sm3` | `sm3` | sm3 模式标识。 |
| `auth.constants.password.transport-mode-aes` | `aes` | AES 传输模式标识。 |
| `auth.constants.password.transport-mode-aes-gcm` | `aes-gcm` | AES-GCM 传输模式标识。 |
| `auth.constants.password.transport-mode-base64` | `base64` | Base64 传输模式标识。 |
| `auth.constants.password.transport-mode-sm2` | `sm2` | SM2 传输模式标识。 |
| `auth.constants.password.transport-split-delimiter` | `:` | AES 密文分隔符（`iv:cipher`）。 |
| `auth.constants.password.transport-split-limit` | `2` | AES 密文分割段数上限。 |
| `auth.constants.password.aes-key-algorithm` | `AES` | AES 密钥算法名。 |
| `auth.constants.password.aes-transformation` | `AES/GCM/NoPadding` | AES 解密 transformation。 |
| `auth.constants.password.aes-gcm-tag-length-bits` | `128` | AES-GCM Tag 位数。 |
| `auth.constants.profile.new-password-min-length` | `6` | 个人资料修改密码最小长度。 |
| `auth.constants.profile.user-agent-header` | `User-Agent` | 登录日志读取 UA 的请求头名。 |
| `auth.constants.login-log.type-login` | `1` | 登录日志：登录类型编码。 |
| `auth.constants.login-log.type-logout` | `2` | 登录日志：登出类型编码。 |
| `auth.constants.login-log.status-fail` | `0` | 登录日志：失败状态编码。 |
| `auth.constants.login-log.status-success` | `1` | 登录日志：成功状态编码。 |
| `auth.constants.controller.bad-request-code` | `400` | AuthController 参数错误状态码。 |
| `auth.constants.controller.unauthorized-code` | `401` | AuthController 未授权状态码。 |
| `auth.constants.controller.forbidden-code` | `403` | AuthTokenFilter 禁止访问状态码。 |
| `auth.constants.controller.not-found-code` | `404` | AuthController 资源不存在状态码。 |
| `auth.constants.controller.too-many-requests-code` | `429` | AuthController 频率限制状态码。 |
| `auth.constants.controller.internal-server-error-code` | `500` | AuthController 服务异常状态码。 |

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
