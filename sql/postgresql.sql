CREATE SEQUENCE IF NOT EXISTS sys_user_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user
(
    id               BIGINT PRIMARY KEY    DEFAULT nextval('sys_user_id_seq'),
    user_name        VARCHAR(64)  NOT NULL,
    nick_name        VARCHAR(64),
    phone            VARCHAR(32),
    email            VARCHAR(128),
    password         VARCHAR(128) NOT NULL,
    status           SMALLINT     NOT NULL DEFAULT 1,
    dept_id          BIGINT,
    data_scope_type  VARCHAR(32),
    data_scope_value VARCHAR(512),
    sex              VARCHAR(16),
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by        VARCHAR(64),
    update_by        VARCHAR(64),
    is_deleted       SMALLINT     NOT NULL DEFAULT 0,
    version          INT          NOT NULL DEFAULT 0,
    remark           VARCHAR(500)
);
ALTER SEQUENCE sys_user_id_seq OWNED BY sys_user.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_user_name ON sys_user (user_name, is_deleted);
COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.id IS '主键ID';
COMMENT ON COLUMN sys_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_user.update_time IS '更新时间';
COMMENT ON COLUMN sys_user.create_by IS '创建人';
COMMENT ON COLUMN sys_user.update_by IS '更新人';
COMMENT ON COLUMN sys_user.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_user.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_user.remark IS '备注';
COMMENT ON COLUMN sys_user.user_name IS '用户名（唯一）';
COMMENT ON COLUMN sys_user.nick_name IS '昵称';
COMMENT ON COLUMN sys_user.phone IS '手机号码';
COMMENT ON COLUMN sys_user.email IS '用户邮箱';
COMMENT ON COLUMN sys_user.password IS '登录密码（加密存储）';
COMMENT ON COLUMN sys_user.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN sys_user.data_scope_type IS '数据范围类型：ALL全量/SELF仅本人/CUSTOM自定义/NONE无数据';
COMMENT ON COLUMN sys_user.data_scope_value IS '数据范围值，CUSTOM时存储自定义范围内容（如ID列表）';
COMMENT ON COLUMN sys_user.sex IS '性别';
CREATE INDEX IF NOT EXISTS idx_sys_user_dept ON sys_user (dept_id);

CREATE SEQUENCE IF NOT EXISTS sys_dept_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_dept
(
    id          BIGINT PRIMARY KEY    DEFAULT nextval('sys_dept_id_seq'),
    name        VARCHAR(128) NOT NULL,
    code        VARCHAR(64),
    parent_id   BIGINT,
    status      SMALLINT     NOT NULL DEFAULT 1,
    sort        INTEGER      NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_dept_id_seq OWNED BY sys_dept.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dept_code ON sys_dept (code, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_dept_parent ON sys_dept (parent_id);
COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.id IS '主键ID';
COMMENT ON COLUMN sys_dept.create_time IS '创建时间';
COMMENT ON COLUMN sys_dept.update_time IS '更新时间';
COMMENT ON COLUMN sys_dept.create_by IS '创建人';
COMMENT ON COLUMN sys_dept.update_by IS '更新人';
COMMENT ON COLUMN sys_dept.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_dept.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_dept.remark IS '备注';
COMMENT ON COLUMN sys_dept.name IS '部门名称';
COMMENT ON COLUMN sys_dept.code IS '部门编码（唯一）';
COMMENT ON COLUMN sys_dept.parent_id IS '上级部门ID';
COMMENT ON COLUMN sys_dept.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_dept.sort IS '排序';

CREATE SEQUENCE IF NOT EXISTS sys_role_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_role
(
    id               BIGINT PRIMARY KEY    DEFAULT nextval('sys_role_id_seq'),
    code             VARCHAR(64)  NOT NULL,
    name             VARCHAR(128) NOT NULL,
    status           SMALLINT     NOT NULL DEFAULT 1,
    data_scope_type  VARCHAR(32),
    data_scope_value VARCHAR(512),
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by        VARCHAR(64),
    update_by        VARCHAR(64),
    is_deleted       SMALLINT     NOT NULL DEFAULT 0,
    version          INT          NOT NULL DEFAULT 0,
    remark           VARCHAR(500)
);
ALTER SEQUENCE sys_role_id_seq OWNED BY sys_role.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role (code, is_deleted);
COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.id IS '主键ID';
COMMENT ON COLUMN sys_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_role.create_by IS '创建人';
COMMENT ON COLUMN sys_role.update_by IS '更新人';
COMMENT ON COLUMN sys_role.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_role.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_role.remark IS '备注';
COMMENT ON COLUMN sys_role.code IS '角色编码（唯一）';
COMMENT ON COLUMN sys_role.name IS '角色名称';
COMMENT ON COLUMN sys_role.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_role.data_scope_type IS '数据范围类型：ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE';
COMMENT ON COLUMN sys_role.data_scope_value IS '数据范围值，CUSTOM_DEPT时存储部门ID列表';

CREATE SEQUENCE IF NOT EXISTS sys_permission_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_permission
(
    id          BIGINT PRIMARY KEY    DEFAULT nextval('sys_permission_id_seq'),
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_permission_id_seq OWNED BY sys_permission.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_code ON sys_permission (code, is_deleted);
COMMENT ON TABLE sys_permission IS '权限表';
COMMENT ON COLUMN sys_permission.id IS '主键ID';
COMMENT ON COLUMN sys_permission.create_time IS '创建时间';
COMMENT ON COLUMN sys_permission.update_time IS '更新时间';
COMMENT ON COLUMN sys_permission.create_by IS '创建人';
COMMENT ON COLUMN sys_permission.update_by IS '更新人';
COMMENT ON COLUMN sys_permission.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_permission.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_permission.remark IS '备注';
COMMENT ON COLUMN sys_permission.code IS '权限编码（唯一）';
COMMENT ON COLUMN sys_permission.name IS '权限名称';
COMMENT ON COLUMN sys_permission.status IS '状态：1-启用，0-禁用';

CREATE SEQUENCE IF NOT EXISTS sys_menu_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_menu
(
    id          BIGINT PRIMARY KEY    DEFAULT nextval('sys_menu_id_seq'),
    name        VARCHAR(128) NOT NULL,
    code        VARCHAR(64),
    parent_id   BIGINT,
    path        VARCHAR(255),
    component   VARCHAR(255),
    permission  VARCHAR(64),
    status      SMALLINT     NOT NULL DEFAULT 1,
    sort        INTEGER      NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_menu_id_seq OWNED BY sys_menu.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_menu_code ON sys_menu (code, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent ON sys_menu (parent_id);
COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.id IS '主键ID';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间';
COMMENT ON COLUMN sys_menu.create_by IS '创建人';
COMMENT ON COLUMN sys_menu.update_by IS '更新人';
COMMENT ON COLUMN sys_menu.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_menu.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_menu.remark IS '备注';
COMMENT ON COLUMN sys_menu.name IS '菜单名称';
COMMENT ON COLUMN sys_menu.code IS '菜单编码（唯一）';
COMMENT ON COLUMN sys_menu.parent_id IS '上级菜单ID';
COMMENT ON COLUMN sys_menu.path IS '路由路径';
COMMENT ON COLUMN sys_menu.component IS '前端组件';
COMMENT ON COLUMN sys_menu.permission IS '菜单权限标识';
COMMENT ON COLUMN sys_menu.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_menu.sort IS '排序';

CREATE SEQUENCE IF NOT EXISTS sys_role_permission_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_role_permission
(
    id            BIGINT PRIMARY KEY DEFAULT nextval('sys_role_permission_id_seq'),
    role_id       BIGINT    NOT NULL,
    permission_id BIGINT    NOT NULL,
    create_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by     VARCHAR(64),
    update_by     VARCHAR(64),
    is_deleted    SMALLINT  NOT NULL DEFAULT 0,
    version       INT       NOT NULL DEFAULT 0,
    remark        VARCHAR(500)
);
ALTER SEQUENCE sys_role_permission_id_seq OWNED BY sys_role_permission.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_permission_role_perm ON sys_role_permission (role_id, permission_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_role ON sys_role_permission (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_perm ON sys_role_permission (permission_id);
COMMENT ON TABLE sys_role_permission IS '角色-权限关联表';
COMMENT ON COLUMN sys_role_permission.id IS '主键ID';
COMMENT ON COLUMN sys_role_permission.create_time IS '创建时间';
COMMENT ON COLUMN sys_role_permission.update_time IS '更新时间';
COMMENT ON COLUMN sys_role_permission.create_by IS '创建人';
COMMENT ON COLUMN sys_role_permission.update_by IS '更新人';
COMMENT ON COLUMN sys_role_permission.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_role_permission.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_role_permission.remark IS '备注';
COMMENT ON COLUMN sys_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_permission.permission_id IS '权限ID';

CREATE SEQUENCE IF NOT EXISTS sys_role_menu_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_role_menu
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('sys_role_menu_id_seq'),
    role_id     BIGINT    NOT NULL,
    menu_id     BIGINT    NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    is_deleted  SMALLINT  NOT NULL DEFAULT 0,
    version     INT       NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_role_menu_id_seq OWNED BY sys_role_menu.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_menu_role_menu ON sys_role_menu (role_id, menu_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role ON sys_role_menu (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu ON sys_role_menu (menu_id);
COMMENT ON TABLE sys_role_menu IS '角色-菜单关联表';
COMMENT ON COLUMN sys_role_menu.id IS '主键ID';
COMMENT ON COLUMN sys_role_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_role_menu.update_time IS '更新时间';
COMMENT ON COLUMN sys_role_menu.create_by IS '创建人';
COMMENT ON COLUMN sys_role_menu.update_by IS '更新人';
COMMENT ON COLUMN sys_role_menu.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_role_menu.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_role_menu.remark IS '备注';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';

CREATE SEQUENCE IF NOT EXISTS sys_user_role_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user_role
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('sys_user_role_id_seq'),
    user_id     BIGINT    NOT NULL,
    role_id     BIGINT    NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    is_deleted  SMALLINT  NOT NULL DEFAULT 0,
    version     INT       NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_user_role_id_seq OWNED BY sys_user_role.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_role_user_role ON sys_user_role (user_id, role_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_user ON sys_user_role (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role ON sys_user_role (role_id);
COMMENT ON TABLE sys_user_role IS '用户-角色关联表';
COMMENT ON COLUMN sys_user_role.id IS '主键ID';
COMMENT ON COLUMN sys_user_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_user_role.create_by IS '创建人';
COMMENT ON COLUMN sys_user_role.update_by IS '更新人';
COMMENT ON COLUMN sys_user_role.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_user_role.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_user_role.remark IS '备注';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

CREATE SEQUENCE IF NOT EXISTS sys_data_scope_rule_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_data_scope_rule
(
    id          BIGINT PRIMARY KEY   DEFAULT nextval('sys_data_scope_rule_id_seq'),
    table_name  VARCHAR(64) NOT NULL,
    column_name VARCHAR(64) NOT NULL,
    enabled     SMALLINT    NOT NULL DEFAULT 1,
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    is_deleted  SMALLINT    NOT NULL DEFAULT 0,
    version     INT         NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_data_scope_rule_id_seq OWNED BY sys_data_scope_rule.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_data_scope_rule_table ON sys_data_scope_rule (table_name, is_deleted);
COMMENT ON TABLE sys_data_scope_rule IS '数据范围规则表';
COMMENT ON COLUMN sys_data_scope_rule.id IS '主键ID';
COMMENT ON COLUMN sys_data_scope_rule.create_time IS '创建时间';
COMMENT ON COLUMN sys_data_scope_rule.update_time IS '更新时间';
COMMENT ON COLUMN sys_data_scope_rule.create_by IS '创建人';
COMMENT ON COLUMN sys_data_scope_rule.update_by IS '更新人';
COMMENT ON COLUMN sys_data_scope_rule.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_data_scope_rule.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_data_scope_rule.remark IS '备注';
COMMENT ON COLUMN sys_data_scope_rule.table_name IS '目标表名（小写匹配）';
COMMENT ON COLUMN sys_data_scope_rule.column_name IS '数据范围字段名';
COMMENT ON COLUMN sys_data_scope_rule.enabled IS '是否启用：1-启用，0-禁用';

CREATE SEQUENCE IF NOT EXISTS sys_order_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_order
(
    id          BIGINT PRIMARY KEY      DEFAULT nextval('sys_order_id_seq'),
    user_id     BIGINT         NOT NULL,
    amount      DECIMAL(18, 2) NOT NULL,
    create_time TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    is_deleted  SMALLINT       NOT NULL DEFAULT 0,
    version     INT            NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_order_id_seq OWNED BY sys_order.id;
CREATE INDEX IF NOT EXISTS idx_sys_order_user ON sys_order (user_id);
COMMENT ON TABLE sys_order IS '订单表';
COMMENT ON COLUMN sys_order.id IS '主键ID';
COMMENT ON COLUMN sys_order.create_time IS '创建时间';
COMMENT ON COLUMN sys_order.update_time IS '更新时间';
COMMENT ON COLUMN sys_order.create_by IS '创建人';
COMMENT ON COLUMN sys_order.update_by IS '更新人';
COMMENT ON COLUMN sys_order.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_order.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_order.remark IS '备注';
COMMENT ON COLUMN sys_order.user_id IS '用户ID';
COMMENT ON COLUMN sys_order.amount IS '订单金额';

CREATE TABLE IF NOT EXISTS sys_cache
(
    cache_key   VARCHAR(255) PRIMARY KEY,
    cache_value TEXT,
    value_class VARCHAR(255),
    expire_at   BIGINT
);
CREATE INDEX IF NOT EXISTS idx_sys_cache_expire_at ON sys_cache (expire_at);
COMMENT ON TABLE sys_cache IS '缓存表';
COMMENT ON COLUMN sys_cache.cache_key IS '缓存键';
COMMENT ON COLUMN sys_cache.cache_value IS '缓存内容（JSON）';
COMMENT ON COLUMN sys_cache.value_class IS '值类型名称';
COMMENT ON COLUMN sys_cache.expire_at IS '过期时间（毫秒时间戳）';

CREATE SEQUENCE IF NOT EXISTS sys_notice_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_notice
(
    id           BIGINT PRIMARY KEY DEFAULT nextval('sys_notice_id_seq'),
    title        VARCHAR(200) NOT NULL,
    content      TEXT         NOT NULL,
    scope_type   VARCHAR(32)  NOT NULL,
    scope_value  VARCHAR(1024),
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by    VARCHAR(64),
    update_by    VARCHAR(64),
    created_name VARCHAR(64),
    is_deleted   SMALLINT     NOT NULL DEFAULT 0,
    version      INT          NOT NULL DEFAULT 0,
    remark       VARCHAR(500)
);
ALTER SEQUENCE sys_notice_id_seq OWNED BY sys_notice.id;
CREATE INDEX IF NOT EXISTS idx_sys_notice_create_time ON sys_notice (create_time);
COMMENT ON TABLE sys_notice IS '系统通知表';
COMMENT ON COLUMN sys_notice.id IS '主键ID';
COMMENT ON COLUMN sys_notice.create_time IS '创建时间';
COMMENT ON COLUMN sys_notice.update_time IS '更新时间';
COMMENT ON COLUMN sys_notice.create_by IS '创建人';
COMMENT ON COLUMN sys_notice.update_by IS '更新人';
COMMENT ON COLUMN sys_notice.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_notice.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_notice.remark IS '备注';
COMMENT ON COLUMN sys_notice.title IS '通知标题';
COMMENT ON COLUMN sys_notice.content IS '通知内容';
COMMENT ON COLUMN sys_notice.scope_type IS '通知范围类型';
COMMENT ON COLUMN sys_notice.scope_value IS '通知范围值（ID列表）';
COMMENT ON COLUMN sys_notice.created_name IS '创建人名称';

CREATE SEQUENCE IF NOT EXISTS sys_notice_recipient_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_notice_recipient
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('sys_notice_recipient_id_seq'),
    notice_id   BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    read_status SMALLINT  NOT NULL DEFAULT 0,
    read_time   TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    is_deleted  SMALLINT  NOT NULL DEFAULT 0,
    version     INT       NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_notice_recipient_id_seq OWNED BY sys_notice_recipient.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_notice_recipient_notice_user ON sys_notice_recipient (notice_id, user_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_notice_recipient_notice ON sys_notice_recipient (notice_id);
CREATE INDEX IF NOT EXISTS idx_sys_notice_recipient_user ON sys_notice_recipient (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_notice_recipient_read ON sys_notice_recipient (read_status);
CREATE INDEX IF NOT EXISTS idx_sys_notice_recipient_user_read ON sys_notice_recipient (user_id, read_status, is_deleted);
COMMENT ON TABLE sys_notice_recipient IS '系统通知接收表';
COMMENT ON COLUMN sys_notice_recipient.id IS '主键ID';
COMMENT ON COLUMN sys_notice_recipient.create_time IS '创建时间';
COMMENT ON COLUMN sys_notice_recipient.update_time IS '更新时间';
COMMENT ON COLUMN sys_notice_recipient.create_by IS '创建人';
COMMENT ON COLUMN sys_notice_recipient.update_by IS '更新人';
COMMENT ON COLUMN sys_notice_recipient.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_notice_recipient.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_notice_recipient.remark IS '备注';
COMMENT ON COLUMN sys_notice_recipient.notice_id IS '通知ID';
COMMENT ON COLUMN sys_notice_recipient.user_id IS '接收用户ID';
COMMENT ON COLUMN sys_notice_recipient.read_status IS '阅读状态：0-未读，1-已读';
COMMENT ON COLUMN sys_notice_recipient.read_time IS '阅读时间';

CREATE SEQUENCE IF NOT EXISTS sys_job_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_job
(
    id               BIGINT PRIMARY KEY    DEFAULT nextval('sys_job_id_seq'),
    name             VARCHAR(128) NOT NULL,
    handler_name     VARCHAR(128) NOT NULL,
    cron_expression  VARCHAR(128) NOT NULL,
    status           SMALLINT     NOT NULL DEFAULT 1,
    allow_concurrent SMALLINT     NOT NULL DEFAULT 1,
    misfire_policy   VARCHAR(32)           DEFAULT 'DEFAULT',
    params           TEXT,
    created_by       BIGINT,
    created_name     VARCHAR(64),
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP,
    remark           VARCHAR(255)
);
ALTER SEQUENCE sys_job_id_seq OWNED BY sys_job.id;
CREATE INDEX IF NOT EXISTS idx_sys_job_status ON sys_job (status);
CREATE INDEX IF NOT EXISTS idx_sys_job_handler ON sys_job (handler_name);
COMMENT ON TABLE sys_job IS '定时任务表';
COMMENT ON COLUMN sys_job.id IS '主键ID';
COMMENT ON COLUMN sys_job.name IS '任务名称';
COMMENT ON COLUMN sys_job.handler_name IS '处理器名称';
COMMENT ON COLUMN sys_job.cron_expression IS 'Cron表达式';
COMMENT ON COLUMN sys_job.status IS '状态：1-启用，0-停用';
COMMENT ON COLUMN sys_job.allow_concurrent IS '是否允许并发：1-允许，0-禁止';
COMMENT ON COLUMN sys_job.misfire_policy IS '误触发策略';
COMMENT ON COLUMN sys_job.params IS '任务参数';
COMMENT ON COLUMN sys_job.remark IS '备注';
COMMENT ON COLUMN sys_job.created_by IS '创建人ID';
COMMENT ON COLUMN sys_job.created_name IS '创建人名称';
COMMENT ON COLUMN sys_job.created_at IS '创建时间';
COMMENT ON COLUMN sys_job.updated_at IS '更新时间';

CREATE SEQUENCE IF NOT EXISTS sys_job_log_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_job_log
(
    id           BIGINT PRIMARY KEY DEFAULT nextval('sys_job_log_id_seq'),
    job_id       BIGINT       NOT NULL,
    job_name     VARCHAR(128) NOT NULL,
    handler_name VARCHAR(128) NOT NULL,
    status       SMALLINT     NOT NULL,
    message      VARCHAR(512),
    start_time   TIMESTAMP    NOT NULL,
    end_time     TIMESTAMP,
    duration_ms  BIGINT
);
ALTER SEQUENCE sys_job_log_id_seq OWNED BY sys_job_log.id;
CREATE INDEX IF NOT EXISTS idx_sys_job_log_job ON sys_job_log (job_id);
CREATE INDEX IF NOT EXISTS idx_sys_job_log_start ON sys_job_log (start_time);
COMMENT ON TABLE sys_job_log IS '定时任务日志表';
COMMENT ON COLUMN sys_job_log.id IS '主键ID';
COMMENT ON COLUMN sys_job_log.job_id IS '任务ID';
COMMENT ON COLUMN sys_job_log.job_name IS '任务名称';
COMMENT ON COLUMN sys_job_log.handler_name IS '处理器名称';
COMMENT ON COLUMN sys_job_log.status IS '执行状态：1-成功，0-失败';
COMMENT ON COLUMN sys_job_log.message IS '执行信息';
COMMENT ON COLUMN sys_job_log.start_time IS '开始时间';
COMMENT ON COLUMN sys_job_log.end_time IS '结束时间';
COMMENT ON COLUMN sys_job_log.duration_ms IS '耗时毫秒';

create table if not exists sys_quartz_job_details
(
    sched_name        varchar(120) not null,
    job_name          varchar(200) not null,
    job_group         varchar(200) not null,
    description       varchar(250) null,
    job_class_name    varchar(250) not null,
    is_durable        boolean      not null,
    is_nonconcurrent  boolean      not null,
    is_update_data    boolean      not null,
    requests_recovery boolean      not null,
    job_data          bytea        null,
    primary key (sched_name, job_name, job_group)
);

create table if not exists sys_quartz_triggers
(
    sched_name     varchar(120) not null,
    trigger_name   varchar(200) not null,
    trigger_group  varchar(200) not null,
    job_name       varchar(200) not null,
    job_group      varchar(200) not null,
    description    varchar(250) null,
    next_fire_time bigint       null,
    prev_fire_time bigint       null,
    priority       integer      null,
    trigger_state  varchar(16)  not null,
    trigger_type   varchar(8)   not null,
    start_time     bigint       not null,
    end_time       bigint       null,
    calendar_name  varchar(200) null,
    misfire_instr  smallint     null,
    job_data       bytea        null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group)
        references sys_quartz_job_details (sched_name, job_name, job_group)
);

create table if not exists sys_quartz_simple_triggers
(
    sched_name      varchar(120) not null,
    trigger_name    varchar(200) not null,
    trigger_group   varchar(200) not null,
    repeat_count    bigint       not null,
    repeat_interval bigint       not null,
    times_triggered bigint       not null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references sys_quartz_triggers (sched_name, trigger_name, trigger_group)
);

create table if not exists sys_quartz_cron_triggers
(
    sched_name      varchar(120) not null,
    trigger_name    varchar(200) not null,
    trigger_group   varchar(200) not null,
    cron_expression varchar(200) not null,
    time_zone_id    varchar(80),
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references sys_quartz_triggers (sched_name, trigger_name, trigger_group)
);

create table if not exists sys_quartz_simprop_triggers
(
    sched_name    varchar(120)   not null,
    trigger_name  varchar(200)   not null,
    trigger_group varchar(200)   not null,
    str_prop_1    varchar(512)   null,
    str_prop_2    varchar(512)   null,
    str_prop_3    varchar(512)   null,
    int_prop_1    int            null,
    int_prop_2    int            null,
    long_prop_1   bigint         null,
    long_prop_2   bigint         null,
    dec_prop_1    decimal(13, 4) null,
    dec_prop_2    decimal(13, 4) null,
    bool_prop_1   varchar(1)     null,
    bool_prop_2   varchar(1)     null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references sys_quartz_triggers (sched_name, trigger_name, trigger_group)
);

create table if not exists sys_quartz_blob_triggers
(
    sched_name    varchar(120) not null,
    trigger_name  varchar(200) not null,
    trigger_group varchar(200) not null,
    blob_data     bytea        null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references sys_quartz_triggers (sched_name, trigger_name, trigger_group)
);

create table if not exists sys_quartz_calendars
(
    sched_name    varchar(120) not null,
    calendar_name varchar(200) not null,
    calendar      bytea        not null,
    primary key (sched_name, calendar_name)
);

create table if not exists sys_quartz_paused_trigger_grps
(
    sched_name    varchar(120) not null,
    trigger_group varchar(200) not null,
    primary key (sched_name, trigger_group)
);

create table if not exists sys_quartz_fired_triggers
(
    sched_name        varchar(120) not null,
    entry_id          varchar(95)  not null,
    trigger_name      varchar(200) not null,
    trigger_group     varchar(200) not null,
    instance_name     varchar(200) not null,
    fired_time        bigint       not null,
    sched_time        bigint       not null,
    priority          integer      not null,
    state             varchar(16)  not null,
    job_name          varchar(200) null,
    job_group         varchar(200) null,
    is_nonconcurrent  boolean      null,
    requests_recovery boolean      null,
    primary key (sched_name, entry_id)
);

create table if not exists sys_quartz_scheduler_state
(
    sched_name        varchar(120) not null,
    instance_name     varchar(200) not null,
    last_checkin_time bigint       not null,
    checkin_interval  bigint       not null,
    primary key (sched_name, instance_name)
);

create table if not exists sys_quartz_locks
(
    sched_name varchar(120) not null,
    lock_name  varchar(40)  not null,
    primary key (sched_name, lock_name)
);

create index if not exists idx_sys_quartz_j_req_recovery on sys_quartz_job_details (sched_name, requests_recovery);
create index if not exists idx_sys_quartz_j_grp on sys_quartz_job_details (sched_name, job_group);
create index if not exists idx_sys_quartz_t_j on sys_quartz_triggers (sched_name, job_name, job_group);
create index if not exists idx_sys_quartz_t_jg on sys_quartz_triggers (sched_name, job_group);
create index if not exists idx_sys_quartz_t_c on sys_quartz_triggers (sched_name, calendar_name);
create index if not exists idx_sys_quartz_t_g on sys_quartz_triggers (sched_name, trigger_group);
create index if not exists idx_sys_quartz_t_state on sys_quartz_triggers (sched_name, trigger_state);
create index if not exists idx_sys_quartz_t_n_state on sys_quartz_triggers (sched_name, trigger_name, trigger_group, trigger_state);
create index if not exists idx_sys_quartz_t_n_g_state on sys_quartz_triggers (sched_name, trigger_group, trigger_state);
create index if not exists idx_sys_quartz_t_next_fire_time on sys_quartz_triggers (sched_name, next_fire_time);
create index if not exists idx_sys_quartz_t_nft_st on sys_quartz_triggers (sched_name, trigger_state, next_fire_time);
create index if not exists idx_sys_quartz_t_nft_misfire on sys_quartz_triggers (sched_name, misfire_instr, next_fire_time);
create index if not exists idx_sys_quartz_t_nft_st_misfire on sys_quartz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);
create index if not exists idx_sys_quartz_t_nft_st_misfire_grp on sys_quartz_triggers (sched_name, misfire_instr,
                                                                                       next_fire_time, trigger_group,
                                                                                       trigger_state);
create index if not exists idx_sys_quartz_ft_trig_inst_name on sys_quartz_fired_triggers (sched_name, instance_name);
create index if not exists idx_sys_quartz_ft_inst_job_req_rcvry on sys_quartz_fired_triggers (sched_name, instance_name, requests_recovery);
create index if not exists idx_sys_quartz_ft_j_g on sys_quartz_fired_triggers (sched_name, job_name, job_group);
create index if not exists idx_sys_quartz_ft_jg on sys_quartz_fired_triggers (sched_name, job_group);
create index if not exists idx_sys_quartz_ft_t_g on sys_quartz_fired_triggers (sched_name, trigger_name, trigger_group);
create index if not exists idx_sys_quartz_ft_tg on sys_quartz_fired_triggers (sched_name, trigger_group);

-- 自动维护 update_time（数据库兜底）
CREATE OR REPLACE FUNCTION fn_sys_update_time()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = OLD.update_time THEN
        NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sys_user_update_time ON sys_user;
CREATE TRIGGER trg_sys_user_update_time
    BEFORE UPDATE
    ON sys_user
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_dept_update_time ON sys_dept;
CREATE TRIGGER trg_sys_dept_update_time
    BEFORE UPDATE
    ON sys_dept
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_role_update_time ON sys_role;
CREATE TRIGGER trg_sys_role_update_time
    BEFORE UPDATE
    ON sys_role
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_permission_update_time ON sys_permission;
CREATE TRIGGER trg_sys_permission_update_time
    BEFORE UPDATE
    ON sys_permission
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_menu_update_time ON sys_menu;
CREATE TRIGGER trg_sys_menu_update_time
    BEFORE UPDATE
    ON sys_menu
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_role_permission_update_time ON sys_role_permission;
CREATE TRIGGER trg_sys_role_permission_update_time
    BEFORE UPDATE
    ON sys_role_permission
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_role_menu_update_time ON sys_role_menu;
CREATE TRIGGER trg_sys_role_menu_update_time
    BEFORE UPDATE
    ON sys_role_menu
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_user_role_update_time ON sys_user_role;
CREATE TRIGGER trg_sys_user_role_update_time
    BEFORE UPDATE
    ON sys_user_role
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_data_scope_rule_update_time ON sys_data_scope_rule;
CREATE TRIGGER trg_sys_data_scope_rule_update_time
    BEFORE UPDATE
    ON sys_data_scope_rule
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_order_update_time ON sys_order;
CREATE TRIGGER trg_sys_order_update_time
    BEFORE UPDATE
    ON sys_order
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_notice_update_time ON sys_notice;
CREATE TRIGGER trg_sys_notice_update_time
    BEFORE UPDATE
    ON sys_notice
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_notice_recipient_update_time ON sys_notice_recipient;
CREATE TRIGGER trg_sys_notice_recipient_update_time
    BEFORE UPDATE
    ON sys_notice_recipient
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

-- 初始化基础数据（默认密码示例：Admin@1234 / Manager@1234 / User@1234）
INSERT INTO sys_dept (id, name, code, parent_id, status, sort, remark)
VALUES (1, '总部', 'HQ', NULL, 1, 0, '根部门'),
       (2, '研发中心', 'RD', 1, 1, 10, '产品研发'),
       (3, '运营中心', 'OPS', 1, 1, 20, '运营支持')
ON CONFLICT (id) DO UPDATE SET name      = EXCLUDED.name,
                               code      = EXCLUDED.code,
                               parent_id = EXCLUDED.parent_id,
                               status    = EXCLUDED.status,
                               sort      = EXCLUDED.sort,
                               remark    = EXCLUDED.remark;

INSERT INTO sys_role (id, code, name, status, data_scope_type, data_scope_value)
VALUES (1, 'admin', '系统管理员', 1, 'ALL', NULL),
       (2, 'manager', '部门主管', 1, 'DEPT_AND_CHILD', NULL),
       (3, 'user', '普通用户', 1, 'SELF', NULL)
ON CONFLICT (id) DO UPDATE SET code             = EXCLUDED.code,
                               name             = EXCLUDED.name,
                               status           = EXCLUDED.status,
                               data_scope_type  = EXCLUDED.data_scope_type,
                               data_scope_value = EXCLUDED.data_scope_value;

INSERT INTO sys_permission (id, code, name, status)
VALUES (1, 'user:query', '用户查询', 1),
       (2, 'user:create', '用户创建', 1),
       (3, 'user:update', '用户更新', 1),
       (4, 'user:disable', '用户停用', 1),
       (5, 'user:password:reset', '重置密码', 1),
       (6, 'user:role:assign', '分配角色', 1),
       (7, 'user:data-scope:set', '设置数据范围', 1),
       (8, 'user:export', '用户导出', 1),
       (9, 'user:import', '用户导入', 1),
       (10, 'role:query', '角色查询', 1),
       (11, 'role:create', '角色创建', 1),
       (12, 'role:update', '角色更新', 1),
       (13, 'role:disable', '角色停用', 1),
       (14, 'role:permission:assign', '分配权限', 1),
       (15, 'role:menu:assign', '分配菜单', 1),
       (16, 'permission:query', '权限查询', 1),
       (17, 'permission:create', '权限创建', 1),
       (18, 'permission:update', '权限更新', 1),
       (19, 'permission:disable', '权限停用', 1),
       (20, 'menu:query', '菜单查询', 1),
       (21, 'menu:create', '菜单创建', 1),
       (22, 'menu:update', '菜单更新', 1),
       (23, 'menu:disable', '菜单停用', 1),
       (24, 'dept:query', '部门查询', 1),
       (25, 'dept:create', '部门创建', 1),
       (26, 'dept:update', '部门更新', 1),
       (27, 'dept:disable', '部门停用', 1),
       (28, 'notice:query', '通知查询', 1),
       (29, 'notice:publish', '通知发布', 1),
       (30, 'job:query', '任务查询', 1),
       (31, 'job:create', '任务创建', 1),
       (32, 'job:update', '任务更新', 1),
       (33, 'job:delete', '任务删除', 1),
       (34, 'job:status', '任务状态', 1),
       (35, 'job:run', '任务执行', 1),
       (36, 'user:delete', '用户删除', 1),
       (37, 'role:delete', '角色删除', 1),
       (38, 'menu:delete', '菜单删除', 1),
       (39, 'dept:delete', '部门删除', 1),
       (40, 'permission:delete', '权限删除', 1),
       (41, 'notice:delete', '通知删除', 1)
ON CONFLICT (id) DO UPDATE SET code   = EXCLUDED.code,
                               name   = EXCLUDED.name,
                               status = EXCLUDED.status;

INSERT INTO sys_menu (id, name, code, parent_id, path, component, permission, status, sort, remark)
VALUES (100, '系统管理', 'system', NULL, '/system', 'Layout', NULL, 1, 10, '系统管理根菜单'),
       (110, '用户管理', 'user', 100, '/system/users', 'UserPage', 'user:query', 1, 10, '用户管理'),
       (120, '角色管理', 'role', 100, '/system/roles', 'RolePage', 'role:query', 1, 20, '角色管理'),
       (130, '菜单管理', 'menu', 100, '/system/menus', 'MenuPage', 'menu:query', 1, 30, '菜单管理'),
       (140, '部门管理', 'dept', 100, '/system/depts', 'DeptPage', 'dept:query', 1, 40, '部门管理'),
       (150, '权限管理', 'permission', 100, '/system/permissions', 'PermissionPage', 'permission:query', 1, 50,
        '权限管理'),
       (160, '系统通知', 'notice', 100, '/system/notices', 'NoticePage', 'notice:query', 1, 60, '系统通知'),
       (170, '定时任务', 'job', 100, '/system/jobs', 'JobPage', 'job:query', 1, 70, '定时任务')
ON CONFLICT (id) DO UPDATE SET name       = EXCLUDED.name,
                               code       = EXCLUDED.code,
                               parent_id  = EXCLUDED.parent_id,
                               path       = EXCLUDED.path,
                               component  = EXCLUDED.component,
                               permission = EXCLUDED.permission,
                               status     = EXCLUDED.status,
                               sort       = EXCLUDED.sort,
                               remark     = EXCLUDED.remark;

INSERT INTO sys_user (id, user_name, nick_name, phone, email, password, status, dept_id, data_scope_type, data_scope_value,
                      sex, remark)
VALUES (1, 'admin', '超级管理员', NULL, NULL,
        'b38dce307683511d93ac894f91397a1b5747899bbca077b4cf01c9c31c4f33e0', 1, 1, 'ALL', NULL, 'M', '内置账号'),
       (2, 'manager', '部门主管', NULL, NULL,
        '826182ec96744b73ee254210a720573c494f1486d38715ae48802dc4818fb465', 1, 2,
        'DEPT_AND_CHILD', NULL, 'M', '内置账号'),
       (3, 'demo', '普通用户', NULL, NULL,
        '2177cc1d2fee90fbc535546218a2537cdfd65ab76b4a6726c88f946d4786de72', 1, 2, 'SELF', NULL, 'F', '内置账号')
ON CONFLICT (id) DO UPDATE SET user_name        = EXCLUDED.user_name,
                               nick_name        = EXCLUDED.nick_name,
                               phone            = EXCLUDED.phone,
                               email            = EXCLUDED.email,
                               password         = EXCLUDED.password,
                               status           = EXCLUDED.status,
                               dept_id          = EXCLUDED.dept_id,
                               data_scope_type  = EXCLUDED.data_scope_type,
                               data_scope_value = EXCLUDED.data_scope_value,
                               sex              = EXCLUDED.sex,
                               remark           = EXCLUDED.remark;

INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3, 3)
ON CONFLICT (user_id, role_id, is_deleted) DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 6),
       (1, 7),
       (1, 8),
       (1, 9),
       (1, 10),
       (1, 11),
       (1, 12),
       (1, 13),
       (1, 14),
       (1, 15),
       (1, 16),
       (1, 17),
       (1, 18),
       (1, 19),
       (1, 20),
       (1, 21),
       (1, 22),
       (1, 23),
       (1, 24),
       (1, 25),
       (1, 26),
       (1, 27),
       (1, 28),
       (1, 29),
       (1, 30),
       (1, 31),
       (1, 32),
       (1, 33),
       (1, 34),
       (1, 35),
       (2, 1),
       (2, 8),
       (2, 10),
       (2, 16),
       (2, 20),
       (2, 24),
       (3, 1)
ON CONFLICT (role_id, permission_id, is_deleted) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 100),
       (1, 110),
       (1, 120),
       (1, 130),
       (1, 140),
       (1, 150),
       (1, 160),
       (1, 170),
       (2, 100),
       (2, 110),
       (2, 140),
       (3, 100),
       (3, 110)
ON CONFLICT (role_id, menu_id, is_deleted) DO NOTHING;

SELECT setval('sys_dept_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_dept));
SELECT setval('sys_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role));
SELECT setval('sys_permission_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_permission));
SELECT setval('sys_menu_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_menu));
SELECT setval('sys_user_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user));
SELECT setval('sys_role_permission_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role_permission));
SELECT setval('sys_role_menu_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role_menu));
SELECT setval('sys_user_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user_role));
SELECT setval('sys_order_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_order));
SELECT setval('sys_notice_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_notice));
SELECT setval('sys_notice_recipient_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_notice_recipient));
SELECT setval('sys_job_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_job));
SELECT setval('sys_job_log_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_job_log));
