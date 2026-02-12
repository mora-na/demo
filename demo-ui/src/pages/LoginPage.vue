<template>
  <main class="shell">
    <section class="hero">
      <span class="badge">{{ t("login.badge") }}</span>
      <h1>{{ t("login.title") }}</h1>
      <p class="lede">
        {{ t("login.lede") }}
      </p>
      <div class="hero-grid">
        <div class="hero-card">
          <div class="hero-title">{{ t("login.transport.title") }}</div>
          <div class="hero-value">{{ transportMode }}</div>
          <div class="hero-note">{{ t("login.transport.note") }}</div>
        </div>
        <div class="hero-card">
          <div class="hero-title">{{ t("login.tokenTtl.title") }}</div>
          <div class="hero-value">{{ t("login.tokenTtl.value", {hours: tokenTtlHours}) }}</div>
          <div class="hero-note">{{ t("login.tokenTtl.note") }}</div>
        </div>
        <div class="hero-card">
          <div class="hero-title">{{ t("login.security.title") }}</div>
          <div class="hero-value">{{ t("login.security.value") }}</div>
          <div class="hero-note">{{ t("login.security.note") }}</div>
        </div>
      </div>
      <div class="hero-strip">
        <div class="strip-item">
          <span class="strip-label">{{ t("login.status.system") }}</span>
          <span class="strip-value">{{ t("login.status.online") }}</span>
        </div>
        <div class="strip-item">
          <span class="strip-label">{{ t("login.status.level") }}</span>
          <span class="strip-value">{{ t("login.status.controlled") }}</span>
        </div>
        <div class="strip-item">
          <span class="strip-label">{{ t("login.status.window") }}</span>
          <span class="strip-value">{{ t("login.status.realtime") }}</span>
        </div>
      </div>
    </section>

    <section class="panel">
      <el-card class="login-card" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <h2>{{ t("login.panel.title") }}</h2>
              <p>{{ t("login.panel.subtitle") }}</p>
            </div>
          </div>
        </template>

        <el-form class="login-form" label-position="top" @submit.prevent="handleSubmit">
          <el-form-item :label="t('login.form.username')">
            <el-input
                v-model.trim="form.userName"
                :disabled="loading"
                :placeholder="t('login.form.usernamePlaceholder')"
                autocomplete="username"
            />
          </el-form-item>

          <el-form-item :label="t('login.form.password')">
            <el-input
                v-model="form.password"
                :disabled="loading"
                :placeholder="t('login.form.passwordPlaceholder')"
                autocomplete="current-password"
                show-password
                type="password"
            />
          </el-form-item>

          <div class="captcha-row">
            <el-form-item :label="t('login.form.captcha')" class="captcha-field">
              <el-input
                  v-model.trim="form.captchaCode"
                  :disabled="loading"
                  :placeholder="t('login.form.captchaPlaceholder')"
                  autocomplete="off"
              />
            </el-form-item>
            <el-button :disabled="loading" class="captcha-button" @click="loadCaptcha">
              <img v-if="captchaImage" :alt="t('login.form.captcha')" :src="captchaImage"/>
              <span v-else>{{ t("login.form.captchaLoading") }}</span>
            </el-button>
          </div>

          <el-button
              :class="{ 'is-loading': loading }"
              :loading="loading"
              class="login-submit"
              native-type="submit"
              size="large"
              type="primary"
              @click="handleSubmit"
          >
            {{ t("login.form.submit") }}
          </el-button>

          <p class="helper">
            {{ t("login.form.helper") }}
          </p>
        </el-form>
      </el-card>
    </section>
  </main>
</template>

<script lang="ts" setup>
import {onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {fetchCaptcha, login} from "../api/auth";
import {useAuthStore} from "../stores/auth";

defineProps<{ transportMode: string }>();
const emit = defineEmits<{ (e: "login-success"): void }>();

const authStore = useAuthStore();
const {t} = useI18n();
const tokenTtlHours = 2;

const form = reactive({
  userName: "",
  password: "",
  captchaCode: "",
  captchaId: ""
});

const captchaImage = ref("");
const loading = ref(false);

async function loadCaptcha() {
  try {
    const result = await fetchCaptcha();
    if (result?.code === 200 && result.data) {
      captchaImage.value = result.data.imageBase64;
      form.captchaId = result.data.captchaId;
      return;
    }
    ElMessage.error(result?.message || t("login.msg.captchaLoadFailed"));
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("login.msg.captchaLoadFailed")));
  }
}

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

async function handleSubmit() {
  if (loading.value) {
    return;
  }
  if (!form.userName || !form.password || !form.captchaCode) {
    ElMessage.warning(t("login.msg.fillAll"));
    return;
  }

  loading.value = true;
  try {
    const result = await login({...form});
    if (result?.code === 200 && result.data?.token) {
      authStore.setSession(result.data.token, form.userName);
      const profileResult = await authStore.loadProfile(true);
      if (!profileResult.ok) {
        ElMessage.warning(profileResult.message || t("login.msg.profileLoadFailed"));
      }
      ElMessage.success(t("login.msg.welcomeUser", {name: form.userName}));
      emit("login-success");
      form.password = "";
      form.captchaCode = "";
    } else {
      ElMessage.error(result?.message || t("login.msg.loginFailed"));
      form.captchaCode = "";
      await loadCaptcha();
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("login.msg.loginFailed")));
    form.captchaCode = "";
    await loadCaptcha();
  } finally {
    loading.value = false;
  }
}

onMounted(loadCaptcha);
</script>

<style scoped>
.shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(280px, 1.05fr) minmax(280px, 0.95fr);
  gap: 48px;
  max-width: 1120px;
  width: 100%;
}

.hero {
  padding: 24px 12px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero h1 {
  font-family: "Fraunces", "Times New Roman", serif;
  font-size: clamp(32px, 4vw, 54px);
  line-height: 1.05;
  margin: 0;
}

.lede {
  font-size: 18px;
  line-height: 1.5;
  color: var(--muted);
  margin: 0;
}

.hero-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.hero-card {
  padding: 18px 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(18, 18, 18, 0.08);
  box-shadow: 0 12px 30px rgba(18, 18, 18, 0.08);
}

.hero-title {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--muted);
}

.hero-value {
  font-size: 22px;
  font-weight: 600;
  margin-top: 6px;
}

.hero-note {
  margin-top: 8px;
  font-size: 13px;
  color: var(--muted);
}

.hero-strip {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.strip-item {
  border-radius: 14px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(18, 18, 18, 0.08);
}

.strip-label {
  display: block;
  font-size: 12px;
  color: var(--muted);
}

.strip-value {
  display: block;
  margin-top: 6px;
  font-weight: 600;
}

.panel {
  display: flex;
  align-items: center;
}

.login-card {
  width: 100%;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(18, 18, 18, 0.06);
  box-shadow: var(--shadow);
}

.login-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(18, 18, 18, 0.06);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.panel-header h2 {
  margin: 0 0 6px 0;
  font-family: "Fraunces", "Times New Roman", serif;
  font-size: 26px;
}

.panel-header p {
  margin: 0;
  color: var(--muted);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 150px;
  gap: 12px;
  align-items: end;
}

.captcha-field {
  margin-bottom: 0;
}

.captcha-button {
  height: 54px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: #fdfbf8;
  padding: 0;
  overflow: hidden;
}

.captcha-button img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.login-submit {
  width: 100%;
  margin-top: 8px;
  box-shadow: 0 12px 24px rgba(43, 124, 255, 0.28);
  position: relative;
  overflow: hidden;
  transition: transform 0.18s ease, box-shadow 0.2s ease;
}

.login-submit::after {
  content: "";
  position: absolute;
  inset: -40% -20%;
  background: linear-gradient(120deg, transparent 30%, rgba(255, 255, 255, 0.55) 50%, transparent 70%);
  opacity: 0;
  transform: translateX(-60%);
}

.login-submit.is-loading {
  transform: translateY(1px);
  box-shadow: 0 10px 18px rgba(43, 124, 255, 0.24);
}

.login-submit.is-loading::after {
  opacity: 1;
  animation: login-shimmer 1.1s linear infinite;
}

.login-submit:active {
  transform: translateY(2px) scale(0.99);
}

@keyframes login-shimmer {
  0% {
    transform: translateX(-60%);
  }
  100% {
    transform: translateX(60%);
  }
}

.helper {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--muted);
  text-align: center;
}

@media (max-width: 980px) {
  .shell {
    grid-template-columns: 1fr;
  }

  .hero {
    order: 2;
    padding: 0;
  }

  .panel {
    order: 1;
  }
}

@media (max-width: 640px) {
  .panel {
    width: 100%;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }

  .captcha-button {
    height: 64px;
  }
}
</style>
