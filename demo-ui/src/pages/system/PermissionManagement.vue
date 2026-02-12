<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">权限管理</div>
        <div class="module-sub">维护权限标识与授权名称。</div>
      </div>
      <div class="module-actions">
        <el-button type="primary" @click="openCreate">新增权限</el-button>
      </div>
    </div>

    <el-table :data="permissions" size="small" v-loading="loading" row-key="id">
      <el-table-column prop="code" label="编码" min-width="140" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-switch
            :model-value="row.status ?? 0"
            :active-value="1"
            :inactive-value="0"
            @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{row}">
          <el-button text size="small" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editorTitle" width="480px" align-center>
      <el-form :model="form" label-position="top">
        <el-form-item label="权限编码">
          <el-input v-model.trim="form.code" />
        </el-form-item>
        <el-form-item label="权限名称">
          <el-input v-model.trim="form.name" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
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

const form = reactive({
  code: "",
  name: "",
  status: 1
});

const editorTitle = computed(() => (editorMode.value === "create" ? "新增权限" : "编辑权限"));

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as {response?: {data?: {message?: string}}; message?: string};
  return err?.response?.data?.message || err?.message || fallback;
}

async function fetchPermissions() {
  loading.value = true;
  try {
    const result = await listPermissions();
    if (result?.code === 200 && result.data) {
      permissions.value = result.data;
    } else {
      ElMessage.error(result?.message || "加载权限失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "加载权限失败"));
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
    ElMessage.warning("请填写权限编码与名称");
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createPermission(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || "创建成功");
        editorVisible.value = false;
        fetchPermissions();
      } else {
        ElMessage.error(result?.message || "创建失败");
      }
    } else if (editorId.value != null) {
      const result = await updatePermission(editorId.value, {code: form.code, name: form.name});
      if (result?.code === 200) {
        ElMessage.success(result?.message || "更新成功");
        editorVisible.value = false;
        fetchPermissions();
      } else {
        ElMessage.error(result?.message || "更新失败");
      }
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "保存失败"));
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
      ElMessage.error(result?.message || "状态更新失败");
    }
  } catch (error) {
    permission.status = previous;
    ElMessage.error(getErrorMessage(error, "状态更新失败"));
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
