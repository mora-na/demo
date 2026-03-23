<template>
  <div
      :class="itemClasses"
      class="palette-item"
      draggable="true"
      @dragend="onDragEnd"
      @dragstart="onDragStart"
  >
    <span class="palette-item__text">{{ item.label }}</span>
    <el-tooltip v-if="item.isTemplate" content="拖入画布将自动展开完整结构" placement="right">
      <Info :size="12" class="palette-item__info"/>
    </el-tooltip>
    <button
        v-if="removable"
        class="palette-item__remove"
        title="移除"
        type="button"
        @click.stop="emit('remove')"
    >
      <X :size="12"/>
    </button>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
import {Info, X} from "lucide-vue-next";
import type {PaletteItem as PaletteItemType} from "../../types/formula";
import {TokenCategory} from "../../types/formula";
import {useDragAndDrop} from "../../composables/formula/useDragAndDrop";

const props = defineProps<{
  item: PaletteItemType;
  removable?: boolean;
}>();

const emit = defineEmits<{ (e: "remove"): void }>();
const {handlePaletteDragStart, handleDragEnd} = useDragAndDrop();
const isDragging = ref(false);

const itemClasses = computed(() => ({
  "palette-item--control": props.item.category === TokenCategory.CONTROL_FLOW,
  "palette-item--logic": props.item.category === TokenCategory.LOGIC_OP,
  "palette-item--compare": props.item.category === TokenCategory.COMPARE_OP,
  "palette-item--arithmetic": props.item.category === TokenCategory.ARITHMETIC_OP,
  "palette-item--grouping": props.item.category === TokenCategory.GROUPING,
  "palette-item--value": props.item.category === TokenCategory.VALUE,
  "palette-item--sub-expr": props.item.category === TokenCategory.SUB_EXPR,
  "palette-item--dragging": isDragging.value,
  "palette-item--removable": props.removable
}));

function onDragStart(e: DragEvent) {
  isDragging.value = true;
  handlePaletteDragStart(e, props.item);
}

function onDragEnd() {
  isDragging.value = false;
  handleDragEnd();
}
</script>

<style lang="scss" scoped>
.palette-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: var(--formula-chip-height, 32px);
  padding: 0 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: grab;
  user-select: none;
  transition: all 0.15s ease;
  border: 1.5px solid;
  position: relative;

  &:active {
    cursor: grabbing;
    transform: scale(0.95);
  }

  &--dragging {
    opacity: 0.4;
  }

  &--removable {
    padding-right: 22px;
  }

  &--control {
    background: #ecf5ff;
    border-color: #a0cfff;
    color: #409eff;
  }

  &--logic {
    background: #f0f9eb;
    border-color: #b3e19d;
    color: #67c23a;
  }

  &--compare {
    background: #fdf6ec;
    border-color: #f3d19e;
    color: #e6a23c;
  }

  &--arithmetic {
    background: #f5f3ff;
    border-color: #c4b5fd;
    color: #7c3aed;
  }

  &--grouping {
    background: #eef2ff;
    border-color: #c7d2fe;
    color: #4f46e5;
  }

  &--value {
    background: #ecfeff;
    border-color: #67e8f9;
    color: #0891b2;
  }

  &--sub-expr {
    background: #f3e8ff;
    border-color: #d8b4fe;
    color: #a855f7;
  }

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transform: translateY(-1px);
  }

  &__info {
    color: inherit;
    opacity: 0.6;
  }

  &__remove {
    position: absolute;
    top: -6px;
    right: -6px;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    border: none;
    background: #f5f7fa;
    color: #909399;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    padding: 0;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);

    &:hover {
      background: #fef0f0;
      color: #f56c6c;
    }
  }
}
</style>
