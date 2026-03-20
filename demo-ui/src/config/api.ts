const DEFAULT_API_PREFIX = "/prod-api";

const rawOrigin = (import.meta.env.VITE_API_ORIGIN || "").trim();
const rawBaseUrl = (import.meta.env.VITE_API_BASE_URL || "").trim();

const stripTrailingSlashes = (value: string) => value.replace(/\/+$/, "");

const joinOriginWithPrefix = (origin: string) => {
    const normalizedOrigin = stripTrailingSlashes(origin);
    const normalizedPrefix = DEFAULT_API_PREFIX.replace(/^\/+/, "");
    return `${normalizedOrigin}/${normalizedPrefix}`;
};

export const API_BASE_URL = rawOrigin
    ? joinOriginWithPrefix(rawOrigin)
    : rawBaseUrl
        ? stripTrailingSlashes(rawBaseUrl)
        : DEFAULT_API_PREFIX;
