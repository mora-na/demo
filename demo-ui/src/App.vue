<template>
  <div class="page">
    <div class="halo"></div>
    <main class="shell">
      <section class="hero">
        <span class="badge">Demo Access</span>
        <h1>Secure your path into the demo stack.</h1>
        <p class="lede">
          Sign in with your account, verify the captcha, and continue with a transport layer that matches the backend.
        </p>
        <div class="hero-grid">
          <div class="hero-card">
            <div class="hero-title">Transport</div>
            <div class="hero-value">{{ transportMode }}</div>
            <div class="hero-note">Password stays encrypted in transit.</div>
          </div>
          <div class="hero-card">
            <div class="hero-title">Token TTL</div>
            <div class="hero-value">2 hours</div>
            <div class="hero-note">Based on server config.</div>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>Welcome back</h2>
            <p>Authenticate to continue.</p>
          </div>
          <button class="ghost" type="button" @click="loadCaptcha" :disabled="loading">
            Refresh
          </button>
        </div>

        <form class="form" @submit.prevent="handleSubmit">
          <label class="field" data-delay="0">
            <span>Username</span>
            <input
              v-model.trim="form.userName"
              type="text"
              autocomplete="username"
              placeholder="alice"
              :disabled="loading"
            />
          </label>

          <label class="field" data-delay="1">
            <span>Password</span>
            <input
              v-model="form.password"
              type="password"
              autocomplete="current-password"
              placeholder="••••••••"
              :disabled="loading"
            />
          </label>

          <div class="captcha" data-delay="2">
            <label class="field">
              <span>Captcha</span>
              <input
                v-model.trim="form.captchaCode"
                type="text"
                autocomplete="off"
                placeholder="code"
                :disabled="loading"
              />
            </label>
            <button class="captcha-image" type="button" @click="loadCaptcha" :disabled="loading">
              <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
              <span v-else>Load</span>
            </button>
          </div>

          <button class="primary" type="submit" :disabled="loading">
            <span v-if="loading">Signing in...</span>
            <span v-else>Sign in</span>
          </button>

          <p v-if="errorMessage" class="notice error">{{ errorMessage }}</p>
          <p v-if="successMessage" class="notice success">{{ successMessage }}</p>

          <div v-if="token" class="token">
            <div class="token-label">Token</div>
            <div class="token-value">{{ token }}</div>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from "vue";
import {fetchCaptcha, login} from "./api/auth";

const form = reactive({
  userName: "",
  password: "",
  captchaCode: "",
  captchaId: ""
});

const captchaImage = ref("");
const loading = ref(false);
const errorMessage = ref("");
const successMessage = ref("");
const token = ref("");

const transportMode = (import.meta.env.VITE_PASSWORD_TRANSPORT_MODE || "plain").toUpperCase();

async function loadCaptcha() {
  errorMessage.value = "";
  try {
    const result = await fetchCaptcha();
    if (result?.code === 200 && result.data) {
      captchaImage.value = result.data.imageBase64;
      form.captchaId = result.data.captchaId;
      return;
    }
    errorMessage.value = result?.message || "Failed to load captcha";
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "Failed to load captcha";
  }
}

function getErrorMessage(error) {
  return error?.response?.data?.message || error?.message || "Login failed";
}

async function handleSubmit() {
  errorMessage.value = "";
  successMessage.value = "";
  token.value = "";

  if (!form.userName || !form.password || !form.captchaCode) {
    errorMessage.value = "Please complete all fields.";
    return;
  }

  loading.value = true;
  try {
    const result = await login({ ...form });
    if (result?.code === 200 && result.data?.token) {
      token.value = result.data.token;
      successMessage.value = "Signed in successfully.";
      localStorage.setItem("demo-token", result.data.token);
    } else {
      errorMessage.value = result?.message || "Login failed";
      await loadCaptcha();
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error);
    await loadCaptcha();
  } finally {
    loading.value = false;
  }
}

onMounted(loadCaptcha);
</script>
