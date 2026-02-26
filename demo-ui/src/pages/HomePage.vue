<template>
  <main :class="{ 'drawer-open': navDrawerVisible }" class="console">

    <Sidebar
        :collapsed="!navDrawerVisible"
        :menus="filteredMenuTree"
        @toggle="toggleNav"
    />

    <div class="console-content">
      <header class="console-topbar">
        <div class="topbar-left">
          <div class="topbar-left-main">
            <div class="topbar-title">{{ t("home.topbar.title") }}</div>
            <div class="topbar-sub">{{ activeGroup?.name || t("home.topbar.chooseModule") }}</div>
          </div>
          <div class="topbar-tags">
            <el-tag effect="dark" :type="noticeStreamConnected ? 'success' : 'danger'">
              {{ noticeStreamConnected ? t("home.notice.streamOnline") : t("home.notice.streamOffline") }}
            </el-tag>
          </div>
        </div>
        <div class="topbar-center">
          <el-input
              v-model.trim="menuQueryInput"
              :placeholder="t('home.topbar.searchPlaceholder')"
              class="topbar-search"
              clearable
          />
        </div>
        <div class="topbar-right">
          <el-popover v-model:visible="noticeVisible" :width="360" placement="bottom-end" trigger="click">
            <template #reference>
              <el-badge :hidden="unreadCount === 0" :value="unreadCount" class="notice-badge">
                <el-button :aria-label="t('home.topbar.notifications')" class="icon-button" text>
                  <Bell class="topbar-icon"/>
                </el-button>
              </el-badge>
            </template>
            <div class="notice-panel">
              <div class="notice-panel-head">
                <div class="notice-panel-title">
                  <span>{{ t("home.notice.title") }}</span>
                  <span :class="['notice-status', noticeStreamConnected ? 'is-online' : 'is-offline']">
                    {{ noticeStreamConnected ? t("home.notice.streamOnline") : t("home.notice.streamOffline") }}
                  </span>
                </div>
                <div class="notice-panel-actions">
                  <el-button
                      v-if="noticeStreamRetryExhausted"
                      size="small"
                      text
                      @click="manualReconnectNoticeStream"
                  >
                    {{ t("home.notice.streamReconnect") }}
                  </el-button>
                  <el-button :disabled="!noticeItems.length" size="small" text @click="handleMarkAllRead">
                    全部已读
                  </el-button>
                </div>
              </div>
              <div class="notice-panel-body">
                <div v-if="noticeLoading" class="notice-empty">加载中...</div>
                <div v-else-if="!noticeItems.length" class="notice-empty">暂无通知</div>
                <button
                    v-for="item in noticeItems"
                    :key="item.id"
                    :class="{unread: item.readStatus !== 1}"
                    class="notice-item"
                    type="button"
                    @click="openNotice(item)"
                >
                  <div class="notice-item-title">{{ item.title }}</div>
                  <div class="notice-item-meta">
                    <span>{{ item.createdName || t('common.userFallback') }}</span>
                    <span>{{ formatDateTime(item.createdAt) }}</span>
                  </div>
                </button>
              </div>
            </div>
          </el-popover>
          <el-button :aria-label="t('home.topbar.settings')" class="icon-button" text @click="openSettings">
            <SlidersHorizontal class="topbar-icon"/>
          </el-button>
          <el-dropdown trigger="click">
            <button class="topbar-user" type="button">
              <el-avatar class="user-avatar" :size="32">{{ userInitial }}</el-avatar>
              <div class="user-meta">
                <div class="user-name">{{ displayName }}</div>
                <div class="user-role">{{ roleSummary }}</div>
              </div>
              <span class="user-chevron">▾</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :disabled="loggingOut" @click="handleLogout">
                  {{ t("home.topbar.logout") }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <section class="console-main">
        <div v-if="!layoutReady" aria-hidden="true" class="main-skeleton">
          <div class="main-skeleton-title"/>
          <div class="main-skeleton-line"/>
          <div class="main-skeleton-grid">
            <div v-for="item in 6" :key="item" class="main-skeleton-card"/>
          </div>
          <div class="main-skeleton-metrics">
            <div v-for="item in 4" :key="item" class="main-skeleton-metric"/>
          </div>
        </div>
        <template v-else>
          <div v-if="passwordPolicyLock" class="main-hero">
            <div>
              <h1>{{ t("home.profile.title") }}</h1>
              <p>{{ t("home.profile.forceNote") }}</p>
            </div>
          </div>
          <OrderManagementPanel v-else-if="isOrderGroup"/>
          <ExtensionPanel
              v-else-if="isExtensionGroup"
              :active-menu-id="activeMenuId"
              :menus="activeChildren"
              @menu-change="selectMenuById"
          />
          <MonitorPanel
              v-else-if="isMonitorGroup"
              :active-menu-id="activeMenuId"
              :menus="activeChildren"
              @menu-change="selectMenuById"
          />
          <DataScopePanel
              v-else-if="isDataScopeGroup"
              :active-code="activeMenuItem?.code"
              :active-menu-id="activeMenuId"
              :menus="activeChildren"
              @menu-change="selectMenuById"
          />
          <template v-else-if="!isSystemGroup">
            <div class="main-hero">
              <div>
                <h1>{{ activeGroup?.name || t("home.main.titleFallback") }}</h1>
                <p>{{ activeGroup ? menuDescription(activeGroup) : t("home.main.descFallback") }}</p>
              </div>
            </div>

            <div class="main-grid">
              <el-empty v-if="!activeChildren.length" :description="t('home.main.empty')"/>
              <template v-else>
                <article
                    v-for="item in activeChildren"
                    :key="item.id"
                    class="main-card"
                    @click="selectMenu(item)"
                >
                  <div class="main-card-head">
                    <div class="main-card-title">{{ item.name }}</div>
                    <span class="main-card-icon">↗</span>
                  </div>
                  <div class="main-card-desc">{{ menuDescription(item) }}</div>
                  <div v-if="menuChildren(item).length" class="main-card-sub">
                    <div class="main-card-subtitle">{{ t("home.main.metrics.submenu") }}</div>
                    <div class="main-card-sublist">
                      <button
                          v-for="child in cardSubmenus(item)"
                          :key="child.id"
                          class="main-submenu-chip"
                          type="button"
                          @click.stop="selectMenu(child)"
                      >
                        <span class="chip-label">{{ child.name }}</span>
                        <span v-if="child.children?.length" class="chip-arrow">▸</span>
                      </button>
                      <button
                          v-if="submenuOverflowCount(item) > 0"
                          class="main-submenu-more"
                          type="button"
                          @click.stop="toggleSubmenuExpand(item.id)"
                      >
                        {{ isSubmenuExpanded(item.id) ? "收起" : `更多 ${submenuOverflowCount(item)}+` }}
                      </button>
                    </div>
                  </div>
                </article>
              </template>
            </div>

            <div class="main-metrics">
              <div class="metric">
                <div class="metric-value">{{ menuGroupCount }}</div>
                <div class="metric-title">{{ t("home.main.metrics.group") }}</div>
              </div>
              <div class="metric">
                <div class="metric-value">{{ submenuCount }}</div>
                <div class="metric-title">{{ t("home.main.metrics.submenu") }}</div>
              </div>
              <div class="metric">
                <div class="metric-value">{{ roleCount }}</div>
                <div class="metric-title">{{ t("home.main.metrics.role") }}</div>
              </div>
              <div class="metric">
                <div class="metric-value">{{ permissionCount }}</div>
                <div class="metric-title">{{ t("home.main.metrics.permission") }}</div>
              </div>
            </div>
          </template>
          <SystemManagementPanel
              v-else
              :active-menu-id="activeMenuId"
              :menus="activeChildren"
              @menu-change="selectMenuById"
          />
        </template>
      </section>
    </div>

    <el-dialog
        v-model="settingsVisible"
        :close-on-click-modal="!passwordPolicyLock"
        :close-on-press-escape="!passwordPolicyLock"
        :show-close="!passwordPolicyLock"
        :title="t('home.profile.title')"
        align-center
        width="560px"
    >
      <el-form :model="profileForm" label-position="top">
        <el-row :gutter="16" class="profile-grid">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('home.profile.userName')">
              <el-input v-model.trim="profileForm.userName" disabled/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('home.profile.nickName')">
              <el-input v-model.trim="profileForm.nickName" :placeholder="t('home.profile.nickNamePlaceholder')"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('home.profile.phone')">
              <el-input v-model.trim="profileForm.phone" :placeholder="t('home.profile.phonePlaceholder')"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('home.profile.email')">
              <el-input v-model.trim="profileForm.email" :placeholder="t('home.profile.emailPlaceholder')"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('home.profile.sex')">
              <el-select v-model="profileForm.sex" :placeholder="t('home.profile.sexPlaceholder')">
                <el-option :label="t('home.profile.sexMale')" value="M"/>
                <el-option :label="t('home.profile.sexFemale')" value="F"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider/>
        <el-row :gutter="16" class="profile-grid">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('home.profile.oldPassword')">
              <el-input v-model.trim="profileForm.oldPassword" show-password type="password"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('home.profile.newPassword')">
              <el-input v-model.trim="profileForm.newPassword" show-password type="password"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('home.profile.confirmPassword')">
              <el-input v-model.trim="profileForm.confirmPassword" show-password type="password"/>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="profile-note">{{ passwordPolicyLock ? t("home.profile.forceNote") : t("home.profile.note") }}</div>
      </el-form>
      <template #footer>
        <el-button v-if="!passwordPolicyLock" @click="settingsVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="savingProfile" type="primary" @click="handleProfileSave">
          {{ t("common.save") }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="noticeDetailVisible" align-center title="系统通知" width="480px">
      <div class="notice-detail">
        <div class="notice-detail-title">{{ noticeDetail?.title || "-" }}</div>
        <div class="notice-detail-time">{{ formatDateTime(noticeDetail?.createdAt) }}</div>
        <div class="notice-detail-content">{{ noticeDetail?.content || "" }}</div>
      </div>
      <template #footer>
        <el-button @click="noticeDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    <div aria-hidden="true" class="route-anchor">
      <router-view/>
    </div>
  </main>
</template>

<script lang="ts" setup>
import {computed, defineAsyncComponent, onMounted, onUnmounted, reactive, ref, watch} from "vue";
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {useRoute, useRouter} from "vue-router";
import {Bell, SlidersHorizontal} from "lucide-vue-next";
import {logout, type MenuTree, updateProfile} from "../api/auth";
import {getUnreadNoticeCount, listMyNotices, markAllNoticesRead, markNoticeRead, type NoticeMyVO} from "../api/system";
import {useAuthStore} from "../stores/auth";
import {useDictStore} from "../stores/dict";
import Sidebar from "../components/Sidebar.vue";

const SystemManagementPanel = defineAsyncComponent(() => import("./system/SystemManagementPanel.vue"));
const DataScopePanel = defineAsyncComponent(() => import("./system/DataScopePanel.vue"));
const MonitorPanel = defineAsyncComponent(() => import("./monitor/MonitorPanel.vue"));
const OrderManagementPanel = defineAsyncComponent(() => import("./order/OrderManagementPanel.vue"));
const ExtensionPanel = defineAsyncComponent(() => import("./extension/ExtensionPanel.vue"));

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();
const dictStore = useDictStore();
const {t} = useI18n();
const passwordPolicyLock = computed(
    () => Boolean(authStore.passwordChangeRequired)
);
const displayName = computed(
    () => authStore.profile?.nickName || authStore.profile?.userName || authStore.userName || t("common.userFallback")
);
const userInitial = computed(() => (displayName.value || "U").slice(0, 1));
const loggingOut = ref(false);
const loadingProfile = ref(false);
const settingsVisible = ref(false);
const savingProfile = ref(false);
const originalNickName = ref("");
const noticeVisible = ref(false);
const noticeLoading = ref(false);
const noticeItems = ref<NoticeMyVO[]>([]);
const noticeDetailVisible = ref(false);
const noticeDetail = ref<NoticeMyVO | null>(null);
const unreadCount = ref(0);
const noticeStreamConnected = ref(false);
const noticeStreamRetryExhausted = ref(false);
const lastNoticePingAt = ref<number | null>(null);
let noticeStream: EventSource | null = null;
let noticeStreamRetryTimer: number | null = null;
let noticeStreamHealthTimer: number | null = null;
const NOTICE_STREAM_RETRY_BASE_MS = 2000;
const NOTICE_STREAM_RETRY_MAX_MS = 30000;
const NOTICE_STREAM_RETRY_MAX_ATTEMPTS = 5;
const NOTICE_STREAM_RETRY_RESET_MS = 60000;
const NOTICE_STREAM_RETRY_JITTER_RATIO = 0.3;
let noticeStreamRetryAttempts = 0;
let noticeStreamLastErrorAt: number | null = null;
let noticeStreamRetryHintMs: number | null = null;

const profileForm = reactive({
  userName: "",
  nickName: "",
  phone: "",
  email: "",
  sex: "",
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

const menuItems = computed(() => authStore.menus || []);
const menuQueryInput = ref("");
const menuQuery = ref("");
let menuQueryTimer: number | null = null;
const activeSelection = computed(() => resolveSelectionFromPath(menuItems.value, currentRoutePath()));
const activeMenuId = computed(() => activeSelection.value?.menu.id ?? null);
const navDrawerVisible = ref(readStoredNavDrawerState() ?? true);
const layoutReady = computed(() => authStore.profileLoaded);
const MENU_STORAGE_KEY = "demo.activeMenuId";
const NAV_DRAWER_STORAGE_KEY = "demo.navDrawerOpen";
const HOME_ROUTE_NAME = "home";
const HOME_PATH_PREFIX = "/home";
const SUBMENU_PREVIEW_LIMIT = 6;
const expandedSubmenuState = ref<Record<number, boolean>>({});
const menuChildrenIndex = computed(() => {
  const index = new Map<number, MenuTree[]>();
  const queue = [...menuItems.value];
  while (queue.length) {
    const current = queue.shift();
    if (!current) {
      continue;
    }
    if (current.id != null && current.children?.length) {
      index.set(current.id, current.children);
    }
    if (current.children?.length) {
      queue.push(...current.children);
    }
  }
  return index;
});

const menuPathIndex = computed(() => buildMenuPathIndex(menuItems.value));

const menuTotal = computed(() => countMenuItems(menuItems.value));
const filteredMenuTree = computed(() => {
  const query = menuQuery.value.trim().toLowerCase();
  if (!query) {
    return menuItems.value;
  }
  return filterMenuTree(menuItems.value, query);
});
const menuGroupCount = computed(() => menuItems.value.length);
const submenuCount = computed(() => Math.max(menuTotal.value - menuGroupCount.value, 0));
const roleCount = computed(() => authStore.roles.length);
const permissionCount = computed(() => authStore.permissions.length);

const activeGroup = computed(() => activeSelection.value?.group || null);

const activeMenuItem = computed(() => activeSelection.value?.menu || null);

const activeChildren = computed(() => {
  if (!activeGroup.value) {
    return [];
  }
  if (activeGroup.value.children && activeGroup.value.children.length) {
    return activeGroup.value.children;
  }
  return [activeGroup.value];
});

const isSystemGroup = computed(() => {
  const group = activeGroup.value;
  if (!group) {
    return false;
  }
  return group.code === "system" || (group.path ? group.path.startsWith("/system") : false);
});

const isDataScopeGroup = computed(() => {
  const group = activeGroup.value;
  if (!group) {
    return false;
  }
  return group.code === "data-scope" || (group.path ? group.path.startsWith("/data-scope") : false);
});

const isMonitorGroup = computed(() => {
  const group = activeGroup.value;
  if (!group) {
    return false;
  }
  return group.code === "monitor" || (group.path ? group.path.startsWith("/monitor") : false);
});

const isOrderGroup = computed(() => {
  const group = activeGroup.value;
  if (!group) {
    return false;
  }
  return group.code === "order" || (group.path ? group.path.startsWith("/orders") : false);
});

const isExtensionGroup = computed(() => {
  const group = activeGroup.value;
  if (!group) {
    return false;
  }
  return group.code === "extension" || (group.path ? group.path.startsWith("/extension") : false);
});

const roleSummary = computed(() =>
    authStore.roles.length ? authStore.roles.join(" / ") : t("common.roleEmpty")
);

let syncingMenuRoute = false;

watch(
    () => [menuItems.value, route.fullPath, route.name],
    () => {
      void syncMenuRouteState();
    },
    {immediate: true}
);

watch(
    () => navDrawerVisible.value,
    (value) => {
      storeNavDrawerState(value);
    }
);

watch(
    () => menuQueryInput.value,
    (value) => {
      if (menuQueryTimer != null) {
        window.clearTimeout(menuQueryTimer);
      }
      menuQueryTimer = window.setTimeout(() => {
        menuQuery.value = value;
      }, 150);
    },
    {immediate: true}
);

watch(
    () => passwordPolicyLock.value,
    (locked, prevLocked) => {
      if (locked) {
        return;
      }
      if (prevLocked) {
        void dictStore.loadAll();
        refreshUnreadCount();
      }
    }
);

watch(
    () => noticeVisible.value,
    (visible) => {
      if (passwordPolicyLock.value) {
        return;
      }
      if (visible) {
        loadMyNotices();
        refreshUnreadCount();
      }
    }
);

watch(
    () => [authStore.token, passwordPolicyLock.value],
    ([token, locked], previous) => {
      const prevToken = previous ? previous[0] : undefined;
      const prevLocked = previous ? previous[1] : undefined;
      if (!token || locked) {
        stopNoticeStream();
        unreadCount.value = 0;
        noticeItems.value = [];
        return;
      }
      if (token !== prevToken || locked !== prevLocked) {
        resetNoticeStreamRetryState();
        startNoticeStream();
        refreshUnreadCount();
      }
    },
    {immediate: true}
);

function normalizePath(path?: string): string {
  if (!path) {
    return "";
  }
  const trimmed = path.trim();
  if (!trimmed) {
    return "";
  }
  const normalized = trimmed.startsWith("/") ? trimmed.slice(1) : trimmed;
  return normalized
      .split("/")
      .filter(Boolean)
      .join("/")
      .toLowerCase();
}

function currentRoutePath(): string {
  const raw = route.fullPath || route.path || window.location.pathname || "";
  const pure = raw.split("?")[0].split("#")[0];
  if (pure.startsWith(HOME_PATH_PREFIX)) {
    return normalizePath(pure.slice(HOME_PATH_PREFIX.length));
  }
  const fallback = window.location.pathname || "";
  if (!fallback.startsWith(HOME_PATH_PREFIX)) {
    return "";
  }
  return normalizePath(fallback.slice(HOME_PATH_PREFIX.length));
}

function menuPath(menu: MenuTree): string {
  return normalizePath(menu.path);
}

function findGroupByPath(items: MenuTree[], path: string): MenuTree | null {
  const target = normalizePath(path);
  if (!target) {
    return null;
  }
  for (const group of items) {
    if (menuPath(group) === target) {
      return group;
    }
  }
  return null;
}

function findMenuByPath(index: Map<string, { group: MenuTree; menu: MenuTree }>, path: string) {
  const target = normalizePath(path);
  if (!target) {
    return null;
  }
  return index.get(target) || null;
}

function buildMenuPathIndex(items: MenuTree[]): Map<string, { group: MenuTree; menu: MenuTree }> {
  const index = new Map<string, { group: MenuTree; menu: MenuTree }>();
  for (const group of items) {
    const groupPath = menuPath(group);
    if (groupPath) {
      index.set(groupPath, {group, menu: group});
    }
    const children = group.children || [];
    for (const child of children) {
      const childPath = menuPath(child);
      if (childPath) {
        index.set(childPath, {group, menu: child});
      }
      if (child.children?.length) {
        addNestedMenuPaths(index, group, child);
      }
    }
  }
  return index;
}

function addNestedMenuPaths(
    index: Map<string, { group: MenuTree; menu: MenuTree }>,
    group: MenuTree,
    menu: MenuTree
) {
  const children = menu.children || [];
  for (const child of children) {
    const childPath = menuPath(child);
    if (childPath) {
      index.set(childPath, {group, menu: child});
    }
    if (child.children?.length) {
      addNestedMenuPaths(index, group, child);
    }
  }
}

function resolveStoredMenuForGroup(group: MenuTree): MenuTree | null {
  const storedId = readStoredMenuId();
  if (storedId == null) {
    return null;
  }
  if (!group.children?.length) {
    return group.id === storedId ? group : null;
  }
  return findMenuById(group.children, storedId) || null;
}

function getDefaultSelection(items: MenuTree[]): { group: MenuTree; menu: MenuTree } | null {
  if (!items.length) {
    return null;
  }
  const storedId = readStoredMenuId();
  if (storedId != null) {
    const storedMenu = findMenuById(items, storedId);
    if (storedMenu) {
      const storedGroup = findGroupById(items, storedMenu.id);
      if (storedGroup) {
        return {group: storedGroup, menu: storedMenu};
      }
    }
  }
  const firstGroup = items[0];
  if (firstGroup.children?.length) {
    return {group: firstGroup, menu: firstGroup.children[0]};
  }
  return {group: firstGroup, menu: firstGroup};
}

function resolveSelectionFromPath(
    items: MenuTree[],
    path: string
): { group: MenuTree; menu: MenuTree } | null {
  if (!items.length) {
    return null;
  }
  const normalized = normalizePath(path);
  if (!normalized) {
    return getDefaultSelection(items);
  }
  const exact = findMenuByPath(menuPathIndex.value, normalized);
  if (exact) {
    return exact;
  }
  const group = findGroupByPath(items, normalized);
  if (!group) {
    return null;
  }
  if (!group.children?.length) {
    return {group, menu: group};
  }
  const storedChild = resolveStoredMenuForGroup(group);
  if (storedChild) {
    return {group, menu: storedChild};
  }
  return {group, menu: group.children[0]};
}

function buildHomePath(path?: string): string {
  const normalized = normalizePath(path);
  if (!normalized) {
    return HOME_PATH_PREFIX;
  }
  return `${HOME_PATH_PREFIX}/${normalized}`;
}

function isCurrentHomePath(path?: string): boolean {
  if (route.name !== HOME_ROUTE_NAME) {
    return false;
  }
  return currentRoutePath() === normalizePath(path);
}

function resolvePreferredMenu(menu: MenuTree): MenuTree | null {
  if (!menu) {
    return null;
  }
  if (!menu.children?.length) {
    return menu;
  }
  const storedId = readStoredMenuId();
  if (storedId != null) {
    const storedChild = findMenuById(menu.children, storedId);
    if (storedChild) {
      return storedChild;
    }
  }
  return menu.children[0] || menu;
}

function resolveMenuPathForNavigation(menu: MenuTree): string | null {
  const targetMenu = resolvePreferredMenu(menu);
  if (!targetMenu) {
    return null;
  }
  const path = menuPath(targetMenu);
  return path || null;
}

async function syncMenuRouteState() {
  if (syncingMenuRoute) {
    return;
  }
  const items = menuItems.value;
  if (!items.length) {
    return;
  }
  const routePath = currentRoutePath();
  let selection = resolveSelectionFromPath(items, routePath);
  if (!selection) {
    const fallback = getDefaultSelection(items);
    if (!fallback) {
      return;
    }
    selection = fallback;
    const targetPath = resolveMenuPathForNavigation(selection.menu);
    storeMenuId(selection.menu.id);
    if (targetPath && !isCurrentHomePath(targetPath)) {
      syncingMenuRoute = true;
      try {
        await router.replace(buildHomePath(targetPath));
      } finally {
        syncingMenuRoute = false;
      }
    }
    return;
  }
  storeMenuId(selection.menu.id);
  const targetPath = resolveMenuPathForNavigation(selection.menu);
  if (targetPath && targetPath !== routePath) {
    syncingMenuRoute = true;
    try {
      await router.replace(buildHomePath(targetPath));
    } finally {
      syncingMenuRoute = false;
    }
  }
}

function routeToMenu(menu: MenuTree) {
  const targetMenu = resolvePreferredMenu(menu);
  if (!targetMenu) {
    return null;
  }
  const path = menuPath(targetMenu);
  return path || null;
}

async function selectMenu(menu: MenuTree) {
  if (menu?.id == null) {
    return;
  }
  const targetPath = routeToMenu(menu);
  if (!targetPath) {
    return;
  }
  if (isCurrentHomePath(targetPath)) {
    const selection = resolveSelectionFromPath(menuItems.value, targetPath);
    if (selection) {
      storeMenuId(selection.menu.id);
    }
    return;
  }
  await router.push(buildHomePath(targetPath));
}

function isSubmenuExpanded(menuId: number | null): boolean {
  if (menuId == null) {
    return false;
  }
  return expandedSubmenuState.value[menuId] === true;
}

function toggleSubmenuExpand(menuId: number) {
  expandedSubmenuState.value = {
    ...expandedSubmenuState.value,
    [menuId]: !expandedSubmenuState.value[menuId]
  };
}

function menuChildren(menu: MenuTree): MenuTree[] {
  if (menu?.id == null) {
    return menu.children ?? [];
  }
  return menuChildrenIndex.value.get(menu.id) ?? menu.children ?? [];
}

function cardSubmenus(menu: MenuTree): MenuTree[] {
  const children = menuChildren(menu);
  if (children.length <= SUBMENU_PREVIEW_LIMIT || isSubmenuExpanded(menu.id)) {
    return children;
  }
  return children.slice(0, SUBMENU_PREVIEW_LIMIT);
}

function submenuOverflowCount(menu: MenuTree): number {
  const count = menuChildren(menu).length;
  if (count <= SUBMENU_PREVIEW_LIMIT) {
    return 0;
  }
  return isSubmenuExpanded(menu.id) ? 0 : count - SUBMENU_PREVIEW_LIMIT;
}


function selectMenuById(id: number) {
  const menu = findMenuById(menuItems.value, id);
  if (!menu) {
    return;
  }
  void selectMenu(menu);
}

function toggleNav() {
  navDrawerVisible.value = !navDrawerVisible.value;
}

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

function formatDateTime(value?: string) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const pad = (num: number) => String(num).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
      date.getHours()
  )}:${pad(date.getMinutes())}`;
}

async function refreshUnreadCount() {
  if (passwordPolicyLock.value) {
    unreadCount.value = 0;
    return;
  }
  if (!authStore.token) {
    unreadCount.value = 0;
    return;
  }
  try {
    const result = await getUnreadNoticeCount();
    if (result?.code === 200) {
      unreadCount.value = result.data ?? 0;
    }
  } catch (error) {
    unreadCount.value = 0;
  }
}

let noticePingTimeoutMs = 45000;

function markNoticeStreamAlive() {
  noticeStreamConnected.value = true;
  lastNoticePingAt.value = Date.now();
  resetNoticeStreamRetryCounter();
}

function startNoticeStreamHealthCheck() {
  if (noticeStreamHealthTimer != null) {
    window.clearInterval(noticeStreamHealthTimer);
  }
  noticeStreamHealthTimer = window.setInterval(() => {
    if (lastNoticePingAt.value == null) {
      return;
    }
    if (noticePingTimeoutMs <= 0) {
      return;
    }
    const stale = Date.now() - lastNoticePingAt.value > noticePingTimeoutMs;
    if (stale) {
      noticeStreamConnected.value = false;
    }
  }, 5000);
}

function stopNoticeStreamHealthCheck() {
  if (noticeStreamHealthTimer != null) {
    window.clearInterval(noticeStreamHealthTimer);
    noticeStreamHealthTimer = null;
  }
}

function mapLatestToNotice(item: Record<string, any>): NoticeMyVO {
  return {
    id: Number(item.id || 0),
    title: item.title || "",
    content: "",
    createdName: item.createdName,
    createdAt: item.createdAt,
    readStatus: item.readStatus,
    readTime: item.readTime
  };
}

function applyNoticePayload(payload: any) {
  let updated = false;
  let hasUnreadCount = payload && typeof payload.unreadCount === "number";
  let skipRefresh = false;
  if (payload && typeof payload.unreadCount === "number") {
    unreadCount.value = payload.unreadCount;
    updated = true;
  }
  if (payload && typeof payload.heartbeatTimeoutMillis === "number") {
    noticePingTimeoutMs = payload.heartbeatTimeoutMillis;
    if (noticePingTimeoutMs <= 0) {
      noticeStreamConnected.value = true;
    }
  }
  if (payload && typeof payload.retryAfterMillis === "number" && payload.retryAfterMillis > 0) {
    noticeStreamRetryHintMs = payload.retryAfterMillis;
  }
  if (payload && payload.streamStatus === "rejected") {
    noticeStreamRetryExhausted.value = false;
    skipRefresh = true;
    hasUnreadCount = true;
    if (noticeStreamRetryTimer != null) {
      window.clearTimeout(noticeStreamRetryTimer);
      noticeStreamRetryTimer = null;
    }
    closeNoticeStream();
    scheduleNoticeStreamReconnect();
  }
  if (payload && Array.isArray(payload.latestNotices) && !noticeVisible.value) {
    noticeItems.value = payload.latestNotices.map(mapLatestToNotice);
    updated = true;
  }
  return {updated, hasUnreadCount: hasUnreadCount || skipRefresh};
}

function resetNoticeStreamRetryCounter() {
  noticeStreamRetryAttempts = 0;
  noticeStreamLastErrorAt = null;
  noticeStreamRetryExhausted.value = false;
}

function resetNoticeStreamRetryState() {
  resetNoticeStreamRetryCounter();
  noticeStreamRetryHintMs = null;
}

function resolveNoticeStreamRetryBaseMs() {
  if (noticeStreamRetryHintMs != null && noticeStreamRetryHintMs > 0) {
    return Math.max(1000, noticeStreamRetryHintMs);
  }
  return NOTICE_STREAM_RETRY_BASE_MS;
}

function computeNoticeStreamRetryDelay(attempt: number) {
  const base = resolveNoticeStreamRetryBaseMs();
  const exponent = Math.max(0, attempt - 1);
  const delay = Math.min(NOTICE_STREAM_RETRY_MAX_MS, base * Math.pow(2, exponent));
  const jitter = Math.round(delay * NOTICE_STREAM_RETRY_JITTER_RATIO * Math.random());
  return delay + jitter;
}

function startNoticeStream() {
  if (passwordPolicyLock.value) {
    return;
  }
  closeNoticeStream();
  const token = authStore.token;
  if (!token) {
    return;
  }
  const baseUrl = (import.meta.env.VITE_API_BASE_URL || "/prod-api").replace(/\/$/, "");
  const url = `${baseUrl}/notices/stream?token=${encodeURIComponent(token)}`;
  const withCredentials = String(import.meta.env.VITE_API_WITH_CREDENTIALS || "").toLowerCase() === "true";
  const source = new EventSource(url, {withCredentials});
  noticeStream = source;
  noticeStreamConnected.value = false;
  lastNoticePingAt.value = null;
  startNoticeStreamHealthCheck();
  source.onopen = () => {
    markNoticeStreamAlive();
  };
  const handlePayloadEvent = (event: MessageEvent) => {
    markNoticeStreamAlive();
    let hasUnreadCount = false;
    if (event?.data) {
      try {
        const payload = JSON.parse(event.data);
        const result = applyNoticePayload(payload);
        hasUnreadCount = result.hasUnreadCount;
      } catch {
        // ignore parse errors
      }
    }
    if (!hasUnreadCount) {
      refreshUnreadCount();
    }
    if (noticeVisible.value) {
      loadMyNotices();
    }
  };
  source.addEventListener("init", handlePayloadEvent);
  source.addEventListener("notice", handlePayloadEvent);
  source.addEventListener("ping", () => {
    markNoticeStreamAlive();
  });
  source.onerror = () => {
    noticeStreamConnected.value = false;
    closeNoticeStream();
    scheduleNoticeStreamReconnect();
  };
}

function scheduleNoticeStreamReconnect() {
  if (!authStore.token || noticeStreamRetryTimer != null || noticeStreamRetryExhausted.value) {
    return;
  }
  const now = Date.now();
  if (noticeStreamLastErrorAt != null && now - noticeStreamLastErrorAt > NOTICE_STREAM_RETRY_RESET_MS) {
    noticeStreamRetryAttempts = 0;
  }
  noticeStreamLastErrorAt = now;
  noticeStreamRetryAttempts += 1;
  if (noticeStreamRetryAttempts > NOTICE_STREAM_RETRY_MAX_ATTEMPTS) {
    noticeStreamRetryExhausted.value = true;
    return;
  }
  const delay = computeNoticeStreamRetryDelay(noticeStreamRetryAttempts);
  noticeStreamRetryTimer = window.setTimeout(() => {
    noticeStreamRetryTimer = null;
    startNoticeStream();
  }, delay);
}

function closeNoticeStream() {
  if (noticeStream) {
    noticeStream.close();
    noticeStream = null;
  }
  stopNoticeStreamHealthCheck();
  noticeStreamConnected.value = false;
  lastNoticePingAt.value = null;
}

function stopNoticeStream() {
  closeNoticeStream();
  if (noticeStreamRetryTimer != null) {
    window.clearTimeout(noticeStreamRetryTimer);
    noticeStreamRetryTimer = null;
  }
  resetNoticeStreamRetryState();
}

function manualReconnectNoticeStream() {
  if (noticeStreamRetryTimer != null) {
    window.clearTimeout(noticeStreamRetryTimer);
    noticeStreamRetryTimer = null;
  }
  resetNoticeStreamRetryState();
  startNoticeStream();
}

async function loadMyNotices() {
  if (passwordPolicyLock.value) {
    noticeItems.value = [];
    return;
  }
  if (noticeLoading.value) {
    return;
  }
  if (!authStore.token) {
    noticeItems.value = [];
    return;
  }
  noticeLoading.value = true;
  try {
    const result = await listMyNotices({pageNum: 1, pageSize: 10});
    if (result?.code === 200 && result.data) {
      noticeItems.value = result.data.data || [];
    } else {
      noticeItems.value = [];
    }
  } finally {
    noticeLoading.value = false;
  }
}

async function openNotice(item: NoticeMyVO) {
  noticeVisible.value = false;
  noticeDetail.value = item;
  noticeDetailVisible.value = true;
  if (item.readStatus !== 1) {
    try {
      const result = await markNoticeRead(item.id);
      if (result?.code === 200) {
        item.readStatus = 1;
        item.readTime = new Date().toISOString();
      }
    } catch (error) {
      ElMessage.error(getErrorMessage(error, t("common.error")));
    } finally {
      await refreshUnreadCount();
    }
  }
}

async function handleMarkAllRead() {
  if (!noticeItems.value.length) {
    return;
  }
  try {
    const result = await markAllNoticesRead();
    if (result?.code === 200) {
      const now = new Date().toISOString();
      noticeItems.value = noticeItems.value.map((item) => ({
        ...item,
        readStatus: 1,
        readTime: item.readTime || now
      }));
      unreadCount.value = 0;
    } else {
      ElMessage.error(result?.message || t("common.error"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("common.error")));
  }
}

async function openSettings() {
  try {
    await authStore.loadProfile(true);
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("home.profile.msg.saveFailed")));
    return;
  }
  const profile = authStore.profile;
  profileForm.userName = profile?.userName || authStore.userName || "";
  profileForm.nickName = profile?.nickName || "";
  profileForm.phone = profile?.phone || "";
  profileForm.email = profile?.email || "";
  profileForm.sex = profile?.sex || "";
  profileForm.oldPassword = "";
  profileForm.newPassword = "";
  profileForm.confirmPassword = "";
  originalNickName.value = profileForm.nickName || "";
  settingsVisible.value = true;
}

async function handleProfileSave() {
  if (savingProfile.value) {
    return;
  }
  const nickName = profileForm.nickName.trim();
  const phone = profileForm.phone.trim();
  const email = profileForm.email.trim();
  const sex = profileForm.sex;
  const wantsPasswordChange =
      Boolean(profileForm.oldPassword) ||
      Boolean(profileForm.newPassword) ||
      Boolean(profileForm.confirmPassword);

  const hasProfileChange =
      nickName !== originalNickName.value ||
      phone !== (authStore.profile?.phone || "") ||
      email !== (authStore.profile?.email || "") ||
      sex !== (authStore.profile?.sex || "");

  if (!wantsPasswordChange && !hasProfileChange) {
    ElMessage.warning(t("home.profile.msg.noChanges"));
    return;
  }
  if (passwordPolicyLock.value && !wantsPasswordChange) {
    ElMessage.warning(t("home.profile.msg.forcePasswordRequired"));
    return;
  }

  if (wantsPasswordChange) {
    if (!profileForm.oldPassword || !profileForm.newPassword || !profileForm.confirmPassword) {
      ElMessage.warning(t("home.profile.msg.fillPassword"));
      return;
    }
    if (profileForm.newPassword !== profileForm.confirmPassword) {
      ElMessage.warning(t("home.profile.msg.confirmMismatch"));
      return;
    }
  }

  savingProfile.value = true;
  try {
    const result = await updateProfile({
      nickName: nickName || undefined,
      phone: phone || undefined,
      email: email || undefined,
      sex: sex || undefined,
      oldPassword: wantsPasswordChange ? profileForm.oldPassword : undefined,
      newPassword: wantsPasswordChange ? profileForm.newPassword : undefined
    });
    if (result?.code === 200) {
      ElMessage.success(result?.message || t("home.profile.msg.saveSuccess"));
      await authStore.loadProfile(true);
      if (!authStore.passwordChangeRequired) {
        settingsVisible.value = false;
      }
    } else {
      ElMessage.error(result?.message || t("home.profile.msg.saveFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("home.profile.msg.saveFailed")));
  } finally {
    savingProfile.value = false;
  }
}

function menuDescription(menu: { remark?: string }) {
  return menu?.remark || t("common.entry");
}

function matchesMenu(menu: MenuTree, query: string) {
  const value = [
    menu.name,
    menu.code,
    menu.path,
    menu.permission,
    menu.remark
  ]
      .filter(Boolean)
      .join(" ")
      .toLowerCase();
  return value.includes(query);
}

function filterMenuTree(items: MenuTree[], query: string): MenuTree[] {
  const results: MenuTree[] = [];
  for (const item of items) {
    if (!item) {
      continue;
    }
    const filteredChildren = item.children ? filterMenuTree(item.children, query) : [];
    if (matchesMenu(item, query) || filteredChildren.length) {
      results.push({...item, children: filteredChildren});
    }
  }
  return results;
}

function countMenuItems(items: MenuTree[]): number {
  let total = 0;
  for (const item of items) {
    total += 1;
    if (item.children?.length) {
      total += countMenuItems(item.children);
    }
  }
  return total;
}

function findMenuById(items: MenuTree[], id: number | null): MenuTree | null {
  if (id == null) {
    return null;
  }
  for (const item of items) {
    if (item.id === id) {
      return item;
    }
    if (item.children?.length) {
      const found = findMenuById(item.children, id);
      if (found) {
        return found;
      }
    }
  }
  return null;
}

function readStoredMenuId(): number | null {
  try {
    const raw = localStorage.getItem(MENU_STORAGE_KEY);
    if (!raw) {
      return null;
    }
    const value = Number(raw);
    return Number.isFinite(value) ? value : null;
  } catch (error) {
    return null;
  }
}

function readStoredNavDrawerState(): boolean | null {
  try {
    const raw = localStorage.getItem(NAV_DRAWER_STORAGE_KEY);
    if (raw === null) {
      return null;
    }
    if (raw === "1") {
      return true;
    }
    if (raw === "0") {
      return false;
    }
    return null;
  } catch (error) {
    return null;
  }
}

function storeMenuId(id: number) {
  try {
    localStorage.setItem(MENU_STORAGE_KEY, String(id));
  } catch (error) {
    // ignore storage errors
  }
}

function storeNavDrawerState(open: boolean) {
  try {
    localStorage.setItem(NAV_DRAWER_STORAGE_KEY, open ? "1" : "0");
  } catch (error) {
    // ignore storage errors
  }
}

function findGroupById(items: MenuTree[], id: number): MenuTree | null {
  for (const item of items) {
    if (item.id === id) {
      return item;
    }
    if (item.children?.length) {
      const found = findMenuById(item.children, id);
      if (found) {
        return item;
      }
    }
  }
  return null;
}

async function loadProfile() {
  if (loadingProfile.value) {
    return;
  }
  if (authStore.profileLoaded) {
    return;
  }
  loadingProfile.value = true;
  try {
    const result = await authStore.loadProfile();
    if (!result.ok) {
      ElMessage.warning(result.message || t("home.msg.profileLoadFailed"));
    }
  } catch (error) {
    ElMessage.warning(getErrorMessage(error, t("home.msg.profileLoadFailed")));
  } finally {
    loadingProfile.value = false;
  }
}

async function handleLogout() {
  if (loggingOut.value) {
    return;
  }
  const token = authStore.token;
  if (!token) {
    authStore.clearSession();
    await router.replace({name: "login"});
    return;
  }
  loggingOut.value = true;
  try {
    const result = await logout(token);
    if (result?.code === 200) {
      ElMessage.success(result?.message || t("home.msg.logoutSuccess"));
      authStore.clearSession();
      await router.replace({name: "login"});
    } else {
      ElMessage.error(result?.message || t("home.msg.logoutFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("home.msg.logoutFailed")));
  } finally {
    loggingOut.value = false;
  }
}

onMounted(async () => {
  await loadProfile();
  if (passwordPolicyLock.value) {
    await openSettings();
    return;
  }
  void dictStore.loadAll();
  await refreshUnreadCount();
});

onUnmounted(() => {
  stopNoticeStream();
  if (menuQueryTimer != null) {
    window.clearTimeout(menuQueryTimer);
    menuQueryTimer = null;
  }
});
</script>

<style scoped>
.console {
  --nav-drawer-width: 240px;
  --nav-drawer-collapsed-width: 72px;
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: none;
  height: 100%;
  min-height: 0;
  display: flex;
  gap: 8px;
  overflow: hidden;
}

.route-anchor {
  display: none;
}

.console-content {
  flex: 1;
  min-width: 0;
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}


.console-topbar {
  grid-area: topbar;
  display: grid;
  grid-template-columns: minmax(200px, 1fr) minmax(200px, 300px) 260px;
  gap: 8px;
  align-items: center;
  padding: 4px 12px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(18, 18, 18, 0.08);
  box-shadow: none;
  backdrop-filter: none;
  position: static;
  min-height: 56px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.topbar-left-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  min-width: 0;
  flex: 1;
}

.topbar-title {
  font-size: 15px;
  font-weight: 600;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topbar-sub {
  font-size: 12px;
  color: var(--muted);
  max-width: 260px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topbar-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 11px;
  color: var(--muted);
  min-width: 120px;
  justify-content: flex-end;
}

.topbar-center {
  display: flex;
  align-items: center;
}

.topbar-search {
  width: 100%;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: flex-end;
  min-width: 240px;
}

.icon-button {
  font-size: 16px;
  padding: 4px 6px;
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
}

.icon-button:hover {
  color: var(--accent);
}

.notice-badge {
  width: 32px;
  display: inline-flex;
  justify-content: center;
}

.notice-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notice-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 600;
}

.notice-panel-actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.notice-panel-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.notice-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 999px;
  background: rgba(18, 18, 18, 0.08);
  color: var(--muted);
}

.notice-panel-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 280px;
  overflow: auto;
}

.notice-item {
  border: 1px solid rgba(18, 18, 18, 0.08);
  background: rgba(255, 255, 255, 0.86);
  border-radius: 10px;
  padding: 8px 10px;
  text-align: left;
  cursor: pointer;
}

.notice-item.unread {
  border-color: rgba(43, 124, 255, 0.4);
  background: rgba(43, 124, 255, 0.08);
}

.notice-item:hover {
  border-color: rgba(18, 18, 18, 0.18);
}

.notice-item-title {
  font-size: 13px;
  font-weight: 600;
}

.notice-item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--muted);
  margin-top: 4px;
}

.notice-empty {
  font-size: 12px;
  color: var(--muted);
  text-align: center;
  padding: 12px 0;
}

.notice-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notice-detail-title {
  font-size: 16px;
  font-weight: 600;
}

.notice-detail-time {
  font-size: 12px;
  color: var(--muted);
}

.notice-detail-content {
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(18, 18, 18, 0.04);
  line-height: 1.6;
}

.topbar-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(18, 18, 18, 0.08);
  cursor: pointer;
  font: inherit;
  text-align: left;
  appearance: none;
  min-width: 0;
}

.user-avatar {
  background: rgba(43, 124, 255, 0.16);
  color: #1c3f8f;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-width: 130px;
}

.user-name {
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 10px;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-chevron {
  font-size: 12px;
  color: var(--muted);
}

.profile-note {
  font-size: 12px;
  color: var(--muted);
  margin-top: -6px;
}

.console-main {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(18, 18, 18, 0.08);
  box-shadow: none;
  flex: 1;
  min-height: 0;
  overflow: auto;
  overscroll-behavior: contain;
}

.main-skeleton {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 520px;
}

.main-skeleton-title {
  height: 32px;
  width: 38%;
  border-radius: 14px;
  background: rgba(18, 18, 18, 0.06);
}

.main-skeleton-line {
  height: 16px;
  width: 52%;
  border-radius: 12px;
  background: rgba(18, 18, 18, 0.05);
}

.main-skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.main-skeleton-card {
  height: 120px;
  border-radius: 18px;
  background: rgba(18, 18, 18, 0.05);
}

.main-skeleton-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
}

.main-skeleton-metric {
  height: 54px;
  border-radius: 12px;
  background: rgba(18, 18, 18, 0.05);
}

.main-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  flex-wrap: wrap;
}

.main-hero h1 {
  margin: 10px 0 6px;
  font-family: "Fraunces", "Times New Roman", serif;
  font-size: clamp(32px, 3.4vw, 46px);
}

.main-hero p {
  margin: 0;
  color: var(--muted);
}

.main-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.main-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(18, 18, 18, 0.08);
  background: linear-gradient(140deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.78));
  cursor: pointer;
}

.main-card:hover {
  box-shadow: 0 16px 30px rgba(18, 18, 18, 0.12);
}

.main-card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.main-card-title {
  font-size: 15px;
  font-weight: 600;
}

.main-card-icon {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  background: rgba(43, 124, 255, 0.12);
  color: #1c3f8f;
  display: grid;
  place-items: center;
  font-size: 14px;
}

.main-card-desc {
  font-size: 13px;
  color: var(--muted);
  line-height: 1.5;
}

.main-card-sub {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 4px;
  padding-top: 10px;
  border-top: 1px dashed rgba(18, 18, 18, 0.1);
}

.main-card-subtitle {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--muted);
}

.main-card-sublist {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.main-submenu-chip,
.main-submenu-more {
  border: 1px solid rgba(18, 18, 18, 0.12);
  background: rgba(255, 255, 255, 0.92);
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--ink);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 180px;
}

.main-submenu-chip:hover,
.main-submenu-more:hover {
  border-color: rgba(43, 124, 255, 0.35);
  color: var(--accent);
}

.main-submenu-more {
  background: rgba(43, 124, 255, 0.08);
  border-color: rgba(43, 124, 255, 0.2);
  color: #1c3f8f;
}

.chip-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chip-arrow {
  font-size: 10px;
  color: var(--muted);
}


.main-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 10px;
}

.metric {
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid rgba(18, 18, 18, 0.08);
  background: rgba(255, 255, 255, 0.7);
}

.metric-title {
  margin-top: 6px;
  font-size: 12px;
  color: var(--muted);
}

.metric-value {
  font-size: 22px;
  font-weight: 700;
}


@media (max-width: 980px) {
  .console-topbar {
    grid-template-columns: 1fr;
  }

  .topbar-right {
    flex-wrap: wrap;
    justify-content: flex-start;
    min-width: 0;
  }
}

@media (max-width: 640px) {
  .console {
    --nav-drawer-collapsed-width: 0px;
    gap: 0;
  }

  .topbar-right {
    width: 100%;
  }

}
</style>
