CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_name VARCHAR(64) NOT NULL COMMENT '用户名（唯一）',
    nick_name VARCHAR(64) COMMENT '昵称',
    password VARCHAR(128) NOT NULL COMMENT '登录密码（加密存储）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    data_scope_type VARCHAR(32) COMMENT '数据范围类型：ALL全量/SELF仅本人/CUSTOM自定义/NONE无数据',
    data_scope_value VARCHAR(512) COMMENT '数据范围值，CUSTOM时存储自定义范围内容（如ID列表）',
    sex VARCHAR(16) COMMENT '性别',
    tst VARCHAR(255) COMMENT '备注/测试字段',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_user_name (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(64) NOT NULL COMMENT '角色编码（唯一）',
    name VARCHAR(128) NOT NULL COMMENT '角色名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(64) NOT NULL COMMENT '权限编码（唯一）',
    name VARCHAR(128) NOT NULL COMMENT '权限名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_permission_role_perm (role_id, permission_id),
    KEY idx_sys_role_permission_role (role_id),
    KEY idx_sys_role_permission_perm (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role_user_role (user_id, role_id),
    KEY idx_sys_user_role_user (user_id),
    KEY idx_sys_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

CREATE TABLE IF NOT EXISTS sys_data_scope_rule (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    table_name VARCHAR(64) NOT NULL COMMENT '目标表名（小写匹配）',
    column_name VARCHAR(64) NOT NULL COMMENT '数据范围字段名',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_data_scope_rule_table (table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据范围规则表';

CREATE TABLE IF NOT EXISTS sys_order (
    id BIGINT NOT NULL COMMENT '订单ID（外部传入，非自增）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(18,2) NOT NULL COMMENT '订单金额',
    PRIMARY KEY (id),
    KEY idx_sys_order_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
