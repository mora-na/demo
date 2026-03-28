import axios, {type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig} from "axios";
import {API_BASE_URL} from "../config/api";

const TOKEN_KEY = "demo-token";
const USER_KEY = "demo-user";
const CLIENT_REQUEST_ID_HEADER = "X-Client-Request-Id";
const TRACE_ID_HEADER = "x-trace-id";
const UPSTREAM_TRACE_ID_HEADER = "x-upstream-trace-id";
const CF_RAY_HEADER = "x-cf-ray";
let redirectingToLogin = false;

const withCredentials = String(import.meta.env.VITE_API_WITH_CREDENTIALS || "").toLowerCase() === "true";

type RequestMetadata = {
    clientRequestId: string;
    startedAt: number;
};

type TraceableRequestConfig = InternalAxiosRequestConfig & {
    metadata?: RequestMetadata;
};

const now = () => Date.now();

const createRequestId = () => {
    if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
        return crypto.randomUUID();
    }
    return `req-${now()}-${Math.random().toString(16).slice(2, 10)}`;
};

const buildRequestUrl = (config?: Partial<InternalAxiosRequestConfig>) => {
    if (!config?.url) {
        return API_BASE_URL;
    }
    if (/^https?:\/\//i.test(config.url)) {
        return config.url;
    }
    const baseURL = config.baseURL || API_BASE_URL;
    return `${String(baseURL).replace(/\/+$/, "")}/${String(config.url).replace(/^\/+/, "")}`;
};

const readHeader = (response?: AxiosResponse | null, headerName?: string) => {
    if (!response || !headerName) {
        return undefined;
    }
    const value = response.headers?.[headerName];
    return Array.isArray(value) ? value.join(",") : value;
};

const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 15000,
    withCredentials
});

api.interceptors.request.use((config: TraceableRequestConfig) => {
    const clientRequestId = createRequestId();
    const startedAt = now();
    const token = localStorage.getItem("demo-token");
    config.metadata = {
        clientRequestId,
        startedAt
    };
    config.headers = config.headers || {};
    config.headers[CLIENT_REQUEST_ID_HEADER] = clientRequestId;
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    console.info("[api][request]", {
        clientRequestId,
        method: String(config.method || "GET").toUpperCase(),
        url: buildRequestUrl(config),
        timeout: config.timeout
    });
    return config;
});

api.interceptors.response.use(
    (response: AxiosResponse) => {
        const config = response.config as TraceableRequestConfig;
        const clientRequestId = config.metadata?.clientRequestId || readHeader(response, CLIENT_REQUEST_ID_HEADER.toLowerCase());
        const durationMs = config.metadata ? now() - config.metadata.startedAt : undefined;
        console.info("[api][response]", {
            clientRequestId,
            method: String(config.method || "GET").toUpperCase(),
            url: buildRequestUrl(config),
            status: response.status,
            durationMs,
            traceId: readHeader(response, TRACE_ID_HEADER),
            upstreamTraceId: readHeader(response, UPSTREAM_TRACE_ID_HEADER),
            cfRay: readHeader(response, CF_RAY_HEADER)
        });
        return response;
    },
    (error: AxiosError) => {
        const config = error.config as TraceableRequestConfig | undefined;
        const clientRequestId = config?.metadata?.clientRequestId
            || (typeof error.response?.headers?.[CLIENT_REQUEST_ID_HEADER.toLowerCase()] === "string"
                ? error.response.headers[CLIENT_REQUEST_ID_HEADER.toLowerCase()]
                : undefined);
        const durationMs = config?.metadata ? now() - config.metadata.startedAt : undefined;
        console.error("[api][error]", {
            clientRequestId,
            method: String(config?.method || "GET").toUpperCase(),
            url: buildRequestUrl(config),
            status: error.response?.status,
            durationMs,
            code: error.code,
            message: error.message,
            traceId: typeof error.response?.headers?.[TRACE_ID_HEADER] === "string"
                ? error.response.headers[TRACE_ID_HEADER]
                : undefined,
            upstreamTraceId: typeof error.response?.headers?.[UPSTREAM_TRACE_ID_HEADER] === "string"
                ? error.response.headers[UPSTREAM_TRACE_ID_HEADER]
                : undefined,
            cfRay: typeof error.response?.headers?.[CF_RAY_HEADER] === "string"
                ? error.response.headers[CF_RAY_HEADER]
                : undefined
        });
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
