<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("dynamicApi.title") }}</div>
        <div class="module-sub">{{ t("dynamicApi.subtitle") }}</div>
      </div>
      <div class="module-actions" @keyup.enter="handleSearch">
        <div class="filter-group">
          <el-input
              v-model.trim="filters.path"
              :placeholder="t('dynamicApi.filter.path')"
              class="filter-input filter-input--wide"
              clearable
          />
          <el-select
              v-model="filters.method"
              :placeholder="t('dynamicApi.filter.method')"
              class="filter-input filter-input--narrow"
              clearable
          >
            <el-option v-for="item in methodOptions" :key="item" :label="item" :value="item"/>
          </el-select>
          <el-select
              v-model="filters.status"
              :placeholder="t('dynamicApi.filter.status')"
              class="filter-input"
              clearable
          >
            <el-option :label="t('dynamicApi.status.draft')" :value="'DRAFT'"/>
            <el-option :label="t('dynamicApi.status.enabled')" :value="'ENABLED'"/>
            <el-option :label="t('dynamicApi.status.disabled')" :value="'DISABLED'"/>
          </el-select>
          <el-select
              v-model="filters.type"
              :placeholder="t('dynamicApi.filter.type')"
              class="filter-input filter-input--narrow"
              clearable
          >
            <el-option v-for="item in typeOptions" :key="item.code" :label="item.label" :value="item.code"/>
          </el-select>
          <el-select
              v-model="filters.authMode"
              :placeholder="t('dynamicApi.filter.authMode')"
              class="filter-input filter-input--narrow"
              clearable
          >
            <el-option :label="t('dynamicApi.auth.inherit')" :value="'INHERIT'"/>
            <el-option :label="t('dynamicApi.auth.public')" :value="'PUBLIC'"/>
          </el-select>
          <el-button @click="handleSearch">{{ t("common.search") }}</el-button>
          <el-button @click="handleReset">{{ t("dynamicApi.filter.reset") }}</el-button>
        </div>
        <div class="action-group">
          <el-button v-permission="'dynamic-api:create'" type="primary" @click="openCreate">
            {{ t("dynamicApi.filter.create") }}
          </el-button>
          <el-button v-permission="'dynamic-api:reload'" @click="handleReload">
            {{ t("dynamicApi.filter.reload") }}
          </el-button>
        </div>
      </div>
    </div>

    <el-table v-loading="loading" :data="apis" row-key="id" size="small" table-layout="auto">
      <el-table-column type="expand">
        <template #default="{row}">
          <div class="detail-grid">
            <div class="detail-item">
              <div class="detail-label">{{ t("dynamicApi.table.authMode") }}</div>
              <div class="detail-value">{{ row.authMode || "-" }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">{{ t("dynamicApi.table.rateLimit") }}</div>
              <div class="detail-value">{{ row.rateLimitPolicy || "-" }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">{{ t("dynamicApi.table.timeout") }}</div>
              <div class="detail-value">{{ row.timeoutMs != null ? `${row.timeoutMs}ms` : "-" }}</div>
            </div>
            <div class="detail-item detail-item--full">
              <div class="detail-label">{{ t("dynamicApi.table.config") }}</div>
              <div class="detail-value detail-pre">{{ row.config || "-" }}</div>
            </div>
            <div class="detail-item detail-item--full">
              <div class="detail-label">{{ t("dynamicApi.dialog.remark") }}</div>
              <div class="detail-value">{{ row.remark || "-" }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">{{ t("dynamicApi.table.updatedAt") }}</div>
              <div class="detail-value">{{ formatDateTime(row.updateTime || row.createTime) }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApi.table.path')" min-width="200" prop="path" show-overflow-tooltip/>
      <el-table-column :label="t('dynamicApi.table.method')" prop="method" width="80"/>
      <el-table-column :label="t('dynamicApi.table.type')" prop="type" width="80"/>
      <el-table-column :label="t('dynamicApi.table.status')" width="100">
        <template #default="{row}">
          <span :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApi.table.updatedAt')" width="150">
        <template #default="{row}">
          {{ formatDateTime(row.updateTime || row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('dynamicApi.table.action')" width="200">
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
        <el-form-item :error="fieldErrors.path" :label="t('dynamicApi.dialog.path')">
          <el-input v-model.trim="form.path" :placeholder="t('dynamicApi.dialog.pathPlaceholder')"/>
        </el-form-item>
        <el-form-item :error="fieldErrors.method" :label="t('dynamicApi.dialog.method')">
          <el-select v-model="form.method" :placeholder="t('dynamicApi.dialog.methodPlaceholder')">
            <el-option v-for="item in methodOptions" :key="item" :label="item" :value="item"/>
          </el-select>
        </el-form-item>
        <el-form-item :error="fieldErrors.type" :label="t('dynamicApi.dialog.type')">
          <el-select v-model="form.type" :placeholder="t('dynamicApi.dialog.typePlaceholder')">
            <el-option v-for="item in typeOptions" :key="item.code" :label="item.label" :value="item.code"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="isCustomType" :error="fieldErrors.config" :label="t('dynamicApi.dialog.config')"
                      class="full-row">
          <el-input
              v-model.trim="form.config"
              :placeholder="t('dynamicApi.dialog.configPlaceholder')"
              :rows="6"
              type="textarea"
          />
        </el-form-item>
        <el-form-item v-if="currentType === 'BEAN'" :error="fieldErrors.beanName"
                      :label="t('dynamicApi.dialog.beanName')">
          <el-select
              v-model="form.beanName"
              :placeholder="t('dynamicApi.dialog.beanNamePlaceholder')"
              filterable
              @change="handleBeanChange"
          >
            <el-option
                v-for="item in beanOptions"
                :key="item.beanName"
                :label="itemLabel(item)"
                :value="item.beanName"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="currentType === 'BEAN'" :error="fieldErrors.paramMode"
                      :label="t('dynamicApi.dialog.paramMode')">
          <el-select v-model="form.paramMode" :placeholder="t('dynamicApi.dialog.paramModePlaceholder')">
            <el-option :label="t('dynamicApi.paramMode.auto')" :value="'AUTO'"/>
            <el-option :label="t('dynamicApi.paramMode.query')" :value="'QUERY'"/>
            <el-option :label="t('dynamicApi.paramMode.bodyJson')" :value="'BODY_JSON'"/>
            <el-option :label="t('dynamicApi.paramMode.form')" :value="'FORM'"/>
            <el-option :label="t('dynamicApi.paramMode.multipart')" :value="'MULTIPART'"/>
          </el-select>
          <div class="form-hint">{{ t("dynamicApi.dialog.paramModeHint") }}</div>
        </el-form-item>
        <el-form-item v-if="currentType === 'BEAN'" :error="fieldErrors.paramSchema"
                      :label="t('dynamicApi.dialog.paramSchema')" class="full-row">
          <el-input
              v-model.trim="form.paramSchema"
              :placeholder="t('dynamicApi.dialog.paramSchemaPlaceholder')"
              :rows="4"
              type="textarea"
          />
        </el-form-item>
        <el-form-item v-if="currentType === 'SQL'" :error="fieldErrors.sql" :label="t('dynamicApi.dialog.sql')"
                      class="full-row">
          <el-input
              v-model.trim="form.sql"
              :placeholder="t('dynamicApi.dialog.sqlPlaceholder')"
              :rows="5"
              type="textarea"
          />
        </el-form-item>
        <el-form-item v-if="currentType === 'HTTP'" :error="fieldErrors.httpUrl"
                      :label="t('dynamicApi.dialog.httpUrl')">
          <el-input v-model.trim="form.httpUrl" :placeholder="t('dynamicApi.dialog.httpUrlPlaceholder')"/>
        </el-form-item>
        <el-form-item v-if="currentType === 'HTTP'" :error="fieldErrors.httpMethod"
                      :label="t('dynamicApi.dialog.httpMethod')">
          <el-select v-model="form.httpMethod" :placeholder="t('dynamicApi.dialog.httpMethodPlaceholder')">
            <el-option :label="t('dynamicApi.http.followRequest')" :value="''"/>
            <el-option v-for="item in methodOptions" :key="item" :label="item" :value="item"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="currentType === 'HTTP'" :label="t('dynamicApi.dialog.httpPassHeaders')">
          <el-switch v-model="form.httpPassHeaders"/>
        </el-form-item>
        <el-form-item v-if="currentType === 'HTTP'" :label="t('dynamicApi.dialog.httpPassQuery')">
          <el-switch v-model="form.httpPassQuery"/>
        </el-form-item>
        <el-form-item :error="fieldErrors.status" :label="t('dynamicApi.dialog.status')">
          <el-select v-model="form.status" :placeholder="t('dynamicApi.dialog.statusPlaceholder')">
            <el-option :label="t('dynamicApi.status.draft')" :value="'DRAFT'"/>
            <el-option :label="t('dynamicApi.status.enabled')" :value="'ENABLED'"/>
            <el-option :label="t('dynamicApi.status.disabled')" :value="'DISABLED'"/>
          </el-select>
        </el-form-item>
        <el-form-item :error="fieldErrors.authMode" :label="t('dynamicApi.dialog.authMode')">
          <el-select v-model="form.authMode" :placeholder="t('dynamicApi.dialog.authModePlaceholder')">
            <el-option :label="t('dynamicApi.auth.inherit')" :value="'INHERIT'"/>
            <el-option :label="t('dynamicApi.auth.public')" :value="'PUBLIC'"/>
          </el-select>
        </el-form-item>
        <el-form-item :error="fieldErrors.rateLimitPolicy" :label="t('dynamicApi.dialog.rateLimit')">
          <el-select
              v-model="form.rateLimitPolicy"
              :placeholder="t('dynamicApi.dialog.rateLimitPlaceholder')"
              allow-create
              clearable
              filterable
          >
            <el-option
                v-for="policy in rateLimitOptions"
                :key="policy.id"
                :label="policyLabel(policy)"
                :value="policy.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :error="fieldErrors.timeoutMs" :label="t('dynamicApi.dialog.timeout')">
          <el-input v-model.number="form.timeoutMs" :placeholder="t('dynamicApi.dialog.timeoutPlaceholder')"
                    type="number"/>
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
import {computed, onMounted, reactive, ref, watch} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  createDynamicApi,
  deleteDynamicApi,
  disableDynamicApi,
  type DynamicApi,
  type DynamicApiBeanMeta,
  type DynamicApiPayload,
  type DynamicApiTypeMeta,
  enableDynamicApi,
  listDynamicApiBeans,
  listDynamicApis,
  listDynamicApiTypes,
  listRateLimitPolicies,
  type RateLimitPolicyMeta,
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
const beanCatalog = ref<DynamicApiBeanMeta[]>([]);
const rateLimitPolicies = ref<RateLimitPolicyMeta[]>([]);
const typeCatalog = ref<DynamicApiTypeMeta[]>([]);
const beanLoading = ref(false);
const policyLoading = ref(false);
const typeLoading = ref(false);
const fieldErrors = reactive<Record<string, string>>({});

const methodOptions = ["GET", "POST", "PUT", "DELETE"];
const defaultTypeOptions: DynamicApiTypeMeta[] = [
  {code: "BEAN", name: "BEAN"},
  {code: "SQL", name: "SQL"},
  {code: "HTTP", name: "HTTP"}
];
const typeOptions = computed(() =>
    (typeCatalog.value && typeCatalog.value.length ? typeCatalog.value : defaultTypeOptions)
        .map((item) => ({
          code: item.code,
          label: item.name || item.code
        }))
);
const builtInTypes = new Set(defaultTypeOptions.map((item) => item.code));

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
  remark: "",
  beanName: "",
  paramMode: "AUTO",
  paramSchema: "",
  sql: "",
  httpUrl: "",
  httpMethod: "",
  httpPassHeaders: true,
  httpPassQuery: true
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("dynamicApi.dialog.createTitle") : t("dynamicApi.dialog.editTitle")
);

const beanOptions = computed(() => beanCatalog.value || []);
const rateLimitOptions = computed(() => rateLimitPolicies.value || []);
const currentType = computed(() => normalizeType(form.type));
const isCustomType = computed(() => !isBuiltInType(form.type));

function normalizeType(value?: string) {
  return (value || "").trim().toUpperCase();
}

function isBuiltInType(value?: string) {
  return builtInTypes.has(normalizeType(value));
}

function clearFieldErrors() {
  Object.keys(fieldErrors).forEach((key) => {
    delete fieldErrors[key];
  });
}

function extractErrorDetails(result: { data?: unknown } | undefined) {
  const data = result?.data;
  if (!data || typeof data !== "object" || Array.isArray(data)) {
    return null;
  }
  const details: Record<string, string> = {};
  Object.entries(data as Record<string, unknown>).forEach(([key, value]) => {
    if (value == null) {
      return;
    }
    details[key] = String(value);
  });
  return Object.keys(details).length ? details : null;
}

function applyFieldErrors(details: Record<string, string> | null) {
  clearFieldErrors();
  if (!details) {
    return;
  }
  const alias: Record<string, string> = {
    url: "httpUrl",
    method: "httpMethod",
    sql: "sql",
    beanName: "beanName",
    paramMode: "paramMode",
    paramSchema: "paramSchema",
    config: "config"
  };
  Object.entries(details).forEach(([key, value]) => {
    const target = alias[key] || key;
    fieldErrors[target] = value;
  });
}

function buildErrorSummary(details: Record<string, string> | null) {
  if (!details) {
    return "";
  }
  const messages = Object.values(details).filter((item) => item && item.trim());
  return messages.join("; ");
}

function itemLabel(item: DynamicApiBeanMeta) {
  if (!item.className) {
    return item.beanName;
  }
  const shortName = item.className.split(".").pop();
  return shortName ? `${item.beanName} (${shortName})` : item.beanName;
}

function policyLabel(policy: RateLimitPolicyMeta) {
  if (!policy) {
    return "";
  }
  const name = policy.name ? `${policy.name} (${policy.id})` : policy.id;
  if (policy.maxRequests && policy.windowSeconds) {
    return `${name} · ${policy.maxRequests}/${policy.windowSeconds}s`;
  }
  return name;
}

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

async function loadBeanCatalog() {
  if (beanLoading.value) {
    return;
  }
  beanLoading.value = true;
  try {
    const result = await listDynamicApiBeans();
    if (result?.code === 200 && Array.isArray(result.data)) {
      beanCatalog.value = result.data;
    } else {
      ElMessage.error(result?.message || t("dynamicApi.msg.beanMetaFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("dynamicApi.msg.beanMetaFailed"));
  } finally {
    beanLoading.value = false;
  }
}

async function loadRateLimitPolicies() {
  if (policyLoading.value) {
    return;
  }
  policyLoading.value = true;
  try {
    const result = await listRateLimitPolicies();
    if (result?.code === 200 && Array.isArray(result.data)) {
      rateLimitPolicies.value = result.data;
    } else {
      ElMessage.error(result?.message || t("dynamicApi.msg.policyLoadFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("dynamicApi.msg.policyLoadFailed"));
  } finally {
    policyLoading.value = false;
  }
}

async function loadTypeCatalog() {
  if (typeLoading.value) {
    return;
  }
  typeLoading.value = true;
  try {
    const result = await listDynamicApiTypes();
    if (result?.code === 200 && Array.isArray(result.data)) {
      typeCatalog.value = result.data;
    } else {
      ElMessage.error(result?.message || t("dynamicApi.msg.typeLoadFailed"));
    }
  } catch (_error) {
    ElMessage.error(t("dynamicApi.msg.typeLoadFailed"));
  } finally {
    typeLoading.value = false;
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
  clearFieldErrors();
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
  form.beanName = "";
  form.paramMode = "AUTO";
  form.paramSchema = "";
  form.sql = "";
  form.httpUrl = "";
  form.httpMethod = "";
  form.httpPassHeaders = true;
  form.httpPassQuery = true;
  editorVisible.value = true;
}

function openEdit(row: DynamicApi) {
  clearFieldErrors();
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
  form.beanName = "";
  form.paramMode = "AUTO";
  form.paramSchema = "";
  form.sql = "";
  form.httpUrl = "";
  form.httpMethod = "";
  form.httpPassHeaders = true;
  form.httpPassQuery = true;
  applyConfigFromRow(row);
  editorVisible.value = true;
}

async function saveApi() {
  clearFieldErrors();
  if (!form.path || !form.method || !form.type) {
    ElMessage.warning(t("dynamicApi.msg.validate"));
    return;
  }
  const normalizedType = normalizeType(form.type);
  if (normalizedType === "BEAN" && !form.beanName) {
    ElMessage.warning(t("dynamicApi.msg.validateBean"));
    return;
  }
  if (normalizedType === "SQL" && !form.sql) {
    ElMessage.warning(t("dynamicApi.msg.validateSql"));
    return;
  }
  if (normalizedType === "HTTP" && !form.httpUrl) {
    ElMessage.warning(t("dynamicApi.msg.validateHttp"));
    return;
  }
  if (!isBuiltInType(form.type) && !form.config) {
    ElMessage.warning(t("dynamicApi.msg.validateConfig"));
    return;
  }
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    const timeoutMs = Number.isFinite(form.timeoutMs as number) ? form.timeoutMs : undefined;
    const isCustom = !isBuiltInType(form.type);
    const payload: DynamicApiPayload = {
      path: form.path,
      method: form.method,
      type: form.type,
      config: isCustom ? form.config || undefined : undefined,
      status: form.status,
      authMode: form.authMode,
      rateLimitPolicy: form.rateLimitPolicy,
      timeoutMs,
      remark: form.remark,
      beanName: normalizedType === "BEAN" ? form.beanName : undefined,
      paramMode: normalizedType === "BEAN" ? form.paramMode : undefined,
      paramSchema: normalizedType === "BEAN" ? form.paramSchema : undefined,
      sql: normalizedType === "SQL" ? form.sql : undefined,
      httpUrl: normalizedType === "HTTP" ? form.httpUrl : undefined,
      httpMethod: normalizedType === "HTTP" ? form.httpMethod : undefined,
      httpPassHeaders: normalizedType === "HTTP" ? form.httpPassHeaders : undefined,
      httpPassQuery: normalizedType === "HTTP" ? form.httpPassQuery : undefined
    };
    const result = editorMode.value === "create"
        ? await createDynamicApi(payload)
        : await updateDynamicApi(currentId.value as number, payload);
    if (result?.code === 200) {
      ElMessage.success(t("common.saveSuccess"));
      editorVisible.value = false;
      loadApis();
    } else {
      const details = extractErrorDetails(result);
      applyFieldErrors(details);
      const summary = buildErrorSummary(details);
      const message = result?.message || t("common.saveFailed");
      ElMessage.error(summary ? `${message}: ${summary}` : message);
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

function handleBeanChange() {
  form.paramSchema = "";
}

function applyConfigFromRow(row: DynamicApi) {
  if (!row) {
    return;
  }
  form.config = row.config || "";
  if (!row.config) {
    return;
  }
  try {
    const config = JSON.parse(row.config);
    const normalizedType = normalizeType(row.type);
    if (normalizedType === "BEAN") {
      form.beanName = config.beanName || "";
      form.paramMode = config.paramMode || "AUTO";
      form.paramSchema = config.paramSchema || "";
      return;
    }
    if (normalizedType === "SQL") {
      form.sql = config.sql || "";
      return;
    }
    if (normalizedType === "HTTP") {
      form.httpUrl = config.url || "";
      form.httpMethod = config.method || "";
      form.httpPassHeaders = config.passHeaders !== false;
      form.httpPassQuery = config.passQuery !== false;
    }
  } catch (_error) {
    // keep raw config
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

watch(
    () => form.type,
    (value, prev) => {
      if (value === prev) {
        return;
      }
      clearFieldErrors();
      const normalizedType = normalizeType(value);
      if (normalizedType === "BEAN") {
        form.config = "";
        form.sql = "";
        form.httpUrl = "";
        form.httpMethod = "";
        form.httpPassHeaders = true;
        form.httpPassQuery = true;
        form.paramMode = form.paramMode || "AUTO";
        form.paramSchema = form.paramSchema || "";
        loadBeanCatalog();
      }
      if (normalizedType === "SQL") {
        form.config = "";
        form.beanName = "";
        form.paramMode = "AUTO";
        form.paramSchema = "";
        form.httpUrl = "";
        form.httpMethod = "";
        form.httpPassHeaders = true;
        form.httpPassQuery = true;
      }
      if (normalizedType === "HTTP") {
        form.config = "";
        form.beanName = "";
        form.paramMode = "AUTO";
        form.paramSchema = "";
        form.sql = "";
        form.httpPassHeaders = form.httpPassHeaders ?? true;
        form.httpPassQuery = form.httpPassQuery ?? true;
      }
      if (!isBuiltInType(value)) {
        form.beanName = "";
        form.paramMode = "AUTO";
        form.paramSchema = "";
        form.sql = "";
        form.httpUrl = "";
        form.httpMethod = "";
        form.httpPassHeaders = true;
        form.httpPassQuery = true;
        form.config = form.config || "";
      }
    }
);

onMounted(() => {
  loadApis();
  loadTypeCatalog();
  loadBeanCatalog();
  loadRateLimitPolicies();
});
</script>

<style scoped>
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

.action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  flex: 0 0 auto;
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
}

@media (max-width: 640px) {
  .filter-input,
  .filter-input--wide,
  .filter-input--narrow {
    width: 100%;
  }

  .action-group {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>

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

.form-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
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
