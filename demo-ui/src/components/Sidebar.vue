<template>
  <aside :class="['layout-sidebar', {collapsed}]">
    <div class="sidebar-header">
      <div class="sidebar-brand">
        <div class="sidebar-logo">{{ t("home.nav.badge") }}</div>
        <div class="sidebar-meta">
          <div class="sidebar-title">{{ t("home.nav.title") }}</div>
          <div class="sidebar-sub">{{ t("home.nav.sub") }}</div>
        </div>
      </div>
      <button
          :aria-label="collapsed ? t('home.nav.expand') : t('home.nav.collapse')"
          class="sidebar-toggle"
          type="button"
          @click="emit('toggle')"
      >
        <Menu class="sidebar-toggle-icon"/>
      </button>
    </div>

    <el-scrollbar class="sidebar-scroll">
      <el-menu
          ref="menuRef"
          :default-active="activePath"
          class="sidebar-menu"
          router
          @close="handleClose"
          @open="handleOpen"
      >
        <NavTree :collapsed="collapsed" :menus="menus"/>
      </el-menu>
    </el-scrollbar>
  </aside>
</template>

<script lang="ts" setup>
import {computed, nextTick, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import {useRoute} from "vue-router";
import {Menu} from "lucide-vue-next";
import type {MenuTree} from "../api/auth";
import NavTree from "./NavTree.vue";
import {menuIndex} from "../utils/menuIndex";

const props = defineProps<{ menus: MenuTree[]; collapsed?: boolean }>();
const emit = defineEmits<{ (e: "toggle"): void }>();

const route = useRoute();
const {t} = useI18n();
const activePath = computed(() => {
  const raw = route.path || "";
  return raw.split("?")[0].split("#")[0].toLowerCase();
});
const collapsed = computed(() => props.collapsed ?? false);

type MenuActions = {
  open: (index: string) => void;
  close: (index: string) => void;
};

const menuRef = ref<MenuActions | null>(null);
const openByParent = new Map<string, string>();

function parentKeyFromPath(indexPath: string[]): string {
  return indexPath.slice(0, -1).join("|");
}

function handleOpen(index: string, indexPath: string[]) {
  const parentKey = parentKeyFromPath(indexPath);
  const current = openByParent.get(parentKey);
  if (current && current !== index) {
    menuRef.value?.close(current);
  }
  openByParent.set(parentKey, index);
}

function handleClose(index: string, indexPath: string[]) {
  const parentKey = parentKeyFromPath(indexPath);
  if (openByParent.get(parentKey) === index) {
    openByParent.delete(parentKey);
  }
  const prefix = indexPath.join("|");
  for (const key of Array.from(openByParent.keys())) {
    if (key === prefix || key.startsWith(`${prefix}|`)) {
      openByParent.delete(key);
    }
  }
}

type MenuPathEntry = { menu: MenuTree; index: string };

function findIndexPath(
    menus: MenuTree[],
    target: string,
    trail: MenuPathEntry[] = []
): MenuPathEntry[] | null {
  for (const menu of menus) {
    const index = menuIndex(menu);
    const nextTrail = [...trail, {menu, index}];
    if (index === target) {
      return nextTrail;
    }
    if (menu.children?.length) {
      const found = findIndexPath(menu.children, target, nextTrail);
      if (found) {
        return found;
      }
    }
  }
  return null;
}

function syncOpenState() {
  const menu = menuRef.value;
  if (!menu || !props.menus?.length) {
    return;
  }
  const pathEntries = findIndexPath(props.menus, activePath.value);
  if (!pathEntries) {
    return;
  }
  const desired = pathEntries
      .filter((entry) => entry.menu.children?.length)
      .map((entry) => entry.index);
  const desiredSet = new Set(desired);
  const currentOpen = new Set(openByParent.values());

  for (const index of currentOpen) {
    if (!desiredSet.has(index)) {
      menu.close(index);
    }
  }

  for (const index of desired) {
    menu.open(index);
  }
}

watch(
    [() => props.menus, activePath],
    ([menus], [prevMenus]) => {
      if (menus !== prevMenus) {
        openByParent.clear();
      }
      void nextTick(() => {
        syncOpenState();
      });
    },
    {immediate: true}
);
</script>
