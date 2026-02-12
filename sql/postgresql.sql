CREATE SEQUENCE IF NOT EXISTS sys_user_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user
(
    id               BIGINT PRIMARY KEY    DEFAULT nextval('sys_user_id_seq'),
    user_name        VARCHAR(64)  NOT NULL,
    nick_name        VARCHAR(64),
    password         VARCHAR(128) NOT NULL,
    status           SMALLINT     NOT NULL DEFAULT 1,
    dept_id          BIGINT,
    data_scope_type  VARCHAR(32),
    data_scope_value VARCHAR(512),
    sex              VARCHAR(16),
    tst              VARCHAR(255)
);
ALTER SEQUENCE sys_user_id_seq OWNED BY sys_user.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_user_name ON sys_user (user_name);
COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.id IS '主键ID';
COMMENT ON COLUMN sys_user.user_name IS '用户名（唯一）';
COMMENT ON COLUMN sys_user.nick_name IS '昵称';
COMMENT ON COLUMN sys_user.password IS '登录密码（加密存储）';
COMMENT ON COLUMN sys_user.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN sys_user.data_scope_type IS '数据范围类型：ALL全量/SELF仅本人/CUSTOM自定义/NONE无数据';
COMMENT ON COLUMN sys_user.data_scope_value IS '数据范围值，CUSTOM时存储自定义范围内容（如ID列表）';
COMMENT ON COLUMN sys_user.sex IS '性别';
COMMENT ON COLUMN sys_user.tst IS '备注/测试字段';
CREATE INDEX IF NOT EXISTS idx_sys_user_dept ON sys_user (dept_id);

CREATE SEQUENCE IF NOT EXISTS sys_dept_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_dept
(
    id        BIGINT PRIMARY KEY    DEFAULT nextval('sys_dept_id_seq'),
    name      VARCHAR(128) NOT NULL,
    code      VARCHAR(64),
    parent_id BIGINT,
    status    SMALLINT     NOT NULL DEFAULT 1,
    sort      INTEGER      NOT NULL DEFAULT 0,
    remark    VARCHAR(255)
);
ALTER SEQUENCE sys_dept_id_seq OWNED BY sys_dept.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dept_code ON sys_dept (code);
CREATE INDEX IF NOT EXISTS idx_sys_dept_parent ON sys_dept (parent_id);
COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.id IS '主键ID';
COMMENT ON COLUMN sys_dept.name IS '部门名称';
COMMENT ON COLUMN sys_dept.code IS '部门编码（唯一）';
COMMENT ON COLUMN sys_dept.parent_id IS '上级部门ID';
COMMENT ON COLUMN sys_dept.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_dept.sort IS '排序';
COMMENT ON COLUMN sys_dept.remark IS '备注';

CREATE SEQUENCE IF NOT EXISTS sys_role_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_role
(
    id               BIGINT PRIMARY KEY    DEFAULT nextval('sys_role_id_seq'),
    code             VARCHAR(64)  NOT NULL,
    name             VARCHAR(128) NOT NULL,
    status           SMALLINT     NOT NULL DEFAULT 1,
    data_scope_type  VARCHAR(32),
    data_scope_value VARCHAR(512)
);
ALTER SEQUENCE sys_role_id_seq OWNED BY sys_role.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role (code);
COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.id IS '主键ID';
COMMENT ON COLUMN sys_role.code IS '角色编码（唯一）';
COMMENT ON COLUMN sys_role.name IS '角色名称';
COMMENT ON COLUMN sys_role.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_role.data_scope_type IS '数据范围类型：ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE';
COMMENT ON COLUMN sys_role.data_scope_value IS '数据范围值，CUSTOM_DEPT时存储部门ID列表';

CREATE SEQUENCE IF NOT EXISTS sys_permission_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_permission
(
    id     BIGINT PRIMARY KEY    DEFAULT nextval('sys_permission_id_seq'),
    code   VARCHAR(64)  NOT NULL,
    name   VARCHAR(128) NOT NULL,
    status SMALLINT     NOT NULL DEFAULT 1
);
ALTER SEQUENCE sys_permission_id_seq OWNED BY sys_permission.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_code ON sys_permission (code);
COMMENT ON TABLE sys_permission IS '权限表';
COMMENT ON COLUMN sys_permission.id IS '主键ID';
COMMENT ON COLUMN sys_permission.code IS '权限编码（唯一）';
COMMENT ON COLUMN sys_permission.name IS '权限名称';
COMMENT ON COLUMN sys_permission.status IS '状态：1-启用，0-禁用';

CREATE SEQUENCE IF NOT EXISTS sys_menu_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_menu
(
    id         BIGINT PRIMARY KEY    DEFAULT nextval('sys_menu_id_seq'),
    name       VARCHAR(128) NOT NULL,
    code       VARCHAR(64),
    parent_id  BIGINT,
    path       VARCHAR(255),
    component  VARCHAR(255),
    permission VARCHAR(64),
    status     SMALLINT     NOT NULL DEFAULT 1,
    sort       INTEGER      NOT NULL DEFAULT 0,
    remark     VARCHAR(255)
);
ALTER SEQUENCE sys_menu_id_seq OWNED BY sys_menu.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_menu_code ON sys_menu (code);
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent ON sys_menu (parent_id);
COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.id IS '主键ID';
COMMENT ON COLUMN sys_menu.name IS '菜单名称';
COMMENT ON COLUMN sys_menu.code IS '菜单编码（唯一）';
COMMENT ON COLUMN sys_menu.parent_id IS '上级菜单ID';
COMMENT ON COLUMN sys_menu.path IS '路由路径';
COMMENT ON COLUMN sys_menu.component IS '前端组件';
COMMENT ON COLUMN sys_menu.permission IS '菜单权限标识';
COMMENT ON COLUMN sys_menu.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_menu.sort IS '排序';
COMMENT ON COLUMN sys_menu.remark IS '备注';

CREATE SEQUENCE IF NOT EXISTS sys_role_permission_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_role_permission
(
    id            BIGINT PRIMARY KEY DEFAULT nextval('sys_role_permission_id_seq'),
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL
);
ALTER SEQUENCE sys_role_permission_id_seq OWNED BY sys_role_permission.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_permission_role_perm ON sys_role_permission (role_id, permission_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_role ON sys_role_permission (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_perm ON sys_role_permission (permission_id);
COMMENT ON TABLE sys_role_permission IS '角色-权限关联表';
COMMENT ON COLUMN sys_role_permission.id IS '主键ID';
COMMENT ON COLUMN sys_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_permission.permission_id IS '权限ID';

CREATE SEQUENCE IF NOT EXISTS sys_role_menu_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_role_menu
(
    id      BIGINT PRIMARY KEY DEFAULT nextval('sys_role_menu_id_seq'),
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL
);
ALTER SEQUENCE sys_role_menu_id_seq OWNED BY sys_role_menu.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_menu_role_menu ON sys_role_menu (role_id, menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role ON sys_role_menu (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu ON sys_role_menu (menu_id);
COMMENT ON TABLE sys_role_menu IS '角色-菜单关联表';
COMMENT ON COLUMN sys_role_menu.id IS '主键ID';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';

CREATE SEQUENCE IF NOT EXISTS sys_user_role_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user_role
(
    id      BIGINT PRIMARY KEY DEFAULT nextval('sys_user_role_id_seq'),
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);
ALTER SEQUENCE sys_user_role_id_seq OWNED BY sys_user_role.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_role_user_role ON sys_user_role (user_id, role_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_user ON sys_user_role (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role ON sys_user_role (role_id);
COMMENT ON TABLE sys_user_role IS '用户-角色关联表';
COMMENT ON COLUMN sys_user_role.id IS '主键ID';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

CREATE SEQUENCE IF NOT EXISTS sys_data_scope_rule_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_data_scope_rule
(
    id          BIGINT PRIMARY KEY   DEFAULT nextval('sys_data_scope_rule_id_seq'),
    table_name  VARCHAR(64) NOT NULL,
    column_name VARCHAR(64) NOT NULL,
    enabled     SMALLINT    NOT NULL DEFAULT 1
);
ALTER SEQUENCE sys_data_scope_rule_id_seq OWNED BY sys_data_scope_rule.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_data_scope_rule_table ON sys_data_scope_rule (table_name);
COMMENT ON TABLE sys_data_scope_rule IS '数据范围规则表';
COMMENT ON COLUMN sys_data_scope_rule.id IS '主键ID';
COMMENT ON COLUMN sys_data_scope_rule.table_name IS '目标表名（小写匹配）';
COMMENT ON COLUMN sys_data_scope_rule.column_name IS '数据范围字段名';
COMMENT ON COLUMN sys_data_scope_rule.enabled IS '是否启用：1-启用，0-禁用';

CREATE TABLE IF NOT EXISTS sys_order
(
    id      BIGINT PRIMARY KEY,
    user_id BIGINT         NOT NULL,
    amount  DECIMAL(18, 2) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_order_user ON sys_order (user_id);
COMMENT ON TABLE sys_order IS '订单表';
COMMENT ON COLUMN sys_order.id IS '订单ID（外部传入，非自增）';
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

-- 初始化基础数据（默认密码示例：Admin@1234 / Manager@1234 / User@1234）
INSERT INTO sys_dept (id, name, code, parent_id, status, sort, remark) VALUES
    (1, '总部', 'HQ', NULL, 1, 0, '根部门'),
    (2, '研发中心', 'RD', 1, 1, 10, '产品研发'),
    (3, '运营中心', 'OPS', 1, 1, 20, '运营支持')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    parent_id = EXCLUDED.parent_id,
    status = EXCLUDED.status,
    sort = EXCLUDED.sort,
    remark = EXCLUDED.remark;

INSERT INTO sys_role (id, code, name, status, data_scope_type, data_scope_value) VALUES
    (1, 'admin', '系统管理员', 1, 'ALL', NULL),
    (2, 'manager', '部门主管', 1, 'DEPT_AND_CHILD', NULL),
    (3, 'user', '普通用户', 1, 'SELF', NULL)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    name = EXCLUDED.name,
    status = EXCLUDED.status,
    data_scope_type = EXCLUDED.data_scope_type,
    data_scope_value = EXCLUDED.data_scope_value;

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
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    name = EXCLUDED.name,
    status = EXCLUDED.status;

INSERT INTO sys_menu (id, name, code, parent_id, path, component, permission, status, sort, remark) VALUES
    (100, '系统管理', 'system', NULL, '/system', 'Layout', NULL, 1, 10, '系统管理根菜单'),
    (110, '用户管理', 'user', 100, '/system/users', 'UserPage', 'user:query', 1, 10, '用户管理'),
    (120, '角色管理', 'role', 100, '/system/roles', 'RolePage', 'role:query', 1, 20, '角色管理'),
    (130, '菜单管理', 'menu', 100, '/system/menus', 'MenuPage', 'menu:query', 1, 30, '菜单管理'),
    (140, '部门管理', 'dept', 100, '/system/depts', 'DeptPage', 'dept:query', 1, 40, '部门管理'),
    (150, '权限管理', 'permission', 100, '/system/permissions', 'PermissionPage', 'permission:query', 1, 50, '权限管理')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    parent_id = EXCLUDED.parent_id,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    permission = EXCLUDED.permission,
    status = EXCLUDED.status,
    sort = EXCLUDED.sort,
    remark = EXCLUDED.remark;

INSERT INTO sys_user (id, user_name, nick_name, password, status, dept_id, data_scope_type, data_scope_value, sex, tst) VALUES
    (1, 'admin', '超级管理员', 'b38dce307683511d93ac894f91397a1b5747899bbca077b4cf01c9c31c4f33e0', 1, 1, 'ALL', NULL, 'M', '内置账号'),
    (2, 'manager', '部门主管', '826182ec96744b73ee254210a720573c494f1486d38715ae48802dc4818fb465', 1, 2, 'DEPT_AND_CHILD', NULL, 'M', '内置账号'),
    (3, 'demo', '普通用户', '2177cc1d2fee90fbc535546218a2537cdfd65ab76b4a6726c88f946d4786de72', 1, 2, 'SELF', NULL, 'F', '内置账号')
ON CONFLICT (id) DO UPDATE SET
    user_name = EXCLUDED.user_name,
    nick_name = EXCLUDED.nick_name,
    password = EXCLUDED.password,
    status = EXCLUDED.status,
    dept_id = EXCLUDED.dept_id,
    data_scope_type = EXCLUDED.data_scope_type,
    data_scope_value = EXCLUDED.data_scope_value,
    sex = EXCLUDED.sex,
    tst = EXCLUDED.tst;

INSERT INTO sys_user_role (user_id, role_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3)
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
    (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17),
    (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24), (1, 25),
    (1, 26), (1, 27),
    (2, 1), (2, 8), (2, 10), (2, 16), (2, 20), (2, 24),
    (3, 1)
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    (1, 100), (1, 110), (1, 120), (1, 130), (1, 140), (1, 150),
    (2, 100), (2, 110), (2, 140),
    (3, 100), (3, 110)
ON CONFLICT (role_id, menu_id) DO NOTHING;

SELECT setval('sys_dept_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_dept));
SELECT setval('sys_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role));
SELECT setval('sys_permission_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_permission));
SELECT setval('sys_menu_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_menu));
SELECT setval('sys_user_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user));
SELECT setval('sys_role_permission_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role_permission));
SELECT setval('sys_role_menu_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role_menu));
SELECT setval('sys_user_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user_role));
