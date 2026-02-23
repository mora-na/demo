import {createI18n} from "vue-i18n";
import enUS from "./messages/en-US";
import zhCN from "./messages/zh-CN";

export type Locale = "zh-CN" | "en-US";

const STORAGE_KEY = "demo-locale";

function resolveInitialLocale(): Locale {
    if (typeof localStorage !== "undefined") {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored === "zh-CN" || stored === "en-US") {
            return stored;
        }
    }
    if (typeof navigator !== "undefined") {
        const language = navigator.language.toLowerCase();
        if (language.startsWith("zh")) {
            return "zh-CN";
        }
    }
    return "en-US";
}

function setHtmlLang(locale: Locale) {
    if (typeof document !== "undefined") {
        document.documentElement.lang = locale;
    }
}

const initialLocale = resolveInitialLocale();
setHtmlLang(initialLocale);

export const i18n = createI18n({
    legacy: false,
    locale: initialLocale,
    fallbackLocale: "zh-CN",
    messages: {
        "zh-CN": zhCN,
        "en-US": enUS
    }
});
