<template>
  <div class="data-scope-card">
    <div class="filter-row">
      <el-select
          v-model="selectedUserId"
          :loading="userLoading"
          :placeholder="t('dataScope.overview.userPlaceholder')"
          filterable
          remote
          clearable
          :remote-method="fetchUserOptions"
      >
        <el-option
            v-for="user in userOptions"
            :key="user.id"
            :label="`${user.userName} (${user.nickName || '-'})`"
            :value="user.id"
        />
      </el-select>
      <el-select
          v-model="selectedPermission"
          :placeholder="t('dataScope.overview.menuPlaceholder')"
          clearable
          filterable
      >
        <el-option
            v-for="menu in permissionOptions"
            :key="menu.permission"
            :label="`${menu.name} (${menu.permission})`"
            :value="menu.permission"
        />
      </el-select>
      <el-button type="primary" :loading="loading" @click="handleResolve">
        {{ t("dataScope.overview.search") }}
      </el-button>
    </div>

    <div v-if="resolveResult" class="resolve-grid">
      <div class="resolve-section">
        <div class="section-title">{{ t("dataScope.overview.userInfo") }}</div>
        <div class="section-body">
          <div class="kv">
            <span>{{ t("dataScope.overview.userName") }}</span>
            <strong>{{ resolveResult.user?.userName }}</strong>
          </div>
          <div class="kv">
            <span>{{ t("dataScope.overview.dept") }}</span>
            <strong>{{ resolveResult.user?.deptName || "-" }}</strong>
          </div>
          <div class="kv">
            <span>{{ t("dataScope.overview.posts") }}</span>
            <strong>{{ (resolveResult.user?.posts || []).join(" / ") || "-" }}</strong>
          </div>
          <div class="kv">
            <span>{{ t("dataScope.overview.roles") }}</span>
            <strong>{{ roleNames }}</strong>
          </div>
        </div>
      </div>

      <div class="resolve-section">
        <div class="section-title">{{ t("dataScope.overview.result") }}</div>
        <div class="section-body">
          <div class="kv">
            <span>{{ t("dataScope.overview.finalScope") }}</span>
            <strong>{{ resolveResult.finalScopeLabel || "-" }}</strong>
          </div>
          <div class="kv">
            <span>{{ t("dataScope.overview.deptIds") }}</span>
            <strong>{{ (resolveResult.mergedDeptIds || []).join(", ") || "-" }}</strong>
          </div>
          <div class="kv">
            <span>{{ t("dataScope.overview.includeSelf") }}</span>
            <strong>{{ resolveResult.includeSelf ? t("common.yes") : t("common.no") }}</strong>
          </div>
        </div>
      </div>

      <div class="resolve-section">
        <div class="section-title">{{ t("dataScope.overview.sql") }}</div>
        <div class="section-body">
          <code class="sql-block">{{ resolveResult.sqlCondition || "-" }}</code>
        </div>
      </div>
    </div>

    <div v-else-if="resolveAll.length" class="resolve-table">
      <el-table :data="resolveAll" size="small">
        <el-table-column prop="menuName" :label="t('dataScope.overview.table.menu')" min-width="180"/>
        <el-table-column prop="permission" :label="t('dataScope.overview.table.permission')" min-width="180"/>
        <el-table-column prop="finalScopeLabel" :label="t('dataScope.overview.table.scope')" min-width="120"/>
        <el-table-column prop="sourceLayer" :label="t('dataScope.overview.table.source')" min-width="120"/>
      </el-table>
    </div>

    <el-empty v-else :description="t('dataScope.overview.empty')"/>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  type DataScopeResolveMenuVO,
  type DataScopeResolveResponse,
  listMenus,
  listUsers,
  type MenuVO,
  resolveAllDataScope,
  resolveDataScope,
  type UserVO
} from "../../api/system";

const {t} = useI18n();
const selectedUserId = ref<number | null>(null);
const selectedPermission = ref<string | null>(null);
const permissionOptions = ref<MenuVO[]>([]);
const userOptions = ref<UserVO[]>([]);
const userLoading = ref(false);
const loading = ref(false);
const resolveResult = ref<DataScopeResolveResponse | null>(null);
const resolveAll = ref<DataScopeResolveMenuVO[]>([]);

const roleNames = computed(() => {
  const roles = resolveResult.value?.user?.roles || [];
  if (!roles.length) {
    return "-";
  }
  return roles.map((item) => item.name || item.code).filter(Boolean).join(" / ");
});

async function fetchMenuOptions() {
  const result = await listMenus();
  if (result?.code === 200 && result.data) {
    permissionOptions.value = result.data.filter((menu) => !!menu.permission);
  }
}

async function fetchUserOptions(query?: string) {
  userLoading.value = true;
  try {
    const result = await listUsers({pageNum: 1, pageSize: 50, userName: query});
    if (result?.code === 200 && result.data) {
      userOptions.value = result.data.data;
    }
  } finally {
    userLoading.value = false;
  }
}

async function handleResolve() {
  if (!selectedUserId.value) {
    ElMessage.warning(t("dataScope.overview.userRequired"));
    return;
  }
  loading.value = true;
  resolveResult.value = null;
  resolveAll.value = [];
  try {
    if (selectedPermission.value) {
      const result = await resolveDataScope(selectedUserId.value, selectedPermission.value);
      if (result?.code === 200 && result.data) {
        resolveResult.value = result.data;
      } else {
        ElMessage.error(result?.message || t("dataScope.overview.loadFailed"));
      }
    } else {
      const result = await resolveAllDataScope(selectedUserId.value);
      if (result?.code === 200 && result.data) {
        resolveAll.value = result.data;
      } else {
        ElMessage.error(result?.message || t("dataScope.overview.loadFailed"));
      }
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchMenuOptions();
  fetchUserOptions();
});
</script>

<style scoped>
.data-scope-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-row {
  display: grid;
  grid-template-columns: minmax(200px, 1fr) minmax(220px, 1fr) auto;
  gap: 12px;
  align-items: center;
}

.resolve-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

.resolve-section {
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.9);
}

.section-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.section-body {
  display: grid;
  gap: 6px;
}

.kv {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
}

.sql-block {
  display: block;
  background: rgba(15, 23, 42, 0.08);
  padding: 8px 10px;
  border-radius: 8px;
  font-size: 12px;
  word-break: break-all;
}

.resolve-table {
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 12px;
  padding: 8px;
}

@media (max-width: 720px) {
  .filter-row {
    grid-template-columns: 1fr;
  }
}
</style>
