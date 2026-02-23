<template>
  <section>
    <div class="section-title">{{ t("druid.menu.datasource") }}</div>
    <div class="section-toolbar">
      <div class="section-toolbar-label">{{ t("druid.datasource.selector") }}</div>
      <el-select
          v-model="localSelectedId"
          class="section-select"
          size="small"
          @update:model-value="handleSelectChange"
      >
        <el-option
            v-for="option in datasourceOptions"
            :key="option.id"
            :label="option.label"
            :value="option.id"
        />
      </el-select>
    </div>

    <table class="druid-table">
      <thead>
      <tr>
        <th class="col-label">{{ t("druid.datasource.table.field") }}</th>
        <th class="col-value">{{ t("druid.datasource.table.value") }}</th>
        <th class="col-desc">{{ t("druid.datasource.table.desc") }}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="row in datasourceFieldRows" :key="row.name">
        <td class="col-label">{{ row.name }}</td>
        <td class="col-value">{{ formatCell(row.value) }}</td>
        <td class="col-desc">{{ row.desc || "-" }}</td>
      </tr>
      </tbody>
    </table>

    <div v-if="datasourceExtraRows.length" class="section-block">
      <div class="block-title">{{ t("druid.datasource.extraTitle") }}</div>
      <table class="druid-table">
        <thead>
        <tr>
          <th class="col-label">{{ t("druid.extra.field") }}</th>
          <th class="col-value">{{ t("druid.extra.value") }}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="row in datasourceExtraRows" :key="row.key">
          <td class="col-label">{{ row.key }}</td>
          <td class="col-value">{{ formatCell(row.value) }}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="section-block">
      <div class="block-title">{{ t("druid.datasource.stackTitle") }}</div>
      <div class="block-pre">
        <pre>{{ formatPre(activeConnectionStack) }}</pre>
      </div>
    </div>

    <div class="section-block">
      <div class="block-title">{{ t("druid.datasource.poolTitle") }}</div>
      <div class="block-pre">
        <pre>{{ formatPre(poolingConnectionInfo) }}</pre>
      </div>
    </div>

    <div class="section-block">
      <div class="block-title">{{ t("druid.datasource.sqlListTitle") }}</div>
      <div class="druid-table-scroll">
        <table class="druid-table druid-sql-table">
          <thead>
          <tr>
            <th v-for="col in datasourceSqlColumns" :key="col.key" :class="col.class">
              {{ col.label }}
            </th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(row, rowIndex) in datasourceSqlRows" :key="row._idx">
            <td v-for="col in datasourceSqlColumns" :key="col.key" :class="col.class">
              {{ formatCell(col.getter(row, rowIndex)) }}
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script lang="ts" setup>
import {computed} from "vue";
import {useI18n} from "vue-i18n";

interface DatasourceOption {
  id: string;
  label: string;
  name?: string;
}

interface TableColumn {
  key: string;
  label: string;
  class?: string;
  getter: (row: Record<string, unknown>, rowIndex: number) => unknown;
}

const props = defineProps<{
  selectedDatasourceId: string | null;
  datasourceOptions: DatasourceOption[];
  datasourceFieldRows: Array<{ name: string; value: unknown; desc?: string }>;
  datasourceExtraRows: Array<{ key: string; value: unknown; desc?: string }>;
  activeConnectionStack: unknown;
  poolingConnectionInfo: unknown;
  datasourceSqlColumns: TableColumn[];
  datasourceSqlRows: Array<Record<string, unknown> & { _idx?: number }>;
  formatCell: (value: unknown) => string;
  formatPre: (value: unknown) => string;
}>();

const emit = defineEmits<{ (e: "update:selectedDatasourceId", id: string | null): void }>();

const {t} = useI18n();
const formatCell = props.formatCell;
const formatPre = props.formatPre;

const localSelectedId = computed({
  get: () => props.selectedDatasourceId,
  set: (value) => emit("update:selectedDatasourceId", value)
});

function handleSelectChange(value: string | null) {
  emit("update:selectedDatasourceId", value);
}
</script>

<style scoped>
.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.section-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.section-toolbar-label {
  font-size: 12px;
  color: var(--muted);
}

.section-select {
  min-width: 160px;
}

.section-block {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.block-title {
  font-size: 13px;
  font-weight: 600;
}

.block-pre {
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  padding: 10px;
  overflow: auto;
}

.block-pre pre {
  margin: 0;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}

.druid-table-scroll {
  width: 100%;
  overflow-x: auto;
}

.druid-table-scroll .druid-table {
  min-width: 1400px;
}

.druid-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.druid-table th,
.druid-table td {
  border: 1px solid rgba(148, 163, 184, 0.25);
  padding: 6px 8px;
  text-align: left;
  vertical-align: top;
}

.col-desc {
  color: var(--muted);
}

.druid-table th {
  background: rgba(148, 163, 184, 0.12);
  font-weight: 600;
}

.col-label {
  width: 32%;
}

.col-value {
  width: 38%;
}

</style>
