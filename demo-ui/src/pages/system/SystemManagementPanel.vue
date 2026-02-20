<template>
  <section class="system-panel">
    <div class="system-head">
      <div>
        <div class="system-title">{{ t("systemPanel.title") }}</div>
        <div class="system-sub">{{ t("systemPanel.subtitle") }}</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="system-tabs" @tab-change="handleTabChange">
      <el-tab-pane v-for="menu in menus" :key="menu.id" :label="menuLabel(menu)" :name="String(menu.id)" lazy/>
    </el-tabs>

    <div class="system-body">
      <UserManagement v-if="activeMenuCode === 'user'"/>
      <RoleManagement v-else-if="activeMenuCode === 'role'"/>
      <MenuManagement v-else-if="activeMenuCode === 'menu'"/>
      <DeptManagement v-else-if="activeMenuCode === 'dept'"/>
      <PostManagement v-else-if="activeMenuCode === 'post'"/>
      <PermissionManagement v-else-if="activeMenuCode === 'permission'"/>
      <DictManagement v-else-if="activeMenuCode === 'dict'"/>
      <NoticeManagement v-else-if="activeMenuCode === 'notice'"/>
      <JobManagement v-else-if="activeMenuCode === 'job'"/>
      <DataScopePanel
          v-else-if="activeMenuCode === 'data-scope' || activeMenuCode.startsWith('data-scope')"
          :active-code="activeMenuCode"
          :active-menu-id="activeMenuId"
          :menus="menus"
          @menu-change="emit('menu-change', $event)"
      />
      <div v-else class="system-placeholder">{{ t("systemPanel.placeholder") }}</div>
    </div>
  </section>
</template>

<script lang="ts" setup>
import {computed, defineAsyncComponent, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import type {MenuTree} from "../../api/auth";

const UserManagement = defineAsyncComponent(() => import("./UserManagement.vue"));
const RoleManagement = defineAsyncComponent(() => import("./RoleManagement.vue"));
const MenuManagement = defineAsyncComponent(() => import("./MenuManagement.vue"));
const DeptManagement = defineAsyncComponent(() => import("./DeptManagement.vue"));
const PostManagement = defineAsyncComponent(() => import("./PostManagement.vue"));
const PermissionManagement = defineAsyncComponent(() => import("./PermissionManagement.vue"));
const DictManagement = defineAsyncComponent(() => import("./DictManagement.vue"));
const NoticeManagement = defineAsyncComponent(() => import("./NoticeManagement.vue"));
const JobManagement = defineAsyncComponent(() => import("./JobManagement.vue"));
const DataScopePanel = defineAsyncComponent(() => import("./DataScopePanel.vue"));

const props = defineProps<{
  menus: MenuTree[];
  activeMenuId: number | null;
}>();

const emit = defineEmits<{ (e: "menu-change", id: number): void }>();

const activeTab = ref("");
const {t} = useI18n();

const activeMenu = computed(() => {
  if (!activeTab.value) {
    return null;
  }
  const id = Number(activeTab.value);
  if (!Number.isFinite(id)) {
    return null;
  }
  return props.menus.find((item) => item.id === id) || null;
});

const activeMenuCode = computed(() => activeMenu.value?.code || "");

function menuLabel(menu: MenuTree) {
  switch (menu.code) {
    case "user":
      return t("systemPanel.tabs.user");
    case "role":
      return t("systemPanel.tabs.role");
    case "menu":
      return t("systemPanel.tabs.menu");
    case "dept":
      return t("systemPanel.tabs.dept");
    case "post":
      return t("systemPanel.tabs.post");
    case "permission":
      return t("systemPanel.tabs.permission");
    case "dict":
      return t("systemPanel.tabs.dict");
    case "notice":
      return t("systemPanel.tabs.notice");
    case "job":
      return t("systemPanel.tabs.job");
    case "data-scope":
      return t("systemPanel.tabs.dataScope");
    default:
      return menu.name;
  }
}

function syncTab() {
  if (props.activeMenuId != null) {
    const match = props.menus.find((item) => item.id === props.activeMenuId);
    if (match) {
      activeTab.value = String(match.id);
      return;
    }
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
