<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px">
        <MENU></MENU>
      </el-aside>
      <el-container>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import MENU from '/src/components/menu.vue'
import User from '/src/pages/user.vue'
</script>

<style lang="scss" scoped>
//标题样式 - 更新为与ai.vue一致的渐变蓝色背景
.screen-title-container {
  width: 100%;
  background: linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%);
  padding: 0px 0 15px;
  position: relative;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.screen-title {
  position: relative;
  text-align: center;
  padding: 15px 0;
}

.title-text {
  font-size: 3vw;
  font-weight: bold;
  letter-spacing: 4px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  display: inline-block;
  padding: 0 60px;
  background: linear-gradient(to right, #2db4a0, #1a7c6c);
  -webkit-background-clip: text;
  color: transparent;
}

.decoration-line {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent,
    #2db4a0,
    transparent
  );
  opacity: 0.8;
}

/* 添加两侧装饰线 */
.screen-title::before,
.screen-title::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 20%;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent,
    #2db4a0
  );
}

.screen-title::before {
  left: 5%;
  transform: translateY(-50%);
}

.screen-title::after {
  right: 5%;
  transform: translateY(-50%) rotate(180deg);
}

@media (max-width: 768px) {
  .title-text {
    font-size: 32px;
  }
}

//主体内容 - 更新背景色和卡片样式
.dashboard-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding: 20px;
}

/* 指标卡片样式 - 更新为与ai.vue一致的卡片样式 */
.metric-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  transition: transform 0.3s;
}

.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.metric-icon {
  background: #e8f7f5;
  border-radius: 50%;
  padding: 12px;
  margin-right: 15px;
  color: #2db4a0;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #1565c0;
  line-height: 1.2;
}

.metric-label {
  color: #666;
  font-size: 14px;
}

/* 分割线样式 */
.section-divider {
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(45, 180, 160, 0.3),
    transparent
  );
  margin: 30px 0;
}

/* 主图表区 */
.main-chart {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.chart-title {
  color: #1565c0;
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 15px;
  padding-left: 8px;
  border-left: 4px solid #2db4a0;
}

/* 侧边栏样式 */
.side-panel {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.panel-title {
  color: #1565c0;
  font-size: 16px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e3f2fd;
}

.data-item {
  margin-bottom: 15px;
}

.data-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

/* 进度条颜色适配 */
:deep(.el-progress-bar__inner) {
  background: linear-gradient(135deg, #2db4a0 0%, #1a7c6c 100%) !important;
}
</style>