<template>
  <section>
    <div class="section-title">{{ t("druid.menu.webapp") }}</div>
    <div class="section-block">
      <div class="block-title">{{ t("druid.webapp.sections.summary") }}</div>
      <table class="druid-table">
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
  </section>
</template>

<script lang="ts" setup>
import {useI18n} from "vue-i18n";

interface TableColumn {
  key: string;
  label: string;
  class?: string;
  getter: (row: Record<string, unknown>, rowIndex: number) => unknown;
}

const props = defineProps<{
  columns: TableColumn[];
  rows: Array<Record<string, unknown> & { _idx?: number }>;
  formatCell: (value: unknown) => string;
}>();

const {t} = useI18n();
const formatCell = props.formatCell;
</script>

<style scoped>
.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
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
