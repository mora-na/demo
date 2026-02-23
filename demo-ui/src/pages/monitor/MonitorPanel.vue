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

    <div class="system-body">
      <OperLogTable v-if="activeMenuCode === 'oper-log'"/>
      <LoginLogTable v-else-if="activeMenuCode === 'login-log'"/>
      <NoticeStreamMetrics v-else-if="activeMenuCode === 'notice-stream-metrics'"/>
      <JobLogMetrics v-else-if="activeMenuCode === 'job-log-metrics'"/>
      <DruidMonitor v-else-if="isDruidActive" :section="druidSection"/>
      <div v-else class="system-placeholder">{{ t("monitorPanel.placeholder") }}</div>
    </div>
  </section>
</template>

<script lang="ts" setup>
import {computed, defineAsyncComponent, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import type {MenuTree} from "../../api/auth";

const OperLogTable = defineAsyncComponent(() => import("./OperLogTable.vue"));
const LoginLogTable = defineAsyncComponent(() => import("./LoginLogTable.vue"));
const NoticeStreamMetrics = defineAsyncComponent(() => import("./NoticeStreamMetrics.vue"));
const JobLogMetrics = defineAsyncComponent(() => import("./JobLogMetrics.vue"));
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

function menuLabel(menu: MenuTree) {
  switch (menu.code) {
    case "oper-log":
      return t("monitorPanel.tabs.operLog");
    case "login-log":
      return t("monitorPanel.tabs.loginLog");
    case "notice-stream-metrics":
      return t("monitorPanel.tabs.noticeStream");
    case "job-log-metrics":
      return t("monitorPanel.tabs.jobLog");
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

function syncTab() {
  if (activeRootMenu.value?.id != null) {
    activeTab.value = String(activeRootMenu.value.id);
    return;
  }
  if (!activeTab.value && props.menus.length) {
    activeTab.value = String(props.menus[0].id);
  }
}

function handleTabChange(name: string | number) {
  const id = Number(name);
  if (Number.isFinite(id)) {
    emit("menu-change", id);
  }
}

watch(
    () => [props.activeMenuId, props.menus],
    () => syncTab(),
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
