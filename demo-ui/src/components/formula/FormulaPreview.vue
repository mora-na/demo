<template>
  <div :class="{ 'formula-preview--error': parseErrors.length > 0 }" class="formula-preview">
    <div class="preview-row">
      <span class="preview-label">
        <BookOpen :size="14"/>
        可读表达式:
      </span>
      <span class="preview-text">{{ displayExpression || "(拖入公式元素开始构建)" }}</span>
    </div>

    <div class="preview-row">
      <span class="preview-label">
        <Code :size="14"/>
        计算表达式:
      </span>
      <code :class="{ 'preview-code--success': isComplete }" class="preview-code">
        {{ compiledExpression || "(空)" }}
      </code>
    </div>

    <div v-if="parseErrors.length > 0" class="preview-errors">
      <span v-for="(err, i) in parseErrors" :key="i" class="error-text">
        <AlertCircle :size="12"/> {{ err }}
      </span>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {AlertCircle, BookOpen, Code} from "lucide-vue-next";

defineProps<{
  displayExpression: string;
  compiledExpression: string;
  parseErrors: string[];
  isComplete: boolean;
}>();
</script>

<style lang="scss" scoped>
.formula-preview {
  padding: 16px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;

  &--error {
    border-color: #fab6b6;
  }
}

.preview-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }
}

.preview-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  min-width: 100px;
  padding-top: 2px;
}

.preview-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  word-break: break-all;
}

.preview-code {
  font-family: "Courier New", "JetBrains Mono", monospace;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  word-break: break-all;

  &--success {
    background: #f0f9eb;
    color: #67c23a;
  }
}

.preview-errors {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #fde2e2;

  .error-text {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #f56c6c;
    margin-bottom: 4px;
  }
}
</style>
