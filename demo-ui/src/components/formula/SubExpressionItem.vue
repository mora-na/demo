<template>
  <div class="sub-expr-item">
    <div
        class="sub-expr-item__chip"
        draggable="true"
        @dragend="onDragEnd"
        @dragstart="onDragStart"
    >
      <span class="sub-expr-item__name">{{ subExpr.name }}</span>
      <div class="sub-expr-item__actions">
        <el-tooltip content="查看详情">
          <button class="icon-btn" @click.stop="showDetail = !showDetail">
            <Eye :size="16"/>
          </button>
        </el-tooltip>
        <el-tooltip content="删除">
          <button class="icon-btn icon-btn--danger" @click.stop="handleDelete">
            <Trash2 :size="16"/>
          </button>
        </el-tooltip>
      </div>
    </div>

    <Transition name="slide-down">
      <div v-if="showDetail" class="sub-expr-item__detail">
        <div v-if="subExpr.description" class="detail-desc">
          {{ subExpr.description }}
        </div>
        <div class="detail-expr">
          <span class="detail-label">表达式：</span>
          <code>{{ subExpr.displayExpression }}</code>
        </div>
        <div class="detail-compiled">
          <span class="detail-label">编译值：</span>
          <code>{{ subExpr.compiledExpression }}</code>
        </div>
        <div class="detail-actions">
          <el-button size="small" @click="insertExpanded">展开插入</el-button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
import {Eye, Trash2} from "lucide-vue-next";
import {ElMessage, ElMessageBox} from "element-plus";
import type {PaletteItem as PaletteItemType, SubExpression} from "../../types/formula";
import {TokenCategory, TokenType} from "../../types/formula";
import {useFormulaStore} from "../../stores/formulaStore";
import {useDragAndDrop} from "../../composables/formula/useDragAndDrop";

const props = defineProps<{
  subExpr: SubExpression;
}>();

const store = useFormulaStore();
const showDetail = ref(false);
const {handlePaletteDragStart, handleDragEnd} = useDragAndDrop();
const paletteItem = computed<PaletteItemType>(() => ({
  label: props.subExpr.name,
  tokenType: TokenType.SUB_EXPR,
  category: TokenCategory.SUB_EXPR,
  outputValue: props.subExpr.compiledExpression,
  editable: false,
  subExprId: props.subExpr.id,
  subExprTokens: props.subExpr.tokens
}));

function onDragStart(e: DragEvent) {
  handlePaletteDragStart(e, paletteItem.value);
}

function onDragEnd() {
  handleDragEnd();
}

function insertExpanded() {
  store.insertSubExpression(props.subExpr.id, store.tokens.length, "expanded");
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm(
        `确定删除子表达式「${props.subExpr.name}」？已使用该子表达式的公式不会受影响。`,
        "删除确认",
        {type: "warning"}
    );
    store.removeSubExpression(props.subExpr.id);
    ElMessage.success("删除成功");
  } catch {
    // ignore
  }
}
</script>

<style lang="scss" scoped>
.sub-expr-item {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;

  &__chip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: var(--formula-chip-height, 32px);
    padding: 0 16px;
    border: 1.5px solid #d8b4fe;
    border-radius: 6px;
    background: #f3e8ff;
    cursor: grab;
    user-select: none;
    transition: all 0.15s ease;
    font-size: 12px;
    font-weight: 500;
  }

  &__chip:hover {
    border-color: #a855f7;
    box-shadow: 0 2px 8px rgba(168, 85, 247, 0.1);
  }

  &__chip:active {
    cursor: grabbing;
  }

  &__name {
    color: #a855f7;
  }

  &__actions {
    display: flex;
    gap: 2px;
  }

  &__detail {
    margin-top: 4px;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
    font-size: 12px;
    width: 100%;

    .detail-label {
      color: #909399;
    }

    code {
      font-family: "Courier New", monospace;
      color: #303133;
      word-break: break-all;
    }
  }
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: #909399;

  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }

  &--danger:hover {
    background: #fef0f0;
    color: #f56c6c;
  }
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s ease;
  overflow: hidden;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  max-height: 0;
}

.slide-down-enter-to,
.slide-down-leave-from {
  max-height: 200px;
}
</style>
