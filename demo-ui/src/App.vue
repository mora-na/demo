<template>
  <el-config-provider :locale="elementLocale">
    <div :class="{ 'page--home': isHomeRoute }" class="page">
      <div class="halo"></div>
      <router-view/>
    </div>
  </el-config-provider>
</template>

<script lang="ts" setup>
import {computed} from "vue";
import {useI18n} from "vue-i18n";
import enUS from "element-plus/es/locale/lang/en";
import zhCN from "element-plus/es/locale/lang/zh-cn";
import {useRoute} from "vue-router";

const {locale} = useI18n();
const route = useRoute();
const elementLocale = computed(() => (locale.value === "en-US" ? enUS : zhCN));
const isHomeRoute = computed(() => route.path.startsWith("/home"));
</script>

<style scoped>
.page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
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
}
</style>
