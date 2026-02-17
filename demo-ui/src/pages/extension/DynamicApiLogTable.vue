<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("dynamicApiLog.title") }}</div>
        <div class="module-sub">{{ t("dynamicApiLog.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-input v-model.trim="filters.apiPath" :placeholder="t('dynamicApiLog.filter.apiPath')" clearable
                  size="small"/>
        <el-input v-model.trim="filters.apiMethod" :placeholder="t('dynamicApiLog.filter.apiMethod')" clearable
                  size="small" style="width: 120px"/>
        <el-input v-model.trim="filters.userName" :placeholder="t('dynamicApiLog.filter.user')" clearable size="small"/>
        <el-select v-model="filters.status" :placeholder="t('dynamicApiLog.filter.status')" clearable size="small"
                   style="width: 110px">
          <el-option :label="t('dynamicApiLog.status.success')" :value="1"/>
          <el-option :label="t('dynamicApiLog.status.fail')" :value="0"/>
        </el-select>
        <el-date-picker
            v-model="filters.timeRange"
            :end-placeholder="t('dynamicApiLog.filter.end')"
            :start-placeholder="t('dynamicApiLog.filter.begin')"
            clearable
            range-separator="-"
            size="small"
            style="width: 260px"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
        <el-button size="small" @click="handleSearch">{{ t("common.search") }}</el-button>
        <el-button size="small" @click="handleReset">{{ t("dynamicApiLog.filter.reset") }}</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="logs" row-key="id" size="small">
      <el-table-column :label="t('dynamicApiLog.table.time')" width="170">
        <template #default="{row}">
          {{ formatDateTime(row.requestTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApiLog.table.api')" min-width="180" prop="apiPath" show-overflow-tooltip/>
      <el-table-column :label="t('dynamicApiLog.table.method')" prop="apiMethod" width="90"/>
      <el-table-column :label="t('dynamicApiLog.table.status')" width="90">
        <template #default="{row}">
          <span :class="row.status === 1 ? 'tag-success' : 'tag-danger'">
            {{ row.status === 1 ? t('dynamicApiLog.status.success') : t('dynamicApiLog.status.fail') }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApiLog.table.code')" prop="responseCode" width="90"/>
      <el-table-column :label="t('dynamicApiLog.table.duration')" width="110">
        <template #default="{row}">
          {{ row.durationMs != null ? `${row.durationMs}ms` : '-' }}
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApiLog.table.user')" prop="userName" show-overflow-tooltip width="120"/>
      <el-table-column :label="t('dynamicApiLog.table.ip')" prop="requestIp" show-overflow-tooltip width="130"/>
      <el-table-column :label="t('dynamicApiLog.table.error')" min-width="160" prop="errorMsg" show-overflow-tooltip/>
      <el-table-column :label="t('dynamicApiLog.table.action')" width="120">
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
</style>
