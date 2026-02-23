<template>
  <section>
    <div class="section-title">{{ t("druid.menu.json") }}</div>
    <table class="druid-table">
      <thead>
      <tr>
        <th class="col-label">{{ t("druid.jsonApi.table.name") }}</th>
        <th class="col-value">{{ t("druid.jsonApi.table.path") }}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="row in rows" :key="row.path">
        <td class="col-label">{{ t(row.labelKey) }}</td>
        <td class="col-value"><span class="code">{{ row.path }}</span></td>
      </tr>
      </tbody>
    </table>
  </section>
</template>

<script lang="ts" setup>
import {useI18n} from "vue-i18n";

interface JsonApiRow {
  labelKey: string;
  path: string;
}

const props = defineProps<{
  rows: JsonApiRow[];
}>();

const {t} = useI18n();
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
  width: 68%;
}

.code {
  font-family: "SFMono-Regular", ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 12px;
}
</style>
