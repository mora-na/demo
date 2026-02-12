<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">系统通知</div>
        <div class="module-sub">发布系统通知并追踪阅读状态。</div>
      </div>
      <div class="module-actions">
        <el-input v-model.trim="filters.keyword" clearable placeholder="标题/内容关键词"/>
        <el-select v-model="filters.scopeType" clearable placeholder="通知范围" style="width: 140px">
          <el-option label="全部" value="ALL"/>
          <el-option label="部门" value="DEPT"/>
          <el-option label="角色" value="ROLE"/>
          <el-option label="用户" value="USER"/>
        </el-select>
        <el-button @click="handleSearch">查询</el-button>
        <el-button type="primary" @click="openPublish">发布通知</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="notices" row-key="id" size="small">
      <el-table-column label="标题" min-width="180" prop="title"/>
      <el-table-column label="范围" width="100">
        <template #default="{row}">
          {{ scopeLabel(row.scopeType) }}
        </template>
      </el-table-column>
      <el-table-column label="已读/总数" width="120">
        <template #default="{row}">
          {{ readSummary(row) }}
        </template>
      </el-table-column>
      <el-table-column label="发布人" prop="createdName" width="120"/>
      <el-table-column label="发布时间" width="170">
        <template #default="{row}">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{row}">
          <el-button size="small" text @click="openDetail(row)">详情</el-button>
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

    <el-dialog v-model="publishVisible" align-center title="发布系统通知" width="540px">
      <el-form :model="publishForm" label-position="top">
        <el-form-item label="通知标题">
          <el-input v-model.trim="publishForm.title" placeholder="请输入通知标题"/>
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input
              v-model.trim="publishForm.content"
              :rows="4"
              placeholder="请输入通知内容"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="通知范围">
          <el-select v-model="publishForm.scopeType" placeholder="请选择范围">
            <el-option label="全部" value="ALL"/>
            <el-option label="部门" value="DEPT"/>
            <el-option label="角色" value="ROLE"/>
            <el-option label="用户" value="USER"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="publishForm.scopeType !== 'ALL'" :label="scopeTargetLabel">
          <el-select
              v-model="publishForm.scopeIds"
              :placeholder="scopePlaceholder"
              filterable
              multiple
          >
            <el-option
                v-for="option in scopeOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button :loading="publishing" type="primary" @click="submitPublish">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" align-center title="通知详情" width="680px">
      <div class="detail-meta">
        <div class="detail-title">{{ detailNotice?.title || "-" }}</div>
        <div class="detail-sub">
          <span>范围：{{ scopeLabel(detailNotice?.scopeType) }}</span>
          <span>发布人：{{ detailNotice?.createdName || "-" }}</span>
          <span>发布时间：{{ formatDateTime(detailNotice?.createdAt) }}</span>
        </div>
        <div class="detail-content">{{ detailNotice?.content || "" }}</div>
      </div>
      <el-divider/>
      <el-table v-loading="detailLoading" :data="recipients" row-key="id" size="small">
        <el-table-column label="用户名" min-width="140" prop="userName"/>
        <el-table-column label="昵称" min-width="140" prop="nickName"/>
        <el-table-column label="部门" width="140">
          <template #default="{row}">
            {{ deptName(row.deptId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            {{ row.readStatus === 1 ? "已读" : "未读" }}
          </template>
        </el-table-column>
        <el-table-column label="阅读时间" width="170">
          <template #default="{row}">
            {{ formatDateTime(row.readTime) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref, watch} from "vue";
import {ElMessage} from "element-plus";
import {
  type DeptVO,
  listDepts,
  listNoticeRecipients,
  listNotices,
  listRoles,
  listUsers,
  type NoticePublishPayload,
  type NoticeRecipientVO,
  type NoticeVO,
  publishNotice,
  type RoleVO,
  type UserVO
} from "../../api/system";

const notices = ref<NoticeVO[]>([]);
const recipients = ref<NoticeRecipientVO[]>([]);
const loading = ref(false);
const detailLoading = ref(false);
const publishing = ref(false);
const publishVisible = ref(false);
const detailVisible = ref(false);
const detailNotice = ref<NoticeVO | null>(null);

const filters = reactive({
  keyword: "",
  scopeType: ""
});

const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const publishForm = reactive<NoticePublishPayload>({
  title: "",
  content: "",
  scopeType: "ALL",
  scopeIds: []
});

const depts = ref<DeptVO[]>([]);
const roles = ref<RoleVO[]>([]);
const users = ref<UserVO[]>([]);

const scopeTargetLabel = computed(() => {
  if (publishForm.scopeType === "DEPT") {
    return "接收部门";
  }
  if (publishForm.scopeType === "ROLE") {
    return "接收角色";
  }
  if (publishForm.scopeType === "USER") {
    return "接收用户";
  }
  return "接收对象";
});

const scopePlaceholder = computed(() => {
  if (publishForm.scopeType === "DEPT") {
    return "请选择部门";
  }
  if (publishForm.scopeType === "ROLE") {
    return "请选择角色";
  }
  if (publishForm.scopeType === "USER") {
    return "请选择用户";
  }
  return "请选择";
});

const scopeOptions = computed(() => {
  if (publishForm.scopeType === "DEPT") {
    return depts.value.map((dept) => ({label: dept.name, value: dept.id}));
  }
  if (publishForm.scopeType === "ROLE") {
    return roles.value.map((role) => ({label: role.name, value: role.id}));
  }
  if (publishForm.scopeType === "USER") {
    return users.value.map((user) => ({label: userLabel(user), value: user.id}));
  }
  return [];
});

function scopeLabel(scopeType?: string) {
  switch (scopeType) {
    case "ALL":
      return "全部";
    case "DEPT":
      return "部门";
    case "ROLE":
      return "角色";
    case "USER":
      return "用户";
    default:
      return scopeType || "-";
  }
}

function readSummary(row: NoticeVO) {
  const totalCount = row.totalCount ?? 0;
  const readCount = row.readCount ?? 0;
  return `${readCount}/${totalCount}`;
}

function formatDateTime(value?: string) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const pad = (num: number) => String(num).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
      date.getHours()
  )}:${pad(date.getMinutes())}`;
}

function deptName(deptId?: number | null) {
  if (!deptId) {
    return "-";
  }
  return depts.value.find((dept) => dept.id === deptId)?.name || "-";
}

function userLabel(user: UserVO) {
  const nick = user.nickName ? ` (${user.nickName})` : "";
  return `${user.userName}${nick}`;
}

async function loadNotices() {
  if (loading.value) {
    return;
  }
  loading.value = true;
  try {
    const result = await listNotices({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      scopeType: filters.scopeType || undefined
    });
    if (result?.code === 200 && result.data) {
      notices.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || "加载通知失败");
    }
  } catch (error) {
    ElMessage.error("加载通知失败");
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  loadNotices();
}

function handlePageChange(value: number) {
  pageNum.value = value;
  loadNotices();
}

function handleSizeChange(value: number) {
  pageSize.value = value;
  pageNum.value = 1;
  loadNotices();
}

function resetPublishForm() {
  publishForm.title = "";
  publishForm.content = "";
  publishForm.scopeType = "ALL";
  publishForm.scopeIds = [];
}

function openPublish() {
  resetPublishForm();
  publishVisible.value = true;
}

async function ensureDepts() {
  if (depts.value.length) {
    return;
  }
  const result = await listDepts();
  if (result?.code === 200 && result.data) {
    depts.value = result.data;
  }
}

async function ensureRoles() {
  if (roles.value.length) {
    return;
  }
  const result = await listRoles();
  if (result?.code === 200 && result.data) {
    roles.value = result.data;
  }
}

async function ensureUsers() {
  if (users.value.length) {
    return;
  }
  const result = await listUsers({pageNum: 1, pageSize: 200});
  if (result?.code === 200 && result.data) {
    users.value = result.data.data || [];
  }
}

async function submitPublish() {
  if (!publishForm.title.trim()) {
    ElMessage.warning("请输入通知标题");
    return;
  }
  if (!publishForm.content.trim()) {
    ElMessage.warning("请输入通知内容");
    return;
  }
  if (publishForm.scopeType !== "ALL" && (!publishForm.scopeIds || !publishForm.scopeIds.length)) {
    ElMessage.warning("请选择接收对象");
    return;
  }
  publishing.value = true;
  try {
    const result = await publishNotice({
      title: publishForm.title.trim(),
      content: publishForm.content.trim(),
      scopeType: publishForm.scopeType,
      scopeIds: publishForm.scopeType === "ALL" ? undefined : publishForm.scopeIds
    });
    if (result?.code === 200) {
      ElMessage.success("通知发布成功");
      publishVisible.value = false;
      loadNotices();
    } else {
      ElMessage.error(result?.message || "通知发布失败");
    }
  } catch (error) {
    ElMessage.error("通知发布失败");
  } finally {
    publishing.value = false;
  }
}

async function openDetail(row: NoticeVO) {
  detailVisible.value = true;
  detailNotice.value = row;
  recipients.value = [];
  detailLoading.value = true;
  try {
    await ensureDepts();
    const result = await listNoticeRecipients(row.id);
    if (result?.code === 200 && result.data) {
      recipients.value = result.data;
    } else {
      ElMessage.error(result?.message || "加载详情失败");
    }
  } catch (error) {
    ElMessage.error("加载详情失败");
  } finally {
    detailLoading.value = false;
  }
}

watch(
    () => publishForm.scopeType,
    (value) => {
      publishForm.scopeIds = [];
      if (value === "DEPT") {
        ensureDepts();
      } else if (value === "ROLE") {
        ensureRoles();
      } else if (value === "USER") {
        ensureUsers();
      }
    }
);

onMounted(loadNotices);
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
  width: 160px;
  flex: 0 0 auto;
}

.module-actions :deep(.el-button) {
  flex: 0 0 auto;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
}

.detail-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
}

.detail-sub {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--muted);
}

.detail-content {
  padding: 10px 12px;
  background: rgba(18, 18, 18, 0.04);
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
}
</style>
