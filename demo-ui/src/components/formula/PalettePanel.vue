<template>
  <aside class="palette-panel">
    <div class="palette-panel__header">
      <h3>公式元素</h3>
      <el-input
          v-model="searchQuery"
          :prefix-icon="Search"
          class="palette-search"
          clearable
          placeholder="搜索..."
          size="small"
      />
      <CustomValueInput class="palette-custom-input"/>
    </div>

    <div class="palette-panel__body">
      <div v-if="isEmpty" class="palette-empty">未找到匹配的公式元素</div>
      <div v-else class="palette-grid">
        <PaletteItem
            v-for="item in filteredBaseItems"
            :key="item.tokenType + item.outputValue"
            :item="item"
        />
        <PaletteItem
            v-for="item in filteredVariableItems"
            :key="item.label + item.outputValue"
            :item="item"
        />
        <SubExpressionItem
            v-for="subExpr in filteredSubExpressions"
            :key="subExpr.id"
            :sub-expr="subExpr"
            class="palette-grid__subexpr"
        />
        <PaletteItem
            v-for="custom in filteredCustomItems"
            :key="custom.id"
            :item="custom.item"
            removable
            @remove="removeCustomValue(custom.id)"
        />
      </div>
    </div>
  </aside>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
import {Search} from "lucide-vue-next";
import {useFormulaStore} from "../../stores/formulaStore";
import {PALETTE_GROUPS} from "../../config/tokenRegistry";
import type {PaletteItem as PaletteItemType, SubExpression} from "../../types/formula";
import {TokenCategory, TokenType} from "../../types/formula";
import PaletteItem from "./PaletteItem.vue";
import CustomValueInput from "./CustomValueInput.vue";
import SubExpressionItem from "./SubExpressionItem.vue";

const store = useFormulaStore();
const searchQuery = ref("");

const baseItems = computed<PaletteItemType[]>(() => PALETTE_GROUPS.flatMap((group) => group.items));
const variableItems = computed<PaletteItemType[]>(() =>
    store.predefinedVariables.map((item) => ({
      label: item.label,
      tokenType: TokenType.VARIABLE,
      category: TokenCategory.VALUE,
      outputValue: item.name,
      editable: false
    }))
);

const customItems = computed(() =>
    store.customValues.map((item) => ({
      id: item.id,
      item: {
        label: item.name,
        tokenType: item.type === "CONSTANT" ? TokenType.CONSTANT : TokenType.VARIABLE,
        category: TokenCategory.VALUE,
        outputValue: item.value,
        editable: true
      }
    }))
);

const query = computed(() => searchQuery.value.trim().toLowerCase());

function matches(item: PaletteItemType): boolean {
  if (!query.value) return true;
  const haystack = `${item.label} ${item.outputValue}`.toLowerCase();
  return haystack.includes(query.value);
}

function matchesSubExpr(item: SubExpression): boolean {
  if (!query.value) return true;
  const haystack = `${item.name} ${item.description || ""} ${item.displayExpression}`.toLowerCase();
  return haystack.includes(query.value);
}

const filteredBaseItems = computed(() => baseItems.value.filter(matches));
const filteredVariableItems = computed(() => variableItems.value.filter(matches));
const filteredSubExpressions = computed(() => store.subExpressions.filter(matchesSubExpr));
const filteredCustomItems = computed(() => customItems.value.filter((item) => matches(item.item)));

const isEmpty = computed(
    () =>
        filteredBaseItems.value.length === 0 &&
        filteredVariableItems.value.length === 0 &&
        filteredSubExpressions.value.length === 0 &&
        filteredCustomItems.value.length === 0
);

function removeCustomValue(id: string) {
  const index = store.customValues.findIndex((cv) => cv.id === id);
  if (index !== -1) store.customValues.splice(index, 1);
}
</script>

<style lang="scss" scoped>
.palette-panel {
  width: 100%;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  --formula-input-height: var(--formula-chip-height, 32px);

  &__header {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  &__body {
    padding: 12px 16px 16px;
  }
}

.palette-search {
  flex: 1;
  min-width: 180px;
  max-width: 320px;
}

.palette-custom-input {
  margin-left: 0;
}

.palette-search :deep(.el-input__wrapper),
.palette-custom-input :deep(.el-input__wrapper) {
  height: var(--formula-input-height);
}

.palette-search :deep(.el-input__inner),
.palette-custom-input :deep(.el-input__inner) {
  height: var(--formula-input-height);
  line-height: var(--formula-input-height);
}

.palette-custom-input :deep(.el-input-group__append),
.palette-custom-input :deep(.el-button) {
  height: var(--formula-input-height);
}

.palette-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: flex-start;
}

.palette-grid__subexpr {
  flex: 0 0 auto;
}

.palette-empty {
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
  padding: 16px;
}
</style>
