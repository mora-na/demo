<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("dict.title") }}</div>
        <div class="module-sub">{{ t("dict.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-button v-permission="'dict:cache:refresh'" size="small" @click="refreshCache">
          {{ t("dict.cacheRefresh") }}
        </el-button>
      </div>
    </div>

    <div class="dict-layout">
      <section class="dict-panel">
        <div class="panel-head">
          <div class="panel-title-row">
            <div class="panel-title">{{ t("dict.type.title") }}</div>
          </div>
          <div class="panel-actions" @keyup.enter="handleTypeSearch">
            <div class="filter-fields">
              <el-input v-model.trim="typeFilters.dictType" :placeholder="t('dict.type.filterType')" class="filter-input" clearable
                        size="small"/>
              <el-input v-model.trim="typeFilters.dictName" :placeholder="t('dict.type.filterName')" class="filter-input" clearable
                        size="small"/>
              <el-select v-model="typeFilters.status" :placeholder="t('dict.type.filterStatus')" clearable size="small"
                         style="width: 120px">
                <el-option :label="t('common.enabled')" :value="1"/>
                <el-option :label="t('common.disabled')" :value="0"/>
              </el-select>
            </div>
            <div class="filter-actions">
              <el-button size="small" @click="handleTypeSearch">{{ t("common.search") }}</el-button>
              <el-button v-permission="'dict:create'" size="small" type="primary" @click="openTypeCreate">
                {{ t("dict.type.create") }}
              </el-button>
            </div>
          </div>
        </div>

        <el-table
            v-loading="typeLoading"
            :data="typeRows"
            size="small"
            highlight-current-row
            row-key="id"
            @row-click="selectType"
        >
          <el-table-column prop="dictType" :label="t('dict.type.dictType')" min-width="140"/>
          <el-table-column prop="dictName" :label="t('dict.type.dictName')" min-width="140"/>
          <el-table-column prop="status" :label="t('dict.type.status')" width="80">
            <template #default="{row}">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? t("common.enabled") : t("common.disabled") }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sort" :label="t('dict.type.sort')" width="70"/>
          <el-table-column :label="t('dict.type.action')" width="160">
            <template #default="{row}">
              <div class="action-buttons">
                <el-button v-permission="'dict:update'" size="small" text @click.stop="openTypeEdit(row)">{{ t("common.edit") }}</el-button>
                <el-button v-permission="'dict:delete'" size="small" text type="danger" @click.stop="removeType(row)">
                  {{ t("common.delete") }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="module-footer">
          <el-pagination
              :current-page="typePageNum"
              :page-size="typePageSize"
              :total="typeTotal"
              layout="total, sizes, prev, pager, next"
              @current-change="handleTypePageChange"
              @size-change="handleTypeSizeChange"
          />
        </div>
      </section>

      <section class="dict-panel">
        <div class="panel-head">
          <div class="panel-title-row">
            <div class="panel-title">
              {{ t("dict.data.title") }}
              <span v-if="activeType" class="panel-sub">({{ activeType.dictType }} - {{ activeType.dictName }})</span>
            </div>
          </div>
          <div class="panel-actions" @keyup.enter="handleDataSearch">
            <div class="filter-fields">
              <el-input v-model.trim="dataFilters.dictLabel" :placeholder="t('dict.data.filterLabel')" class="filter-input" clearable
                        size="small"/>
              <el-input v-model.trim="dataFilters.dictValue" :placeholder="t('dict.data.filterValue')" class="filter-input" clearable
                        size="small"/>
              <el-select v-model="dataFilters.status" :placeholder="t('dict.data.filterStatus')" clearable size="small"
                         style="width: 120px">
                <el-option :label="t('common.enabled')" :value="1"/>
                <el-option :label="t('common.disabled')" :value="0"/>
              </el-select>
            </div>
            <div class="filter-actions">
              <el-button size="small" @click="handleDataSearch">{{ t("common.search") }}</el-button>
              <el-button v-permission="'dict:create'" size="small" type="primary" @click="openDataCreate">
                {{ t("dict.data.create") }}
              </el-button>
            </div>
          </div>
        </div>

        <el-table v-loading="dataLoading" :data="dataRows" size="small" row-key="id">
          <el-table-column prop="dictLabel" :label="t('dict.data.dictLabel')" min-width="140"/>
          <el-table-column prop="dictValue" :label="t('dict.data.dictValue')" min-width="140"/>
          <el-table-column prop="status" :label="t('dict.data.status')" width="80">
            <template #default="{row}">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? t("common.enabled") : t("common.disabled") }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sort" :label="t('dict.data.sort')" width="70"/>
          <el-table-column :label="t('dict.data.action')" width="160">
            <template #default="{row}">
              <div class="action-buttons">
                <el-button v-permission="'dict:update'" size="small" text @click="openDataEdit(row)">{{ t("common.edit") }}</el-button>
                <el-button v-permission="'dict:delete'" size="small" text type="danger" @click="removeData(row)">
                  {{ t("common.delete") }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="module-footer">
          <el-pagination
              :current-page="dataPageNum"
              :page-size="dataPageSize"
              :total="dataTotal"
              layout="total, sizes, prev, pager, next"
              @current-change="handleDataPageChange"
              @size-change="handleDataSizeChange"
          />
        </div>
      </section>
    </div>

    <el-dialog v-model="typeEditorVisible" :title="typeEditorTitle" align-center width="520px">
      <el-form :model="typeForm" label-position="top">
        <el-form-item :label="t('dict.type.dictType')">
          <el-input v-model.trim="typeForm.dictType"/>
        </el-form-item>
        <el-form-item :label="t('dict.type.dictName')">
          <el-input v-model.trim="typeForm.dictName"/>
        </el-form-item>
        <el-form-item :label="t('dict.type.status')">
          <el-select v-model="typeForm.status">
            <el-option :label="t('common.enabled')" :value="1"/>
            <el-option :label="t('common.disabled')" :value="0"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('dict.type.sort')">
          <el-input-number v-model="typeForm.sort" :min="0"/>
        </el-form-item>
        <el-form-item :label="t('dict.type.remark')">
          <el-input v-model.trim="typeForm.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeEditorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="typeSaving" type="primary" @click="saveType">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dataEditorVisible" :title="dataEditorTitle" align-center width="520px">
      <el-form :model="dataForm" label-position="top">
        <el-form-item :label="t('dict.data.dictLabel')">
          <el-input v-model.trim="dataForm.dictLabel"/>
        </el-form-item>
        <el-form-item :label="t('dict.data.dictValue')">
          <el-input v-model.trim="dataForm.dictValue"/>
        </el-form-item>
        <el-form-item :label="t('dict.data.status')">
          <el-select v-model="dataForm.status">
            <el-option :label="t('common.enabled')" :value="1"/>
            <el-option :label="t('common.disabled')" :value="0"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('dict.data.sort')">
          <el-input-number v-model="dataForm.sort" :min="0"/>
        </el-form-item>
        <el-form-item :label="t('dict.data.remark')">
          <el-input v-model.trim="dataForm.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataEditorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="dataSaving" type="primary" @click="saveData">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  createDictData,
  createDictType,
  deleteDictData,
  deleteDictType,
  type DictDataCreatePayload,
  type DictDataUpdatePayload,
  type DictDataVO,
  type DictTypeCreatePayload,
  type DictTypeUpdatePayload,
  type DictTypeVO,
  listDictData,
  listDictTypes,
  refreshDictCache,
  updateDictData,
  updateDictType
} from "../../api/system";
import {useDictStore} from "../../stores/dict";

const {t} = useI18n();
const dictStore = useDictStore();

const typeRows = ref<DictTypeVO[]>([]);
const dataRows = ref<DictDataVO[]>([]);
const typeLoading = ref(false);
const dataLoading = ref(false);
const typeSaving = ref(false);
const dataSaving = ref(false);
const typeEditorVisible = ref(false);
const dataEditorVisible = ref(false);
const typeEditorMode = ref<"create" | "edit">("create");
const dataEditorMode = ref<"create" | "edit">("create");
const typeEditorId = ref<number | null>(null);
const dataEditorId = ref<number | null>(null);
const activeType = ref<DictTypeVO | null>(null);
const typePageNum = ref(1);
const typePageSize = ref(10);
const typeTotal = ref(0);
const dataPageNum = ref(1);
const dataPageSize = ref(10);
const dataTotal = ref(0);

const typeFilters = reactive({
  dictType: "",
  dictName: "",
  status: undefined as number | undefined
});

const dataFilters = reactive({
  dictLabel: "",
  dictValue: "",
  status: undefined as number | undefined
});

const typeForm = reactive<DictTypeCreatePayload & DictTypeUpdatePayload>({
  dictType: "",
  dictName: "",
  status: 1,
  sort: 0,
  remark: ""
});

const dataForm = reactive<DictDataCreatePayload & DictDataUpdatePayload>({
  dictType: "",
  dictLabel: "",
  dictValue: "",
  status: 1,
  sort: 0,
  remark: ""
});

const typeEditorTitle = computed(() =>
    typeEditorMode.value === "create" ? t("dict.type.createTitle") : t("dict.type.editTitle")
);

const dataEditorTitle = computed(() =>
    dataEditorMode.value === "create" ? t("dict.data.createTitle") : t("dict.data.editTitle")
);

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

async function fetchTypes() {
  typeLoading.value = true;
  try {
    const result = await listDictTypes({
      pageNum: typePageNum.value,
      pageSize: typePageSize.value,
      dictType: typeFilters.dictType || undefined,
      dictName: typeFilters.dictName || undefined,
      status: typeFilters.status
    });
    if (result?.code === 200 && result.data) {
      typeRows.value = result.data.data || [];
      typeTotal.value = result.data.total || 0;
      if (activeType.value) {
        const updated = typeRows.value.find((item) => item.id === activeType.value?.id);
        if (updated) {
          activeType.value = updated;
        }
      } else if (typeRows.value.length) {
        activeType.value = typeRows.value[0];
        dataPageNum.value = 1;
        await fetchData();
      }
    } else {
      ElMessage.error(result?.message || t("dict.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("dict.msg.loadFailed")));
  } finally {
    typeLoading.value = false;
  }
}

async function fetchData() {
  if (!activeType.value) {
    dataRows.value = [];
    dataTotal.value = 0;
    return;
  }
  dataLoading.value = true;
  try {
    const result = await listDictData({
      pageNum: dataPageNum.value,
      pageSize: dataPageSize.value,
      dictType: activeType.value.dictType,
      dictLabel: dataFilters.dictLabel || undefined,
      dictValue: dataFilters.dictValue || undefined,
      status: dataFilters.status
    });
    if (result?.code === 200 && result.data) {
      dataRows.value = result.data.data || [];
      dataTotal.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("dict.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("dict.msg.loadFailed")));
  } finally {
    dataLoading.value = false;
  }
}

function selectType(row: DictTypeVO) {
  activeType.value = row;
  dataPageNum.value = 1;
  fetchData();
}

function handleTypeSearch() {
  typePageNum.value = 1;
  fetchTypes();
}

function handleDataSearch() {
  dataPageNum.value = 1;
  fetchData();
}

function handleTypePageChange(page: number) {
  typePageNum.value = page;
  fetchTypes();
}

function handleTypeSizeChange(size: number) {
  typePageSize.value = size;
  typePageNum.value = 1;
  fetchTypes();
}

function handleDataPageChange(page: number) {
  dataPageNum.value = page;
  fetchData();
}

function handleDataSizeChange(size: number) {
  dataPageSize.value = size;
  dataPageNum.value = 1;
  fetchData();
}

function resetTypeForm() {
  typeForm.dictType = "";
  typeForm.dictName = "";
  typeForm.status = 1;
  typeForm.sort = 0;
  typeForm.remark = "";
}

function resetDataForm() {
  dataForm.dictType = "";
  dataForm.dictLabel = "";
  dataForm.dictValue = "";
  dataForm.status = 1;
  dataForm.sort = 0;
  dataForm.remark = "";
}

function openTypeCreate() {
  typeEditorMode.value = "create";
  typeEditorId.value = null;
  resetTypeForm();
  typeEditorVisible.value = true;
}

function openTypeEdit(row: DictTypeVO) {
  typeEditorMode.value = "edit";
  typeEditorId.value = row.id;
  typeForm.dictType = row.dictType;
  typeForm.dictName = row.dictName;
  typeForm.status = row.status ?? 1;
  typeForm.sort = row.sort ?? 0;
  typeForm.remark = row.remark || "";
  typeEditorVisible.value = true;
}

async function saveType() {
  if (!typeForm.dictType || !typeForm.dictName) {
    ElMessage.warning(t("dict.msg.validateType"));
    return;
  }
  typeSaving.value = true;
  try {
    if (typeEditorMode.value === "create") {
      const result = await createDictType(typeForm);
      if (result?.code === 200) {
        ElMessage.success(t("common.saveSuccess"));
        typeEditorVisible.value = false;
        dictStore.clearCache();
        await fetchTypes();
      } else {
        ElMessage.error(result?.message || t("common.saveFailed"));
      }
    } else if (typeEditorId.value != null) {
      const result = await updateDictType(typeEditorId.value, typeForm);
      if (result?.code === 200) {
        ElMessage.success(t("common.saveSuccess"));
        typeEditorVisible.value = false;
        dictStore.clearCache();
        await fetchTypes();
        if (activeType.value) {
          activeType.value = {...activeType.value, dictType: typeForm.dictType, dictName: typeForm.dictName};
        }
        await fetchData();
      } else {
        ElMessage.error(result?.message || t("common.saveFailed"));
      }
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("common.saveFailed")));
  } finally {
    typeSaving.value = false;
  }
}

async function removeType(row: DictTypeVO) {
  try {
    await ElMessageBox.confirm(
        t("dict.msg.deleteTypeConfirm", {name: row.dictName}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  try {
    const result = await deleteDictType(row.id);
    if (result?.code === 200) {
      ElMessage.success(t("common.deleteSuccess"));
      if (activeType.value?.id === row.id) {
        activeType.value = null;
        dataRows.value = [];
      }
      await fetchTypes();
      await fetchData();
      dictStore.clearCache();
    } else {
      ElMessage.error(result?.message || t("common.deleteFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("common.deleteFailed")));
  }
}

function openDataCreate() {
  if (!activeType.value) {
    ElMessage.warning(t("dict.msg.selectType"));
    return;
  }
  dataEditorMode.value = "create";
  dataEditorId.value = null;
  resetDataForm();
  dataForm.dictType = activeType.value.dictType;
  dataEditorVisible.value = true;
}

function openDataEdit(row: DictDataVO) {
  dataEditorMode.value = "edit";
  dataEditorId.value = row.id;
  dataForm.dictType = row.dictType;
  dataForm.dictLabel = row.dictLabel;
  dataForm.dictValue = row.dictValue;
  dataForm.status = row.status ?? 1;
  dataForm.sort = row.sort ?? 0;
  dataForm.remark = row.remark || "";
  dataEditorVisible.value = true;
}

async function saveData() {
  if (!dataForm.dictLabel || !dataForm.dictValue) {
    ElMessage.warning(t("dict.msg.validateData"));
    return;
  }
  dataSaving.value = true;
  try {
    if (dataEditorMode.value === "create") {
      const result = await createDictData(dataForm);
      if (result?.code === 200) {
        ElMessage.success(t("common.saveSuccess"));
        dataEditorVisible.value = false;
        dictStore.clearCache();
        await fetchData();
      } else {
        ElMessage.error(result?.message || t("common.saveFailed"));
      }
    } else if (dataEditorId.value != null) {
      const result = await updateDictData(dataEditorId.value, {
        dictLabel: dataForm.dictLabel,
        dictValue: dataForm.dictValue,
        status: dataForm.status,
        sort: dataForm.sort,
        remark: dataForm.remark
      });
      if (result?.code === 200) {
        ElMessage.success(t("common.saveSuccess"));
        dataEditorVisible.value = false;
        dictStore.clearCache();
        await fetchData();
      } else {
        ElMessage.error(result?.message || t("common.saveFailed"));
      }
    } else {
      ElMessage.warning(t("dict.msg.selectData"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("common.saveFailed")));
  } finally {
    dataSaving.value = false;
  }
}

async function removeData(row: DictDataVO) {
  try {
    await ElMessageBox.confirm(
        t("dict.msg.deleteDataConfirm", {name: row.dictLabel}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  try {
    const result = await deleteDictData(row.id);
    if (result?.code === 200) {
      ElMessage.success(t("common.deleteSuccess"));
      await fetchData();
      dictStore.clearCache();
    } else {
      ElMessage.error(result?.message || t("common.deleteFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("common.deleteFailed")));
  }
}

async function refreshCache() {
  try {
    const result = await refreshDictCache();
    if (result?.code === 200) {
      ElMessage.success(t("dict.msg.cacheRefreshed"));
      dictStore.clearCache();
    } else {
      ElMessage.error(result?.message || t("dict.msg.cacheRefreshFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("dict.msg.cacheRefreshFailed")));
  }
}

onMounted(() => {
  fetchTypes();
});
</script>

<style scoped>
.dict-layout {
  display: grid;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
  gap: 12px;
}

.dict-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border-radius: 18px;
  border: 1px solid rgba(18, 18, 18, 0.08);
  background: rgba(255, 255, 255, 0.7);
}

.panel-head {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.panel-title-row {
  display: flex;
  align-items: center;
  width: 100%;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
}

.panel-sub {
  margin-left: 6px;
  font-size: 12px;
  color: var(--muted);
}

.panel-actions {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  width: 100%;
  flex-wrap: nowrap;
}

.filter-fields {
  display: flex;
  gap: 8px;
  flex: 1;
  min-width: 0;
  flex-wrap: nowrap;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.filter-input {
  width: 140px;
}

.action-buttons {
  display: flex;
  gap: 6px;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .dict-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .panel-actions {
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .filter-fields {
    flex-wrap: wrap;
  }
}
</style>
