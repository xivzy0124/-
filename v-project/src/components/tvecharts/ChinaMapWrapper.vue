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
          <div class="sub-title">气象实时监测</div>
        </div>

        <div class="header-right"></div>
      </div>

      <div class="divider-line"></div>

      <WeatherMonitor ref="weatherMonitorRef" />

      <div class="v-ruler">
        <span v-for="i in 8" :key="i" class="tick"></span>
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
  console.log('=== 开发者日志 - 切换区域 ===')
  console.log('当前 store 状态:', {
    省份: mapLocationStore.currentProvince,
    城市: mapLocationStore.currentCity,
    蔬菜: mapLocationStore.currentProduct,
  })
  console.log('选择的区域:', regionName)

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
  font-family: 'Microsoft YaHei', sans-serif;
}

/* ==================== 
   2. 左侧面板样式 
   ==================== */
.side-decor {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 22%;
  max-width: 300px;
  height: 85%;
  z-index: 30;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: auto;
}

.left-panel {
  left: 20px;
  border-left: 1px solid rgba(0, 247, 255, 0.3);
  padding-left: 15px;
  background: linear-gradient(90deg, rgba(0, 247, 255, 0.1), transparent);
}

/* ==================== 
   3. 头部 Header (左侧专用)
   ==================== */
.panel-header {
  position: relative;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 5px;
  flex-shrink: 0;
}

.header-left,
.header-right {
  width: 30px;
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
  font-size: 18px;
  color: #00f7ff;
  font-weight: bold;
  letter-spacing: 1px;
  text-shadow: 0 0 5px rgba(0, 247, 255, 0.5);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  text-align: center;
}

.sub-title {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1;
  margin-top: 4px;
}

/* 返回按钮 */
.mini-back-btn {
  width: 24px;
  height: 24px;
  border: 1px solid #45d0b2;
  background: rgba(0, 20, 40, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
  pointer-events: auto;
}

.mini-back-btn:hover {
  background: #45d0b2;
  box-shadow: 0 0 10px #45d0b2;
}

.mini-back-btn:hover .arrow-icon {
  color: #000;
}

.arrow-icon {
  font-size: 12px;
  color: #45d0b2;
  padding-right: 2px;
}

.divider-line {
  width: 100%;
  height: 1px;
  background: repeating-linear-gradient(
    90deg,
    rgba(69, 208, 178, 0.3) 0,
    rgba(69, 208, 178, 0.3) 4px,
    transparent 4px,
    transparent 8px
  );
  margin: 5px 0;
  flex-shrink: 0;
}

/* ==================== 
   4. 天气列表样式 
   ==================== */
.weather-monitor {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding-top: 5px;
  min-height: 0;
}

.weather-list {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.weather-row {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 5px;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
  box-sizing: border-box;
  min-height: 40px;
}

.weather-row:last-child {
  border-bottom: none;
}

.row-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 65px;
}

.row-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 2px;
}

.row-right {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  justify-content: center;
  flex: 0 0 60px;
}

.date-text {
  color: rgba(255, 255, 255, 0.7);
  width: 40px;
  font-size: 13px;
  font-weight: 500;
}

.weather-icon {
  font-size: 16px;
  width: 25px;
  text-align: center;
}

.condition-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
}

.wind-text {
  font-size: 10px;
  color: rgba(0, 247, 255, 0.6);
  white-space: nowrap;
}

.row-right {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  justify-content: center;
  flex: 0 0 60px;
}

.temp-text {
  color: #fff;
  font-family: 'Courier New', monospace;
  font-weight: bold;
  font-size: 14px;
  letter-spacing: 0;
}

.max-temp {
  color: #00f7ff;
}

.humidity-text {
  font-size: 10px;
  color: rgba(0, 247, 255, 0.6);
}

.no-data {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.3);
  font-size: 14px;
}

/* ==================== 
   5. 装饰动画与组件 
   ==================== */
.v-ruler {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  opacity: 0.3;
  padding-top: 10px;
  flex-shrink: 0;
}

.tick {
  width: 8px;
  height: 1px;
  background: #00f7ff;
}

/* 地图核心 */
.map-core {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 10;
}

/* 角落 */
.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 2px solid #00f7ff;
  box-shadow: 0 0 8px #00f7ff;
  z-index: 20;
}

.top-left {
  top: 0;
  left: 0;
  border-right: 0;
  border-bottom: 0;
}

.top-right {
  top: 0;
  right: 0;
  border-left: 0;
  border-bottom: 0;
}

.bottom-left {
  bottom: 0;
  left: 0;
  border-right: 0;
  border-top: 0;
}

.bottom-right {
  bottom: 0;
  right: 0;
  border-left: 0;
  border-top: 0;
}

.decor-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(69, 208, 178, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(69, 208, 178, 0.05) 1px, transparent 1px);
  background-size: 30px 30px;
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
  animation: rotate 20s linear infinite;
}

.ring-2 {
  width: 85%;
  aspect-ratio: 1;
  border-style: dotted;
  animation: rotate-reverse 30s linear infinite;
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
    rgba(0, 247, 255, 0.08) 50%,
    transparent 100%
  );
  background-size: 100% 200%;
  animation: scan 4s linear infinite;
  z-index: 5;
  pointer-events: none;
}

.decor-label {
  position: absolute;
  bottom: 10px;
  right: 20px;
  font-size: 10px;
  color: rgba(0, 247, 255, 0.4);
  font-family: monospace;
  z-index: 20;
}

.blinking {
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.5;
  }
}

@keyframes rotate {
  from {
    transform: translate(-50%, -50%) rotate(0deg);
  }

  to {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

@keyframes rotate-reverse {
  from {
    transform: translate(-50%, -50%) rotate(360deg);
  }

  to {
    transform: translate(-50%, -50%) rotate(0deg);
  }
}

@keyframes scan {
  0% {
    top: -100%;
    opacity: 0;
  }

  50% {
    opacity: 1;
  }

  100% {
    top: 100%;
    opacity: 0;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
