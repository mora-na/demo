import type {MenuTree} from "../api/auth";

export function normalizeMenuPath(path?: string): string {
    if (!path) {
        return "";
    }
    const trimmed = path.trim();
    if (!trimmed) {
        return "";
    }
    const normalized = trimmed.startsWith("/") ? trimmed.slice(1) : trimmed;
    return normalized
        .split("/")
        .filter(Boolean)
        .join("/")
        .toLowerCase();
}

export function buildHomePath(path?: string): string {
    const normalized = normalizeMenuPath(path);
    if (!normalized) {
        return "/home";
    }
    const value = normalized.startsWith("home/") ? normalized : `home/${normalized}`;
    return `/${value}`;
}

export function menuIndex(menu: MenuTree): string {
    const normalized = normalizeMenuPath(menu.path);
    if (!normalized) {
        const fallback =
            menu.id != null ? String(menu.id) : normalizeMenuPath(menu.code || menu.name || "") || "unknown";
        return `menu-${fallback}`;
    }
    return buildHomePath(normalized);
}
