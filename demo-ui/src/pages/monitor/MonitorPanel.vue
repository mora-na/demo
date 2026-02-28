<template>
  <section class="system-panel">
    <div class="system-head">
      <div>
        <div class="system-title">{{ t("monitorPanel.title") }}</div>
        <div class="system-sub">{{ t("monitorPanel.subtitle") }}</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="system-tabs" @tab-change="handleTabChange">
      <el-tab-pane v-for="menu in menus" :key="menu.id" :label="menuLabel(menu)" :name="String(menu.id)" lazy/>
    </el-tabs>

    <el-tabs
        v-if="isDruidActive && druidMenus.length"
        v-model="druidTab"
        class="system-tabs druid-tabs"
        @tab-change="handleDruidTabChange"
    >
      <el-tab-pane v-for="menu in druidMenus" :key="menu.id" :label="druidMenuLabel(menu)" :name="String(menu.id)"
                   lazy/>
    </el-tabs>

    <div class="system-body">
      <NoticeStreamMetrics v-if="activeMenuCode === 'notice-stream-metrics'"/>
      <DruidMonitor v-else-if="isDruidActive" :section="druidSection"/>
      <div v-else class="system-placeholder">{{ t("monitorPanel.placeholder") }}</div>
    </div>
  </section>
</template>

<script lang="ts" setup>
import {computed, defineAsyncComponent, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import type {MenuTree} from "../../api/auth";

const NoticeStreamMetrics = defineAsyncComponent(() => import("./NoticeStreamMetrics.vue"));
const DruidMonitor = defineAsyncComponent(() => import("./DruidMonitor.vue"));

const props = defineProps<{
  menus: MenuTree[];
  activeMenuId: number | null;
}>();

const emit = defineEmits<{ (e: "menu-change", id: number): void }>();

const activeTab = ref("");
const {t} = useI18n();

function findMenuPath(items: MenuTree[], id: number): MenuTree[] | null {
  for (const item of items) {
    if (item.id === id) {
      return [item];
    }
    if (item.children?.length) {
      const path = findMenuPath(item.children, id);
      if (path) {
        return [item, ...path];
      }
    }
  }
  return null;
}

const activeMenuPath = computed(() => {
  if (props.activeMenuId == null) {
    return null;
  }
  return findMenuPath(props.menus, props.activeMenuId);
});

const activeRootMenu = computed(() => {
  const path = activeMenuPath.value;
  return path && path.length ? path[0] : null;
});

const activeLeafMenu = computed(() => {
  const path = activeMenuPath.value;
  return path && path.length ? path[path.length - 1] : null;
});

const activeMenuCode = computed(() => activeRootMenu.value?.code || "");

const isDruidActive = computed(() => activeMenuCode.value === "druid-monitor");

const druidSection = computed(() => resolveDruidSection(activeLeafMenu.value));

const druidMenus = computed(() => (isDruidActive.value ? activeRootMenu.value?.children || [] : []));

const druidMenuBySection = computed(() => {
  const map: Record<string, MenuTree | null> = {};
  for (const menu of druidMenus.value) {
    const section = resolveDruidSection(menu);
    if (!map[section]) {
      map[section] = menu;
    }
  }
  return map;
});

const druidTab = ref("");

function menuLabel(menu: MenuTree) {
  switch (menu.code) {
    case "notice-stream-metrics":
      return t("monitorPanel.tabs.noticeStream");
    case "druid-monitor":
      return t("monitorPanel.tabs.druid");
    default:
      return menu.name;
  }
}

function resolveDruidSection(menu?: MenuTree | null) {
  if (!menu) {
    return "home";
  }
  const path = (menu.path || "").toLowerCase();
  if (path.startsWith("/monitor/druid/")) {
    const seg = path.split("/").filter(Boolean).pop() || "";
    if (seg) {
      return seg;
    }
  }
  const code = (menu.code || "").toLowerCase();
  if (code.startsWith("druid-monitor-")) {
    return code.replace("druid-monitor-", "") || "home";
  }
  return "home";
}

function druidMenuLabel(menu: MenuTree) {
  const section = resolveDruidSection(menu);
  switch (section) {
    case "datasource":
      return t("druid.menu.datasource");
    case "sql":
      return t("druid.menu.sql");
    case "wall":
      return t("druid.menu.wall");
    case "webapp":
      return t("druid.menu.webapp");
    case "weburi":
      return t("druid.menu.weburi");
    case "session":
      return t("druid.menu.session");
    case "spring":
      return t("druid.menu.spring");
    case "json":
      return t("druid.menu.json");
    case "home":
    default:
      return t("druid.menu.home");
  }
}

function syncTab() {
  if (activeRootMenu.value?.id != null) {
    activeTab.value = String(activeRootMenu.value.id);
    return;
  }
  if (!activeTab.value && props.menus.length) {
    activeTab.value = String(props.menus[0].id);
  }
}

function syncDruidTab() {
  if (!isDruidActive.value || !druidMenus.value.length) {
    return;
  }
  const current = druidMenuBySection.value[druidSection.value];
  if (current?.id != null) {
    druidTab.value = String(current.id);
    return;
  }
  if (!druidTab.value) {
    druidTab.value = String(druidMenus.value[0].id);
  }
}

function handleTabChange(name: string | number) {
  const id = Number(name);
  if (Number.isFinite(id)) {
    emit("menu-change", id);
  }
}

function handleDruidTabChange(name: string | number) {
  const id = Number(name);
  if (Number.isFinite(id)) {
    emit("menu-change", id);
  }
}

watch(
    () => [props.activeMenuId, props.menus],
    () => {
      syncTab();
      syncDruidTab();
    },
    {immediate: true}
);
</script>

<style scoped>
.system-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.system-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.system-title {
  font-size: 16px;
  font-weight: 600;
}

.system-sub {
  font-size: 12px;
  color: var(--muted);
}

.system-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.system-placeholder {
  padding: 24px;
  border-radius: 16px;
  border: 1px dashed rgba(18, 18, 18, 0.18);
  color: var(--muted);
  text-align: center;
}
</style>
