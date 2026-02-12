<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">部门管理</div>
        <div class="module-sub">维护组织部门与层级结构。</div>
      </div>
      <div class="module-actions">
        <el-button type="primary" @click="openCreate">新增部门</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="depts" row-key="id" size="small">
      <el-table-column label="名称" min-width="140" prop="name"/>
      <el-table-column label="编码" min-width="120" prop="code"/>
      <el-table-column label="父级" min-width="120">
        <template #default="{row}">
          {{ parentName(row.parentId) }}
        </template>
      </el-table-column>
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
        <el-form-item label="父级部门">
          <el-tree-select
              v-model="form.parentId"
              :data="deptTree"
              :props="{label: 'name', value: 'id', children: 'children'}"
              :render-after-expand="false"
              check-strictly
              clearable
              placeholder="请选择"
          />
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
        <el-button :loading="saving" type="primary" @click="saveDept">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {buildTree, type TreeNode} from "../../utils/tree";
import {createDept, type DeptVO, listDepts, updateDept, updateDeptStatus} from "../../api/system";

const depts = ref<DeptVO[]>([]);
const deptTree = ref<TreeNode<DeptVO>[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorDeptId = ref<number | null>(null);

const form = reactive({
  name: "",
  code: "",
  parentId: undefined as number | undefined,
  status: 1,
  sort: 0,
  remark: ""
});

const editorTitle = computed(() => (editorMode.value === "create" ? "新增部门" : "编辑部门"));

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

function parentName(parentId?: number | null) {
  if (!parentId) {
    return "-";
  }
  const match = depts.value.find((item) => item.id === parentId);
  return match?.name || String(parentId);
}

function resetFormState() {
  form.name = "";
  form.code = "";
  form.parentId = undefined;
  form.status = 1;
  form.sort = 0;
  form.remark = "";
}

async function fetchDepts() {
  loading.value = true;
  try {
    const result = await listDepts();
    if (result?.code === 200 && result.data) {
      depts.value = result.data;
      deptTree.value = buildTree(result.data);
    } else {
      ElMessage.error(result?.message || "加载部门失败");
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "加载部门失败"));
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editorMode.value = "create";
  editorDeptId.value = null;
  resetFormState();
  editorVisible.value = true;
}

function openEdit(dept: DeptVO) {
  editorMode.value = "edit";
  editorDeptId.value = dept.id;
  resetFormState();
  form.name = dept.name;
  form.code = dept.code || "";
  form.parentId = dept.parentId ?? undefined;
  form.status = dept.status ?? 1;
  form.sort = dept.sort ?? 0;
  form.remark = dept.remark || "";
  editorVisible.value = true;
}

async function saveDept() {
  if (!form.name) {
    ElMessage.warning("请输入部门名称");
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createDept(form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || "创建成功");
        editorVisible.value = false;
        fetchDepts();
      } else {
        ElMessage.error(result?.message || "创建失败");
      }
    } else if (editorDeptId.value != null) {
      const result = await updateDept(editorDeptId.value, form);
      if (result?.code === 200) {
        ElMessage.success(result?.message || "更新成功");
        editorVisible.value = false;
        fetchDepts();
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

async function handleStatusChange(dept: DeptVO, value: number) {
  const previous = dept.status ?? 0;
  dept.status = value;
  try {
    const result = await updateDeptStatus(dept.id, value);
    if (result?.code !== 200) {
      dept.status = previous;
      ElMessage.error(result?.message || "状态更新失败");
    }
  } catch (error) {
    dept.status = previous;
    ElMessage.error(getErrorMessage(error, "状态更新失败"));
  }
}

onMounted(fetchDepts);
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
