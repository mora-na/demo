<template>
  <div class="empty-canvas-hint">
    <div class="hint-title">拖拽左侧公式元素到此处</div>
    <div class="hint-sub">可组合判断、运算与变量，实时预览表达式</div>
  </div>
</template>

<script lang="ts" setup>
</script>

<style scoped>
.empty-canvas-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 160px;
  color: #909399;
  font-size: 13px;
  text-align: center;
  gap: 6px;
}

.hint-title {
  font-weight: 600;
  color: #606266;
}
</style>
