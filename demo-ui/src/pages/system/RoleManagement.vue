<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("role.title") }}</div>
        <div class="module-sub">{{ t("role.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-button type="primary" @click="openCreate">{{ t("role.create") }}</el-button>
        <el-button v-if="selectedRoleIds.length" type="danger" @click="removeRoles">
          {{ t("role.filter.delete") }}
        </el-button>
      </div>
    </div>

    <el-table
        v-loading="loading"
        :data="roles"
        row-key="id"
        size="small"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="46"/>
      <el-table-column :label="t('role.table.code')" min-width="140" prop="code"/>
      <el-table-column :label="t('role.table.name')" min-width="140" prop="name"/>
      <el-table-column :label="t('role.table.dataScope')" min-width="140" prop="dataScopeType"/>
      <el-table-column :label="t('role.table.status')" width="100">
        <template #default="{row}">
          <el-switch
              :active-value="1"
              :inactive-value="0"
              :model-value="row.status ?? 0"
              @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('role.table.action')" width="240">
        <template #default="{row}">
          <el-button size="small" text @click="openEdit(row)">{{ t("role.table.edit") }}</el-button>
          <el-button size="small" text @click="openPermissions(row)">{{ t("role.table.assignPermissions") }}</el-button>
          <el-button size="small" text @click="openMenus(row)">{{ t("role.table.assignMenus") }}</el-button>
          <el-button size="small" text type="danger" @click="removeRole(row)">{{ t("role.table.delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="640px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="16" class="form-grid">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('role.dialog.code')">
              <el-input v-model.trim="form.code"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('role.dialog.name')">
              <el-input v-model.trim="form.name"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('role.dialog.status')">
              <el-select v-model="form.status" :placeholder="t('role.dialog.statusPlaceholder')">
                <el-option :value="1" :label="t('role.dialog.statusEnabled')"/>
                <el-option :value="0" :label="t('role.dialog.statusDisabled')"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('role.dialog.dataScopeType')">
              <el-select v-model="form.dataScopeType" :placeholder="t('role.dialog.dataScopePlaceholder')">
                <el-option :label="t('role.scope.all')" value="ALL"/>
                <el-option :label="t('role.scope.dept')" value="DEPT"/>
                <el-option :label="t('role.scope.deptAndChild')" value="DEPT_AND_CHILD"/>
                <el-option :label="t('role.scope.custom')" value="CUSTOM"/>
                <el-option :label="t('role.scope.customDept')" value="CUSTOM_DEPT"/>
                <el-option :label="t('role.scope.self')" value="SELF"/>
                <el-option :label="t('role.scope.none')" value="NONE"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item :label="t('role.dialog.dataScopeValue')">
              <el-input v-model.trim="form.dataScopeValue" :placeholder="t('role.dialog.dataScopeValuePlaceholder')"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="saving" type="primary" @click="saveRole">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionVisible" align-center :title="t('role.permissions.title')" width="520px">
      <el-form label-position="top">
        <el-form-item :label="t('role.permissions.list')">
          <el-select v-model="selectedPermissionIds" multiple :placeholder="t('role.permissions.placeholder')">
            <el-option
                v-for="permission in permissions"
                :key="permission.id"
                :label="permission.name"
                :value="permission.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="assigning" type="primary" @click="savePermissions">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuVisible" align-center :title="t('role.menus.title')" width="520px">
      <el-tree
          ref="menuTreeRef"
          :data="menuTree"
          :props="{label: 'name', children: 'children'}"
          node-key="id"
          show-checkbox
      />
      <template #footer>
        <el-button @click="menuVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="assigningMenus" type="primary" @click="saveMenus">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, nextTick, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  assignRoleMenus,
  assignRolePermissions,
  createRole,
  deleteRole,
  deleteRoles,
  getRoleMenuIds,
  listMenus,
  listPermissions,
  listRoles,
  type MenuVO,
  type PermissionVO,
  type RoleCreatePayload,
  type RoleUpdatePayload,
  type RoleVO,
  updateRole,
  updateRoleStatus
} from "../../api/system";
import {buildTree, type TreeNode} from "../../utils/tree";

const roles = ref<RoleVO[]>([]);
const permissions = ref<PermissionVO[]>([]);
const menus = ref<MenuVO[]>([]);
const menuTree = ref<TreeNode<MenuVO>[]>([]);
const loading = ref(false);
const saving = ref(false);
const assigning = ref(false);
const assigningMenus = ref(false);
const editorVisible = ref(false);
const permissionVisible = ref(false);
const menuVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorRoleId = ref<number | null>(null);
const selectedRoleId = ref<number | null>(null);
const selectedRoleIds = ref<number[]>([]);
const selectedPermissionIds = ref<number[]>([]);
const {t} = useI18n();
type MenuTreeRef = {
  setCheckedKeys: (keys: number[]) => void;
  getCheckedKeys: (leafOnly?: boolean) => number[];
  getHalfCheckedKeys: () => number[];
};

const menuTreeRef = ref<MenuTreeRef | null>(null);

const form = reactive<RoleCreatePayload & RoleUpdatePayload>({
  code: "",
  name: "",
  status: 1,
  dataScopeType: "",
  dataScopeValue: ""
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("role.dialog.createTitle") : t("role.dialog.editTitle")
);

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

function handleSelectionChange(rows: RoleVO[]) {
  selectedRoleIds.value = rows.map((row) => row.id);
}

async function fetchRoles() {
  loading.value = true;
  try {
    const result = await listRoles();
    if (result?.code === 200 && result.data) {
      roles.value = result.data;
    } else {
      ElMessage.error(result?.message || t("role.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("role.msg.loadFailed")));
  } finally {
    loading.value = false;
  }
}

async function fetchPermissions() {
  if (permissions.value.length) {
    return;
  }
  const result = await listPermissions();
  if (result?.code === 200 && result.data) {
    permissions.value = result.data;
  }
}

async function fetchMenus() {
  const result = await listMenus();
  if (result?.code === 200 && result.data) {
    menus.value = result.data;
    menuTree.value = buildTree(result.data);
  }
}

function resetFormState() {
  form.code = "";
  form.name = "";
  form.status = 1;
  form.dataScopeType = "";
  form.dataScopeValue = "";
}

function openCreate() {
  editorMode.value = "create";
  editorRoleId.value = null;
  resetFormState();
  editorVisible.value = true;
}

function openEdit(role: RoleVO) {
  editorMode.value = "edit";
  editorRoleId.value = role.id;
  resetFormState();
  form.code = role.code;
  form.name = role.name;
  form.status = role.status ?? 1;
  form.dataScopeType = role.dataScopeType || "";
  form.dataScopeValue = role.dataScopeValue || "";
  editorVisible.value = true;
}

async function saveRole() {
  if (!form.code || !form.name) {
    ElMessage.warning(t("role.msg.validateForm"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createRole(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("role.msg.createSuccess"));
        editorVisible.value = false;
        fetchRoles();
      } else {
        ElMessage.error(result?.message || t("role.msg.createFailed"));
      }
    } else if (editorRoleId.value != null) {
      const payload: RoleUpdatePayload = {
        code: form.code,
        name: form.name,
        dataScopeType: form.dataScopeType,
        dataScopeValue: form.dataScopeValue
      };
      const result = await updateRole(editorRoleId.value, payload);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("role.msg.updateSuccess"));
        editorVisible.value = false;
        fetchRoles();
      } else {
        ElMessage.error(result?.message || t("role.msg.updateFailed"));
      }
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("role.msg.saveFailed")));
  } finally {
    saving.value = false;
  }
}

async function handleStatusChange(role: RoleVO, value: number) {
  const previous = role.status ?? 0;
  role.status = value;
  try {
    const result = await updateRoleStatus(role.id, value);
    if (result?.code !== 200) {
      role.status = previous;
      ElMessage.error(result?.message || t("role.msg.statusUpdateFailed"));
    }
  } catch (error) {
    role.status = previous;
    ElMessage.error(getErrorMessage(error, t("role.msg.statusUpdateFailed")));
  }
}

async function openPermissions(role: RoleVO) {
  selectedRoleId.value = role.id;
  await fetchPermissions();
  selectedPermissionIds.value = role.permissionIds ? [...role.permissionIds] : [];
  permissionVisible.value = true;
}

async function savePermissions() {
  if (selectedRoleId.value == null) {
    return;
  }
  assigning.value = true;
  try {
    const result = await assignRolePermissions(selectedRoleId.value, selectedPermissionIds.value);
    if (result?.code === 200) {
      ElMessage.success(result?.message || t("role.msg.permissionsUpdated"));
      permissionVisible.value = false;
      fetchRoles();
    } else {
      ElMessage.error(result?.message || t("role.msg.permissionsUpdateFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("role.msg.permissionsUpdateFailed")));
  } finally {
    assigning.value = false;
  }
}

async function openMenus(role: RoleVO) {
  selectedRoleId.value = role.id;
  await fetchMenus();
  const result = await getRoleMenuIds(role.id);
  const ids = result?.code === 200 && result.data ? result.data : [];
  menuVisible.value = true;
  await nextTick();
  menuTreeRef.value?.setCheckedKeys(ids);
}

async function saveMenus() {
  if (selectedRoleId.value == null) {
    return;
  }
  assigningMenus.value = true;
  try {
    const tree = menuTreeRef.value;
    const checked = tree?.getCheckedKeys(false) as number[] | undefined;
    const halfChecked = tree?.getHalfCheckedKeys() as number[] | undefined;
    const menuIds = Array.from(new Set([...(checked || []), ...(halfChecked || [])]));
    const result = await assignRoleMenus(selectedRoleId.value, menuIds);
    if (result?.code === 200) {
      ElMessage.success(result?.message || t("role.msg.menusUpdated"));
      menuVisible.value = false;
    } else {
      ElMessage.error(result?.message || t("role.msg.menusUpdateFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("role.msg.menusUpdateFailed")));
  } finally {
    assigningMenus.value = false;
  }
}

async function removeRole(role: RoleVO) {
  try {
    await ElMessageBox.confirm(
        t("role.msg.deleteConfirm", {name: role.name}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteRole(role.id);
  if (result?.code === 200) {
    ElMessage.success(t("role.msg.deleteSuccess"));
    fetchRoles();
  } else {
    ElMessage.error(result?.message || t("role.msg.deleteFailed"));
  }
}

async function removeRoles() {
  if (!selectedRoleIds.value.length) {
    ElMessage.warning(t("role.msg.deleteEmpty"));
    return;
  }
  try {
    await ElMessageBox.confirm(
        t("role.msg.batchDeleteConfirm", {count: selectedRoleIds.value.length}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteRoles(selectedRoleIds.value);
  if (result?.code === 200) {
    ElMessage.success(t("role.msg.deleteSuccess"));
    fetchRoles();
  } else {
    ElMessage.error(result?.message || t("role.msg.deleteFailed"));
  }
}

onMounted(() => {
  fetchRoles();
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
  gap: 8px;
  align-items: center;
}

.form-grid :deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>
