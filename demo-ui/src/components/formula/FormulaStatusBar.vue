<template>
  <footer class="formula-status-bar">
    <div class="status-item">
      <Blocks :size="14"/>
      <span>元素: {{ tokenCount }}</span>
    </div>
    <div v-if="errorCount > 0" class="status-item status-item--error">
      <AlertCircle :size="14"/>
      <span>错误: {{ errorCount }}</span>
    </div>
    <div v-if="warningCount > 0" class="status-item status-item--warning">
      <AlertTriangle :size="14"/>
      <span>警告: {{ warningCount }}</span>
    </div>
    <div v-if="errorCount === 0 && warningCount === 0 && tokenCount > 0" class="status-item status-item--success">
      <CheckCircle :size="14"/>
      <span>表达式合法</span>
    </div>
  </footer>
</template>

<script lang="ts" setup>
import {AlertCircle, AlertTriangle, Blocks, CheckCircle} from "lucide-vue-next";

defineProps<{
  tokenCount: number;
  errorCount: number;
  warningCount: number;
}>();
</script>

<style lang="scss" scoped>
.formula-status-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 6px 20px;
  background: white;
  border-top: 1px solid #e4e7ed;
  font-size: 12px;
  color: #909399;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 4px;

  &--error {
    color: #f56c6c;
  }

  &--warning {
    color: #e6a23c;
  }

  &--success {
    color: #67c23a;
  }
}
</style>
