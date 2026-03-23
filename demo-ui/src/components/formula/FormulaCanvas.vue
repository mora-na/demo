<template>
  <div
      ref="canvasRef"
      :class="{ 'formula-canvas--drag-active': isDragging }"
      class="formula-canvas"
      @dragleave="onDragLeave"
      @drop="onDrop"
      @dragover.prevent="onDragOver"
  >
    <EmptyCanvasHint v-if="tokens.length === 0 && !isDragging"/>

    <div v-if="tokens.length === 0 && isDragging" class="canvas-drop-hint">
      <Upload :size="32"/>
      <span>释放到此处添加公式元素</span>
    </div>

    <TransitionGroup v-else class="token-list" name="token-list" tag="div">
      <div v-if="dropIndicatorIndex === 0" key="indicator-start" class="drop-indicator"/>

      <template v-for="(token, index) in tokens" :key="token.id">
        <TokenChip
            :drag-disabled="isTemplateLocked(token)"
            :index="index"
            :is-drag-source="dragSourceId === token.id"
            :is-shifted="isTokenShifted(index)"
            :nesting-color="getNestingColor(token.nestingLevel)"
            :token="token"
            @delete="handleDelete"
            @duplicate="handleDuplicate"
            @update-value="handleUpdateValue"
            @drag-start="handleTokenDragStart"
            @drag-end="handleTokenDragEnd"
        />

        <div v-if="dropIndicatorIndex === index + 1" :key="`indicator-${index}`" class="drop-indicator"/>
      </template>
    </TransitionGroup>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
import {Upload} from "lucide-vue-next";
import {useFormulaStore} from "../../stores/formulaStore";
import {useDragAndDrop} from "../../composables/formula/useDragAndDrop";
import TokenChip from "./TokenChip.vue";
import EmptyCanvasHint from "./EmptyCanvasHint.vue";
import type {FormulaToken} from "../../types/formula";
import {TokenType} from "../../types/formula";

const store = useFormulaStore();
const canvasRef = ref<HTMLElement | null>(null);

const {
  isDragging,
  dropIndicatorIndex,
  handleCanvasDragStart,
  handleCanvasDragOver,
  handleCanvasDragLeave,
  handleCanvasDrop,
  handleDragEnd
} = useDragAndDrop();

const tokens = computed(() => store.tokens);
const dragSourceId = ref<string | null>(null);

function onDragOver(e: DragEvent) {
  if (!canvasRef.value) return;
  handleCanvasDragOver(e, canvasRef.value);
}

function onDragLeave(e: DragEvent) {
  handleCanvasDragLeave(e);
}

function onDrop(e: DragEvent) {
  handleCanvasDrop(e);
  dragSourceId.value = null;
}

function handleTokenDragStart(e: DragEvent, token: FormulaToken, index: number) {
  dragSourceId.value = token.id;
  handleCanvasDragStart(e, token, index);
}

function handleTokenDragEnd() {
  handleDragEnd();
  dragSourceId.value = null;
}

function handleDelete(tokenId: string) {
  store.removeToken(tokenId);
}

function handleDuplicate(tokenId: string) {
  store.duplicateToken(tokenId);
}

function handleUpdateValue(tokenId: string, display: string, output: string) {
  store.updateTokenValue(tokenId, display, output);
}

function isTokenShifted(index: number): boolean {
  if (dropIndicatorIndex.value == null || dropIndicatorIndex.value < 0) return false;
  return index >= dropIndicatorIndex.value;
}

function isTemplateLocked(token: FormulaToken): boolean {
  return (token.type === TokenType.THEN || token.type === TokenType.ELSE) && Boolean(token.templateGroupId);
}

function getNestingColor(level: number): string {
  const colors = ["#409eff", "#67c23a", "#a855f7", "#e6a23c", "#909399"];
  return colors[Math.min(level, colors.length - 1)];
}
</script>

<style lang="scss" scoped>
.formula-canvas {
  min-height: 200px;
  padding: 16px;
  background: #fafafa;
  border: 2px dashed #e4e7ed;
  border-radius: 12px;
  transition: all 0.2s ease;

  &--drag-active {
    border-color: #409eff;
    background: #f0f7ff;
  }
}

.token-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  min-height: 40px;
}

.drop-indicator {
  width: 3px;
  height: 32px;
  background: #409eff;
  border-radius: 2px;
  animation: indicator-pulse 0.8s ease infinite;
}

@keyframes indicator-pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
}

.token-list-enter-active {
  transition: all 0.3s ease;
}

.token-list-leave-active {
  transition: all 0.2s ease;
  position: absolute;
}

.token-list-enter-from {
  opacity: 0;
  transform: scale(0.8) translateY(-10px);
}

.token-list-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.token-list-move {
  transition: transform 0.3s ease;
}

.canvas-drop-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 160px;
  color: #409eff;
  font-size: 14px;
}
</style>
