import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {type DictDataVO, fetchAllDictData, fetchDictDataBatch, fetchDictDataByType} from "../api/system";

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
        if (!parsed || typeof parsed.loadedAt !== "number" || typeof parsed.data !== "object") {
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

    async function loadType(dictType: string, force = false) {
        if (!dictType) {
            return [];
        }
        if (!force && !isStale.value && dictMap.value[dictType]) {
            return dictMap.value[dictType];
        }
        if (!force) {
            hydrateFromLocal();
            if (dictMap.value[dictType]) {
                return dictMap.value[dictType];
            }
        }
        const result = await fetchDictDataByType(dictType);
        if (result?.code === 200 && result.data) {
            dictMap.value = {...dictMap.value, [dictType]: result.data};
            loadedAt.value = Date.now();
            writeLocalCache({loadedAt: loadedAt.value, data: dictMap.value});
        }
        return dictMap.value[dictType] || [];
    }

    async function loadBatch(dictTypes: string[], force = false) {
        const types = (dictTypes || []).filter(Boolean);
        if (!types.length) {
            return {};
        }
        if (!force && !isStale.value) {
            const existing = types.filter((type) => dictMap.value[type]);
            if (existing.length === types.length) {
                return types.reduce((acc, type) => {
                    acc[type] = dictMap.value[type];
                    return acc;
                }, {} as Record<string, DictDataVO[]>);
            }
        }
        const result = await fetchDictDataBatch(types);
        if (result?.code === 200 && result.data) {
            dictMap.value = {...dictMap.value, ...result.data};
            loadedAt.value = Date.now();
            writeLocalCache({loadedAt: loadedAt.value, data: dictMap.value});
        }
        return types.reduce((acc, type) => {
            acc[type] = dictMap.value[type] || [];
            return acc;
        }, {} as Record<string, DictDataVO[]>);
    }

    function getOptions(dictType: string) {
        return dictMap.value[dictType] || [];
    }

    function getLabel(dictType: string, value?: string | number | null) {
        const items = dictMap.value[dictType] || [];
        const text = value == null ? "" : String(value);
        const match = items.find((item) => String(item.dictValue) === text);
        return match?.dictLabel || text;
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
        loadType,
        loadBatch,
        getOptions,
        getLabel,
        clearCache
    };
});
