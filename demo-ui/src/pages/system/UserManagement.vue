<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("user.title") }}</div>
        <div class="module-sub">{{ t("user.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-input v-model.trim="filters.userName" clearable :placeholder="t('user.filter.userNamePlaceholder')"/>
        <el-input v-model.trim="filters.nickName" clearable :placeholder="t('user.filter.nickNamePlaceholder')"/>
        <el-select v-model="filters.status" clearable :placeholder="t('user.filter.statusPlaceholder')" style="width: 120px">
          <el-option :value="1" :label="t('user.dialog.statusEnabled')"/>
          <el-option :value="0" :label="t('user.dialog.statusDisabled')"/>
        </el-select>
        <el-button @click="handleSearch">{{ t("user.filter.search") }}</el-button>
        <el-button type="primary" @click="openCreate">{{ t("user.filter.create") }}</el-button>
        <el-button v-if="selectedUserIds.length" type="danger" @click="removeUsers">
          {{ t("user.filter.delete") }}
        </el-button>
      </div>
    </div>

    <el-table
        v-loading="loading"
        :data="users"
        row-key="id"
        size="small"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="46"/>
      <el-table-column :label="t('user.table.userName')" min-width="120" prop="userName" show-overflow-tooltip/>
      <el-table-column :label="t('user.table.nickName')" min-width="120" prop="nickName" show-overflow-tooltip/>
      <el-table-column :label="t('user.table.phone')" width="120" prop="phone" show-overflow-tooltip/>
      <el-table-column :label="t('user.table.email')" min-width="160" prop="email" show-overflow-tooltip/>
      <el-table-column :label="t('user.table.sex')" width="70">
        <template #default="{row}">
          {{ sexLabel(row.sex) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('user.table.dept')" prop="deptId" width="110">
        <template #default="{row}">
          {{ deptName(row.deptId) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('user.table.status')" width="90">
        <template #default="{row}">
          <el-switch
              :active-value="1"
              :inactive-value="0"
              :model-value="row.status ?? 0"
              @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('user.table.action')" width="320">
        <template #default="{row}">
          <div class="action-buttons">
            <el-button size="small" text @click="openEdit(row)">{{ t("user.table.edit") }}</el-button>
            <el-button size="small" text @click="openReset(row)">{{ t("user.table.resetPassword") }}</el-button>
            <el-button size="small" text @click="openRoles(row)">{{ t("user.table.assignRoles") }}</el-button>
            <el-button size="small" text type="danger" @click="removeUser(row)">{{ t("user.table.delete") }}</el-button>
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

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="680px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="16" class="form-grid">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.userName')">
              <el-input v-model.trim="form.userName" :disabled="editorMode === 'edit'"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.nickName')">
              <el-input v-model.trim="form.nickName"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.phone')">
              <el-input v-model.trim="form.phone" :placeholder="t('user.dialog.phonePlaceholder')"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.email')">
              <el-input v-model.trim="form.email" :placeholder="t('user.dialog.emailPlaceholder')"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.sex')">
              <el-select v-model="form.sex" :placeholder="t('user.dialog.sexPlaceholder')">
                <el-option :label="t('user.sex.male')" value="M"/>
                <el-option :label="t('user.sex.female')" value="F"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.dept')">
              <el-select v-model="form.deptId" :placeholder="t('user.dialog.deptPlaceholder')">
                <el-option
                    v-for="dept in depts"
                    :key="dept.id"
                    :label="dept.name"
                    :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.status')">
              <el-select v-model="form.status" :placeholder="t('user.dialog.statusPlaceholder')">
                <el-option :value="1" :label="t('user.dialog.statusEnabled')"/>
                <el-option :value="0" :label="t('user.dialog.statusDisabled')"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="editorMode === 'create'" :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.initialPassword')">
              <el-input v-model.trim="form.password" show-password type="password"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.dataScopeType')">
              <el-select v-model="form.dataScopeType" :placeholder="t('user.dialog.dataScopePlaceholder')">
                <el-option :label="t('user.scope.all')" value="ALL"/>
                <el-option :label="t('user.scope.dept')" value="DEPT"/>
                <el-option :label="t('user.scope.deptAndChild')" value="DEPT_AND_CHILD"/>
                <el-option :label="t('user.scope.custom')" value="CUSTOM"/>
                <el-option :label="t('user.scope.customDept')" value="CUSTOM_DEPT"/>
                <el-option :label="t('user.scope.self')" value="SELF"/>
                <el-option :label="t('user.scope.none')" value="NONE"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('user.dialog.dataScopeValue')">
              <el-input
                  v-model.trim="form.dataScopeValue"
                  :placeholder="t('user.dialog.dataScopeValuePlaceholder')"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item :label="t('user.dialog.remark')">
              <el-input v-model.trim="form.remark"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="saving" type="primary" @click="saveUser">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleVisible" align-center :title="t('user.roles.title')" width="480px">
      <el-form label-position="top">
        <el-form-item :label="t('user.roles.list')">
          <el-select v-model="selectedRoleIds" multiple :placeholder="t('user.roles.placeholder')">
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="assigning" type="primary" @click="saveRoles">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetVisible" align-center :title="t('user.reset.title')" width="420px">
      <el-form label-position="top">
        <el-form-item :label="t('user.reset.newPassword')">
          <el-input v-model.trim="resetForm.newPassword" show-password type="password"/>
        </el-form-item>
        <el-form-item :label="t('user.reset.confirmPassword')">
          <el-input v-model.trim="resetForm.confirmPassword" show-password type="password"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="resetting" type="primary" @click="saveReset">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  assignUserRoles,
  createUser,
  deleteUser,
  deleteUsers,
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
const selectedUserIds = ref<number[]>([]);
const {t} = useI18n();

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
  phone: "",
  email: "",
  password: "",
  sex: "",
  status: 1,
  deptId: undefined,
  dataScopeType: "",
  dataScopeValue: "",
  remark: ""
});

const selectedRoleIds = ref<number[]>([]);

const resetForm = reactive({
  newPassword: "",
  confirmPassword: ""
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("user.dialog.createTitle") : t("user.dialog.editTitle")
);

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

function sexLabel(value?: string) {
  if (value === "M") {
    return t("user.sex.male");
  }
  if (value === "F") {
    return t("user.sex.female");
  }
  return t("user.sex.unknown");
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
      ElMessage.error(result?.message || t("user.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("user.msg.loadFailed")));
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

function handleSelectionChange(rows: UserVO[]) {
  selectedUserIds.value = rows.map((row) => row.id);
}

function resetFormState() {
  form.userName = "";
  form.nickName = "";
  form.phone = "";
  form.email = "";
  form.password = "";
  form.sex = "";
  form.status = 1;
  form.deptId = undefined;
  form.dataScopeType = "";
  form.dataScopeValue = "";
  form.remark = "";
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
  form.phone = row.phone || "";
  form.email = row.email || "";
  form.sex = row.sex || "";
  form.status = row.status ?? 1;
  form.deptId = row.deptId ?? undefined;
  form.dataScopeType = row.dataScopeType || "";
  form.dataScopeValue = row.dataScopeValue || "";
  form.remark = row.remark || "";
  editorVisible.value = true;
}

async function saveUser() {
  if (!form.userName) {
    ElMessage.warning(t("user.msg.validateUserName"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createUser(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("user.msg.createSuccess"));
        editorVisible.value = false;
        fetchUsers();
      } else {
        ElMessage.error(result?.message || t("user.msg.createFailed"));
      }
    } else if (editorUserId.value != null) {
      const payload: UserUpdatePayload = {...form};
      delete (payload as { password?: string }).password;
      const result = await updateUser(editorUserId.value, payload);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("user.msg.updateSuccess"));
        editorVisible.value = false;
        fetchUsers();
      } else {
        ElMessage.error(result?.message || t("user.msg.updateFailed"));
      }
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("user.msg.saveFailed")));
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
      ElMessage.error(result?.message || t("user.msg.statusUpdateFailed"));
    }
  } catch (error) {
    row.status = previous;
    ElMessage.error(getErrorMessage(error, t("user.msg.statusUpdateFailed")));
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

async function removeUser(row: UserVO) {
  try {
    await ElMessageBox.confirm(
        t("user.msg.deleteConfirm", {name: row.userName}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteUser(row.id);
  if (result?.code === 200) {
    ElMessage.success(t("user.msg.deleteSuccess"));
    fetchUsers();
  } else {
    ElMessage.error(result?.message || t("user.msg.deleteFailed"));
  }
}

async function removeUsers() {
  if (!selectedUserIds.value.length) {
    ElMessage.warning(t("user.msg.deleteEmpty"));
    return;
  }
  try {
    await ElMessageBox.confirm(
        t("user.msg.batchDeleteConfirm", {count: selectedUserIds.value.length}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteUsers(selectedUserIds.value);
  if (result?.code === 200) {
    ElMessage.success(t("user.msg.deleteSuccess"));
    fetchUsers();
  } else {
    ElMessage.error(result?.message || t("user.msg.deleteFailed"));
  }
}

async function saveRoles() {
  if (selectedUserId.value == null) {
    return;
  }
  assigning.value = true;
  try {
    const result = await assignUserRoles(selectedUserId.value, selectedRoleIds.value);
    if (result?.code === 200) {
      ElMessage.success(result?.message || t("user.msg.rolesUpdated"));
      roleVisible.value = false;
    } else {
      ElMessage.error(result?.message || t("user.msg.rolesUpdateFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("user.msg.rolesUpdateFailed")));
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
    ElMessage.warning(t("user.msg.validatePassword"));
    return;
  }
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    ElMessage.warning(t("user.msg.validatePasswordConfirm"));
    return;
  }
  if (selectedUserId.value == null) {
    return;
  }
  resetting.value = true;
  try {
    const result = await resetUserPassword(selectedUserId.value, resetForm.newPassword);
    if (result?.code === 200) {
      ElMessage.success(result?.message || t("user.msg.passwordReset"));
      resetVisible.value = false;
    } else {
      ElMessage.error(result?.message || t("user.msg.passwordResetFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("user.msg.passwordResetFailed")));
  } finally {
    resetting.value = false;
  }
}

onMounted(() => {
  fetchDepts();
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

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
  align-items: center;
}

.action-buttons :deep(.el-button) {
  padding: 0 6px;
  min-height: 22px;
  font-size: 12px;
  white-space: nowrap;
}

.form-grid :deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>
