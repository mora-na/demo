<template>
  <div class="page" :class="{ 'page--home': currentView === 'home' }">
    <div class="halo"></div>
    <LoginPage
      v-if="currentView === 'login'"
      :transport-mode="transportMode"
      @login-success="handleLoginSuccess"
    />
    <HomePage v-else-if="currentView === 'home'" @logout="handleLogout" />
    <div v-else class="boot">正在校验登录状态…</div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {useAuthStore} from "./stores/auth";
import HomePage from "./pages/HomePage.vue";
import LoginPage from "./pages/LoginPage.vue";

const authStore = useAuthStore();
const currentView = ref<"checking" | "login" | "home">("checking");
const transportMode = (import.meta.env.VITE_PASSWORD_TRANSPORT_MODE || "plain").toUpperCase();

function handleLoginSuccess() {
  currentView.value = "home";
}

function handleLogout() {
  currentView.value = "login";
}

onMounted(async () => {
  if (!authStore.token) {
    currentView.value = "login";
    return;
  }
  try {
    const result = await authStore.loadProfile();
    if (result.ok) {
      currentView.value = "home";
      return;
    }
  } catch {
    // Swallow errors and fall back to login.
  }
  authStore.clearSession();
  currentView.value = "login";
});
</script>

<style scoped>
.page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  overflow: hidden;
}

.page--home {
  align-items: stretch;
  justify-content: flex-start;
}

.page::before,
.page::after {
  content: "";
  position: absolute;
  border-radius: 999px;
  opacity: 0.65;
  z-index: 0;
}

.page::before {
  width: 420px;
  height: 420px;
  background: radial-gradient(circle at 30% 30%, rgba(43, 124, 255, 0.5), rgba(43, 124, 255, 0));
  top: -140px;
  left: -160px;
}

.page::after {
  width: 520px;
  height: 520px;
  background: radial-gradient(circle at 40% 40%, rgba(43, 124, 255, 0.4), rgba(43, 124, 255, 0));
  bottom: -180px;
  right: -200px;
}

.halo {
  position: absolute;
  width: 900px;
  height: 900px;
  border-radius: 50%;
  background: conic-gradient(
    from 90deg,
    rgba(255, 255, 255, 0.4),
    rgba(25, 183, 255, 0.18),
    rgba(43, 124, 255, 0.22),
    rgba(255, 255, 255, 0.4)
  );
  opacity: 0.35;
  transform: translateY(-20%) rotate(-10deg);
  z-index: 0;
  animation: drift 18s ease-in-out infinite;
}

.boot {
  position: relative;
  z-index: 1;
  font-size: 14px;
  color: var(--muted);
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(18, 18, 18, 0.08);
  box-shadow: var(--shadow);
  backdrop-filter: blur(12px);
  padding: 16px 22px;
  border-radius: 999px;
}

@keyframes drift {
  0%,
  100% {
    transform: translateY(-20%) rotate(-10deg);
  }
  50% {
    transform: translateY(-24%) rotate(6deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .halo {
    animation: none;
  }
}
</style>
