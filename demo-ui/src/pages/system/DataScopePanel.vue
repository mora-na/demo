<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("dataScope.title") }}</div>
        <div class="module-sub">{{ t("dataScope.subtitle") }}</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="data-scope-tabs">
      <el-tab-pane :label="t('dataScope.tabs.overview')" name="overview" lazy>
        <DataScopeOverview/>
      </el-tab-pane>
      <el-tab-pane :label="t('dataScope.tabs.mapping')" name="mapping" lazy>
        <DataScopeMapping/>
      </el-tab-pane>
      <el-tab-pane :label="t('dataScope.tabs.user')" name="user" lazy>
        <DataScopeUserOverrides/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import DataScopeOverview from "./DataScopeOverview.vue";
import DataScopeMapping from "./DataScopeMapping.vue";
import DataScopeUserOverrides from "./DataScopeUserOverrides.vue";

const props = defineProps<{
  activeCode?: string;
}>();

const {t} = useI18n();
const activeTab = ref("overview");

const mappedTab = computed(() => {
  if (!props.activeCode) {
    return "overview";
  }
  if (props.activeCode.includes("mapping")) {
    return "mapping";
  }
  if (props.activeCode.includes("user")) {
    return "user";
  }
  return "overview";
});

watch(
    () => mappedTab.value,
    (value) => {
      activeTab.value = value;
    },
    {immediate: true}
);
</script>

<style scoped>
.system-module {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.module-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.module-title {
  font-size: 16px;
  font-weight: 600;
}

.module-sub {
  font-size: 12px;
  color: var(--muted);
}

</style>
