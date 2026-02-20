import {createApp} from "vue";
import ElementPlus from "element-plus";
import {createPinia} from "pinia";
import App from "./App.vue";
import {i18n} from "./i18n";
import {useAuthStore} from "./stores/auth";
import router from "./router";
import "element-plus/dist/index.css";
import "./style.css";

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
app.use(i18n);
app.use(ElementPlus);
app.use(router);

const connection = (navigator as Navigator & { connection?: { saveData?: boolean } }).connection;
const reduceMotion = window.matchMedia?.("(prefers-reduced-motion: reduce)")?.matches || connection?.saveData;
if (reduceMotion) {
    document.documentElement.dataset.reduceMotion = "true";
}

type PermissionValue = string | string[];

function normalizePermissions(value: PermissionValue | undefined | null) {
    if (!value) {
        return [];
    }
    return Array.isArray(value) ? value : [value];
}

function applyPermission(
    el: HTMLElement,
    value: PermissionValue | undefined | null,
    modifiers: Partial<Record<string, boolean>> | undefined
) {
    const authStore = useAuthStore(pinia);
    const required = normalizePermissions(value);
    if (!required.length) {
        el.style.display = "";
        return;
    }
    const userPermissions = authStore.permissions || [];
    const requireAll = Boolean(modifiers?.all);
    const allowed = requireAll
        ? required.every((item) => userPermissions.includes(item))
        : required.some((item) => userPermissions.includes(item));
    el.style.display = allowed ? "" : "none";
}

app.directive("permission", {
    mounted(el, binding) {
        applyPermission(el as HTMLElement, binding.value, binding.modifiers);
    },
    updated(el, binding) {
        applyPermission(el as HTMLElement, binding.value, binding.modifiers);
    }
});

app.mount("#app");
