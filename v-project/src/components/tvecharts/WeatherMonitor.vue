<template>
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
        <div class="row-center">
          <span class="condition-text">{{ day.textDay }}</span>
          <span class="wind-text">{{ day.windDirDay }} {{ day.windScaleDay }}级</span>
        </div>
        <div class="row-right">
          <span class="temp-text">
            {{ day.tempMin }}°/<span class="max-temp">{{ day.tempMax }}°</span>
          </span>
          <span class="humidity-text">{{ day.humidity }}%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { get7DayWeather } from '../../api/requestFuntion.js'
import { mapLocation } from '../../stores/store.js'
import provinceCapitalMap from '../../assets/json/provinceCapitalMap.json'
import cityCodeMap from '../../assets/json/cityCodeMap.json'
import weatherIconMap from '../../assets/json/weatherIconMap.json'

const mapLocationStore = mapLocation()

const weatherForecast = ref([])
const loading = ref(false)

const getWeatherIcon = (text) => weatherIconMap[text] || '🌤️'

const formatDay = (dateString) => {
  const date = new Date(dateString)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const getCurrentCityCode = () => {
  const currentProvince = mapLocationStore.currentProvince
  const currentCity = mapLocationStore.currentCity

  if (currentProvince === '全国' || currentProvince === '北京市') {
    return '101010100'
  }

  if (currentCity && cityCodeMap[currentCity]) {
    return cityCodeMap[currentCity]
  }

  if (provinceCapitalMap[currentProvince]) {
    return provinceCapitalMap[currentProvince].code
  }

  return '101010100'
}

const fetchWeather = async (code) => {
  try {
    weatherForecast.value = await get7DayWeather(code)
  } catch (e) {
    console.error('天气获取失败', e)
  } finally {
    loading.value = false
  }
}

watch(
  () => mapLocationStore.currentProvince,
  async (newProvince, oldProvince) => {
    if (newProvince && newProvince !== oldProvince && provinceCapitalMap[newProvince]) {
      const capitalInfo = provinceCapitalMap[newProvince]
      loading.value = true
      await fetchWeather(capitalInfo.code)
    }
  }
)

watch(
  () => mapLocationStore.currentCity,
  async (newCity, oldCity) => {
    if (newCity && newCity !== oldCity && cityCodeMap[newCity]) {
      loading.value = true
      await fetchWeather(cityCodeMap[newCity])
    }
  }
)

const initWeather = () => {
  const currentCode = getCurrentCityCode()
  fetchWeather(currentCode)
}

defineExpose({
  initWeather
})
</script>

<style scoped>
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
</style>
