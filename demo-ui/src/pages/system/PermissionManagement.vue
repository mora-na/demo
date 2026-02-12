<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("permission.title") }}</div>
        <div class="module-sub">{{ t("permission.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-button type="primary" @click="openCreate">{{ t("permission.create") }}</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="permissions" row-key="id" size="small">
      <el-table-column :label="t('permission.table.code')" min-width="140" prop="code"/>
      <el-table-column :label="t('permission.table.name')" min-width="160" prop="name"/>
      <el-table-column :label="t('permission.table.status')" width="100">
        <template #default="{row}">
          <el-switch
              :active-value="1"
              :inactive-value="0"
              :model-value="row.status ?? 0"
              @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('permission.table.action')" width="140">
        <template #default="{row}">
          <el-button size="small" text @click="openEdit(row)">{{ t("permission.table.edit") }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="480px">
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('permission.dialog.code')">
          <el-input v-model.trim="form.code"/>
        </el-form-item>
        <el-form-item :label="t('permission.dialog.name')">
          <el-input v-model.trim="form.name"/>
        </el-form-item>
        <el-form-item :label="t('permission.dialog.status')">
          <el-select v-model="form.status" :placeholder="t('permission.dialog.statusPlaceholder')">
            <el-option :value="1" :label="t('permission.dialog.statusEnabled')"/>
            <el-option :value="0" :label="t('permission.dialog.statusDisabled')"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="saving" type="primary" @click="savePermission">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  createPermission,
  listPermissions,
  type PermissionVO,
  updatePermission,
  updatePermissionStatus
} from "../../api/system";

const permissions = ref<PermissionVO[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorId = ref<number | null>(null);
const {t} = useI18n();

const form = reactive({
  code: "",
  name: "",
  status: 1
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("permission.dialog.createTitle") : t("permission.dialog.editTitle")
);

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

async function fetchPermissions() {
  loading.value = true;
  try {
    const result = await listPermissions();
    if (result?.code === 200 && result.data) {
      permissions.value = result.data;
    } else {
      ElMessage.error(result?.message || t("permission.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("permission.msg.loadFailed")));
  } finally {
    loading.value = false;
  }
}

function resetFormState() {
  form.code = "";
  form.name = "";
  form.status = 1;
}

function openCreate() {
  editorMode.value = "create";
  editorId.value = null;
  resetFormState();
  editorVisible.value = true;
}

function openEdit(permission: PermissionVO) {
  editorMode.value = "edit";
  editorId.value = permission.id;
  resetFormState();
  form.code = permission.code;
  form.name = permission.name;
  form.status = permission.status ?? 1;
  editorVisible.value = true;
}

async function savePermission() {
  if (!form.code || !form.name) {
    ElMessage.warning(t("permission.msg.validateForm"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createPermission(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("permission.msg.createSuccess"));
        editorVisible.value = false;
        fetchPermissions();
      } else {
        ElMessage.error(result?.message || t("permission.msg.createFailed"));
      }
    } else if (editorId.value != null) {
      const result = await updatePermission(editorId.value, {code: form.code, name: form.name});
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("permission.msg.updateSuccess"));
        editorVisible.value = false;
        fetchPermissions();
      } else {
        ElMessage.error(result?.message || t("permission.msg.updateFailed"));
      }
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("permission.msg.saveFailed")));
  } finally {
    saving.value = false;
  }
}

async function handleStatusChange(permission: PermissionVO, value: number) {
  const previous = permission.status ?? 0;
  permission.status = value;
  try {
    const result = await updatePermissionStatus(permission.id, value);
    if (result?.code !== 200) {
      permission.status = previous;
      ElMessage.error(result?.message || t("permission.msg.statusUpdateFailed"));
    }
  } catch (error) {
    permission.status = previous;
    ElMessage.error(getErrorMessage(error, t("permission.msg.statusUpdateFailed")));
  }
}

onMounted(fetchPermissions);
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
</style>
