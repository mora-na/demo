<template>
  <section>
    <div class="section-title">{{ t("druid.menu.sql") }}</div>
    <div class="section-toolbar">
      <div class="section-toolbar-label">{{ t("druid.sql.refreshLabel") }}</div>
      <el-select v-model="localRefreshInterval" class="section-select" size="small">
        <el-option v-for="option in refreshOptions" :key="option.value" :label="option.label" :value="option.value"/>
      </el-select>
    </div>
    <div class="druid-table-scroll">
      <table class="druid-table druid-sql-table">
        <thead>
        <tr>
          <th v-for="col in columns" :key="col.key" :class="col.class">{{ col.label }}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(row, rowIndex) in rows" :key="row._idx">
          <td v-for="col in columns" :key="col.key" :class="col.class">
            {{ formatCell(col.getter(row, rowIndex)) }}
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="detailRows.length" class="section-block">
      <div class="block-title">{{ t("druid.sql.detailTitle") }}</div>
      <div class="druid-table-scroll">
        <table class="druid-table druid-sql-table">
          <thead>
          <tr>
            <th v-for="col in detailColumns" :key="col.key" :class="col.class">{{ col.label }}</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(row, rowIndex) in detailRows" :key="row._idx">
            <td v-for="col in detailColumns" :key="col.key" :class="col.class">
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

interface TableColumn {
  key: string;
  label: string;
  class?: string;
  getter: (row: Record<string, unknown>, rowIndex: number) => unknown;
}

interface RefreshOption {
  value: number;
  label: string;
}

const props = defineProps<{
  refreshInterval: number;
  refreshOptions: RefreshOption[];
  columns: TableColumn[];
  rows: Array<Record<string, unknown> & { _idx?: number }>;
  detailColumns: TableColumn[];
  detailRows: Array<Record<string, unknown> & { _idx?: number }>;
  formatCell: (value: unknown) => string;
}>();

const emit = defineEmits<{ (e: "update:refreshInterval", value: number): void }>();

const {t} = useI18n();
const formatCell = props.formatCell;

const localRefreshInterval = computed({
  get: () => props.refreshInterval,
  set: (value) => emit("update:refreshInterval", value)
});
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

.druid-table th {
  background: rgba(148, 163, 184, 0.12);
  font-weight: 600;
}

</style>
