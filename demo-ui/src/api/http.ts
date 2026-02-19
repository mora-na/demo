import axios from "axios";

const TOKEN_KEY = "demo-token";
const USER_KEY = "demo-user";
let redirectingToLogin = false;

const withCredentials = String(import.meta.env.VITE_API_WITH_CREDENTIALS || "").toLowerCase() === "true";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "/prod-api",
    timeout: 15000,
    withCredentials
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("demo-token");
    if (token) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error?.response?.status === 401) {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(USER_KEY);
            const base = (import.meta.env.BASE_URL || "/").replace(/\/+$/, "");
            const loginPath = `${base}/login` || "/login";
            const isOnLogin = window.location.pathname === loginPath || window.location.pathname.endsWith("/login");
            if (!isOnLogin && !redirectingToLogin) {
                redirectingToLogin = true;
                const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`;
                window.location.replace(`${loginPath}?redirect=${encodeURIComponent(currentPath)}`);
            }
        }
        return Promise.reject(error);
    }
);

export default api;
