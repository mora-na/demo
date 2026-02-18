export default {
    app: {
        checking: "Checking login status…"
    },
    common: {
        online: "Online",
        userFallback: "User",
        roleEmpty: "Unassigned",
        entry: "Entry point",
        cancel: "Cancel",
        save: "Save",
        search: "Search",
        edit: "Edit",
        delete: "Delete",
        enabled: "Enabled",
        disabled: "Disabled",
        yes: "Yes",
        no: "No",
        saveSuccess: "Saved",
        saveFailed: "Save failed",
        deleteSuccess: "Deleted",
        deleteFailed: "Delete failed",
        confirmTitle: "Confirmation",
        missingToken: "Missing login token",
        profileLoadFailed: "Failed to load user profile"
    },
    login: {
        badge: "Demo Access",
        title: "Enter the demo securely.",
        lede: "Sign in with your account, complete the captcha, and match the backend transport mode.",
        transport: {
            title: "Transport Mode",
            note: "Passwords stay encrypted during transport."
        },
        tokenTtl: {
            title: "Token TTL",
            value: "{hours} hours",
            note: "Based on server configuration."
        },
        security: {
            title: "Security Policy",
            value: "Captcha",
            note: "Required before every login."
        },
        status: {
            system: "System Status",
            online: "Online",
            level: "Security Level",
            controlled: "Controlled",
            window: "Login Window",
            realtime: "Live refresh"
        },
        panel: {
            title: "Welcome back",
            subtitle: "Verify your identity to continue."
        },
        form: {
            username: "Username",
            usernamePlaceholder: "Enter username",
            password: "Password",
            passwordPlaceholder: "Enter password",
            captcha: "Captcha",
            captchaPlaceholder: "Enter captcha",
            captchaLoading: "Load captcha",
            submit: "Sign in",
            helper: "After signing in, you'll land on the console and can access demo modules."
        },
        msg: {
            captchaLoadFailed: "Failed to load captcha",
            fillAll: "Please complete all fields.",
            loginFailed: "Login failed",
            profileLoadFailed: "Failed to load user profile",
            passwordChangeRequired: "Your account must change password before continuing.",
            welcomeUser: "Welcome, {name}!"
        }
    },
    home: {
        nav: {
            badge: "Console",
            title: "Demo System",
            sub: "Modular Management Center",
            section: "Modules",
            empty: "No accessible menus",
            expand: "Expand navigation",
            collapse: "Collapse navigation"
        },
        topbar: {
            title: "Console Overview",
            chooseModule: "Select a module first",
            searchPlaceholder: "Search menus, paths, or permissions",
            notifications: "Notifications",
            settings: "Settings",
            logout: "Sign out"
        },
        notice: {
            title: "System Notices",
            streamOnline: "Live",
            streamOffline: "Offline"
        },
        main: {
            titleFallback: "Console",
            descFallback: "Pick a module from the left to view details",
            empty: "No accessible submenus",
            newTask: "New task",
            metrics: {
                group: "Modules",
                submenu: "Submenus",
                role: "Roles",
                permission: "Permissions"
            }
        },
        profile: {
            title: "Profile Settings",
            userName: "Username",
            nickName: "Nickname",
            nickNamePlaceholder: "Enter nickname",
            phone: "Phone",
            phonePlaceholder: "Enter phone",
            email: "Email",
            emailPlaceholder: "Enter email",
            sex: "Sex",
            sexPlaceholder: "Select",
            sexMale: "Male",
            sexFemale: "Female",
            oldPassword: "Current password",
            newPassword: "New password",
            confirmPassword: "Confirm new password",
            note: "Leave blank if you don't want to change the password.",
            forceNote: "Security policy requires this account to change password first.",
            msg: {
                noChanges: "No changes to save",
                fillPassword: "Complete all password fields",
                confirmMismatch: "New passwords do not match",
                forcePasswordRequired: "You must change password before continuing",
                saveFailed: "Failed to update profile",
                saveSuccess: "Profile updated"
            }
        },
        msg: {
            profileLoadFailed: "Failed to load user profile",
            logoutSuccess: "Signed out",
            logoutFailed: "Sign out failed"
        }
    },
    job: {
        title: "Scheduled Jobs",
        subtitle: "Manage scheduled jobs and execution strategies.",
        filter: {
            namePlaceholder: "Job name",
            handlerPlaceholder: "Handler",
            statusPlaceholder: "Status",
            search: "Search",
            create: "New job"
        },
        table: {
            name: "Job name",
            handler: "Handler",
            cron: "Cron",
            nextFireTime: "Next run",
            concurrent: "Concurrent",
            status: "Status",
            action: "Actions",
            yes: "Yes",
            no: "No",
            edit: "Edit",
            run: "Run now",
            logs: "Run logs",
            delete: "Delete"
        },
        dialog: {
            createTitle: "New job",
            editTitle: "Edit job",
            name: "Job name",
            namePlaceholder: "Enter job name",
            handler: "Handler",
            handlerPlaceholder: "Select a handler",
            cron: "Cron expression",
            cronPlaceholder: "e.g. 0 0/5 * * * ?",
            misfire: "Misfire policy",
            misfirePlaceholder: "Select a policy",
            misfireDefault: "Default",
            misfireIgnore: "Ignore misfires",
            misfireFire: "Fire and proceed",
            misfireDoNothing: "Do nothing",
            concurrent: "Allow concurrent",
            concurrentPlaceholder: "Select",
            concurrentYes: "Allow",
            concurrentNo: "Disallow",
            status: "Job status",
            statusPlaceholder: "Select",
            statusEnabled: "Enabled",
            statusDisabled: "Disabled",
            params: "Params",
            paramsPlaceholder: "Optional, JSON or string",
            remark: "Remark"
        },
        cronHelper: {
            open: "Builder",
            title: "Cron Expression Builder",
            template: "Pattern",
            templatePlaceholder: "Select",
            groups: {
                frequency: "By frequency",
                timePoint: "By exact time",
                date: "By date",
                combo: "Combination"
            },
            templates: {
                freqSeconds: "Every N seconds (Quartz)",
                freqMinutes: "Every N minutes",
                freqHours: "Every N hours",
                freqDays: "Every N days",
                freqWeeks: "Every N weeks (weekday)",
                freqMonths: "Every N months (day)",
                freqYears: "Yearly on date",
                timeFixed: "Fixed time (daily)",
                timeMulti: "Multiple times",
                timeRange: "Time range",
                dateMonthDays: "Specific days of month",
                dateWeekdays: "Specific weekdays",
                dateLastDay: "Last day of month (L)",
                dateNearestWeekday: "Nearest weekday (W)",
                dateNthWeekday: "Nth weekday (#)",
                comboWorkdaysHours: "Workdays + hours",
                comboQuarterStart: "Quarter + month start",
                comboRangeStep: "Range + step"
            },
            intervalSeconds: "Second interval",
            intervalMinutes: "Minute interval",
            intervalHours: "Hour interval",
            intervalDays: "Day interval",
            intervalWeeks: "Week interval",
            intervalMonths: "Month interval",
            hour: "Hour",
            minute: "Minute",
            second: "Second",
            hours: "Hours",
            minutes: "Minutes",
            hoursPlaceholder: "Select hours",
            minutesPlaceholder: "Select minutes",
            time: "Run time",
            rangeStartHour: "Start hour",
            rangeEndHour: "End hour",
            stepMinutes: "Minute step",
            nth: "Nth",
            months: "Months",
            monthsPlaceholder: "Select months",
            weekday: "Weekday",
            weekdayPlaceholder: "Select",
            weekdays: {
                mon: "Mon",
                tue: "Tue",
                wed: "Wed",
                thu: "Thu",
                fri: "Fri",
                sat: "Sat",
                sun: "Sun"
            },
            dayOfMonth: "Day of month (1-31)",
            dayOfMonthPlaceholder: "Select days",
            preview: "Expression preview",
            apply: "Use this expression",
            invalid: "Generated expression is invalid"
        },
        logs: {
            title: "Execution logs",
            task: "Job",
            handler: "Handler",
            status: "Status",
            statusSuccess: "Success",
            statusFail: "Failed",
            startTime: "Start time",
            duration: "Duration (ms)",
            message: "Failure reason",
            action: "Action",
            viewLog: "View log",
            detailTitle: "Execution log",
            emptyLog: "No log content",
            close: "Close"
        },
        msg: {
            loadFailed: "Failed to load jobs",
            createSuccess: "Job created",
            createFailed: "Failed to create job",
            updateSuccess: "Job updated",
            updateFailed: "Failed to update job",
            saveFailed: "Failed to save job",
            statusUpdated: "Status updated",
            statusUpdateFailed: "Failed to update status",
            runSuccess: "Job triggered",
            runFailed: "Failed to trigger job",
            deleteSuccess: "Job deleted",
            deleteFailed: "Failed to delete job",
            loadLogFailed: "Failed to load logs",
            loadLogDetailFailed: "Failed to load execution log",
            validateName: "Enter a job name",
            validateHandler: "Select a handler",
            validateCron: "Enter a Cron expression"
        }
    },
    systemPanel: {
        title: "System Management",
        subtitle: "Manage users, permissions, and organizational structure.",
        placeholder: "Select a system management tab.",
        tabs: {
            user: "Users",
            role: "Roles",
            menu: "Menus",
            dept: "Departments",
            post: "Posts",
            permission: "Permissions",
            dict: "Dictionaries",
            notice: "Notices",
            job: "Scheduled Jobs",
            dataScope: "Data Scope"
        }
    },
    dict: {
        title: "Dictionary Management",
        subtitle: "Maintain dictionary types and entries.",
        cacheRefresh: "Refresh Cache",
        type: {
            title: "Dictionary Types",
            create: "New Type",
            createTitle: "Create Dictionary Type",
            editTitle: "Edit Dictionary Type",
            dictType: "Type Code",
            dictName: "Type Name",
            status: "Status",
            sort: "Sort",
            remark: "Remark",
            action: "Action",
            filterType: "Type code",
            filterName: "Type name",
            filterStatus: "Status"
        },
        data: {
            title: "Dictionary Data",
            create: "New Entry",
            createTitle: "Create Dictionary Entry",
            editTitle: "Edit Dictionary Entry",
            dictLabel: "Label",
            dictValue: "Value",
            status: "Status",
            sort: "Sort",
            remark: "Remark",
            action: "Action",
            filterLabel: "Label",
            filterValue: "Value",
            filterStatus: "Status"
        },
        msg: {
            loadFailed: "Failed to load dictionaries.",
            validateType: "Please fill type and name.",
            validateData: "Please fill label and value.",
            deleteTypeConfirm: "Delete dictionary type \"{name}\"?",
            deleteDataConfirm: "Delete dictionary entry \"{name}\"?",
            selectType: "Select a dictionary type first.",
            selectData: "Select a row to edit.",
            cacheRefreshed: "Dictionary cache refreshed.",
            cacheRefreshFailed: "Failed to refresh cache."
        }
    },
    monitorPanel: {
        title: "System Monitor",
        subtitle: "Runtime and login auditing.",
        placeholder: "Select a monitor tab.",
        tabs: {
            operLog: "Operation Logs",
            loginLog: "Login Logs"
        }
    },
    extensionPanel: {
        title: "API Extension",
        subtitle: "Manage dynamic endpoints and audit calls.",
        placeholder: "Select an extension tab.",
        tabs: {
            dynamicApi: "Dynamic APIs",
            dynamicApiLog: "API Logs"
        }
    },
    operLog: {
        title: "Operation Logs",
        subtitle: "Audit backend actions and API behavior.",
        filter: {
            user: "Operator",
            title: "Module",
            type: "Type",
            status: "Status",
            begin: "Start time",
            end: "End time",
            reset: "Reset"
        },
        table: {
            time: "Time",
            user: "Operator",
            title: "Module",
            operation: "Operation",
            type: "Type",
            method: "Method",
            ip: "IP",
            status: "Status",
            cost: "Cost",
            error: "Error"
        },
        type: {
            other: "Other",
            insert: "Create",
            update: "Update",
            delete: "Delete",
            grant: "Grant",
            export: "Export",
            import: "Import",
            forceLogout: "Force logout",
            clean: "Clean"
        },
        status: {
            success: "Success",
            fail: "Failed"
        },
        msg: {
            loadFailed: "Failed to load operation logs"
        }
    },
    loginLog: {
        title: "Login Logs",
        subtitle: "Track login, logout, and exceptions.",
        filter: {
            user: "Account",
            ip: "IP",
            type: "Type",
            status: "Status",
            begin: "Start time",
            end: "End time",
            reset: "Reset"
        },
        table: {
            time: "Time",
            user: "Account",
            type: "Type",
            status: "Status",
            ip: "IP",
            location: "Location",
            browser: "Browser",
            os: "OS",
            device: "Device",
            msg: "Message"
        },
        type: {
            login: "Login",
            logout: "Logout"
        },
        status: {
            success: "Success",
            fail: "Failed"
        },
        msg: {
            loadFailed: "Failed to load login logs"
        }
    },
    dynamicApi: {
        title: "Dynamic APIs",
        subtitle: "Create internal endpoints safely.",
        filter: {
            path: "Path",
            method: "Method",
            status: "Status",
            type: "Type",
            authMode: "Auth",
            reset: "Reset",
            create: "New API",
            reload: "Reload"
        },
        table: {
            path: "Path",
            method: "Method",
            type: "Type",
            status: "Status",
            authMode: "Auth",
            rateLimit: "Rate limit",
            timeout: "Timeout",
            config: "Config",
            updatedAt: "Updated",
            action: "Action"
        },
        dialog: {
            createTitle: "Create Dynamic API",
            editTitle: "Edit Dynamic API",
            path: "Path",
            pathPlaceholder: "/ext/your/path",
            method: "Method",
            methodPlaceholder: "Select method",
            type: "Type",
            typePlaceholder: "Select type",
            beanName: "Bean",
            beanNamePlaceholder: "Select bean",
            paramMode: "Param mode",
            paramModePlaceholder: "Select parameter mode",
            paramModeHint: "AUTO merges path/query/JSON body. MULTIPART is for file uploads.",
            paramSchema: "Param schema",
            paramSchemaPlaceholder: "Optional JSON example or schema",
            sql: "SQL",
            sqlPlaceholder: "SELECT ...",
            httpUrl: "Forward URL",
            httpUrlPlaceholder: "https://internal/api",
            httpMethod: "Forward method",
            httpMethodPlaceholder: "Follow request",
            httpPassHeaders: "Pass headers",
            httpPassQuery: "Pass query",
            status: "Status",
            statusPlaceholder: "Select status",
            authMode: "Auth mode",
            authModePlaceholder: "Select auth mode",
            rateLimit: "Rate limit policy",
            rateLimitPlaceholder: "Optional policy key",
            timeout: "Timeout (ms)",
            timeoutPlaceholder: "Optional",
            config: "Config JSON",
            configPlaceholder: "{\"beanName\":\"...\",\"paramMode\":\"AUTO\"}",
            remark: "Remark"
        },
        paramMode: {
            auto: "Auto merge",
            query: "Query params",
            bodyJson: "JSON body",
            form: "Form params",
            multipart: "Multipart files"
        },
        http: {
            followRequest: "Follow request"
        },
        status: {
            draft: "Draft",
            enabled: "Enabled",
            disabled: "Disabled"
        },
        auth: {
            inherit: "Inherit",
            public: "Public"
        },
        action: {
            enable: "Enable",
            disable: "Disable"
        },
        msg: {
            loadFailed: "Failed to load dynamic APIs",
            reloadSuccess: "Reloaded",
            reloadFailed: "Reload failed",
            enableSuccess: "Enabled",
            disableSuccess: "Disabled",
            statusFailed: "Failed to change status",
            deleteConfirm: "Delete dynamic API {path}?",
            validate: "Fill path, method, and type.",
            validateBean: "Select handler bean.",
            validateSql: "Fill SQL statement.",
            validateHttp: "Fill forward URL.",
            beanMetaFailed: "Failed to load bean list.",
            policyLoadFailed: "Failed to load rate limit policies."
        }
    },
    dynamicApiLog: {
        title: "Dynamic API Logs",
        subtitle: "Trace dynamic endpoint calls.",
        filter: {
            apiPath: "API path",
            apiMethod: "Method",
            user: "User",
            status: "Status",
            begin: "Start time",
            end: "End time",
            reset: "Reset"
        },
        table: {
            time: "Time",
            api: "API",
            method: "Method",
            status: "Status",
            code: "Code",
            duration: "Duration",
            user: "User",
            ip: "IP",
            error: "Error",
            action: "Action"
        },
        status: {
            success: "Success",
            fail: "Failed"
        },
        msg: {
            loadFailed: "Failed to load logs",
            deleteConfirm: "Delete log #{id}?"
        }
    },
    dataScope: {
        title: "Data Scope",
        subtitle: "Configure data visibility by role, menu, and user overrides.",
        tabs: {
            overview: "Overview",
            mapping: "Field Mapping",
            user: "User Overrides"
        },
        overview: {
            userPlaceholder: "Select user",
            menuPlaceholder: "Select menu (optional)",
            search: "Search",
            userInfo: "User Info",
            userName: "User",
            dept: "Department",
            posts: "Posts",
            roles: "Roles",
            result: "Result",
            finalScope: "Final scope",
            deptIds: "Dept IDs",
            includeSelf: "Include self",
            sql: "SQL condition",
            table: {
                menu: "Menu",
                permission: "Permission",
                scope: "Final scope",
                source: "Layer"
            },
            empty: "Select a user to query",
            userRequired: "Please select a user",
            loadFailed: "Failed to load data scope"
        },
        mapping: {
            scopeKey: "Permission key",
            tableName: "Table name",
            tableAlias: "Alias",
            deptColumn: "Dept column",
            userColumn: "User column",
            status: "Status",
            action: "Actions",
            create: "New mapping",
            edit: "Edit mapping",
            validate: "Fill permission key and table name",
            loadFailed: "Failed to load mappings",
            deleteConfirm: "Delete mapping {key}?"
        },
        user: {
            userName: "User",
            menuKeyword: "Menu/permission",
            statusPlaceholder: "Status",
            create: "New override",
            edit: "Edit override",
            user: "User",
            dept: "Department",
            menu: "Menu",
            scopeKey: "Permission key",
            scopeType: "Scope type",
            scopeValue: "Scope value",
            status: "Status",
            remark: "Remark",
            action: "Actions",
            userPlaceholder: "Select user",
            global: "Global override",
            validate: "Select user and permission key",
            loadFailed: "Failed to load overrides",
            deleteConfirm: "Delete override for {name}?"
        }
    },
    post: {
        title: "Post Management",
        subtitle: "Maintain posts and their departments.",
        create: "New post",
        filter: {
            delete: "Delete selected"
        },
        table: {
            name: "Name",
            code: "Code",
            dept: "Department",
            sort: "Sort",
            status: "Status",
            action: "Actions",
            edit: "Edit",
            delete: "Delete"
        },
        dialog: {
            createTitle: "New post",
            editTitle: "Edit post",
            name: "Name",
            code: "Code",
            dept: "Department",
            deptPlaceholder: "Select",
            sort: "Sort",
            status: "Status",
            statusPlaceholder: "Select",
            statusEnabled: "Enabled",
            statusDisabled: "Disabled",
            remark: "Remark"
        },
        msg: {
            loadFailed: "Failed to load posts",
            createSuccess: "Created",
            createFailed: "Create failed",
            updateSuccess: "Updated",
            updateFailed: "Update failed",
            saveFailed: "Save failed",
            statusUpdateFailed: "Failed to update status",
            validateName: "Enter post name",
            deleteConfirm: "Delete post {name}?",
            batchDeleteConfirm: "Delete the selected {count} posts?",
            deleteSuccess: "Deleted",
            deleteFailed: "Delete failed",
            deleteEmpty: "Select posts to delete"
        }
    },
    dept: {
        title: "Department Management",
        subtitle: "Maintain departments and hierarchy.",
        create: "New department",
        filter: {
            delete: "Delete selected"
        },
        table: {
            name: "Name",
            code: "Code",
            parent: "Parent",
            sort: "Sort",
            status: "Status",
            action: "Actions",
            edit: "Edit",
            delete: "Delete"
        },
        dialog: {
            createTitle: "New department",
            editTitle: "Edit department",
            name: "Name",
            code: "Code",
            parent: "Parent department",
            parentPlaceholder: "Select",
            sort: "Sort",
            status: "Status",
            statusPlaceholder: "Select",
            statusEnabled: "Enabled",
            statusDisabled: "Disabled",
            remark: "Remark"
        },
        msg: {
            loadFailed: "Failed to load departments",
            createSuccess: "Created",
            createFailed: "Create failed",
            updateSuccess: "Updated",
            updateFailed: "Update failed",
            saveFailed: "Save failed",
            statusUpdateFailed: "Failed to update status",
            validateName: "Enter department name",
            deleteConfirm: "Delete department {name}?",
            batchDeleteConfirm: "Delete the selected {count} departments?",
            deleteSuccess: "Deleted",
            deleteFailed: "Delete failed",
            deleteEmpty: "Select departments to delete"
        }
    },
    menu: {
        title: "Menu Management",
        subtitle: "Maintain system menus and frontend routes.",
        create: "New menu",
        filter: {
            delete: "Delete selected"
        },
        table: {
            name: "Name",
            code: "Code",
            parent: "Parent",
            path: "Path",
            permission: "Permission",
            sort: "Sort",
            status: "Status",
            action: "Actions",
            edit: "Edit",
            delete: "Delete"
        },
        dialog: {
            createTitle: "New menu",
            editTitle: "Edit menu",
            name: "Name",
            code: "Code",
            parent: "Parent menu",
            parentPlaceholder: "Select",
            path: "Path",
            component: "Component",
            permission: "Permission key",
            sort: "Sort",
            status: "Status",
            statusPlaceholder: "Select",
            statusEnabled: "Enabled",
            statusDisabled: "Disabled",
            remark: "Remark"
        },
        msg: {
            loadFailed: "Failed to load menus",
            createSuccess: "Created",
            createFailed: "Create failed",
            updateSuccess: "Updated",
            updateFailed: "Update failed",
            saveFailed: "Save failed",
            statusUpdateFailed: "Failed to update status",
            validateName: "Enter menu name",
            deleteConfirm: "Delete menu {name}?",
            batchDeleteConfirm: "Delete the selected {count} menus?",
            deleteSuccess: "Deleted",
            deleteFailed: "Delete failed",
            deleteEmpty: "Select menus to delete"
        }
    },
    permission: {
        title: "Permission Management",
        subtitle: "Maintain permission keys and names.",
        create: "New permission",
        filter: {
            delete: "Delete selected"
        },
        table: {
            code: "Code",
            name: "Name",
            status: "Status",
            action: "Actions",
            edit: "Edit",
            delete: "Delete"
        },
        dialog: {
            createTitle: "New permission",
            editTitle: "Edit permission",
            code: "Permission code",
            name: "Permission name",
            status: "Status",
            statusPlaceholder: "Select",
            statusEnabled: "Enabled",
            statusDisabled: "Disabled"
        },
        msg: {
            loadFailed: "Failed to load permissions",
            createSuccess: "Created",
            createFailed: "Create failed",
            updateSuccess: "Updated",
            updateFailed: "Update failed",
            saveFailed: "Save failed",
            statusUpdateFailed: "Failed to update status",
            validateForm: "Enter code and name",
            deleteConfirm: "Delete permission {name}?",
            batchDeleteConfirm: "Delete the selected {count} permissions?",
            deleteSuccess: "Deleted",
            deleteFailed: "Delete failed",
            deleteEmpty: "Select permissions to delete"
        }
    },
    role: {
        title: "Role Management",
        subtitle: "Maintain roles, permissions, and menu assignments.",
        create: "New role",
        filter: {
            delete: "Delete selected"
        },
        table: {
            code: "Code",
            name: "Name",
            dataScope: "Data scope",
            status: "Status",
            action: "Actions",
            edit: "Edit",
            assignPermissions: "Assign permissions",
            assignMenus: "Assign menus",
            menuDataScope: "Menu scope",
            delete: "Delete"
        },
        tabs: {
            basic: "Basic",
            dataScope: "Data scope",
            menuScope: "Menu-level scope"
        },
        dialog: {
            createTitle: "New role",
            editTitle: "Edit role",
            code: "Role code",
            name: "Role name",
            status: "Status",
            statusPlaceholder: "Select",
            statusEnabled: "Enabled",
            statusDisabled: "Disabled",
            dataScopeType: "Data scope type",
            dataScopePlaceholder: "Select",
            dataScopeValue: "Data scope value",
            dataScopeValuePlaceholder: "Comma-separated IDs",
            customDept: "Select custom departments"
        },
        scope: {
            all: "All",
            dept: "Dept only",
            deptAndChild: "Dept & children",
            custom: "Custom",
            customDept: "Custom dept",
            self: "Self only",
            none: "No access"
        },
        permissions: {
            title: "Assign permissions",
            list: "Permission list",
            placeholder: "Select"
        },
        menus: {
            title: "Assign menus"
        },
        menuScope: {
            title: "Menu data scope",
            hint: "Configure only menus that need special scope; others inherit role defaults.",
            open: "Open config",
            scopeType: "Data scope",
            inherit: "Inherit role default",
            clear: "Clear",
            apply: "Apply to menu",
            empty: "Select a menu",
            needRole: "Save role before configuring menu scope"
        },
        msg: {
            loadFailed: "Failed to load roles",
            createSuccess: "Created",
            createFailed: "Create failed",
            updateSuccess: "Updated",
            updateFailed: "Update failed",
            saveFailed: "Save failed",
            statusUpdateFailed: "Failed to update status",
            validateForm: "Enter role code and name",
            permissionsUpdated: "Permissions updated",
            permissionsUpdateFailed: "Failed to update permissions",
            menusUpdated: "Menus updated",
            menusUpdateFailed: "Failed to update menus",
            deleteConfirm: "Delete role {name}?",
            batchDeleteConfirm: "Delete the selected {count} roles?",
            deleteSuccess: "Deleted",
            deleteFailed: "Delete failed",
            deleteEmpty: "Select roles to delete"
        }
    },
    user: {
        title: "User Management",
        subtitle: "Manage users, status, and role relations.",
        filter: {
            userNamePlaceholder: "Username",
            nickNamePlaceholder: "Nickname",
            statusPlaceholder: "Status",
            search: "Search",
            create: "New user",
            delete: "Delete selected"
        },
        table: {
            userName: "Username",
            nickName: "Nickname",
            phone: "Phone",
            email: "Email",
            sex: "Sex",
            dept: "Department",
            status: "Status",
            action: "Actions",
            edit: "Edit",
            resetPassword: "Reset password",
            assignRoles: "Assign roles",
            assignPosts: "Assign posts",
            dataScope: "Data scope",
            delete: "Delete"
        },
        dialog: {
            createTitle: "New user",
            editTitle: "Edit user",
            userName: "Username",
            nickName: "Nickname",
            phone: "Phone",
            phonePlaceholder: "Enter phone",
            email: "Email",
            emailPlaceholder: "Enter email",
            sex: "Sex",
            sexPlaceholder: "Select",
            dept: "Department",
            deptPlaceholder: "Select",
            status: "Status",
            statusPlaceholder: "Select",
            statusEnabled: "Enabled",
            statusDisabled: "Disabled",
            initialPassword: "Initial password",
            dataScopeType: "Data scope type",
            dataScopePlaceholder: "Select",
            dataScopeValue: "Data scope value",
            dataScopeValuePlaceholder: "Comma-separated IDs",
            remark: "Remark"
        },
        scope: {
            all: "All",
            dept: "Dept only",
            deptAndChild: "Dept & children",
            custom: "Custom",
            customDept: "Custom dept",
            self: "Self only",
            none: "No access"
        },
        dataScope: {
            title: "User Data Scope",
            user: "User",
            dept: "Department",
            create: "New scope",
            menu: "Menu",
            scopeKey: "Permission key",
            scopeType: "Scope type",
            scopeValue: "Scope value",
            status: "Status",
            remark: "Remark",
            action: "Actions",
            global: "Global override",
            validate: "Select permission key",
            loadFailed: "Failed to load user scopes",
            deleteConfirm: "Delete scope for {name}?",
            edit: "Edit scope"
        },
        sex: {
            male: "Male",
            female: "Female",
            unknown: "-"
        },
        roles: {
            title: "Assign roles",
            list: "Role list",
            placeholder: "Select"
        },
        posts: {
            title: "Assign posts",
            list: "Post list",
            placeholder: "Select"
        },
        reset: {
            title: "Reset password",
            newPassword: "New password",
            confirmPassword: "Confirm password"
        },
        msg: {
            loadFailed: "Failed to load users",
            createSuccess: "Created",
            createFailed: "Create failed",
            updateSuccess: "Updated",
            updateFailed: "Update failed",
            saveFailed: "Save failed",
            statusUpdateFailed: "Failed to update status",
            rolesUpdated: "Roles updated",
            rolesUpdateFailed: "Failed to update roles",
            postsUpdated: "Posts updated",
            postsUpdateFailed: "Failed to update posts",
            passwordReset: "Password reset",
            passwordResetFailed: "Failed to reset password",
            validateUserName: "Enter username",
            validatePassword: "Enter new password",
            validatePasswordConfirm: "Passwords do not match",
            deleteConfirm: "Delete user {name}?",
            batchDeleteConfirm: "Delete the selected {count} users?",
            deleteSuccess: "Deleted",
            deleteFailed: "Delete failed",
            deleteEmpty: "Select users to delete"
        }
    },
    order: {
        title: "Order Management",
        subtitle: "Review and maintain order records.",
        filter: {
            userIdPlaceholder: "User ID",
            minAmountPlaceholder: "Min amount",
            maxAmountPlaceholder: "Max amount",
            search: "Search"
        },
        table: {
            id: "Order ID",
            user: "Customer",
            amount: "Amount",
            createdAt: "Placed at",
            remark: "Remark",
            action: "Actions",
            delete: "Delete"
        },
        msg: {
            loadFailed: "Failed to load orders",
            deleteConfirm: "Delete order #{id}?",
            deleteSuccess: "Order deleted",
            deleteFailed: "Failed to delete order"
        }
    },
    notice: {
        title: "System Notices",
        subtitle: "Publish notices and track read status.",
        filter: {
            keywordPlaceholder: "Title/content keyword",
            scopePlaceholder: "Scope",
            search: "Search",
            publish: "Publish",
            delete: "Delete selected"
        },
        scope: {
            all: "All",
            dept: "Department",
            role: "Role",
            user: "User"
        },
        table: {
            title: "Title",
            scope: "Scope",
            readSummary: "Read/Total",
            publisher: "Publisher",
            publishTime: "Published at",
            action: "Actions",
            detail: "Detail",
            delete: "Delete"
        },
        publish: {
            title: "Publish notice",
            titleLabel: "Title",
            titlePlaceholder: "Enter notice title",
            contentLabel: "Content",
            contentPlaceholder: "Enter notice content",
            scopeLabel: "Scope",
            scopePlaceholder: "Select scope",
            targetDept: "Target dept",
            targetRole: "Target role",
            targetUser: "Target user",
            targetDefault: "Targets",
            targetDeptPlaceholder: "Select departments",
            targetRolePlaceholder: "Select roles",
            targetUserPlaceholder: "Select users",
            targetDefaultPlaceholder: "Select",
            publish: "Publish"
        },
        detail: {
            title: "Notice detail",
            scopeLabel: "Scope: ",
            publisherLabel: "Publisher: ",
            publishTimeLabel: "Published at: ",
            userName: "Username",
            nickName: "Nickname",
            dept: "Department",
            status: "Status",
            statusRead: "Read",
            statusUnread: "Unread",
            readTime: "Read time",
            close: "Close"
        },
        msg: {
            loadFailed: "Failed to load notices",
            publishSuccess: "Notice published",
            publishFailed: "Failed to publish notice",
            detailLoadFailed: "Failed to load detail",
            validateTitle: "Enter notice title",
            validateContent: "Enter notice content",
            validateTarget: "Select recipients",
            deleteConfirm: "Delete notice \"{title}\"?",
            batchDeleteConfirm: "Delete the selected {count} notices?",
            deleteSuccess: "Deleted",
            deleteFailed: "Delete failed",
            deleteEmpty: "Select notices to delete"
        }
    }
};
