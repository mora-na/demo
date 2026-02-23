<template>
  <section>
    <div class="section-title">{{ t("druid.menu.home") }}</div>
    <table class="druid-table">
      <thead>
      <tr>
        <th class="col-label">{{ t("druid.home.table.label") }}</th>
        <th class="col-value">{{ t("druid.home.table.value") }}</th>
        <th class="col-desc">{{ t("druid.home.table.desc") }}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="row in rows" :key="row.labelKey">
        <td class="col-label">{{ t(row.labelKey) }}</td>
        <td :class="{ 'cell-pre': row.pre }" class="col-value">{{ formatCell(row.value) }}</td>
        <td class="col-desc">{{ t(row.descKey) }}</td>
      </tr>
      </tbody>
    </table>
  </section>
</template>

<script lang="ts" setup>
import {useI18n} from "vue-i18n";

interface HomeRow {
  labelKey: string;
  descKey: string;
  value: unknown;
  pre?: boolean;
}

const props = defineProps<{
  rows: HomeRow[];
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

.col-label {
  width: 32%;
}

.col-value {
  width: 38%;
}

.col-desc {
  width: 30%;
}

.cell-pre {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
