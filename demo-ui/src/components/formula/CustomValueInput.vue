<template>
  <div class="custom-input">
    <el-input
        v-model="customValue"
        class="custom-input__field"
        clearable
        placeholder="输入自定义值"
        size="small"
        @keydown.enter="addCustom"
    >
      <template #append>
        <el-button :disabled="!customValue.trim()" @click="addCustom">
          <Plus :size="14"/>
        </el-button>
      </template>
    </el-input>
  </div>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import {Plus} from "lucide-vue-next";
import {useFormulaStore} from "../../stores/formulaStore";

const store = useFormulaStore();
const customValue = ref("");

function createId() {
  return globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function addCustom() {
  const val = customValue.value.trim();
  if (!val) return;
  if (store.customValues.some((cv) => cv.name === val)) {
    return;
  }
  store.customValues.push({
    id: createId(),
    type: "CONSTANT",
    name: val,
    value: val
  });
  customValue.value = "";
}
</script>

<style lang="scss" scoped>
.custom-input {
  display: flex;
  align-items: center;
}

.custom-input__field {
  width: 220px;
}
</style>
