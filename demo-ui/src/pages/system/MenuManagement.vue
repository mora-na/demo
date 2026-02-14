<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("menu.title") }}</div>
        <div class="module-sub">{{ t("menu.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-button v-permission="'menu:create'" type="primary" @click="openCreate">{{ t("menu.create") }}</el-button>
        <el-button v-permission="'menu:delete'" v-if="selectedMenuIds.length" type="danger" @click="removeMenus">
          {{ t("menu.filter.delete") }}
        </el-button>
      </div>
    </div>

    <el-table
        v-loading="loading"
        :data="menus"
        row-key="id"
        size="small"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="46"/>
      <el-table-column :label="t('menu.table.name')" min-width="140" prop="name"/>
      <el-table-column :label="t('menu.table.code')" min-width="120" prop="code"/>
      <el-table-column :label="t('menu.table.parent')" min-width="120">
        <template #default="{row}">
          {{ parentName(row.parentId) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('menu.table.path')" min-width="160" prop="path"/>
      <el-table-column :label="t('menu.table.permission')" min-width="160" prop="permission"/>
      <el-table-column :label="t('menu.table.sort')" prop="sort" width="80"/>
      <el-table-column :label="t('menu.table.status')" width="100">
        <template #default="{row}">
          <el-switch
              v-permission="'menu:disable'"
              :active-value="1"
              :inactive-value="0"
              :model-value="row.status ?? 0"
              @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('menu.table.action')" width="140">
        <template #default="{row}">
          <el-button v-permission="'menu:update'" size="small" text @click="openEdit(row)">{{ t("menu.table.edit") }}</el-button>
          <el-button v-permission="'menu:delete'" size="small" text type="danger" @click="removeMenu(row)">{{ t("menu.table.delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="700px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="16" class="form-grid">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('menu.dialog.name')">
              <el-input v-model.trim="form.name"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('menu.dialog.code')">
              <el-input v-model.trim="form.code"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('menu.dialog.parent')">
              <el-tree-select
                  v-model="form.parentId"
                  :data="menuTree"
                  :props="{label: 'name', value: 'id', children: 'children'}"
                  :render-after-expand="false"
                  check-strictly
                  clearable
                  :placeholder="t('menu.dialog.parentPlaceholder')"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('menu.dialog.path')">
              <el-input v-model.trim="form.path"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('menu.dialog.component')">
              <el-input v-model.trim="form.component"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('menu.dialog.permission')">
              <el-input v-model.trim="form.permission"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('menu.dialog.sort')">
              <el-input-number v-model="form.sort" :min="0"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('menu.dialog.status')">
              <el-select v-model="form.status" :placeholder="t('menu.dialog.statusPlaceholder')">
                <el-option :value="1" :label="t('menu.dialog.statusEnabled')"/>
                <el-option :value="0" :label="t('menu.dialog.statusDisabled')"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item :label="t('menu.dialog.remark')">
              <el-input v-model.trim="form.remark"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button v-permission="editorMode === 'create' ? 'menu:create' : 'menu:update'" :loading="saving" type="primary" @click="saveMenu">{{ t("common.save") }}</el-button>
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
  createMenu,
  deleteMenu,
  deleteMenus,
  listMenus,
  type MenuVO,
  updateMenu,
  updateMenuStatus
} from "../../api/system";

const menus = ref<MenuVO[]>([]);
const menuTree = ref<TreeNode<MenuVO>[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorMenuId = ref<number | null>(null);
const selectedMenuIds = ref<number[]>([]);
const {t} = useI18n();

const form = reactive({
  name: "",
  code: "",
  parentId: undefined as number | undefined,
  path: "",
  component: "",
  permission: "",
  status: 1,
  sort: 0,
  remark: ""
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("menu.dialog.createTitle") : t("menu.dialog.editTitle")
);

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

function handleSelectionChange(rows: MenuVO[]) {
  selectedMenuIds.value = rows.map((row) => row.id);
}

function parentName(parentId?: number | null) {
  if (!parentId) {
    return "-";
  }
  const match = menus.value.find((item) => item.id === parentId);
  return match?.name || String(parentId);
}

function resetFormState() {
  form.name = "";
  form.code = "";
  form.parentId = undefined;
  form.path = "";
  form.component = "";
  form.permission = "";
  form.status = 1;
  form.sort = 0;
  form.remark = "";
}

async function fetchMenus() {
  loading.value = true;
  try {
    const result = await listMenus();
    if (result?.code === 200 && result.data) {
      menus.value = result.data;
      menuTree.value = buildTree(result.data);
    } else {
      ElMessage.error(result?.message || t("menu.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("menu.msg.loadFailed")));
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editorMode.value = "create";
  editorMenuId.value = null;
  resetFormState();
  editorVisible.value = true;
}

function openEdit(menu: MenuVO) {
  editorMode.value = "edit";
  editorMenuId.value = menu.id;
  resetFormState();
  form.name = menu.name;
  form.code = menu.code || "";
  form.parentId = menu.parentId ?? undefined;
  form.path = menu.path || "";
  form.component = menu.component || "";
  form.permission = menu.permission || "";
  form.status = menu.status ?? 1;
  form.sort = menu.sort ?? 0;
  form.remark = menu.remark || "";
  editorVisible.value = true;
}

async function saveMenu() {
  if (!form.name) {
    ElMessage.warning(t("menu.msg.validateName"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createMenu(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("menu.msg.createSuccess"));
        editorVisible.value = false;
        fetchMenus();
      } else {
        ElMessage.error(result?.message || t("menu.msg.createFailed"));
      }
    } else if (editorMenuId.value != null) {
      const result = await updateMenu(editorMenuId.value, form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("menu.msg.updateSuccess"));
        editorVisible.value = false;
        fetchMenus();
      } else {
        ElMessage.error(result?.message || t("menu.msg.updateFailed"));
      }
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("menu.msg.saveFailed")));
  } finally {
    saving.value = false;
  }
}

async function handleStatusChange(menu: MenuVO, value: number) {
  const previous = menu.status ?? 0;
  menu.status = value;
  try {
    const result = await updateMenuStatus(menu.id, value);
    if (result?.code !== 200) {
      menu.status = previous;
      ElMessage.error(result?.message || t("menu.msg.statusUpdateFailed"));
    }
  } catch (error) {
    menu.status = previous;
    ElMessage.error(getErrorMessage(error, t("menu.msg.statusUpdateFailed")));
  }
}

async function removeMenu(menu: MenuVO) {
  try {
    await ElMessageBox.confirm(
        t("menu.msg.deleteConfirm", {name: menu.name}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteMenu(menu.id);
  if (result?.code === 200) {
    ElMessage.success(t("menu.msg.deleteSuccess"));
    fetchMenus();
  } else {
    ElMessage.error(result?.message || t("menu.msg.deleteFailed"));
  }
}

async function removeMenus() {
  if (!selectedMenuIds.value.length) {
    ElMessage.warning(t("menu.msg.deleteEmpty"));
    return;
  }
  try {
    await ElMessageBox.confirm(
        t("menu.msg.batchDeleteConfirm", {count: selectedMenuIds.value.length}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteMenus(selectedMenuIds.value);
  if (result?.code === 200) {
    ElMessage.success(t("menu.msg.deleteSuccess"));
    fetchMenus();
  } else {
    ElMessage.error(result?.message || t("menu.msg.deleteFailed"));
  }
}

onMounted(fetchMenus);
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
