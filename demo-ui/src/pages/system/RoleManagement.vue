<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("role.title") }}</div>
        <div class="module-sub">{{ t("role.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-button v-permission="'role:create'" size="small" type="primary" @click="openCreate">{{
            t("role.create")
          }}
        </el-button>
        <el-button v-if="selectedRoleIds.length" v-permission="'role:delete'" size="small" type="danger"
                   @click="removeRoles">
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
              v-permission="'role:disable'"
              :active-value="1"
              :inactive-value="0"
              :model-value="row.status ?? 0"
              @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('role.table.action')" width="420">
        <template #default="{row}">
          <div class="action-buttons">
            <el-button v-permission="'role:update'" size="small" text @click="openEdit(row)">{{ t("role.table.edit") }}</el-button>
            <el-button v-permission="'role:permission:assign'" size="small" text @click="openPermissions(row)">{{ t("role.table.assignPermissions") }}</el-button>
            <el-button v-permission="'role:menu:assign'" size="small" text @click="openMenus(row)">{{ t("role.table.assignMenus") }}</el-button>
            <el-button v-permission="'role:menu:data-scope'" size="small" text @click="openMenuScopes(row)">{{ t("role.table.menuDataScope") }}</el-button>
            <el-button v-permission="'role:delete'" size="small" text type="danger" @click="removeRole(row)">{{ t("role.table.delete") }}</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="720px">
      <el-tabs v-model="editorTab">
        <el-tab-pane :label="t('role.tabs.basic')" name="basic">
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
            </el-row>
          </el-form>
        </el-tab-pane>
        <el-tab-pane :label="t('role.tabs.dataScope')" name="scope">
          <el-form label-position="top">
            <el-form-item :label="t('role.dialog.dataScopeType')">
              <el-radio-group v-model="form.dataScopeType" class="scope-group">
                <el-radio value="ALL">{{ t('role.scope.all') }}</el-radio>
                <el-radio value="DEPT_AND_CHILD">{{ t('role.scope.deptAndChild') }}</el-radio>
                <el-radio value="DEPT">{{ t('role.scope.dept') }}</el-radio>
                <el-radio value="CUSTOM_DEPT">{{ t('role.scope.customDept') }}</el-radio>
                <el-radio value="SELF">{{ t('role.scope.self') }}</el-radio>
                <el-radio value="NONE">{{ t('role.scope.none') }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <div v-if="isCustomScope" class="custom-dept">
              <div class="custom-title">{{ t('role.dialog.customDept') }}</div>
              <el-tree
                  ref="deptTreeRef"
                  :data="deptTree"
                  :props="{label: 'name', children: 'children'}"
                  node-key="id"
                  show-checkbox
              />
            </div>
          </el-form>
        </el-tab-pane>
        <el-tab-pane :label="t('role.tabs.menuScope')" name="menu-scope">
          <div class="menu-scope-note">{{ t('role.menuScope.hint') }}</div>
          <el-button @click="openMenuScopesByTab">{{ t('role.menuScope.open') }}</el-button>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button v-permission="editorMode === 'create' ? 'role:create' : 'role:update'" :loading="saving" type="primary" @click="saveRole">{{ t("common.save") }}</el-button>
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
        <el-button v-permission="'role:permission:assign'" :loading="assigning" type="primary" @click="savePermissions">{{ t("common.save") }}</el-button>
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
        <el-button v-permission="'role:menu:assign'" :loading="assigningMenus" type="primary" @click="saveMenus">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuScopeVisible" align-center :title="t('role.menuScope.title')" width="860px">
      <div class="menu-scope-layout">
        <div class="menu-scope-tree">
          <el-tree
              ref="menuScopeTreeRef"
              :data="menuScopeTree"
              :props="{label: 'name', children: 'children'}"
              node-key="id"
              highlight-current
              @node-click="handleMenuScopeSelect"
          />
        </div>
        <div class="menu-scope-config">
          <div v-if="activeMenuScopeItem" class="menu-scope-card">
            <div class="menu-scope-title">{{ activeMenuScopeItem.menuName }}</div>
            <div class="menu-scope-sub">{{ activeMenuScopeItem.permission || '-' }}</div>
            <el-form label-position="top">
              <el-form-item :label="t('role.menuScope.scopeType')">
                <el-radio-group v-model="menuScopeForm.dataScopeType">
                  <el-radio value="INHERIT">{{ t('role.menuScope.inherit') }}</el-radio>
                  <el-radio value="ALL">{{ t('role.scope.all') }}</el-radio>
                  <el-radio value="DEPT_AND_CHILD">{{ t('role.scope.deptAndChild') }}</el-radio>
                  <el-radio value="DEPT">{{ t('role.scope.dept') }}</el-radio>
                  <el-radio value="CUSTOM_DEPT">{{ t('role.scope.customDept') }}</el-radio>
                  <el-radio value="SELF">{{ t('role.scope.self') }}</el-radio>
                  <el-radio value="NONE">{{ t('role.scope.none') }}</el-radio>
                </el-radio-group>
              </el-form-item>
              <div v-if="isMenuScopeCustom" class="custom-dept">
                <div class="custom-title">{{ t('role.dialog.customDept') }}</div>
                <el-tree
                    ref="menuDeptTreeRef"
                    :data="deptTree"
                    :props="{label: 'name', children: 'children'}"
                    node-key="id"
                    show-checkbox
                />
              </div>
            </el-form>
            <div class="menu-scope-actions">
              <el-button v-permission="'role:menu:data-scope'" @click="clearMenuScope">{{ t('role.menuScope.clear') }}</el-button>
              <el-button v-permission="'role:menu:data-scope'" type="primary" @click="applyMenuScope">{{ t('role.menuScope.apply') }}</el-button>
            </div>
          </div>
          <el-empty v-else :description="t('role.menuScope.empty')"/>
        </div>
      </div>
      <template #footer>
        <el-button @click="menuScopeVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button v-permission="'role:menu:data-scope'" :loading="menuScopeSaving" type="primary" @click="saveMenuScopes">
          {{ t('common.save') }}
        </el-button>
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
  type DeptVO,
  getRoleMenuDataScope,
  getRoleMenuIds,
  listDeptOptions,
  listMenus,
  listPermissions,
  listRoles,
  type MenuVO,
  type PermissionVO,
  type RoleCreatePayload,
  type RoleMenuDataScopeItem,
  type RoleMenuDataScopeItemPayload,
  type RoleUpdatePayload,
  type RoleVO,
  saveRoleMenuDataScope,
  updateRole,
  updateRoleStatus
} from "../../api/system";
import {buildTree, type TreeNode} from "../../utils/tree";

const roles = ref<RoleVO[]>([]);
const permissions = ref<PermissionVO[]>([]);
const menus = ref<MenuVO[]>([]);
const menuTree = ref<TreeNode<MenuVO>[]>([]);
const depts = ref<DeptVO[]>([]);
const deptTree = ref<TreeNode<DeptVO>[]>([]);
const loading = ref(false);
const saving = ref(false);
const assigning = ref(false);
const assigningMenus = ref(false);
const menuScopeSaving = ref(false);
const editorVisible = ref(false);
const permissionVisible = ref(false);
const menuVisible = ref(false);
const menuScopeVisible = ref(false);
const editorTab = ref("basic");
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
const deptTreeRef = ref<MenuTreeRef | null>(null);

const menuScopeRoleId = ref<number | null>(null);
const menuScopeItems = ref<RoleMenuDataScopeItem[]>([]);
const menuScopeTree = ref<TreeNode<any>[]>([]);
const menuScopeTreeRef = ref<MenuTreeRef | null>(null);
const menuScopeActiveMenuId = ref<number | null>(null);
const menuDeptTreeRef = ref<MenuTreeRef | null>(null);
const menuScopeForm = reactive<{ dataScopeType: string; customDeptIds: number[] }>({
  dataScopeType: "INHERIT",
  customDeptIds: []
});

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

const isCustomScope = computed(() => {
  return form.dataScopeType === "CUSTOM_DEPT" || form.dataScopeType === "CUSTOM";
});

const activeMenuScopeItem = computed(() => {
  if (menuScopeActiveMenuId.value == null) {
    return null;
  }
  return menuScopeItems.value.find((item) => item.menuId === menuScopeActiveMenuId.value) || null;
});

const isMenuScopeCustom = computed(() => {
  return menuScopeForm.dataScopeType === "CUSTOM_DEPT" || menuScopeForm.dataScopeType === "CUSTOM";
});

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

function parseDeptIds(value?: string): number[] {
  if (!value) {
    return [];
  }
  return value
      .split(",")
      .map((item) => Number(item.trim()))
      .filter((item) => Number.isFinite(item));
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

async function fetchDepts() {
  if (depts.value.length) {
    return;
  }
  const result = await listDeptOptions();
  if (result?.code === 200 && result.data) {
    depts.value = result.data;
    deptTree.value = buildTree(result.data);
  }
}

function resetFormState() {
  form.code = "";
  form.name = "";
  form.status = 1;
  form.dataScopeType = "SELF";
  form.dataScopeValue = "";
}

function openCreate() {
  editorMode.value = "create";
  editorRoleId.value = null;
  resetFormState();
  editorTab.value = "basic";
  fetchDepts().then(() => nextTick(() => deptTreeRef.value?.setCheckedKeys([])));
  editorVisible.value = true;
}

function openEdit(role: RoleVO) {
  editorMode.value = "edit";
  editorRoleId.value = role.id;
  resetFormState();
  form.code = role.code;
  form.name = role.name;
  form.status = role.status ?? 1;
  form.dataScopeType = role.dataScopeType || "SELF";
  form.dataScopeValue = role.dataScopeValue || "";
  editorTab.value = "basic";
  fetchDepts().then(() => {
    const deptIds = parseDeptIds(form.dataScopeValue);
    nextTick(() => deptTreeRef.value?.setCheckedKeys(deptIds));
  });
  editorVisible.value = true;
}

async function saveRole() {
  if (!form.code || !form.name) {
    ElMessage.warning(t("role.msg.validateForm"));
    return;
  }
  if (isCustomScope.value) {
    const tree = deptTreeRef.value;
    const checked = tree?.getCheckedKeys(false) as number[] | undefined;
    form.dataScopeValue = (checked || []).join(",");
  } else {
    form.dataScopeValue = "";
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createRole(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("role.msg.createSuccess"));
        editorVisible.value = false;
        await fetchRoles();
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
        await fetchRoles();
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
      await fetchRoles();
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

async function openMenuScopes(role: RoleVO) {
  await fetchMenus();
  await fetchDepts();
  menuScopeRoleId.value = role.id;
  menuScopeVisible.value = true;
  await loadMenuScope(role.id);
}

function openMenuScopesByTab() {
  if (editorRoleId.value == null) {
    ElMessage.warning(t("role.menuScope.needRole"));
    return;
  }
  const role = roles.value.find((item) => item.id === editorRoleId.value);
  if (!role) {
    ElMessage.warning(t("role.menuScope.needRole"));
    return;
  }
  openMenuScopes(role);
}

async function loadMenuScope(roleId: number) {
  menuScopeItems.value = [];
  menuScopeTree.value = [];
  menuScopeActiveMenuId.value = null;
  menuScopeForm.dataScopeType = "INHERIT";
  menuScopeForm.customDeptIds = [];
  const result = await getRoleMenuDataScope(roleId);
  if (result?.code === 200 && result.data) {
    const items = result.data.items || [];
    menuScopeItems.value = items;
    const treeSource = items.map((item) => ({
      id: item.menuId,
      menuId: item.menuId,
      name: item.menuName || "",
      parentId: item.parentId ?? null,
      permission: item.permission,
      dataScopeType: item.dataScopeType,
      customDeptIds: item.customDeptIds || []
    }));
    menuScopeTree.value = buildTree(treeSource);
    const firstLeaf = items.find((item) => !!item.permission);
    if (firstLeaf) {
      menuScopeActiveMenuId.value = firstLeaf.menuId;
      syncMenuScopeForm(firstLeaf);
    }
  }
}

function handleMenuScopeSelect(node: { menuId?: number; permission?: string }) {
  if (!node || !node.menuId) {
    return;
  }
  const target = menuScopeItems.value.find((item) => item.menuId === node.menuId) || null;
  if (!target || !target.permission) {
    menuScopeActiveMenuId.value = null;
    return;
  }
  menuScopeActiveMenuId.value = target.menuId;
  syncMenuScopeForm(target);
}

function syncMenuScopeForm(item: RoleMenuDataScopeItem) {
  menuScopeForm.dataScopeType = item.dataScopeType || "INHERIT";
  menuScopeForm.customDeptIds = item.customDeptIds ? [...item.customDeptIds] : [];
  nextTick(() => {
    menuDeptTreeRef.value?.setCheckedKeys(menuScopeForm.customDeptIds || []);
  });
}

function applyMenuScope() {
  if (!activeMenuScopeItem.value) {
    return;
  }
  const menuId = activeMenuScopeItem.value.menuId;
  const type = menuScopeForm.dataScopeType;
  let customDeptIds: number[] = [];
  if (isMenuScopeCustom.value) {
    const tree = menuDeptTreeRef.value;
    const checked = tree?.getCheckedKeys(false) as number[] | undefined;
    customDeptIds = checked || [];
  }
  updateMenuScopeItem(menuId, type, customDeptIds);
}

function clearMenuScope() {
  if (!activeMenuScopeItem.value) {
    return;
  }
  updateMenuScopeItem(activeMenuScopeItem.value.menuId, "INHERIT", []);
  syncMenuScopeForm(activeMenuScopeItem.value);
}

function updateMenuScopeItem(menuId: number, type: string, customDeptIds: number[]) {
  const items = menuScopeItems.value;
  const index = items.findIndex((item) => item.menuId === menuId);
  if (index < 0) {
    return;
  }
  items[index] = {
    ...items[index],
    dataScopeType: type === "INHERIT" ? null : type,
    customDeptIds: type === "CUSTOM_DEPT" || type === "CUSTOM" ? customDeptIds : []
  };
  menuScopeItems.value = [...items];
}

async function saveMenuScopes() {
  if (menuScopeRoleId.value == null) {
    return;
  }
  menuScopeSaving.value = true;
  try {
    const payload: RoleMenuDataScopeItemPayload[] = menuScopeItems.value
        .filter((item) => !!item.permission)
        .map((item) => ({
          menuId: item.menuId,
          dataScopeType: item.dataScopeType || undefined,
          customDeptIds: item.customDeptIds || []
        }));
    const result = await saveRoleMenuDataScope(menuScopeRoleId.value, payload);
    if (result?.code === 200) {
      ElMessage.success(t("common.saveSuccess"));
      menuScopeVisible.value = false;
    } else {
      ElMessage.error(result?.message || t("common.saveFailed"));
    }
  } finally {
    menuScopeSaving.value = false;
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
    await fetchRoles();
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
    await fetchRoles();
  } else {
    ElMessage.error(result?.message || t("role.msg.deleteFailed"));
  }
}

onMounted(() => {
  fetchRoles();
  fetchDepts();
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

.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: nowrap;
  align-items: center;
}

.scope-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.custom-dept {
  margin-top: 8px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 12px;
  padding: 12px;
}

.custom-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.menu-scope-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
}

.menu-scope-tree {
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 12px;
  padding: 8px;
  min-height: 360px;
}

.menu-scope-card {
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 12px;
  padding: 12px;
  min-height: 360px;
}

.menu-scope-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.menu-scope-sub {
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 12px;
}

.menu-scope-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.menu-scope-note {
  color: var(--muted);
  margin-bottom: 8px;
}

@media (max-width: 860px) {
  .menu-scope-layout {
    grid-template-columns: 1fr;
  }
}
</style>
