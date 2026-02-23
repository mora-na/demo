<template>
  <template v-for="item in menus" :key="item.id">
    <el-sub-menu v-if="item.children?.length" :index="menuIndex(item)">
      <template #title>
        <component :is="menuIconComponent(item)" class="menu-icon"/>
        <span>{{ menuLabel(item) }}</span>
      </template>
      <NavTree :menus="item.children"/>
    </el-sub-menu>

    <el-menu-item v-else :index="menuIndex(item)">
      <component :is="menuIconComponent(item)" class="menu-icon"/>
      <span>{{ menuLabel(item) }}</span>
    </el-menu-item>
  </template>
</template>

<script lang="ts" setup>
import type {MenuTree} from "../api/auth";
import {menuIconComponent} from "../utils/menuIcon";
import {menuIndex} from "../utils/menuIndex";

defineOptions({name: "NavTree"});

defineProps<{ menus: MenuTree[] }>();

function menuLabel(menu: MenuTree): string {
  return menu.name || menu.code || "-";
}
</script>
