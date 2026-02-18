<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("notice.title") }}</div>
        <div class="module-sub">{{ t("notice.subtitle") }}</div>
      </div>
      <div class="module-actions" @keyup.enter="handleSearch">
        <el-input v-model.trim="filters.keyword" clearable :placeholder="t('notice.filter.keywordPlaceholder')"/>
        <el-select v-model="filters.scopeType" clearable :placeholder="t('notice.filter.scopePlaceholder')" style="width: 140px">
          <el-option :label="t('notice.scope.all')" value="ALL"/>
          <el-option :label="t('notice.scope.dept')" value="DEPT"/>
          <el-option :label="t('notice.scope.role')" value="ROLE"/>
          <el-option :label="t('notice.scope.user')" value="USER"/>
        </el-select>
        <el-button @click="handleSearch">{{ t("notice.filter.search") }}</el-button>
        <el-button v-permission="'notice:publish'" type="primary" @click="openPublish">{{ t("notice.filter.publish") }}</el-button>
        <el-button v-permission="'notice:delete'" v-if="selectedNoticeIds.length" type="danger" @click="removeNotices">
          {{ t("notice.filter.delete") }}
        </el-button>
      </div>
    </div>

    <el-table
        v-loading="loading"
        :data="notices"
        row-key="id"
        size="small"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="46"/>
      <el-table-column :label="t('notice.table.title')" min-width="180" prop="title"/>
      <el-table-column :label="t('notice.table.scope')" width="100">
        <template #default="{row}">
          {{ scopeLabel(row.scopeType) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('notice.table.readSummary')" width="120">
        <template #default="{row}">
          {{ readSummary(row) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('notice.table.publisher')" prop="createdName" width="120"/>
      <el-table-column :label="t('notice.table.publishTime')" width="170">
        <template #default="{row}">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('notice.table.action')" width="200">
        <template #default="{row}">
          <el-button size="small" text @click="openDetail(row)">{{ t("notice.table.detail") }}</el-button>
          <el-button v-permission="'notice:delete'" size="small" text type="danger" @click="removeNotice(row)">{{ t("notice.table.delete") }}</el-button>
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

    <el-dialog v-model="publishVisible" align-center :title="t('notice.publish.title')" width="540px">
      <el-form :model="publishForm" label-position="top">
        <el-form-item :label="t('notice.publish.titleLabel')">
          <el-input v-model.trim="publishForm.title" :placeholder="t('notice.publish.titlePlaceholder')"/>
        </el-form-item>
        <el-form-item :label="t('notice.publish.contentLabel')">
          <el-input
              v-model.trim="publishForm.content"
              :rows="4"
              :placeholder="t('notice.publish.contentPlaceholder')"
              type="textarea"
          />
        </el-form-item>
        <el-form-item :label="t('notice.publish.scopeLabel')">
          <el-select v-model="publishForm.scopeType" :placeholder="t('notice.publish.scopePlaceholder')">
            <el-option :label="t('notice.scope.all')" value="ALL"/>
            <el-option :label="t('notice.scope.dept')" value="DEPT"/>
            <el-option :label="t('notice.scope.role')" value="ROLE"/>
            <el-option :label="t('notice.scope.user')" value="USER"/>
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
        <el-button @click="publishVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button v-permission="'notice:publish'" :loading="publishing" type="primary" @click="submitPublish">{{ t("notice.publish.publish") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" align-center :title="t('notice.detail.title')" width="680px">
      <div class="detail-meta">
        <div class="detail-title">{{ detailNotice?.title || "-" }}</div>
        <div class="detail-sub">
          <span>{{ t("notice.detail.scopeLabel") }}{{ scopeLabel(detailNotice?.scopeType) }}</span>
          <span>{{ t("notice.detail.publisherLabel") }}{{ detailNotice?.createdName || "-" }}</span>
          <span>{{ t("notice.detail.publishTimeLabel") }}{{ formatDateTime(detailNotice?.createdAt) }}</span>
        </div>
        <div class="detail-content">{{ detailNotice?.content || "" }}</div>
      </div>
      <el-divider/>
      <el-table v-loading="detailLoading" :data="recipients" row-key="id" size="small">
        <el-table-column :label="t('notice.detail.userName')" min-width="140" prop="userName"/>
        <el-table-column :label="t('notice.detail.nickName')" min-width="140" prop="nickName"/>
        <el-table-column :label="t('notice.detail.dept')" width="140">
          <template #default="{row}">
            {{ deptName(row.deptId) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('notice.detail.status')" width="100">
          <template #default="{row}">
            {{ row.readStatus === 1 ? t("notice.detail.statusRead") : t("notice.detail.statusUnread") }}
          </template>
        </el-table-column>
        <el-table-column :label="t('notice.detail.readTime')" width="170">
          <template #default="{row}">
            {{ formatDateTime(row.readTime) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">{{ t("notice.detail.close") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref, watch} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {useAuthStore} from "../../stores/auth";
import {
  deleteNotice,
  deleteNotices,
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
const {t} = useI18n();
const authStore = useAuthStore();

const filters = reactive({
  keyword: "",
  scopeType: ""
});

const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const selectedNoticeIds = ref<number[]>([]);

const publishForm = reactive<NoticePublishPayload>({
  title: "",
  content: "",
  scopeType: "ALL",
  scopeIds: []
});

const depts = ref<DeptVO[]>([]);
const roles = ref<RoleVO[]>([]);
const users = ref<UserVO[]>([]);

const deptFallbackOptions = computed(() => {
  const deptId = authStore.profile?.deptId;
  if (!deptId) {
    return [];
  }
  const label = authStore.profile?.deptName || `#${deptId}`;
  return [{label, value: deptId}];
});

const roleFallbackOptions = computed(() => {
  if (!authStore.roleTargets?.length) {
    return [];
  }
  return authStore.roleTargets
      .filter((role) => role?.id != null)
      .map((role) => ({
        label: role.name || role.code || `#${role.id}`,
        value: role.id
      }));
});

const selfUserFallbackOptions = computed(() => {
  const profile = authStore.profile;
  if (!profile?.id) {
    return [];
  }
  const nick = profile.nickName ? ` (${profile.nickName})` : "";
  return [{label: `${profile.userName}${nick}`, value: profile.id}];
});

const scopeTargetLabel = computed(() => {
  if (publishForm.scopeType === "DEPT") {
    return t("notice.publish.targetDept");
  }
  if (publishForm.scopeType === "ROLE") {
    return t("notice.publish.targetRole");
  }
  if (publishForm.scopeType === "USER") {
    return t("notice.publish.targetUser");
  }
  return t("notice.publish.targetDefault");
});

const scopePlaceholder = computed(() => {
  if (publishForm.scopeType === "DEPT") {
    return t("notice.publish.targetDeptPlaceholder");
  }
  if (publishForm.scopeType === "ROLE") {
    return t("notice.publish.targetRolePlaceholder");
  }
  if (publishForm.scopeType === "USER") {
    return t("notice.publish.targetUserPlaceholder");
  }
  return t("notice.publish.targetDefaultPlaceholder");
});

const scopeOptions = computed(() => {
  if (publishForm.scopeType === "DEPT") {
    const options = depts.value.map((dept) => ({label: dept.name, value: dept.id}));
    return options.length ? options : deptFallbackOptions.value;
  }
  if (publishForm.scopeType === "ROLE") {
    const options = roles.value.map((role) => ({label: role.name, value: role.id}));
    return options.length ? options : roleFallbackOptions.value;
  }
  if (publishForm.scopeType === "USER") {
    const options = users.value.map((user) => ({label: userLabel(user), value: user.id}));
    return options.length ? options : selfUserFallbackOptions.value;
  }
  return [];
});

function scopeLabel(scopeType?: string) {
  switch (scopeType) {
    case "ALL":
      return t("notice.scope.all");
    case "DEPT":
      return t("notice.scope.dept");
    case "ROLE":
      return t("notice.scope.role");
    case "USER":
      return t("notice.scope.user");
    default:
      return scopeType || "-";
  }
}

function readSummary(row: NoticeVO) {
  const totalCount = row.totalCount ?? 0;
  const readCount = row.readCount ?? 0;
  return `${readCount}/${totalCount}`;
}

function handleSelectionChange(rows: NoticeVO[]) {
  selectedNoticeIds.value = rows.map((row) => row.id);
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
      ElMessage.error(result?.message || t("notice.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("notice.msg.loadFailed"));
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
    ElMessage.warning(t("notice.msg.validateTitle"));
    return;
  }
  if (!publishForm.content.trim()) {
    ElMessage.warning(t("notice.msg.validateContent"));
    return;
  }
  if (publishForm.scopeType !== "ALL" && (!publishForm.scopeIds || !publishForm.scopeIds.length)) {
    ElMessage.warning(t("notice.msg.validateTarget"));
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
      ElMessage.success(t("notice.msg.publishSuccess"));
      publishVisible.value = false;
      loadNotices();
    } else {
      ElMessage.error(result?.message || t("notice.msg.publishFailed"));
    }
  } catch (error) {
    ElMessage.error(t("notice.msg.publishFailed"));
  } finally {
    publishing.value = false;
  }
}

async function removeNotice(row: NoticeVO) {
  try {
    await ElMessageBox.confirm(
        t("notice.msg.deleteConfirm", {title: row.title}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteNotice(row.id);
  if (result?.code === 200) {
    ElMessage.success(t("notice.msg.deleteSuccess"));
    loadNotices();
  } else {
    ElMessage.error(result?.message || t("notice.msg.deleteFailed"));
  }
}

async function removeNotices() {
  if (!selectedNoticeIds.value.length) {
    ElMessage.warning(t("notice.msg.deleteEmpty"));
    return;
  }
  try {
    await ElMessageBox.confirm(
        t("notice.msg.batchDeleteConfirm", {count: selectedNoticeIds.value.length}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteNotices(selectedNoticeIds.value);
  if (result?.code === 200) {
    ElMessage.success(t("notice.msg.deleteSuccess"));
    loadNotices();
  } else {
    ElMessage.error(result?.message || t("notice.msg.deleteFailed"));
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
      ElMessage.error(result?.message || t("notice.msg.detailLoadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("notice.msg.detailLoadFailed"));
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

onMounted(async () => {
  if (!authStore.profileLoaded) {
    await authStore.loadProfile();
  }
  loadNotices();
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
