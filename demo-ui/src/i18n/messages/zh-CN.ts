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
        search: "搜索",
        refresh: "刷新",
        edit: "编辑",
        delete: "删除",
        enabled: "启用",
        disabled: "停用",
        yes: "是",
        no: "否",
        saveSuccess: "保存成功",
        saveFailed: "保存失败",
        deleteSuccess: "删除成功",
        deleteFailed: "删除失败",
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
            passwordChangeRequired: "当前账号需要先修改密码，请先完成密码更新。",
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
            streamOffline: "断线",
            streamReconnect: "重连"
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
            forceNote: "因安全策略要求，当前账号必须先完成密码修改。",
            msg: {
                noChanges: "没有需要保存的修改",
                fillPassword: "请完整填写密码修改字段",
                confirmMismatch: "两次输入的新密码不一致",
                forcePasswordRequired: "当前登录必须先修改密码",
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
            logCollectLevel: "日志收集级别",
            logCollectLevelPlaceholder: "请选择级别",
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
                timePoint: "按时间点（精确）",
                date: "按日期",
                frequency: "按频率（间隔）",
                combo: "组合条件"
            },
            templates: {
                freqSeconds: "每 N 秒（Quartz）",
                freqMinutes: "每 N 分钟",
                freqHours: "每 N 小时",
                freqDays: "每月内每 N 天",
                freqWeeks: "每周内按星期步进",
                freqMonths: "每 N 月（指定日期）",
                freqYears: "每年指定日期",
                timeFixed: "固定时刻（每天）",
                timeMulti: "多个时刻（列举）",
                timeRange: "时段内（范围）",
                dateMonthDays: "每月指定日",
                dateWeekdays: "每周指定星期",
                dateLastDay: "每月最后一天（L）",
                dateLastDayOffset: "每月倒数第 N 天（L-n）",
                dateLastWeekday: "每月最后一个工作日（LW）",
                dateLastWeekdayOfMonth: "每月最后一个星期几（xL）",
                dateNearestWeekday: "最近工作日（W）",
                dateNthWeekday: "第 N 个星期几（#）",
                dateYear: "指定年份日期",
                comboWorkdaysHours: "工作日 + 工作时间",
                comboQuarterStart: "季度 + 月初",
                comboRangeStep: "范围 + 步长"
            },
            intervalSeconds: "秒间隔",
            intervalMinutes: "分钟间隔",
            intervalHours: "小时间隔",
            intervalDays: "天步进",
            intervalWeeks: "星期步进",
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
            year: "年份",
            lastDayOffset: "倒数天数",
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
            nextRuns: "未来 5 次执行时间",
            nextRunsEmpty: "暂无可预览的执行时间",
            apply: "使用该表达式",
            invalid: "生成的表达式无效",
            timeZone: "时区"
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
        metrics: {
            title: "任务日志监控",
            subtitle: "JobLogCollector 缓冲状态。",
            buffers: "缓冲占用",
            maxLength: "单次最大长度 {value}",
            hold: "保留时间",
            mergeDelay: "合并延迟 {value}ms",
            degrade: "降级状态",
            degraded: "已降级",
            normal: "正常",
            enabled: "启用",
            disabled: "关闭",
            autoDegrade: "自动降级",
            enabledLabel: "采集状态",
            ratio: "阈值 {value}",
            loadFailed: "加载监控指标失败"
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
            post: "岗位管理",
            permission: "权限管理",
            dict: "字典管理",
            notice: "系统通知",
            job: "定时任务",
            dataScope: "数据权限"
        }
    },
    dict: {
        title: "字典管理",
        subtitle: "维护系统字典类型与数据项。",
        cacheRefresh: "刷新缓存",
        type: {
            title: "字典类型",
            create: "新增类型",
            createTitle: "新增字典类型",
            editTitle: "编辑字典类型",
            dictType: "字典类型",
            dictName: "字典名称",
            status: "状态",
            sort: "排序",
            remark: "备注",
            action: "操作",
            filterType: "类型编码",
            filterName: "类型名称",
            filterStatus: "状态"
        },
        data: {
            title: "字典数据",
            create: "新增数据",
            createTitle: "新增字典数据",
            editTitle: "编辑字典数据",
            dictLabel: "字典标签",
            dictValue: "字典值",
            status: "状态",
            sort: "排序",
            remark: "备注",
            action: "操作",
            filterLabel: "标签",
            filterValue: "值",
            filterStatus: "状态"
        },
        msg: {
            loadFailed: "加载字典失败",
            validateType: "请填写字典类型与名称",
            validateData: "请填写字典标签与值",
            deleteTypeConfirm: "确认删除字典类型【{name}】吗？",
            deleteDataConfirm: "确认删除字典数据【{name}】吗？",
            selectType: "请先选择字典类型",
            selectData: "请选择需要编辑的数据",
            cacheRefreshed: "字典缓存已刷新",
            cacheRefreshFailed: "刷新缓存失败"
        }
    },
    monitorPanel: {
        title: "系统监控",
        subtitle: "运行与登录审计。",
        placeholder: "请选择系统监控子菜单。",
        tabs: {
            operLog: "操作日志",
            loginLog: "登录日志",
            noticeStream: "通知流监控",
            jobLog: "任务日志监控",
            druid: "数据源监控"
        }
    },
    druid: {
        title: "数据源监控",
        subtitle: "Druid 运行与数据源监控。",
        loadFailed: "加载数据源监控失败",
        unavailable: "未检测到 Druid 监控数据。",
        generatedAt: "更新时间 {time}",
        menu: {
            title: "监控菜单",
            home: "首页",
            datasource: "数据源",
            sql: "SQL监控",
            wall: "SQL防火墙",
            webapp: "Web应用",
            weburi: "URI监控",
            session: "Session监控",
            spring: "Spring监控",
            json: "JSON API"
        },
        home: {
            table: {
                label: "指标",
                value: "数值",
                desc: "解释"
            },
            rows: {
                startTime: {
                    label: "启动时间",
                    desc: "服务启动时间"
                },
                version: {
                    label: "版本",
                    desc: "Druid 版本信息"
                },
                driver: {
                    label: "驱动",
                    desc: "已加载驱动"
                },
                resetEnable: {
                    label: "是否允许重置",
                    desc: "是否允许重置监控数据"
                },
                resetCount: {
                    label: "重置次数",
                    desc: "累计重置次数"
                },
                javaVersion: {
                    label: "Java版本",
                    desc: "运行 Java 版本"
                },
                jvmName: {
                    label: "JVM名称",
                    desc: "当前 JVM 名称"
                },
                classPath: {
                    label: "classpath路径",
                    desc: "Java classpath 路径"
                }
            }
        },
        datasource: {
            selector: "选择数据源",
            noData: "暂无数据源",
            table: {
                label: "指标",
                field: "字段名",
                value: "数值",
                desc: "解释"
            },
            extraTitle: "其他字段",
            stackTitle: "活跃连接堆栈查看",
            poolTitle: "连接池中连接信息",
            sqlListTitle: "sql列表",
            fields: {
                userName: "用户名",
                url: "连接地址",
                dbType: "数据库类型",
                driverClassName: "驱动类名",
                filterClassNames: "过滤器类名",
                testOnBorrow: "获取连接时检测",
                testWhileIdle: "空闲时检测",
                testOnReturn: "连接放回连接池时检测",
                initialSize: "初始化连接大小",
                minIdle: "最小空闲连接数",
                maxActive: "最大连接数",
                queryTimeout: "查询超时时间",
                transactionQueryTimeout: "事务查询超时时间",
                loginTimeout: "登录超时时间",
                validConnectionChecker: "连接有效性检查类名",
                exceptionSorter: "异常排序器类名",
                defaultAutoCommit: "默认自动提交",
                defaultReadOnly: "默认只读设置",
                defaultTransactionIsolation: "默认事务隔离",
                minEvictableIdleTimeMillis: "最小可回收空闲时间",
                maxEvictableIdleTimeMillis: "最大可回收空闲时间",
                keepAlive: "保活",
                failFast: "快速失败",
                poolPreparedStatements: "启用预编译语句池",
                maxPoolPreparedStatementPerConnectionSize: "单连接预编译语句缓存上限",
                maxWait: "最大等待时间",
                maxWaitThreadCount: "最大等待线程数",
                logDifferentThread: "记录不同线程",
                useUnfairLock: "使用非公平锁",
                initGlobalVariants: "初始化全局变量",
                initVariants: "初始化变量",
                connectCount: "累计总次数",
                waitTime: "等待总时长",
                waitThreadCount: "等待线程数量",
                transactionCount: "事务启动数",
                transactionHistogram: "事务时间分布",
                poolingCount: "池中连接数",
                poolingPeak: "池中连接数峰值",
                poolingPeakTime: "池中连接数峰值时间",
                activeCount: "活跃连接数",
                activePeak: "活跃连接数峰值",
                activePeakTime: "活跃连接数峰值时间",
                logicConnectCount: "逻辑连接打开次数",
                logicCloseCount: "逻辑连接关闭次数",
                logicConnectErrorCount: "逻辑连接错误次数",
                discardCount: "校验失败废弃连接数",
                logicConnectReuseCount: "逻辑连接回收重用次数",
                physicalConnectCount: "物理连接打开次数",
                physicalCloseCount: "物理关闭数量",
                physicalConnectErrorCount: "物理连接错误次数",
                executeCount: "执行数",
                executeQueryCount: "查询执行数",
                executeUpdateCount: "更新执行数",
                executeBatchCount: "批量执行数",
                errorCount: "错误数",
                commitCount: "提交数",
                rollbackCount: "回滚数",
                preparedStatementOpenCount: "预编译语句打开次数",
                preparedStatementCloseCount: "预编译语句关闭次数",
                psCacheAccessCount: "PS 缓存访问次数",
                psCacheHitCount: "PS 缓存命中次数",
                psCacheMissCount: "PS 缓存未命中次数",
                connectionHoldTimeHistogram: "连接持有时间分布",
                clobOpenCount: "CLOB 打开次数",
                blobOpenCount: "BLOB 打开次数",
                keepAliveCheckCount: "保活检测次数"
            }
        },
        extra: {
            key: "字段",
            field: "字段名",
            value: "值",
            desc: "解释"
        },
        sql: {
            refreshLabel: "SQL刷新时间",
            refresh: {
                none: "不自动刷新",
                five: "5 秒",
                ten: "10 秒",
                thirty: "30 秒"
            },
            detailTitle: "SQL 明细",
            columns: {
                index: "N",
                sql: "SQL",
                datasource: "数据源",
                executeCount: "执行数",
                totalTime: "执行时间",
                maxTime: "最慢",
                transactionCount: "事务执行",
                errorCount: "错误数",
                updateCount: "更新行数",
                fetchCount: "读取行数",
                runningCount: "执行中",
                concurrentMax: "最大并发",
                executeHistogram: "执行时间分布",
                executeRsHistogram: "执行+RS时分布",
                fetchHistogram: "读取行分布",
                updateHistogram: "更新行分布"
            }
        },
        wall: {
            sections: {
                summary: "防火墙概览",
                list: "黑白名单",
                table: "表级规则",
                sql: "SQL 规则",
                other: "其他"
            }
        },
        webapp: {
            sections: {
                summary: "Web应用统计"
            },
            columns: {
                contextPath: "应用路径",
                runningCount: "执行中",
                concurrentMax: "最大并发",
                requestCount: "请求次数",
                requestTime: "请求时间（和）",
                jdbcExecuteCount: "Jdbc执行数",
                jdbcExecuteTime: "Jdbc时间",
                jdbcCommitCount: "事务提交数",
                jdbcRollbackCount: "事务回滚数",
                errorCount: "错误数"
            }
        },
        weburi: {
            columns: {
                index: "N",
                uri: "URI",
                requestCount: "请求次数",
                requestTime: "请求时间（和）",
                requestTimeMax: "请求最慢（单次）",
                runningCount: "执行中",
                concurrentMax: "最大并发",
                jdbcExecuteCount: "Jdbc执行数",
                jdbcExecuteErrorCount: "Jdbc出错数",
                jdbcExecuteTime: "Jdbc时间",
                jdbcCommitCount: "事务提交数",
                jdbcRollbackCount: "事务回滚数",
                fetchRowCount: "读取行数",
                updateRowCount: "更新行数",
                histogram: "区间分布"
            }
        },
        session: {
            columns: {
                index: "N",
                sessionId: "会话",
                principal: "用户",
                createTime: "创建时间",
                lastAccessTime: "最后访问时间",
                remoteAddress: "远端IP",
                requestCount: "请求次数",
                requestTimeTotal: "总请求时长",
                runningCount: "执行中",
                concurrentMax: "最大并发",
                jdbcExecuteCount: "Jdbc执行数",
                jdbcExecuteTime: "Jdbc时间",
                jdbcCommitCount: "事务提交数",
                jdbcRollbackCount: "事务回滚数",
                fetchRowCount: "读取行数",
                updateRowCount: "更新行数"
            }
        },
        spring: {
            columns: {
                index: "N",
                className: "类名",
                method: "方法",
                executeCount: "执行数",
                totalTime: "总耗时",
                runningCount: "执行中",
                concurrentMax: "最大并发",
                errorCount: "错误数",
                jdbcCommitCount: "事务提交数",
                jdbcRollbackCount: "事务回滚数",
                fetchRowCount: "读取行数",
                updateRowCount: "更新行数"
            }
        },
        jsonApi: {
            table: {
                name: "名称",
                path: "路径"
            },
            items: {
                basic: "basic.json",
                datasource: "datasource.json",
                datasourceDetail: "datasource-{id}.json",
                activeConnectionStack: "activeConnectionStackTrace-{datasourceId}.json",
                sql: "sql.json",
                wallStat: "wallStat.json",
                wall: "wall-{id}.json",
                weburi: "weburi.json",
                websession: "websession.json",
                resetAll: "reset-all.json"
            }
        },
        columns: {
            sql: "SQL",
            sqlhash: "SQL 哈希",
            datasource: "数据源",
            datasourceid: "数据源ID",
            datasourcename: "数据源名称",
            dbtype: "数据库类型",
            url: "连接地址",
            username: "用户名",
            driverclassname: "驱动类名",
            filterclassnames: "Filter 类名",
            executecount: "执行数",
            totaltime: "总耗时",
            maxtime: "最慢",
            avgtime: "平均耗时",
            errorcount: "错误数",
            effectedrowcount: "更新行数",
            updaterowcount: "更新行数",
            fetchrowcount: "读取行数",
            runningcount: "执行中",
            concurrentmax: "最大并发",
            executetime: "执行时间",
            executetimehistogram: "执行时间分布",
            executeandresultsetholdtimehistogram: "执行+RS时分布",
            fetchrowcounthistogram: "读取行分布",
            updaterowcounthistogram: "更新行分布",
            intransactioncount: "事务执行",
            transactioncount: "事务执行",
            requestcount: "请求次数",
            requesttime: "请求时间（和）",
            requesttimemax: "请求最慢（单次）",
            contextpath: "应用路径",
            uri: "URI",
            sessionid: "会话",
            principal: "用户",
            createtime: "创建时间",
            lastaccesstime: "最后访问时间",
            remoteaddress: "远端IP",
            class: "类名",
            method: "方法",
            jdbcexecutecount: "Jdbc执行数",
            jdbcexecutetime: "Jdbc时间",
            jdbcexecuteerrorcount: "Jdbc出错数",
            jdbcfetchrowcount: "Jdbc读取行数",
            jdbcupdaterowcount: "Jdbc更新行数",
            jdbccommitcount: "事务提交数",
            jdbcrollbackcount: "事务回滚数",
            histogram: "区间分布"
        }
    },
    extensionPanel: {
        title: "接口扩展",
        subtitle: "动态接口配置与调用审计。",
        placeholder: "请选择扩展模块页签。",
        tabs: {
            dynamicApi: "动态接口",
            dynamicApiLog: "调用日志"
        }
    },
    operLog: {
        title: "操作日志",
        subtitle: "记录后台操作与接口行为。",
        filter: {
            user: "操作人",
            title: "模块",
            type: "类型",
            status: "状态",
            begin: "开始时间",
            end: "结束时间",
            reset: "重置"
        },
        table: {
            time: "时间",
            user: "操作人",
            title: "模块",
            operation: "操作",
            type: "类型",
            method: "方法",
            ip: "IP",
            status: "状态",
            cost: "耗时",
            error: "异常"
        },
        type: {
            other: "其他",
            insert: "新增",
            update: "修改",
            delete: "删除",
            grant: "授权",
            export: "导出",
            import: "导入",
            forceLogout: "强退",
            clean: "清空"
        },
        status: {
            success: "成功",
            fail: "失败"
        },
        msg: {
            loadFailed: "操作日志加载失败"
        }
    },
    loginLog: {
        title: "登录日志",
        subtitle: "记录登录、登出与异常行为。",
        filter: {
            user: "账号",
            ip: "IP",
            type: "类型",
            status: "状态",
            begin: "开始时间",
            end: "结束时间",
            reset: "重置"
        },
        table: {
            time: "时间",
            user: "账号",
            type: "类型",
            status: "状态",
            ip: "IP",
            location: "归属地",
            browser: "浏览器",
            os: "系统",
            device: "设备",
            msg: "提示"
        },
        type: {
            login: "登录",
            logout: "登出"
        },
        status: {
            success: "成功",
            fail: "失败"
        },
        msg: {
            loadFailed: "登录日志加载失败"
        }
    },
    dynamicApi: {
        title: "动态接口",
        subtitle: "为内部系统按需扩展接口。",
        filter: {
            path: "路径",
            method: "方法",
            status: "状态",
            type: "类型",
            authMode: "鉴权",
            reset: "重置",
            create: "新建接口",
            reload: "重新加载"
        },
        table: {
            path: "路径",
            method: "方法",
            type: "类型",
            status: "状态",
            authMode: "鉴权",
            rateLimit: "限流策略",
            timeout: "超时",
            config: "配置",
            updatedAt: "更新时间",
            action: "操作"
        },
        dialog: {
            createTitle: "新建动态接口",
            editTitle: "编辑动态接口",
            path: "路径",
            pathPlaceholder: "/ext/your/path",
            method: "方法",
            methodPlaceholder: "选择方法",
            type: "类型",
            typePlaceholder: "选择类型",
            beanName: "Bean",
            beanNamePlaceholder: "选择 Bean",
            paramMode: "参数模式",
            paramModePlaceholder: "选择参数模式",
            paramModeHint: "AUTO 会合并路径/查询/JSON Body；MULTIPART 用于文件上传。",
            paramSchema: "参数结构",
            paramSchemaPlaceholder: "可选，JSON 示例或结构说明",
            sql: "SQL",
            sqlPlaceholder: "SELECT ...",
            httpUrl: "转发地址",
            httpUrlPlaceholder: "https://internal/api",
            httpMethod: "转发方法",
            httpMethodPlaceholder: "跟随请求",
            httpPassHeaders: "透传请求头",
            httpPassQuery: "透传查询参数",
            status: "状态",
            statusPlaceholder: "选择状态",
            authMode: "鉴权模式",
            authModePlaceholder: "选择鉴权模式",
            rateLimit: "限流策略",
            rateLimitPlaceholder: "可选策略标识",
            timeout: "超时(毫秒)",
            timeoutPlaceholder: "可选",
            config: "配置JSON",
            configPlaceholder: "{\"beanName\":\"...\",\"paramMode\":\"AUTO\"}",
            remark: "备注"
        },
        paramMode: {
            auto: "自动合并",
            query: "查询参数",
            bodyJson: "JSON 请求体",
            form: "表单参数",
            multipart: "文件上传"
        },
        http: {
            followRequest: "跟随请求"
        },
        status: {
            draft: "草稿",
            enabled: "启用",
            disabled: "停用"
        },
        auth: {
            inherit: "继承",
            public: "公开"
        },
        action: {
            enable: "启用",
            disable: "停用"
        },
        msg: {
            loadFailed: "加载动态接口失败",
            reloadSuccess: "已重新加载",
            reloadFailed: "重新加载失败",
            enableSuccess: "已启用",
            disableSuccess: "已停用",
            statusFailed: "状态更新失败",
            deleteConfirm: "确认删除动态接口 {path}？",
            validate: "请填写路径、方法与类型。",
            validateBean: "请选择 Handler Bean。",
            validateSql: "请填写 SQL 语句。",
            validateHttp: "请填写转发地址。",
            validateConfig: "请填写配置 JSON。",
            beanMetaFailed: "加载 Bean 列表失败。",
            policyLoadFailed: "加载限流策略失败。",
            typeLoadFailed: "加载类型列表失败。"
        }
    },
    dynamicApiLog: {
        title: "动态接口日志",
        subtitle: "跟踪动态接口调用情况。",
        filter: {
            apiPath: "接口路径",
            apiMethod: "方法",
            user: "用户",
            status: "状态",
            begin: "开始时间",
            end: "结束时间",
            reset: "重置"
        },
        table: {
            time: "时间",
            api: "接口",
            method: "方法",
            status: "状态",
            code: "响应码",
            duration: "耗时",
            user: "用户",
            ip: "IP",
            error: "错误",
            params: "参数",
            errorDetails: "错误详情",
            meta: "元数据",
            trace: "TraceId",
            action: "操作"
        },
        status: {
            success: "成功",
            fail: "失败"
        },
        msg: {
            loadFailed: "加载日志失败",
            deleteConfirm: "确认删除日志 #{id}？"
        }
    },
    dataScope: {
        title: "数据权限",
        subtitle: "按角色、菜单与用户特例配置数据范围。",
        tabs: {
            overview: "权限总览",
            mapping: "字段映射配置",
            user: "用户特例授权"
        },
        overview: {
            userPlaceholder: "选择用户",
            menuPlaceholder: "选择菜单（可选）",
            search: "查询",
            userInfo: "用户信息",
            userName: "用户",
            dept: "部门",
            posts: "岗位",
            roles: "角色",
            result: "解析结果",
            finalScope: "最终范围",
            deptIds: "部门集合",
            includeSelf: "包含本人",
            sql: "SQL 条件",
            table: {
                menu: "菜单",
                permission: "权限标识",
                scope: "最终范围",
                source: "生效层级"
            },
            empty: "请选择用户后查询",
            userRequired: "请先选择用户",
            loadFailed: "加载解析失败"
        },
        mapping: {
            scopeKey: "权限标识",
            tableName: "业务表名",
            tableAlias: "表别名",
            deptColumn: "部门字段",
            userColumn: "用户字段",
            status: "状态",
            action: "操作",
            create: "新增映射",
            edit: "编辑映射",
            validate: "请填写权限标识与表名",
            loadFailed: "加载字段映射失败",
            deleteConfirm: "确认删除 {key} 的映射吗？"
        },
        user: {
            userName: "用户",
            menuKeyword: "菜单/权限",
            statusPlaceholder: "状态",
            create: "新增特例",
            edit: "编辑特例",
            user: "用户",
            dept: "部门",
            menu: "菜单",
            scopeKey: "权限标识",
            scopeType: "范围类型",
            scopeValue: "范围值",
            status: "状态",
            remark: "备注",
            action: "操作",
            userPlaceholder: "选择用户",
            global: "全局覆盖",
            validate: "请选择用户和权限标识",
            loadFailed: "加载用户特例失败",
            deleteConfirm: "确认删除 {name} 的特例吗？"
        }
    },
    post: {
        title: "岗位管理",
        subtitle: "维护岗位与所属部门关系。",
        create: "新增岗位",
        filter: {
            delete: "批量删除"
        },
        table: {
            name: "名称",
            code: "编码",
            dept: "所属部门",
            sort: "排序",
            status: "状态",
            action: "操作",
            edit: "编辑",
            delete: "删除"
        },
        dialog: {
            createTitle: "新增岗位",
            editTitle: "编辑岗位",
            name: "名称",
            code: "编码",
            dept: "所属部门",
            deptPlaceholder: "请选择",
            sort: "排序",
            status: "状态",
            statusPlaceholder: "请选择",
            statusEnabled: "启用",
            statusDisabled: "禁用",
            remark: "备注"
        },
        msg: {
            loadFailed: "加载岗位失败",
            createSuccess: "创建成功",
            createFailed: "创建失败",
            updateSuccess: "更新成功",
            updateFailed: "更新失败",
            saveFailed: "保存失败",
            statusUpdateFailed: "状态更新失败",
            validateName: "请输入岗位名称",
            deleteConfirm: "确认删除岗位 {name}？",
            batchDeleteConfirm: "确认删除选中的 {count} 个岗位？",
            deleteSuccess: "删除成功",
            deleteFailed: "删除失败",
            deleteEmpty: "请先选择要删除的岗位"
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
            menuDataScope: "菜单范围",
            delete: "删除"
        },
        tabs: {
            basic: "基本信息",
            dataScope: "数据范围",
            menuScope: "菜单级数据范围"
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
            dataScopeValuePlaceholder: "逗号分隔 ID",
            customDept: "选择自定义部门"
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
        menuScope: {
            title: "菜单级数据范围配置",
            hint: "仅对需要特殊控制的菜单设置范围，未配置则继承角色默认范围。",
            open: "打开配置",
            scopeType: "数据范围",
            inherit: "继承角色默认",
            clear: "清除配置",
            apply: "应用到该菜单",
            empty: "请选择一个菜单",
            needRole: "请先保存角色再配置菜单范围"
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
            assignPosts: "分配岗位",
            dataScope: "数据范围",
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
        dataScope: {
            title: "用户数据范围",
            user: "用户",
            dept: "部门",
            create: "新增范围",
            menu: "菜单",
            scopeKey: "权限标识",
            scopeType: "范围类型",
            scopeValue: "范围值",
            status: "状态",
            remark: "备注",
            action: "操作",
            global: "全局覆盖",
            validate: "请选择权限标识",
            loadFailed: "加载用户范围失败",
            deleteConfirm: "确认删除 {name} 的范围吗？",
            edit: "编辑范围"
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
        posts: {
            title: "分配岗位",
            list: "岗位列表",
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
            postsUpdated: "岗位已更新",
            postsUpdateFailed: "岗位更新失败",
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
    order: {
        title: "订单管理",
        subtitle: "查看并维护系统内的订单记录。",
        filter: {
            userIdPlaceholder: "用户ID",
            minAmountPlaceholder: "最小金额",
            maxAmountPlaceholder: "最大金额",
            search: "查询"
        },
        table: {
            id: "订单ID",
            user: "下单用户",
            amount: "订单金额",
            createdAt: "下单时间",
            remark: "备注",
            action: "操作",
            delete: "删除"
        },
        msg: {
            loadFailed: "订单加载失败",
            deleteConfirm: "确认删除订单 #{id} 吗？",
            deleteSuccess: "订单已删除",
            deleteFailed: "订单删除失败"
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
        metrics: {
            title: "通知流监控",
            subtitle: "SSE 连接与缓存运行状态。",
            connections: "连接数",
            activeUsers: "活跃用户 {count}",
            cache: "最新缓存",
            latestLimit: "latestLimit {value}",
            degrade: "降级状态",
            degraded: "已降级",
            normal: "正常",
            autoDegrade: "自动降级",
            enabled: "启用",
            disabled: "关闭",
            thresholds: "阈值比例",
            ratios: "连接 {conn}% / 缓存 {cache}%",
            cacheExpire: "缓存过期 {minutes} 分钟",
            loadFailed: "加载监控指标失败"
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
