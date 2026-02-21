<template>
  <div v-permission="'job:log:metrics'" class="metrics-panel">
    <div class="metrics-head">
      <div>
        <div class="metrics-title">{{ t("job.metrics.title") }}</div>
        <div class="metrics-sub">{{ t("job.metrics.subtitle") }}</div>
      </div>
      <el-button size="small" text @click="loadLogCollectorMetrics">{{ t("common.refresh") }}</el-button>
    </div>
    <div v-loading="metricsLoading" class="metrics-grid">
      <div class="metric-card">
        <div class="metric-label">{{ t("job.metrics.buffers") }}</div>
        <div class="metric-value">{{ formatBufferUsage(logCollectorMetrics) }}</div>
        <div class="metric-sub">{{ t("job.metrics.maxLength", {value: logCollectorMetrics?.maxLength ?? 0}) }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">{{ t("job.metrics.hold") }}</div>
        <div class="metric-value">{{ formatHold(logCollectorMetrics) }}</div>
        <div class="metric-sub">{{
            t("job.metrics.mergeDelay", {value: logCollectorMetrics?.mergeDelayMillis ?? 0})
          }}
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-label">{{ t("job.metrics.degrade") }}</div>
        <div class="metric-value">
          <el-tag :type="logCollectorMetrics?.degraded ? 'danger' : 'success'" size="small">
            {{ logCollectorMetrics?.degraded ? t("job.metrics.degraded") : t("job.metrics.normal") }}
          </el-tag>
        </div>
        <div class="metric-sub">{{ t("job.metrics.autoDegrade") }}:
          {{ logCollectorMetrics?.autoDegradeEnabled ? t("job.metrics.enabled") : t("job.metrics.disabled") }}
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-label">{{ t("job.metrics.enabledLabel") }}</div>
        <div class="metric-value">
          <el-tag :type="logCollectorMetrics?.enabled ? 'success' : 'warning'" size="small">
            {{ logCollectorMetrics?.enabled ? t("job.metrics.enabled") : t("job.metrics.disabled") }}
          </el-tag>
        </div>
        <div class="metric-sub">
          {{ t("job.metrics.ratio", {value: formatRatio(logCollectorMetrics?.degradeBufferRatio)}) }}
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
import {getJobLogCollectorMetrics, type JobLogCollectorMetrics} from "../../api/system";

const {t} = useI18n();
const authStore = useAuthStore();
const logCollectorMetrics = ref<JobLogCollectorMetrics | null>(null);
const metricsLoading = ref(false);

function canViewLogMetrics() {
  return authStore.permissions.includes("job:log:metrics");
}

async function loadLogCollectorMetrics() {
  if (!canViewLogMetrics()) {
    return;
  }
  if (metricsLoading.value) {
    return;
  }
  metricsLoading.value = true;
  try {
    const result = await getJobLogCollectorMetrics();
    if (result?.code === 200 && result.data) {
      logCollectorMetrics.value = result.data;
    } else {
      ElMessage.error(result?.message || t("job.metrics.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("job.metrics.loadFailed"));
  } finally {
    metricsLoading.value = false;
  }
}

function formatBufferUsage(metrics: JobLogCollectorMetrics | null) {
  if (!metrics) {
    return "-";
  }
  const max = metrics.maxBuffers;
  if (max > 0) {
    return `${metrics.bufferSize}/${max}`;
  }
  return String(metrics.bufferSize ?? 0);
}

function formatHold(metrics: JobLogCollectorMetrics | null) {
  if (!metrics) {
    return "-";
  }
  const seconds = Math.round((metrics.maxHoldMillis || 0) / 1000);
  return `${seconds}s`;
}

function formatRatio(value?: number) {
  const ratio = Math.round((value || 0) * 100);
  return `${ratio}%`;
}

onMounted(() => {
  loadLogCollectorMetrics();
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
