<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("post.title") }}</div>
        <div class="module-sub">{{ t("post.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-button type="primary" @click="openCreate">{{ t("post.create") }}</el-button>
        <el-button v-if="selectedPostIds.length" type="danger" @click="removePosts">
          {{ t("post.filter.delete") }}
        </el-button>
      </div>
    </div>

    <el-table
        v-loading="loading"
        :data="posts"
        row-key="id"
        size="small"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="46"/>
      <el-table-column :label="t('post.table.name')" min-width="140" prop="name"/>
      <el-table-column :label="t('post.table.code')" min-width="120" prop="code"/>
      <el-table-column :label="t('post.table.dept')" min-width="140">
        <template #default="{row}">
          {{ deptName(row.deptId) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('post.table.sort')" prop="sort" width="80"/>
      <el-table-column :label="t('post.table.status')" width="100">
        <template #default="{row}">
          <el-switch
              :active-value="1"
              :inactive-value="0"
              :model-value="row.status ?? 0"
              @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('post.table.action')" width="180">
        <template #default="{row}">
          <el-button size="small" text @click="openEdit(row)">{{ t("post.table.edit") }}</el-button>
          <el-button size="small" text type="danger" @click="removePost(row)">
            {{ t("post.table.delete") }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="620px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="16" class="form-grid">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('post.dialog.name')">
              <el-input v-model.trim="form.name"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('post.dialog.code')">
              <el-input v-model.trim="form.code"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('post.dialog.dept')">
              <el-select v-model="form.deptId" :placeholder="t('post.dialog.deptPlaceholder')">
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
            <el-form-item :label="t('post.dialog.sort')">
              <el-input-number v-model="form.sort" :min="0"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="t('post.dialog.status')">
              <el-select v-model="form.status" :placeholder="t('post.dialog.statusPlaceholder')">
                <el-option :value="1" :label="t('post.dialog.statusEnabled')"/>
                <el-option :value="0" :label="t('post.dialog.statusDisabled')"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item :label="t('post.dialog.remark')">
              <el-input v-model.trim="form.remark"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="saving" type="primary" @click="savePost">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  createPost,
  deletePost,
  deletePosts,
  type DeptVO,
  listDepts,
  listPosts,
  type PostVO,
  updatePost,
  updatePostStatus
} from "../../api/system";

const posts = ref<PostVO[]>([]);
const depts = ref<DeptVO[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorPostId = ref<number | null>(null);
const selectedPostIds = ref<number[]>([]);
const {t} = useI18n();

const form = reactive({
  name: "",
  code: "",
  deptId: undefined as number | undefined,
  status: 1,
  sort: 0,
  remark: ""
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("post.dialog.createTitle") : t("post.dialog.editTitle")
);

function getErrorMessage(error: unknown, fallback: string): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string };
  return err?.response?.data?.message || err?.message || fallback;
}

function handleSelectionChange(rows: PostVO[]) {
  selectedPostIds.value = rows.map((row) => row.id);
}

function deptName(deptId?: number | null) {
  if (!deptId) {
    return "-";
  }
  const match = depts.value.find((item) => item.id === deptId);
  return match?.name || String(deptId);
}

function resetFormState() {
  form.name = "";
  form.code = "";
  form.deptId = undefined;
  form.status = 1;
  form.sort = 0;
  form.remark = "";
}

async function fetchPosts() {
  loading.value = true;
  try {
    const result = await listPosts();
    if (result?.code === 200 && result.data) {
      posts.value = result.data;
    } else {
      ElMessage.error(result?.message || t("post.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("post.msg.loadFailed")));
  } finally {
    loading.value = false;
  }
}

async function fetchDepts() {
  const result = await listDepts();
  if (result?.code === 200 && result.data) {
    depts.value = result.data;
  }
}

function openCreate() {
  if (!depts.value.length) {
    fetchDepts();
  }
  editorMode.value = "create";
  editorPostId.value = null;
  resetFormState();
  editorVisible.value = true;
}

function openEdit(post: PostVO) {
  if (!depts.value.length) {
    fetchDepts();
  }
  editorMode.value = "edit";
  editorPostId.value = post.id;
  resetFormState();
  form.name = post.name;
  form.code = post.code || "";
  form.deptId = post.deptId ?? undefined;
  form.status = post.status ?? 1;
  form.sort = post.sort ?? 0;
  form.remark = post.remark || "";
  editorVisible.value = true;
}

async function removePost(post: PostVO) {
  try {
    await ElMessageBox.confirm(
        t("post.msg.deleteConfirm", {name: post.name}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deletePost(post.id);
  if (result?.code === 200) {
    ElMessage.success(t("post.msg.deleteSuccess"));
    fetchPosts();
  } else {
    ElMessage.error(result?.message || t("post.msg.deleteFailed"));
  }
}

async function removePosts() {
  if (!selectedPostIds.value.length) {
    ElMessage.warning(t("post.msg.deleteEmpty"));
    return;
  }
  try {
    await ElMessageBox.confirm(
        t("post.msg.batchDeleteConfirm", {count: selectedPostIds.value.length}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deletePosts(selectedPostIds.value);
  if (result?.code === 200) {
    ElMessage.success(t("post.msg.deleteSuccess"));
    fetchPosts();
  } else {
    ElMessage.error(result?.message || t("post.msg.deleteFailed"));
  }
}

async function savePost() {
  if (!form.name) {
    ElMessage.warning(t("post.msg.validateName"));
    return;
  }
  if (!form.deptId) {
    ElMessage.warning(t("post.dialog.deptPlaceholder"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createPost({
        name: form.name,
        code: form.code || undefined,
        deptId: form.deptId,
        status: form.status,
        sort: form.sort,
        remark: form.remark
      });
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("post.msg.createSuccess"));
        editorVisible.value = false;
        fetchPosts();
      } else {
        ElMessage.error(result?.message || t("post.msg.createFailed"));
      }
    } else if (editorPostId.value != null) {
      const result = await updatePost(editorPostId.value, {
        name: form.name,
        code: form.code || undefined,
        deptId: form.deptId,
        status: form.status,
        sort: form.sort,
        remark: form.remark
      });
      if (result?.code === 200) {
        ElMessage.success(result?.message || t("post.msg.updateSuccess"));
        editorVisible.value = false;
        fetchPosts();
      } else {
        ElMessage.error(result?.message || t("post.msg.updateFailed"));
      }
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, t("post.msg.saveFailed")));
  } finally {
    saving.value = false;
  }
}

async function handleStatusChange(row: PostVO, value: number) {
  const previous = row.status ?? 0;
  row.status = value;
  try {
    const result = await updatePostStatus(row.id, value);
    if (result?.code !== 200) {
      row.status = previous;
      ElMessage.error(result?.message || t("post.msg.statusUpdateFailed"));
    }
  } catch (error) {
    row.status = previous;
    ElMessage.error(getErrorMessage(error, t("post.msg.statusUpdateFailed")));
  }
}

onMounted(() => {
  fetchDepts();
  fetchPosts();
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
  gap: 8px;
  align-items: center;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
}

.form-grid :deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>
