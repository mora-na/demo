CREATE TABLE IF NOT EXISTS sys_user
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_name VARCHAR(64) NOT NULL COMMENT '用户名（唯一）',
    nick_name VARCHAR(64) COMMENT '昵称',
    phone VARCHAR(32) COMMENT '手机号码',
    email VARCHAR(128) COMMENT '用户邮箱',
    password VARCHAR(128) NOT NULL COMMENT '登录密码（加密存储）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    dept_id BIGINT COMMENT '部门ID',
    data_scope_type VARCHAR(32) COMMENT '数据范围类型：ALL全量/SELF仅本人/CUSTOM自定义/NONE无数据',
    data_scope_value VARCHAR(512) COMMENT '数据范围值，CUSTOM时存储自定义范围内容（如ID列表）',
    sex VARCHAR(16) COMMENT '性别',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_user_name (user_name, is_deleted),
    KEY idx_sys_user_dept (dept_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户表';

CREATE TABLE IF NOT EXISTS sys_dept
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(128) NOT NULL COMMENT '部门名称',
    code VARCHAR(64) COMMENT '部门编码（唯一）',
    parent_id BIGINT COMMENT '上级部门ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dept_code (code, is_deleted),
    KEY idx_sys_dept_parent (parent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='部门表';

CREATE TABLE IF NOT EXISTS sys_post
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(128) NOT NULL COMMENT '岗位名称',
    code VARCHAR(64) COMMENT '岗位编码（唯一）',
    dept_id BIGINT NOT NULL COMMENT '所属部门ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_post_code (code, is_deleted),
    KEY idx_sys_post_dept (dept_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='岗位表';

CREATE TABLE IF NOT EXISTS sys_role
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(64) NOT NULL COMMENT '角色编码（唯一）',
    name VARCHAR(128) NOT NULL COMMENT '角色名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    data_scope_type VARCHAR(32) COMMENT '数据范围类型：ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE',
    data_scope_value VARCHAR(512) COMMENT '数据范围值，CUSTOM_DEPT时存储部门ID列表',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色表';

CREATE TABLE IF NOT EXISTS sys_permission
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(64) NOT NULL COMMENT '权限编码（唯一）',
    name VARCHAR(128) NOT NULL COMMENT '权限名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (code, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='权限表';

CREATE TABLE IF NOT EXISTS sys_menu
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(128) NOT NULL COMMENT '菜单名称',
    code VARCHAR(64) COMMENT '菜单编码（唯一）',
    parent_id BIGINT COMMENT '上级菜单ID',
    path VARCHAR(255) COMMENT '路由路径',
    component VARCHAR(255) COMMENT '前端组件',
    permission VARCHAR(64) COMMENT '菜单权限标识',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_code (code, is_deleted),
    KEY idx_sys_menu_parent (parent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='菜单表';

CREATE TABLE IF NOT EXISTS sys_role_permission
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_permission_role_perm (role_id, permission_id, is_deleted),
    KEY idx_sys_role_permission_role (role_id),
    KEY idx_sys_role_permission_perm (permission_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色-权限关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    data_scope_type VARCHAR(32) COMMENT '菜单级数据范围类型',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu_role_menu (role_id, menu_id, is_deleted),
    KEY idx_sys_role_menu_role (role_id),
    KEY idx_sys_role_menu_menu (menu_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色-菜单关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu_dept
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu_dept_role_menu_dept (role_id, menu_id, dept_id, is_deleted),
    KEY idx_sys_role_menu_dept_role (role_id),
    KEY idx_sys_role_menu_dept_menu (menu_id),
    KEY idx_sys_role_menu_dept_dept (dept_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色-菜单-部门关联表';

CREATE TABLE IF NOT EXISTS sys_user_role
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role_user_role (user_id, role_id, is_deleted),
    KEY idx_sys_user_role_user (user_id),
    KEY idx_sys_user_role_role (role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户-角色关联表';

CREATE TABLE IF NOT EXISTS sys_user_data_scope
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    scope_key VARCHAR(200) NOT NULL COMMENT '数据范围标识（通常为菜单权限标识）',
    data_scope_type VARCHAR(32) COMMENT '数据范围类型',
    data_scope_value VARCHAR(512) COMMENT '数据范围值（自定义部门ID列表）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_data_scope_user_key (user_id, scope_key, is_deleted),
    KEY idx_sys_user_data_scope_user (user_id),
    KEY idx_sys_user_data_scope_key (scope_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户数据范围覆盖表';

CREATE TABLE IF NOT EXISTS sys_user_post
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '岗位ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_post_user_post (user_id, post_id, is_deleted),
    KEY idx_sys_user_post_user (user_id),
    KEY idx_sys_user_post_post (post_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户-岗位关联表';

CREATE TABLE IF NOT EXISTS sys_data_scope_rule
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    scope_key VARCHAR(200) NOT NULL COMMENT '唯一标识，通常=菜单权限标识',
    table_name VARCHAR(100) NOT NULL COMMENT '业务表名',
    table_alias VARCHAR(20) DEFAULT '' COMMENT '表别名',
    dept_column VARCHAR(100) DEFAULT 'create_dept' COMMENT '部门字段名',
    user_column VARCHAR(100) DEFAULT 'create_by' COMMENT '用户字段名',
    filter_type TINYINT DEFAULT 1 COMMENT '1=追加WHERE 2=追加EXISTS子查询 3=JOIN过滤',
    status TINYINT DEFAULT 1 COMMENT '0=禁用 1=启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_data_scope_rule_key (scope_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据范围规则表';

INSERT INTO sys_data_scope_rule (id, scope_key, table_name, table_alias, dept_column, user_column, filter_type, status, create_time, update_time, remark)
VALUES (1, 'order:query', 'sys_order', '', 'create_dept', 'user_id', 1, 1, NOW(), NOW(), '订单数据范围')
ON DUPLICATE KEY UPDATE table_name  = VALUES(table_name),
                        table_alias = VALUES(table_alias),
                        dept_column = VALUES(dept_column),
                        user_column = VALUES(user_column),
                        filter_type = VALUES(filter_type),
                        status      = VALUES(status),
                        update_time = VALUES(update_time),
                        remark      = VALUES(remark);

CREATE TABLE IF NOT EXISTS sys_order
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(18, 2) NOT NULL COMMENT '订单金额',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_sys_order_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';

CREATE TABLE IF NOT EXISTS sys_cache
(
    cache_key   VARCHAR(255) NOT NULL COMMENT '缓存键',
    cache_value LONGTEXT COMMENT '缓存内容（JSON）',
    value_class VARCHAR(255) COMMENT '值类型名称',
    expire_at   BIGINT COMMENT '过期时间（毫秒时间戳）',
    PRIMARY KEY (cache_key),
    KEY idx_sys_cache_expire_at (expire_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='缓存表';

CREATE TABLE IF NOT EXISTS sys_notice
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    scope_type VARCHAR(32) NOT NULL COMMENT '通知范围类型',
    scope_value VARCHAR(1024) COMMENT '通知范围值（ID列表）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    created_name VARCHAR(64) COMMENT '创建人名称',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_sys_notice_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统通知表';

CREATE TABLE IF NOT EXISTS sys_notice_recipient
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    notice_id BIGINT NOT NULL COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    read_status TINYINT NOT NULL DEFAULT 0 COMMENT '阅读状态：0-未读，1-已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_notice_recipient_notice_user (notice_id, user_id, is_deleted),
    KEY idx_sys_notice_recipient_notice (notice_id),
    KEY idx_sys_notice_recipient_user (user_id),
    KEY idx_sys_notice_recipient_read (read_status),
    KEY idx_sys_notice_recipient_user_read (user_id, read_status, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统通知接收表';

CREATE TABLE IF NOT EXISTS sys_job
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(128) NOT NULL COMMENT '任务名称',
    handler_name VARCHAR(128) NOT NULL COMMENT '处理器名称',
    cron_expression VARCHAR(128) NOT NULL COMMENT 'Cron表达式',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    allow_concurrent TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许并发：1-允许，0-禁止',
    misfire_policy VARCHAR(32) DEFAULT 'DEFAULT' COMMENT '误触发策略',
    params TEXT COMMENT '任务参数',
    created_by BIGINT COMMENT '创建人ID',
    created_name VARCHAR(64) COMMENT '创建人名称',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    remark VARCHAR(255) COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_sys_job_status (status),
    KEY idx_sys_job_handler (handler_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='定时任务表';

CREATE TABLE IF NOT EXISTS sys_job_log
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    job_id BIGINT NOT NULL COMMENT '任务ID',
    job_name VARCHAR(128) NOT NULL COMMENT '任务名称',
    handler_name VARCHAR(128) NOT NULL COMMENT '处理器名称',
    status TINYINT NOT NULL COMMENT '执行状态：1-成功，0-失败',
    message VARCHAR(512) COMMENT '执行信息',
    log_detail TEXT COMMENT '执行日志',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms BIGINT COMMENT '耗时毫秒',
    PRIMARY KEY (id),
    KEY idx_sys_job_log_job (job_id),
    KEY idx_sys_job_log_start (start_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='定时任务日志表';

create table if not exists sys_quartz_job_details
(
    sched_name        varchar(120) not null,
    job_name          varchar(200) not null,
    job_group         varchar(200) not null,
    description       varchar(250) null,
    job_class_name    varchar(250) not null,
    is_durable        tinyint(1)   not null,
    is_nonconcurrent  tinyint(1)   not null,
    is_update_data    tinyint(1)   not null,
    requests_recovery tinyint(1)   not null,
    job_data          blob         null,
    primary key (sched_name, job_name, job_group)
) engine = innodb
  default charset = utf8mb4;

create table if not exists sys_quartz_triggers
(
    sched_name     varchar(120) not null,
    trigger_name   varchar(200) not null,
    trigger_group  varchar(200) not null,
    job_name       varchar(200) not null,
    job_group      varchar(200) not null,
    description    varchar(250) null,
    next_fire_time bigint(13)   null,
    prev_fire_time bigint(13)   null,
    priority       integer      null,
    trigger_state  varchar(16)  not null,
    trigger_type   varchar(8)   not null,
    start_time     bigint(13)   not null,
    end_time       bigint(13)   null,
    calendar_name  varchar(200) null,
    misfire_instr  smallint(2)  null,
    job_data       blob         null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group)
        references sys_quartz_job_details (sched_name, job_name, job_group)
) engine = innodb
  default charset = utf8mb4;

create table if not exists sys_quartz_simple_triggers
(
    sched_name      varchar(120) not null,
    trigger_name    varchar(200) not null,
    trigger_group   varchar(200) not null,
    repeat_count    bigint(7)    not null,
    repeat_interval bigint(12)   not null,
    times_triggered bigint(10)   not null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references sys_quartz_triggers (sched_name, trigger_name, trigger_group)
) engine = innodb
  default charset = utf8mb4;

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
) engine = innodb
  default charset = utf8mb4;

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
) engine = innodb
  default charset = utf8mb4;

create table if not exists sys_quartz_blob_triggers
(
    sched_name    varchar(120) not null,
    trigger_name  varchar(200) not null,
    trigger_group varchar(200) not null,
    blob_data     blob         null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references sys_quartz_triggers (sched_name, trigger_name, trigger_group)
) engine = innodb
  default charset = utf8mb4;

create table if not exists sys_quartz_calendars
(
    sched_name    varchar(120) not null,
    calendar_name varchar(200) not null,
    calendar      blob         not null,
    primary key (sched_name, calendar_name)
) engine = innodb
  default charset = utf8mb4;

create table if not exists sys_quartz_paused_trigger_grps
(
    sched_name    varchar(120) not null,
    trigger_group varchar(200) not null,
    primary key (sched_name, trigger_group)
) engine = innodb
  default charset = utf8mb4;

create table if not exists sys_quartz_fired_triggers
(
    sched_name        varchar(120) not null,
    entry_id          varchar(95)  not null,
    trigger_name      varchar(200) not null,
    trigger_group     varchar(200) not null,
    instance_name     varchar(200) not null,
    fired_time        bigint(13)   not null,
    sched_time        bigint(13)   not null,
    priority          integer      not null,
    state             varchar(16)  not null,
    job_name          varchar(200) null,
    job_group         varchar(200) null,
    is_nonconcurrent  tinyint(1)   null,
    requests_recovery tinyint(1)   null,
    primary key (sched_name, entry_id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists sys_quartz_scheduler_state
(
    sched_name        varchar(120) not null,
    instance_name     varchar(200) not null,
    last_checkin_time bigint(13)   not null,
    checkin_interval  bigint(13)   not null,
    primary key (sched_name, instance_name)
) engine = innodb
  default charset = utf8mb4;

create table if not exists sys_quartz_locks
(
    sched_name varchar(120) not null,
    lock_name  varchar(40)  not null,
    primary key (sched_name, lock_name)
) engine = innodb
  default charset = utf8mb4;

create index idx_sys_quartz_j_req_recovery on sys_quartz_job_details (sched_name, requests_recovery);
create index idx_sys_quartz_j_grp on sys_quartz_job_details (sched_name, job_group);
create index idx_sys_quartz_t_j on sys_quartz_triggers (sched_name, job_name, job_group);
create index idx_sys_quartz_t_jg on sys_quartz_triggers (sched_name, job_group);
create index idx_sys_quartz_t_c on sys_quartz_triggers (sched_name, calendar_name);
create index idx_sys_quartz_t_g on sys_quartz_triggers (sched_name, trigger_group);
create index idx_sys_quartz_t_state on sys_quartz_triggers (sched_name, trigger_state);
create index idx_sys_quartz_t_n_state on sys_quartz_triggers (sched_name, trigger_name, trigger_group, trigger_state);
create index idx_sys_quartz_t_n_g_state on sys_quartz_triggers (sched_name, trigger_group, trigger_state);
create index idx_sys_quartz_t_next_fire_time on sys_quartz_triggers (sched_name, next_fire_time);
create index idx_sys_quartz_t_nft_st on sys_quartz_triggers (sched_name, trigger_state, next_fire_time);
create index idx_sys_quartz_t_nft_misfire on sys_quartz_triggers (sched_name, misfire_instr, next_fire_time);
create index idx_sys_quartz_t_nft_st_misfire on sys_quartz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);
create index idx_sys_quartz_t_nft_st_misfire_grp on sys_quartz_triggers (sched_name, misfire_instr, next_fire_time,
                                                                         trigger_group, trigger_state);
create index idx_sys_quartz_ft_trig_inst_name on sys_quartz_fired_triggers (sched_name, instance_name);
create index idx_sys_quartz_ft_inst_job_req_rcvry on sys_quartz_fired_triggers (sched_name, instance_name, requests_recovery);
create index idx_sys_quartz_ft_j_g on sys_quartz_fired_triggers (sched_name, job_name, job_group);
create index idx_sys_quartz_ft_jg on sys_quartz_fired_triggers (sched_name, job_group);
create index idx_sys_quartz_ft_t_g on sys_quartz_fired_triggers (sched_name, trigger_name, trigger_group);
create index idx_sys_quartz_ft_tg on sys_quartz_fired_triggers (sched_name, trigger_group);

-- 初始化基础数据（默认密码示例：Admin@1234 / Manager@1234 / User@1234）
INSERT INTO sys_dept (id, name, code, parent_id, status, sort, remark)
VALUES (1, '总部', 'HQ', NULL, 1, 0, '根部门'),
       (2, '研发中心', 'RD', 1, 1, 10, '产品研发'),
       (3, '运营中心', 'OPS', 1, 1, 20, '运营支持')
ON DUPLICATE KEY UPDATE name      = VALUES(name),
                        parent_id = VALUES(parent_id),
                        status    = VALUES(status),
                        sort      = VALUES(sort),
                        remark    = VALUES(remark);

INSERT INTO sys_post (id, name, code, dept_id, status, sort, remark)
VALUES (1, '部门主管', 'MANAGER', 1, 1, 0, '部门主管岗位'),
       (2, '普通员工', 'STAFF', 1, 1, 10, '普通员工岗位'),
       (3, '研发经理', 'RD_MANAGER', 2, 1, 0, '研发部门岗位'),
       (4, '研发工程师', 'RD_ENGINEER', 2, 1, 10, '研发工程师岗位'),
       (5, '运营专员', 'OPS_STAFF', 3, 1, 10, '运营岗位')
ON DUPLICATE KEY UPDATE name    = VALUES(name),
                        code    = VALUES(code),
                        dept_id = VALUES(dept_id),
                        status  = VALUES(status),
                        sort    = VALUES(sort),
                        remark  = VALUES(remark);

INSERT INTO sys_role (id, code, name, status, data_scope_type, data_scope_value)
VALUES (1, 'admin', '系统管理员', 1, 'ALL', NULL),
       (2, 'manager', '部门主管', 1, 'DEPT_AND_CHILD', NULL),
       (3, 'user', '普通用户', 1, 'SELF', NULL)
ON DUPLICATE KEY UPDATE name             = VALUES(name),
                        status           = VALUES(status),
                        data_scope_type  = VALUES(data_scope_type),
                        data_scope_value = VALUES(data_scope_value);

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
       (41, 'notice:delete', '通知删除', 1),
       (42, 'post:query', '岗位查询', 1),
       (43, 'post:create', '岗位创建', 1),
       (44, 'post:update', '岗位更新', 1),
       (45, 'post:disable', '岗位停用', 1),
       (46, 'post:delete', '岗位删除', 1),
       (47, 'user:post:assign', '分配岗位', 1),
       (48, 'role:menu:data-scope', '菜单数据范围', 1),
       (49, 'data-scope:resolve', '数据权限总览', 1),
       (50, 'data-scope:rule:query', '字段映射查询', 1),
       (51, 'data-scope:rule:create', '字段映射创建', 1),
       (52, 'data-scope:rule:update', '字段映射更新', 1),
       (53, 'data-scope:rule:delete', '字段映射删除', 1),
       (54, 'data-scope:user:query', '用户数据范围查询', 1),
       (55, 'data-scope:user:manage', '用户数据范围管理', 1),
       (56, 'order:query', '订单查询', 1),
       (59, 'order:delete', '订单删除', 1)
ON DUPLICATE KEY UPDATE name   = VALUES(name),
                        status = VALUES(status);

INSERT INTO sys_menu (id, name, code, parent_id, path, component, permission, status, sort, remark)
VALUES (100, '系统管理', 'system', NULL, '/system', 'Layout', NULL, 1, 10, '系统管理根菜单'),
       (110, '用户管理', 'user', 100, '/system/users', 'UserPage', 'user:query', 1, 10, '用户管理'),
       (120, '角色管理', 'role', 100, '/system/roles', 'RolePage', 'role:query', 1, 20, '角色管理'),
       (130, '菜单管理', 'menu', 100, '/system/menus', 'MenuPage', 'menu:query', 1, 30, '菜单管理'),
       (140, '部门管理', 'dept', 100, '/system/depts', 'DeptPage', 'dept:query', 1, 40, '部门管理'),
       (145, '岗位管理', 'post', 100, '/system/posts', 'PostPage', 'post:query', 1, 45, '岗位管理'),
       (150, '权限管理', 'permission', 100, '/system/permissions', 'PermissionPage', 'permission:query', 1, 50,
        '权限管理'),
       (160, '系统通知', 'notice', 100, '/system/notices', 'NoticePage', 'notice:query', 1, 60, '系统通知'),
       (170, '定时任务', 'job', 100, '/system/jobs', 'JobPage', 'job:query', 1, 70, '定时任务'),
       (180, '数据权限', 'data-scope', 100, '/system/data-scope', 'DataScopePage', NULL, 1, 80, '数据权限'),
       (181, '权限总览', 'data-scope-overview', 180, '/system/data-scope/overview', 'DataScopeOverviewPage',
        'data-scope:resolve', 1, 10, '权限总览'),
       (182, '字段映射配置', 'data-scope-mapping', 180, '/system/data-scope/mapping', 'DataScopeMappingPage',
        'data-scope:rule:query', 1, 20, '字段映射配置'),
       (183, '用户特例授权', 'data-scope-user', 180, '/system/data-scope/user', 'DataScopeUserPage',
        'data-scope:user:query', 1, 30, '用户特例授权'),
       (200, '订单管理', 'order', NULL, '/orders', 'OrderPage', 'order:query', 1, 20, '订单管理')
ON DUPLICATE KEY UPDATE name       = VALUES(name),
                        parent_id  = VALUES(parent_id),
                        path       = VALUES(path),
                        component  = VALUES(component),
                        permission = VALUES(permission),
                        status     = VALUES(status),
                        sort       = VALUES(sort),
                        remark     = VALUES(remark);

INSERT INTO sys_user (id, user_name, nick_name, phone, email, password, status, dept_id, data_scope_type, data_scope_value,
                      sex, remark)
VALUES (1, 'admin', '超级管理员', NULL, NULL,
        'b38dce307683511d93ac894f91397a1b5747899bbca077b4cf01c9c31c4f33e0', 1, 1, 'ALL', NULL, 'M', '内置账号'),
       (2, 'manager', '部门主管', NULL, NULL,
        '826182ec96744b73ee254210a720573c494f1486d38715ae48802dc4818fb465', 1, 2,
        'DEPT_AND_CHILD', NULL, 'M', '内置账号'),
       (3, 'demo', '普通用户', NULL, NULL,
        '2177cc1d2fee90fbc535546218a2537cdfd65ab76b4a6726c88f946d4786de72', 1, 2, 'SELF', NULL, 'F', '内置账号')
ON DUPLICATE KEY UPDATE nick_name        = VALUES(nick_name),
                        phone            = VALUES(phone),
                        email            = VALUES(email),
                        password         = VALUES(password),
                        status           = VALUES(status),
                        dept_id          = VALUES(dept_id),
                        data_scope_type  = VALUES(data_scope_type),
                        data_scope_value = VALUES(data_scope_value),
                        sex              = VALUES(sex),
                        remark           = VALUES(remark);

INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3, 3)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

INSERT INTO sys_user_post (user_id, post_id)
VALUES (1, 1),
       (2, 1),
       (3, 2)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

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
       (1, 42),
       (1, 43),
       (1, 44),
       (1, 45),
       (1, 46),
       (1, 47),
       (1, 48),
       (1, 49),
       (1, 50),
       (1, 51),
       (1, 52),
       (1, 53),
       (1, 54),
       (1, 55),
       (1, 56),
       (1, 57),
       (1, 58),
       (1, 59),
       (2, 1),
       (2, 8),
       (2, 10),
       (2, 16),
       (2, 20),
       (2, 24),
       (2, 56),
       (2, 57),
       (2, 58),
       (2, 59),
       (3, 1),
       (3, 56),
       (3, 57),
       (3, 58),
       (3, 59)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 100),
       (1, 110),
       (1, 120),
       (1, 130),
       (1, 140),
       (1, 145),
       (1, 150),
       (1, 160),
       (1, 170),
       (1, 180),
       (1, 181),
       (1, 182),
       (1, 183),
       (1, 200),
       (2, 100),
       (2, 110),
       (2, 140),
       (2, 200),
       (3, 200)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_order (id, user_id, amount, create_time, update_time, create_by, create_dept, update_by, is_deleted, version, remark)
VALUES (1, 1, 1999.00, '2026-02-01 09:12:00', '2026-02-01 09:12:00', 1, 1, 1, 0, 0, '入门套餐'),
       (2, 2, 499.00, '2026-02-03 14:35:00', '2026-02-03 14:35:00', 2, 2, 2, 0, 0, '部门采购'),
       (3, 3, 129.90, '2026-02-05 10:20:00', '2026-02-05 10:20:00', 3, 2, 3, 0, 0, '演示订单'),
       (4, 3, 799.00, '2026-02-07 16:05:00', '2026-02-07 16:05:00', 3, 2, 3, 0, 0, '升级套餐'),
       (5, 1, 2499.00, '2026-02-10 09:50:00', '2026-02-10 09:50:00', 1, 1, 1, 0, 0, '年度订阅'),
       (6, 2, 89.00, '2026-02-12 11:15:00', '2026-02-12 11:15:00', 2, 2, 2, 0, 0, '补充采购')
ON DUPLICATE KEY UPDATE user_id     = VALUES(user_id),
                        amount      = VALUES(amount),
                        create_time = VALUES(create_time),
                        update_time = VALUES(update_time),
                        create_by   = VALUES(create_by),
                        create_dept = VALUES(create_dept),
                        update_by   = VALUES(update_by),
                        is_deleted  = VALUES(is_deleted),
                        version     = VALUES(version),
                        remark      = VALUES(remark);
