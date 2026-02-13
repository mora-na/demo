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
            msg: {
                noChanges: "No changes to save",
                fillPassword: "Complete all password fields",
                confirmMismatch: "New passwords do not match",
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
            message: "Message",
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
            permission: "Permissions",
            notice: "Notices",
            job: "Scheduled Jobs"
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
            delete: "Delete"
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
            dataScopeValuePlaceholder: "Comma-separated IDs"
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
