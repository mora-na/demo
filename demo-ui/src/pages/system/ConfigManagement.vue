<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("config.title") }}</div>
        <div class="module-sub">{{ t("config.subtitle") }}</div>
      </div>
      <div class="module-actions" @keyup.enter="handleSearch">
        <el-input v-model.trim="filters.group" :placeholder="t('config.filter.group')" class="filter-input" clearable
                  size="small"/>
        <el-input v-model.trim="filters.key" :placeholder="t('config.filter.key')" class="filter-input" clearable
                  size="small"/>
        <el-select v-model="filters.type" :placeholder="t('config.filter.type')" class="filter-select-wide" clearable
                   size="small">
          <el-option v-for="option in typeOptions" :key="option.value" :label="option.label" :value="option.value"/>
        </el-select>
        <el-select v-model="filters.status" :placeholder="t('config.filter.status')" class="filter-select" clearable
                   size="small">
          <el-option :label="t('common.enabled')" :value="1"/>
          <el-option :label="t('common.disabled')" :value="0"/>
        </el-select>
        <el-select v-model="filters.hotUpdate" :placeholder="t('config.filter.hotUpdate')" class="filter-select-wide" clearable
                   size="small">
          <el-option :label="t('config.hotUpdate.enabled')" :value="1"/>
          <el-option :label="t('config.hotUpdate.disabled')" :value="0"/>
        </el-select>
        <el-select v-model="filters.sensitive" :placeholder="t('config.filter.sensitive')" class="filter-select-wide" clearable
                   size="small">
          <el-option :label="t('config.sensitive.yes')" :value="1"/>
          <el-option :label="t('config.sensitive.no')" :value="0"/>
        </el-select>
        <el-button size="small" @click="handleSearch">{{ t("common.search") }}</el-button>
        <el-button v-permission="'config:create'" size="small" type="primary" @click="openCreate">
          {{ t("config.actions.create") }}
        </el-button>
        <el-button v-permission="'config:cache:refresh'" size="small" @click="openRefresh">
          {{ t("config.actions.refresh") }}
        </el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="rows" row-key="id" size="small">
      <el-table-column :label="t('config.table.group')" min-width="80" prop="group"/>
      <el-table-column :label="t('config.table.key')" min-width="240" prop="key"/>
      <el-table-column :label="t('config.table.type')" prop="type" width="90">
        <template #default="{row}">
          <el-tag effect="plain" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('config.table.value')" min-width="200" prop="value" show-overflow-tooltip>
        <template #default="{row}">
          <span class="config-value">{{ formatValue(row.value) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('config.table.status')" prop="status" width="80">
        <template #default="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? t("common.enabled") : t("common.disabled") }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('config.table.hotUpdate')" prop="hotUpdate" width="90">
        <template #default="{row}">
          <el-tag :type="row.hotUpdate === 1 ? 'success' : 'info'">
            {{ row.hotUpdate === 1 ? t("config.hotUpdate.enabled") : t("config.hotUpdate.disabled") }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('config.table.sensitive')" prop="sensitive" width="80">
        <template #default="{row}">
          <el-tag :type="row.sensitive === 1 ? 'warning' : 'info'">
            {{ row.sensitive === 1 ? t("config.sensitive.yes") : t("config.sensitive.no") }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('config.table.version')" prop="configVersion" width="70"/>
      <el-table-column :label="t('config.table.updatedAt')" prop="updateTime" width="130">
        <template #default="{row}">
          {{ formatDateTime(row.updateTime || row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('config.table.action')" width="120">
        <template #default="{row}">
          <div class="action-buttons">
            <el-button v-permission="'config:update'" size="small" text @click="openEdit(row)">{{
                t("common.edit")
              }}
            </el-button>
            <el-button v-permission="'config:delete'" size="small" text type="danger" @click="removeConfig(row)">
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

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="620px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="16">
          <el-col :sm="12" :xs="24">
            <el-form-item :label="t('config.form.group')">
              <el-input v-model.trim="form.group" :disabled="editorMode === 'edit'"
                        :placeholder="t('config.form.groupPlaceholder')"/>
            </el-form-item>
          </el-col>
          <el-col :sm="12" :xs="24">
            <el-form-item :label="t('config.form.key')" required>
              <el-input v-model.trim="form.key" :disabled="editorMode === 'edit'"
                        :placeholder="t('config.form.keyPlaceholder')"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :sm="12" :xs="24">
            <el-form-item :label="t('config.form.type')">
              <el-select v-model="form.type" :placeholder="t('config.form.typePlaceholder')">
                <el-option v-for="option in typeOptions" :key="option.value" :label="option.label"
                           :value="option.value"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :sm="12" :xs="24">
            <el-form-item :label="t('config.form.status')">
              <el-select v-model="form.status">
                <el-option :label="t('common.enabled')" :value="1"/>
                <el-option :label="t('common.disabled')" :value="0"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :sm="12" :xs="24">
            <el-form-item :label="t('config.form.hotUpdate')">
              <el-switch v-model="form.hotUpdate" :active-value="1" :inactive-value="0"/>
            </el-form-item>
          </el-col>
          <el-col :sm="12" :xs="24">
            <el-form-item :label="t('config.form.sensitive')">
              <el-switch v-model="form.sensitive" :active-value="1" :inactive-value="0"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('config.form.value')" required>
          <el-select v-if="isBooleanType" v-model="form.value" :placeholder="t('config.form.valuePlaceholder')"
                     class="value-input">
            <el-option :label="t('config.boolean.true')" value="true"/>
            <el-option :label="t('config.boolean.false')" value="false"/>
          </el-select>
          <el-input
              v-else-if="isJsonType"
              v-model="form.value"
              :autosize="{minRows: 4, maxRows: 10}"
              :placeholder="valuePlaceholder"
              :rows="4"
              class="value-input"
              type="textarea"
          />
          <el-input
              v-else
              v-model="form.value"
              :placeholder="valuePlaceholder"
              :show-password="form.sensitive === 1"
              :type="valueInputType"
              class="value-input"
          />
          <div v-if="isEditingSensitive" class="form-hint">{{ t("config.form.sensitiveHint") }}</div>
        </el-form-item>
        <el-form-item v-if="isJsonType" :label="t('config.form.schema')">
          <el-input
              v-model.trim="form.schema"
              :placeholder="t('config.form.schemaPlaceholder')"
              :rows="3"
              type="textarea"
          />
          <div class="form-hint">{{ t("config.form.schemaHint") }}</div>
        </el-form-item>
        <el-form-item :label="t('config.form.remark')">
          <el-input v-model.trim="form.remark" :placeholder="t('config.form.remarkPlaceholder')"/>
        </el-form-item>
        <el-form-item v-if="editorMode === 'edit'" :label="t('config.form.version')">
          <el-input v-model="formVersion" disabled/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="saving" type="primary" @click="saveConfig">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="refreshVisible" :title="t('config.refresh.title')" align-center width="420px">
      <el-form :model="refreshForm" label-position="top">
        <el-form-item :label="t('config.refresh.group')">
          <el-input v-model.trim="refreshForm.group" :placeholder="t('config.refresh.groupPlaceholder')"/>
        </el-form-item>
        <el-form-item :label="t('config.refresh.key')">
          <el-input v-model.trim="refreshForm.key" :placeholder="t('config.refresh.keyPlaceholder')"/>
        </el-form-item>
        <div class="form-hint">{{ t("config.refresh.hint") }}</div>
      </el-form>
      <template #footer>
        <el-button @click="refreshVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="refreshing" type="primary" @click="submitRefresh">{{ t("common.confirm") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref, watch} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  type ConfigCreatePayload,
  type ConfigQuery,
  type ConfigValueType,
  type ConfigVO,
  createConfig,
  deleteConfig,
  listConfigs,
  refreshConfigCache,
  updateConfig
} from "../../api/system";

const {t} = useI18n();

const rows = ref<ConfigVO[]>([]);
const loading = ref(false);
const saving = ref(false);
const refreshing = ref(false);
const editorVisible = ref(false);
const refreshVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorId = ref<number | null>(null);
const formVersion = ref<string>("-");
const isEditingSensitive = ref(false);

const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const filters = reactive({
  group: "",
  key: "",
  type: "" as ConfigValueType | "",
  status: undefined as number | undefined,
  hotUpdate: undefined as number | undefined,
  sensitive: undefined as number | undefined
});

const form = reactive<ConfigCreatePayload & { type: ConfigValueType }>({
  group: "",
  key: "",
  value: "",
  type: "STRING",
  schema: "",
  status: 1,
  hotUpdate: 0,
  sensitive: 0,
  remark: ""
});

const refreshForm = reactive({
  group: "",
  key: ""
});

const typeOptions = computed(() => [
  {label: t("config.type.string"), value: "STRING"},
  {label: t("config.type.number"), value: "NUMBER"},
  {label: t("config.type.boolean"), value: "BOOLEAN"},
  {label: t("config.type.json"), value: "JSON"}
]);

const isJsonType = computed(() => form.type === "JSON");
const isBooleanType = computed(() => form.type === "BOOLEAN");
const valueInputType = computed(() => (form.sensitive === 1 ? "password" : "text"));

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("config.dialog.createTitle") : t("config.dialog.editTitle")
);

const valuePlaceholder = computed(() => {
  if (isEditingSensitive.value) {
    return t("config.form.valuePlaceholderSensitive");
  }
  if (isJsonType.value) {
    return t("config.form.valuePlaceholderJson");
  }
  return t("config.form.valuePlaceholder");
});

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
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

function typeLabel(type?: ConfigValueType) {
  switch (type) {
    case "NUMBER":
      return t("config.type.number");
    case "BOOLEAN":
      return t("config.type.boolean");
    case "JSON":
      return t("config.type.json");
    case "STRING":
    default:
      return t("config.type.string");
  }
}

function formatValue(value?: string) {
  if (!value) {
    return "-";
  }
  return value;
}

async function fetchConfigs() {
  loading.value = true;
  try {
    const params: ConfigQuery = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      group: filters.group || undefined,
      key: filters.key || undefined,
      type: filters.type || undefined,
      status: filters.status,
      hotUpdate: filters.hotUpdate,
      sensitive: filters.sensitive
    };
    const result = await listConfigs(params);
    if (result?.code === 200 && result.data) {
      rows.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("config.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("config.msg.loadFailed")));
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  form.group = "";
  form.key = "";
  form.value = "";
  form.type = "STRING";
  form.schema = "";
  form.status = 1;
  form.hotUpdate = 0;
  form.sensitive = 0;
  form.remark = "";
  formVersion.value = "-";
  isEditingSensitive.value = false;
}

function openCreate() {
  editorMode.value = "create";
  editorId.value = null;
  resetForm();
  editorVisible.value = true;
}

function openEdit(row: ConfigVO) {
  editorMode.value = "edit";
  editorId.value = row.id;
  form.group = row.group || "";
  form.key = row.key || "";
  form.type = (row.type || "STRING") as ConfigValueType;
  form.schema = row.schema || "";
  form.status = row.status ?? 1;
  form.hotUpdate = row.hotUpdate ?? 0;
  form.sensitive = row.sensitive ?? 0;
  form.remark = row.remark || "";
  formVersion.value = row.configVersion != null ? String(row.configVersion) : "-";
  isEditingSensitive.value = row.sensitive === 1;
  form.value = isEditingSensitive.value ? "" : row.value || "";
  editorVisible.value = true;
}

function handleSearch() {
  pageNum.value = 1;
  fetchConfigs();
}

function handlePageChange(value: number) {
  pageNum.value = value;
  fetchConfigs();
}

function handleSizeChange(value: number) {
  pageSize.value = value;
  pageNum.value = 1;
  fetchConfigs();
}

function openRefresh() {
  refreshForm.group = "";
  refreshForm.key = "";
  refreshVisible.value = true;
}

function validateJson(value: string): boolean {
  try {
    JSON.parse(value);
    return true;
  } catch (_error) {
    return false;
  }
}

function normalizeValue(): string | null {
  const raw = form.value?.trim();
  if (!raw) {
    ElMessage.warning(t("config.msg.valueRequired"));
    return null;
  }
  if (form.type === "NUMBER") {
    if (!Number.isFinite(Number(raw))) {
      ElMessage.warning(t("config.msg.valueInvalid"));
      return null;
    }
  }
  if (form.type === "BOOLEAN") {
    if (!["true", "false"].includes(raw.toLowerCase())) {
      ElMessage.warning(t("config.msg.valueInvalid"));
      return null;
    }
  }
  if (form.type === "JSON" && !validateJson(raw)) {
    ElMessage.warning(t("config.msg.valueInvalid"));
    return null;
  }
  return raw;
}

function normalizeSchema(): string | undefined | null {
  if (!isJsonType.value) {
    return undefined;
  }
  const raw = form.schema?.trim();
  if (!raw) {
    return undefined;
  }
  if (!validateJson(raw)) {
    ElMessage.warning(t("config.msg.schemaInvalid"));
    return null;
  }
  return raw;
}

async function saveConfig() {
  const key = form.key?.trim();
  if (!key) {
    ElMessage.warning(t("config.msg.keyRequired"));
    return;
  }
  const value = normalizeValue();
  if (value == null) {
    return;
  }
  const schema = normalizeSchema();
  if (schema === null) {
    return;
  }
  saving.value = true;
  try {
    const payload: ConfigCreatePayload = {
      key,
      group: form.group?.trim() || undefined,
      value,
      type: form.type,
      schema,
      status: form.status ?? 1,
      hotUpdate: form.hotUpdate ?? 0,
      sensitive: form.sensitive ?? 0,
      remark: form.remark?.trim() || undefined
    };
    if (editorMode.value === "create") {
      const result = await createConfig(payload);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("config.msg.createSuccess"));
        editorVisible.value = false;
        await fetchConfigs();
      } else {
        ElMessage.error(result?.message || t("config.msg.createFailed"));
      }
    } else if (editorId.value != null) {
      const result = await updateConfig(editorId.value, payload);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("config.msg.updateSuccess"));
        editorVisible.value = false;
        await fetchConfigs();
      } else {
        ElMessage.error(result?.message || t("config.msg.updateFailed"));
      }
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("common.saveFailed")));
  } finally {
    saving.value = false;
  }
}

async function removeConfig(row: ConfigVO) {
  try {
    await ElMessageBox.confirm(
        t("config.msg.deleteConfirm", {name: row.key}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch (_error) {
    return;
  }
  try {
    const result = await deleteConfig(row.id);
    if (result?.code === 200) {
      ElMessage.success(result?.message || t("config.msg.deleteSuccess"));
      await fetchConfigs();
    } else {
      ElMessage.error(result?.message || t("config.msg.deleteFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("config.msg.deleteFailed")));
  }
}

async function submitRefresh() {
  refreshing.value = true;
  try {
    const params = {
      group: refreshForm.group?.trim() || undefined,
      key: refreshForm.key?.trim() || undefined
    };
    const result = await refreshConfigCache(params);
    if (result?.code === 200) {
      ElMessage.success(result?.message || t("config.msg.refreshSuccess"));
      refreshVisible.value = false;
    } else {
      ElMessage.error(result?.message || t("config.msg.refreshFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("config.msg.refreshFailed")));
  } finally {
    refreshing.value = false;
  }
}

watch(
    () => form.type,
    (value) => {
      if (value !== "JSON") {
        form.schema = "";
      }
      if (value === "BOOLEAN" && !["true", "false"].includes(form.value?.toLowerCase?.() || "")) {
        form.value = "true";
      }
    }
);

onMounted(() => {
  fetchConfigs();
});
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
}

.filter-input {
  width: 150px;
}

.filter-select {
  width: 120px;
}

.filter-select-wide {
  width: 140px;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

.action-buttons {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}

.action-buttons :deep(.el-button) {
  padding: 0 6px;
}

.action-buttons :deep(.el-button + .el-button) {
  margin-left: 0;
}

.config-value {
  display: inline-block;
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.value-input {
  width: 100%;
}

.form-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--muted);
}
</style>
