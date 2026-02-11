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
        <el-button text type="primary" @click="handleLogout">退出登录</el-button>
      </div>
    </section>

    <section class="nav-grid">
      <el-card v-for="item in navItems" :key="item.title" class="nav-card" shadow="hover">
        <div class="nav-meta">{{ item.tag }}</div>
        <h3>{{ item.title }}</h3>
        <p>{{ item.desc }}</p>
        <div class="nav-actions">
          <el-button type="primary" link>进入</el-button>
          <span class="nav-hint">{{ item.hint }}</span>
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
        <div class="meta-title">会话令牌</div>
        <div class="meta-token">{{ authStore.token }}</div>
        <div class="meta-note">用于调用后端受保护接口。</div>
      </el-card>
      <el-card class="meta-card" shadow="never">
        <div class="meta-title">安全策略</div>
        <div class="meta-value">验证码 + 传输加密</div>
        <div class="meta-note">与服务端配置一致。</div>
      </el-card>
    </section>
  </main>
</template>

<script setup lang="ts">
import {computed} from "vue";
import {useAuthStore} from "../stores/auth";

defineProps<{transportMode: string}>();
const emit = defineEmits<{(e: "logout"): void}>();

const authStore = useAuthStore();
const displayName = computed(() => authStore.userName || "用户");

const navItems = [
  {
    title: "用户与权限",
    desc: "管理账号、角色分配与权限策略。",
    tag: "核心模块",
    hint: "权限策略"
  },
  {
    title: "系统设置",
    desc: "配置验证码、密钥与安全策略。",
    tag: "配置中心",
    hint: "安全策略"
  },
  {
    title: "接口监控",
    desc: "查看接口运行状态与调用日志。",
    tag: "可观测性",
    hint: "接口日志"
  },
  {
    title: "数据看板",
    desc: "追踪关键指标与告警状态。",
    tag: "洞察",
    hint: "指标趋势"
  }
];

function handleLogout() {
  authStore.clearSession();
  emit("logout");
}
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
