<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">用户管理</div>
        <div class="module-sub">维护系统用户、账号状态与角色关系。</div>
      </div>
      <div class="module-actions">
        <el-input v-model.trim="filters.userName" clearable placeholder="用户名"/>
        <el-input v-model.trim="filters.nickName" clearable placeholder="昵称"/>
        <el-select v-model="filters.status" clearable placeholder="状态" style="width: 120px">
          <el-option :value="1" label="启用"/>
          <el-option :value="0" label="禁用"/>
        </el-select>
        <el-button @click="handleSearch">查询</el-button>
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="users" row-key="id" size="small">
      <el-table-column label="用户名" min-width="140" prop="userName"/>
      <el-table-column label="昵称" min-width="140" prop="nickName"/>
      <el-table-column label="性别" width="80">
        <template #default="{row}">
          {{ sexLabel(row.sex) }}
        </template>
      </el-table-column>
      <el-table-column label="部门" prop="deptId" width="120">
        <template #default="{row}">
          {{ deptName(row.deptId) }}
        </template>
      </el-table-column>
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
      <el-table-column label="操作" width="220">
        <template #default="{row}">
          <el-button size="small" text @click="openEdit(row)">编辑</el-button>
          <el-button size="small" text @click="openReset(row)">重置密码</el-button>
          <el-button size="small" text @click="openRoles(row)">分配角色</el-button>
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

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="520px">
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model.trim="form.userName" :disabled="editorMode === 'edit'"/>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model.trim="form.nickName"/>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.sex" placeholder="请选择">
            <el-option label="男" value="M"/>
            <el-option label="女" value="F"/>
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="form.deptId" placeholder="请选择">
            <el-option
                v-for="dept in depts"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option :value="1" label="启用"/>
            <el-option :value="0" label="禁用"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="editorMode === 'create'" label="初始密码">
          <el-input v-model.trim="form.password" show-password type="password"/>
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
        <el-form-item label="备注">
          <el-input v-model.trim="form.tst"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleVisible" align-center title="分配角色" width="480px">
      <el-form label-position="top">
        <el-form-item label="角色列表">
          <el-select v-model="selectedRoleIds" multiple placeholder="请选择">
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button :loading="assigning" type="primary" @click="saveRoles">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetVisible" align-center title="重置密码" width="420px">
      <el-form label-position="top">
        <el-form-item label="新密码">
          <el-input v-model.trim="resetForm.newPassword" show-password type="password"/>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model.trim="resetForm.confirmPassword" show-password type="password"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button :loading="resetting" type="primary" @click="saveReset">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {
  assignUserRoles,
  createUser,
  type DeptVO,
  getUserRoleIds,
  listDepts,
  listRoles,
  listUsers,
  resetUserPassword,
  type RoleVO,
  updateUser,
  updateUserStatus,
  type UserCreatePayload,
  type UserUpdatePayload,
  type UserVO
} from "../../api/system";

const users = ref<UserVO[]>([]);
const roles = ref<RoleVO[]>([]);
const depts = ref<DeptVO[]>([]);
const loading = ref(false);
const saving = ref(false);
const assigning = ref(false);
const resetting = ref(false);
const editorVisible = ref(false);
const roleVisible = ref(false);
const resetVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorUserId = ref<number | null>(null);
const selectedUserId = ref<number | null>(null);

const filters = reactive({
  userName: "",
  nickName: "",
  status: undefined as number | undefined
});

const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const form = reactive<UserCreatePayload & UserUpdatePayload>({
  userName: "",
  nickName: "",
  password: "",
  sex: "",
  status: 1,
  deptId: undefined,
  dataScopeType: "",
  dataScopeValue: "",
  tst: ""
});

const selectedRoleIds = ref<number[]>([]);

const resetForm = reactive({
  newPassword: "",
  confirmPassword: ""
});

const editorTitle = computed(() => (editorMode.value === "create" ? "新增用户" : "编辑用户"));

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

function sexLabel(value?: string) {
  if (value === "M") {
    return "男";
  }
  if (value === "F") {
    return "女";
  }
  return "-";
}

function deptName(id?: number | null) {
  if (!id) {
    return "-";
  }
  const match = depts.value.find((dept) => dept.id === id);
  return match?.name || String(id);
}

async function fetchUsers() {
  loading.value = true;
  try {
    const result = await listUsers({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userName: filters.userName || undefined,
      nickName: filters.nickName || undefined,
      status: filters.status
    });
    if (result?.code === 200 && result.data) {
      users.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || "加载用户失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "加载用户失败"));
  } finally {
    loading.value = false;
  }
}

async function fetchRoles() {
  if (roles.value.length) {
    return;
  }
  const result = await listRoles();
  if (result?.code === 200 && result.data) {
    roles.value = result.data;
  }
}

async function fetchDepts() {
  const result = await listDepts();
  if (result?.code === 200 && result.data) {
    depts.value = result.data;
  }
}

function handleSearch() {
  pageNum.value = 1;
  fetchUsers();
}

function handlePageChange(value: number) {
  pageNum.value = value;
  fetchUsers();
}

function handleSizeChange(value: number) {
  pageSize.value = value;
  pageNum.value = 1;
  fetchUsers();
}

function resetFormState() {
  form.userName = "";
  form.nickName = "";
  form.password = "";
  form.sex = "";
  form.status = 1;
  form.deptId = undefined;
  form.dataScopeType = "";
  form.dataScopeValue = "";
  form.tst = "";
}

function openCreate() {
  if (!depts.value.length) {
    fetchDepts();
  }
  editorMode.value = "create";
  editorUserId.value = null;
  resetFormState();
  editorVisible.value = true;
}

function openEdit(row: UserVO) {
  if (!depts.value.length) {
    fetchDepts();
  }
  editorMode.value = "edit";
  editorUserId.value = row.id;
  resetFormState();
  form.userName = row.userName;
  form.nickName = row.nickName;
  form.sex = row.sex || "";
  form.status = row.status ?? 1;
  form.deptId = row.deptId ?? undefined;
  form.dataScopeType = row.dataScopeType || "";
  form.dataScopeValue = row.dataScopeValue || "";
  form.tst = row.tst || "";
  editorVisible.value = true;
}

async function saveUser() {
  if (!form.userName) {
    ElMessage.warning("请输入用户名");
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createUser(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || "创建成功");
        editorVisible.value = false;
        fetchUsers();
      } else {
        ElMessage.error(result?.message || "创建失败");
      }
    } else if (editorUserId.value != null) {
      const payload: UserUpdatePayload = {...form};
      delete (payload as { password?: string }).password;
      const result = await updateUser(editorUserId.value, payload);
      if (result?.code === 200) {
        ElMessage.success(result?.message || "更新成功");
        editorVisible.value = false;
        fetchUsers();
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

async function handleStatusChange(row: UserVO, value: number) {
  const previous = row.status ?? 0;
  row.status = value;
  try {
    const result = await updateUserStatus(row.id, value);
    if (result?.code !== 200) {
      row.status = previous;
      ElMessage.error(result?.message || "状态更新失败");
    }
  } catch (error) {
    row.status = previous;
    ElMessage.error(getErrorMessage(error, "状态更新失败"));
  }
}

async function openRoles(row: UserVO) {
  selectedUserId.value = row.id;
  await fetchRoles();
  const result = await getUserRoleIds(row.id);
  if (result?.code === 200 && result.data) {
    selectedRoleIds.value = result.data;
  } else {
    selectedRoleIds.value = [];
  }
  roleVisible.value = true;
}

async function saveRoles() {
  if (selectedUserId.value == null) {
    return;
  }
  assigning.value = true;
  try {
    const result = await assignUserRoles(selectedUserId.value, selectedRoleIds.value);
    if (result?.code === 200) {
      ElMessage.success(result?.message || "角色已更新");
      roleVisible.value = false;
    } else {
      ElMessage.error(result?.message || "角色更新失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "角色更新失败"));
  } finally {
    assigning.value = false;
  }
}

function openReset(row: UserVO) {
  selectedUserId.value = row.id;
  resetForm.newPassword = "";
  resetForm.confirmPassword = "";
  resetVisible.value = true;
}

async function saveReset() {
  if (!resetForm.newPassword) {
    ElMessage.warning("请输入新密码");
    return;
  }
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    ElMessage.warning("两次输入的密码不一致");
    return;
  }
  if (selectedUserId.value == null) {
    return;
  }
  resetting.value = true;
  try {
    const result = await resetUserPassword(selectedUserId.value, resetForm.newPassword);
    if (result?.code === 200) {
      ElMessage.success(result?.message || "密码已重置");
      resetVisible.value = false;
    } else {
      ElMessage.error(result?.message || "密码重置失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "密码重置失败"));
  } finally {
    resetting.value = false;
  }
}

onMounted(() => {
  fetchUsers();
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
  flex-wrap: nowrap;
  overflow-x: auto;
  gap: 8px;
  align-items: center;
}

.module-actions :deep(.el-input),
.module-actions :deep(.el-select) {
  width: 150px;
  flex: 0 0 auto;
}

.module-actions :deep(.el-button) {
  flex: 0 0 auto;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
