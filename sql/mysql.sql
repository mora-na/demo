CREATE TABLE IF NOT EXISTS sys_user
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    user_name
    VARCHAR
(
    64
) NOT NULL COMMENT '用户名（唯一）',
    nick_name VARCHAR
(
    64
) COMMENT '昵称',
    password VARCHAR
(
    128
) NOT NULL COMMENT '登录密码（加密存储）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    dept_id BIGINT COMMENT '部门ID',
    data_scope_type VARCHAR
(
    32
) COMMENT '数据范围类型：ALL全量/SELF仅本人/CUSTOM自定义/NONE无数据',
    data_scope_value VARCHAR
(
    512
) COMMENT '数据范围值，CUSTOM时存储自定义范围内容（如ID列表）',
    sex VARCHAR
(
    16
) COMMENT '性别',
    tst VARCHAR
(
    255
) COMMENT '备注/测试字段',
    PRIMARY KEY
(
    id
),
    UNIQUE KEY uk_sys_user_user_name
(
    user_name
),
    KEY idx_sys_user_dept
(
    dept_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_dept
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    name
    VARCHAR
(
    128
) NOT NULL COMMENT '部门名称',
    code VARCHAR
(
    64
) COMMENT '部门编码（唯一）',
    parent_id BIGINT COMMENT '上级部门ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    remark VARCHAR
(
    255
) COMMENT '备注',
    PRIMARY KEY
(
    id
),
    UNIQUE KEY uk_sys_dept_code
(
    code
),
    KEY idx_sys_dept_parent
(
    parent_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE IF NOT EXISTS sys_role
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    code
    VARCHAR
(
    64
) NOT NULL COMMENT '角色编码（唯一）',
    name VARCHAR
(
    128
) NOT NULL COMMENT '角色名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    data_scope_type VARCHAR
(
    32
) COMMENT '数据范围类型：ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE',
    data_scope_value VARCHAR
(
    512
) COMMENT '数据范围值，CUSTOM_DEPT时存储部门ID列表',
    PRIMARY KEY
(
    id
),
    UNIQUE KEY uk_sys_role_code
(
    code
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_permission
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    code
    VARCHAR
(
    64
) NOT NULL COMMENT '权限编码（唯一）',
    name VARCHAR
(
    128
) NOT NULL COMMENT '权限名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    PRIMARY KEY
(
    id
),
    UNIQUE KEY uk_sys_permission_code
(
    code
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS sys_menu
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    name
    VARCHAR
(
    128
) NOT NULL COMMENT '菜单名称',
    code VARCHAR
(
    64
) COMMENT '菜单编码（唯一）',
    parent_id BIGINT COMMENT '上级菜单ID',
    path VARCHAR
(
    255
) COMMENT '路由路径',
    component VARCHAR
(
    255
) COMMENT '前端组件',
    permission VARCHAR
(
    64
) COMMENT '菜单权限标识',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    remark VARCHAR
(
    255
) COMMENT '备注',
    PRIMARY KEY
(
    id
),
    UNIQUE KEY uk_sys_menu_code
(
    code
),
    KEY idx_sys_menu_parent
(
    parent_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

CREATE TABLE IF NOT EXISTS sys_role_permission
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    role_id
    BIGINT
    NOT
    NULL
    COMMENT
    '角色ID',
    permission_id
    BIGINT
    NOT
    NULL
    COMMENT
    '权限ID',
    PRIMARY
    KEY
(
    id
),
    UNIQUE KEY uk_sys_role_permission_role_perm
(
    role_id,
    permission_id
),
    KEY idx_sys_role_permission_role
(
    role_id
),
    KEY idx_sys_role_permission_perm
(
    permission_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    role_id
    BIGINT
    NOT
    NULL
    COMMENT
    '角色ID',
    menu_id
    BIGINT
    NOT
    NULL
    COMMENT
    '菜单ID',
    PRIMARY
    KEY
(
    id
),
    UNIQUE KEY uk_sys_role_menu_role_menu
(
    role_id,
    menu_id
),
    KEY idx_sys_role_menu_role
(
    role_id
),
    KEY idx_sys_role_menu_menu
(
    menu_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';

CREATE TABLE IF NOT EXISTS sys_user_role
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    user_id
    BIGINT
    NOT
    NULL
    COMMENT
    '用户ID',
    role_id
    BIGINT
    NOT
    NULL
    COMMENT
    '角色ID',
    PRIMARY
    KEY
(
    id
),
    UNIQUE KEY uk_sys_user_role_user_role
(
    user_id,
    role_id
),
    KEY idx_sys_user_role_user
(
    user_id
),
    KEY idx_sys_user_role_role
(
    role_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

CREATE TABLE IF NOT EXISTS sys_data_scope_rule
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    table_name
    VARCHAR
(
    64
) NOT NULL COMMENT '目标表名（小写匹配）',
    column_name VARCHAR
(
    64
) NOT NULL COMMENT '数据范围字段名',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    PRIMARY KEY
(
    id
),
    UNIQUE KEY uk_sys_data_scope_rule_table
(
    table_name
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据范围规则表';

CREATE TABLE IF NOT EXISTS sys_order
(
    id
    BIGINT
    NOT
    NULL
    COMMENT
    '订单ID（外部传入，非自增）',
    user_id
    BIGINT
    NOT
    NULL
    COMMENT
    '用户ID',
    amount
    DECIMAL
(
    18,
    2
) NOT NULL COMMENT '订单金额',
    PRIMARY KEY
(
    id
),
    KEY idx_sys_order_user
(
    user_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS sys_cache
(
    cache_key VARCHAR(255) NOT NULL COMMENT '缓存键',
    cache_value LONGTEXT COMMENT '缓存内容（JSON）',
    value_class VARCHAR(255) COMMENT '值类型名称',
    expire_at BIGINT COMMENT '过期时间（毫秒时间戳）',
    PRIMARY KEY (cache_key),
    KEY idx_sys_cache_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缓存表';

-- 初始化基础数据（默认密码示例：Admin@1234 / Manager@1234 / User@1234）
INSERT INTO sys_dept (id, name, code, parent_id, status, sort, remark) VALUES
    (1, '总部', 'HQ', NULL, 1, 0, '根部门'),
    (2, '研发中心', 'RD', 1, 1, 10, '产品研发'),
    (3, '运营中心', 'OPS', 1, 1, 20, '运营支持')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    parent_id = VALUES(parent_id),
    status = VALUES(status),
    sort = VALUES(sort),
    remark = VALUES(remark);

INSERT INTO sys_role (id, code, name, status, data_scope_type, data_scope_value) VALUES
    (1, 'admin', '系统管理员', 1, 'ALL', NULL),
    (2, 'manager', '部门主管', 1, 'DEPT_AND_CHILD', NULL),
    (3, 'user', '普通用户', 1, 'SELF', NULL)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    status = VALUES(status),
    data_scope_type = VALUES(data_scope_type),
    data_scope_value = VALUES(data_scope_value);

INSERT INTO sys_permission (id, code, name, status) VALUES
    (1, 'user:query', '用户查询', 1),
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
    (27, 'dept:disable', '部门停用', 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    status = VALUES(status);

INSERT INTO sys_menu (id, name, code, parent_id, path, component, permission, status, sort, remark) VALUES
    (100, '系统管理', 'system', NULL, '/system', 'Layout', NULL, 1, 10, '系统管理根菜单'),
    (110, '用户管理', 'user', 100, '/system/users', 'UserPage', 'user:query', 1, 10, '用户管理'),
    (120, '角色管理', 'role', 100, '/system/roles', 'RolePage', 'role:query', 1, 20, '角色管理'),
    (130, '菜单管理', 'menu', 100, '/system/menus', 'MenuPage', 'menu:query', 1, 30, '菜单管理'),
    (140, '部门管理', 'dept', 100, '/system/depts', 'DeptPage', 'dept:query', 1, 40, '部门管理'),
    (150, '权限管理', 'permission', 100, '/system/permissions', 'PermissionPage', 'permission:query', 1, 50, '权限管理')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    parent_id = VALUES(parent_id),
    path = VALUES(path),
    component = VALUES(component),
    permission = VALUES(permission),
    status = VALUES(status),
    sort = VALUES(sort),
    remark = VALUES(remark);

INSERT INTO sys_user (id, user_name, nick_name, password, status, dept_id, data_scope_type, data_scope_value, sex, tst) VALUES
    (1, 'admin', '超级管理员', 'b38dce307683511d93ac894f91397a1b5747899bbca077b4cf01c9c31c4f33e0', 1, 1, 'ALL', NULL, 'M', '内置账号'),
    (2, 'manager', '部门主管', '826182ec96744b73ee254210a720573c494f1486d38715ae48802dc4818fb465', 1, 2, 'DEPT_AND_CHILD', NULL, 'M', '内置账号'),
    (3, 'demo', '普通用户', '2177cc1d2fee90fbc535546218a2537cdfd65ab76b4a6726c88f946d4786de72', 1, 2, 'SELF', NULL, 'F', '内置账号')
ON DUPLICATE KEY UPDATE
    nick_name = VALUES(nick_name),
    password = VALUES(password),
    status = VALUES(status),
    dept_id = VALUES(dept_id),
    data_scope_type = VALUES(data_scope_type),
    data_scope_value = VALUES(data_scope_value),
    sex = VALUES(sex),
    tst = VALUES(tst);

INSERT INTO sys_user_role (user_id, role_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3)
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id);

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
    (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17),
    (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24), (1, 25),
    (1, 26), (1, 27),
    (2, 1), (2, 8), (2, 10), (2, 16), (2, 20), (2, 24),
    (3, 1)
ON DUPLICATE KEY UPDATE
    role_id = VALUES(role_id);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    (1, 100), (1, 110), (1, 120), (1, 130), (1, 140), (1, 150),
    (2, 100), (2, 110), (2, 140),
    (3, 100), (3, 110)
ON DUPLICATE KEY UPDATE
    role_id = VALUES(role_id);
