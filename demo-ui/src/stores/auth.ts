import {computed, ref} from "vue";
import {defineStore} from "pinia";

const TOKEN_KEY = "demo-token";
const USER_KEY = "demo-user";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || "");
  const userName = ref<string>(localStorage.getItem(USER_KEY) || "");

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
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  return {
    token,
    userName,
    isAuthenticated,
    setSession,
    clearSession
  };
});
