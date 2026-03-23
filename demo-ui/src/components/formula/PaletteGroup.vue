<template>
  <div class="palette-group">
    <div class="palette-group__title" @click="expanded = !expanded">
      <component :is="expanded ? ChevronDown : ChevronRight" :size="16"/>
      <component :is="groupIcon" :size="16"/>
      <span>{{ group.groupName }}</span>
    </div>

    <Transition name="collapse">
      <div v-if="expanded" class="palette-group__items">
        <PaletteItem v-for="item in group.items" :key="item.tokenType + item.outputValue" :item="item"/>
      </div>
    </Transition>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
import {
  Calculator,
  ChevronDown,
  ChevronRight,
  Equal,
  GitBranch,
  Parentheses,
  ToggleLeft,
  Variable
} from "lucide-vue-next";
import type {PaletteItem as PaletteItemType} from "../../types/formula";
import PaletteItem from "./PaletteItem.vue";

const props = defineProps<{
  group: {
    groupName: string;
    groupKey: string;
    icon: string;
    items: PaletteItemType[];
  };
  defaultExpanded?: boolean;
}>();

const expanded = ref(props.defaultExpanded ?? false);

const iconMap: Record<string, any> = {
  GitBranch,
  ToggleLeft,
  Equal,
  Calculator,
  Parentheses,
  Variable
};

const groupIcon = computed(() => iconMap[props.group.icon] || Variable);
</script>

<style lang="scss" scoped>
.palette-group {
  min-width: 220px;
  flex: 0 0 auto;
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 12px;

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    border-bottom: 1px solid #f5f5f5;
    cursor: pointer;
    font-size: 13px;
    font-weight: 600;
    color: #606266;
    transition: background 0.15s;
    user-select: none;

    &:hover {
      background: #f5f7fa;
    }
  }

  &__items {
    padding: 4px 12px 12px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }
}
</style>
