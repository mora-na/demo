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
  }
};
