<template>
  <main class="console" :class="{ 'nav-collapsed': navCollapsed }">
    <aside class="console-nav">
      <div class="nav-brand">
        <div class="nav-brand-row">
          <span class="badge">{{ t("home.nav.badge") }}</span>
          <div class="nav-title">{{ t("home.nav.title") }}</div>
          <div class="nav-sub">{{ t("home.nav.sub") }}</div>
        </div>
        <button
          class="nav-toggle"
          type="button"
          :aria-label="navCollapsed ? t('home.nav.expand') : t('home.nav.collapse')"
          @click="toggleNav"
        >
          <span v-if="navCollapsed">»</span>
          <span v-else>«</span>
        </button>
      </div>
      <div class="nav-section">
        <div class="nav-section-title">{{ t("home.nav.section") }}</div>
        <div class="nav-tree">
          <el-empty v-if="!filteredMenuTree.length" :description="t('home.nav.empty')" />
          <div v-else class="nav-groups">
            <div v-for="group in filteredMenuTree" :key="group.id" class="nav-group">
              <button
                class="nav-item nav-root"
                :class="{ active: activeGroup?.id === group.id }"
                type="button"
                @click="selectMenu(group)"
              >
                <span class="nav-icon">{{ menuInitial(group.name) }}</span>
                <span class="nav-label">{{ group.name }}</span>
              </button>
              <div v-if="group.children?.length" class="nav-children">
                <button
                  v-for="child in group.children"
                  :key="child.id"
                  class="nav-item nav-child"
                  :class="{ active: activeMenuId === child.id }"
                  type="button"
                  @click="selectMenu(child)"
                >
                  <span class="nav-icon">{{ menuInitial(child.name) }}</span>
                  <span class="nav-label">{{ child.name }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <header class="console-topbar">
      <div class="topbar-left">
        <div>
          <div class="topbar-title">{{ t("home.topbar.title") }}</div>
          <div class="topbar-sub">{{ activeGroup?.name || t("home.topbar.chooseModule") }}</div>
        </div>
        <div class="topbar-tags">
          <el-tag type="success" effect="dark">{{ t("common.online") }}</el-tag>
        </div>
      </div>
      <div class="topbar-center">
        <el-input
          v-model.trim="menuQuery"
          class="topbar-search"
          :placeholder="t('home.topbar.searchPlaceholder')"
          clearable
        />
      </div>
      <div class="topbar-right">
        <el-button text class="icon-button" :aria-label="t('home.topbar.notifications')">🔔</el-button>
        <el-button text class="icon-button" :aria-label="t('home.topbar.settings')" @click="openSettings">⚙️</el-button>
        <el-dropdown trigger="click">
          <button class="topbar-user" type="button">
            <el-avatar size="36" class="user-avatar">{{ userInitial }}</el-avatar>
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
      <div class="main-hero">
        <div>
          <h1>{{ activeGroup?.name || t("home.main.titleFallback") }}</h1>
          <p>{{ activeGroup ? menuDescription(activeGroup) : t("home.main.descFallback") }}</p>
        </div>
      </div>

      <div class="main-grid">
        <el-empty v-if="!activeChildren.length" :description="t('home.main.empty')" />
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
    </section>

    <el-dialog v-model="settingsVisible" :title="t('home.profile.title')" width="460px" align-center>
      <el-form :model="profileForm" label-position="top">
        <el-form-item :label="t('home.profile.userName')">
          <el-input v-model.trim="profileForm.userName" disabled />
        </el-form-item>
        <el-form-item :label="t('home.profile.nickName')">
          <el-input v-model.trim="profileForm.nickName" :placeholder="t('home.profile.nickNamePlaceholder')" />
        </el-form-item>
        <el-divider />
        <el-form-item :label="t('home.profile.oldPassword')">
          <el-input v-model.trim="profileForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="t('home.profile.newPassword')">
          <el-input v-model.trim="profileForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="t('home.profile.confirmPassword')">
          <el-input v-model.trim="profileForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <div class="profile-note">{{ t("home.profile.note") }}</div>
      </el-form>
      <template #footer>
        <el-button @click="settingsVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button type="primary" :loading="savingProfile" @click="handleProfileSave">
          {{ t("common.save") }}
        </el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue";
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {logout, type MenuTree, updateProfile} from "../api/auth";
import {useAuthStore} from "../stores/auth";

const emit = defineEmits<{(e: "logout"): void}>();

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

const profileForm = ref({
  userName: "",
  nickName: "",
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

const menuItems = computed(() => authStore.menus || []);
const menuQuery = ref("");
const activeMenuId = ref<number | null>(null);
const navCollapsed = ref(false);

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

const activeChildren = computed(() => {
  if (!activeGroup.value) {
    return [];
  }
  if (activeGroup.value.children && activeGroup.value.children.length) {
    return activeGroup.value.children;
  }
  return [activeGroup.value];
});

const roleSummary = computed(() =>
  authStore.roles.length ? authStore.roles.join(" / ") : t("common.roleEmpty")
);

watch(
  menuItems,
  (items) => {
    if (items.length && activeMenuId.value == null) {
      activeMenuId.value = items[0].id;
    }
  },
  {immediate: true}
);

function selectMenu(menu: MenuTree) {
  if (menu?.id == null) {
    return;
  }
  activeMenuId.value = menu.id;
}

function toggleNav() {
  navCollapsed.value = !navCollapsed.value;
}

function menuInitial(name?: string) {
  if (!name) {
    return "•";
  }
  const trimmed = name.trim();
  return trimmed ? trimmed.slice(0, 1) : "•";
}

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as {response?: {data?: {message?: string}}; message?: string};
  return err?.response?.data?.message || err?.message || fallback;
}

function openSettings() {
  const profile = authStore.profile;
  profileForm.value = {
    userName: profile?.userName || authStore.userName || "",
    nickName: profile?.nickName || "",
    oldPassword: "",
    newPassword: "",
    confirmPassword: ""
  };
  originalNickName.value = profileForm.value.nickName || "";
  settingsVisible.value = true;
}

async function handleProfileSave() {
  if (savingProfile.value) {
    return;
  }
  const nickName = profileForm.value.nickName.trim();
  const wantsPasswordChange =
    Boolean(profileForm.value.oldPassword) ||
    Boolean(profileForm.value.newPassword) ||
    Boolean(profileForm.value.confirmPassword);

  if (!wantsPasswordChange && nickName === originalNickName.value) {
    ElMessage.warning(t("home.profile.msg.noChanges"));
    return;
  }

  if (wantsPasswordChange) {
    if (!profileForm.value.oldPassword || !profileForm.value.newPassword || !profileForm.value.confirmPassword) {
      ElMessage.warning(t("home.profile.msg.fillPassword"));
      return;
    }
    if (profileForm.value.newPassword !== profileForm.value.confirmPassword) {
      ElMessage.warning(t("home.profile.msg.confirmMismatch"));
      return;
    }
  }

  savingProfile.value = true;
  try {
    const result = await updateProfile({
      nickName: nickName || undefined,
      oldPassword: wantsPasswordChange ? profileForm.value.oldPassword : undefined,
      newPassword: wantsPasswordChange ? profileForm.value.newPassword : undefined
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

function menuDescription(menu: {remark?: string}) {
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

onMounted(loadProfile);
</script>

<style scoped>
.console {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: none;
  min-height: calc(100vh - 32px);
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  grid-template-rows: auto 1fr;
  grid-template-areas:
    "nav topbar"
    "nav main";
  gap: 12px;
}

.console.nav-collapsed {
  grid-template-columns: 76px minmax(0, 1fr);
}

.console-nav {
  grid-area: nav;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 14px 12px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(18, 18, 18, 0.08);
  box-shadow: var(--shadow);
  backdrop-filter: blur(12px);
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

.nav-toggle {
  border: 1px solid rgba(18, 18, 18, 0.1);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  width: 32px;
  height: 32px;
  font-size: 16px;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: border 0.2s ease, transform 0.2s ease;
}

.nav-toggle:hover {
  border-color: rgba(43, 124, 255, 0.4);
  transform: translateY(-1px);
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
  padding: 10px 12px 10px 18px;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  flex-direction: column;
  gap: 4px;
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


.nav-icon {
  display: none;
  width: 28px;
  height: 28px;
  border-radius: 10px;
  background: rgba(43, 124, 255, 0.12);
  color: #1c3f8f;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
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

.console.nav-collapsed .nav-brand-row,
.console.nav-collapsed .nav-section-title,
.console.nav-collapsed .nav-label,
.console.nav-collapsed .badge {
  display: none;
}

.console.nav-collapsed .nav-item {
  padding: 10px;
  align-items: center;
}

.console.nav-collapsed .nav-icon {
  display: inline-flex;
}

.console.nav-collapsed .nav-children {
  padding-left: 0;
}

.console.nav-collapsed .console-nav {
  padding: 12px 8px;
}

.console.nav-collapsed .nav-brand {
  justify-content: center;
}

.console-topbar {
  grid-area: topbar;
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(220px, 320px) auto;
  gap: 12px;
  align-items: center;
  padding: 10px 14px;
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
  flex-direction: column;
  gap: 6px;
}

.topbar-title {
  font-size: 16px;
  font-weight: 600;
}

.topbar-sub {
  font-size: 13px;
  color: var(--muted);
}

.topbar-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
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
  gap: 8px;
}

.icon-button {
  font-size: 18px;
  padding: 6px 8px;
  color: var(--muted);
}

.icon-button:hover {
  color: var(--accent);
}

.topbar-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
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
  font-size: 13px;
  font-weight: 600;
}

.user-role {
  font-size: 11px;
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

.console-main {
  grid-area: main;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(18, 18, 18, 0.08);
  box-shadow: var(--shadow);
}

.main-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
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
  gap: 12px;
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
  gap: 12px;
}

.metric {
  padding: 12px 14px;
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


@media (max-width: 1200px) {
  .console {
    grid-template-columns: 220px minmax(0, 1fr);
    grid-template-rows: auto 1fr;
    grid-template-areas:
      "nav topbar"
      "nav main";
  }
  .console.nav-collapsed {
    grid-template-columns: 72px minmax(0, 1fr);
  }
}

@media (max-width: 980px) {
  .console {
    grid-template-columns: 1fr;
    grid-template-areas:
      "topbar"
      "nav"
      "main";
  }
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
    min-height: auto;
  }
  .console-nav,
  .console-topbar,
  .console-main {
    padding: 16px;
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
