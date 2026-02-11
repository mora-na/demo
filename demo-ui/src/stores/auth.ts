import {computed, ref} from "vue";
import {defineStore} from "pinia";
import {fetchProfile, type MenuTree, type UserProfileInfo} from "../api/auth";

const TOKEN_KEY = "demo-token";
const USER_KEY = "demo-user";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || "");
  const userName = ref<string>(localStorage.getItem(USER_KEY) || "");
  const profile = ref<UserProfileInfo | null>(null);
  const roles = ref<string[]>([]);
  const permissions = ref<string[]>([]);
  const menus = ref<MenuTree[]>([]);
  const profileLoaded = ref(false);

  const isAuthenticated = computed(() => Boolean(token.value));

  function setSession(newToken: string, name: string) {
    token.value = newToken;
    userName.value = name;
    localStorage.setItem(TOKEN_KEY, newToken);
    localStorage.setItem(USER_KEY, name);
  }

  function clearSession() {
    token.value = "";
    userName.value = "";
    profile.value = null;
    roles.value = [];
    permissions.value = [];
    menus.value = [];
    profileLoaded.value = false;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  async function loadProfile(force = false) {
    if (!token.value) {
      return {ok: false, message: "缺少登录令牌"};
    }
    if (profileLoaded.value && !force) {
      return {ok: true};
    }
    const result = await fetchProfile();
    if (result?.code === 200 && result.data) {
      profile.value = result.data.user;
      roles.value = result.data.roles || [];
      permissions.value = result.data.permissions || [];
      menus.value = result.data.menus || [];
      profileLoaded.value = true;
      if (result.data.user?.userName) {
        userName.value = result.data.user.userName;
        localStorage.setItem(USER_KEY, result.data.user.userName);
      }
      return {ok: true};
    }
    return {ok: false, message: result?.message || "用户信息加载失败"};
  }

  return {
    token,
    userName,
    profile,
    roles,
    permissions,
    menus,
    profileLoaded,
    isAuthenticated,
    setSession,
    clearSession,
    loadProfile
  };
});
