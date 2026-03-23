<template>
  <template v-for="item in menus" :key="item.id">
    <el-sub-menu v-if="item.children?.length" :index="menuIndex(item)">
      <template #title>
        <el-tooltip
            :content="menuLabel(item)"
            :disabled="!collapsed"
            :show-after="200"
            placement="right"
        >
          <span class="menu-item-content">
            <component :is="menuIconComponent(item)" class="menu-icon"/>
            <span class="menu-label">{{ menuLabel(item) }}</span>
          </span>
        </el-tooltip>
      </template>
      <NavTree :collapsed="collapsed" :menus="item.children"/>
    </el-sub-menu>

    <el-menu-item v-else :index="menuIndex(item)">
      <el-tooltip
          :content="menuLabel(item)"
          :disabled="!collapsed"
          :show-after="200"
          placement="right"
      >
        <span class="menu-item-content">
          <component :is="menuIconComponent(item)" class="menu-icon"/>
          <span class="menu-label">{{ menuLabel(item) }}</span>
        </span>
      </el-tooltip>
    </el-menu-item>
  </template>
</template>

<script lang="ts" setup>
import type {MenuTree} from "../api/auth";
import {menuIconComponent} from "../utils/menuIcon";
import {menuIndex} from "../utils/menuIndex";

defineOptions({name: "NavTree"});

defineProps<{ menus: MenuTree[]; collapsed?: boolean }>();

function menuLabel(menu: MenuTree): string {
  return menu.name || menu.code || "-";
}
</script>
