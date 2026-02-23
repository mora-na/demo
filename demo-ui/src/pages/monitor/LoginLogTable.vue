<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("loginLog.title") }}</div>
        <div class="module-sub">{{ t("loginLog.subtitle") }}</div>
      </div>
      <div class="module-actions" @keyup.enter="handleSearch">
        <el-input v-model.trim="filters.userName" clearable size="small" :placeholder="t('loginLog.filter.user')"/>
        <el-input v-model.trim="filters.loginIp" clearable size="small" :placeholder="t('loginLog.filter.ip')"/>
        <el-select v-model="filters.loginType" clearable size="small" :placeholder="t('loginLog.filter.type')" style="width: 120px">
          <el-option :value="1" :label="t('loginLog.type.login')"/>
          <el-option :value="2" :label="t('loginLog.type.logout')"/>
        </el-select>
        <el-select v-model="filters.status" clearable size="small" :placeholder="t('loginLog.filter.status')" style="width: 110px">
          <el-option :value="1" :label="t('loginLog.status.success')"/>
          <el-option :value="0" :label="t('loginLog.status.fail')"/>
        </el-select>
        <el-date-picker
            v-model="filters.timeRange"
            clearable
            size="small"
            type="datetimerange"
            range-separator="-"
            :start-placeholder="t('loginLog.filter.begin')"
            :end-placeholder="t('loginLog.filter.end')"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 260px"
        />
        <el-button size="small" @click="handleSearch">{{ t("common.search") }}</el-button>
        <el-button size="small" @click="handleReset">{{ t("loginLog.filter.reset") }}</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="logs" row-key="id" size="small">
      <el-table-column :label="t('loginLog.table.time')" width="170">
        <template #default="{row}">
          {{ formatDateTime(row.loginTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('loginLog.table.user')" prop="userName" width="140" show-overflow-tooltip/>
      <el-table-column :label="t('loginLog.table.type')" width="90">
        <template #default="{row}">
          {{ row.loginType === 2 ? t("loginLog.type.logout") : t("loginLog.type.login") }}
        </template>
      </el-table-column>
      <el-table-column :label="t('loginLog.table.status')" width="90">
        <template #default="{row}">
          <span :class="row.status === 1 ? 'tag-success' : 'tag-danger'">
            {{ row.status === 1 ? t("loginLog.status.success") : t("loginLog.status.fail") }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('loginLog.table.ip')" prop="loginIp" width="130" show-overflow-tooltip/>
      <el-table-column :label="t('loginLog.table.location')" prop="loginLocation" width="120" show-overflow-tooltip/>
      <el-table-column :label="t('loginLog.table.browser')" prop="browser" width="120" show-overflow-tooltip/>
      <el-table-column :label="t('loginLog.table.os')" prop="os" width="110" show-overflow-tooltip/>
      <el-table-column :label="t('loginLog.table.device')" prop="deviceType" width="90" show-overflow-tooltip/>
      <el-table-column :label="t('loginLog.table.msg')" min-width="160" show-overflow-tooltip>
        <template #default="{row}">
          {{ row.msg || "-" }}
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
import {listLoginLogs, type LoginLog, type LoginLogQuery} from "../../api/system";

const {t} = useI18n();
const loading = ref(false);
const logs = ref<LoginLog[]>([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const filters = reactive({
  userName: "",
  loginIp: "",
  status: undefined as number | undefined,
  loginType: undefined as number | undefined,
  timeRange: [] as string[]
});

function buildQuery(): LoginLogQuery {
  const [beginTime, endTime] = filters.timeRange || [];
  return {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    userName: filters.userName || undefined,
    loginIp: filters.loginIp || undefined,
    status: filters.status,
    loginType: filters.loginType,
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
    const result = await listLoginLogs(buildQuery());
    if (result?.code === 200 && result.data) {
      logs.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("loginLog.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("loginLog.msg.loadFailed"));
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
  filters.loginIp = "";
  filters.status = undefined;
  filters.loginType = undefined;
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
