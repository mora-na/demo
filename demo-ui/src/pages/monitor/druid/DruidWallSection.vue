<template>
  <section>
    <div class="section-title">{{ t("druid.menu.wall") }}</div>
    <div v-for="group in groups" :key="group.key" class="section-block">
      <div class="block-title">{{ t(group.titleKey) }}</div>
      <table class="druid-table">
        <thead>
        <tr>
          <th class="col-label">{{ t("druid.extra.key") }}</th>
          <th class="col-value">{{ t("druid.extra.value") }}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="row in group.rows" :key="row.key">
          <td class="col-label">{{ row.key }}</td>
          <td :class="{ 'cell-pre': shouldPreformat(row.key) }" class="col-value">
            {{ formatCell(row.value) }}
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script lang="ts" setup>
import {useI18n} from "vue-i18n";

const props = defineProps<{
  groups: Array<{ key: string; titleKey: string; rows: Array<{ key: string; value: unknown }> }>;
  formatCell: (value: unknown) => string;
  shouldPreformat: (key?: string) => boolean;
}>();

const {t} = useI18n();
const formatCell = props.formatCell;
const shouldPreformat = props.shouldPreformat;
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

.col-label {
  width: 32%;
}

.col-value {
  width: 68%;
}

.cell-pre {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
