import type {Component} from "vue";
import {
    Bell,
    Circle,
    Cog,
    Database,
    Home,
    KeyRound,
    LayoutDashboard,
    Monitor,
    Package,
    ScrollText,
    Shield,
    ShoppingCart,
    Users,
    Wrench
} from "lucide-vue-next";
import type {MenuTree} from "../api/auth";

const MENU_ICON_MAP: Record<string, Component> = {
    home: Home,
    dashboard: LayoutDashboard,
    system: Cog,
    "data-scope": Shield,
    "data-scope-mapping": Shield,
    "data-scope-user": Shield,
    "data-scope-user-override": Shield,
    "data-scope-resolve": Shield,
    user: Users,
    role: Users,
    menu: LayoutDashboard,
    dept: Users,
    post: Users,
    permission: KeyRound,
    notice: Bell,
    order: ShoppingCart,
    job: Wrench,
    report: Monitor,
    monitor: Monitor,
    "notice-stream-metrics": Bell,
    "druid-monitor": Database,
    "druid-monitor-home": Database,
    "druid-monitor-datasource": Database,
    "druid-monitor-datasources": Database,
    "druid-monitor-sql": Database,
    "druid-monitor-web": Database,
    "druid-monitor-spring": Database,
    "druid-monitor-session": Database,
    "druid-monitor-wall": Database,
    extension: Package,
    config: Cog,
    file: Package,
    dict: ScrollText,
    tool: Wrench,
    "dynamic-api": Package
};

const FALLBACK_ICONS: Component[] = [
    Bell,
    Cog,
    LayoutDashboard,
    Monitor,
    Package,
    ScrollText,
    Shield,
    Users,
    Wrench
];

function hashSeed(value: string): number {
    let hash = 0;
    for (let i = 0; i < value.length; i += 1) {
        hash = (hash * 31 + value.charCodeAt(i)) | 0;
    }
    return Math.abs(hash);
}

export function menuIconComponent(menu: MenuTree): Component {
    const code = (menu.code || "").toLowerCase();
    const path = (menu.path || "").toLowerCase();
    if (MENU_ICON_MAP[code]) {
        return MENU_ICON_MAP[code];
    }
    for (const [key, icon] of Object.entries(MENU_ICON_MAP)) {
        if (code.includes(key) || path.includes(key)) {
            return icon;
        }
    }
    const seed = code || path || `${menu.id ?? ""}`;
    if (!seed || !FALLBACK_ICONS.length) {
        return Circle;
    }
    const index = hashSeed(seed) % FALLBACK_ICONS.length;
    return FALLBACK_ICONS[index] || Circle;
}
