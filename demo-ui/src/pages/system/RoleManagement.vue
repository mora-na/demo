<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">角色管理</div>
        <div class="module-sub">维护角色信息与权限、菜单授权。</div>
      </div>
      <div class="module-actions">
        <el-button type="primary" @click="openCreate">新增角色</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="roles" row-key="id" size="small">
      <el-table-column label="编码" min-width="140" prop="code"/>
      <el-table-column label="名称" min-width="140" prop="name"/>
      <el-table-column label="数据范围" min-width="140" prop="dataScopeType"/>
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-switch
              :active-value="1"
              :inactive-value="0"
              :model-value="row.status ?? 0"
              @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{row}">
          <el-button size="small" text @click="openEdit(row)">编辑</el-button>
          <el-button size="small" text @click="openPermissions(row)">分配权限</el-button>
          <el-button size="small" text @click="openMenus(row)">分配菜单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="520px">
      <el-form :model="form" label-position="top">
        <el-form-item label="角色编码">
          <el-input v-model.trim="form.code"/>
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model.trim="form.name"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option :value="1" label="启用"/>
            <el-option :value="0" label="禁用"/>
          </el-select>
        </el-form-item>
        <el-form-item label="数据范围类型">
          <el-select v-model="form.dataScopeType" placeholder="请选择">
            <el-option label="全部" value="ALL"/>
            <el-option label="本部门" value="DEPT"/>
            <el-option label="部门及下级" value="DEPT_AND_CHILD"/>
            <el-option label="自定义" value="CUSTOM"/>
            <el-option label="自定义部门" value="CUSTOM_DEPT"/>
            <el-option label="仅本人" value="SELF"/>
            <el-option label="无权限" value="NONE"/>
          </el-select>
        </el-form-item>
        <el-form-item label="数据范围值">
          <el-input v-model.trim="form.dataScopeValue" placeholder="逗号分隔 ID"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionVisible" align-center title="分配权限" width="520px">
      <el-form label-position="top">
        <el-form-item label="权限列表">
          <el-select v-model="selectedPermissionIds" multiple placeholder="请选择">
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
        <el-button @click="permissionVisible = false">取消</el-button>
        <el-button :loading="assigning" type="primary" @click="savePermissions">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuVisible" align-center title="分配菜单" width="520px">
      <el-tree
          ref="menuTreeRef"
          :data="menuTree"
          :props="{label: 'name', children: 'children'}"
          node-key="id"
          show-checkbox
      />
      <template #footer>
        <el-button @click="menuVisible = false">取消</el-button>
        <el-button :loading="assigningMenus" type="primary" @click="saveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, nextTick, onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {
  assignRoleMenus,
  assignRolePermissions,
  createRole,
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
const selectedPermissionIds = ref<number[]>([]);
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

const editorTitle = computed(() => (editorMode.value === "create" ? "新增角色" : "编辑角色"));

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

async function fetchRoles() {
  loading.value = true;
  try {
    const result = await listRoles();
    if (result?.code === 200 && result.data) {
      roles.value = result.data;
    } else {
      ElMessage.error(result?.message || "加载角色失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "加载角色失败"));
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
    ElMessage.warning("请填写角色编码与名称");
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createRole(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || "创建成功");
        editorVisible.value = false;
        fetchRoles();
      } else {
        ElMessage.error(result?.message || "创建失败");
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
        ElMessage.success(result?.message || "更新成功");
        editorVisible.value = false;
        fetchRoles();
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

async function handleStatusChange(role: RoleVO, value: number) {
  const previous = role.status ?? 0;
  role.status = value;
  try {
    const result = await updateRoleStatus(role.id, value);
    if (result?.code !== 200) {
      role.status = previous;
      ElMessage.error(result?.message || "状态更新失败");
    }
  } catch (error) {
    role.status = previous;
    ElMessage.error(getErrorMessage(error, "状态更新失败"));
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
      ElMessage.success(result?.message || "权限已更新");
      permissionVisible.value = false;
      fetchRoles();
    } else {
      ElMessage.error(result?.message || "权限更新失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "权限更新失败"));
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
      ElMessage.success(result?.message || "菜单已更新");
      menuVisible.value = false;
    } else {
      ElMessage.error(result?.message || "菜单更新失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "菜单更新失败"));
  } finally {
    assigningMenus.value = false;
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
</style>
