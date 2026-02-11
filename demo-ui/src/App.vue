<template>
  <div class="page">
    <div class="halo"></div>
    <LoginPage
      v-if="currentView === 'login'"
      :transport-mode="transportMode"
      @login-success="handleLoginSuccess"
    />
    <HomePage v-else :transport-mode="transportMode" @logout="handleLogout" />
  </div>
</template>

<script setup lang="ts">
import {ref} from "vue";
import {useAuthStore} from "./stores/auth";
import HomePage from "./pages/HomePage.vue";
import LoginPage from "./pages/LoginPage.vue";

const authStore = useAuthStore();
const currentView = ref<"login" | "home">(authStore.isAuthenticated ? "home" : "login");
const transportMode = (import.meta.env.VITE_PASSWORD_TRANSPORT_MODE || "plain").toUpperCase();

function handleLoginSuccess() {
  currentView.value = "home";
}

function handleLogout() {
  currentView.value = "login";
}
</script>

<style scoped>
.page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  overflow: hidden;
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
  background: radial-gradient(circle at 30% 30%, rgba(255, 107, 74, 0.55), rgba(255, 107, 74, 0));
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
    rgba(255, 107, 74, 0.18),
    rgba(43, 124, 255, 0.2),
    rgba(255, 255, 255, 0.4)
  );
  opacity: 0.35;
  transform: translateY(-20%) rotate(-10deg);
  z-index: 0;
  animation: drift 18s ease-in-out infinite;
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
