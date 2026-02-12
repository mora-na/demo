<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">定时任务</div>
        <div class="module-sub">管理系统内的定时任务与执行策略。</div>
      </div>
      <div class="module-actions">
        <el-input v-model.trim="filters.name" clearable placeholder="任务名称"/>
        <el-input v-model.trim="filters.handlerName" clearable placeholder="处理器"/>
        <el-select v-model="filters.status" clearable placeholder="状态" style="width: 120px">
          <el-option :value="1" label="启用"/>
          <el-option :value="0" label="停用"/>
        </el-select>
        <el-button @click="handleSearch">查询</el-button>
        <el-button type="primary" @click="openCreate">新增任务</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="jobs" row-key="id" size="small">
      <el-table-column label="任务名称" min-width="160" prop="name"/>
      <el-table-column label="处理器" min-width="160" prop="handlerName"/>
      <el-table-column label="Cron" min-width="160" prop="cronExpression"/>
      <el-table-column label="下一次执行" width="170">
        <template #default="{row}">
          {{ formatDateTime(row.nextFireTime) }}
        </template>
      </el-table-column>
      <el-table-column label="并发" width="80">
        <template #default="{row}">
          {{ row.allowConcurrent === 0 ? "否" : "是" }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
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
          <el-button size="small" text @click="runOnce(row)">立即执行</el-button>
          <el-button size="small" text @click="openLogs(row)">执行记录</el-button>
          <el-button size="small" text @click="removeJob(row)">删除</el-button>
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
        <el-form-item label="任务名称">
          <el-input v-model.trim="form.name" placeholder="请输入任务名称"/>
        </el-form-item>
        <el-form-item label="处理器">
          <el-select v-model="form.handlerName" filterable placeholder="请选择处理器">
            <el-option
                v-for="handler in handlers"
                :key="handler.name"
                :label="handler.name"
                :value="handler.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Cron 表达式">
          <el-input v-model.trim="form.cronExpression" placeholder="例如：0 0/5 * * * ?"/>
        </el-form-item>
        <el-form-item label="误触发策略">
          <el-select v-model="form.misfirePolicy" placeholder="请选择策略">
            <el-option label="默认" value="DEFAULT"/>
            <el-option label="忽略错过" value="IGNORE_MISFIRE"/>
            <el-option label="立即执行" value="FIRE_AND_PROCEED"/>
            <el-option label="跳过执行" value="DO_NOTHING"/>
          </el-select>
        </el-form-item>
        <el-form-item label="允许并发">
          <el-select v-model="form.allowConcurrent" placeholder="请选择">
            <el-option :value="1" label="允许"/>
            <el-option :value="0" label="禁止"/>
          </el-select>
        </el-form-item>
        <el-form-item label="任务范围">
          <el-select v-model="form.targetType" placeholder="请选择范围">
            <el-option label="全部" value="ALL"/>
            <el-option label="部门" value="DEPT"/>
            <el-option label="角色" value="ROLE"/>
            <el-option label="用户" value="USER"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.targetType !== 'ALL'" :label="scopeTargetLabel">
          <el-select v-model="form.targetIds" :placeholder="scopePlaceholder" filterable multiple>
            <el-option
                v-for="option in scopeOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option :value="1" label="启用"/>
            <el-option :value="0" label="停用"/>
          </el-select>
        </el-form-item>
        <el-form-item label="参数">
          <el-input v-model.trim="form.params" :rows="3" placeholder="可选，JSON或字符串" type="textarea"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="form.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="saveJob">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logVisible" align-center title="执行记录" width="720px">
      <el-table v-loading="logLoading" :data="logs" row-key="id" size="small">
        <el-table-column label="任务" min-width="160" prop="jobName"/>
        <el-table-column label="处理器" min-width="140" prop="handlerName"/>
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            {{ row.status === 1 ? "成功" : "失败" }}
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{row}">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="耗时(ms)" width="100">
          <template #default="{row}">
            {{ row.durationMs ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="信息" min-width="200" prop="message"/>
      </el-table>
      <div class="module-footer">
        <el-pagination
            :current-page="logPageNum"
            :page-size="logPageSize"
            :total="logTotal"
            layout="total, sizes, prev, pager, next"
            @current-change="handleLogPageChange"
            @size-change="handleLogSizeChange"
        />
      </div>
      <template #footer>
        <el-button @click="logVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref, watch} from "vue";
import {ElMessage} from "element-plus";
import {
  createJob,
  deleteJob,
  type DeptVO,
  type JobCreatePayload,
  type JobHandlerInfo,
  type JobLogVO,
  type JobUpdatePayload,
  type JobVO,
  listDepts,
  listJobHandlers,
  listJobLogs,
  listJobs,
  listRoles,
  listUsers,
  type RoleVO,
  runJob,
  updateJob,
  updateJobStatus,
  type UserVO
} from "../../api/system";

const jobs = ref<JobVO[]>([]);
const handlers = ref<JobHandlerInfo[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorId = ref<number | null>(null);

const logs = ref<JobLogVO[]>([]);
const logVisible = ref(false);
const logLoading = ref(false);
const logPageNum = ref(1);
const logPageSize = ref(10);
const logTotal = ref(0);
const activeLogJobId = ref<number | null>(null);

const filters = reactive({
  name: "",
  handlerName: "",
  status: undefined as number | undefined
});

const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const form = reactive<JobCreatePayload & JobUpdatePayload>({
  name: "",
  handlerName: "",
  cronExpression: "",
  status: 1,
  allowConcurrent: 1,
  misfirePolicy: "DEFAULT",
  targetType: "ALL",
  targetIds: [],
  params: "",
  remark: ""
});

const depts = ref<DeptVO[]>([]);
const roles = ref<RoleVO[]>([]);
const users = ref<UserVO[]>([]);

const editorTitle = computed(() => (editorMode.value === "create" ? "新增任务" : "编辑任务"));

const scopeTargetLabel = computed(() => {
  if (form.targetType === "DEPT") {
    return "接收部门";
  }
  if (form.targetType === "ROLE") {
    return "接收角色";
  }
  if (form.targetType === "USER") {
    return "接收用户";
  }
  return "接收对象";
});

const scopePlaceholder = computed(() => {
  if (form.targetType === "DEPT") {
    return "请选择部门";
  }
  if (form.targetType === "ROLE") {
    return "请选择角色";
  }
  if (form.targetType === "USER") {
    return "请选择用户";
  }
  return "请选择";
});

const scopeOptions = computed(() => {
  if (form.targetType === "DEPT") {
    return depts.value.map((dept) => ({label: dept.name, value: dept.id}));
  }
  if (form.targetType === "ROLE") {
    return roles.value.map((role) => ({label: role.name, value: role.id}));
  }
  if (form.targetType === "USER") {
    return users.value.map((user) => ({label: userLabel(user), value: user.id}));
  }
  return [];
});

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

function userLabel(user: UserVO) {
  const nick = user.nickName ? ` (${user.nickName})` : "";
  return `${user.userName}${nick}`;
}

async function loadHandlers() {
  const result = await listJobHandlers();
  if (result?.code === 200 && result.data) {
    handlers.value = result.data;
  }
}

async function loadJobs() {
  if (loading.value) {
    return;
  }
  loading.value = true;
  try {
    const result = await listJobs({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: filters.name || undefined,
      handlerName: filters.handlerName || undefined,
      status: filters.status
    });
    if (result?.code === 200 && result.data) {
      jobs.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || "加载任务失败");
    }
  } catch (error) {
    ElMessage.error("加载任务失败");
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  loadJobs();
}

function handlePageChange(value: number) {
  pageNum.value = value;
  loadJobs();
}

function handleSizeChange(value: number) {
  pageSize.value = value;
  pageNum.value = 1;
  loadJobs();
}

function resetForm() {
  form.name = "";
  form.handlerName = "";
  form.cronExpression = "";
  form.status = 1;
  form.allowConcurrent = 1;
  form.misfirePolicy = "DEFAULT";
  form.targetType = "ALL";
  form.targetIds = [];
  form.params = "";
  form.remark = "";
}

function openCreate() {
  editorMode.value = "create";
  editorId.value = null;
  resetForm();
  editorVisible.value = true;
}

function openEdit(row: JobVO) {
  editorMode.value = "edit";
  editorId.value = row.id;
  form.name = row.name;
  form.handlerName = row.handlerName;
  form.cronExpression = row.cronExpression;
  form.status = row.status ?? 1;
  form.allowConcurrent = row.allowConcurrent ?? 1;
  form.misfirePolicy = row.misfirePolicy || "DEFAULT";
  form.targetType = row.targetType || "ALL";
  form.targetIds = row.targetIds ? [...row.targetIds] : [];
  form.params = row.params || "";
  form.remark = row.remark || "";
  editorVisible.value = true;
}

async function saveJob() {
  if (!form.name.trim()) {
    ElMessage.warning("请输入任务名称");
    return;
  }
  if (!form.handlerName) {
    ElMessage.warning("请选择处理器");
    return;
  }
  if (!form.cronExpression.trim()) {
    ElMessage.warning("请输入 Cron 表达式");
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createJob({...form});
      if (result?.code === 200) {
        ElMessage.success("任务创建成功");
        editorVisible.value = false;
        loadJobs();
      } else {
        ElMessage.error(result?.message || "任务创建失败");
      }
    } else if (editorId.value != null) {
      const payload: JobUpdatePayload = {...form};
      const result = await updateJob(editorId.value, payload);
      if (result?.code === 200) {
        ElMessage.success("任务更新成功");
        editorVisible.value = false;
        loadJobs();
      } else {
        ElMessage.error(result?.message || "任务更新失败");
      }
    }
  } catch (error) {
    ElMessage.error("保存任务失败");
  } finally {
    saving.value = false;
  }
}

async function handleStatusChange(row: JobVO, value: number) {
  const result = await updateJobStatus(row.id, value);
  if (result?.code === 200) {
    row.status = value;
    ElMessage.success("状态已更新");
    loadJobs();
  } else {
    ElMessage.error(result?.message || "状态更新失败");
  }
}

async function runOnce(row: JobVO) {
  const result = await runJob(row.id);
  if (result?.code === 200) {
    ElMessage.success("任务已触发");
  } else {
    ElMessage.error(result?.message || "任务触发失败");
  }
}

async function removeJob(row: JobVO) {
  const result = await deleteJob(row.id);
  if (result?.code === 200) {
    ElMessage.success("任务已删除");
    loadJobs();
  } else {
    ElMessage.error(result?.message || "删除失败");
  }
}

async function openLogs(row: JobVO) {
  activeLogJobId.value = row.id;
  logVisible.value = true;
  logPageNum.value = 1;
  await loadLogs();
}

async function loadLogs() {
  if (logLoading.value || activeLogJobId.value == null) {
    return;
  }
  logLoading.value = true;
  try {
    const result = await listJobLogs(activeLogJobId.value, {
      pageNum: logPageNum.value,
      pageSize: logPageSize.value
    });
    if (result?.code === 200 && result.data) {
      logs.value = result.data.data || [];
      logTotal.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || "加载日志失败");
    }
  } catch (error) {
    ElMessage.error("加载日志失败");
  } finally {
    logLoading.value = false;
  }
}

function handleLogPageChange(value: number) {
  logPageNum.value = value;
  loadLogs();
}

function handleLogSizeChange(value: number) {
  logPageSize.value = value;
  logPageNum.value = 1;
  loadLogs();
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

watch(
    () => form.targetType,
    (value) => {
      form.targetIds = [];
      if (value === "DEPT") {
        ensureDepts();
      } else if (value === "ROLE") {
        ensureRoles();
      } else if (value === "USER") {
        ensureUsers();
      }
    }
);

onMounted(() => {
  loadHandlers();
  loadJobs();
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
