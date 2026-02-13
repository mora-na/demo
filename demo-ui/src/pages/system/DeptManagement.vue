<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("dept.title") }}</div>
        <div class="module-sub">{{ t("dept.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-button type="primary" @click="openCreate">{{ t("dept.create") }}</el-button>
        <el-button v-if="selectedDeptIds.length" type="danger" @click="removeDepts">
          {{ t("dept.filter.delete") }}
        </el-button>
      </div>
    </div>

    <el-table
        v-loading="loading"
        :data="depts"
        row-key="id"
        size="small"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="46"/>
      <el-table-column :label="t('dept.table.name')" min-width="140" prop="name"/>
      <el-table-column :label="t('dept.table.code')" min-width="120" prop="code"/>
      <el-table-column :label="t('dept.table.parent')" min-width="120">
        <template #default="{row}">
          {{ parentName(row.parentId) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('dept.table.sort')" prop="sort" width="80"/>
      <el-table-column :label="t('dept.table.status')" width="100">
        <template #default="{row}">
          <el-switch
              :active-value="1"
              :inactive-value="0"
              :model-value="row.status ?? 0"
              @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('dept.table.action')" width="180">
        <template #default="{row}">
          <el-button size="small" text @click="openEdit(row)">{{ t("dept.table.edit") }}</el-button>
          <el-button size="small" text type="danger" @click="removeDept(row)">
            {{ t("dept.table.delete") }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="620px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="16" class="form-grid">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dept.dialog.name')">
              <el-input v-model.trim="form.name"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dept.dialog.code')">
              <el-input v-model.trim="form.code"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dept.dialog.parent')">
              <el-tree-select
                  v-model="form.parentId"
                  :data="deptTree"
                  :props="{label: 'name', value: 'id', children: 'children'}"
                  :render-after-expand="false"
                  check-strictly
                  clearable
                  :placeholder="t('dept.dialog.parentPlaceholder')"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dept.dialog.sort')">
              <el-input-number v-model="form.sort" :min="0"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dept.dialog.status')">
              <el-select v-model="form.status" :placeholder="t('dept.dialog.statusPlaceholder')">
                <el-option :value="1" :label="t('dept.dialog.statusEnabled')"/>
                <el-option :value="0" :label="t('dept.dialog.statusDisabled')"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item :label="t('dept.dialog.remark')">
              <el-input v-model.trim="form.remark"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="saving" type="primary" @click="saveDept">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {buildTree, type TreeNode} from "../../utils/tree";
import {
  createDept,
  deleteDept,
  deleteDepts,
  type DeptVO,
  listDepts,
  updateDept,
  updateDeptStatus
} from "../../api/system";

const depts = ref<DeptVO[]>([]);
const deptTree = ref<TreeNode<DeptVO>[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorDeptId = ref<number | null>(null);
const selectedDeptIds = ref<number[]>([]);
const {t} = useI18n();

const form = reactive({
  name: "",
  code: "",
  parentId: undefined as number | undefined,
  status: 1,
  sort: 0,
  remark: ""
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("dept.dialog.createTitle") : t("dept.dialog.editTitle")
);

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

function handleSelectionChange(rows: DeptVO[]) {
  selectedDeptIds.value = rows.map((row) => row.id);
}

function parentName(parentId?: number | null) {
  if (!parentId) {
    return "-";
  }
  const match = depts.value.find((item) => item.id === parentId);
  return match?.name || String(parentId);
}

function resetFormState() {
  form.name = "";
  form.code = "";
  form.parentId = undefined;
  form.status = 1;
  form.sort = 0;
  form.remark = "";
}

async function fetchDepts() {
  loading.value = true;
  try {
    const result = await listDepts();
    if (result?.code === 200 && result.data) {
      depts.value = result.data;
      deptTree.value = buildTree(result.data);
    } else {
      ElMessage.error(result?.message || t("dept.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("dept.msg.loadFailed")));
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editorMode.value = "create";
  editorDeptId.value = null;
  resetFormState();
  editorVisible.value = true;
}

function openEdit(dept: DeptVO) {
  editorMode.value = "edit";
  editorDeptId.value = dept.id;
  resetFormState();
  form.name = dept.name;
  form.code = dept.code || "";
  form.parentId = dept.parentId ?? undefined;
  form.status = dept.status ?? 1;
  form.sort = dept.sort ?? 0;
  form.remark = dept.remark || "";
  editorVisible.value = true;
}

async function removeDept(dept: DeptVO) {
  try {
    await ElMessageBox.confirm(
        t("dept.msg.deleteConfirm", {name: dept.name}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteDept(dept.id);
  if (result?.code === 200) {
    ElMessage.success(t("dept.msg.deleteSuccess"));
    fetchDepts();
  } else {
    ElMessage.error(result?.message || t("dept.msg.deleteFailed"));
  }
}

async function removeDepts() {
  if (!selectedDeptIds.value.length) {
    ElMessage.warning(t("dept.msg.deleteEmpty"));
    return;
  }
  try {
    await ElMessageBox.confirm(
        t("dept.msg.batchDeleteConfirm", {count: selectedDeptIds.value.length}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteDepts(selectedDeptIds.value);
  if (result?.code === 200) {
    ElMessage.success(t("dept.msg.deleteSuccess"));
    fetchDepts();
  } else {
    ElMessage.error(result?.message || t("dept.msg.deleteFailed"));
  }
}

async function saveDept() {
  if (!form.name) {
    ElMessage.warning(t("dept.msg.validateName"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createDept(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("dept.msg.createSuccess"));
        editorVisible.value = false;
        fetchDepts();
      } else {
        ElMessage.error(result?.message || t("dept.msg.createFailed"));
      }
    } else if (editorDeptId.value != null) {
      const result = await updateDept(editorDeptId.value, form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("dept.msg.updateSuccess"));
        editorVisible.value = false;
        fetchDepts();
      } else {
        ElMessage.error(result?.message || t("dept.msg.updateFailed"));
      }
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("dept.msg.saveFailed")));
  } finally {
    saving.value = false;
  }
}

async function handleStatusChange(dept: DeptVO, value: number) {
  const previous = dept.status ?? 0;
  dept.status = value;
  try {
    const result = await updateDeptStatus(dept.id, value);
    if (result?.code !== 200) {
      dept.status = previous;
      ElMessage.error(result?.message || t("dept.msg.statusUpdateFailed"));
    }
  } catch (error) {
    dept.status = previous;
    ElMessage.error(getErrorMessage(error, t("dept.msg.statusUpdateFailed")));
  }
}

onMounted(fetchDepts);
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
  gap: 8px;
  align-items: center;
}

.form-grid :deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>
