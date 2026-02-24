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
          <td class="col-value">
            <pre v-if="shouldPreformat(row.key)" class="cell-pre">{{ formatStructured(row.value) }}</pre>
            <span v-else>{{ formatCell(row.value) }}</span>
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

function formatStructured(value: unknown) {
  if (value == null || value === "") {
    return "-";
  }
  if (typeof value === "string") {
    const trimmed = value.trim();
    if (trimmed) {
      if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
        try {
          return JSON.stringify(JSON.parse(trimmed), null, 2);
        } catch (_err) {
          return normalizeWhitespace(trimmed);
        }
      }
      return normalizeWhitespace(trimmed);
    }
    return "-";
  }
  if (typeof value === "object") {
    try {
      return JSON.stringify(value, null, 2);
    } catch (_err) {
      return String(value);
    }
  }
  return formatCell(value);
}

function normalizeWhitespace(value: string) {
  const unescaped = value
      .replace(/\\r\\n/g, "\n")
      .replace(/\\n/g, "\n")
      .replace(/\\t/g, "\t")
      .replace(/\\r/g, "\n");
  return unescaped
      .replace(/\r\n/g, "\n")
      .replace(/\r/g, "\n")
      .replace(/\t/g, "  ");
}
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
  width: 16%;
}

.col-value {
  width: 84%;
}

.cell-pre {
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}
</style>
