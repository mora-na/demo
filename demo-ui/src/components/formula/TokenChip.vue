<template>
  <div
      :class="chipClasses"
      :data-token-id="token.id"
      :data-token-index="index"
      :draggable="!dragDisabled && !isEditing"
      class="token-chip"
      @dragend="onDragEnd"
      @dragstart="onDragStart"
      @contextmenu.prevent="showContextMenu"
  >
    <span v-if="tokenIcon" class="token-chip__icon">
      <component :is="tokenIcon" :size="14"/>
    </span>

    <span v-if="!token.editable" class="token-chip__text">
      {{ token.displayText }}
    </span>

    <input
        v-else
        ref="editInputRef"
        :placeholder="token.type === TokenType.CONSTANT ? '输入值' : '变量名'"
        :value="token.displayText"
        class="token-chip__input"
        @blur="onEditBlur"
        @focus="isEditing = true"
        @input="onEditInput"
        @keydown.enter="onEditConfirm"
        @keydown.escape="onEditCancel"
    />

    <button
        v-if="showDeleteBtn"
        class="token-chip__delete"
        title="删除"
        @click.stop="emit('delete', token.id)"
    >
      <X :size="12"/>
    </button>

    <el-tooltip
        v-if="token.validationState !== 'valid'"
        :content="token.validationMessage"
        effect="dark"
        placement="top"
    >
      <span class="token-chip__error-icon">
        <AlertCircle :size="14"/>
      </span>
    </el-tooltip>

    <Teleport to="body">
      <div
          v-if="contextMenuVisible"
          :style="contextMenuStyle"
          class="token-context-menu"
          @click.stop
      >
        <div class="menu-item" @click="handleCopy">
          <Copy :size="14"/>
          复制
        </div>
        <div class="menu-item" @click="handleDuplicate">
          <CopyPlus :size="14"/>
          在后方复制
        </div>
        <div class="menu-item menu-item--danger" @click="handleDelete">
          <Trash2 :size="14"/>
          删除
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
import {AlertCircle, Copy, CopyPlus, Trash2, X} from "lucide-vue-next";
import type {FormulaToken} from "../../types/formula";
import {TokenCategory, TokenType} from "../../types/formula";

interface TokenChipProps {
  token: FormulaToken;
  index: number;
  isDragSource: boolean;
  isShifted: boolean;
  nestingColor: string;
  dragDisabled: boolean;
}

interface TokenChipEmits {
  (e: "delete", tokenId: string): void;

  (e: "duplicate", tokenId: string): void;

  (e: "update-value", tokenId: string, display: string, output: string): void;

  (e: "drag-start", event: DragEvent, token: FormulaToken, index: number): void;

  (e: "drag-end"): void;
}

const props = defineProps<TokenChipProps>();
const emit = defineEmits<TokenChipEmits>();

const isEditing = ref(false);
const editInputRef = ref<HTMLInputElement | null>(null);
const contextMenuVisible = ref(false);
const contextMenuStyle = ref<Record<string, string>>({});
const showDeleteBtn = computed(() => true);

const chipClasses = computed(() => ({
  "token-chip--control": props.token.category === TokenCategory.CONTROL_FLOW,
  "token-chip--logic": props.token.category === TokenCategory.LOGIC_OP,
  "token-chip--compare": props.token.category === TokenCategory.COMPARE_OP,
  "token-chip--arithmetic": props.token.category === TokenCategory.ARITHMETIC_OP,
  "token-chip--grouping": props.token.category === TokenCategory.GROUPING,
  "token-chip--value": props.token.category === TokenCategory.VALUE,
  "token-chip--placeholder": props.token.type === TokenType.PLACEHOLDER,
  "token-chip--sub-expr": props.token.category === TokenCategory.SUB_EXPR,
  "token-chip--error": props.token.validationState === "error",
  "token-chip--warning": props.token.validationState === "warning",
  "token-chip--dragging": props.isDragSource,
  "token-chip--shifted": props.isShifted,
  "token-chip--editable": props.token.editable,
  [`token-chip--nesting-${props.token.nestingLevel}`]: true
}));

const tokenIcon = computed(() => {
  const iconMap: Partial<Record<TokenCategory, any>> = {
    [TokenCategory.CONTROL_FLOW]: null,
    [TokenCategory.LOGIC_OP]: null
  };
  return iconMap[props.token.category] || null;
});

function onDragStart(e: DragEvent) {
  emit("drag-start", e, props.token, props.index);
}

function onDragEnd() {
  emit("drag-end");
}

function onEditInput(e: Event) {
  const value = (e.target as HTMLInputElement).value;
  emit("update-value", props.token.id, value, value);
}

function onEditBlur() {
  isEditing.value = false;
}

function onEditConfirm() {
  isEditing.value = false;
  editInputRef.value?.blur();
}

function onEditCancel() {
  isEditing.value = false;
  editInputRef.value?.blur();
}

function showContextMenu(e: MouseEvent) {
  contextMenuVisible.value = true;
  contextMenuStyle.value = {
    position: "fixed",
    left: `${e.clientX}px`,
    top: `${e.clientY}px`,
    zIndex: "9999"
  };
  document.addEventListener("click", hideContextMenu, {once: true});
}

function hideContextMenu() {
  contextMenuVisible.value = false;
}

function handleCopy() {
  navigator.clipboard.writeText(JSON.stringify(props.token));
  hideContextMenu();
}

function handleDuplicate() {
  emit("duplicate", props.token.id);
  hideContextMenu();
}

function handleDelete() {
  emit("delete", props.token.id);
  hideContextMenu();
}
</script>

<style lang="scss" scoped>
.token-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: var(--formula-chip-height, 32px);
  padding: 0 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: grab;
  user-select: none;
  transition: all 0.2s ease;
  position: relative;
  white-space: nowrap;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .token-chip__delete {
      opacity: 1;
    }
  }

  &:active {
    cursor: grabbing;
  }

  &--control {
    background: #ecf5ff;
    border: 1.5px solid #409eff;
    color: #409eff;
  }

  &--logic {
    background: #f0f9eb;
    border: 1.5px solid #67c23a;
    color: #67c23a;
  }

  &--compare {
    background: #fdf6ec;
    border: 1.5px solid #e6a23c;
    color: #e6a23c;
  }

  &--arithmetic {
    background: #f5f3ff;
    border: 1.5px solid #c4b5fd;
    color: #7c3aed;
    font-family: "Courier New", monospace;
    font-weight: 700;
    font-size: 15px;
  }

  &--grouping {
    background: #eef2ff;
    border: 1.5px solid #c7d2fe;
    color: #4f46e5;
    font-family: "Courier New", monospace;
    font-weight: 700;
    font-size: 16px;
    padding: 0 8px;
  }

  &--value {
    background: #ecfeff;
    border: 1.5px solid #67e8f9;
    color: #0891b2;

    &.token-chip--editable {
      background: #fff;
      cursor: text;
    }
  }

  &--placeholder {
    background: transparent;
    border: 2px dashed #c0c4cc;
    color: #c0c4cc;
    padding: 0 24px;
    min-width: 80px;
    justify-content: center;

    &:hover {
      border-color: #409eff;
      color: #409eff;
    }
  }

  &--sub-expr {
    background: #f3e8ff;
    border: 1.5px solid #a855f7;
    color: #a855f7;
  }

  &--error {
    border-color: #f56c6c !important;
    background: #fef0f0 !important;
    animation: token-shake 0.3s ease;

    .token-chip__text,
    .token-chip__input {
      color: #f56c6c !important;
    }
  }

  &--warning {
    border-color: #e6a23c !important;
    background: #fdf6ec !important;
  }

  &--dragging {
    opacity: 0.3;
    transform: scale(0.95);
  }

  &--shifted {
    transition: transform 0.2s ease;
  }

  &--nesting-0 {
  }

  &--nesting-1 {
    &.token-chip--control {
      border-color: #67c23a;
      color: #67c23a;
      background: #f0f9eb;
    }
  }

  &--nesting-2 {
    &.token-chip--control {
      border-color: #a855f7;
      color: #a855f7;
      background: #f3e8ff;
    }
  }

  &__input {
    border: none;
    background: transparent;
    outline: none;
    font-size: inherit;
    font-weight: inherit;
    color: inherit;
    width: 60px;
    min-width: 30px;
    text-align: center;
    height: 100%;
    line-height: var(--formula-chip-height, 32px);

    &::placeholder {
      color: #c0c4cc;
    }
  }

  &__delete {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    border: none;
    background: rgba(0, 0, 0, 0.1);
    cursor: pointer;
    opacity: 0;
    transition: opacity 0.15s;
    padding: 0;

    &:hover {
      background: #f56c6c;
      color: white;
    }
  }

  &__error-icon {
    display: flex;
    color: #f56c6c;
    margin-left: 2px;
  }
}

@keyframes token-shake {
  0%, 100% {
    transform: translateX(0);
  }
  20% {
    transform: translateX(-3px);
  }
  40% {
    transform: translateX(3px);
  }
  60% {
    transform: translateX(-2px);
  }
  80% {
    transform: translateX(2px);
  }
}

.token-context-menu {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  padding: 4px 0;
  min-width: 140px;

  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    cursor: pointer;
    font-size: 13px;
    color: #303133;
    transition: background 0.15s;

    &:hover {
      background: #f5f7fa;
    }

    &--danger {
      color: #f56c6c;

      &:hover {
        background: #fef0f0;
      }
    }
  }
}
</style>
