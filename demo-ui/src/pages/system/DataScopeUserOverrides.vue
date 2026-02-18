<template>
  <div class="override-panel">
    <div class="override-head">
      <div class="filters" @keyup.enter="handleSearch">
        <el-input v-model.trim="filters.userName" clearable :placeholder="t('dataScope.user.userName')"/>
        <el-input v-model.trim="filters.menuKeyword" clearable :placeholder="t('dataScope.user.menuKeyword')"/>
        <el-select v-model="filters.status" clearable :placeholder="t('dataScope.user.statusPlaceholder')" style="width: 120px">
          <el-option :label="t('common.enabled')" :value="1"/>
          <el-option :label="t('common.disabled')" :value="0"/>
        </el-select>
        <el-button v-permission="'data-scope:user:query'" @click="handleSearch">{{ t("common.search") }}</el-button>
      </div>
      <el-button v-permission="'data-scope:user:manage'" type="primary" @click="openCreate">{{ t("dataScope.user.create") }}</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" size="small">
      <el-table-column prop="userName" :label="t('dataScope.user.user')" min-width="120"/>
      <el-table-column prop="deptName" :label="t('dataScope.user.dept')" min-width="120"/>
      <el-table-column prop="menuName" :label="t('dataScope.user.menu')" min-width="160"/>
      <el-table-column prop="scopeKey" :label="t('dataScope.user.scopeKey')" min-width="180"/>
      <el-table-column prop="dataScopeType" :label="t('dataScope.user.scopeType')" min-width="120"/>
      <el-table-column prop="dataScopeValue" :label="t('dataScope.user.scopeValue')" min-width="140"/>
      <el-table-column prop="status" :label="t('dataScope.user.status')" width="90">
        <template #default="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? t("common.enabled") : t("common.disabled") }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('dataScope.user.action')" width="160">
        <template #default="{row}">
          <div class="action-buttons">
            <el-button v-permission="'data-scope:user:manage'" size="small" text @click="openEdit(row)">{{ t("common.edit") }}</el-button>
            <el-button v-permission="'data-scope:user:manage'" size="small" text type="danger" @click="removeRow(row)">{{ t("common.delete") }}</el-button>
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
            <el-form-item :label="t('dataScope.user.user')">
              <el-select
                  v-model="form.userId"
                  :disabled="editorMode === 'edit'"
                  filterable
                  remote
                  :placeholder="t('dataScope.user.userPlaceholder')"
                  :remote-method="handleUserSearch"
                  @visible-change="handleUserDropdown"
              >
                <el-option
                    v-for="user in userOptions"
                    :key="user.id"
                    :label="`${user.userName} (${user.nickName || '-'})`"
                    :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.user.scopeKey')">
              <el-select v-model="form.scopeKey" :disabled="editorMode === 'edit'" filterable clearable>
                <el-option :label="t('dataScope.user.global')" value="*"/>
                <el-option
                    v-for="menu in permissionOptions"
                    :key="menu.permission"
                    :label="`${menu.name} (${menu.permission})`"
                    :value="menu.permission"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.user.scopeType')">
              <el-select v-model="form.dataScopeType">
                <el-option label="ALL" value="ALL"/>
                <el-option label="DEPT" value="DEPT"/>
                <el-option label="DEPT_AND_CHILD" value="DEPT_AND_CHILD"/>
                <el-option label="CUSTOM_DEPT" value="CUSTOM_DEPT"/>
                <el-option label="SELF" value="SELF"/>
                <el-option label="NONE" value="NONE"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.user.scopeValue')">
              <el-input v-model.trim="form.dataScopeValue"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('dataScope.user.status')">
              <el-select v-model="form.status">
                <el-option :label="t('common.enabled')" :value="1"/>
                <el-option :label="t('common.disabled')" :value="0"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item :label="t('dataScope.user.remark')">
              <el-input v-model.trim="form.remark"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button v-permission="'data-scope:user:manage'" :loading="saving" type="primary" @click="saveOverride">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  createUserDataScope,
  deleteUserDataScope,
  listUserDataScopes,
  type MenuVO,
  updateUserDataScope,
  type UserDataScopeCreatePayload,
  type UserDataScopeUpdatePayload,
  type UserDataScopeVO,
  type UserVO
} from "../../api/system";
import {loadDataScopeMenus, loadDataScopeUsers, searchDataScopeUsers} from "./dataScopeOptions";

const {t} = useI18n();
const rows = ref<UserDataScopeVO[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorId = ref<number | null>(null);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const filters = reactive({userName: "", menuKeyword: "", status: undefined as number | undefined});
const userOptions = ref<UserVO[]>([]);
const permissionOptions = ref<MenuVO[]>([]);

const form = reactive<UserDataScopeCreatePayload & UserDataScopeUpdatePayload & { userId?: number | null; scopeKey?: string }>({
  userId: null,
  scopeKey: "",
  dataScopeType: "SELF",
  dataScopeValue: "",
  status: 1,
  remark: ""
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("dataScope.user.create") : t("dataScope.user.edit")
);

async function fetchMenus() {
  permissionOptions.value = await loadDataScopeMenus();
}

async function fetchUserOptions() {
  userOptions.value = await loadDataScopeUsers();
}

async function handleUserSearch(query: string) {
  userOptions.value = await searchDataScopeUsers(query);
}

function handleUserDropdown(visible: boolean) {
  if (visible && !userOptions.value.length) {
    fetchUserOptions();
  }
}

async function fetchRows() {
  loading.value = true;
  try {
    const result = await listUserDataScopes({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userName: filters.userName,
      menuKeyword: filters.menuKeyword,
      status: filters.status
    });
    if (result?.code === 200 && result.data) {
      rows.value = result.data.data;
      total.value = result.data.total;
    } else {
      ElMessage.error(result?.message || t("dataScope.user.loadFailed"));
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
  form.userId = null;
  form.scopeKey = "";
  form.dataScopeType = "SELF";
  form.dataScopeValue = "";
  form.status = 1;
  form.remark = "";
}

function openCreate() {
  editorMode.value = "create";
  editorId.value = null;
  resetForm();
  editorVisible.value = true;
}

function openEdit(row: UserDataScopeVO) {
  editorMode.value = "edit";
  editorId.value = row.id;
  form.userId = row.userId;
  form.scopeKey = row.scopeKey ?? "";
  form.dataScopeType = row.dataScopeType || "SELF";
  form.dataScopeValue = row.dataScopeValue || "";
  form.status = row.status ?? 1;
  form.remark = row.remark || "";
  editorVisible.value = true;
}

async function saveOverride() {
  if (!form.userId || !form.scopeKey) {
    ElMessage.warning(t("dataScope.user.validate"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const payload: UserDataScopeCreatePayload = {
        scopeKey: form.scopeKey,
        dataScopeType: form.dataScopeType || "SELF",
        dataScopeValue: form.dataScopeValue,
        status: form.status,
        remark: form.remark
      };
      const result = await createUserDataScope(form.userId, payload);
      if (result?.code === 200) {
        ElMessage.success(t("common.saveSuccess"));
        editorVisible.value = false;
        await fetchRows();
      } else {
        ElMessage.error(result?.message || t("common.saveFailed"));
      }
    } else if (editorId.value != null) {
      const payload: UserDataScopeUpdatePayload = {
        dataScopeType: form.dataScopeType,
        dataScopeValue: form.dataScopeValue,
        status: form.status,
        remark: form.remark
      };
      const result = await updateUserDataScope(editorId.value, payload);
      if (result?.code === 200) {
        ElMessage.success(t("common.saveSuccess"));
        editorVisible.value = false;
        await fetchRows();
      } else {
        ElMessage.error(result?.message || t("common.saveFailed"));
      }
    }
  } finally {
    saving.value = false;
  }
}

async function removeRow(row: UserDataScopeVO) {
  try {
    await ElMessageBox.confirm(
        t("dataScope.user.deleteConfirm", {name: row.userName}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteUserDataScope(row.id);
  if (result?.code === 200) {
    ElMessage.success(t("common.deleteSuccess"));
    await fetchRows();
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
  fetchMenus();
  fetchUserOptions();
  fetchRows();
});
</script>

<style scoped>
.override-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.override-head {
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
