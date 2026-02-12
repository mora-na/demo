<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">菜单管理</div>
        <div class="module-sub">维护系统菜单与前端路由配置。</div>
      </div>
      <div class="module-actions">
        <el-button type="primary" @click="openCreate">新增菜单</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="menus" row-key="id" size="small">
      <el-table-column label="名称" min-width="140" prop="name"/>
      <el-table-column label="编码" min-width="120" prop="code"/>
      <el-table-column label="父级" min-width="120">
        <template #default="{row}">
          {{ parentName(row.parentId) }}
        </template>
      </el-table-column>
      <el-table-column label="路径" min-width="160" prop="path"/>
      <el-table-column label="权限" min-width="160" prop="permission"/>
      <el-table-column label="排序" prop="sort" width="80"/>
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
      <el-table-column label="操作" width="140">
        <template #default="{row}">
          <el-button size="small" text @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="520px">
      <el-form :model="form" label-position="top">
        <el-form-item label="名称">
          <el-input v-model.trim="form.name"/>
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model.trim="form.code"/>
        </el-form-item>
        <el-form-item label="父级菜单">
          <el-tree-select
              v-model="form.parentId"
              :data="menuTree"
              :props="{label: 'name', value: 'id', children: 'children'}"
              :render-after-expand="false"
              check-strictly
              clearable
              placeholder="请选择"
          />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model.trim="form.path"/>
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model.trim="form.component"/>
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model.trim="form.permission"/>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option :value="1" label="启用"/>
            <el-option :value="0" label="禁用"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="form.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="saveMenu">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {buildTree, type TreeNode} from "../../utils/tree";
import {createMenu, listMenus, type MenuVO, updateMenu, updateMenuStatus} from "../../api/system";

const menus = ref<MenuVO[]>([]);
const menuTree = ref<TreeNode<MenuVO>[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorMenuId = ref<number | null>(null);

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

const editorTitle = computed(() => (editorMode.value === "create" ? "新增菜单" : "编辑菜单"));

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
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
      ElMessage.error(result?.message || "加载菜单失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "加载菜单失败"));
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
    ElMessage.warning("请输入菜单名称");
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createMenu(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || "创建成功");
        editorVisible.value = false;
        fetchMenus();
      } else {
        ElMessage.error(result?.message || "创建失败");
      }
    } else if (editorMenuId.value != null) {
      const result = await updateMenu(editorMenuId.value, form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || "更新成功");
        editorVisible.value = false;
        fetchMenus();
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

async function handleStatusChange(menu: MenuVO, value: number) {
  const previous = menu.status ?? 0;
  menu.status = value;
  try {
    const result = await updateMenuStatus(menu.id, value);
    if (result?.code !== 200) {
      menu.status = previous;
      ElMessage.error(result?.message || "状态更新失败");
    }
  } catch (error) {
    menu.status = previous;
    ElMessage.error(getErrorMessage(error, "状态更新失败"));
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
</style>
