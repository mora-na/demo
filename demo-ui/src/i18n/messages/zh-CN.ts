export default {
    app: {
        checking: "正在校验登录状态…"
    },
    common: {
        online: "在线",
        userFallback: "用户",
        roleEmpty: "未分配",
        entry: "功能入口",
        cancel: "取消",
        save: "保存",
        confirmTitle: "提示",
        missingToken: "缺少登录令牌",
        profileLoadFailed: "用户信息加载失败"
    },
    login: {
        badge: "演示入口",
        title: "安全进入演示系统。",
        lede: "使用账号登录，完成验证码校验，并选择与后端匹配的传输方式。",
        transport: {
            title: "传输方式",
            note: "传输过程中密码保持加密。"
        },
        tokenTtl: {
            title: "令牌有效期",
            value: "{hours} 小时",
            note: "基于服务端配置。"
        },
        security: {
            title: "安全策略",
            value: "验证码",
            note: "每次登录前需校验。"
        },
        status: {
            system: "系统状态",
            online: "在线",
            level: "安全等级",
            controlled: "受控",
            window: "登录窗口",
            realtime: "实时刷新"
        },
        panel: {
            title: "欢迎回来",
            subtitle: "请验证身份后继续。"
        },
        form: {
            username: "用户名",
            usernamePlaceholder: "请输入用户名",
            password: "密码",
            passwordPlaceholder: "请输入密码",
            captcha: "验证码",
            captchaPlaceholder: "输入验证码",
            captchaLoading: "加载验证码",
            submit: "登录",
            helper: "登录后将进入首页导航面板，可继续访问演示模块。"
        },
        msg: {
            captchaLoadFailed: "验证码加载失败",
            fillAll: "请填写所有字段。",
            loginFailed: "登录失败",
            profileLoadFailed: "用户信息加载失败",
            welcomeUser: "欢迎，{name}！"
        }
    },
    home: {
        nav: {
            badge: "控制台",
            title: "演示系统",
            sub: "模块化管理中心",
            section: "功能模块",
            empty: "暂无可访问菜单",
            expand: "展开导航",
            collapse: "收起导航"
        },
        topbar: {
            title: "控制台概览",
            chooseModule: "请先选择模块",
            searchPlaceholder: "搜索菜单、路径或权限",
            notifications: "通知",
            settings: "设置",
            logout: "退出登录"
        },
        notice: {
            title: "系统通知",
            streamOnline: "实时",
            streamOffline: "断线"
        },
        main: {
            titleFallback: "控制台",
            descFallback: "从左侧选择模块查看内容",
            empty: "暂无可访问子菜单",
            newTask: "新建任务",
            metrics: {
                group: "模块数",
                submenu: "子菜单数",
                role: "角色数",
                permission: "权限数"
            }
        },
        profile: {
            title: "个人设置",
            userName: "用户名",
            nickName: "昵称",
            nickNamePlaceholder: "请输入昵称",
            phone: "手机号",
            phonePlaceholder: "请输入手机号",
            email: "邮箱",
            emailPlaceholder: "请输入邮箱",
            sex: "性别",
            sexPlaceholder: "请选择",
            sexMale: "男",
            sexFemale: "女",
            oldPassword: "原密码",
            newPassword: "新密码",
            confirmPassword: "确认新密码",
            note: "不修改密码可留空。",
            msg: {
                noChanges: "没有需要保存的修改",
                fillPassword: "请完整填写密码修改字段",
                confirmMismatch: "两次输入的新密码不一致",
                saveFailed: "资料更新失败",
                saveSuccess: "资料已更新"
            }
        },
        msg: {
            profileLoadFailed: "用户信息加载失败",
            logoutSuccess: "已退出登录",
            logoutFailed: "退出登录失败"
        }
    },
    job: {
        title: "定时任务",
        subtitle: "管理系统内的定时任务与执行策略。",
        filter: {
            namePlaceholder: "任务名称",
            handlerPlaceholder: "处理器",
            statusPlaceholder: "状态",
            search: "查询",
            create: "新增任务"
        },
        table: {
            name: "任务名称",
            handler: "处理器",
            cron: "Cron",
            nextFireTime: "下一次执行",
            concurrent: "并发",
            status: "状态",
            action: "操作",
            yes: "是",
            no: "否",
            edit: "编辑",
            run: "立即执行",
            logs: "执行记录",
            delete: "删除"
        },
        dialog: {
            createTitle: "新增任务",
            editTitle: "编辑任务",
            name: "任务名称",
            namePlaceholder: "请输入任务名称",
            handler: "任务处理器",
            handlerPlaceholder: "请选择任务处理器",
            cron: "Cron 表达式",
            cronPlaceholder: "例如：0 0/5 * * * ?",
            misfire: "错过触发处理策略",
            misfirePlaceholder: "请选择策略",
            misfireDefault: "默认",
            misfireIgnore: "忽略错过",
            misfireFire: "立即执行",
            misfireDoNothing: "跳过执行",
            concurrent: "允许并发",
            concurrentPlaceholder: "请选择",
            concurrentYes: "允许",
            concurrentNo: "禁止",
            status: "任务状态",
            statusPlaceholder: "请选择",
            statusEnabled: "启用",
            statusDisabled: "停用",
            params: "参数",
            paramsPlaceholder: "可选，JSON或字符串",
            remark: "备注"
        },
        cronHelper: {
            open: "生成",
            title: "Cron 表达式生成器",
            template: "生成方式",
            templatePlaceholder: "请选择",
            groups: {
                frequency: "按频率（间隔）",
                timePoint: "按时间点（精确）",
                date: "按日期",
                combo: "组合条件"
            },
            templates: {
                freqSeconds: "每 N 秒（Quartz）",
                freqMinutes: "每 N 分钟",
                freqHours: "每 N 小时",
                freqDays: "每 N 天",
                freqWeeks: "每 N 周（指定星期）",
                freqMonths: "每 N 月（指定日期）",
                freqYears: "每年指定日期",
                timeFixed: "固定时刻（每天）",
                timeMulti: "多个时刻（列举）",
                timeRange: "时段内（范围）",
                dateMonthDays: "每月指定日",
                dateWeekdays: "每周指定星期",
                dateLastDay: "每月最后一天（L）",
                dateNearestWeekday: "最近工作日（W）",
                dateNthWeekday: "第 N 个星期几（#）",
                comboWorkdaysHours: "工作日 + 工作时间",
                comboQuarterStart: "季度 + 月初",
                comboRangeStep: "范围 + 步长"
            },
            intervalSeconds: "秒间隔",
            intervalMinutes: "分钟间隔",
            intervalHours: "小时间隔",
            intervalDays: "天间隔",
            intervalWeeks: "周间隔",
            intervalMonths: "月间隔",
            hour: "小时",
            minute: "分钟",
            second: "秒",
            hours: "小时",
            minutes: "分钟",
            hoursPlaceholder: "请选择小时",
            minutesPlaceholder: "请选择分钟",
            time: "执行时间",
            rangeStartHour: "起始小时",
            rangeEndHour: "结束小时",
            stepMinutes: "分钟步长",
            nth: "第 N 个",
            months: "月份",
            monthsPlaceholder: "请选择月份",
            weekday: "星期",
            weekdayPlaceholder: "请选择",
            weekdays: {
                mon: "周一",
                tue: "周二",
                wed: "周三",
                thu: "周四",
                fri: "周五",
                sat: "周六",
                sun: "周日"
            },
            dayOfMonth: "日期(1-31)",
            dayOfMonthPlaceholder: "请选择日期",
            preview: "表达式预览",
            apply: "使用该表达式",
            invalid: "生成的表达式无效"
        },
        logs: {
            title: "执行记录",
            task: "任务",
            handler: "处理器",
            status: "状态",
            statusSuccess: "成功",
            statusFail: "失败",
            startTime: "开始时间",
            duration: "耗时(ms)",
            message: "失败原因",
            action: "操作",
            viewLog: "查看日志",
            detailTitle: "执行日志",
            emptyLog: "暂无执行日志",
            close: "关闭"
        },
        msg: {
            loadFailed: "加载任务失败",
            createSuccess: "任务创建成功",
            createFailed: "任务创建失败",
            updateSuccess: "任务更新成功",
            updateFailed: "任务更新失败",
            saveFailed: "保存任务失败",
            statusUpdated: "状态已更新",
            statusUpdateFailed: "状态更新失败",
            runSuccess: "任务已触发",
            runFailed: "任务触发失败",
            deleteSuccess: "任务已删除",
            deleteFailed: "删除失败",
            loadLogFailed: "加载日志失败",
            loadLogDetailFailed: "加载执行日志失败",
            validateName: "请输入任务名称",
            validateHandler: "请选择处理器",
            validateCron: "请输入 Cron 表达式"
        }
    },
    systemPanel: {
        title: "系统管理",
        subtitle: "维护用户、权限与组织结构配置。",
        placeholder: "请选择系统管理子菜单。",
        tabs: {
            user: "用户管理",
            role: "角色管理",
            menu: "菜单管理",
            dept: "部门管理",
            permission: "权限管理",
            notice: "系统通知",
            job: "定时任务"
        }
    },
    dept: {
        title: "部门管理",
        subtitle: "维护组织部门与层级结构。",
        create: "新增部门",
        filter: {
            delete: "批量删除"
        },
        table: {
            name: "名称",
            code: "编码",
            parent: "父级",
            sort: "排序",
            status: "状态",
            action: "操作",
            edit: "编辑",
            delete: "删除"
        },
        dialog: {
            createTitle: "新增部门",
            editTitle: "编辑部门",
            name: "名称",
            code: "编码",
            parent: "父级部门",
            parentPlaceholder: "请选择",
            sort: "排序",
            status: "状态",
            statusPlaceholder: "请选择",
            statusEnabled: "启用",
            statusDisabled: "禁用",
            remark: "备注"
        },
        msg: {
            loadFailed: "加载部门失败",
            createSuccess: "创建成功",
            createFailed: "创建失败",
            updateSuccess: "更新成功",
            updateFailed: "更新失败",
            saveFailed: "保存失败",
            statusUpdateFailed: "状态更新失败",
            validateName: "请输入部门名称",
            deleteConfirm: "确认删除部门 {name}？",
            batchDeleteConfirm: "确认删除选中的 {count} 个部门？",
            deleteSuccess: "删除成功",
            deleteFailed: "删除失败",
            deleteEmpty: "请先选择要删除的部门"
        }
    },
    menu: {
        title: "菜单管理",
        subtitle: "维护系统菜单与前端路由配置。",
        create: "新增菜单",
        filter: {
            delete: "批量删除"
        },
        table: {
            name: "名称",
            code: "编码",
            parent: "父级",
            path: "路径",
            permission: "权限",
            sort: "排序",
            status: "状态",
            action: "操作",
            edit: "编辑",
            delete: "删除"
        },
        dialog: {
            createTitle: "新增菜单",
            editTitle: "编辑菜单",
            name: "名称",
            code: "编码",
            parent: "父级菜单",
            parentPlaceholder: "请选择",
            path: "路径",
            component: "组件",
            permission: "权限标识",
            sort: "排序",
            status: "状态",
            statusPlaceholder: "请选择",
            statusEnabled: "启用",
            statusDisabled: "禁用",
            remark: "备注"
        },
        msg: {
            loadFailed: "加载菜单失败",
            createSuccess: "创建成功",
            createFailed: "创建失败",
            updateSuccess: "更新成功",
            updateFailed: "更新失败",
            saveFailed: "保存失败",
            statusUpdateFailed: "状态更新失败",
            validateName: "请输入菜单名称",
            deleteConfirm: "确认删除菜单 {name}？",
            batchDeleteConfirm: "确认删除选中的 {count} 个菜单？",
            deleteSuccess: "删除成功",
            deleteFailed: "删除失败",
            deleteEmpty: "请先选择要删除的菜单"
        }
    },
    permission: {
        title: "权限管理",
        subtitle: "维护权限标识与授权名称。",
        create: "新增权限",
        filter: {
            delete: "批量删除"
        },
        table: {
            code: "编码",
            name: "名称",
            status: "状态",
            action: "操作",
            edit: "编辑",
            delete: "删除"
        },
        dialog: {
            createTitle: "新增权限",
            editTitle: "编辑权限",
            code: "权限编码",
            name: "权限名称",
            status: "状态",
            statusPlaceholder: "请选择",
            statusEnabled: "启用",
            statusDisabled: "禁用"
        },
        msg: {
            loadFailed: "加载权限失败",
            createSuccess: "创建成功",
            createFailed: "创建失败",
            updateSuccess: "更新成功",
            updateFailed: "更新失败",
            saveFailed: "保存失败",
            statusUpdateFailed: "状态更新失败",
            validateForm: "请填写权限编码与名称",
            deleteConfirm: "确认删除权限 {name}？",
            batchDeleteConfirm: "确认删除选中的 {count} 个权限？",
            deleteSuccess: "删除成功",
            deleteFailed: "删除失败",
            deleteEmpty: "请先选择要删除的权限"
        }
    },
    role: {
        title: "角色管理",
        subtitle: "维护角色信息与权限、菜单授权。",
        create: "新增角色",
        filter: {
            delete: "批量删除"
        },
        table: {
            code: "编码",
            name: "名称",
            dataScope: "数据范围",
            status: "状态",
            action: "操作",
            edit: "编辑",
            assignPermissions: "分配权限",
            assignMenus: "分配菜单",
            delete: "删除"
        },
        dialog: {
            createTitle: "新增角色",
            editTitle: "编辑角色",
            code: "角色编码",
            name: "角色名称",
            status: "状态",
            statusPlaceholder: "请选择",
            statusEnabled: "启用",
            statusDisabled: "禁用",
            dataScopeType: "数据范围类型",
            dataScopePlaceholder: "请选择",
            dataScopeValue: "数据范围值",
            dataScopeValuePlaceholder: "逗号分隔 ID"
        },
        scope: {
            all: "全部",
            dept: "本部门",
            deptAndChild: "部门及下级",
            custom: "自定义",
            customDept: "自定义部门",
            self: "仅本人",
            none: "无权限"
        },
        permissions: {
            title: "分配权限",
            list: "权限列表",
            placeholder: "请选择"
        },
        menus: {
            title: "分配菜单"
        },
        msg: {
            loadFailed: "加载角色失败",
            createSuccess: "创建成功",
            createFailed: "创建失败",
            updateSuccess: "更新成功",
            updateFailed: "更新失败",
            saveFailed: "保存失败",
            statusUpdateFailed: "状态更新失败",
            validateForm: "请填写角色编码与名称",
            permissionsUpdated: "权限已更新",
            permissionsUpdateFailed: "权限更新失败",
            menusUpdated: "菜单已更新",
            menusUpdateFailed: "菜单更新失败",
            deleteConfirm: "确认删除角色 {name}？",
            batchDeleteConfirm: "确认删除选中的 {count} 个角色？",
            deleteSuccess: "删除成功",
            deleteFailed: "删除失败",
            deleteEmpty: "请先选择要删除的角色"
        }
    },
    user: {
        title: "用户管理",
        subtitle: "维护系统用户、账号状态与角色关系。",
        filter: {
            userNamePlaceholder: "用户名",
            nickNamePlaceholder: "昵称",
            statusPlaceholder: "状态",
            search: "查询",
            create: "新增用户",
            delete: "批量删除"
        },
        table: {
            userName: "用户名",
            nickName: "昵称",
            phone: "手机号",
            email: "邮箱",
            sex: "性别",
            dept: "部门",
            status: "状态",
            action: "操作",
            edit: "编辑",
            resetPassword: "重置密码",
            assignRoles: "分配角色",
            delete: "删除"
        },
        dialog: {
            createTitle: "新增用户",
            editTitle: "编辑用户",
            userName: "用户名",
            nickName: "昵称",
            phone: "手机号",
            phonePlaceholder: "请输入手机号",
            email: "邮箱",
            emailPlaceholder: "请输入邮箱",
            sex: "性别",
            sexPlaceholder: "请选择",
            dept: "部门",
            deptPlaceholder: "请选择",
            status: "状态",
            statusPlaceholder: "请选择",
            statusEnabled: "启用",
            statusDisabled: "禁用",
            initialPassword: "初始密码",
            dataScopeType: "数据范围类型",
            dataScopePlaceholder: "请选择",
            dataScopeValue: "数据范围值",
            dataScopeValuePlaceholder: "逗号分隔 ID",
            remark: "备注"
        },
        scope: {
            all: "全部",
            dept: "本部门",
            deptAndChild: "部门及下级",
            custom: "自定义",
            customDept: "自定义部门",
            self: "仅本人",
            none: "无权限"
        },
        sex: {
            male: "男",
            female: "女",
            unknown: "-"
        },
        roles: {
            title: "分配角色",
            list: "角色列表",
            placeholder: "请选择"
        },
        reset: {
            title: "重置密码",
            newPassword: "新密码",
            confirmPassword: "确认密码"
        },
        msg: {
            loadFailed: "加载用户失败",
            createSuccess: "创建成功",
            createFailed: "创建失败",
            updateSuccess: "更新成功",
            updateFailed: "更新失败",
            saveFailed: "保存失败",
            statusUpdateFailed: "状态更新失败",
            rolesUpdated: "角色已更新",
            rolesUpdateFailed: "角色更新失败",
            passwordReset: "密码已重置",
            passwordResetFailed: "密码重置失败",
            validateUserName: "请输入用户名",
            validatePassword: "请输入新密码",
            validatePasswordConfirm: "两次输入的密码不一致",
            deleteConfirm: "确认删除用户 {name}？",
            batchDeleteConfirm: "确认删除选中的 {count} 个用户？",
            deleteSuccess: "删除成功",
            deleteFailed: "删除失败",
            deleteEmpty: "请先选择要删除的用户"
        }
    },
    notice: {
        title: "系统通知",
        subtitle: "发布系统通知并追踪阅读状态。",
        filter: {
            keywordPlaceholder: "标题/内容关键词",
            scopePlaceholder: "通知范围",
            search: "查询",
            publish: "发布通知",
            delete: "批量删除"
        },
        scope: {
            all: "全部",
            dept: "部门",
            role: "角色",
            user: "用户"
        },
        table: {
            title: "标题",
            scope: "范围",
            readSummary: "已读/总数",
            publisher: "发布人",
            publishTime: "发布时间",
            action: "操作",
            detail: "详情",
            delete: "删除"
        },
        publish: {
            title: "发布系统通知",
            titleLabel: "通知标题",
            titlePlaceholder: "请输入通知标题",
            contentLabel: "通知内容",
            contentPlaceholder: "请输入通知内容",
            scopeLabel: "通知范围",
            scopePlaceholder: "请选择范围",
            targetDept: "接收部门",
            targetRole: "接收角色",
            targetUser: "接收用户",
            targetDefault: "接收对象",
            targetDeptPlaceholder: "请选择部门",
            targetRolePlaceholder: "请选择角色",
            targetUserPlaceholder: "请选择用户",
            targetDefaultPlaceholder: "请选择",
            publish: "发布"
        },
        detail: {
            title: "通知详情",
            scopeLabel: "范围：",
            publisherLabel: "发布人：",
            publishTimeLabel: "发布时间：",
            userName: "用户名",
            nickName: "昵称",
            dept: "部门",
            status: "状态",
            statusRead: "已读",
            statusUnread: "未读",
            readTime: "阅读时间",
            close: "关闭"
        },
        msg: {
            loadFailed: "加载通知失败",
            publishSuccess: "通知发布成功",
            publishFailed: "通知发布失败",
            detailLoadFailed: "加载详情失败",
            validateTitle: "请输入通知标题",
            validateContent: "请输入通知内容",
            validateTarget: "请选择接收对象",
            deleteConfirm: "确认删除通知《{title}》？",
            batchDeleteConfirm: "确认删除选中的 {count} 条通知？",
            deleteSuccess: "删除成功",
            deleteFailed: "删除失败",
            deleteEmpty: "请先选择要删除的通知"
        }
    }
};
