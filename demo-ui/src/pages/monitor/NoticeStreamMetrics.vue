<template>
  <div v-permission="'notice:stream:metrics'" class="metrics-panel">
    <div class="metrics-head">
      <div>
        <div class="metrics-title">{{ t("notice.metrics.title") }}</div>
        <div class="metrics-sub">{{ t("notice.metrics.subtitle") }}</div>
      </div>
      <el-button size="small" text @click="loadStreamMetrics">{{ t("common.refresh") }}</el-button>
    </div>
    <div v-loading="metricsLoading" class="metrics-grid">
      <div class="metric-card">
        <div class="metric-label">{{ t("notice.metrics.connections") }}</div>
        <div class="metric-value">{{ formatConnections(streamMetrics) }}</div>
        <div class="metric-sub">{{ t("notice.metrics.activeUsers", {count: streamMetrics?.activeUsers ?? 0}) }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">{{ t("notice.metrics.cache") }}</div>
        <div class="metric-value">{{ formatCacheUsage(streamMetrics) }}</div>
        <div class="metric-sub">{{ t("notice.metrics.latestLimit", {value: streamMetrics?.latestLimit ?? 0}) }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">{{ t("notice.metrics.degrade") }}</div>
        <div class="metric-value">
          <el-tag :type="streamMetrics?.degraded ? 'danger' : 'success'" size="small">
            {{ streamMetrics?.degraded ? t("notice.metrics.degraded") : t("notice.metrics.normal") }}
          </el-tag>
        </div>
        <div class="metric-sub">{{ t("notice.metrics.autoDegrade") }}:
          {{ streamMetrics?.autoDegradeEnabled ? t("notice.metrics.enabled") : t("notice.metrics.disabled") }}
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-label">{{ t("notice.metrics.thresholds") }}</div>
        <div class="metric-value">{{ formatDegradeRatios(streamMetrics) }}</div>
        <div class="metric-sub">
          {{ t("notice.metrics.cacheExpire", {minutes: streamMetrics?.latestCacheExpireMinutes ?? 0}) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {useAuthStore} from "../../stores/auth";
import {getNoticeStreamMetrics, type NoticeStreamMetrics} from "../../api/system";

const {t} = useI18n();
const authStore = useAuthStore();
const streamMetrics = ref<NoticeStreamMetrics | null>(null);
const metricsLoading = ref(false);

function canViewStreamMetrics() {
  return authStore.permissions.includes("notice:stream:metrics");
}

async function loadStreamMetrics() {
  if (!canViewStreamMetrics()) {
    return;
  }
  if (metricsLoading.value) {
    return;
  }
  metricsLoading.value = true;
  try {
    const result = await getNoticeStreamMetrics();
    if (result?.code === 200 && result.data) {
      streamMetrics.value = result.data;
    } else {
      ElMessage.error(result?.message || t("notice.metrics.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("notice.metrics.loadFailed"));
  } finally {
    metricsLoading.value = false;
  }
}

function formatConnections(metrics: NoticeStreamMetrics | null) {
  if (!metrics) {
    return "-";
  }
  const max = metrics.maxTotalConnections;
  if (max > 0) {
    return `${metrics.totalConnections}/${max}`;
  }
  return String(metrics.totalConnections ?? 0);
}

function formatCacheUsage(metrics: NoticeStreamMetrics | null) {
  if (!metrics) {
    return "-";
  }
  const max = metrics.latestCacheMaxSize;
  if (max > 0) {
    return `${metrics.latestCacheSize}/${max}`;
  }
  return String(metrics.latestCacheSize ?? 0);
}

function formatDegradeRatios(metrics: NoticeStreamMetrics | null) {
  if (!metrics) {
    return "-";
  }
  const conn = Math.round((metrics.degradeConnectionRatio || 0) * 100);
  const cache = Math.round((metrics.degradeCacheRatio || 0) * 100);
  return t("notice.metrics.ratios", {conn, cache});
}

onMounted(() => {
  loadStreamMetrics();
});
</script>

<style scoped>
.metrics-panel {
  padding: 12px;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  background: rgba(255, 255, 255, 0.75);
}

.metrics-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 10px;
}

.metrics-title {
  font-size: 14px;
  font-weight: 600;
}

.metrics-sub {
  font-size: 12px;
  color: var(--muted);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
}

.metric-card {
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.metric-label {
  font-size: 12px;
  color: var(--muted);
}

.metric-value {
  font-size: 16px;
  font-weight: 600;
}

.metric-sub {
  font-size: 11px;
  color: var(--muted);
}
</style>
