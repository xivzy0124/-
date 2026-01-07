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

      <div class="weather-monitor">
        <div v-if="loading" class="no-data">
          <span class="blinking">数据同步中...</span>
        </div>
        <div v-else-if="weatherForecast.length === 0" class="no-data">
          <span>暂无数据</span>
        </div>
        <div v-else class="weather-list">
          <div v-for="(day, index) in weatherForecast" :key="day.fxDate" class="weather-row">
            <div class="row-left">
              <span class="date-text">{{ index === 0 ? '今日' : formatDay(day.fxDate) }}</span>
              <span class="weather-icon">{{ getWeatherIcon(day.textDay) }}</span>
            </div>
            <div class="row-right">
              <span class="temp-text">
                {{ day.tempMin }}°/<span class="max-temp">{{ day.tempMax }}°</span>
              </span>
              <span class="condition-text">{{ day.textDay }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="v-ruler">
        <span v-for="i in 8" :key="i" class="tick"></span>
      </div>
    </div>

    <ChinaMapRight @category-change="handleProductChange" />

    <div class="decor-label">智慧农业数据分析平台 // 实时监控</div>

    <div class="map-core">
      <ChinaMap ref="chinaMapRef" :city-data="cityData" @region-change="handleMapChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import ChinaMap from './ChinaMap.vue'
import ChinaMapRight from './ChinaMapRight.vue'

import { get7DayWeather } from '../../api/requestFuntion.js'
import { mapLocation } from '../../stores/store.js'
import provinceCapitalMap from '../../assets/json/provinceCapitalMap.json'
import cityCodeMap from '../../assets/json/cityCodeMap.json'
import weatherIconMap from '../../assets/json/weatherIconMap.json'

// Props 定义
const props = defineProps({
  cityData: { type: Array, default: () => [] },
})

// 引用与状态
const chinaMapRef = ref(null)
const mapLocationStore = mapLocation()

// 天气相关状态
const weatherForecast = ref([])
const loading = ref(false)

// 计算属性：判断是否处于下钻状态（非北京/非默认状态）
const isDrillDown = computed(() => {
  return mapLocationStore.currentProvince !== '北京' && mapLocationStore.currentProvince !== '全国'
})

// 计算属性：显示正确的城市名称
const displayCityName = computed(() => {
  const currentProvince = mapLocationStore.currentProvince
  const currentCity = mapLocationStore.currentCity

  // 如果是全国，显示北京市
  if (currentProvince === '全国' || currentProvince === '北京市') {
    return '北京市'
  }

  // 如果是省份级别
  if (provinceCapitalMap[currentProvince]) {
    const capitalCity = provinceCapitalMap[currentProvince].name
    // 如果当前城市不等于省会城市，说明点击了具体城市，显示当前城市
    if (currentCity !== capitalCity) {
      return currentCity
    }
    // 否则显示省会城市
    return capitalCity
  }

  // 如果是具体城市，显示城市名称
  return currentCity
})

// --- 工具函数 ---

// 获取天气图标
const getWeatherIcon = (text) => weatherIconMap[text] || '🌤️'

// 格式化日期
const formatDay = (dateString) => {
  const date = new Date(dateString)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// --- 核心逻辑 ---

// 1. 处理来自 ChinaMapRight 的产品切换事件
const handleProductChange = (category) => {
  console.log('父组件感知到产品切换:', category.name)
  // 如果需要根据产品获取地图分布数据，可以在这里调用接口
  // fetchMapDataByProduct(category.name)
}

// 2. 处理地图区域切换 (点击地图下钻)
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
    await fetchWeather('101010100')
    return
  }

  let targetCode = ''
  let displayName = regionName

  // 查找映射代码
  if (provinceCapitalMap[regionName]) {
    // 点击的是省份，更新省份状态，城市显示省会
    mapLocationStore.setCurrentProvince(regionName)
    mapLocationStore.setCurrentCity(provinceCapitalMap[regionName].name)
    targetCode = provinceCapitalMap[regionName].code
    displayName = provinceCapitalMap[regionName].name
  } else if (cityCodeMap[regionName]) {
    // 点击的是具体城市，更新城市状态
    mapLocationStore.setCurrentCity(regionName)
    targetCode = cityCodeMap[regionName]
  }

  // 如果找到了对应的城市代码，更新状态并获取天气
  if (targetCode) {
    loading.value = true
    console.log('更新后的 store 状态:', {
      省份: mapLocationStore.currentProvince,
      城市: mapLocationStore.currentCity,
      蔬菜: mapLocationStore.currentProduct,
    })
    console.log('天气代码:', targetCode)
    await fetchWeather(targetCode)
  }
}

// 3. 处理返回按钮点击
const handleBackToChina = async () => {
  // 调用地图子组件的方法返回上一级
  await chinaMapRef.value?.backToPrevious()
}

// 4. 获取天气数据
const fetchWeather = async (code) => {
  try {
    weatherForecast.value = await get7DayWeather(code)
  } catch (e) {
    console.error('天气获取失败', e)
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  // 初始化加载默认天气
  fetchWeather('101010100')
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
}

.weather-row:last-child {
  border-bottom: none;
}

.row-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
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

.row-right {
  text-align: right;
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-end;
  flex: 1;
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

.condition-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  min-width: 42px;
  text-align: right;
  white-space: nowrap;
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
