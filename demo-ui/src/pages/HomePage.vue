<template>
  <main class="home">
    <section class="home-hero">
      <div class="home-title">
        <span class="badge">控制台</span>
        <h1>欢迎回来，{{ displayName }}</h1>
        <p>选择一个模块开始管理演示系统。</p>
        <div class="status-row">
          <el-tag type="success" effect="dark">在线</el-tag>
          <span>Token TTL 2 小时</span>
          <span>传输方式 {{ transportMode }}</span>
        </div>
      </div>
      <div class="home-actions">
        <el-button type="primary" size="large">进入仪表盘</el-button>
        <el-button text type="primary" :loading="loggingOut" @click="handleLogout">退出登录</el-button>
      </div>
    </section>

    <section class="nav-grid">
      <el-empty v-if="!menuItems.length" description="暂无可访问菜单" />
      <el-card v-for="menu in menuItems" v-else :key="menu.id" class="nav-card" shadow="hover">
        <div class="nav-meta">{{ menu.code || "菜单" }}</div>
        <h3>{{ menu.name }}</h3>
        <p>{{ menuDescription(menu) }}</p>
        <div class="nav-actions">
          <el-button type="primary" link>进入</el-button>
          <span class="nav-hint">{{ menuHint(menu) }}</span>
        </div>
      </el-card>
    </section>

    <section class="meta-grid">
      <el-card class="meta-card" shadow="never">
        <div class="meta-title">当前会话</div>
        <div class="meta-value">{{ displayName }}</div>
        <div class="meta-note">已同步至本地存储。</div>
      </el-card>
      <el-card class="meta-card" shadow="never">
        <div class="meta-title">角色信息</div>
        <div class="meta-value">{{ roleSummary }}</div>
        <div class="meta-note">来自当前用户角色配置。</div>
      </el-card>
      <el-card class="meta-card" shadow="never">
        <div class="meta-title">权限数量</div>
        <div class="meta-value">{{ permissionSummary }}</div>
        <div class="meta-note">包含角色权限与菜单权限。</div>
      </el-card>
      <el-card class="meta-card" shadow="never">
        <div class="meta-title">会话令牌</div>
        <div class="meta-token">{{ authStore.token }}</div>
        <div class="meta-note">用于调用后端受保护接口。</div>
      </el-card>
    </section>
  </main>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import {ElMessage} from "element-plus";
import {logout} from "../api/auth";
import {useAuthStore} from "../stores/auth";

defineProps<{transportMode: string}>();
const emit = defineEmits<{(e: "logout"): void}>();

const authStore = useAuthStore();
const displayName = computed(
  () => authStore.profile?.nickName || authStore.profile?.userName || authStore.userName || "用户"
);
const loggingOut = ref(false);
const loadingProfile = ref(false);

const menuItems = computed(() => authStore.menus || []);
const roleSummary = computed(() => (authStore.roles.length ? authStore.roles.join(" / ") : "未分配"));
const permissionSummary = computed(() => `${authStore.permissions.length} 项`);

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as {response?: {data?: {message?: string}}; message?: string};
  return err?.response?.data?.message || err?.message || fallback;
}

function menuDescription(menu: {remark?: string; path?: string; permission?: string}) {
  return menu.remark || menu.path || menu.permission || "未配置描述";
}

function menuHint(menu: {children?: unknown[]}) {
  const total = menu.children?.length || 0;
  return total ? `${total} 个子菜单` : "无子菜单";
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
      ElMessage.warning(result.message || "用户信息加载失败");
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
      ElMessage.success(result?.message || "已退出登录");
      authStore.clearSession();
      emit("logout");
    } else {
      ElMessage.error(result?.message || "退出登录失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "退出登录失败"));
  } finally {
    loggingOut.value = false;
  }
}

onMounted(loadProfile);
</script>

<style scoped>
.home {
  position: relative;
  z-index: 1;
  width: min(1200px, 100%);
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.home-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  flex-wrap: wrap;
}

.home-title h1 {
  margin: 12px 0 8px;
  font-family: "Fraunces", "Times New Roman", serif;
  font-size: clamp(30px, 4vw, 48px);
}

.home-title p {
  margin: 0;
  color: var(--muted);
}

.status-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 16px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--muted);
}

.home-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 18px;
}

.nav-grid :deep(.el-empty) {
  grid-column: 1 / -1;
}

.nav-card {
  border-radius: 20px;
  border: 1px solid rgba(18, 18, 18, 0.08);
  background: linear-gradient(140deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.78));
}

.nav-card h3 {
  margin: 8px 0;
  font-size: 18px;
}

.nav-card p {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.5;
}

.nav-meta {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--muted);
}

.nav-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.nav-hint {
  font-size: 12px;
  color: var(--muted);
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
}

.meta-card {
  border-radius: 20px;
  border: 1px solid rgba(18, 18, 18, 0.08);
  background: rgba(255, 255, 255, 0.9);
}

.meta-title {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--muted);
}

.meta-value {
  margin-top: 8px;
  font-size: 18px;
  font-weight: 600;
}

.meta-token {
  margin-top: 8px;
  font-size: 12px;
  color: var(--muted);
  word-break: break-all;
}

.meta-note {
  margin-top: 10px;
  font-size: 12px;
  color: var(--muted);
}

@media (max-width: 640px) {
  .home-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
