<template>
  <div class="tech-map-wrapper">
    <div class="scan-light"></div>
    <div class="radar-ring ring-1"></div>
    <div class="radar-ring ring-2"></div>
    <div class="corner top-left"></div>
    <div class="corner top-right"></div>
    <div class="corner bottom-left"></div>
    <div class="corner bottom-right"></div>
    <div class="decor-grid"></div>

    <div class="side-decor left-panel">
      <div class="panel-header">
        <div class="header-left">
          <transition name="fade">
            <div
              v-if="isDrillDown"
              class="mini-back-btn"
              @click="handleBackToChina"
              title="返回上级"
            >
              <span class="arrow-icon">❮</span>
            </div>
          </transition>
        </div>

        <div class="header-content">
          <div class="panel-title" :title="displayCityName">
            {{ displayCityName }}
          </div>
          <div class="sub-title">气象实时预报</div>
        </div>

        <div class="header-right"></div>
      </div>

      <div class="divider-line"></div>

      <div class="weather-wrapper">
        <WeatherMonitor ref="weatherMonitorRef" />
      </div>

      <div class="v-ruler">
        <span v-for="i in 8" :key="i" class="tick" :style="{ opacity: 1 - i * 0.1 }"></span>
      </div>
    </div>

    <ChinaMapRight @category-change="handleProductChange" />

    <div class="decor-label">智慧农业数据分析平台 // 实时监控</div>

    <div class="map-core">
      <ChinaMap ref="chinaMapRef" :city-data="cityData" @region-change="handleMapChange" @map-level-change="handleMapLevelChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import ChinaMap from './ChinaMap.vue'
import ChinaMapRight from './ChinaMapRight.vue'
import WeatherMonitor from './WeatherMonitor.vue'

import { mapLocation } from '../../stores/store.js'
import provinceCapitalMap from '../../assets/json/provinceCapitalMap.json'
import cityCodeMap from '../../assets/json/cityCodeMap.json'

// Props 定义
const props = defineProps({
  cityData: { type: Array, default: () => [] },
})

// 引用与状态
const chinaMapRef = ref(null)
const weatherMonitorRef = ref(null)
const mapLocationStore = mapLocation()
const isChinaMap = ref(true)

const isDrillDown = computed(() => !isChinaMap.value)

const displayCityName = computed(() => {
  const currentProvince = mapLocationStore.currentProvince
  const currentCity = mapLocationStore.currentCity

  if (currentProvince === '全国' || currentProvince === '北京市') {
    return '北京市'
  }

  if (provinceCapitalMap[currentProvince]) {
    const capitalCity = provinceCapitalMap[currentProvince].name
    if (currentCity !== capitalCity) {
      return currentCity
    }
    return capitalCity
  }

  return currentCity
})

const handleProductChange = (category) => {
  console.log('父组件感知到产品切换:', category.name)
}

const handleMapChange = async (regionName) => {
  if (regionName === '全国' || regionName === '北京市') {
    mapLocationStore.resetLocation()
    return
  }

  if (provinceCapitalMap[regionName]) {
    mapLocationStore.setCurrentProvince(regionName)
  } else if (cityCodeMap[regionName]) {
    mapLocationStore.setCurrentCity(regionName)
  }
}

const handleMapLevelChange = (isChina) => {
  isChinaMap.value = isChina
}

const handleBackToChina = async () => {
  await chinaMapRef.value?.backToPrevious()
}

const loadProvinceAndHighlightCity = async (provinceName, cityName) => {
  await chinaMapRef.value?.loadProvinceAndHighlightCity(provinceName, cityName)
}

defineExpose({
  loadProvinceAndHighlightCity,
})

onMounted(() => {
  weatherMonitorRef.value?.initWeather()
})
</script>

<style scoped>
/* ==================== 
   1. 基础容器与背景 
   ==================== */
.tech-map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  padding: 15px;
  box-sizing: border-box;
  background-color: #0a192f;
  background-image:
    linear-gradient(rgba(10, 25, 47, 0.7), rgba(10, 25, 47, 0.7)), url('/img/bg.png');
  background-size: cover;
  overflow: hidden;
  color: #fff;
  /* 优化全局字体，优先使用系统现代字体 */
  font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', 'Arial', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased; /* 字体抗锯齿优化 */
  -moz-osx-font-smoothing: grayscale;
}

/* ==================== 
   2. 左侧面板样式
   ==================== */
.side-decor {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 18%;          
  min-width: 235px;    
  max-width: 270px;    
  height: 85%;
  z-index: 30;
  display: flex;
  flex-direction: column;
  pointer-events: auto;
  gap: 0; 
}

.left-panel {
  left: 20px;
  background: linear-gradient(145deg, rgba(5, 20, 35, 0.4) 0%, rgba(5, 20, 35, 0.2) 100%);
  backdrop-filter: blur(3px); 
  border: 1px solid rgba(0, 247, 255, 0.15); 
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  border-radius: 6px; 
  overflow: hidden;
}

/* ==================== 
   3. 头部 Header (字体优化版)
   ==================== */
.panel-header {
  position: relative;
  height: 42px; 
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 6px; 
  flex-shrink: 0;
  background: linear-gradient(90deg, rgba(0, 247, 255, 0.05), transparent);
  border-bottom: 1px solid rgba(0, 247, 255, 0.1);
}

.header-left,
.header-right {
  width: 24px; 
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.panel-title {
  /* 增大字号，加粗但不至于太重 */
  font-size: 16px; 
  color: #fff;
  font-weight: 600; 
  /* 增加字间距，提升科技感 */
  letter-spacing: 1.2px; 
  text-shadow: 0 0 10px rgba(0, 247, 255, 0.5);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  text-align: center;
}

.sub-title {
  /* 缩小字号，使用无衬线等宽字体风格 */
  font-size: 10px; 
  color: #45d0b2;
  opacity: 0.9;
  line-height: 1;
  margin-top: 2px;
  letter-spacing: 0.8px;
  text-transform: uppercase; /* 转大写 */
  font-weight: 500;
}

/* 返回按钮 */
.mini-back-btn {
  width: 22px; 
  height: 22px;
  border: 1px solid rgba(69, 208, 178, 0.5);
  background: rgba(0, 20, 40, 0.6);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 0 5px rgba(69, 208, 178, 0.1);
}

.mini-back-btn:hover {
  background: #45d0b2;
  border-color: #45d0b2;
  box-shadow: 0 0 12px rgba(69, 208, 178, 0.5);
  transform: scale(1.05); /* 微放大效果 */
}

.mini-back-btn:hover .arrow-icon {
  color: #0b1525;
}

.arrow-icon {
  font-size: 10px;
  color: #45d0b2;
  font-weight: bold;
  transform: translateY(-1px); /* 视觉居中微调 */
}

.divider-line {
  width: 100%;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(0, 247, 255, 0.3) 50%,
    transparent 100%
  );
  margin: 0;
  flex-shrink: 0;
  opacity: 0.5;
}

/* ==================== 
   4. 内容区域 
   ==================== */
.weather-wrapper {
  flex: 1;
  overflow: hidden;
  padding: 4px 5px; 
  display: flex;
  flex-direction: column;
}

/* ==================== 
   5. 装饰动画与组件 
   ==================== */
.v-ruler {
  height: 8px; 
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 4px;
  align-items: flex-end;
  padding-bottom: 2px;
  background: rgba(0, 0, 0, 0.1); 
  flex-shrink: 0;
}

.tick {
  width: 2px;
  height: 3px;
  background: #00f7ff;
  border-radius: 1px;
}

/* 地图核心 */
.map-core {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 10;
}

/* 角落装饰 */
.corner {
  position: absolute;
  width: 30px;
  height: 30px;
  border: 2px solid rgba(0, 247, 255, 0.6);
  z-index: 20;
  pointer-events: none;
}

.top-left { top: 10px; left: 10px; border-right: 0; border-bottom: 0; }
.top-right { top: 10px; right: 10px; border-left: 0; border-bottom: 0; }
.bottom-left { bottom: 10px; left: 10px; border-right: 0; border-top: 0; }
.bottom-right { bottom: 10px; right: 10px; border-left: 0; border-top: 0; }

.decor-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(69, 208, 178, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(69, 208, 178, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 1;
  pointer-events: none;
}

.radar-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  border: 1px dashed rgba(69, 208, 178, 0.1);
  z-index: 2;
  pointer-events: none;
}

.ring-1 {
  width: 60%;
  aspect-ratio: 1;
  animation: rotate 60s linear infinite;
}

.ring-2 {
  width: 85%;
  aspect-ratio: 1;
  border-style: dotted;
  animation: rotate-reverse 80s linear infinite;
}

.scan-light {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  background: linear-gradient(
    180deg,
    transparent 0%,
    rgba(0, 247, 255, 0.05) 50%,
    transparent 100%
  );
  background-size: 100% 200%;
  animation: scan 6s linear infinite;
  z-index: 5;
  pointer-events: none;
}

.decor-label {
  position: absolute;
  bottom: 20px;
  right: 30px;
  font-size: 12px;
  color: rgba(0, 247, 255, 0.6);
  font-family: 'JetBrains Mono', monospace; /* 代码风格字体 */
  z-index: 20;
  text-shadow: 0 0 5px rgba(0, 247, 255, 0.3);
  letter-spacing: 1px;
}

@keyframes rotate { from { transform: translate(-50%, -50%) rotate(0deg); } to { transform: translate(-50%, -50%) rotate(360deg); } }
@keyframes rotate-reverse { from { transform: translate(-50%, -50%) rotate(360deg); } to { transform: translate(-50%, -50%) rotate(0deg); } }
@keyframes scan { 0% { top: -100%; opacity: 0; } 50% { opacity: 1; } 100% { top: 100%; opacity: 0; } }

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>