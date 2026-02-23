import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {type DictDataVO, fetchAllDictData} from "../api/system";

const CACHE_KEY = "demo-dict-cache";
const CACHE_TTL_MS = 10 * 60 * 1000;

interface DictCachePayload {
    loadedAt: number;
    data: Record<string, DictDataVO[]>;
}

function readLocalCache(): DictCachePayload | null {
    const raw = localStorage.getItem(CACHE_KEY);
    if (!raw) {
        return null;
    }
    try {
        const parsed = JSON.parse(raw) as DictCachePayload;
        if (!parsed || typeof parsed.data !== "object") {
            return null;
        }
        return parsed;
    } catch {
        return null;
    }
}

function writeLocalCache(payload: DictCachePayload) {
    localStorage.setItem(CACHE_KEY, JSON.stringify(payload));
}

export const useDictStore = defineStore("dict", () => {
    const dictMap = ref<Record<string, DictDataVO[]>>({});
    const loadedAt = ref(0);

    const isStale = computed(() => !loadedAt.value || Date.now() - loadedAt.value > CACHE_TTL_MS);

    function setCache(data: Record<string, DictDataVO[]>, timestamp = Date.now()) {
        dictMap.value = data;
        loadedAt.value = timestamp;
        writeLocalCache({loadedAt: timestamp, data});
    }

    function hydrateFromLocal() {
        const payload = readLocalCache();
        if (!payload) {
            return false;
        }
        if (Date.now() - payload.loadedAt > CACHE_TTL_MS) {
            return false;
        }
        dictMap.value = payload.data || {};
        loadedAt.value = payload.loadedAt;
        return true;
    }

    async function loadAll(force = false) {
        if (!force && !isStale.value && Object.keys(dictMap.value).length) {
            return dictMap.value;
        }
        if (!force && hydrateFromLocal()) {
            return dictMap.value;
        }
        const result = await fetchAllDictData();
        if (result?.code === 200 && result.data) {
            setCache(result.data);
        }
        return dictMap.value;
    }
    function clearCache() {
        dictMap.value = {};
        loadedAt.value = 0;
        localStorage.removeItem(CACHE_KEY);
    }

    return {
        dictMap,
        loadedAt,
        isStale,
        loadAll,
        clearCache
    };
});
