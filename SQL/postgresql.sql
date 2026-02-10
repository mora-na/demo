CREATE SEQUENCE IF NOT EXISTS sys_user_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY DEFAULT nextval('sys_user_id_seq'),
    user_name VARCHAR(64) NOT NULL,
    nick_name VARCHAR(64),
    password VARCHAR(128) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    dept_id BIGINT,
    data_scope_type VARCHAR(32),
    data_scope_value VARCHAR(512),
    sex VARCHAR(16),
    tst VARCHAR(255)
);
ALTER SEQUENCE sys_user_id_seq OWNED BY sys_user.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_user_name ON sys_user(user_name);
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
CREATE INDEX IF NOT EXISTS idx_sys_user_dept ON sys_user(dept_id);

CREATE SEQUENCE IF NOT EXISTS sys_dept_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT PRIMARY KEY DEFAULT nextval('sys_dept_id_seq'),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64),
    parent_id BIGINT,
    status SMALLINT NOT NULL DEFAULT 1,
    sort INTEGER NOT NULL DEFAULT 0,
    remark VARCHAR(255)
);
ALTER SEQUENCE sys_dept_id_seq OWNED BY sys_dept.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dept_code ON sys_dept(code);
CREATE INDEX IF NOT EXISTS idx_sys_dept_parent ON sys_dept(parent_id);
COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.id IS '主键ID';
COMMENT ON COLUMN sys_dept.name IS '部门名称';
COMMENT ON COLUMN sys_dept.code IS '部门编码（唯一）';
COMMENT ON COLUMN sys_dept.parent_id IS '上级部门ID';
COMMENT ON COLUMN sys_dept.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_dept.sort IS '排序';
COMMENT ON COLUMN sys_dept.remark IS '备注';

CREATE SEQUENCE IF NOT EXISTS sys_role_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY DEFAULT nextval('sys_role_id_seq'),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    data_scope_type VARCHAR(32),
    data_scope_value VARCHAR(512)
);
ALTER SEQUENCE sys_role_id_seq OWNED BY sys_role.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role(code);
COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.id IS '主键ID';
COMMENT ON COLUMN sys_role.code IS '角色编码（唯一）';
COMMENT ON COLUMN sys_role.name IS '角色名称';
COMMENT ON COLUMN sys_role.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_role.data_scope_type IS '数据范围类型：ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE';
COMMENT ON COLUMN sys_role.data_scope_value IS '数据范围值，CUSTOM_DEPT时存储部门ID列表';

CREATE SEQUENCE IF NOT EXISTS sys_permission_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY DEFAULT nextval('sys_permission_id_seq'),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1
);
ALTER SEQUENCE sys_permission_id_seq OWNED BY sys_permission.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_code ON sys_permission(code);
COMMENT ON TABLE sys_permission IS '权限表';
COMMENT ON COLUMN sys_permission.id IS '主键ID';
COMMENT ON COLUMN sys_permission.code IS '权限编码（唯一）';
COMMENT ON COLUMN sys_permission.name IS '权限名称';
COMMENT ON COLUMN sys_permission.status IS '状态：1-启用，0-禁用';

CREATE SEQUENCE IF NOT EXISTS sys_menu_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY DEFAULT nextval('sys_menu_id_seq'),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64),
    parent_id BIGINT,
    path VARCHAR(255),
    component VARCHAR(255),
    permission VARCHAR(64),
    status SMALLINT NOT NULL DEFAULT 1,
    sort INTEGER NOT NULL DEFAULT 0,
    remark VARCHAR(255)
);
ALTER SEQUENCE sys_menu_id_seq OWNED BY sys_menu.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_menu_code ON sys_menu(code);
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent ON sys_menu(parent_id);
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
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY DEFAULT nextval('sys_role_permission_id_seq'),
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL
);
ALTER SEQUENCE sys_role_permission_id_seq OWNED BY sys_role_permission.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_permission_role_perm ON sys_role_permission(role_id, permission_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_role ON sys_role_permission(role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_perm ON sys_role_permission(permission_id);
COMMENT ON TABLE sys_role_permission IS '角色-权限关联表';
COMMENT ON COLUMN sys_role_permission.id IS '主键ID';
COMMENT ON COLUMN sys_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_permission.permission_id IS '权限ID';

CREATE SEQUENCE IF NOT EXISTS sys_role_menu_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT PRIMARY KEY DEFAULT nextval('sys_role_menu_id_seq'),
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL
);
ALTER SEQUENCE sys_role_menu_id_seq OWNED BY sys_role_menu.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_menu_role_menu ON sys_role_menu(role_id, menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role ON sys_role_menu(role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu ON sys_role_menu(menu_id);
COMMENT ON TABLE sys_role_menu IS '角色-菜单关联表';
COMMENT ON COLUMN sys_role_menu.id IS '主键ID';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';

CREATE SEQUENCE IF NOT EXISTS sys_user_role_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY DEFAULT nextval('sys_user_role_id_seq'),
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);
ALTER SEQUENCE sys_user_role_id_seq OWNED BY sys_user_role.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_role_user_role ON sys_user_role(user_id, role_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_user ON sys_user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role ON sys_user_role(role_id);
COMMENT ON TABLE sys_user_role IS '用户-角色关联表';
COMMENT ON COLUMN sys_user_role.id IS '主键ID';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

CREATE SEQUENCE IF NOT EXISTS sys_data_scope_rule_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_data_scope_rule (
    id BIGINT PRIMARY KEY DEFAULT nextval('sys_data_scope_rule_id_seq'),
    table_name VARCHAR(64) NOT NULL,
    column_name VARCHAR(64) NOT NULL,
    enabled SMALLINT NOT NULL DEFAULT 1
);
ALTER SEQUENCE sys_data_scope_rule_id_seq OWNED BY sys_data_scope_rule.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_data_scope_rule_table ON sys_data_scope_rule(table_name);
COMMENT ON TABLE sys_data_scope_rule IS '数据范围规则表';
COMMENT ON COLUMN sys_data_scope_rule.id IS '主键ID';
COMMENT ON COLUMN sys_data_scope_rule.table_name IS '目标表名（小写匹配）';
COMMENT ON COLUMN sys_data_scope_rule.column_name IS '数据范围字段名';
COMMENT ON COLUMN sys_data_scope_rule.enabled IS '是否启用：1-启用，0-禁用';

CREATE TABLE IF NOT EXISTS sys_order (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_order_user ON sys_order(user_id);
COMMENT ON TABLE sys_order IS '订单表';
COMMENT ON COLUMN sys_order.id IS '订单ID（外部传入，非自增）';
COMMENT ON COLUMN sys_order.user_id IS '用户ID';
COMMENT ON COLUMN sys_order.amount IS '订单金额';
