<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("dynamicApi.title") }}</div>
        <div class="module-sub">{{ t("dynamicApi.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-input v-model.trim="filters.path" :placeholder="t('dynamicApi.filter.path')" clearable/>
        <el-select v-model="filters.method" :placeholder="t('dynamicApi.filter.method')" clearable style="width: 120px">
          <el-option v-for="item in methodOptions" :key="item" :label="item" :value="item"/>
        </el-select>
        <el-select v-model="filters.status" :placeholder="t('dynamicApi.filter.status')" clearable style="width: 130px">
          <el-option :label="t('dynamicApi.status.draft')" :value="'DRAFT'"/>
          <el-option :label="t('dynamicApi.status.enabled')" :value="'ENABLED'"/>
          <el-option :label="t('dynamicApi.status.disabled')" :value="'DISABLED'"/>
        </el-select>
        <el-select v-model="filters.type" :placeholder="t('dynamicApi.filter.type')" clearable style="width: 120px">
          <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item"/>
        </el-select>
        <el-select v-model="filters.authMode" :placeholder="t('dynamicApi.filter.authMode')" clearable
                   style="width: 120px">
          <el-option :label="t('dynamicApi.auth.inherit')" :value="'INHERIT'"/>
          <el-option :label="t('dynamicApi.auth.public')" :value="'PUBLIC'"/>
        </el-select>
        <el-button @click="handleSearch">{{ t("common.search") }}</el-button>
        <el-button @click="handleReset">{{ t("dynamicApi.filter.reset") }}</el-button>
        <el-button v-permission="'dynamic-api:create'" type="primary" @click="openCreate">
          {{ t("dynamicApi.filter.create") }}
        </el-button>
        <el-button v-permission="'dynamic-api:reload'" @click="handleReload">
          {{ t("dynamicApi.filter.reload") }}
        </el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="apis" row-key="id" size="small">
      <el-table-column :label="t('dynamicApi.table.path')" min-width="180" prop="path" show-overflow-tooltip/>
      <el-table-column :label="t('dynamicApi.table.method')" prop="method" width="90"/>
      <el-table-column :label="t('dynamicApi.table.type')" prop="type" width="90"/>
      <el-table-column :label="t('dynamicApi.table.status')" width="110">
        <template #default="{row}">
          <span :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApi.table.authMode')" prop="authMode" width="120"/>
      <el-table-column :label="t('dynamicApi.table.rateLimit')" min-width="120" prop="rateLimitPolicy"
                       show-overflow-tooltip/>
      <el-table-column :label="t('dynamicApi.table.timeout')" width="110">
        <template #default="{row}">
          {{ row.timeoutMs != null ? `${row.timeoutMs}ms` : '-' }}
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApi.table.config')" min-width="160" prop="config" show-overflow-tooltip/>
      <el-table-column :label="t('dynamicApi.table.updatedAt')" width="170">
        <template #default="{row}">
          {{ formatDateTime(row.updateTime || row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApi.table.action')" width="260">
        <template #default="{row}">
          <div class="action-buttons">
            <el-button v-permission="'dynamic-api:update'" size="small" text @click="openEdit(row)">
              {{ t("common.edit") }}
            </el-button>
            <el-button
                v-permission="'dynamic-api:status'"
                size="small"
                text
                @click="toggleStatus(row)"
            >
              {{ row.status === 'ENABLED' ? t('dynamicApi.action.disable') : t('dynamicApi.action.enable') }}
            </el-button>
            <el-button v-permission="'dynamic-api:delete'" size="small" text type="danger" @click="removeApi(row)">
              {{ t("common.delete") }}
            </el-button>
          </div>
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

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="720px">
      <el-form :model="form" class="job-editor-form" label-position="top">
        <el-form-item :label="t('dynamicApi.dialog.path')">
          <el-input v-model.trim="form.path" :placeholder="t('dynamicApi.dialog.pathPlaceholder')"/>
        </el-form-item>
        <el-form-item :label="t('dynamicApi.dialog.method')">
          <el-select v-model="form.method" :placeholder="t('dynamicApi.dialog.methodPlaceholder')">
            <el-option v-for="item in methodOptions" :key="item" :label="item" :value="item"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('dynamicApi.dialog.type')">
          <el-select v-model="form.type" :placeholder="t('dynamicApi.dialog.typePlaceholder')">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('dynamicApi.dialog.status')">
          <el-select v-model="form.status" :placeholder="t('dynamicApi.dialog.statusPlaceholder')">
            <el-option :label="t('dynamicApi.status.draft')" :value="'DRAFT'"/>
            <el-option :label="t('dynamicApi.status.enabled')" :value="'ENABLED'"/>
            <el-option :label="t('dynamicApi.status.disabled')" :value="'DISABLED'"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('dynamicApi.dialog.authMode')">
          <el-select v-model="form.authMode" :placeholder="t('dynamicApi.dialog.authModePlaceholder')">
            <el-option :label="t('dynamicApi.auth.inherit')" :value="'INHERIT'"/>
            <el-option :label="t('dynamicApi.auth.public')" :value="'PUBLIC'"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('dynamicApi.dialog.rateLimit')">
          <el-input v-model.trim="form.rateLimitPolicy" :placeholder="t('dynamicApi.dialog.rateLimitPlaceholder')"/>
        </el-form-item>
        <el-form-item :label="t('dynamicApi.dialog.timeout')">
          <el-input v-model.number="form.timeoutMs" :placeholder="t('dynamicApi.dialog.timeoutPlaceholder')"
                    type="number"/>
        </el-form-item>
        <el-form-item :label="t('dynamicApi.dialog.config')" class="full-row">
          <el-input
              v-model.trim="form.config"
              :placeholder="t('dynamicApi.dialog.configPlaceholder')"
              :rows="5"
              type="textarea"
          />
        </el-form-item>
        <el-form-item :label="t('dynamicApi.dialog.remark')" class="full-row">
          <el-input v-model.trim="form.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button
            v-permission="editorMode === 'create' ? 'dynamic-api:create' : 'dynamic-api:update'"
            :loading="saving"
            type="primary"
            @click="saveApi"
        >{{ t("common.save") }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  createDynamicApi,
  deleteDynamicApi,
  disableDynamicApi,
  type DynamicApi,
  type DynamicApiPayload,
  enableDynamicApi,
  listDynamicApis,
  reloadDynamicApis,
  updateDynamicApi
} from "../../api/extension";

const {t} = useI18n();
const loading = ref(false);
const saving = ref(false);
const apis = ref<DynamicApi[]>([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const methodOptions = ["GET", "POST", "PUT", "DELETE"];
const typeOptions = ["BEAN", "SQL", "HTTP"];

const filters = reactive({
  path: "",
  method: "",
  status: "",
  type: "",
  authMode: ""
});

const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const currentId = ref<number | null>(null);
const form = reactive<DynamicApiPayload>({
  path: "",
  method: "GET",
  type: "BEAN",
  config: "",
  status: "DRAFT",
  authMode: "INHERIT",
  rateLimitPolicy: "",
  timeoutMs: undefined,
  remark: ""
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("dynamicApi.dialog.createTitle") : t("dynamicApi.dialog.editTitle")
);

function buildQuery() {
  return {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    path: filters.path || undefined,
    method: filters.method || undefined,
    status: filters.status || undefined,
    type: filters.type || undefined,
    authMode: filters.authMode || undefined
  };
}

async function loadApis() {
  if (loading.value) {
    return;
  }
  loading.value = true;
  try {
    const result = await listDynamicApis(buildQuery());
    if (result?.code === 200 && result.data) {
      apis.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("dynamicApi.msg.loadFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("dynamicApi.msg.loadFailed"));
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  loadApis();
}

function handleReset() {
  filters.path = "";
  filters.method = "";
  filters.status = "";
  filters.type = "";
  filters.authMode = "";
  pageNum.value = 1;
  loadApis();
}

function handlePageChange(value: number) {
  pageNum.value = value;
  loadApis();
}

function handleSizeChange(value: number) {
  pageSize.value = value;
  pageNum.value = 1;
  loadApis();
}

function openCreate() {
  editorMode.value = "create";
  currentId.value = null;
  form.path = "";
  form.method = "GET";
  form.type = "BEAN";
  form.config = "";
  form.status = "DRAFT";
  form.authMode = "INHERIT";
  form.rateLimitPolicy = "";
  form.timeoutMs = undefined;
  form.remark = "";
  editorVisible.value = true;
}

function openEdit(row: DynamicApi) {
  editorMode.value = "edit";
  currentId.value = row.id;
  form.path = row.path || "";
  form.method = row.method || "GET";
  form.type = row.type || "BEAN";
  form.config = row.config || "";
  form.status = row.status || "DRAFT";
  form.authMode = row.authMode || "INHERIT";
  form.rateLimitPolicy = row.rateLimitPolicy || "";
  form.timeoutMs = row.timeoutMs;
  form.remark = row.remark || "";
  editorVisible.value = true;
}

async function saveApi() {
  if (!form.path || !form.method || !form.type || !form.config) {
    ElMessage.warning(t("dynamicApi.msg.validate"));
    return;
  }
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    const timeoutMs = Number.isFinite(form.timeoutMs as number) ? form.timeoutMs : undefined;
    const payload: DynamicApiPayload = {
      path: form.path,
      method: form.method,
      type: form.type,
      config: form.config,
      status: form.status,
      authMode: form.authMode,
      rateLimitPolicy: form.rateLimitPolicy,
      timeoutMs,
      remark: form.remark
    };
    const result = editorMode.value === "create"
        ? await createDynamicApi(payload)
        : await updateDynamicApi(currentId.value as number, payload);
    if (result?.code === 200) {
      ElMessage.success(t("common.saveSuccess"));
      editorVisible.value = false;
      loadApis();
    } else {
      ElMessage.error(result?.message || t("common.saveFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("common.saveFailed"));
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(row: DynamicApi) {
  try {
    const action = row.status === "ENABLED" ? disableDynamicApi : enableDynamicApi;
    const result = await action(row.id);
    if (result?.code === 200) {
      ElMessage.success(
          row.status === "ENABLED" ? t("dynamicApi.msg.disableSuccess") : t("dynamicApi.msg.enableSuccess")
      );
      loadApis();
    } else {
      ElMessage.error(result?.message || t("dynamicApi.msg.statusFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("dynamicApi.msg.statusFailed"));
  }
}

async function removeApi(row: DynamicApi) {
  try {
    await ElMessageBox.confirm(
        t("dynamicApi.msg.deleteConfirm", {path: row.path || row.id}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch (_error) {
    return;
  }
  try {
    const result = await deleteDynamicApi(row.id);
    if (result?.code === 200) {
      ElMessage.success(t("common.deleteSuccess"));
      loadApis();
    } else {
      ElMessage.error(result?.message || t("common.deleteFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("common.deleteFailed"));
  }
}

async function handleReload() {
  try {
    const result = await reloadDynamicApis();
    if (result?.code === 200) {
      ElMessage.success(t("dynamicApi.msg.reloadSuccess"));
      loadApis();
    } else {
      ElMessage.error(result?.message || t("dynamicApi.msg.reloadFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("dynamicApi.msg.reloadFailed"));
  }
}

function statusLabel(status?: string) {
  switch (status) {
    case "ENABLED":
      return t("dynamicApi.status.enabled");
    case "DISABLED":
      return t("dynamicApi.status.disabled");
    case "DRAFT":
    default:
      return t("dynamicApi.status.draft");
  }
}

function statusClass(status?: string) {
  switch (status) {
    case "ENABLED":
      return "tag-success";
    case "DISABLED":
      return "tag-danger";
    default:
      return "tag-muted";
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

onMounted(loadApis);
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

.tag-muted {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #4b5563;
  background: rgba(148, 163, 184, 0.2);
}

.job-editor-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.job-editor-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.job-editor-form :deep(.full-row) {
  grid-column: 1 / -1;
}

.job-editor-form :deep(.el-select),
.job-editor-form :deep(.el-input) {
  width: 100%;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
  align-items: center;
}

.action-buttons :deep(.el-button) {
  padding: 0 6px;
  min-height: 22px;
  font-size: 12px;
  white-space: nowrap;
}
</style>
