<template>
  <div class="map-container">
    <div ref="chartRef" class="echarts-box"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, shallowRef } from 'vue'
import * as echarts from 'echarts'
import { mapCity } from '../../stores/store.js'

const mapCityStore = mapCity()

// 定义向父组件发送事件
const emit = defineEmits(['region-change'])

const props = defineProps({
  cityData: {
    type: Array,
    default: () => [],
  },
})

// 省份映射表
const provinceCodeMap = {
  // 直辖市
  北京市: '110000',
  天津市: '120000',
  上海市: '310000',
  重庆市: '500000',

  // 省
  河北省: '130000',
  山西省: '140000',
  辽宁省: '210000',
  吉林省: '220000',
  黑龙江省: '230000',
  江苏省: '320000',
  浙江省: '330000',
  安徽省: '340000',
  福建省: '350000',
  江西省: '360000',
  山东省: '370000',
  河南省: '410000',
  湖北省: '420000',
  湖南省: '430000',
  广东省: '440000',
  海南省: '460000',
  四川省: '510000',
  贵州省: '520000',
  云南省: '530000',
  陕西省: '610000',
  甘肃省: '620000',
  青海省: '630000',
  台湾省: '710000',

  // 自治区
  内蒙古自治区: '150000',
  广西壮族自治区: '450000',
  西藏自治区: '540000',
  宁夏回族自治区: '640000',
  新疆维吾尔自治区: '650000',

  // 特别行政区
  香港特别行政区: '810000',
  澳门特别行政区: '820000',
}

const chartRef = ref(null)
// 使用 shallowRef 优化 ECharts 实例性能
const chartInstance = shallowRef(null)
const currentMap = ref('china')

// 加载地图数据
const loadMapData = async (mapName, adcode) => {
  chartInstance.value.showLoading({
    text: '系统数据加载中...',
    color: '#00f7ff',
    textColor: '#00f7ff',
    maskColor: 'rgba(10, 30, 60, 0.5)',
  })

  try {
    const response = await fetch(`https://geo.datav.aliyun.com/areas_v3/bound/${adcode}_full.json`)
    const geoJson = await response.json()

    echarts.registerMap(mapName, geoJson)
    currentMap.value = mapName

    chartInstance.value.hideLoading()
    setOptions(mapName)
  } catch (error) {
    console.error('地图加载失败:', error)
    chartInstance.value.hideLoading()
  }
}

// 返回全国视图的方法（供父组件调用）
const backToChina = async () => {
  await loadMapData('china', '100000')
  emit('region-change', '全国')
}

// 暴露方法给父组件
defineExpose({
  backToChina,
})

// 初始化图表
const initChart = async () => {
  if (!chartRef.value) return
  chartInstance.value = echarts.init(chartRef.value)

  await loadMapData('china', '100000')

  // --- 监听点击事件 ---
  chartInstance.value.on('click', (params) => {
    if (params.componentType === 'geo') {
      // 1. 如果还在全国地图，并且点击了省份
      if (currentMap.value === 'china') {
        const adcode = provinceCodeMap[params.name]
        if (adcode) {
          // 下钻地图
          loadMapData(params.name, adcode)
          mapCityStore.setCurrentProvince(params.name)
        }
      }
      // 2. 如果已经是省份地图，点击了具体的市
      else {
        // 尝试加载市级地图
        // 假设市级adcode是在省级adcode基础上加上0101（注：实际项目可能需要更精确的市级adcode映射）
        const provinceAdcode = provinceCodeMap[currentMap.value]
        if (provinceAdcode) {
          const cityAdcode = `${provinceAdcode}0101`

          loadMapData(params.name, cityAdcode)
            .catch(() => {
              console.log('没有市级地图数据，仅更新天气信息')
            })
            .finally(() => {
              // 无论是否加载成功地图，都通知父组件更新数据
              mapCityStore.setCurrentCity(params.name)
              emit('region-change', params.name)
            })
        } else {
          // 如果找不到对应省代码，直接切换数据
          mapCityStore.setCurrentCity(params.name)
          emit('region-change', params.name)
        }
      }
    }
  })
}

// 设置配置项
const setOptions = (mapName) => {
  if (!chartInstance.value) return

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0,0,0,0.8)',
      borderColor: '#45d0b2', // Tooltip 边框保持原色
      borderWidth: 1,
      textStyle: { color: '#fff' },
    },
    geo: {
      map: mapName,
      roam: true,
      zoom: 1.2,
      label: {
        show: mapName !== 'china',
        color: '#7fffd4', // 字体颜色提亮
        fontSize: 10,
      },
      itemStyle: {
        // 区域填充渐变
        areaColor: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(69, 208, 178, 0.1)' },
            { offset: 1, color: 'rgba(0, 20, 60, 0.9)' },
          ],
        },
        // === 核心修改：高亮边界线 ===
        borderColor: '#b9f8f4', // 接近白色的亮青色
        borderWidth: 1.5, // 线条加粗
        shadowColor: '#00ffff', // 荧光青色发光
        shadowBlur: 10, // 发光晕染范围
        shadowOffsetX: 0,
        shadowOffsetY: 0,
      },
      emphasis: {
        label: { show: true, color: '#fff' },
        itemStyle: {
          areaColor: 'rgba(69, 208, 178, 0.8)', // 悬浮时填充变亮
          borderColor: '#ffffff', // 悬浮时边框纯白
          borderWidth: 2,
          shadowBlur: 20, // 悬浮时发光更强
          shadowColor: '#fff',
        },
      },
    },
    series: [], // 如果有散点图等 series 数据可在此添加
  }
  chartInstance.value.setOption(option, true)
}

const handleResize = () => chartInstance.value?.resize()

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
})
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  position: relative;
  background: transparent;
}

.echarts-box {
  width: 100%;
  height: 100%;
}
</style>
