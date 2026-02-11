<template>
  <main class="shell">
    <section class="hero">
      <span class="badge">演示入口</span>
      <h1>安全进入演示系统。</h1>
      <p class="lede">
        使用账号登录，完成验证码校验，并选择与后端匹配的传输方式。
      </p>
      <div class="hero-grid">
        <div class="hero-card">
          <div class="hero-title">传输方式</div>
          <div class="hero-value">{{ transportMode }}</div>
          <div class="hero-note">传输过程中密码保持加密。</div>
        </div>
        <div class="hero-card">
          <div class="hero-title">令牌有效期</div>
          <div class="hero-value">2 小时</div>
          <div class="hero-note">基于服务端配置。</div>
        </div>
        <div class="hero-card">
          <div class="hero-title">安全策略</div>
          <div class="hero-value">验证码</div>
          <div class="hero-note">每次登录前需校验。</div>
        </div>
      </div>
      <div class="hero-strip">
        <div class="strip-item">
          <span class="strip-label">系统状态</span>
          <span class="strip-value">在线</span>
        </div>
        <div class="strip-item">
          <span class="strip-label">安全等级</span>
          <span class="strip-value">受控</span>
        </div>
        <div class="strip-item">
          <span class="strip-label">登录窗口</span>
          <span class="strip-value">实时刷新</span>
        </div>
      </div>
    </section>

    <section class="panel">
      <el-card class="login-card" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <h2>欢迎回来</h2>
              <p>请验证身份后继续。</p>
            </div>
            <el-button text type="primary" @click="loadCaptcha" :disabled="loading">
              刷新验证码
            </el-button>
          </div>
        </template>

        <el-form class="login-form" label-position="top" @submit.prevent="handleSubmit">
          <el-form-item label="用户名">
            <el-input
              v-model.trim="form.userName"
              autocomplete="username"
              placeholder="请输入用户名"
              :disabled="loading"
            />
          </el-form-item>

          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              autocomplete="current-password"
              placeholder="请输入密码"
              :disabled="loading"
            />
          </el-form-item>

          <div class="captcha-row">
            <el-form-item class="captcha-field" label="验证码">
              <el-input
                v-model.trim="form.captchaCode"
                autocomplete="off"
                placeholder="输入验证码"
                :disabled="loading"
              />
            </el-form-item>
            <el-button class="captcha-button" @click="loadCaptcha" :disabled="loading">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else>加载验证码</span>
            </el-button>
          </div>

          <el-button
            class="login-submit"
            type="primary"
            size="large"
            native-type="submit"
            :loading="loading"
            @click="handleSubmit"
          >
            登录
          </el-button>

          <p class="helper">
            登录后将进入首页导航面板，可继续访问演示模块。
          </p>
        </el-form>
      </el-card>
    </section>
  </main>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {fetchCaptcha, login} from "../api/auth";
import {useAuthStore} from "../stores/auth";

defineProps<{transportMode: string}>();
const emit = defineEmits<{(e: "login-success"): void}>();

const authStore = useAuthStore();

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
    ElMessage.error(result?.message || "验证码加载失败");
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "验证码加载失败"));
  }
}

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as {response?: {data?: {message?: string}}; message?: string};
  return err?.response?.data?.message || err?.message || fallback;
}

async function handleSubmit() {
  if (!form.userName || !form.password || !form.captchaCode) {
    ElMessage.warning("请填写所有字段。");
    return;
  }

  loading.value = true;
  try {
    const result = await login({...form});
    if (result?.code === 200 && result.data?.token) {
      authStore.setSession(result.data.token, form.userName);
      const profileResult = await authStore.loadProfile(true);
      if (!profileResult.ok) {
        ElMessage.warning(profileResult.message || "用户信息加载失败");
      }
      ElMessage.success(`欢迎，${form.userName}！`);
      form.password = "";
      form.captchaCode = "";
      setTimeout(() => {
        emit("login-success");
      }, 900);
    } else {
      ElMessage.error(result?.message || "登录失败");
      form.captchaCode = "";
      await loadCaptcha();
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "登录失败"));
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
  box-shadow: 0 12px 24px rgba(255, 107, 74, 0.28);
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
