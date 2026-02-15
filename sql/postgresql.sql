CREATE SEQUENCE IF NOT EXISTS sys_user_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user
(
    id               BIGINT PRIMARY KEY    DEFAULT nextval('sys_user_id_seq'),
    user_name        VARCHAR(64)  NOT NULL,
    nick_name        VARCHAR(64),
    phone            VARCHAR(32),
    email            VARCHAR(128),
    password         VARCHAR(128) NOT NULL,
    password_updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    force_password_change SMALLINT  NOT NULL DEFAULT 1,
    status           SMALLINT     NOT NULL DEFAULT 1,
    dept_id          BIGINT,
    data_scope_type  VARCHAR(32),
    data_scope_value VARCHAR(512),
    sex              VARCHAR(16),
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
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
COMMENT ON COLUMN sys_user.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_user.update_by IS '更新人';
COMMENT ON COLUMN sys_user.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_user.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_user.remark IS '备注';
COMMENT ON COLUMN sys_user.user_name IS '用户名（唯一）';
COMMENT ON COLUMN sys_user.nick_name IS '昵称';
COMMENT ON COLUMN sys_user.phone IS '手机号码';
COMMENT ON COLUMN sys_user.email IS '用户邮箱';
COMMENT ON COLUMN sys_user.password IS '登录密码（加密存储）';
COMMENT ON COLUMN sys_user.password_updated_at IS '密码最近修改时间';
COMMENT ON COLUMN sys_user.force_password_change IS '是否必须修改密码：1-是，0-否';
COMMENT ON COLUMN sys_user.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN sys_user.data_scope_type IS '数据范围类型：ALL全量/SELF仅本人/CUSTOM自定义/NONE无数据';
COMMENT ON COLUMN sys_user.data_scope_value IS '数据范围值，CUSTOM时存储自定义范围内容（如ID列表）';
COMMENT ON COLUMN sys_user.sex IS '性别';
CREATE INDEX IF NOT EXISTS idx_sys_user_dept ON sys_user (dept_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_status_dept_deleted ON sys_user (status, dept_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_phone_deleted ON sys_user (phone, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_email_deleted ON sys_user (email, is_deleted);

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
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
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
COMMENT ON COLUMN sys_dept.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_dept.update_by IS '更新人';
COMMENT ON COLUMN sys_dept.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_dept.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_dept.remark IS '备注';
COMMENT ON COLUMN sys_dept.name IS '部门名称';
COMMENT ON COLUMN sys_dept.code IS '部门编码（唯一）';
COMMENT ON COLUMN sys_dept.parent_id IS '上级部门ID';
COMMENT ON COLUMN sys_dept.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_dept.sort IS '排序';

CREATE SEQUENCE IF NOT EXISTS sys_post_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_post
(
    id          BIGINT PRIMARY KEY    DEFAULT nextval('sys_post_id_seq'),
    name        VARCHAR(128) NOT NULL,
    code        VARCHAR(64),
    dept_id     BIGINT       NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    sort        INTEGER      NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_post_id_seq OWNED BY sys_post.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_post_code ON sys_post (code, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_post_dept ON sys_post (dept_id);
CREATE INDEX IF NOT EXISTS idx_sys_post_dept_status_deleted ON sys_post (dept_id, status, is_deleted);
COMMENT ON TABLE sys_post IS '岗位表';
COMMENT ON COLUMN sys_post.id IS '主键ID';
COMMENT ON COLUMN sys_post.create_time IS '创建时间';
COMMENT ON COLUMN sys_post.update_time IS '更新时间';
COMMENT ON COLUMN sys_post.create_by IS '创建人';
COMMENT ON COLUMN sys_post.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_post.update_by IS '更新人';
COMMENT ON COLUMN sys_post.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_post.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_post.remark IS '备注';
COMMENT ON COLUMN sys_post.name IS '岗位名称';
COMMENT ON COLUMN sys_post.code IS '岗位编码（唯一）';
COMMENT ON COLUMN sys_post.dept_id IS '所属部门ID';
COMMENT ON COLUMN sys_post.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_post.sort IS '排序';

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
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted       SMALLINT     NOT NULL DEFAULT 0,
    version          INT          NOT NULL DEFAULT 0,
    remark           VARCHAR(500)
);
ALTER SEQUENCE sys_role_id_seq OWNED BY sys_role.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role (code, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_role_status_deleted ON sys_role (status, is_deleted);
COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.id IS '主键ID';
COMMENT ON COLUMN sys_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_role.create_by IS '创建人';
COMMENT ON COLUMN sys_role.create_dept IS '创建人所属部门ID（数据归属部门）';
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
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_permission_id_seq OWNED BY sys_permission.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_code ON sys_permission (code, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_permission_status_deleted ON sys_permission (status, is_deleted);
COMMENT ON TABLE sys_permission IS '权限表';
COMMENT ON COLUMN sys_permission.id IS '主键ID';
COMMENT ON COLUMN sys_permission.create_time IS '创建时间';
COMMENT ON COLUMN sys_permission.update_time IS '更新时间';
COMMENT ON COLUMN sys_permission.create_by IS '创建人';
COMMENT ON COLUMN sys_permission.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_permission.update_by IS '更新人';
COMMENT ON COLUMN sys_permission.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_permission.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_permission.remark IS '备注';
COMMENT ON COLUMN sys_permission.code IS '权限编码（唯一）';
COMMENT ON COLUMN sys_permission.name IS '权限名称';
COMMENT ON COLUMN sys_permission.status IS '状态：1-启用，0-禁用';

CREATE SEQUENCE IF NOT EXISTS sys_dict_type_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_dict_type
(
    id          BIGINT PRIMARY KEY    DEFAULT nextval('sys_dict_type_id_seq'),
    dict_type   VARCHAR(64)  NOT NULL,
    dict_name   VARCHAR(128) NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    sort        INTEGER      NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_dict_type_id_seq OWNED BY sys_dict_type.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dict_type ON sys_dict_type (dict_type, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_dict_type_status_deleted_sort ON sys_dict_type (status, is_deleted, sort, id);
COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.id IS '主键ID';
COMMENT ON COLUMN sys_dict_type.dict_type IS '字典类型（唯一）';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict_type.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_dict_type.sort IS '排序';
COMMENT ON COLUMN sys_dict_type.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_type.update_time IS '更新时间';
COMMENT ON COLUMN sys_dict_type.create_by IS '创建人';
COMMENT ON COLUMN sys_dict_type.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_dict_type.update_by IS '更新人';
COMMENT ON COLUMN sys_dict_type.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_dict_type.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_dict_type.remark IS '备注';

CREATE SEQUENCE IF NOT EXISTS sys_dict_data_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_dict_data
(
    id          BIGINT PRIMARY KEY    DEFAULT nextval('sys_dict_data_id_seq'),
    dict_type   VARCHAR(64)  NOT NULL,
    dict_label  VARCHAR(128) NOT NULL,
    dict_value  VARCHAR(128) NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    sort        INTEGER      NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_dict_data_id_seq OWNED BY sys_dict_data.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dict_data ON sys_dict_data (dict_type, dict_value, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_type ON sys_dict_data (dict_type);
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_type_status_deleted_sort ON sys_dict_data (dict_type, status, is_deleted, sort, id);
COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.id IS '主键ID';
COMMENT ON COLUMN sys_dict_data.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典值';
COMMENT ON COLUMN sys_dict_data.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_dict_data.sort IS '排序';
COMMENT ON COLUMN sys_dict_data.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_data.update_time IS '更新时间';
COMMENT ON COLUMN sys_dict_data.create_by IS '创建人';
COMMENT ON COLUMN sys_dict_data.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_dict_data.update_by IS '更新人';
COMMENT ON COLUMN sys_dict_data.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_dict_data.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_dict_data.remark IS '备注';

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
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_menu_id_seq OWNED BY sys_menu.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_menu_code ON sys_menu (code, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent ON sys_menu (parent_id);
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent_status_deleted_sort ON sys_menu (parent_id, status, is_deleted, sort, id);
CREATE INDEX IF NOT EXISTS idx_sys_menu_permission_deleted ON sys_menu (permission, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_menu_status_deleted_sort ON sys_menu (status, is_deleted, sort, id);
COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.id IS '主键ID';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间';
COMMENT ON COLUMN sys_menu.create_by IS '创建人';
COMMENT ON COLUMN sys_menu.create_dept IS '创建人所属部门ID（数据归属部门）';
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
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted    SMALLINT  NOT NULL DEFAULT 0,
    version       INT       NOT NULL DEFAULT 0,
    remark        VARCHAR(500)
);
ALTER SEQUENCE sys_role_permission_id_seq OWNED BY sys_role_permission.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_permission_role_perm ON sys_role_permission (role_id, permission_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_role ON sys_role_permission (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_perm ON sys_role_permission (permission_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_perm_deleted ON sys_role_permission (permission_id, is_deleted);
COMMENT ON TABLE sys_role_permission IS '角色-权限关联表';
COMMENT ON COLUMN sys_role_permission.id IS '主键ID';
COMMENT ON COLUMN sys_role_permission.create_time IS '创建时间';
COMMENT ON COLUMN sys_role_permission.update_time IS '更新时间';
COMMENT ON COLUMN sys_role_permission.create_by IS '创建人';
COMMENT ON COLUMN sys_role_permission.create_dept IS '创建人所属部门ID（数据归属部门）';
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
    data_scope_type VARCHAR(32),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT  NOT NULL DEFAULT 0,
    version     INT       NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_role_menu_id_seq OWNED BY sys_role_menu.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_menu_role_menu ON sys_role_menu (role_id, menu_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role ON sys_role_menu (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu ON sys_role_menu (menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu_deleted ON sys_role_menu (menu_id, is_deleted);
COMMENT ON TABLE sys_role_menu IS '角色-菜单关联表';
COMMENT ON COLUMN sys_role_menu.id IS '主键ID';
COMMENT ON COLUMN sys_role_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_role_menu.update_time IS '更新时间';
COMMENT ON COLUMN sys_role_menu.create_by IS '创建人';
COMMENT ON COLUMN sys_role_menu.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_role_menu.update_by IS '更新人';
COMMENT ON COLUMN sys_role_menu.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_role_menu.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_role_menu.remark IS '备注';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_role_menu.data_scope_type IS '菜单级数据范围类型';

CREATE SEQUENCE IF NOT EXISTS sys_role_menu_dept_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_role_menu_dept
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('sys_role_menu_dept_id_seq'),
    role_id     BIGINT    NOT NULL,
    menu_id     BIGINT    NOT NULL,
    dept_id     BIGINT    NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT  NOT NULL DEFAULT 0,
    version     INT       NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_role_menu_dept_id_seq OWNED BY sys_role_menu_dept.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_menu_dept_role_menu_dept ON sys_role_menu_dept (role_id, menu_id, dept_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_dept_role ON sys_role_menu_dept (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_dept_menu ON sys_role_menu_dept (menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_dept_dept ON sys_role_menu_dept (dept_id);
COMMENT ON TABLE sys_role_menu_dept IS '角色-菜单-部门关联表';
COMMENT ON COLUMN sys_role_menu_dept.id IS '主键ID';
COMMENT ON COLUMN sys_role_menu_dept.create_time IS '创建时间';
COMMENT ON COLUMN sys_role_menu_dept.update_time IS '更新时间';
COMMENT ON COLUMN sys_role_menu_dept.create_by IS '创建人';
COMMENT ON COLUMN sys_role_menu_dept.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_role_menu_dept.update_by IS '更新人';
COMMENT ON COLUMN sys_role_menu_dept.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_role_menu_dept.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_role_menu_dept.remark IS '备注';
COMMENT ON COLUMN sys_role_menu_dept.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu_dept.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_role_menu_dept.dept_id IS '部门ID';

CREATE SEQUENCE IF NOT EXISTS sys_user_role_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user_role
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('sys_user_role_id_seq'),
    user_id     BIGINT    NOT NULL,
    role_id     BIGINT    NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT  NOT NULL DEFAULT 0,
    version     INT       NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_user_role_id_seq OWNED BY sys_user_role.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_role_user_role ON sys_user_role (user_id, role_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_user ON sys_user_role (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role ON sys_user_role (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_deleted ON sys_user_role (role_id, is_deleted);
COMMENT ON TABLE sys_user_role IS '用户-角色关联表';
COMMENT ON COLUMN sys_user_role.id IS '主键ID';
COMMENT ON COLUMN sys_user_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_user_role.create_by IS '创建人';
COMMENT ON COLUMN sys_user_role.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_user_role.update_by IS '更新人';
COMMENT ON COLUMN sys_user_role.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_user_role.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_user_role.remark IS '备注';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

CREATE SEQUENCE IF NOT EXISTS sys_user_data_scope_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user_data_scope
(
    id               BIGINT PRIMARY KEY DEFAULT nextval('sys_user_data_scope_id_seq'),
    user_id          BIGINT    NOT NULL,
    scope_key        VARCHAR(200) NOT NULL,
    data_scope_type  VARCHAR(32),
    data_scope_value VARCHAR(512),
    status           SMALLINT  NOT NULL DEFAULT 1,
    create_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept      BIGINT,
    update_by BIGINT,
    is_deleted       SMALLINT  NOT NULL DEFAULT 0,
    version          INT       NOT NULL DEFAULT 0,
    remark           VARCHAR(500)
);
ALTER SEQUENCE sys_user_data_scope_id_seq OWNED BY sys_user_data_scope.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_data_scope_user_key ON sys_user_data_scope (user_id, scope_key, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_data_scope_user ON sys_user_data_scope (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_data_scope_key ON sys_user_data_scope (scope_key);
CREATE INDEX IF NOT EXISTS idx_sys_user_data_scope_user_status_deleted ON sys_user_data_scope (user_id, status, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_data_scope_scope_status_deleted ON sys_user_data_scope (scope_key, status, is_deleted);
COMMENT ON TABLE sys_user_data_scope IS '用户数据范围覆盖表';
COMMENT ON COLUMN sys_user_data_scope.id IS '主键ID';
COMMENT ON COLUMN sys_user_data_scope.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_data_scope.update_time IS '更新时间';
COMMENT ON COLUMN sys_user_data_scope.create_by IS '创建人';
COMMENT ON COLUMN sys_user_data_scope.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_user_data_scope.update_by IS '更新人';
COMMENT ON COLUMN sys_user_data_scope.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_user_data_scope.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_user_data_scope.remark IS '备注';
COMMENT ON COLUMN sys_user_data_scope.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_data_scope.scope_key IS '数据范围标识';
COMMENT ON COLUMN sys_user_data_scope.data_scope_type IS '数据范围类型';
COMMENT ON COLUMN sys_user_data_scope.data_scope_value IS '数据范围值（自定义部门ID列表）';
COMMENT ON COLUMN sys_user_data_scope.status IS '状态：1-启用，0-禁用';

CREATE SEQUENCE IF NOT EXISTS sys_user_post_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_user_post
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('sys_user_post_id_seq'),
    user_id     BIGINT     NOT NULL,
    post_id     BIGINT     NOT NULL,
    create_time TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT   NOT NULL DEFAULT 0,
    version     INT        NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_user_post_id_seq OWNED BY sys_user_post.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_post_user_post ON sys_user_post (user_id, post_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_post_user ON sys_user_post (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_post_post ON sys_user_post (post_id);
COMMENT ON TABLE sys_user_post IS '用户-岗位关联表';
COMMENT ON COLUMN sys_user_post.id IS '主键ID';
COMMENT ON COLUMN sys_user_post.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_post.update_time IS '更新时间';
COMMENT ON COLUMN sys_user_post.create_by IS '创建人';
COMMENT ON COLUMN sys_user_post.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_user_post.update_by IS '更新人';
COMMENT ON COLUMN sys_user_post.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_user_post.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_user_post.remark IS '备注';
COMMENT ON COLUMN sys_user_post.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_post.post_id IS '岗位ID';

CREATE SEQUENCE IF NOT EXISTS sys_data_scope_rule_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_data_scope_rule
(
    id           BIGINT PRIMARY KEY DEFAULT nextval('sys_data_scope_rule_id_seq'),
    scope_key    VARCHAR(200) NOT NULL,
    table_name   VARCHAR(100) NOT NULL,
    table_alias  VARCHAR(20)  DEFAULT '',
    dept_column  VARCHAR(100) DEFAULT 'create_dept',
    user_column  VARCHAR(100) DEFAULT 'create_by',
    filter_type  SMALLINT     DEFAULT 1,
    status       SMALLINT     DEFAULT 1,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept  BIGINT,
    update_by BIGINT,
    is_deleted   SMALLINT     NOT NULL DEFAULT 0,
    version      INT          NOT NULL DEFAULT 0,
    remark       VARCHAR(500)
);
ALTER SEQUENCE sys_data_scope_rule_id_seq OWNED BY sys_data_scope_rule.id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_data_scope_rule_key ON sys_data_scope_rule (scope_key);
CREATE INDEX IF NOT EXISTS idx_sys_data_scope_rule_status_deleted ON sys_data_scope_rule (status, is_deleted);
COMMENT ON TABLE sys_data_scope_rule IS '数据范围规则表';
COMMENT ON COLUMN sys_data_scope_rule.id IS '主键ID';
COMMENT ON COLUMN sys_data_scope_rule.create_time IS '创建时间';
COMMENT ON COLUMN sys_data_scope_rule.update_time IS '更新时间';
COMMENT ON COLUMN sys_data_scope_rule.create_by IS '创建人';
COMMENT ON COLUMN sys_data_scope_rule.create_dept IS '创建人所属部门ID（数据归属部门）';
COMMENT ON COLUMN sys_data_scope_rule.update_by IS '更新人';
COMMENT ON COLUMN sys_data_scope_rule.is_deleted IS '逻辑删除(0-未删除 1-已删除)';
COMMENT ON COLUMN sys_data_scope_rule.version IS '乐观锁版本号';
COMMENT ON COLUMN sys_data_scope_rule.remark IS '备注';
COMMENT ON COLUMN sys_data_scope_rule.scope_key IS '唯一标识，通常=菜单权限标识';
COMMENT ON COLUMN sys_data_scope_rule.table_name IS '业务表名';
COMMENT ON COLUMN sys_data_scope_rule.table_alias IS '表别名';
COMMENT ON COLUMN sys_data_scope_rule.dept_column IS '部门字段名';
COMMENT ON COLUMN sys_data_scope_rule.user_column IS '用户字段名';
COMMENT ON COLUMN sys_data_scope_rule.filter_type IS '过滤方式';
COMMENT ON COLUMN sys_data_scope_rule.status IS '状态：0-禁用 1-启用';

INSERT INTO sys_data_scope_rule (id, scope_key, table_name, table_alias, dept_column, user_column, filter_type, status, create_time, update_time, remark)
VALUES (1, 'order:query', 'sys_order', '', 'create_dept', 'user_id', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '订单数据范围'),
       (2, 'user:query', 'sys_user', '', 'dept_id', 'id', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '用户数据范围')
;

CREATE SEQUENCE IF NOT EXISTS sys_order_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_order
(
    id          BIGINT PRIMARY KEY      DEFAULT nextval('sys_order_id_seq'),
    user_id     BIGINT         NOT NULL,
    amount      DECIMAL(18, 2) NOT NULL,
    create_time TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    is_deleted  SMALLINT       NOT NULL DEFAULT 0,
    version     INT            NOT NULL DEFAULT 0,
    remark      VARCHAR(500)
);
ALTER SEQUENCE sys_order_id_seq OWNED BY sys_order.id;
CREATE INDEX IF NOT EXISTS idx_sys_order_user ON sys_order (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_order_user_deleted_create_time ON sys_order (user_id, is_deleted, create_time, id);
CREATE INDEX IF NOT EXISTS idx_sys_order_user_deleted_amount ON sys_order (user_id, is_deleted, amount);
COMMENT ON TABLE sys_order IS '订单表';
COMMENT ON COLUMN sys_order.id IS '主键ID';
COMMENT ON COLUMN sys_order.create_time IS '创建时间';
COMMENT ON COLUMN sys_order.update_time IS '更新时间';
COMMENT ON COLUMN sys_order.create_by IS '创建人';
COMMENT ON COLUMN sys_order.create_dept IS '创建人所属部门ID（数据归属部门）';
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
CREATE INDEX IF NOT EXISTS idx_sys_cache_expire_at_key ON sys_cache (expire_at, cache_key);
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
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
    created_name VARCHAR(64),
    is_deleted   SMALLINT     NOT NULL DEFAULT 0,
    version      INT          NOT NULL DEFAULT 0,
    remark       VARCHAR(500)
);
ALTER SEQUENCE sys_notice_id_seq OWNED BY sys_notice.id;
CREATE INDEX IF NOT EXISTS idx_sys_notice_create_time ON sys_notice (create_time);
CREATE INDEX IF NOT EXISTS idx_sys_notice_deleted_create_time ON sys_notice (is_deleted, create_time, id);
CREATE INDEX IF NOT EXISTS idx_sys_notice_scope_deleted_create_time ON sys_notice (scope_type, is_deleted, create_time, id);
COMMENT ON TABLE sys_notice IS '系统通知表';
COMMENT ON COLUMN sys_notice.id IS '主键ID';
COMMENT ON COLUMN sys_notice.create_time IS '创建时间';
COMMENT ON COLUMN sys_notice.update_time IS '更新时间';
COMMENT ON COLUMN sys_notice.create_by IS '创建人';
COMMENT ON COLUMN sys_notice.create_dept IS '创建人所属部门ID（数据归属部门）';
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
    create_by BIGINT,
    create_dept BIGINT,
    update_by BIGINT,
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
CREATE INDEX IF NOT EXISTS idx_sys_notice_recipient_user_deleted_notice ON sys_notice_recipient (user_id, is_deleted, notice_id);
CREATE INDEX IF NOT EXISTS idx_sys_notice_recipient_notice_deleted_read_time ON sys_notice_recipient (notice_id, is_deleted, read_status, read_time, user_id);
COMMENT ON TABLE sys_notice_recipient IS '系统通知接收表';
COMMENT ON COLUMN sys_notice_recipient.id IS '主键ID';
COMMENT ON COLUMN sys_notice_recipient.create_time IS '创建时间';
COMMENT ON COLUMN sys_notice_recipient.update_time IS '更新时间';
COMMENT ON COLUMN sys_notice_recipient.create_by IS '创建人';
COMMENT ON COLUMN sys_notice_recipient.create_dept IS '创建人所属部门ID（数据归属部门）';
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
CREATE INDEX IF NOT EXISTS idx_sys_job_status_id ON sys_job (status, id);
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
    log_detail   TEXT,
    start_time   TIMESTAMP    NOT NULL,
    end_time     TIMESTAMP,
    duration_ms  BIGINT
);
ALTER SEQUENCE sys_job_log_id_seq OWNED BY sys_job_log.id;
CREATE INDEX IF NOT EXISTS idx_sys_job_log_job ON sys_job_log (job_id);
CREATE INDEX IF NOT EXISTS idx_sys_job_log_start ON sys_job_log (start_time);
CREATE INDEX IF NOT EXISTS idx_sys_job_log_job_start_id ON sys_job_log (job_id, start_time, id);
COMMENT ON TABLE sys_job_log IS '定时任务日志表';
COMMENT ON COLUMN sys_job_log.id IS '主键ID';
COMMENT ON COLUMN sys_job_log.job_id IS '任务ID';
COMMENT ON COLUMN sys_job_log.job_name IS '任务名称';
COMMENT ON COLUMN sys_job_log.handler_name IS '处理器名称';
COMMENT ON COLUMN sys_job_log.status IS '执行状态：1-成功，0-失败';
COMMENT ON COLUMN sys_job_log.message IS '执行信息';
COMMENT ON COLUMN sys_job_log.log_detail IS '执行日志';
COMMENT ON COLUMN sys_job_log.start_time IS '开始时间';
COMMENT ON COLUMN sys_job_log.end_time IS '结束时间';
COMMENT ON COLUMN sys_job_log.duration_ms IS '耗时毫秒';

CREATE SEQUENCE IF NOT EXISTS sys_oper_log_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_oper_log
(
    id             BIGINT PRIMARY KEY DEFAULT nextval('sys_oper_log_id_seq'),
    user_id        BIGINT,
    user_name      VARCHAR(64),
    dept_id        BIGINT,
    dept_name      VARCHAR(128),
    title          VARCHAR(128),
    operation      VARCHAR(256),
    business_type  SMALLINT     NOT NULL DEFAULT 0,
    method         VARCHAR(255),
    request_method VARCHAR(16),
    oper_url       VARCHAR(512),
    oper_ip        VARCHAR(128),
    oper_location  VARCHAR(255),
    oper_param     TEXT,
    oper_result    TEXT,
    before_data    TEXT,
    after_data     TEXT,
    status         SMALLINT     NOT NULL DEFAULT 1,
    error_msg      VARCHAR(2000),
    cost_time      BIGINT       NOT NULL DEFAULT 0,
    oper_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER SEQUENCE sys_oper_log_id_seq OWNED BY sys_oper_log.id;
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_user ON sys_oper_log (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_type ON sys_oper_log (business_type);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_status ON sys_oper_log (status);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_time ON sys_oper_log (oper_time);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_status_type_time ON sys_oper_log (status, business_type, oper_time, id);
COMMENT ON TABLE sys_oper_log IS '操作日志表';
COMMENT ON COLUMN sys_oper_log.id IS '主键ID';
COMMENT ON COLUMN sys_oper_log.user_id IS '操作人ID';
COMMENT ON COLUMN sys_oper_log.user_name IS '操作人账号';
COMMENT ON COLUMN sys_oper_log.dept_id IS '部门ID';
COMMENT ON COLUMN sys_oper_log.dept_name IS '部门名称';
COMMENT ON COLUMN sys_oper_log.title IS '模块标题';
COMMENT ON COLUMN sys_oper_log.operation IS '操作描述';
COMMENT ON COLUMN sys_oper_log.business_type IS '业务类型';
COMMENT ON COLUMN sys_oper_log.method IS '请求方法';
COMMENT ON COLUMN sys_oper_log.request_method IS 'HTTP方法';
COMMENT ON COLUMN sys_oper_log.oper_url IS '请求URL';
COMMENT ON COLUMN sys_oper_log.oper_ip IS '操作IP';
COMMENT ON COLUMN sys_oper_log.oper_location IS 'IP归属地';
COMMENT ON COLUMN sys_oper_log.oper_param IS '请求参数';
COMMENT ON COLUMN sys_oper_log.oper_result IS '返回结果';
COMMENT ON COLUMN sys_oper_log.before_data IS '操作前数据';
COMMENT ON COLUMN sys_oper_log.after_data IS '操作后数据';
COMMENT ON COLUMN sys_oper_log.status IS '操作状态';
COMMENT ON COLUMN sys_oper_log.error_msg IS '错误信息';
COMMENT ON COLUMN sys_oper_log.cost_time IS '耗时毫秒';
COMMENT ON COLUMN sys_oper_log.oper_time IS '操作时间';

CREATE SEQUENCE IF NOT EXISTS sys_login_log_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS sys_login_log
(
    id             BIGINT PRIMARY KEY DEFAULT nextval('sys_login_log_id_seq'),
    user_id        BIGINT,
    user_name      VARCHAR(64),
    login_ip       VARCHAR(128),
    login_location VARCHAR(255),
    browser        VARCHAR(128),
    os             VARCHAR(128),
    device_type    VARCHAR(64),
    login_type     SMALLINT     NOT NULL DEFAULT 1,
    status         SMALLINT     NOT NULL DEFAULT 1,
    msg            VARCHAR(500),
    login_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER SEQUENCE sys_login_log_id_seq OWNED BY sys_login_log.id;
CREATE INDEX IF NOT EXISTS idx_sys_login_log_user ON sys_login_log (user_name);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_time ON sys_login_log (login_time);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_ip ON sys_login_log (login_ip);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_status ON sys_login_log (status);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_status_type_time ON sys_login_log (status, login_type, login_time, id);
COMMENT ON TABLE sys_login_log IS '登录日志表';
COMMENT ON COLUMN sys_login_log.id IS '主键ID';
COMMENT ON COLUMN sys_login_log.user_id IS '用户ID';
COMMENT ON COLUMN sys_login_log.user_name IS '登录账号';
COMMENT ON COLUMN sys_login_log.login_ip IS '登录IP';
COMMENT ON COLUMN sys_login_log.login_location IS 'IP归属地';
COMMENT ON COLUMN sys_login_log.browser IS '浏览器';
COMMENT ON COLUMN sys_login_log.os IS '操作系统';
COMMENT ON COLUMN sys_login_log.device_type IS '设备类型';
COMMENT ON COLUMN sys_login_log.login_type IS '类型 1=登录 2=登出';
COMMENT ON COLUMN sys_login_log.status IS '状态 0=失败 1=成功';
COMMENT ON COLUMN sys_login_log.msg IS '提示消息';
COMMENT ON COLUMN sys_login_log.login_time IS '登录时间';

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

DROP TRIGGER IF EXISTS trg_sys_post_update_time ON sys_post;
CREATE TRIGGER trg_sys_post_update_time
    BEFORE UPDATE
    ON sys_post
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

DROP TRIGGER IF EXISTS trg_sys_role_menu_dept_update_time ON sys_role_menu_dept;
CREATE TRIGGER trg_sys_role_menu_dept_update_time
    BEFORE UPDATE
    ON sys_role_menu_dept
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_user_role_update_time ON sys_user_role;
CREATE TRIGGER trg_sys_user_role_update_time
    BEFORE UPDATE
    ON sys_user_role
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_user_post_update_time ON sys_user_post;
CREATE TRIGGER trg_sys_user_post_update_time
    BEFORE UPDATE
    ON sys_user_post
    FOR EACH ROW
EXECUTE FUNCTION fn_sys_update_time();

DROP TRIGGER IF EXISTS trg_sys_user_data_scope_update_time ON sys_user_data_scope;
CREATE TRIGGER trg_sys_user_data_scope_update_time
    BEFORE UPDATE
    ON sys_user_data_scope
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

-- 初始化基础数据（默认密码示例：Passowrd@123）
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
       (74, 'dict:cache:refresh', '字典缓存刷新', 1)
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
       (200, '订单管理', 'order', NULL, '/orders', 'OrderPage', 'order:query', 1, 20, '订单管理')
;

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

INSERT INTO sys_user (id, user_name, nick_name, phone, email, password, status, dept_id, data_scope_type, data_scope_value,
                      sex, remark)
VALUES (1, 'admin', '管理员', NULL, NULL,
        'bb5446b87e58c601c59db53c17e4c11ef7e305e259697ec835e07e9735de7ff5', 1, 1, 'ALL', NULL, 'M',
        '内置账号'),
       (2, 'dev_mgr', '张研发', NULL, NULL,
        'bb5446b87e58c601c59db53c17e4c11ef7e305e259697ec835e07e9735de7ff5', 1, 100, 'DEPT_AND_CHILD', NULL,
        'M', '研发中心主管'),
       (3, 'dev_user', '李工程', NULL, NULL,
        'bb5446b87e58c601c59db53c17e4c11ef7e305e259697ec835e07e9735de7ff5', 1, 100, 'SELF', NULL, 'M',
        '研发中心成员'),
       (4, 'ops_mgr', '王运营', NULL, NULL,
        'bb5446b87e58c601c59db53c17e4c11ef7e305e259697ec835e07e9735de7ff5', 1, 200, 'DEPT_AND_CHILD', NULL,
        'M', '运营中心主管'),
       (5, 'ops_user', '赵专员', NULL, NULL,
        'bb5446b87e58c601c59db53c17e4c11ef7e305e259697ec835e07e9735de7ff5', 1, 200, 'SELF', NULL, 'F',
        '运营中心成员'),
       (6, 'test', '测试账号', NULL, NULL,
        'bb5446b87e58c601c59db53c17e4c11ef7e305e259697ec835e07e9735de7ff5', 1, 100, 'SELF', NULL, 'M',
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
       (1, 170, NULL),
       (1, 180, NULL),
       (1, 181, NULL),
       (1, 182, NULL),
       (1, 183, NULL),
       (1, 190, NULL),
       (1, 191, NULL),
       (1, 192, NULL),
       (1, 200, NULL),
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

INSERT INTO sys_order (id, user_id, amount, create_time, update_time, create_by, create_dept, update_by, is_deleted, version, remark)
VALUES (1, 2, 1999.00, '2026-02-01 09:12:00', '2026-02-01 09:12:00', 2, 100, 2, 0, 0, '研发采购'),
       (2, 3, 499.00, '2026-02-03 14:35:00', '2026-02-03 14:35:00', 3, 100, 3, 0, 0, '研发材料'),
       (3, 4, 129.90, '2026-02-05 10:20:00', '2026-02-05 10:20:00', 4, 200, 4, 0, 0, '运营投放'),
       (4, 5, 799.00, '2026-02-07 16:05:00', '2026-02-07 16:05:00', 5, 200, 5, 0, 0, '渠道采购'),
       (5, 1, 2499.00, '2026-02-10 09:50:00', '2026-02-10 09:50:00', 1, 1, 1, 0, 0, '年度订阅'),
       (6, 6, 89.00, '2026-02-12 11:15:00', '2026-02-12 11:15:00', 6, 100, 6, 0, 0, '测试订单')
;

SELECT setval('sys_dept_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_dept));
SELECT setval('sys_post_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_post));
SELECT setval('sys_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role));
SELECT setval('sys_permission_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_permission));
SELECT setval('sys_menu_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_menu));
SELECT setval('sys_user_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user));
SELECT setval('sys_role_permission_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role_permission));
SELECT setval('sys_role_menu_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role_menu));
SELECT setval('sys_role_menu_dept_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role_menu_dept));
SELECT setval('sys_user_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user_role));
SELECT setval('sys_user_data_scope_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user_data_scope));
SELECT setval('sys_user_post_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user_post));
SELECT setval('sys_dict_type_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_dict_type));
SELECT setval('sys_dict_data_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_dict_data));
SELECT setval('sys_order_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_order));
SELECT setval('sys_notice_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_notice));
SELECT setval('sys_notice_recipient_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_notice_recipient));
SELECT setval('sys_job_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_job));
SELECT setval('sys_job_log_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_job_log));
SELECT setval('sys_oper_log_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_oper_log));
SELECT setval('sys_login_log_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_login_log));
