<template>
  <main :class="{ 'drawer-open': navDrawerVisible }" class="console">

    <aside :class="{ open: navDrawerVisible, ready: navDrawerReady }" class="console-drawer">
      <nav class="console-nav">
        <div class="nav-brand">
          <div class="nav-brand-row">
            <span class="badge">{{ t("home.nav.badge") }}</span>
            <div class="nav-title">{{ t("home.nav.title") }}</div>
            <div class="nav-sub">{{ t("home.nav.sub") }}</div>
          </div>
        </div>
        <div class="nav-section">
          <div class="nav-section-title">{{ t("home.nav.section") }}</div>
          <div class="nav-tree">
            <el-empty v-if="!filteredMenuTree.length" :description="t('home.nav.empty')"/>
            <div v-else class="nav-groups">
              <div v-for="group in filteredMenuTree" :key="group.id" class="nav-group">
                <button
                    :class="{ active: activeGroup?.id === group.id }"
                    class="nav-item nav-root"
                    type="button"
                    @click="handleGroupClick(group)"
                >
                  <span class="nav-icon">
                    <component :is="menuIconComponent(group)" class="nav-icon-svg"/>
                  </span>
                  <span class="nav-label">{{ group.name }}</span>
                  <span
                      v-if="group.children?.length"
                      :class="{ expanded: isGroupExpanded(group) }"
                      class="nav-arrow"
                  >
                    ▾
                  </span>
                </button>
                <div
                    v-if="group.children?.length"
                    v-show="isGroupExpanded(group)"
                    class="nav-children"
                >
                  <button
                      v-for="child in group.children"
                      :key="child.id"
                      :class="{ active: activeMenuId === child.id }"
                      class="nav-item nav-child"
                      type="button"
                      @click="selectMenu(child)"
                  >
                    <span class="nav-icon">
                      <component :is="menuIconComponent(child)" class="nav-icon-svg"/>
                    </span>
                    <span class="nav-label">{{ child.name }}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </nav>
    </aside>

    <div class="console-content">
      <header class="console-topbar">
        <div class="topbar-left">
          <div class="topbar-left-main">
            <el-button
                :aria-label="navDrawerVisible ? t('home.nav.collapse') : t('home.nav.expand')"
                class="icon-button nav-trigger"
                text
                @click="toggleNav"
            >
              <Menu class="topbar-icon"/>
            </el-button>
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
              v-model.trim="menuQuery"
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
                <el-button :disabled="!noticeItems.length" size="small" text @click="handleMarkAllRead">
                  全部已读
                </el-button>
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
        <OrderManagementPanel v-if="isOrderGroup"/>
        <MonitorPanel
            v-else-if="isMonitorGroup"
            :active-menu-id="activeMenuId"
            :menus="activeChildren"
            @menu-change="selectMenuById"
        />
        <DataScopePanel v-else-if="isDataScopeGroup" :active-code="activeMenuItem?.code"/>
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
      </section>
    </div>

    <el-dialog v-model="settingsVisible" :title="t('home.profile.title')" align-center width="560px">
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
        <div class="profile-note">{{ t("home.profile.note") }}</div>
      </el-form>
      <template #footer>
        <el-button @click="settingsVisible = false">{{ t("common.cancel") }}</el-button>
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
  </main>
</template>

<script lang="ts" setup>
import type {Component} from "vue";
import {computed, onMounted, onUnmounted, reactive, ref, watch} from "vue";
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  Activity,
  BarChart3,
  Bell,
  BookOpen,
  Briefcase,
  Building2,
  Circle,
  Folder,
  Home,
  KeyRound,
  LayoutDashboard,
  LayoutList,
  Menu,
  ScrollText,
  Settings2,
  Shield,
  ShoppingCart,
  SlidersHorizontal,
  Timer,
  Users,
  Wrench
} from "lucide-vue-next";
import {logout, type MenuTree, updateProfile} from "../api/auth";
import {getUnreadNoticeCount, listMyNotices, markAllNoticesRead, markNoticeRead, type NoticeMyVO} from "../api/system";
import {useAuthStore} from "../stores/auth";
import SystemManagementPanel from "./system/SystemManagementPanel.vue";
import DataScopePanel from "./system/DataScopePanel.vue";
import MonitorPanel from "./monitor/MonitorPanel.vue";
import OrderManagementPanel from "./order/OrderManagementPanel.vue";

const emit = defineEmits<{ (e: "logout"): void }>();

const authStore = useAuthStore();
const {t} = useI18n();
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
const lastNoticePingAt = ref<number | null>(null);
let noticeStream: EventSource | null = null;
let noticeStreamRetryTimer: number | null = null;
let noticeStreamHealthTimer: number | null = null;

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
const menuQuery = ref("");
const activeMenuId = ref<number | null>(null);
const navDrawerVisible = ref(true);
const navDrawerReady = ref(false);
const MENU_STORAGE_KEY = "demo.activeMenuId";
const NAV_DRAWER_STORAGE_KEY = "demo.navDrawerOpen";
const expandedGroupIds = ref<number[]>([]);

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

const activeMenuSource = computed(() => (menuQuery.value.trim() ? filteredMenuTree.value : menuItems.value));

const activeGroup = computed(() => {
  const source = activeMenuSource.value;
  if (!source.length) {
    return null;
  }
  if (activeMenuId.value == null) {
    return source[0];
  }
  return findGroupById(source, activeMenuId.value) || source[0];
});

const activeMenuItem = computed(() => findMenuById(menuItems.value, activeMenuId.value));

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

const roleSummary = computed(() =>
    authStore.roles.length ? authStore.roles.join(" / ") : t("common.roleEmpty")
);

watch(
    menuItems,
    (items) => {
      if (!items.length || activeMenuId.value != null) {
        return;
      }
      const stored = readStoredMenuId();
      if (stored != null && findMenuById(items, stored)) {
        activeMenuId.value = stored;
        return;
      }
      activeMenuId.value = items[0].id;
    },
    {immediate: true}
);

watch(
    () => [isSystemGroup.value, activeChildren.value, activeMenuId.value],
    () => {
      if (!isSystemGroup.value) {
        return;
      }
      const children = activeChildren.value;
      if (!children.length) {
        return;
      }
      const activeId = activeMenuId.value;
      if (activeId == null || !children.some((item) => item.id === activeId)) {
        activeMenuId.value = children[0].id;
      }
    },
    {immediate: true}
);

watch(
    () => [isDataScopeGroup.value, activeChildren.value, activeMenuId.value],
    () => {
      if (!isDataScopeGroup.value) {
        return;
      }
      const children = activeChildren.value;
      if (!children.length) {
        return;
      }
      const activeId = activeMenuId.value;
      if (activeId == null || !children.some((item) => item.id === activeId)) {
        activeMenuId.value = children[0].id;
      }
    },
    {immediate: true}
);

watch(
    () => [isMonitorGroup.value, activeChildren.value, activeMenuId.value],
    () => {
      if (!isMonitorGroup.value) {
        return;
      }
      const children = activeChildren.value;
      if (!children.length) {
        return;
      }
      const activeId = activeMenuId.value;
      if (activeId == null || !children.some((item) => item.id === activeId)) {
        activeMenuId.value = children[0].id;
      }
    },
    {immediate: true}
);

watch(
    () => activeMenuId.value,
    (value) => {
      if (value != null) {
        storeMenuId(value);
      }
    }
);

watch(
    () => navDrawerVisible.value,
    (value) => {
      storeNavDrawerState(value);
    }
);

watch(
    () => noticeVisible.value,
    (visible) => {
      if (visible) {
        loadMyNotices();
        refreshUnreadCount();
      }
    }
);

watch(
    () => authStore.token,
    (token, prevToken) => {
      if (!token) {
        stopNoticeStream();
        unreadCount.value = 0;
        noticeItems.value = [];
        return;
      }
      if (token !== prevToken) {
        startNoticeStream();
        refreshUnreadCount();
      }
    },
    {immediate: true}
);

function selectMenu(menu: MenuTree) {
  if (menu?.id == null) {
    return;
  }
  activeMenuId.value = menu.id;
  storeMenuId(menu.id);
}

function handleGroupClick(menu: MenuTree) {
  if (menu?.id == null) {
    return;
  }
  if (menu.children?.length) {
    toggleGroup(menu.id);
  }
  selectMenu(menu);
}

function toggleGroup(id: number) {
  const current = expandedGroupIds.value;
  if (current.includes(id)) {
    expandedGroupIds.value = current.filter((item) => item !== id);
    return;
  }
  expandedGroupIds.value = [...current, id];
}

function isGroupExpanded(group: MenuTree) {
  return expandedGroupIds.value.includes(group.id);
}

function selectMenuById(id: number) {
  activeMenuId.value = id;
  storeMenuId(id);
}

function toggleNav() {
  navDrawerVisible.value = !navDrawerVisible.value;
}

function menuInitial(name?: string) {
  if (!name) {
    return "•";
  }
  const trimmed = name.trim();
  return trimmed ? trimmed.slice(0, 1) : "•";
}

const MENU_ICON_MAP: Record<string, Component> = {
  home: Home,
  dashboard: LayoutDashboard,
  system: Settings2,
  "data-scope": Shield,
  user: Users,
  role: Shield,
  menu: LayoutList,
  dept: Building2,
  post: Briefcase,
  permission: KeyRound,
  notice: Bell,
  order: ShoppingCart,
  job: Timer,
  log: ScrollText,
  report: BarChart3,
  monitor: Activity,
  config: Settings2,
  file: Folder,
  dict: BookOpen,
  tool: Wrench
};

function menuIconComponent(menu: MenuTree): Component {
  const code = (menu.code || "").toLowerCase();
  const path = (menu.path || "").toLowerCase();
  if (MENU_ICON_MAP[code]) {
    return MENU_ICON_MAP[code];
  }
  for (const [key, icon] of Object.entries(MENU_ICON_MAP)) {
    if (code.includes(key) || path.includes(key)) {
      return icon;
    }
  }
  return Circle;
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
  const hasUnreadCount = payload && typeof payload.unreadCount === "number";
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
  if (payload && Array.isArray(payload.latestNotices) && !noticeVisible.value) {
    noticeItems.value = payload.latestNotices.map(mapLatestToNotice);
    updated = true;
  }
  return {updated, hasUnreadCount};
}

function startNoticeStream() {
  stopNoticeStream();
  const token = authStore.token;
  if (!token) {
    return;
  }
  const baseUrl = (import.meta.env.VITE_API_BASE_URL || "/prod-api").replace(/\/$/, "");
  const url = `${baseUrl}/notices/stream?token=${encodeURIComponent(token)}`;
  const source = new EventSource(url);
  noticeStream = source;
  noticeStreamConnected.value = false;
  lastNoticePingAt.value = null;
  startNoticeStreamHealthCheck();
  source.onopen = () => {
    markNoticeStreamAlive();
  };
  const handlePayloadEvent = (event: MessageEvent) => {
    markNoticeStreamAlive();
    let updated = false;
    let hasUnreadCount = false;
    if (event?.data) {
      try {
        const payload = JSON.parse(event.data);
        const result = applyNoticePayload(payload);
        updated = result.updated;
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
    stopNoticeStream();
    scheduleNoticeStreamReconnect();
  };
}

function scheduleNoticeStreamReconnect() {
  if (!authStore.token || noticeStreamRetryTimer != null) {
    return;
  }
  noticeStreamRetryTimer = window.setTimeout(() => {
    noticeStreamRetryTimer = null;
    startNoticeStream();
  }, 5000);
}

function stopNoticeStream() {
  if (noticeStream) {
    noticeStream.close();
    noticeStream = null;
  }
  if (noticeStreamRetryTimer != null) {
    window.clearTimeout(noticeStreamRetryTimer);
    noticeStreamRetryTimer = null;
  }
  stopNoticeStreamHealthCheck();
  noticeStreamConnected.value = false;
  lastNoticePingAt.value = null;
}

async function loadMyNotices() {
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
      refreshUnreadCount();
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
  await authStore.loadProfile(true);
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
      settingsVisible.value = false;
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
    emit("logout");
    return;
  }
  loggingOut.value = true;
  try {
    const result = await logout(token);
    if (result?.code === 200) {
      ElMessage.success(result?.message || t("home.msg.logoutSuccess"));
      authStore.clearSession();
      emit("logout");
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
  const storedDrawer = readStoredNavDrawerState();
  if (storedDrawer !== null) {
    navDrawerVisible.value = storedDrawer;
  }
  navDrawerReady.value = true;
  await loadProfile();
  await refreshUnreadCount();
});

onUnmounted(() => {
  stopNoticeStream();
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
  min-height: calc(100vh - 16px);
  display: flex;
  gap: 8px;
}

.console-drawer {
  flex: 0 0 var(--nav-drawer-width);
  max-width: var(--nav-drawer-width);
  min-width: 0;
  display: flex;
  overflow: hidden;
  opacity: 1;
  pointer-events: auto;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(18, 18, 18, 0.08);
  box-shadow: var(--shadow);
  backdrop-filter: blur(12px);
}

.console-drawer.ready {
  transition: flex-basis 0.2s ease, max-width 0.2s ease;
}

.console-drawer.open {
  flex-basis: var(--nav-drawer-width);
  max-width: var(--nav-drawer-width);
}

.console-drawer:not(.open) {
  flex-basis: var(--nav-drawer-collapsed-width);
  max-width: var(--nav-drawer-collapsed-width);
}

.console-content {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.console-nav {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px 10px;
}

.nav-brand {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.nav-brand-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-title {
  font-family: "Fraunces", "Times New Roman", serif;
  font-size: 22px;
}

.nav-sub {
  font-size: 13px;
  color: var(--muted);
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nav-section-title {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--muted);
}

.nav-tree {
  overflow: auto;
  padding-right: 4px;
  flex: 1;
}

.nav-groups {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  width: 100%;
  border: 1px solid transparent;
  border-radius: 14px;
  padding: 10px 12px 10px 14px;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  text-align: left;
  position: relative;
  cursor: pointer;
  transition: border 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.nav-item:hover {
  border-color: rgba(18, 18, 18, 0.16);
  transform: translateY(-1px);
}

.nav-item.active {
  border-color: rgba(43, 124, 255, 0.35);
  background: rgba(43, 124, 255, 0.12);
  box-shadow: 0 10px 20px rgba(43, 124, 255, 0.12);
}

.nav-item.active::before {
  content: "";
  position: absolute;
  left: 6px;
  top: 8px;
  bottom: 8px;
  width: 4px;
  border-radius: 999px;
  background: var(--accent);
}

.nav-root {
  font-weight: 600;
}

.nav-arrow {
  margin-left: auto;
  font-size: 12px;
  color: var(--muted);
  transition: transform 0.2s ease, color 0.2s ease;
}

.nav-arrow.expanded {
  transform: rotate(180deg);
  color: var(--accent);
}


.nav-icon {
  display: inline-flex;
  width: 28px;
  height: 28px;
  border-radius: 10px;
  background: rgba(47, 107, 255, 0.1);
  color: #2341a8;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.nav-icon-svg {
  width: 16px;
  height: 16px;
  stroke: currentColor;
  fill: none;
  stroke-width: 1.6;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.nav-item.nav-child .nav-icon {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  font-size: 12px;
  background: rgba(15, 23, 42, 0.08);
  color: #2b3a55;
}

.nav-item.nav-child .nav-icon-svg {
  width: 14px;
  height: 14px;
}

.nav-label {
  display: block;
}

.nav-children {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-left: 6px;
}

.console-drawer:not(.open) .nav-brand-row,
.console-drawer:not(.open) .nav-section-title,
.console-drawer:not(.open) .nav-label,
.console-drawer:not(.open) .badge,
.console-drawer:not(.open) .nav-arrow {
  display: none;
}

.console-drawer:not(.open) .nav-item {
  padding: 10px;
  justify-content: center;
}

.console-drawer:not(.open) .nav-children {
  padding-left: 0;
}

.console-drawer:not(.open) .nav-brand {
  justify-content: center;
}

.console-topbar {
  grid-area: topbar;
  display: grid;
  grid-template-columns: minmax(200px, 1fr) minmax(200px, 300px) auto;
  gap: 8px;
  align-items: center;
  padding: 4px 12px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(18, 18, 18, 0.08);
  box-shadow: var(--shadow);
  backdrop-filter: blur(12px);
  position: sticky;
  top: 0;
  z-index: 3;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.topbar-left-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.topbar-title {
  font-size: 15px;
  font-weight: 600;
}

.topbar-sub {
  font-size: 12px;
  color: var(--muted);
}

.topbar-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 11px;
  color: var(--muted);
}

.topbar-center {
  display: flex;
  align-items: center;
}

.topbar-search {
  width: 100%;
}

.topbar-search :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.96);
  box-shadow: inset 0 0 0 1px rgba(18, 18, 18, 0.18);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.icon-button {
  font-size: 16px;
  padding: 4px 6px;
  color: var(--muted);
}

.icon-button:hover {
  color: var(--accent);
}

.notice-badge :deep(.el-badge__content) {
  transform: translate(8px, -4px);
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

.notice-status.is-online {
  background: rgba(38, 179, 93, 0.16);
  color: #1c7f45;
}

.notice-status.is-offline {
  background: rgba(220, 38, 38, 0.12);
  color: #b91c1c;
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
  transition: border-color 0.2s ease;
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
}

.user-avatar {
  background: rgba(43, 124, 255, 0.16);
  color: #1c3f8f;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 12px;
  font-weight: 600;
}

.user-role {
  font-size: 10px;
  color: var(--muted);
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

.profile-grid :deep(.el-form-item) {
  margin-bottom: 12px;
}

.profile-grid :deep(.el-select),
.profile-grid :deep(.el-input) {
  width: 100%;
}

.console-main {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(18, 18, 18, 0.08);
  box-shadow: var(--shadow);
  flex: 1;
  min-height: 0;
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

.main-actions {
  display: flex;
  gap: 10px;
  align-items: center;
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
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

.main-card:hover {
  transform: translateY(-2px);
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
  }
}

@media (max-width: 640px) {
  .console {
    --nav-drawer-collapsed-width: 0px;
    gap: 0;
  }

  .console {
    min-height: auto;
  }

  .console-nav,
  .console-topbar,
  .console-main {
    padding: 12px;
  }

  .topbar-right {
    width: 100%;
  }

  .main-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
