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
  }
};
