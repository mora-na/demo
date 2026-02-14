<template>
  <div class="mapping-panel">
    <div class="mapping-head">
      <div class="filters">
        <el-input v-model.trim="filters.scopeKey" clearable :placeholder="t('dataScope.mapping.scopeKey')"/>
        <el-input v-model.trim="filters.tableName" clearable :placeholder="t('dataScope.mapping.tableName')"/>
        <el-button v-permission="'data-scope:rule:query'" @click="handleSearch">{{ t("common.search") }}</el-button>
      </div>
      <el-button v-permission="'data-scope:rule:create'" type="primary" @click="openCreate">{{ t("dataScope.mapping.create") }}</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" size="small">
      <el-table-column prop="scopeKey" :label="t('dataScope.mapping.scopeKey')" min-width="180"/>
      <el-table-column prop="tableName" :label="t('dataScope.mapping.tableName')" min-width="140"/>
      <el-table-column prop="tableAlias" :label="t('dataScope.mapping.tableAlias')" min-width="80"/>
      <el-table-column prop="deptColumn" :label="t('dataScope.mapping.deptColumn')" min-width="120"/>
      <el-table-column prop="userColumn" :label="t('dataScope.mapping.userColumn')" min-width="120"/>
      <el-table-column prop="status" :label="t('dataScope.mapping.status')" width="90">
        <template #default="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? t("common.enabled") : t("common.disabled") }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('dataScope.mapping.action')" width="160">
        <template #default="{row}">
          <div class="action-buttons">
            <el-button v-permission="'data-scope:rule:update'" size="small" text @click="openEdit(row)">{{ t("common.edit") }}</el-button>
            <el-button v-permission="'data-scope:rule:delete'" size="small" text type="danger" @click="removeRow(row)">{{ t("common.delete") }}</el-button>
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

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="560px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.mapping.scopeKey')">
              <el-input v-model.trim="form.scopeKey"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.mapping.tableName')">
              <el-input v-model.trim="form.tableName"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.mapping.tableAlias')">
              <el-input v-model.trim="form.tableAlias"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.mapping.deptColumn')">
              <el-input v-model.trim="form.deptColumn"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.mapping.userColumn')">
              <el-input v-model.trim="form.userColumn"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.mapping.status')">
              <el-select v-model="form.status">
                <el-option :label="t('common.enabled')" :value="1"/>
                <el-option :label="t('common.disabled')" :value="0"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button v-permission="editorMode === 'create' ? 'data-scope:rule:create' : 'data-scope:rule:update'" :loading="saving" type="primary" @click="saveRule">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  createDataScopeRule,
  type DataScopeRuleCreatePayload,
  type DataScopeRuleUpdatePayload,
  type DataScopeRuleVO,
  deleteDataScopeRule,
  listDataScopeRules,
  updateDataScopeRule
} from "../../api/system";

const {t} = useI18n();
const rows = ref<DataScopeRuleVO[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorId = ref<number | null>(null);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const filters = reactive({scopeKey: "", tableName: ""});

const form = reactive<DataScopeRuleCreatePayload & DataScopeRuleUpdatePayload>({
  scopeKey: "",
  tableName: "",
  tableAlias: "",
  deptColumn: "",
  userColumn: "",
  status: 1
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("dataScope.mapping.create") : t("dataScope.mapping.edit")
);

async function fetchRows() {
  loading.value = true;
  try {
    const result = await listDataScopeRules({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      scopeKey: filters.scopeKey,
      tableName: filters.tableName
    });
    if (result?.code === 200 && result.data) {
      rows.value = result.data.data;
      total.value = result.data.total;
    } else {
      ElMessage.error(result?.message || t("dataScope.mapping.loadFailed"));
    }
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  fetchRows();
}

function resetForm() {
  form.scopeKey = "";
  form.tableName = "";
  form.tableAlias = "";
  form.deptColumn = "";
  form.userColumn = "";
  form.status = 1;
}

function openCreate() {
  editorMode.value = "create";
  editorId.value = null;
  resetForm();
  editorVisible.value = true;
}

function openEdit(row: DataScopeRuleVO) {
  editorMode.value = "edit";
  editorId.value = row.id;
  form.scopeKey = row.scopeKey;
  form.tableName = row.tableName;
  form.tableAlias = row.tableAlias || "";
  form.deptColumn = row.deptColumn || "";
  form.userColumn = row.userColumn || "";
  form.status = row.status ?? 1;
  editorVisible.value = true;
}

async function saveRule() {
  if (!form.scopeKey || !form.tableName) {
    ElMessage.warning(t("dataScope.mapping.validate"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createDataScopeRule(form);
      if (result?.code === 200) {
        ElMessage.success(t("common.saveSuccess"));
        editorVisible.value = false;
        fetchRows();
      } else {
        ElMessage.error(result?.message || t("common.saveFailed"));
      }
    } else if (editorId.value != null) {
      const payload: DataScopeRuleUpdatePayload = {
        scopeKey: form.scopeKey,
        tableName: form.tableName,
        tableAlias: form.tableAlias,
        deptColumn: form.deptColumn,
        userColumn: form.userColumn,
        status: form.status
      };
      const result = await updateDataScopeRule(editorId.value, payload);
      if (result?.code === 200) {
        ElMessage.success(t("common.saveSuccess"));
        editorVisible.value = false;
        fetchRows();
      } else {
        ElMessage.error(result?.message || t("common.saveFailed"));
      }
    }
  } finally {
    saving.value = false;
  }
}

async function removeRow(row: DataScopeRuleVO) {
  try {
    await ElMessageBox.confirm(
        t("dataScope.mapping.deleteConfirm", {key: row.scopeKey}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteDataScopeRule(row.id);
  if (result?.code === 200) {
    ElMessage.success(t("common.deleteSuccess"));
    fetchRows();
  } else {
    ElMessage.error(result?.message || t("common.deleteFailed"));
  }
}

function handlePageChange(page: number) {
  pageNum.value = page;
  fetchRows();
}

function handleSizeChange(size: number) {
  pageSize.value = size;
  fetchRows();
}

onMounted(() => {
  fetchRows();
});
</script>

<style scoped>
.mapping-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mapping-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons {
  display: flex;
  gap: 6px;
  align-items: center;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
