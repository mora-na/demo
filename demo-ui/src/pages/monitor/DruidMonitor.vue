<template>
  <div v-permission="'druid:monitor'" class="druid-page">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("druid.title") }}</div>
        <div class="module-sub">{{ t("druid.subtitle") }}</div>
      </div>
      <div class="module-actions">
        <div class="module-time">{{ t("druid.generatedAt", {time: formatDateTime(summary?.generatedAt)}) }}</div>
        <el-button size="small" text @click="loadSummary">{{ t("common.refresh") }}</el-button>
      </div>
    </div>

    <div v-if="summary && summary.available === false" class="druid-unavailable">
      {{ t("druid.unavailable") }}
    </div>

    <main v-loading="loading" class="druid-content">
      <DruidHomeSection
          v-if="activeSection === 'home'"
          :format-cell="formatCell"
          :rows="homeRows"
      />
      <DruidDatasourceSection
          v-else-if="activeSection === 'datasource'"
          v-model:selected-datasource-id="selectedDatasourceId"
          :active-connection-stack="activeConnectionStackDisplay"
          :datasource-extra-rows="datasourceExtraRows"
          :datasource-field-rows="datasourceFieldRows"
          :datasource-options="datasourceOptions"
          :datasource-sql-columns="datasourceSqlColumns"
          :datasource-sql-rows="datasourceSqlRows"
          :format-cell="formatCell"
          :format-pre="formatPre"
          :pooling-connection-info="poolingConnectionInfoDisplay"
      />
      <DruidSqlSection
          v-else-if="activeSection === 'sql'"
          v-model:refresh-interval="sqlRefreshInterval"
          :columns="sqlColumns"
          :detail-columns="sqlDetailColumns"
          :detail-rows="sqlDetailRows"
          :format-cell="formatCell"
          :refresh-options="sqlRefreshOptions"
          :rows="sqlRows"
      />
      <DruidWallSection
          v-else-if="activeSection === 'wall'"
          :format-cell="formatCell"
          :groups="wallGroups"
          :should-preformat="shouldPreformatWallValue"
      />
      <DruidWebAppSection
          v-else-if="activeSection === 'webapp'"
          :columns="webAppColumns"
          :format-cell="formatCell"
          :rows="webAppRows"
      />
      <DruidWebUriSection
          v-else-if="activeSection === 'weburi'"
          :columns="webUriColumns"
          :format-cell="formatCell"
          :rows="webUriRows"
      />
      <DruidSessionSection
          v-else-if="activeSection === 'session'"
          :columns="sessionColumns"
          :format-cell="formatCell"
          :rows="sessionRows"
      />
      <DruidSpringSection
          v-else-if="activeSection === 'spring'"
          :columns="springColumns"
          :format-cell="formatCell"
          :rows="springRows"
      />
      <DruidJsonSection
          v-else-if="activeSection === 'json'"
          :rows="jsonApiRows"
      />
    </main>
  </div>
</template>

<script lang="ts" setup>
import {computed, onBeforeUnmount, onMounted, ref, watch} from "vue";
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {useAuthStore} from "../../stores/auth";
import {type DruidMonitorSummary, getDruidMonitorSummary} from "../../api/system";
import DruidHomeSection from "./druid/DruidHomeSection.vue";
import DruidDatasourceSection from "./druid/DruidDatasourceSection.vue";
import DruidSqlSection from "./druid/DruidSqlSection.vue";
import DruidWallSection from "./druid/DruidWallSection.vue";
import DruidWebAppSection from "./druid/DruidWebAppSection.vue";
import DruidWebUriSection from "./druid/DruidWebUriSection.vue";
import DruidSessionSection from "./druid/DruidSessionSection.vue";
import DruidSpringSection from "./druid/DruidSpringSection.vue";
import DruidJsonSection from "./druid/DruidJsonSection.vue";

type DruidRow = Record<string, unknown> & { _idx?: number };

interface TableColumn {
  key: string;
  label: string;
  class?: string;
  aliases?: string[];
  getter: (row: DruidRow, rowIndex: number) => unknown;
}

interface BaseColumn {
  key: string;
  labelKey?: string;
  class?: string;
  aliases?: string[];
  getter: TableColumn["getter"];
}

interface FieldDef {
  labelKey: string;
  valueKeys: string[];
  extraKeys?: string[];
  fieldName?: string;
}

const {t, te} = useI18n();
const authStore = useAuthStore();

const props = defineProps<{
  section?: string;
}>();

const summary = ref<DruidMonitorSummary | null>(null);
const loading = ref(false);
const selectedDatasourceId = ref<string | null>(null);
const sqlRefreshInterval = ref(0);
const refreshTimer = ref<number | null>(null);

const sectionKeys = [
  "home",
  "datasource",
  "sql",
  "wall",
  "webapp",
  "weburi",
  "session",
  "spring",
  "json"
] as const;

const sectionSet = new Set(sectionKeys);

const activeSection = computed(() => {
  const raw = (props.section || "").toLowerCase();
  if (sectionSet.has(raw as (typeof sectionKeys)[number])) {
    return raw;
  }
  return "home";
});

const sqlRefreshOptions = computed(() => [
  {value: 0, label: t("druid.sql.refresh.none")},
  {value: 5, label: t("druid.sql.refresh.five")},
  {value: 10, label: t("druid.sql.refresh.ten")},
  {value: 30, label: t("druid.sql.refresh.thirty")}
]);

function canView() {
  return authStore.permissions.includes("druid:monitor");
}

async function loadSummary() {
  if (!canView() || loading.value) {
    return;
  }
  loading.value = true;
  try {
    const result = await getDruidMonitorSummary();
    if (result?.code === 200 && result.data) {
      summary.value = result.data;
      return;
    }
    ElMessage.error(result?.message || t("druid.loadFailed"));
  } catch (_error) {
    ElMessage.error(t("druid.loadFailed"));
  } finally {
    loading.value = false;
  }
}

function normalizeRows(rows?: Record<string, unknown>[]) {
  if (!rows || !Array.isArray(rows)) {
    return [] as DruidRow[];
  }
  return rows
      .filter((row) => row && typeof row === "object")
      .map((row, index) => ({...row, _idx: index}));
}

function normalizeUnknownRows(value?: Record<string, unknown>[] | Record<string, unknown>) {
  if (Array.isArray(value)) {
    return normalizeRows(value);
  }
  if (value && typeof value === "object") {
    return mapToRows(value).map((row, index) => ({...row, _idx: index}));
  }
  return [] as DruidRow[];
}

function mapToRows(map?: Record<string, unknown>) {
  if (!map || typeof map !== "object") {
    return [] as { key: string; value: unknown }[];
  }
  return Object.keys(map)
      .sort((a, b) => a.localeCompare(b))
      .map((key) => ({key, value: map[key]}));
}

function readValue(row: Record<string, unknown> | null | undefined, keys: string[]) {
  if (!row) {
    return undefined;
  }
  for (const key of keys) {
    if (row[key] != null) {
      return row[key];
    }
  }
  const lowerMap: Record<string, string> = {};
  Object.keys(row).forEach((key) => {
    lowerMap[key.toLowerCase()] = key;
  });
  for (const key of keys) {
    const actual = lowerMap[key.toLowerCase()];
    if (actual && row[actual] != null) {
      return row[actual];
    }
  }
  return undefined;
}

function extractIdentity(row: Record<string, unknown> | null | undefined) {
  const value = readValue(row, ["Identity", "identity", "ID", "Id", "id", "DataSourceId", "dataSourceId"]);
  if (value == null) {
    return null;
  }
  if (typeof value === "number") {
    return String(value);
  }
  const str = String(value).trim();
  return str ? str : null;
}

function formatStringValue(value: string) {
  const trimmed = value.trim();
  if (!trimmed) {
    return "-";
  }
  if (trimmed.startsWith("[") && trimmed.endsWith("]") && trimmed.length > 40) {
    try {
      const parsed = JSON.parse(trimmed);
      if (Array.isArray(parsed)) {
        return parsed.map((item) => String(item)).join("\n");
      }
    } catch (_err) {
      // ignore parse errors
    }
  }
  if (trimmed.includes("\n")) {
    return trimmed;
  }
  if (trimmed.includes(",") && trimmed.length > 120) {
    return trimmed
        .split(",")
        .map((part) => part.trim())
        .filter(Boolean)
        .join("\n");
  }
  if (trimmed.includes(";") && trimmed.length > 120) {
    return trimmed
        .split(";")
        .map((part) => part.trim())
        .filter(Boolean)
        .join("\n");
  }
  return trimmed;
}

function shouldPreformatWallValue(key?: string) {
  if (!key) {
    return false;
  }
  const lower = key.toLowerCase();
  return lower.includes("whitelist") || lower.includes("blacklist");
}

function formatClassPath(value: unknown) {
  if (typeof value !== "string") {
    return value;
  }
  const trimmed = value.trim();
  if (!trimmed) {
    return value;
  }
  if (trimmed.includes(";")) {
    return trimmed.split(";").join(";\n");
  }
  if (trimmed.includes(":") && trimmed.includes("/")) {
    return trimmed.split(":").join(":\n");
  }
  return trimmed;
}

function formatCell(value: unknown) {
  if (value == null || value === "") {
    return "-";
  }
  if (typeof value === "string") {
    return formatStringValue(value);
  }
  if (Array.isArray(value)) {
    return value.map((item) => String(item)).join("\n");
  }
  if (typeof value === "number") {
    return Number.isFinite(value) ? value.toLocaleString() : String(value);
  }
  if (typeof value === "boolean") {
    return value ? "true" : "false";
  }
  try {
    return JSON.stringify(value);
  } catch (_err) {
    return String(value);
  }
}

function formatPre(value: unknown) {
  if (value == null || value === "") {
    return "-";
  }
  if (typeof value === "string") {
    return formatStringValue(value);
  }
  if (Array.isArray(value)) {
    return value.map((item) => String(item)).join("\n");
  }
  try {
    return JSON.stringify(value, null, 2);
  } catch (_err) {
    return String(value);
  }
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
  )}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function colFromKeys(key: string, labelKey: string, valueKeys: string[], className?: string): BaseColumn {
  return {
    key,
    labelKey,
    class: className,
    aliases: valueKeys,
    getter: (row) => readValue(row, valueKeys)
  };
}

const datasourceRows = computed(() => normalizeRows(summary.value?.datasources));
const datasourceDetailRows = computed(() => normalizeRows(summary.value?.datasourceDetails));
const datasourceMap = computed(() => mapById(datasourceRows.value));
const datasourceDetailMap = computed(() => mapById(datasourceDetailRows.value));

interface DatasourceOption {
  id: string;
  label: string;
  name?: string;
}

const datasourceOptions = computed((): DatasourceOption[] => {
  const options: DatasourceOption[] = [];
  datasourceRows.value.forEach((row) => {
    const id = extractIdentity(row);
    if (!id) {
      return;
    }
    const name = readValue(row, ["Name", "name", "DataSource", "dataSource", "DataSourceName", "datasourceName"]);
    const nameText = name == null ? "" : String(name);
    const label = nameText ? `${nameText} (${id})` : id;
    if (nameText) {
      options.push({id, label, name: nameText});
      return;
    }
    options.push({id, label});
  });
  return options.length ? options : [{id: "0", label: t("druid.datasource.noData")}];
});

watch(datasourceOptions, (options) => {
  if (!options.length) {
    selectedDatasourceId.value = null;
    return;
  }
  const has = options.some((item) => item.id === selectedDatasourceId.value);
  if (!has) {
    selectedDatasourceId.value = options[0].id;
  }
}, {immediate: true});

const currentDatasource = computed(() => {
  if (!selectedDatasourceId.value) {
    return null;
  }
  const base = datasourceMap.value.get(selectedDatasourceId.value) || null;
  const detail = datasourceDetailMap.value.get(selectedDatasourceId.value) || null;
  if (base && detail) {
    return {...base, ...detail};
  }
  return base || detail || null;
});

const currentDatasourceName = computed(() => {
  return readValue(currentDatasource.value || undefined, ["Name", "name", "DataSource", "dataSource", "DataSourceName", "datasourceName"]);
});

const activeConnectionStacks = computed(() => summary.value?.activeConnectionStacks || {});
const poolingConnectionInfo = computed(() => summary.value?.poolingConnectionInfo || {});

const activeConnectionStackDisplay = computed(() => {
  if (!selectedDatasourceId.value) {
    return "-";
  }
  return activeConnectionStacks.value[String(selectedDatasourceId.value)] ?? "-";
});

const poolingConnectionInfoDisplay = computed(() => {
  if (!selectedDatasourceId.value) {
    return "-";
  }
  return poolingConnectionInfo.value[String(selectedDatasourceId.value)] ?? "-";
});

const homeRows = computed(() => {
  const basic = summary.value?.basic || {};
  return [
    {
      labelKey: "druid.home.rows.startTime.label",
      descKey: "druid.home.rows.startTime.desc",
      value: readValue(basic, ["StartTime", "startTime"])
    },
    {
      labelKey: "druid.home.rows.version.label",
      descKey: "druid.home.rows.version.desc",
      value: readValue(basic, ["Version", "version"])
    },
    {
      labelKey: "druid.home.rows.driver.label",
      descKey: "druid.home.rows.driver.desc",
      value: readValue(basic, ["Drivers", "drivers", "Driver", "driver"])
    },
    {
      labelKey: "druid.home.rows.resetEnable.label",
      descKey: "druid.home.rows.resetEnable.desc",
      value: readValue(basic, ["ResetEnable", "resetEnable"])
    },
    {
      labelKey: "druid.home.rows.resetCount.label",
      descKey: "druid.home.rows.resetCount.desc",
      value: readValue(basic, ["ResetCount", "resetCount"])
    },
    {
      labelKey: "druid.home.rows.javaVersion.label",
      descKey: "druid.home.rows.javaVersion.desc",
      value: readValue(basic, ["JavaVersion", "javaVersion"])
    },
    {
      labelKey: "druid.home.rows.jvmName.label",
      descKey: "druid.home.rows.jvmName.desc",
      value: readValue(basic, ["JavaVMName", "javaVmName", "JvmName", "jvmName", "JavaVM"])
    },
    {
      labelKey: "druid.home.rows.classPath.label",
      descKey: "druid.home.rows.classPath.desc",
      value: formatClassPath(readValue(basic, ["JavaClassPath", "javaClassPath", "ClassPath", "classPath"])),
      pre: true
    }
  ];
});

const datasourceFieldDefs: FieldDef[] = [
  {
    labelKey: "druid.datasource.fields.userName",
    valueKeys: ["UserName", "userName", "Username", "username", "User", "user"]
  },
  {labelKey: "druid.datasource.fields.url", valueKeys: ["URL", "Url", "url", "JdbcUrl", "jdbcUrl"]},
  {
    labelKey: "druid.datasource.fields.dbType",
    valueKeys: ["DbType", "dbType", "dbtype", "DatabaseType", "databaseType"]
  },
  {
    labelKey: "druid.datasource.fields.driverClassName",
    valueKeys: ["DriverClassName", "driverClassName", "Driver", "driver"]
  },
  {
    labelKey: "druid.datasource.fields.filterClassNames",
    valueKeys: ["FilterClassNames", "filterClassNames", "Filters", "filters"]
  },
  {labelKey: "druid.datasource.fields.testOnBorrow", valueKeys: ["TestOnBorrow", "testOnBorrow"]},
  {labelKey: "druid.datasource.fields.testWhileIdle", valueKeys: ["TestWhileIdle", "testWhileIdle"]},
  {labelKey: "druid.datasource.fields.testOnReturn", valueKeys: ["TestOnReturn", "testOnReturn"]},
  {labelKey: "druid.datasource.fields.initialSize", valueKeys: ["InitialSize", "initialSize"]},
  {labelKey: "druid.datasource.fields.minIdle", valueKeys: ["MinIdle", "minIdle"]},
  {labelKey: "druid.datasource.fields.maxActive", valueKeys: ["MaxActive", "maxActive", "MaxPool", "maxPool"]},
  {
    labelKey: "druid.datasource.fields.queryTimeout",
    valueKeys: ["QueryTimeout", "queryTimeout", "DefaultQueryTimeout", "defaultQueryTimeout"]
  },
  {
    labelKey: "druid.datasource.fields.transactionQueryTimeout",
    valueKeys: ["TransactionQueryTimeout", "transactionQueryTimeout"]
  },
  {labelKey: "druid.datasource.fields.loginTimeout", valueKeys: ["LoginTimeout", "loginTimeout"]},
  {
    labelKey: "druid.datasource.fields.validConnectionChecker",
    valueKeys: ["ValidConnectionCheckerClassName", "validConnectionCheckerClassName"]
  },
  {
    labelKey: "druid.datasource.fields.exceptionSorter",
    valueKeys: ["ExceptionSorterClassName", "exceptionSorterClassName"]
  },
  {labelKey: "druid.datasource.fields.defaultAutoCommit", valueKeys: ["DefaultAutoCommit", "defaultAutoCommit"]},
  {labelKey: "druid.datasource.fields.defaultReadOnly", valueKeys: ["DefaultReadOnly", "defaultReadOnly"]},
  {
    labelKey: "druid.datasource.fields.defaultTransactionIsolation",
    valueKeys: ["DefaultTransactionIsolation", "defaultTransactionIsolation"]
  },
  {
    labelKey: "druid.datasource.fields.minEvictableIdleTimeMillis",
    valueKeys: ["MinEvictableIdleTimeMillis", "minEvictableIdleTimeMillis"]
  },
  {
    labelKey: "druid.datasource.fields.maxEvictableIdleTimeMillis",
    valueKeys: ["MaxEvictableIdleTimeMillis", "maxEvictableIdleTimeMillis"]
  },
  {labelKey: "druid.datasource.fields.keepAlive", valueKeys: ["KeepAlive", "keepAlive"]},
  {labelKey: "druid.datasource.fields.failFast", valueKeys: ["FailFast", "failFast"]},
  {
    labelKey: "druid.datasource.fields.poolPreparedStatements",
    valueKeys: ["PoolPreparedStatements", "poolPreparedStatements"]
  },
  {
    labelKey: "druid.datasource.fields.maxPoolPreparedStatementPerConnectionSize",
    valueKeys: ["MaxPoolPreparedStatementPerConnectionSize", "maxPoolPreparedStatementPerConnectionSize"]
  },
  {labelKey: "druid.datasource.fields.maxWait", valueKeys: ["MaxWait", "maxWait"]},
  {labelKey: "druid.datasource.fields.maxWaitThreadCount", valueKeys: ["MaxWaitThreadCount", "maxWaitThreadCount"]},
  {labelKey: "druid.datasource.fields.logDifferentThread", valueKeys: ["LogDifferentThread", "logDifferentThread"]},
  {labelKey: "druid.datasource.fields.useUnfairLock", valueKeys: ["UseUnfairLock", "useUnfairLock"]},
  {labelKey: "druid.datasource.fields.initGlobalVariants", valueKeys: ["InitGlobalVariants", "initGlobalVariants"]},
  {labelKey: "druid.datasource.fields.initVariants", valueKeys: ["InitVariants", "initVariants"]},
  {
    labelKey: "druid.datasource.fields.connectCount",
    valueKeys: ["ConnectCount", "connectCount", "CreateCount", "createCount"]
  },
  {
    labelKey: "druid.datasource.fields.waitTime",
    valueKeys: ["NotEmptyWaitTime", "notEmptyWaitTime", "WaitTime", "waitTime"]
  },
  {
    labelKey: "druid.datasource.fields.waitThreadCount",
    valueKeys: ["WaitThreadCount", "waitThreadCount", "NotEmptyWaitThreadCount"]
  },
  {labelKey: "druid.datasource.fields.transactionCount", valueKeys: ["TransactionCount", "transactionCount"]},
  {
    labelKey: "druid.datasource.fields.transactionHistogram",
    valueKeys: ["TransactionHistogram", "transactionHistogram"]
  },
  {labelKey: "druid.datasource.fields.poolingCount", valueKeys: ["PoolingCount", "poolingCount"]},
  {labelKey: "druid.datasource.fields.poolingPeak", valueKeys: ["PoolingPeak", "poolingPeak"]},
  {labelKey: "druid.datasource.fields.poolingPeakTime", valueKeys: ["PoolingPeakTime", "poolingPeakTime"]},
  {labelKey: "druid.datasource.fields.activeCount", valueKeys: ["ActiveCount", "activeCount"]},
  {labelKey: "druid.datasource.fields.activePeak", valueKeys: ["ActivePeak", "activePeak"]},
  {labelKey: "druid.datasource.fields.activePeakTime", valueKeys: ["ActivePeakTime", "activePeakTime"]},
  {labelKey: "druid.datasource.fields.logicConnectCount", valueKeys: ["LogicConnectCount", "logicConnectCount"]},
  {labelKey: "druid.datasource.fields.logicCloseCount", valueKeys: ["LogicCloseCount", "logicCloseCount"]},
  {
    labelKey: "druid.datasource.fields.logicConnectErrorCount",
    valueKeys: ["LogicConnectErrorCount", "logicConnectErrorCount"]
  },
  {
    labelKey: "druid.datasource.fields.discardCount",
    valueKeys: ["DiscardCount", "discardCount", "DiscardErrorCount", "discardErrorCount"]
  },
  {
    labelKey: "druid.datasource.fields.logicConnectReuseCount",
    valueKeys: ["RecycleCount", "recycleCount", "ReturnCount", "returnCount"]
  },
  {
    labelKey: "druid.datasource.fields.physicalConnectCount",
    valueKeys: ["PhysicalConnectCount", "physicalConnectCount", "CreateCount", "createCount"]
  },
  {
    labelKey: "druid.datasource.fields.physicalCloseCount",
    valueKeys: ["PhysicalCloseCount", "physicalCloseCount", "CloseCount", "closeCount"]
  },
  {
    labelKey: "druid.datasource.fields.physicalConnectErrorCount",
    valueKeys: ["PhysicalConnectErrorCount", "physicalConnectErrorCount"]
  },
  {labelKey: "druid.datasource.fields.executeCount", valueKeys: ["ExecuteCount", "executeCount"]},
  {labelKey: "druid.datasource.fields.executeQueryCount", valueKeys: ["ExecuteQueryCount", "executeQueryCount"]},
  {labelKey: "druid.datasource.fields.executeUpdateCount", valueKeys: ["ExecuteUpdateCount", "executeUpdateCount"]},
  {labelKey: "druid.datasource.fields.executeBatchCount", valueKeys: ["ExecuteBatchCount", "executeBatchCount"]},
  {labelKey: "druid.datasource.fields.errorCount", valueKeys: ["ErrorCount", "errorCount"]},
  {labelKey: "druid.datasource.fields.commitCount", valueKeys: ["CommitCount", "commitCount"]},
  {labelKey: "druid.datasource.fields.rollbackCount", valueKeys: ["RollbackCount", "rollbackCount"]},
  {
    labelKey: "druid.datasource.fields.preparedStatementOpenCount",
    valueKeys: ["PreparedStatementCount", "preparedStatementCount", "PSOpenCount", "psOpenCount"]
  },
  {
    labelKey: "druid.datasource.fields.preparedStatementCloseCount",
    valueKeys: ["PreparedStatementCloseCount", "preparedStatementCloseCount", "PSCloseCount", "psCloseCount"]
  },
  {labelKey: "druid.datasource.fields.psCacheAccessCount", valueKeys: ["PSCacheAccessCount", "psCacheAccessCount"]},
  {labelKey: "druid.datasource.fields.psCacheHitCount", valueKeys: ["PSCacheHitCount", "psCacheHitCount"]},
  {labelKey: "druid.datasource.fields.psCacheMissCount", valueKeys: ["PSCacheMissCount", "psCacheMissCount"]},
  {
    labelKey: "druid.datasource.fields.connectionHoldTimeHistogram",
    valueKeys: ["ConnectionHoldTimeHistogram", "connectionHoldTimeHistogram"]
  },
  {labelKey: "druid.datasource.fields.clobOpenCount", valueKeys: ["ClobOpenCount", "clobOpenCount"]},
  {labelKey: "druid.datasource.fields.blobOpenCount", valueKeys: ["BlobOpenCount", "blobOpenCount"]},
  {labelKey: "druid.datasource.fields.keepAliveCheckCount", valueKeys: ["KeepAliveCheckCount", "keepAliveCheckCount"]}
];

const datasourceFieldRows = computed(() => {
  return datasourceFieldDefs.map((def) => {
    const name = def.fieldName || def.valueKeys[0] || def.labelKey;
    const desc = te(def.labelKey) ? t(def.labelKey) : "-";
    return {
      name,
      value: readValue(currentDatasource.value || undefined, def.valueKeys),
      desc
    };
  });
});

const datasourceExtraRows = computed(() => {
  if (!currentDatasource.value) {
    return [];
  }
  const usedKeys = new Set<string>();
  datasourceFieldDefs.forEach((def) => {
    def.valueKeys.forEach((key) => usedKeys.add(key.toLowerCase()));
  });
  const rows: { key: string; value: unknown; desc: string }[] = [];
  Object.keys(currentDatasource.value).forEach((key) => {
    if (key === "_idx") {
      return;
    }
    if (usedKeys.has(key.toLowerCase())) {
      return;
    }
    const normalized = normalizeColumnLabel(key);
    const labelKey = `druid.columns.${normalized}`;
    const desc = te(labelKey) ? t(labelKey) : "-";
    rows.push({key, value: (currentDatasource.value as Record<string, unknown>)[key], desc});
  });
  return rows.sort((a, b) => a.key.localeCompare(b.key));
});

const sqlRows = computed(() => normalizeRows(summary.value?.sqls));
const sqlDetailRows = computed(() => normalizeRows(summary.value?.sqlDetails));

const datasourceSqlRows = computed(() => {
  if (!selectedDatasourceId.value) {
    return sqlRows.value;
  }
  const id = String(selectedDatasourceId.value);
  const name = currentDatasourceName.value ? String(currentDatasourceName.value) : null;
  return sqlRows.value.filter((row) => {
    const rowId = readValue(row, ["DataSourceId", "dataSourceId", "DatasourceId", "datasourceId"]);
    if (rowId != null) {
      return String(rowId) === id;
    }
    const rowName = readValue(row, ["DataSource", "dataSource", "DataSourceName", "datasourceName"]);
    if (rowName != null && name) {
      return String(rowName) === name;
    }
    return false;
  });
});

const webAppRows = computed(() => normalizeUnknownRows(summary.value?.webapp));
const webUriRows = computed(() => normalizeRows(summary.value?.weburi));
const springRows = computed(() => normalizeRows(summary.value?.spring));
const sessionRows = computed(() => normalizeUnknownRows(summary.value?.session));

const datasourceSqlColumns = computed(() => buildColumns([
  {
    key: "index",
    labelKey: "druid.sql.columns.index",
    getter: (_row, index) => index + 1
  },
  colFromKeys("sql", "druid.sql.columns.sql", ["SQL", "sql"], "col-sql"),
  colFromKeys("executeCount", "druid.sql.columns.executeCount", ["ExecuteCount", "executeCount"]),
  colFromKeys("totalTime", "druid.sql.columns.totalTime", ["TotalTime", "totalTime", "ExecuteTime", "executeTime"]),
  colFromKeys("maxTime", "druid.sql.columns.maxTime", ["MaxTime", "maxTime"]),
  colFromKeys("errorCount", "druid.sql.columns.errorCount", ["ErrorCount", "errorCount"])
], datasourceSqlRows.value));

const sqlColumns = computed(() => buildColumns([
  {
    key: "index",
    labelKey: "druid.sql.columns.index",
    getter: (_row, index) => index + 1
  },
  colFromKeys("sql", "druid.sql.columns.sql", ["SQL", "sql"], "col-sql"),
  colFromKeys("datasource", "druid.sql.columns.datasource", ["DataSource", "dataSource", "DataSourceName", "datasourceName"]),
  colFromKeys("executeCount", "druid.sql.columns.executeCount", ["ExecuteCount", "executeCount"]),
  colFromKeys("totalTime", "druid.sql.columns.totalTime", ["TotalTime", "totalTime", "ExecuteTime", "executeTime"]),
  colFromKeys("maxTime", "druid.sql.columns.maxTime", ["MaxTime", "maxTime"]),
  colFromKeys("transactionCount", "druid.sql.columns.transactionCount", ["InTransactionCount", "inTransactionCount", "TransactionCount", "transactionCount"]),
  colFromKeys("errorCount", "druid.sql.columns.errorCount", ["ErrorCount", "errorCount"]),
  colFromKeys("updateCount", "druid.sql.columns.updateCount", ["EffectedRowCount", "effectedRowCount", "UpdatedRowCount", "updateRowCount"]),
  colFromKeys("fetchCount", "druid.sql.columns.fetchCount", ["FetchRowCount", "fetchRowCount"]),
  colFromKeys("runningCount", "druid.sql.columns.runningCount", ["RunningCount", "runningCount"]),
  colFromKeys("concurrentMax", "druid.sql.columns.concurrentMax", ["ConcurrentMax", "concurrentMax"]),
  colFromKeys("executeHistogram", "druid.sql.columns.executeHistogram", ["ExecuteTimeHistogram", "executeTimeHistogram", "ExecuteHistogram", "executeHistogram", "Histogram", "histogram"]),
  colFromKeys("executeRsHistogram", "druid.sql.columns.executeRsHistogram", ["ExecuteAndResultSetHoldTimeHistogram", "executeAndResultSetHoldTimeHistogram"]),
  colFromKeys("fetchHistogram", "druid.sql.columns.fetchHistogram", ["FetchRowCountHistogram", "fetchRowCountHistogram"]),
  colFromKeys("updateHistogram", "druid.sql.columns.updateHistogram", ["UpdateRowCountHistogram", "updateRowCountHistogram", "EffectedRowCountHistogram"])
], sqlRows.value));

const sqlDetailColumns = computed(() => buildColumns([], sqlDetailRows.value));

const webAppColumns = computed(() => buildColumns([
  colFromKeys("contextPath", "druid.webapp.columns.contextPath", ["ContextPath", "contextPath"]),
  colFromKeys("runningCount", "druid.webapp.columns.runningCount", ["RunningCount", "runningCount"]),
  colFromKeys("concurrentMax", "druid.webapp.columns.concurrentMax", ["ConcurrentMax", "concurrentMax"]),
  colFromKeys("requestCount", "druid.webapp.columns.requestCount", ["RequestCount", "requestCount"]),
  colFromKeys("requestTime", "druid.webapp.columns.requestTime", ["RequestTime", "requestTime"]),
  colFromKeys("jdbcExecuteCount", "druid.webapp.columns.jdbcExecuteCount", ["JdbcExecuteCount", "jdbcExecuteCount"]),
  colFromKeys("jdbcExecuteTime", "druid.webapp.columns.jdbcExecuteTime", ["JdbcExecuteTime", "jdbcExecuteTime"]),
  colFromKeys("jdbcCommitCount", "druid.webapp.columns.jdbcCommitCount", ["JdbcCommitCount", "jdbcCommitCount"]),
  colFromKeys("jdbcRollbackCount", "druid.webapp.columns.jdbcRollbackCount", ["JdbcRollbackCount", "jdbcRollbackCount"]),
  colFromKeys("errorCount", "druid.webapp.columns.errorCount", ["ErrorCount", "errorCount"])
], webAppRows.value));

const webUriColumns = computed(() => buildColumns([
  {key: "index", labelKey: "druid.weburi.columns.index", getter: (_row, index) => index + 1},
  colFromKeys("uri", "druid.weburi.columns.uri", ["URI", "uri"], "col-sql"),
  colFromKeys("requestCount", "druid.weburi.columns.requestCount", ["RequestCount", "requestCount"]),
  colFromKeys("requestTime", "druid.weburi.columns.requestTime", ["RequestTime", "requestTime"]),
  colFromKeys("requestTimeMax", "druid.weburi.columns.requestTimeMax", ["RequestTimeMax", "requestTimeMax"]),
  colFromKeys("runningCount", "druid.weburi.columns.runningCount", ["RunningCount", "runningCount"]),
  colFromKeys("concurrentMax", "druid.weburi.columns.concurrentMax", ["ConcurrentMax", "concurrentMax"]),
  colFromKeys("jdbcExecuteCount", "druid.weburi.columns.jdbcExecuteCount", ["JdbcExecuteCount", "jdbcExecuteCount"]),
  colFromKeys("jdbcExecuteErrorCount", "druid.weburi.columns.jdbcExecuteErrorCount", ["JdbcExecuteErrorCount", "jdbcExecuteErrorCount"]),
  colFromKeys("jdbcExecuteTime", "druid.weburi.columns.jdbcExecuteTime", ["JdbcExecuteTime", "jdbcExecuteTime"]),
  colFromKeys("jdbcCommitCount", "druid.weburi.columns.jdbcCommitCount", ["JdbcCommitCount", "jdbcCommitCount"]),
  colFromKeys("jdbcRollbackCount", "druid.weburi.columns.jdbcRollbackCount", ["JdbcRollbackCount", "jdbcRollbackCount"]),
  colFromKeys("fetchRowCount", "druid.weburi.columns.fetchRowCount", ["FetchRowCount", "fetchRowCount"]),
  colFromKeys("updateRowCount", "druid.weburi.columns.updateRowCount", ["UpdateRowCount", "updateRowCount", "EffectedRowCount", "effectedRowCount"]),
  colFromKeys("histogram", "druid.weburi.columns.histogram", ["RequestTimeHistogram", "requestTimeHistogram", "Histogram", "histogram"])
], webUriRows.value));

const sessionColumns = computed(() => buildColumns([
  {key: "index", labelKey: "druid.session.columns.index", getter: (_row, index) => index + 1},
  colFromKeys("sessionId", "druid.session.columns.sessionId", ["SessionId", "sessionId"]),
  colFromKeys("principal", "druid.session.columns.principal", ["Principal", "principal"]),
  colFromKeys("createTime", "druid.session.columns.createTime", ["CreateTime", "createTime"]),
  colFromKeys("lastAccessTime", "druid.session.columns.lastAccessTime", ["LastAccessTime", "lastAccessTime"]),
  colFromKeys("remoteAddress", "druid.session.columns.remoteAddress", ["RemoteAddress", "remoteAddress"]),
  colFromKeys("requestCount", "druid.session.columns.requestCount", ["RequestCount", "requestCount"]),
  colFromKeys("requestTimeTotal", "druid.session.columns.requestTimeTotal", ["RequestTime", "requestTime", "RequestTimeTotal", "requestTimeTotal"]),
  colFromKeys("runningCount", "druid.session.columns.runningCount", ["RunningCount", "runningCount"]),
  colFromKeys("concurrentMax", "druid.session.columns.concurrentMax", ["ConcurrentMax", "concurrentMax"]),
  colFromKeys("jdbcExecuteCount", "druid.session.columns.jdbcExecuteCount", ["JdbcExecuteCount", "jdbcExecuteCount"]),
  colFromKeys("jdbcExecuteTime", "druid.session.columns.jdbcExecuteTime", ["JdbcExecuteTime", "jdbcExecuteTime"]),
  colFromKeys("jdbcCommitCount", "druid.session.columns.jdbcCommitCount", ["JdbcCommitCount", "jdbcCommitCount"]),
  colFromKeys("jdbcRollbackCount", "druid.session.columns.jdbcRollbackCount", ["JdbcRollbackCount", "jdbcRollbackCount"]),
  colFromKeys("fetchRowCount", "druid.session.columns.fetchRowCount", ["FetchRowCount", "fetchRowCount"]),
  colFromKeys("updateRowCount", "druid.session.columns.updateRowCount", ["UpdateRowCount", "updateRowCount", "EffectedRowCount", "effectedRowCount"])
], sessionRows.value));

const springColumns = computed(() => buildColumns([
  {key: "index", labelKey: "druid.spring.columns.index", getter: (_row, index) => index + 1},
  colFromKeys("class", "druid.spring.columns.className", ["Class", "class", "ClassName", "className"]),
  colFromKeys("method", "druid.spring.columns.method", ["Method", "method"]),
  colFromKeys("executeCount", "druid.spring.columns.executeCount", ["ExecuteCount", "executeCount"]),
  colFromKeys("totalTime", "druid.spring.columns.totalTime", ["TotalTime", "totalTime"]),
  colFromKeys("runningCount", "druid.spring.columns.runningCount", ["RunningCount", "runningCount"]),
  colFromKeys("concurrentMax", "druid.spring.columns.concurrentMax", ["ConcurrentMax", "concurrentMax"]),
  colFromKeys("errorCount", "druid.spring.columns.errorCount", ["ErrorCount", "errorCount"]),
  colFromKeys("jdbcCommitCount", "druid.spring.columns.jdbcCommitCount", ["JdbcCommitCount", "jdbcCommitCount"]),
  colFromKeys("jdbcRollbackCount", "druid.spring.columns.jdbcRollbackCount", ["JdbcRollbackCount", "jdbcRollbackCount"]),
  colFromKeys("fetchRowCount", "druid.spring.columns.fetchRowCount", ["FetchRowCount", "fetchRowCount"]),
  colFromKeys("updateRowCount", "druid.spring.columns.updateRowCount", ["UpdateRowCount", "updateRowCount", "EffectedRowCount", "effectedRowCount"])
], springRows.value));

const wallGroups = computed(() => {
  const rows = mapToRows(summary.value?.wall);
  if (!rows.length) {
    return [{key: "empty", titleKey: "druid.wall.sections.summary", rows: []}];
  }
  const summaryRows: typeof rows = [];
  const listRows: typeof rows = [];
  const tableRows: typeof rows = [];
  const sqlRows: typeof rows = [];
  const otherRows: typeof rows = [];
  rows.forEach((row) => {
    const key = row.key.toLowerCase();
    if (key.includes("whitelist") || key.includes("blacklist")) {
      listRows.push(row);
    } else if (key.includes("table")) {
      tableRows.push(row);
    } else if (key.includes("sql") || key.includes("select") || key.includes("insert") || key.includes("update") || key.includes("delete")) {
      sqlRows.push(row);
    } else if (key.includes("count") || key.includes("time") || key.includes("error") || key.includes("violation")) {
      summaryRows.push(row);
    } else {
      otherRows.push(row);
    }
  });
  const groups = [
    {key: "summary", titleKey: "druid.wall.sections.summary", rows: summaryRows},
    {key: "list", titleKey: "druid.wall.sections.list", rows: listRows},
    {key: "table", titleKey: "druid.wall.sections.table", rows: tableRows},
    {key: "sql", titleKey: "druid.wall.sections.sql", rows: sqlRows},
    {key: "other", titleKey: "druid.wall.sections.other", rows: otherRows}
  ];
  return groups.filter((group) => group.rows.length > 0);
});

const jsonApiRows = [
  {labelKey: "druid.jsonApi.items.basic", path: "basic.json"},
  {labelKey: "druid.jsonApi.items.datasource", path: "datasource.json"},
  {labelKey: "druid.jsonApi.items.datasourceDetail", path: "datasource-{id}.json"},
  {labelKey: "druid.jsonApi.items.activeConnectionStack", path: "activeConnectionStackTrace-{datasourceId}.json"},
  {labelKey: "druid.jsonApi.items.sql", path: "sql.json"},
  {labelKey: "druid.jsonApi.items.wallStat", path: "wallStat.json"},
  {labelKey: "druid.jsonApi.items.wall", path: "wall-{id}.json"},
  {labelKey: "druid.jsonApi.items.weburi", path: "weburi.json"},
  {labelKey: "druid.jsonApi.items.websession", path: "websession.json"},
  {labelKey: "druid.jsonApi.items.resetAll", path: "reset-all.json"}
];

function mapById(rows: DruidRow[]) {
  const map = new Map<string, DruidRow>();
  rows.forEach((row) => {
    const id = extractIdentity(row);
    if (id) {
      map.set(id, row);
    }
  });
  return map;
}

function normalizeColumnLabel(key: string) {
  return key.replace(/[^a-zA-Z0-9]+/g, "_").toLowerCase();
}

function buildColumns(baseColumns: BaseColumn[], rows: DruidRow[]) {
  const columns: TableColumn[] = baseColumns.map((col) => ({
    key: col.key,
    class: col.class,
    label: col.labelKey ? t(col.labelKey) : col.key,
    aliases: col.aliases,
    getter: col.getter
  }));
  if (!rows.length) {
    return columns;
  }
  const usedKeys = new Set<string>();
  baseColumns.forEach((col) => {
    if (col.key) {
      usedKeys.add(col.key.toLowerCase());
    }
    if (col.aliases && col.aliases.length) {
      col.aliases.forEach((alias) => usedKeys.add(alias.toLowerCase()));
    }
  });
  const extraKeys = new Set<string>();
  rows.forEach((row) => {
    Object.keys(row).forEach((key) => {
      if (key === "_idx") {
        return;
      }
      if (usedKeys.has(key.toLowerCase())) {
        return;
      }
      extraKeys.add(key);
    });
  });
  Array.from(extraKeys)
      .sort((a, b) => a.localeCompare(b))
      .forEach((key) => {
        const normalized = normalizeColumnLabel(key);
        const labelKey = `druid.columns.${normalized}`;
        columns.push({
          key,
          label: te(labelKey) ? t(labelKey) : key,
          getter: (row) => readValue(row, [key])
        });
      });
  return columns;
}

function applySqlRefresh() {
  if (refreshTimer.value != null) {
    clearInterval(refreshTimer.value);
    refreshTimer.value = null;
  }
  if (activeSection.value !== "sql") {
    return;
  }
  if (sqlRefreshInterval.value <= 0) {
    return;
  }
  refreshTimer.value = window.setInterval(() => {
    loadSummary();
  }, sqlRefreshInterval.value * 1000);
}

watch([activeSection, sqlRefreshInterval], applySqlRefresh);

onMounted(() => {
  loadSummary();
});

onBeforeUnmount(() => {
  if (refreshTimer.value != null) {
    clearInterval(refreshTimer.value);
  }
});
</script>

<style scoped>
.druid-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.module-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
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
  align-items: center;
  gap: 10px;
}

.module-time {
  font-size: 12px;
  color: var(--muted);
}

.druid-unavailable {
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px dashed rgba(15, 23, 42, 0.12);
  color: var(--muted);
  background: rgba(255, 255, 255, 0.7);
}

.druid-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
