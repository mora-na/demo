<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("operLog.title") }}</div>
        <div class="module-sub">{{ t("operLog.subtitle") }}</div>
      </div>
      <div class="module-actions" @keyup.enter="handleSearch">
        <el-input v-model.trim="filters.userName" clearable size="small" :placeholder="t('operLog.filter.user')"/>
        <el-input v-model.trim="filters.title" clearable size="small" :placeholder="t('operLog.filter.title')"/>
        <el-select v-model="filters.businessType" clearable size="small" :placeholder="t('operLog.filter.type')" style="width: 130px">
          <el-option v-for="item in businessTypeOptions" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
        <el-select v-model="filters.status" clearable size="small" :placeholder="t('operLog.filter.status')" style="width: 110px">
          <el-option :value="1" :label="t('operLog.status.success')"/>
          <el-option :value="0" :label="t('operLog.status.fail')"/>
        </el-select>
        <el-date-picker
            v-model="filters.timeRange"
            clearable
            size="small"
            type="datetimerange"
            range-separator="-"
            :start-placeholder="t('operLog.filter.begin')"
            :end-placeholder="t('operLog.filter.end')"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 260px"
        />
        <el-button size="small" @click="handleSearch">{{ t("common.search") }}</el-button>
        <el-button size="small" @click="handleReset">{{ t("operLog.filter.reset") }}</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="logs" row-key="id" size="small">
      <el-table-column :label="t('operLog.table.time')" width="170">
        <template #default="{row}">
          {{ formatDateTime(row.operTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('operLog.table.user')" prop="userName" width="120" show-overflow-tooltip/>
      <el-table-column :label="t('operLog.table.title')" prop="title" min-width="140" show-overflow-tooltip/>
      <el-table-column :label="t('operLog.table.operation')" prop="operation" min-width="180" show-overflow-tooltip/>
      <el-table-column :label="t('operLog.table.type')" width="110">
        <template #default="{row}">
          {{ businessTypeLabel(row.businessType) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('operLog.table.method')" width="90">
        <template #default="{row}">
          {{ row.requestMethod || "-" }}
        </template>
      </el-table-column>
      <el-table-column :label="t('operLog.table.ip')" prop="operIp" width="130" show-overflow-tooltip/>
      <el-table-column :label="t('operLog.table.status')" width="90">
        <template #default="{row}">
          <span :class="row.status === 1 ? 'tag-success' : 'tag-danger'">
            {{ row.status === 1 ? t("operLog.status.success") : t("operLog.status.fail") }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('operLog.table.cost')" width="110">
        <template #default="{row}">
          {{ row.costTime != null ? `${row.costTime}ms` : "-" }}
        </template>
      </el-table-column>
      <el-table-column :label="t('operLog.table.error')" min-width="160" show-overflow-tooltip>
        <template #default="{row}">
          {{ row.errorMsg || "-" }}
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
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {listOperLogs, type OperLog, type OperLogQuery} from "../../api/system";

const {t} = useI18n();
const loading = ref(false);
const logs = ref<OperLog[]>([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const filters = reactive({
  userName: "",
  title: "",
  status: undefined as number | undefined,
  businessType: undefined as number | undefined,
  timeRange: [] as string[]
});

const businessTypeOptions = [
  {value: 0, label: t("operLog.type.other")},
  {value: 1, label: t("operLog.type.insert")},
  {value: 2, label: t("operLog.type.update")},
  {value: 3, label: t("operLog.type.delete")},
  {value: 4, label: t("operLog.type.grant")},
  {value: 5, label: t("operLog.type.export")},
  {value: 6, label: t("operLog.type.import")},
  {value: 7, label: t("operLog.type.forceLogout")},
  {value: 8, label: t("operLog.type.clean")}
];

function businessTypeLabel(value?: number) {
  const match = businessTypeOptions.find((item) => item.value === value);
  return match ? match.label : t("operLog.type.other");
}

function buildQuery(): OperLogQuery {
  const [beginTime, endTime] = filters.timeRange || [];
  return {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    userName: filters.userName || undefined,
    title: filters.title || undefined,
    status: filters.status,
    businessType: filters.businessType,
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
    const result = await listOperLogs(buildQuery());
    if (result?.code === 200 && result.data) {
      logs.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("operLog.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("operLog.msg.loadFailed"));
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  loadLogs();
}

function handleReset() {
  filters.userName = "";
  filters.title = "";
  filters.status = undefined;
  filters.businessType = undefined;
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
.system-module {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.module-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.module-title {
  font-size: 16px;
  font-weight: 600;
}

.module-sub {
  font-size: 12px;
  color: var(--muted);
}

.module-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  min-width: 0;
  max-width: 100%;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
}

.tag-success {
  color: #1a7f37;
}

.tag-danger {
  color: #d1242f;
}
</style>
