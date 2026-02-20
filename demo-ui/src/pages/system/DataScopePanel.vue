<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("dataScope.title") }}</div>
        <div class="module-sub">{{ t("dataScope.subtitle") }}</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="data-scope-tabs" @tab-change="handleTabChange">
      <el-tab-pane :label="t('dataScope.tabs.overview')" name="overview" lazy>
        <DataScopeOverview/>
      </el-tab-pane>
      <el-tab-pane :label="t('dataScope.tabs.mapping')" name="mapping" lazy>
        <DataScopeMapping/>
      </el-tab-pane>
      <el-tab-pane :label="t('dataScope.tabs.user')" name="user" lazy>
        <DataScopeUserOverrides/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts" setup>
import {computed, defineAsyncComponent, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import type {MenuTree} from "../../api/auth";

const DataScopeOverview = defineAsyncComponent(() => import("./DataScopeOverview.vue"));
const DataScopeMapping = defineAsyncComponent(() => import("./DataScopeMapping.vue"));
const DataScopeUserOverrides = defineAsyncComponent(() => import("./DataScopeUserOverrides.vue"));

const props = defineProps<{
  activeCode?: string;
  activeMenuId?: number | null;
  menus?: MenuTree[];
}>();

const emit = defineEmits<{ (e: "menu-change", id: number): void }>();

const {t} = useI18n();
const activeTab = ref("overview");

type DataScopeTab = "overview" | "mapping" | "user";

const dataScopeMenus = computed(() => {
  const items = props.menus || [];
  return items.filter((menu) => {
    const code = (menu.code || "").toLowerCase();
    const path = (menu.path || "").toLowerCase();
    return code.startsWith("data-scope") || path.startsWith("/data-scope");
  });
});

function resolveTabFromMenu(menu?: MenuTree | null): DataScopeTab | null {
  if (!menu) {
    return null;
  }
  const code = (menu.code || "").toLowerCase();
  const path = (menu.path || "").toLowerCase();
  if (code.includes("mapping") || path.endsWith("/mapping")) {
    return "mapping";
  }
  if (code.includes("user") || path.endsWith("/user")) {
    return "user";
  }
  if (code.includes("overview") || path.endsWith("/overview")) {
    return "overview";
  }
  return null;
}

const dataScopeMenuByTab = computed(() => {
  const map: Record<DataScopeTab, MenuTree | null> = {
    overview: null,
    mapping: null,
    user: null
  };
  for (const menu of dataScopeMenus.value) {
    const tab = resolveTabFromMenu(menu);
    if (tab && !map[tab]) {
      map[tab] = menu;
    }
  }
  return map;
});

const mappedTab = computed(() => {
  if (props.activeMenuId != null) {
    const match = dataScopeMenus.value.find((item) => item.id === props.activeMenuId);
    const resolved = resolveTabFromMenu(match);
    if (resolved) {
      return resolved;
    }
  }
  if (props.activeCode) {
    const code = props.activeCode.toLowerCase();
    if (code.includes("mapping")) {
      return "mapping";
    }
    if (code.includes("user")) {
      return "user";
    }
  }
  return "overview";
});

function handleTabChange(name: string | number) {
  const tab = String(name) as DataScopeTab;
  const target = dataScopeMenuByTab.value[tab];
  if (target && target.id != null && target.id !== props.activeMenuId) {
    emit("menu-change", target.id);
  }
}

watch(
    () => mappedTab.value,
    (value) => {
      activeTab.value = value;
    },
    {immediate: true}
);
</script>

<style scoped>
.system-module {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.module-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.module-title {
  font-size: 16px;
  font-weight: 600;
}

.module-sub {
  font-size: 12px;
  color: var(--muted);
}

</style>
