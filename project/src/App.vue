<template>
  <div class="app-container">
    <el-container>
      <!-- 侧边栏部分 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="app-aside">
        <div class="sidebar-header">
          <div class="app-logo" :class="{ 'collapsed': isCollapse }">
            <img src="data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgICAgICAgPHBhdGggZD0iTTExLjk5OTkgMi43MTc5QzkuMTczMTkgMi43MTc5IDYuNDI0NzEgMy44NjY4MyA0LjQwMTk5IDUuODg5NTZDMi4zNzkyNiA3LjkxMjI4IDEuMjMwMzIgMTAuNjYwOCAxLjIzMDMyIDEzLjQ4NzVDMS4yMzAzMiAxNi4zMTQyIDIuMzc5MjYgMTkuMDYyNiA0LjQwMTk5IDIxLjA4NTRDNS4zOTMyOSAyMi4wNzY3IDYuNTY4MzEgMjIuODYwMiA3Ljg2MTEzIDIzLjM5MTlDOS4xNTM5NSAyMy45MjM2IDEwLjU0MDggMjQuMTk1OSAxMS45NDIgMjQuMTk1OUMxMy4zNDMzIDI0LjE5NTkgMTQuNzMwMSAyMy45MjM2IDE2LjAyMyAyMy4zOTE5QzE3LjMxNTggMjIuODYwMiAxOC40OTA4IDIyLjA3NjcgMTkuNDgyMSAyMS4wODU0QzIxLjUwNDggMTkuMDYyNiAyMi42NTM4IDE2LjMxNDIgMjIuNjUzOCAxMy40ODc1QzIyLjY1MzggMTAuNjYwOCAyMS41MDQ4IDcuOTEyMjggMTkuNDgyMSA1Ljg4OTU2QzE3LjQ1OTQgMy44NjY4MyAxNC43MTEgMi43MTc5IDExLjk5OTkgMi43MTc5WiIgZmlsbD0idXJsKCNwYWludDBfbGluZWFyKSIvPgogICAgICAgIDxwYXRoIGQ9Ik0xNy45MjY4IDcuODQwN0MxNi4wNjM2IDUuOTc4MjMgMTMuNDI2OCA1LjA0MDcgMTAuNzk5OSA1LjIwNDAyQzguMTczMSA1LjM2NzM1IDUuNjU4NjkgNi42MTQ4OSAzLjk4NTY1IDguNjk4M0MyLjMxMjYyIDEwLjc4MTcgMS42MzU0NiAxMy40OTU1IDIuMTAxNjkgMTYuMTMxNkMyLjU2NzkxIDE4Ljc2NzcgNC4xMzY1NCAyMS4wODY3IDYuNDExMzIgMjIuNDg5MkM4LjE2NDY3IDIxLjI0MiA5LjY3ODE0IDE5LjY4NTUgMTAuODY4OCAxNy44OTA0QzEyLjA1OTQgMTYuMDk1MyAxMi45MDgxIDE0LjA5MTUgMTMuMzc0MyAxMS45ODdDMTQuMjE5MyAxMy4yMTIxIDE0Ljc2NDcgMTQuNjIxNSAxNC45NjA2IDE2LjA5OEMxNS4xNTY1IDE3LjU3NDYgMTQuOTk2OSAxOS4wNzQ3IDE0LjQ5NTkgMjAuNDcwM0MxNi41NDU5IDE5LjI2MDMgMTguMTA5NyAxNy4zNzIyIDE4LjkwMTMgMTUuMTQyMkMxOS42OTI5IDEyLjkxMjMgMTkuNjYzNCAxMC40ODA1IDE4LjgxODQgOC4yNzA0MUMxOC41MjcyIDguMTIwMDQgMTguMjMyOSA3Ljk3NzA1IDE3LjkzNjggNy44NDI1OUwxNy45MjY4IDcuODQwN1oiIGZpbGw9IndoaXRlIi8+CiAgICAgICAgPGRlZnM+CiAgICAgICAgICA8bGluZWFyR3JhZGllbnQgaWQ9InBhaW50MF9saW5lYXIiIHgxPSIxLjIzMDMyIiB5MT0iMTMuNDg3NSIgeDI9IjIyLjY1MzgiIHkyPSIxMy40ODc1IiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiMwMGM5ZmYiLz4KICAgICAgICAgICAgPHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjOTJmZTlkIi8+CiAgICAgICAgICA8L2xpbmVhckdyYWRpZW50PgogICAgICAgIDwvZGVmcz4KICAgICAgPC9zdmc+" alt="Logo" class="logo-image">
            <span class="logo-name" v-if="!isCollapse">价溯云图</span>
          </div>
          <el-button  type="text" class="collapse-btn" @click="isCollapse = !isCollapse">
            <el-icon>
                   <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
           </el-button>
        </div>
        <div class="menu-container">
          <el-menu :default-active="activeMenu" router class="app-menu" :collapse="isCollapse" @open="handleOpen" @close="handleClose" :collapse-transition="false">
            <el-menu-item index="/ai" class="menu-item">
              <el-icon><ChatDotRound /></el-icon>
              <template #title>基于天气的AI价格预测</template>
            </el-menu-item> 
            <el-menu-item index="/user" class="menu-item">
              <el-icon><User /></el-icon>
              <template #title>全国蔬菜最新价格概览</template>
            </el-menu-item>
            <el-menu-item index="/market" class="menu-item">
              <el-icon><document /></el-icon>
              <template #title>全国蔬菜历史价格汇总</template>
            </el-menu-item>
            <el-menu-item index="/jobber" class="menu-item">
              <el-icon><icon-menu /></el-icon>
              <template #title>全国各省区域价格详情</template>
            </el-menu-item>

          </el-menu>
        </div>
      </el-aside>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Document,
  Menu as IconMenu,
  User,
  ChatDotRound,
  Fold,
  Expand,
  QuestionFilled
} from '@element-plus/icons-vue'

import { useRoute } from 'vue-router'
const route = useRoute()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

const handleOpen = (key, keyPath) => {
  console.log(key, keyPath);
};

const handleClose = (key, keyPath) => {
  console.log(key, keyPath);
};
</script>

<style lang="scss" scoped>
:root {
  --primary-gradient: linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%);
  --secondary-gradient: linear-gradient(135deg, #64b5f6 0%, #1e88e5 100%);
  --primary-color: #1e88e5;
  --accent-color: #64b5f6;
  --text-primary: #ffffff;
  --text-secondary: #e3f2fd;
  --surface-1: rgba(255, 255, 255, 0.1);
  --surface-2: rgba(255, 255, 255, 0.05);
  --border-color: rgba(255, 255, 255, 0.15);
}

.app-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: var(--primary-gradient);
}

.app-aside {
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-right: 1px solid var(--border-color);
  transition: width 0.3s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  height: 60px;
}

.app-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  
  &.collapsed {
    justify-content: center;
  }
}

.logo-image {
  height: 28px;
  width: auto;
  filter: brightness(0) invert(1);
}

.logo-name {
  font-weight: 500;
  font-size: 25px;
  color: var(--text-primary);
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
  white-space: nowrap;
}

.collapse-btn {
  font-size: 18px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }
}

.menu-container {
  flex: 1;
  overflow-y: auto;
}

.app-menu {
  border-right: none;
  background: transparent;
  
  :deep(.el-menu-item) {
    color: var(--text-primary);
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
    
    &.is-active {
      background: rgba(255, 255, 255, 0.2) !important;
      color: var(--text-primary) !important;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 4px;
        background: var(--text-primary);
        border-radius: 0 4px 4px 0;
      }
    }
  }
  
  :deep(.el-icon) {
    color: var(--text-primary);
  }
}

.menu-item {
  margin: 8px 0;
  border-radius: 0 24px 24px 0;
  margin-right: 10px;
  
  &.is-active {
    background: rgba(255, 255, 255, 0.2) !important;
    color: var(--text-primary) !important;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: var(--text-primary);
      border-radius: 0 4px 4px 0;
    }
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.1) !important;
  }
}

.app-main {
  padding: 16px;
  overflow-y: auto;
  height: 100vh;
  background: var(--primary-gradient);
}
</style>