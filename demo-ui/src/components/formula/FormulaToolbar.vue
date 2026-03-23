<template>
  <header class="formula-toolbar">
    <div class="toolbar-left">
      <h2 class="toolbar-title">公式构建器</h2>
    </div>

    <div class="toolbar-center">
      <el-button-group>
        <el-button :disabled="!canUndo" title="撤销 (Ctrl+Z)" @click="emit('undo')">
          <Undo2 :size="16"/>
        </el-button>
        <el-button :disabled="!canRedo" title="重做 (Ctrl+Shift+Z)" @click="emit('redo')">
          <Redo2 :size="16"/>
        </el-button>
      </el-button-group>

      <el-divider direction="vertical"/>

      <el-button :disabled="!hasTokens" title="清空画布" @click="emit('clear')">
        <Trash2 :size="16"/>
        <span>清空</span>
      </el-button>

      <el-button :disabled="!hasTokens" title="保存为子表达式" @click="emit('saveSubExpr')">
        <Save :size="16"/>
        <span>保存子表达式</span>
      </el-button>

      <el-divider direction="vertical"/>

      <el-button title="预览公式" @click="emit('preview')">
        <Eye :size="16"/>
        <span>预览</span>
      </el-button>

      <el-button :disabled="!isCompiled" title="导出表达式" type="primary" @click="emit('export')">
        <Download :size="16"/>
        <span>导出</span>
      </el-button>
    </div>

    <div class="toolbar-right">
      <el-tag v-if="isCompiled" size="small" type="success">
        <CheckCircle :size="12"/>
        编译通过
      </el-tag>
      <el-tag v-else-if="hasTokens && hasErrors" size="small" type="danger">
        <AlertCircle :size="12"/>
        存在错误
      </el-tag>
      <el-tag v-else-if="hasTokens" size="small" type="warning">
        <AlertTriangle :size="12"/>
        未完成
      </el-tag>
    </div>
  </header>
</template>

<script lang="ts" setup>
import {AlertCircle, AlertTriangle, CheckCircle, Download, Eye, Redo2, Save, Trash2, Undo2} from "lucide-vue-next";

defineProps<{
  canUndo: boolean;
  canRedo: boolean;
  hasErrors: boolean;
  hasTokens: boolean;
  isCompiled: boolean;
}>();

const emit = defineEmits<{
  (e: "undo"): void;
  (e: "redo"): void;
  (e: "clear"): void;
  (e: "saveSubExpr"): void;
  (e: "preview"): void;
  (e: "export"): void;
}>();
</script>

<style lang="scss" scoped>
.formula-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  z-index: 10;
}

.toolbar-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.toolbar-center {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-tag {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}
</style>
