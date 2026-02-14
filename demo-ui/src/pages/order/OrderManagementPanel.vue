<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("order.title") }}</div>
        <div class="module-sub">{{ t("order.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <el-input-number
            v-model="filters.userId"
            :min="1"
            :placeholder="t('order.filter.userIdPlaceholder')"
            controls-position="right"
        />
        <el-input-number
            v-model="filters.minAmount"
            :min="0"
            :precision="2"
            :step="10"
            :placeholder="t('order.filter.minAmountPlaceholder')"
            controls-position="right"
        />
        <el-input-number
            v-model="filters.maxAmount"
            :min="0"
            :precision="2"
            :step="10"
            :placeholder="t('order.filter.maxAmountPlaceholder')"
            controls-position="right"
        />
        <el-button @click="handleSearch">{{ t("order.filter.search") }}</el-button>
        <el-button type="primary" @click="openCreate">{{ t("order.filter.create") }}</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="orders" row-key="id" size="small">
      <el-table-column :label="t('order.table.id')" prop="id" width="110"/>
      <el-table-column :label="t('order.table.user')" min-width="140">
        <template #default="{row}">
          {{ formatUser(row) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('order.table.amount')" width="140">
        <template #default="{row}">
          {{ formatAmount(row.amount) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('order.table.createdAt')" width="170">
        <template #default="{row}">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('order.table.remark')" min-width="180" prop="remark" show-overflow-tooltip/>
      <el-table-column :label="t('order.table.action')" width="160">
        <template #default="{row}">
          <div class="action-buttons">
            <el-button size="small" text @click="openEdit(row)">{{ t("order.table.edit") }}</el-button>
            <el-button size="small" text type="danger" @click="removeOrder(row)">
              {{ t("order.table.delete") }}
            </el-button>
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

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="520px">
      <el-form :model="form" label-position="top" class="order-editor-form">
        <el-form-item :label="t('order.dialog.userId')">
          <el-input-number
              v-model="form.userId"
              :min="1"
              :placeholder="t('order.dialog.userIdPlaceholder')"
              controls-position="right"
          />
        </el-form-item>
        <el-form-item :label="t('order.dialog.amount')">
          <el-input-number
              v-model="form.amount"
              :min="0"
              :precision="2"
              :step="10"
              :placeholder="t('order.dialog.amountPlaceholder')"
              controls-position="right"
          />
        </el-form-item>
        <el-form-item :label="t('order.dialog.remark')">
          <el-input v-model.trim="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :loading="saving" type="primary" @click="saveOrder">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  createOrder,
  deleteOrder,
  listOrders,
  type OrderCreatePayload,
  type OrderQuery,
  type OrderUpdatePayload,
  type OrderVO,
  updateOrder
} from "../../api/order";

const {t} = useI18n();

const orders = ref<OrderVO[]>([]);
const loading = ref(false);
const saving = ref(false);

const filters = reactive({
  userId: null as number | null,
  minAmount: null as number | null,
  maxAmount: null as number | null
});

const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const form = reactive({
  id: null as number | null,
  userId: null as number | null,
  amount: null as number | null,
  remark: ""
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("order.dialog.createTitle") : t("order.dialog.editTitle")
);

function formatAmount(amount?: number | string) {
  if (amount == null) {
    return "-";
  }
  const value = typeof amount === "string" ? Number(amount) : amount;
  if (Number.isNaN(value)) {
    return "-";
  }
  return value.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
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

function formatUser(order: OrderVO) {
  if (!order) {
    return "-";
  }
  const name = order.userName || "";
  const nick = order.nickName ? ` (${order.nickName})` : "";
  if (name) {
    return `${name}${nick}`;
  }
  return order.userId != null ? `#${order.userId}` : "-";
}

async function loadOrders() {
  if (loading.value) {
    return;
  }
  loading.value = true;
  try {
    const params: OrderQuery = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userId: filters.userId ?? undefined,
      minAmount: filters.minAmount ?? undefined,
      maxAmount: filters.maxAmount ?? undefined
    };
    const result = await listOrders(params);
    if (result?.code === 200 && result.data) {
      orders.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("order.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("order.msg.loadFailed"));
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  loadOrders();
}

function handlePageChange(value: number) {
  pageNum.value = value;
  loadOrders();
}

function handleSizeChange(value: number) {
  pageSize.value = value;
  pageNum.value = 1;
  loadOrders();
}

function resetForm() {
  form.id = null;
  form.userId = null;
  form.amount = null;
  form.remark = "";
}

function openCreate() {
  resetForm();
  editorMode.value = "create";
  editorVisible.value = true;
}

function openEdit(row: OrderVO) {
  form.id = row.id ?? null;
  form.userId = row.userId ?? null;
  if (row.amount == null) {
    form.amount = null;
  } else {
    const value = typeof row.amount === "string" ? Number(row.amount) : row.amount;
    form.amount = Number.isNaN(value) ? null : value;
  }
  form.remark = row.remark || "";
  editorMode.value = "edit";
  editorVisible.value = true;
}

async function saveOrder() {
  if (saving.value) {
    return;
  }
  if (form.userId == null || form.amount == null || form.amount <= 0) {
    ElMessage.warning(t("order.msg.fillRequired"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const payload: OrderCreatePayload = {
        userId: form.userId,
        amount: form.amount,
        remark: form.remark || undefined
      };
      const result = await createOrder(payload);
      if (result?.code === 200) {
        ElMessage.success(t("order.msg.createSuccess"));
        editorVisible.value = false;
        loadOrders();
      } else {
        ElMessage.error(result?.message || t("order.msg.createFailed"));
      }
      return;
    }
    if (!form.id) {
      ElMessage.error(t("order.msg.updateFailed"));
      return;
    }
    const payload: OrderUpdatePayload = {
      userId: form.userId,
      amount: form.amount,
      remark: form.remark || undefined
    };
    const result = await updateOrder(form.id, payload);
    if (result?.code === 200) {
      ElMessage.success(t("order.msg.updateSuccess"));
      editorVisible.value = false;
      loadOrders();
    } else {
      ElMessage.error(result?.message || t("order.msg.updateFailed"));
    }
  } catch (error) {
    ElMessage.error(t("order.msg.updateFailed"));
  } finally {
    saving.value = false;
  }
}

async function removeOrder(row: OrderVO) {
  if (!row?.id) {
    return;
  }
  try {
    await ElMessageBox.confirm(
        t("order.msg.deleteConfirm", {id: row.id}),
        t("common.confirmTitle"),
        {type: "warning"}
    );
  } catch {
    return;
  }
  const result = await deleteOrder(row.id);
  if (result?.code === 200) {
    ElMessage.success(t("order.msg.deleteSuccess"));
    loadOrders();
  } else {
    ElMessage.error(result?.message || t("order.msg.deleteFailed"));
  }
}

onMounted(loadOrders);
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

.module-actions :deep(.el-input-number) {
  width: 140px;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 4px;
}

.action-buttons {
  display: inline-flex;
  gap: 6px;
}
</style>
