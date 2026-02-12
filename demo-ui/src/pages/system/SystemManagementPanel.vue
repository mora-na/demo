<template>
  <section class="system-panel">
    <div class="system-head">
      <div>
        <div class="system-title">系统管理</div>
        <div class="system-sub">维护用户、权限与组织结构配置。</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="system-tabs" @tab-change="handleTabChange">
      <el-tab-pane v-for="menu in menus" :key="menu.id" :label="menu.name" :name="String(menu.id)"/>
    </el-tabs>

    <div class="system-body">
      <UserManagement v-if="activeMenuCode === 'user'"/>
      <RoleManagement v-else-if="activeMenuCode === 'role'"/>
      <MenuManagement v-else-if="activeMenuCode === 'menu'"/>
      <DeptManagement v-else-if="activeMenuCode === 'dept'"/>
      <PermissionManagement v-else-if="activeMenuCode === 'permission'"/>
      <NoticeManagement v-else-if="activeMenuCode === 'notice'"/>
      <div v-else class="system-placeholder">请选择系统管理子菜单。</div>
    </div>
  </section>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from "vue";
import type {MenuTree} from "../../api/auth";
import UserManagement from "./UserManagement.vue";
import RoleManagement from "./RoleManagement.vue";
import MenuManagement from "./MenuManagement.vue";
import DeptManagement from "./DeptManagement.vue";
import PermissionManagement from "./PermissionManagement.vue";
import NoticeManagement from "./NoticeManagement.vue";

const props = defineProps<{
  menus: MenuTree[];
  activeMenuId: number | null;
}>();

const emit = defineEmits<{ (e: "menu-change", id: number): void }>();

const activeTab = ref("");

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
