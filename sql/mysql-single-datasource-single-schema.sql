DROP DATABASE IF EXISTS demo;
CREATE DATABASE demo DEFAULT CHARACTER SET utf8mb4;

USE demo;

CREATE TABLE IF NOT EXISTS sys_user
(
    id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_name             VARCHAR(64)  NOT NULL COMMENT '用户名（唯一）',
    nick_name             VARCHAR(64) COMMENT '昵称',
    phone                 VARCHAR(32) COMMENT '手机号码',
    email                 VARCHAR(128) COMMENT '用户邮箱',
    password              VARCHAR(128) NOT NULL COMMENT '登录密码（加密存储）',
    password_updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '密码最近修改时间',
    force_password_change TINYINT      NOT NULL DEFAULT 1 COMMENT '是否必须修改密码：1-是，0-否',
    status                TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    dept_id               BIGINT COMMENT '部门ID',
    data_scope_type       VARCHAR(32) COMMENT '数据范围类型：ALL全量/SELF仅本人/CUSTOM自定义/NONE无数据',
    data_scope_value      VARCHAR(512) COMMENT '数据范围值，CUSTOM时存储自定义范围内容（如ID列表）',
    sex                   VARCHAR(16) COMMENT '性别',
    create_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by             BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept           BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by             BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted            TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version               INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark                VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_user_name (user_name, is_deleted),
    KEY idx_sys_user_dept (dept_id),
    KEY idx_sys_user_status_dept_deleted (status, dept_id, is_deleted),
    KEY idx_sys_user_phone_deleted (phone, is_deleted),
    KEY idx_sys_user_email_deleted (email, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='系统用户表';

CREATE TABLE IF NOT EXISTS sys_dept
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(128) NOT NULL COMMENT '部门名称',
    code        VARCHAR(64) COMMENT '部门编码（唯一）',
    parent_id   BIGINT COMMENT '上级部门ID',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dept_code (code, is_deleted),
    KEY idx_sys_dept_parent (parent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='部门表';

CREATE TABLE IF NOT EXISTS sys_post
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(128) NOT NULL COMMENT '岗位名称',
    code        VARCHAR(64) COMMENT '岗位编码（唯一）',
    dept_id     BIGINT       NOT NULL COMMENT '所属部门ID',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_post_code (code, is_deleted),
    KEY idx_sys_post_dept (dept_id),
    KEY idx_sys_post_dept_status_deleted (dept_id, status, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='岗位表';

CREATE TABLE IF NOT EXISTS sys_role
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code             VARCHAR(64)  NOT NULL COMMENT '角色编码（唯一）',
    name             VARCHAR(128) NOT NULL COMMENT '角色名称',
    status           TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    data_scope_type  VARCHAR(32) COMMENT '数据范围类型：ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE',
    data_scope_value VARCHAR(512) COMMENT '数据范围值，CUSTOM_DEPT时存储部门ID列表',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by        BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept      BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by        BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version          INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark           VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code, is_deleted),
    KEY idx_sys_role_status_deleted (status, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='角色表';

CREATE TABLE IF NOT EXISTS sys_permission
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code        VARCHAR(64)  NOT NULL COMMENT '权限编码（唯一）',
    name        VARCHAR(128) NOT NULL COMMENT '权限名称',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (code, is_deleted),
    KEY idx_sys_permission_status_deleted (status, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='权限表';

USE demo;

CREATE TABLE IF NOT EXISTS sys_dict_type
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dict_type   VARCHAR(64)  NOT NULL COMMENT '字典类型（唯一）',
    dict_name   VARCHAR(128) NOT NULL COMMENT '字典名称',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dict_type (dict_type, is_deleted),
    KEY idx_sys_dict_type_status_deleted_sort (status, is_deleted, sort, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_data
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dict_type   VARCHAR(64)  NOT NULL COMMENT '字典类型',
    dict_label  VARCHAR(128) NOT NULL COMMENT '字典标签',
    dict_value  VARCHAR(128) NOT NULL COMMENT '字典值',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dict_data (dict_type, dict_value, is_deleted),
    KEY idx_sys_dict_data_type (dict_type),
    KEY idx_sys_dict_data_type_status_deleted_sort (dict_type, status, is_deleted, sort, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='字典数据表';

USE demo;

CREATE TABLE IF NOT EXISTS sys_menu
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(128) NOT NULL COMMENT '菜单名称',
    code        VARCHAR(64) COMMENT '菜单编码（唯一）',
    parent_id   BIGINT COMMENT '上级菜单ID',
    path        VARCHAR(255) COMMENT '路由路径',
    component   VARCHAR(255) COMMENT '前端组件',
    permission  VARCHAR(64) COMMENT '菜单权限标识',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_code (code, is_deleted),
    KEY idx_sys_menu_parent (parent_id),
    KEY idx_sys_menu_parent_status_deleted_sort (parent_id, status, is_deleted, sort, id),
    KEY idx_sys_menu_permission_deleted (permission, is_deleted),
    KEY idx_sys_menu_status_deleted_sort (status, is_deleted, sort, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='菜单表';

CREATE TABLE IF NOT EXISTS sys_role_permission
(
    id            BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id       BIGINT     NOT NULL COMMENT '角色ID',
    permission_id BIGINT     NOT NULL COMMENT '权限ID',
    create_time   DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by     BIGINT              DEFAULT NULL COMMENT '创建人',
    create_dept   BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by     BIGINT              DEFAULT NULL COMMENT '更新人',
    is_deleted    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version       INT        NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark        VARCHAR(500)        DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_permission_role_perm (role_id, permission_id, is_deleted),
    KEY idx_sys_role_permission_role (role_id),
    KEY idx_sys_role_permission_perm (permission_id),
    KEY idx_sys_role_permission_perm_deleted (permission_id, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='角色-权限关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu
(
    id              BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id         BIGINT     NOT NULL COMMENT '角色ID',
    menu_id         BIGINT     NOT NULL COMMENT '菜单ID',
    data_scope_type VARCHAR(32) COMMENT '菜单级数据范围类型',
    create_time     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT              DEFAULT NULL COMMENT '创建人',
    create_dept     BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by       BIGINT              DEFAULT NULL COMMENT '更新人',
    is_deleted      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version         INT        NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark          VARCHAR(500)        DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu_role_menu (role_id, menu_id, is_deleted),
    KEY idx_sys_role_menu_role (role_id),
    KEY idx_sys_role_menu_menu (menu_id),
    KEY idx_sys_role_menu_menu_deleted (menu_id, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='角色-菜单关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu_dept
(
    id          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id     BIGINT     NOT NULL COMMENT '角色ID',
    menu_id     BIGINT     NOT NULL COMMENT '菜单ID',
    dept_id     BIGINT     NOT NULL COMMENT '部门ID',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT              DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT              DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT        NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)        DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu_dept_role_menu_dept (role_id, menu_id, dept_id, is_deleted),
    KEY idx_sys_role_menu_dept_role (role_id),
    KEY idx_sys_role_menu_dept_menu (menu_id),
    KEY idx_sys_role_menu_dept_dept (dept_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='角色-菜单-部门关联表';

CREATE TABLE IF NOT EXISTS sys_user_role
(
    id          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT     NOT NULL COMMENT '用户ID',
    role_id     BIGINT     NOT NULL COMMENT '角色ID',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT              DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT              DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT        NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)        DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role_user_role (user_id, role_id, is_deleted),
    KEY idx_sys_user_role_user (user_id),
    KEY idx_sys_user_role_role (role_id),
    KEY idx_sys_user_role_role_deleted (role_id, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='用户-角色关联表';

CREATE TABLE IF NOT EXISTS sys_user_data_scope
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id          BIGINT       NOT NULL COMMENT '用户ID',
    scope_key        VARCHAR(200) NOT NULL COMMENT '数据范围标识（通常为菜单权限标识）',
    data_scope_type  VARCHAR(32) COMMENT '数据范围类型',
    data_scope_value VARCHAR(512) COMMENT '数据范围值（自定义部门ID列表）',
    status           TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by        BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept      BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by        BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version          INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark           VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_data_scope_user_key (user_id, scope_key, is_deleted),
    KEY idx_sys_user_data_scope_user (user_id),
    KEY idx_sys_user_data_scope_key (scope_key),
    KEY idx_sys_user_data_scope_user_status_deleted (user_id, status, is_deleted),
    KEY idx_sys_user_data_scope_scope_status_deleted (scope_key, status, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='用户数据范围覆盖表';

CREATE TABLE IF NOT EXISTS sys_user_post
(
    id          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT     NOT NULL COMMENT '用户ID',
    post_id     BIGINT     NOT NULL COMMENT '岗位ID',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT              DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT              DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT        NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)        DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_post_user_post (user_id, post_id, is_deleted),
    KEY idx_sys_user_post_user (user_id),
    KEY idx_sys_user_post_post (post_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='用户-岗位关联表';

CREATE TABLE IF NOT EXISTS sys_data_scope_rule
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    scope_key   VARCHAR(200) NOT NULL COMMENT '唯一标识，通常=菜单权限标识',
    table_name  VARCHAR(100) NOT NULL COMMENT '业务表名',
    table_alias VARCHAR(20)           DEFAULT '' COMMENT '表别名',
    dept_column VARCHAR(100)          DEFAULT 'create_dept' COMMENT '部门字段名',
    user_column VARCHAR(100)          DEFAULT 'create_by' COMMENT '用户字段名',
    filter_type TINYINT               DEFAULT 1 COMMENT '1=追加WHERE 2=追加EXISTS子查询 3=JOIN过滤',
    status      TINYINT               DEFAULT 1 COMMENT '0=禁用 1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_data_scope_rule_key (scope_key),
    KEY idx_sys_data_scope_rule_status_deleted (status, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='数据范围规则表';


INSERT INTO sys_data_scope_rule (id, scope_key, table_name, table_alias, dept_column, user_column, filter_type, status,
                                 create_time, update_time, remark)
VALUES (1, 'order:query', 'sys_order', '', 'create_dept', 'user_id', 1, 1, NOW(), NOW(), '订单数据范围'),
       (2, 'user:query', 'sys_user', '', 'dept_id', 'id', 1, 1, NOW(), NOW(), '用户数据范围'),
       (3, 'dept:query', 'sys_dept', '', 'id', 'create_by', 1, 1, NOW(), NOW(), '部门数据范围')
;

USE demo;

CREATE TABLE IF NOT EXISTS sys_order
(
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT         NOT NULL COMMENT '用户ID',
    amount      DECIMAL(18, 2) NOT NULL COMMENT '订单金额',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                  DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT                  DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)            DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_sys_order_user (user_id),
    KEY idx_sys_order_user_deleted_create_time (user_id, is_deleted, create_time, id),
    KEY idx_sys_order_dept_deleted_create_time (create_dept, is_deleted, create_time, id),
    KEY idx_sys_order_user_deleted_amount (user_id, is_deleted, amount)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='订单表';

USE demo;

CREATE TABLE IF NOT EXISTS sys_cache
(
    cache_key   VARCHAR(255) NOT NULL COMMENT '缓存键',
    cache_value LONGTEXT COMMENT '缓存内容（JSON）',
    value_class VARCHAR(255) COMMENT '值类型名称',
    expire_at   BIGINT COMMENT '过期时间（毫秒时间戳）',
    PRIMARY KEY (cache_key),
    KEY idx_sys_cache_expire_at (expire_at),
    KEY idx_sys_cache_expire_at_key (expire_at, cache_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='缓存表';

USE demo;

CREATE TABLE IF NOT EXISTS sys_notice
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title        VARCHAR(200) NOT NULL COMMENT '通知标题',
    content      TEXT         NOT NULL COMMENT '通知内容',
    scope_type   VARCHAR(32)  NOT NULL COMMENT '通知范围类型',
    scope_value  VARCHAR(1024) COMMENT '通知范围值（ID列表）',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by    BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept  BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by    BIGINT                DEFAULT NULL COMMENT '更新人',
    created_name VARCHAR(64) COMMENT '创建人名称',
    is_deleted   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark       VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_sys_notice_create_time (create_time),
    KEY idx_sys_notice_deleted_create_time (is_deleted, create_time, id),
    KEY idx_sys_notice_scope_deleted_create_time (scope_type, is_deleted, create_time, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='系统通知表';

CREATE TABLE IF NOT EXISTS sys_notice_recipient
(
    id          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    notice_id   BIGINT     NOT NULL COMMENT '通知ID',
    user_id     BIGINT     NOT NULL COMMENT '接收用户ID',
    read_status TINYINT    NOT NULL DEFAULT 0 COMMENT '阅读状态：0-未读，1-已读',
    read_time   DATETIME COMMENT '阅读时间',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT              DEFAULT NULL COMMENT '创建人',
    create_dept BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by   BIGINT              DEFAULT NULL COMMENT '更新人',
    is_deleted  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version     INT        NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark      VARCHAR(500)        DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_notice_recipient_notice_user (notice_id, user_id, is_deleted),
    KEY idx_sys_notice_recipient_notice (notice_id),
    KEY idx_sys_notice_recipient_user (user_id),
    KEY idx_sys_notice_recipient_read (read_status),
    KEY idx_sys_notice_recipient_user_read (user_id, read_status, is_deleted),
    KEY idx_sys_notice_recipient_user_deleted_notice (user_id, is_deleted, notice_id),
    KEY idx_sys_notice_recipient_notice_deleted_read_time (notice_id, is_deleted, read_status, read_time, user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='系统通知接收表';

USE demo;

CREATE TABLE IF NOT EXISTS sys_job
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name             VARCHAR(128) NOT NULL COMMENT '任务名称',
    handler_name     VARCHAR(128) NOT NULL COMMENT '处理器名称',
    cron_expression  VARCHAR(128) NOT NULL COMMENT 'Cron表达式',
    status           TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    allow_concurrent TINYINT      NOT NULL DEFAULT 1 COMMENT '是否允许并发：1-允许，0-禁止',
    misfire_policy   VARCHAR(32)           DEFAULT 'DEFAULT' COMMENT '误触发策略',
    params           TEXT COMMENT '任务参数',
    log_collect_level VARCHAR(16) DEFAULT 'INFO' COMMENT '日志收集级别',
    created_by       BIGINT COMMENT '创建人ID',
    created_name     VARCHAR(64) COMMENT '创建人名称',
    created_at       DATETIME     NOT NULL COMMENT '创建时间',
    updated_at       DATETIME COMMENT '更新时间',
    remark           VARCHAR(255) COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_sys_job_status (status),
    KEY idx_sys_job_handler (handler_name),
    KEY idx_sys_job_status_id (status, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='定时任务表';

CREATE TABLE IF NOT EXISTS sys_job_log
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    job_id       BIGINT       NOT NULL COMMENT '任务ID',
    job_name     VARCHAR(128) NOT NULL COMMENT '任务名称',
    handler_name VARCHAR(128) NOT NULL COMMENT '处理器名称',
    status       TINYINT      NOT NULL COMMENT '执行状态：1-成功，0-失败',
    message      VARCHAR(512) COMMENT '执行信息',
    log_detail   TEXT COMMENT '执行日志',
    start_time   DATETIME     NOT NULL COMMENT '开始时间',
    end_time     DATETIME COMMENT '结束时间',
    duration_ms  BIGINT COMMENT '耗时毫秒',
    PRIMARY KEY (id),
    KEY idx_sys_job_log_job (job_id),
    KEY idx_sys_job_log_start (start_time),
    KEY idx_sys_job_log_job_start_id (job_id, start_time, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='定时任务日志表';

CREATE TABLE IF NOT EXISTS sys_job_log_detail
(
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    log_id     BIGINT      NOT NULL COMMENT '日志ID',
    part_type  VARCHAR(16) NOT NULL COMMENT '日志片段类型: MANUAL/AUTO',
    log_detail TEXT COMMENT '日志内容',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_job_log_detail_log_type (log_id, part_type),
    KEY idx_sys_job_log_detail_log (log_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='定时任务日志明细表';

USE demo;

CREATE TABLE IF NOT EXISTS sys_oper_log
(
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT COMMENT '操作人ID',
    user_name      VARCHAR(64) COMMENT '操作人账号',
    dept_id        BIGINT COMMENT '部门ID',
    dept_name      VARCHAR(128) COMMENT '部门名称',
    title          VARCHAR(128) COMMENT '模块标题',
    operation      VARCHAR(256) COMMENT '操作描述',
    business_type  TINYINT  NOT NULL DEFAULT 0 COMMENT '业务类型',
    method         VARCHAR(255) COMMENT '请求方法',
    request_method VARCHAR(16) COMMENT 'HTTP方法',
    oper_url       VARCHAR(512) COMMENT '请求URL',
    oper_ip        VARCHAR(128) COMMENT '操作IP',
    oper_location  VARCHAR(255) COMMENT 'IP归属地',
    oper_param     TEXT COMMENT '请求参数',
    oper_result    TEXT COMMENT '返回结果',
    before_data    TEXT COMMENT '操作前数据',
    after_data     TEXT COMMENT '操作后数据',
    status         TINYINT  NOT NULL DEFAULT 1 COMMENT '操作状态',
    error_msg      VARCHAR(2000) COMMENT '错误信息',
    cost_time      BIGINT   NOT NULL DEFAULT 0 COMMENT '耗时毫秒',
    oper_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_sys_oper_log_user (user_id),
    KEY idx_sys_oper_log_type (business_type),
    KEY idx_sys_oper_log_status (status),
    KEY idx_sys_oper_log_time (oper_time),
    KEY idx_sys_oper_log_status_type_time (status, business_type, oper_time, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='操作日志表';

CREATE TABLE IF NOT EXISTS sys_login_log
(
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT COMMENT '用户ID',
    user_name      VARCHAR(64) COMMENT '登录账号',
    login_ip       VARCHAR(128) COMMENT '登录IP',
    login_location VARCHAR(255) COMMENT 'IP归属地',
    browser        VARCHAR(128) COMMENT '浏览器',
    os             VARCHAR(128) COMMENT '操作系统',
    device_type    VARCHAR(64) COMMENT '设备类型',
    login_type     TINYINT  NOT NULL DEFAULT 1 COMMENT '类型 1=登录 2=登出',
    status         TINYINT  NOT NULL DEFAULT 1 COMMENT '状态 0=失败 1=成功',
    msg            VARCHAR(500) COMMENT '提示消息',
    login_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_sys_login_log_user (user_name),
    KEY idx_sys_login_log_time (login_time),
    KEY idx_sys_login_log_ip (login_ip),
    KEY idx_sys_login_log_status (status),
    KEY idx_sys_login_log_status_type_time (status, login_type, login_time, id),
    KEY idx_sys_login_log_user_type_status_time (user_id, login_type, status, login_time, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='登录日志表';

CREATE TABLE IF NOT EXISTS sys_dynamic_api_log
(
    id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    api_id        BIGINT COMMENT '接口ID',
    api_path      VARCHAR(256) COMMENT '接口路径',
    api_method    VARCHAR(16) COMMENT 'HTTP方法',
    api_type      VARCHAR(16) COMMENT '类型',
    auth_mode     VARCHAR(16) COMMENT '认证模式',
    status        TINYINT  NOT NULL DEFAULT 1 COMMENT '状态 0=失败 1=成功',
    response_code INT COMMENT '响应码',
    error_msg     VARCHAR(2000) COMMENT '错误信息',
    error_details TEXT COMMENT '错误详情',
    meta          TEXT COMMENT '元数据',
    trace_id      VARCHAR(128) COMMENT 'TraceId',
    user_id       BIGINT COMMENT '用户ID',
    user_name     VARCHAR(64) COMMENT '用户账号',
    request_ip    VARCHAR(128) COMMENT '请求IP',
    request_param TEXT COMMENT '请求参数',
    duration_ms   BIGINT COMMENT '耗时毫秒',
    request_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    PRIMARY KEY (id),
    KEY idx_dynamic_api_log_api (api_id),
    KEY idx_dynamic_api_log_status (status),
    KEY idx_dynamic_api_log_time (request_time),
    KEY idx_dynamic_api_log_method_status_time (api_method, status, request_time, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='动态接口日志表';

CREATE TABLE IF NOT EXISTS dynamic_api
(
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    path              VARCHAR(256) NOT NULL COMMENT '接口路径',
    method            VARCHAR(16)  NOT NULL COMMENT 'HTTP方法',
    status            VARCHAR(16)  NOT NULL COMMENT '状态',
    type              VARCHAR(16)  NOT NULL COMMENT '类型',
    config            TEXT COMMENT '配置JSON',
    auth_mode         VARCHAR(16) COMMENT '认证模式',
    rate_limit_policy VARCHAR(64) COMMENT '限流策略',
    timeout_ms        INT COMMENT '超时毫秒',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by         BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept       BIGINT COMMENT '创建人所属部门ID',
    update_by         BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version           INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark            VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dynamic_api_method_path (method, path, is_deleted),
    KEY idx_dynamic_api_status (status),
    KEY idx_dynamic_api_type (type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='动态接口配置表';

USE demo;

-- 清理 Quartz 表，避免重复建索引失败
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS sys_quartz_fired_triggers;
DROP TABLE IF EXISTS sys_quartz_paused_trigger_grps;
DROP TABLE IF EXISTS sys_quartz_scheduler_state;
DROP TABLE IF EXISTS sys_quartz_locks;
DROP TABLE IF EXISTS sys_quartz_simple_triggers;
DROP TABLE IF EXISTS sys_quartz_cron_triggers;
DROP TABLE IF EXISTS sys_quartz_simprop_triggers;
DROP TABLE IF EXISTS sys_quartz_blob_triggers;
DROP TABLE IF EXISTS sys_quartz_triggers;
DROP TABLE IF EXISTS sys_quartz_job_details;
DROP TABLE IF EXISTS sys_quartz_calendars;
SET FOREIGN_KEY_CHECKS = 1;

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

-- 初始化基础数据（默认密码示例：Passowrd@123）
USE demo;

INSERT INTO sys_dept (id, name, code, parent_id, status, sort, remark)
VALUES (1, '总部', 'HQ', NULL, 1, 0, '根部门'),
       (100, '研发中心', 'RD', 1, 1, 10, '产品研发'),
       (200, '运营中心', 'OPS', 1, 1, 20, '运营支持')
;

INSERT INTO sys_post (id, name, code, dept_id, status, sort, remark)
VALUES (1, '总经理', 'CEO', 1, 1, 0, '总部最高负责人'),
       (2, '部门主管', 'DEPT_MANAGER', 1, 1, 10, '全局岗位，各部门通用'),
       (3, '普通员工', 'STAFF', 1, 1, 20, '全局岗位，各部门通用'),
       (4, '研发经理', 'RD_MANAGER', 100, 1, 30, '研发中心岗位'),
       (5, '研发工程师', 'RD_ENGINEER', 100, 1, 40, '研发中心岗位'),
       (6, '运营经理', 'OPS_MANAGER', 200, 1, 50, '运营中心岗位'),
       (7, '运营专员', 'OPS_STAFF', 200, 1, 60, '运营中心岗位')
;

INSERT INTO sys_role (id, code, name, status, data_scope_type, data_scope_value)
VALUES (1, 'admin', '系统管理员', 1, 'ALL', NULL),
       (2, 'dept_mgr', '部门主管', 1, 'DEPT_AND_CHILD', NULL),
       (3, 'user', '普通用户', 1, 'SELF', NULL)
;

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
       (57, 'order:create', '订单创建', 1),
       (58, 'order:update', '订单更新', 1),
       (59, 'order:delete', '订单删除', 1),
       (60, 'profile:update', '个人资料更新', 1),
       (61, 'profile:password', '个人密码修改', 1),
       (62, 'notice:create', '通知创建', 1),
       (63, 'notice:update', '通知编辑', 1),
       (64, 'notice:disable', '通知撤回', 1),
       (65, 'log:query', '操作日志查询', 1),
       (66, 'log:export', '操作日志导出', 1),
       (67, 'log:delete', '操作日志清理', 1),
       (68, 'login-log:query', '登录日志查询', 1),
       (69, 'login-log:delete', '登录日志清理', 1),
       (70, 'dict:query', '字典查询', 1),
       (71, 'dict:create', '字典创建', 1),
       (72, 'dict:update', '字典修改', 1),
       (73, 'dict:delete', '字典删除', 1),
       (74, 'dict:cache:refresh', '字典缓存刷新', 1),
       (75, 'dynamic-api:query', '动态接口查询', 1),
       (76, 'dynamic-api:create', '动态接口创建', 1),
       (77, 'dynamic-api:update', '动态接口更新', 1),
       (78, 'dynamic-api:status', '动态接口状态', 1),
       (79, 'dynamic-api:delete', '动态接口删除', 1),
       (80, 'dynamic-api:reload', '动态接口重载', 1),
       (81, 'dynamic-api-log:query', '动态接口日志查询', 1),
       (82, 'dynamic-api-log:delete', '动态接口日志删除', 1),
       (83, 'notice:stream:metrics', '通知流监控', 1),
       (84, 'job:log:metrics', '任务日志监控', 1),
       (85, 'druid:monitor', '数据源监控', 1),
       (86, 'config:query', '配置查询', 1),
       (87, 'config:create', '配置创建', 1),
       (88, 'config:update', '配置更新', 1),
       (89, 'config:delete', '配置删除', 1),
       (90, 'config:cache:refresh', '配置缓存刷新', 1)
;

INSERT INTO sys_menu (id, name, code, parent_id, path, component, permission, status, sort, remark)
VALUES (100, '系统管理', 'system', NULL, '/system', 'Layout', NULL, 1, 10, '系统管理根菜单'),
       (110, '用户管理', 'user', 100, '/system/user', 'UserPage', 'user:query', 1, 10, '用户管理'),
       (120, '角色管理', 'role', 100, '/system/role', 'RolePage', 'role:query', 1, 20, '角色管理'),
       (130, '菜单管理', 'menu', 100, '/system/menu', 'MenuPage', 'menu:query', 1, 30, '菜单管理'),
       (140, '部门管理', 'dept', 100, '/system/dept', 'DeptPage', 'dept:query', 1, 40, '部门管理'),
       (145, '岗位管理', 'post', 100, '/system/post', 'PostPage', 'post:query', 1, 45, '岗位管理'),
       (150, '权限管理', 'permission', 100, '/system/permission', 'PermissionPage', 'permission:query', 1, 50,
        '权限管理'),
       (155, '字典管理', 'dict', 100, '/system/dict', 'DictPage', 'dict:query', 1, 55, '字典管理'),
       (160, '系统通知', 'notice', 100, '/system/notice', 'NoticePage', 'notice:query', 1, 60, '系统通知'),
       (165, '配置管理', 'config', 100, '/system/config', 'ConfigPage', 'config:query', 1, 65, '配置管理'),
       (170, '定时任务', 'job', 100, '/system/job', 'JobPage', 'job:query', 1, 70, '定时任务'),
       (180, '数据权限', 'data-scope', NULL, '/data-scope', 'DataScopePage', NULL, 1, 30, '数据权限'),
       (181, '权限总览', 'data-scope-overview', 180, '/data-scope/overview', 'DataScopeOverviewPage',
        'data-scope:resolve', 1, 10, '权限总览'),
       (182, '字段映射配置', 'data-scope-mapping', 180, '/data-scope/mapping', 'DataScopeMappingPage',
        'data-scope:rule:query', 1, 20, '字段映射配置'),
       (183, '用户特例授权', 'data-scope-user', 180, '/data-scope/user', 'DataScopeUserPage',
        'data-scope:user:query', 1, 30, '用户特例授权'),
       (190, '系统监控', 'monitor', NULL, '/monitor', 'Layout', NULL, 1, 40, '系统监控'),
       (191, '操作日志', 'oper-log', 190, '/monitor/oper-log', 'OperLogPage', 'log:query', 1, 10, '操作日志'),
       (192, '登录日志', 'login-log', 190, '/monitor/login-log', 'LoginLogPage', 'login-log:query', 1, 20, '登录日志'),
       (193, '通知流监控', 'notice-stream-metrics', 190, '/monitor/notice-stream', 'NoticeStreamMetricsPage',
        'notice:stream:metrics', 1, 30, '通知流监控'),
       (194, '任务日志监控', 'job-log-metrics', 190, '/monitor/job-log', 'JobLogMetricsPage', 'job:log:metrics', 1, 40,
        '任务日志监控'),
       (195, '数据源监控', 'druid-monitor', 190, '/monitor/druid', 'DruidMonitorPage', 'druid:monitor', 1, 50,
        '数据源监控'),
       (196, '首页', 'druid-monitor-home', 195, '/monitor/druid/home', 'DruidMonitorPage', 'druid:monitor', 1, 10,
        '数据源监控-首页'),
       (197, '数据源', 'druid-monitor-datasource', 195, '/monitor/druid/datasource', 'DruidMonitorPage',
        'druid:monitor', 1, 20, '数据源监控-数据源'),
       (198, 'SQL监控', 'druid-monitor-sql', 195, '/monitor/druid/sql', 'DruidMonitorPage', 'druid:monitor', 1, 30,
        '数据源监控-SQL监控'),
       (199, 'SQL防火墙', 'druid-monitor-wall', 195, '/monitor/druid/wall', 'DruidMonitorPage', 'druid:monitor', 1, 40,
        '数据源监控-SQL防火墙'),
       (201, 'Web应用', 'druid-monitor-webapp', 195, '/monitor/druid/webapp', 'DruidMonitorPage', 'druid:monitor', 1,
        50, '数据源监控-Web应用'),
       (202, 'URI监控', 'druid-monitor-weburi', 195, '/monitor/druid/weburi', 'DruidMonitorPage', 'druid:monitor', 1,
        60, '数据源监控-URI监控'),
       (203, 'Session监控', 'druid-monitor-session', 195, '/monitor/druid/session', 'DruidMonitorPage',
        'druid:monitor', 1, 70, '数据源监控-Session监控'),
       (204, 'Spring监控', 'druid-monitor-spring', 195, '/monitor/druid/spring', 'DruidMonitorPage', 'druid:monitor', 1,
        80, '数据源监控-Spring监控'),
       (205, 'JSON API', 'druid-monitor-json', 195, '/monitor/druid/json', 'DruidMonitorPage', 'druid:monitor', 1, 90,
        '数据源监控-JSON API'),
       (200, '订单管理', 'order', NULL, '/orders', 'OrderPage', 'order:query', 1, 20, '订单管理'),
       (210, '接口扩展', 'extension', NULL, '/extension', 'Layout', NULL, 1, 50, '接口扩展'),
       (211, '动态接口', 'dynamic-api', 210, '/extension/dynamic-api', 'DynamicApiPage', 'dynamic-api:query', 1, 10,
        '动态接口管理'),
       (212, '调用日志', 'dynamic-api-log', 210, '/extension/dynamic-api-log', 'DynamicApiLogPage',
        'dynamic-api-log:query', 1, 20, '动态接口日志')
;

USE demo;

INSERT INTO sys_dict_type (id, dict_type, dict_name, status, sort, remark)
VALUES (1, 'sys_gender', '性别', 1, 10, '系统内置'),
       (2, 'sys_status', '状态', 1, 20, '系统内置')
;

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, status, sort, remark)
VALUES (1, 'sys_gender', '男', 'M', 1, 10, NULL),
       (2, 'sys_gender', '女', 'F', 1, 20, NULL),
       (3, 'sys_status', '正常', '1', 1, 10, NULL),
       (4, 'sys_status', '停用', '0', 1, 20, NULL)
;

USE demo;

INSERT INTO sys_user (id, user_name, nick_name, phone, email, password, status, dept_id, data_scope_type,
                      data_scope_value,
                      sex, remark)
VALUES (1, 'admin', '管理员', NULL, NULL,
        '678fb8755982bf36f799473f29b36e8fcde6b6bcb8de8e6451cb1100ce6d9c28', 1, 1, 'ALL', NULL, 'M',
        '内置账号'),
       (2, 'dev_mgr', '张研发', NULL, NULL,
        '678fb8755982bf36f799473f29b36e8fcde6b6bcb8de8e6451cb1100ce6d9c28', 1, 100, 'DEPT_AND_CHILD', NULL,
        'M', '研发中心主管'),
       (3, 'dev_user', '李工程', NULL, NULL,
        '678fb8755982bf36f799473f29b36e8fcde6b6bcb8de8e6451cb1100ce6d9c28', 1, 100, 'SELF', NULL, 'M',
        '研发中心成员'),
       (4, 'ops_mgr', '王运营', NULL, NULL,
        '678fb8755982bf36f799473f29b36e8fcde6b6bcb8de8e6451cb1100ce6d9c28', 1, 200, 'DEPT_AND_CHILD', NULL,
        'M', '运营中心主管'),
       (5, 'ops_user', '赵专员', NULL, NULL,
        '678fb8755982bf36f799473f29b36e8fcde6b6bcb8de8e6451cb1100ce6d9c28', 1, 200, 'SELF', NULL, 'F',
        '运营中心成员'),
       (6, 'test', '测试账号', NULL, NULL,
        '678fb8755982bf36f799473f29b36e8fcde6b6bcb8de8e6451cb1100ce6d9c28', 1, 100, 'SELF', NULL, 'M',
        '测试账号')
;

INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3, 3),
       (4, 2),
       (5, 3),
       (6, 3)
;

INSERT INTO sys_user_post (user_id, post_id)
VALUES (1, 1),
       (2, 2),
       (2, 4),
       (3, 5),
       (4, 2),
       (4, 6),
       (5, 7),
       (6, 3)
;

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
       (1, 36),
       (1, 37),
       (1, 38),
       (1, 39),
       (1, 40),
       (1, 41),
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
       (1, 60),
       (1, 61),
       (1, 62),
       (1, 63),
       (1, 64),
       (1, 65),
       (1, 66),
       (1, 67),
       (1, 68),
       (1, 69),
       (1, 70),
       (1, 71),
       (1, 72),
       (1, 73),
       (1, 74),
       (1, 75),
       (1, 76),
       (1, 77),
       (1, 78),
       (1, 79),
       (1, 80),
       (1, 81),
       (1, 82),
       (1, 83),
       (1, 84),
       (1, 86),
       (1, 87),
       (1, 88),
       (1, 89),
       (1, 90),
       (2, 1),
       (2, 4),
       (2, 5),
       (2, 8),
       (2, 28),
       (2, 29),
       (2, 56),
       (2, 60),
       (2, 61),
       (2, 62),
       (2, 63),
       (2, 64),
       (3, 28),
       (3, 56),
       (3, 60),
       (3, 61)
;

INSERT INTO sys_role_menu (role_id, menu_id, data_scope_type)
VALUES (1, 10, NULL),
       (1, 11, NULL),
       (1, 20, NULL),
       (1, 21, NULL),
       (1, 100, NULL),
       (1, 110, NULL),
       (1, 120, NULL),
       (1, 130, NULL),
       (1, 140, NULL),
       (1, 145, NULL),
       (1, 150, NULL),
       (1, 155, NULL),
       (1, 160, NULL),
       (1, 165, NULL),
       (1, 170, NULL),
       (1, 180, NULL),
       (1, 181, NULL),
       (1, 182, NULL),
       (1, 183, NULL),
       (1, 190, NULL),
       (1, 191, NULL),
       (1, 192, NULL),
       (1, 193, NULL),
       (1, 194, NULL),
       (1, 200, NULL),
       (1, 210, NULL),
       (1, 211, NULL),
       (1, 212, NULL),
       (2, 10, NULL),
       (2, 11, NULL),
       (2, 20, NULL),
       (2, 21, NULL),
       (2, 100, NULL),
       (2, 110, NULL),
       (2, 160, 'DEPT'),
       (2, 200, NULL),
       (3, 10, NULL),
       (3, 11, NULL),
       (3, 20, NULL),
       (3, 21, NULL),
       (3, 100, NULL),
       (3, 160, NULL),
       (3, 200, NULL)
;

USE demo;


INSERT INTO sys_order (id, user_id, amount, create_time, update_time, create_by, create_dept, update_by, is_deleted,
                       version, remark)
VALUES (1, 2, 1999.00, '2026-02-01 09:12:00', '2026-02-01 09:12:00', 2, 100, 2, 0, 0, '研发采购'),
       (2, 3, 499.00, '2026-02-03 14:35:00', '2026-02-03 14:35:00', 3, 100, 3, 0, 0, '研发材料'),
       (3, 4, 129.90, '2026-02-05 10:20:00', '2026-02-05 10:20:00', 4, 200, 4, 0, 0, '运营投放'),
       (4, 5, 799.00, '2026-02-07 16:05:00', '2026-02-07 16:05:00', 5, 200, 5, 0, 0, '渠道采购'),
       (5, 1, 2499.00, '2026-02-10 09:50:00', '2026-02-10 09:50:00', 1, 1, 1, 0, 0, '年度订阅'),
       (6, 6, 89.00, '2026-02-12 11:15:00', '2026-02-12 11:15:00', 6, 100, 6, 0, 0, '测试订单')
;



-- =========================
-- 单数据源账号与权限（需 root/DBA 执行）
-- 单账号覆盖单 database：demo
-- 默认账号与 application-dev.yml 的单数据源默认值一致：demo_system_rw
-- =========================
CREATE USER IF NOT EXISTS 'demo_system_rw'@'%' IDENTIFIED BY 'demo_system_rw';

REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'demo_system_rw'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE ON demo.* TO 'demo_system_rw'@'%';

FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS sys_config
(
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    config_key     VARCHAR(128) NOT NULL COMMENT '配置键',
    config_group   VARCHAR(64)  NOT NULL DEFAULT 'default' COMMENT '配置分组',
    config_value   TEXT COMMENT '配置值',
    config_type    VARCHAR(32)  NOT NULL DEFAULT 'STRING' COMMENT '配置类型',
    config_schema  TEXT COMMENT 'JSON Schema',
    config_version INT          NOT NULL DEFAULT 1 COMMENT '配置版本号',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    hot_update TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持热更新：1-是，0-否',
    sensitive      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否敏感配置：1-是，0-否',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by      BIGINT                DEFAULT NULL COMMENT '创建人',
    create_dept    BIGINT COMMENT '创建人所属部门ID（数据归属部门）',
    update_by      BIGINT                DEFAULT NULL COMMENT '更新人',
    is_deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    version        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark         VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_config_group_key (config_group, config_key, is_deleted),
    KEY idx_sys_config_status_deleted (status, is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT
        ='系统配置表';
