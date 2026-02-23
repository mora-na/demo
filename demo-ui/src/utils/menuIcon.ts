import type {Component} from "vue";
import {
    Activity,
    BarChart3,
    Bell,
    BookOpen,
    Briefcase,
    Building2,
    Calendar,
    Circle,
    ClipboardList,
    Code,
    Cog,
    Database,
    DatabaseZap,
    FileSearch,
    Folder,
    Globe,
    Home,
    KeyRound,
    LayoutDashboard,
    LayoutList,
    Monitor,
    Package,
    Receipt,
    ScrollText,
    Settings2,
    Shield,
    ShieldCheck,
    ShieldOff,
    ShieldPlus,
    ShoppingCart,
    Tag,
    Timer,
    UserCog,
    Users,
    Wrench
} from "lucide-vue-next";
import type {MenuTree} from "../api/auth";

const MENU_ICON_MAP: Record<string, Component> = {
    home: Home,
    dashboard: LayoutDashboard,
    system: Cog,
    "data-scope": Shield,
    "data-scope-mapping": ShieldPlus,
    "data-scope-user": ShieldOff,
    "data-scope-user-override": ShieldOff,
    "data-scope-resolve": ShieldCheck,
    user: Users,
    role: UserCog,
    menu: LayoutList,
    dept: Building2,
    post: Briefcase,
    permission: KeyRound,
    notice: Bell,
    order: ShoppingCart,
    job: Timer,
    log: ScrollText,
    report: BarChart3,
    monitor: Monitor,
    "oper-log": FileSearch,
    "login-log": KeyRound,
    "notice-stream-metrics": Bell,
    "job-log-metrics": Timer,
    "druid-monitor": Database,
    "druid-monitor-home": Database,
    "druid-monitor-datasource": DatabaseZap,
    "druid-monitor-datasources": DatabaseZap,
    "druid-monitor-sql": Code,
    "druid-monitor-web": Globe,
    "druid-monitor-spring": Settings2,
    "druid-monitor-session": Users,
    "druid-monitor-wall": ShieldCheck,
    extension: Package,
    config: Settings2,
    file: Folder,
    dict: BookOpen,
    tool: Wrench,
    "dynamic-api": Code,
    "dynamic-api-log": Receipt
};

const FALLBACK_ICONS: Component[] = [
    Activity,
    BarChart3,
    Bell,
    BookOpen,
    Briefcase,
    Building2,
    Calendar,
    ClipboardList,
    Code,
    Cog,
    Database,
    Folder,
    Globe,
    KeyRound,
    LayoutDashboard,
    LayoutList,
    Monitor,
    Package,
    ScrollText,
    Settings2,
    Shield,
    ShieldCheck,
    ShoppingCart,
    Tag,
    Timer,
    UserCog,
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
