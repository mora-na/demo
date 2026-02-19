<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("dynamicApiLog.title") }}</div>
        <div class="module-sub">{{ t("dynamicApiLog.subtitle") }}</div>
      </div>
      <div class="module-actions" @keyup.enter="handleSearch">
        <div class="filter-group">
          <el-input
              v-model.trim="filters.apiPath"
              :placeholder="t('dynamicApiLog.filter.apiPath')"
              class="filter-input filter-input--wide"
              clearable
              size="small"
          />
          <el-input
              v-model.trim="filters.apiMethod"
              :placeholder="t('dynamicApiLog.filter.apiMethod')"
              class="filter-input filter-input--narrow"
              clearable
              size="small"
          />
          <el-input
              v-model.trim="filters.userName"
              :placeholder="t('dynamicApiLog.filter.user')"
              class="filter-input"
              clearable
              size="small"
          />
          <el-select
              v-model="filters.status"
              :placeholder="t('dynamicApiLog.filter.status')"
              class="filter-input filter-input--narrow"
              clearable
              size="small"
          >
            <el-option :label="t('dynamicApiLog.status.success')" :value="1"/>
            <el-option :label="t('dynamicApiLog.status.fail')" :value="0"/>
          </el-select>
          <el-date-picker
              v-model="filters.timeRange"
              :end-placeholder="t('dynamicApiLog.filter.end')"
              :start-placeholder="t('dynamicApiLog.filter.begin')"
              class="filter-input filter-date"
              clearable
              range-separator="-"
              size="small"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
          <el-button size="small" @click="handleSearch">{{ t("common.search") }}</el-button>
          <el-button size="small" @click="handleReset">{{ t("dynamicApiLog.filter.reset") }}</el-button>
        </div>
      </div>
    </div>

    <el-table v-loading="loading" :data="logs" row-key="id" size="small" table-layout="auto">
      <el-table-column type="expand">
        <template #default="{row}">
          <div class="detail-grid">
            <div class="detail-item">
              <div class="detail-label">{{ t("dynamicApiLog.table.code") }}</div>
              <div class="detail-value">{{ row.responseCode ?? "-" }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">{{ t("dynamicApiLog.table.duration") }}</div>
              <div class="detail-value">{{ row.durationMs != null ? `${row.durationMs}ms` : "-" }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">{{ t("dynamicApiLog.table.user") }}</div>
              <div class="detail-value">{{ row.userName || "-" }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">{{ t("dynamicApiLog.table.ip") }}</div>
              <div class="detail-value">{{ row.requestIp || "-" }}</div>
            </div>
            <div class="detail-item detail-item--full">
              <div class="detail-label">{{ t("dynamicApiLog.table.error") }}</div>
              <div class="detail-value detail-pre">{{ row.errorMsg || "-" }}</div>
            </div>
            <div class="detail-item detail-item--full">
              <div class="detail-label">{{ t("dynamicApiLog.table.params") }}</div>
              <div class="detail-value detail-pre">{{ row.requestParam || "-" }}</div>
            </div>
            <div v-if="row.errorDetails" class="detail-item detail-item--full">
              <div class="detail-label">{{ t("dynamicApiLog.table.errorDetails") }}</div>
              <div class="detail-value detail-pre">{{ row.errorDetails }}</div>
            </div>
            <div v-if="row.meta" class="detail-item detail-item--full">
              <div class="detail-label">{{ t("dynamicApiLog.table.meta") }}</div>
              <div class="detail-value detail-pre">{{ row.meta }}</div>
            </div>
            <div class="detail-item detail-item--full">
              <div class="detail-label">{{ t("dynamicApiLog.table.trace") }}</div>
              <div class="detail-value">{{ row.traceId || "-" }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApiLog.table.time')" width="160">
        <template #default="{row}">
          {{ formatDateTime(row.requestTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApiLog.table.api')" min-width="200" prop="apiPath" show-overflow-tooltip/>
      <el-table-column :label="t('dynamicApiLog.table.method')" prop="apiMethod" width="80"/>
      <el-table-column :label="t('dynamicApiLog.table.status')" width="80">
        <template #default="{row}">
          <span :class="row.status === 1 ? 'tag-success' : 'tag-danger'">
            {{ row.status === 1 ? t('dynamicApiLog.status.success') : t('dynamicApiLog.status.fail') }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApiLog.table.action')" width="110">
        <template #default="{row}">
          <el-button v-permission="'dynamic-api-log:delete'" size="small" text type="danger" @click="removeLog(row)">
            {{ t("common.delete") }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="module-footer">
      <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  deleteDynamicApiLog,
  type DynamicApiLog,
  type DynamicApiLogQuery,
  listDynamicApiLogs
} from "../../api/extension";

const {t} = useI18n();
const loading = ref(false);
const logs = ref<DynamicApiLog[]>([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const filters = reactive({
  apiPath: "",
  apiMethod: "",
  userName: "",
  status: undefined as number | undefined,
  timeRange: [] as string[]
});

function buildQuery(): DynamicApiLogQuery {
  const [beginTime, endTime] = filters.timeRange || [];
  return {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    apiPath: filters.apiPath || undefined,
    apiMethod: filters.apiMethod || undefined,
    userName: filters.userName || undefined,
    status: filters.status,
    beginTime,
    endTime
  };
}

async function loadLogs() {
  if (loading.value) {
    return;
  }
  loading.value = true;
  try {
    const result = await listDynamicApiLogs(buildQuery());
    if (result?.code === 200 && result.data) {
      logs.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("dynamicApiLog.msg.loadFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("dynamicApiLog.msg.loadFailed"));
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  loadLogs();
}

function handleReset() {
  filters.apiPath = "";
  filters.apiMethod = "";
  filters.userName = "";
  filters.status = undefined;
  filters.timeRange = [];
  pageNum.value = 1;
  loadLogs();
}

function handlePageChange(value: number) {
  pageNum.value = value;
  loadLogs();
}

function handleSizeChange(value: number) {
  pageSize.value = value;
  pageNum.value = 1;
  loadLogs();
}

async function removeLog(row: DynamicApiLog) {
  try {
    await ElMessageBox.confirm(
        t("dynamicApiLog.msg.deleteConfirm", {id: row.id}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch (_error) {
    return;
  }
  try {
    const result = await deleteDynamicApiLog(row.id);
    if (result?.code === 200) {
      ElMessage.success(t("common.deleteSuccess"));
      loadLogs();
    } else {
      ElMessage.error(result?.message || t("common.deleteFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("common.deleteFailed"));
  }
}

function formatDateTime(value?: string) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const pad = (num: number) => String(num).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
      date.getHours()
  )}:${pad(date.getMinutes())}`;
}

onMounted(loadLogs);
</script>

<style scoped>
.tag-success {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #0f5132;
  background: rgba(25, 135, 84, 0.16);
}

.tag-danger {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #842029;
  background: rgba(220, 53, 69, 0.16);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  padding: 8px 4px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.detail-item--full {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px;
  color: var(--muted);
}

.detail-value {
  font-size: 13px;
  color: var(--el-text-color-primary, var(--ink));
  word-break: break-all;
}

.detail-pre {
  white-space: pre-wrap;
}

.module-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.filter-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  flex: 1 1 520px;
  min-width: 0;
}

.filter-input {
  width: 160px;
}

.filter-input--wide {
  width: 220px;
}

.filter-input--narrow {
  width: 120px;
}

.filter-date {
  width: 240px;
}

@media (max-width: 900px) {
  .module-actions {
    justify-content: flex-start;
  }

  .filter-input {
    width: 140px;
  }

  .filter-input--wide {
    width: 200px;
  }

  .filter-input--narrow {
    width: 110px;
  }

  .filter-date {
    width: 220px;
  }
}

@media (max-width: 640px) {
  .filter-input,
  .filter-input--wide,
  .filter-input--narrow,
  .filter-date {
    width: 100%;
  }
}
</style>
