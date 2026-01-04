<template>
  <el-menu
    router
    default-active="1"
    class="el-menu-vertical-demo"
    :collapse="isCollapse"
    @open="handleOpen"
    @close="handleClose"
    background-color="#f5f7fa"
    text-color="#1565c0"
    active-text-color="#2db4a0"
  >
    <el-menu-item index="1">
      <el-icon><User /></el-icon>
      <template #title>用户</template>
    </el-menu-item>
    
    <el-menu-item index="2">
      <el-icon><icon-menu /></el-icon>
      <template #title>批发商</template>
    </el-menu-item>
    <el-menu-item index="3">
      <el-icon><document /></el-icon>
      <template #title>批发市场</template>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { ref } from 'vue'
import {
  Document,
  Menu as IconMenu,
  User,
} from '@element-plus/icons-vue'

const isCollapse = ref(false)
const handleOpen = (key, keyPath) => {
  console.log(key, keyPath);
};

const handleClose = (key, keyPath) => {
  console.log(key, keyPath);
};
</script>

<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}

:deep(.el-menu-item) {
  margin: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-menu-item:hover) {
  background-color: #e3f2fd;
  transform: translateX(5px);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%);
  color: white;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: white;
}
</style>