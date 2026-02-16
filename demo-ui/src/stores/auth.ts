import {computed, ref} from "vue";
import {defineStore} from "pinia";
import {i18n} from "../i18n";
import {fetchProfile, type MenuTree, type UserProfileInfo, type UserRoleTarget} from "../api/auth";

const TOKEN_KEY = "demo-token";
const USER_KEY = "demo-user";

function resolveErrorMessage(error: unknown, fallback: string): string {
    const err = error as { response?: { data?: { message?: string } }; message?: string };
    return err?.response?.data?.message || err?.message || fallback;
}

export const useAuthStore = defineStore("auth", () => {
    const token = ref<string>(localStorage.getItem(TOKEN_KEY) || "");
    const userName = ref<string>(localStorage.getItem(USER_KEY) || "");
    const profile = ref<UserProfileInfo | null>(null);
    const roles = ref<string[]>([]);
    const roleTargets = ref<UserRoleTarget[]>([]);
    const permissions = ref<string[]>([]);
    const menus = ref<MenuTree[]>([]);
    const passwordChangeRequired = ref(false);
    const profileLoaded = ref(false);

    const isAuthenticated = computed(() => Boolean(token.value));

    function setSession(newToken: string, name: string, requirePasswordChange = false) {
        token.value = newToken;
        userName.value = name;
        passwordChangeRequired.value = requirePasswordChange;
        localStorage.setItem(TOKEN_KEY, newToken);
        localStorage.setItem(USER_KEY, name);
    }

    function clearSession() {
        token.value = "";
        userName.value = "";
        profile.value = null;
        roles.value = [];
        roleTargets.value = [];
        permissions.value = [];
        menus.value = [];
        passwordChangeRequired.value = false;
        profileLoaded.value = false;
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    }

    async function loadProfile(force = false) {
        if (!token.value) {
            return {ok: false, message: i18n.global.t("common.missingToken")};
        }
        if (profileLoaded.value && !force) {
            return {ok: true};
        }
        try {
            const result = await fetchProfile();
            if (result?.code === 200 && result.data) {
                profile.value = result.data.user;
                roles.value = result.data.roles || [];
                roleTargets.value = result.data.roleTargets || [];
                permissions.value = result.data.permissions || [];
                menus.value = result.data.menus || [];
                passwordChangeRequired.value = Boolean(result.data.passwordChangeRequired);
                profileLoaded.value = true;
                if (result.data.user?.userName) {
                    userName.value = result.data.user.userName;
                    localStorage.setItem(USER_KEY, result.data.user.userName);
                }
                return {ok: true};
            }
            return {ok: false, message: result?.message || i18n.global.t("common.profileLoadFailed")};
        } catch (error) {
            return {ok: false, message: resolveErrorMessage(error, i18n.global.t("common.profileLoadFailed"))};
        }
    }

    return {
        token,
        userName,
        profile,
        roles,
        roleTargets,
        permissions,
        menus,
        passwordChangeRequired,
        profileLoaded,
        isAuthenticated,
        setSession,
        clearSession,
        loadProfile
    };
});
