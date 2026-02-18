import {createRouter, createWebHistory} from "vue-router";
import LoginPage from "../pages/LoginPage.vue";
import HomePage from "../pages/HomePage.vue";
import {useAuthStore} from "../stores/auth";

const transportMode = (import.meta.env.VITE_PASSWORD_TRANSPORT_MODE || "plain").toUpperCase();

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: "/",
            redirect: "/home"
        },
        {
            path: "/login",
            name: "login",
            component: LoginPage,
            props: () => ({transportMode})
        },
        {
            path: "/home/:pathMatch(.*)*",
            name: "home",
            component: HomePage,
            meta: {requiresAuth: true}
        },
        {
            path: "/:pathMatch(.*)*",
            redirect: "/home"
        }
    ]
});

router.beforeEach(async (to) => {
    const authStore = useAuthStore();
    try {
        if (to.meta?.requiresAuth) {
            if (!authStore.token) {
                return {name: "login"};
            }
            const result = await authStore.loadProfile();
            if (!result.ok) {
                authStore.clearSession();
                return {name: "login"};
            }
            return true;
        }
        if (to.name === "login" && authStore.token) {
            const result = await authStore.loadProfile();
            if (result.ok) {
                return {name: "home"};
            }
            authStore.clearSession();
        }
        return true;
    } catch (_error) {
        authStore.clearSession();
        if (to.name === "login") {
            return true;
        }
        return {name: "login"};
    }
});

export default router;
