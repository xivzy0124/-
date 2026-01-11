<template>
  <div class="map-container">
    <div ref="chartRef" class="echarts-box" :class="animateClass"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, shallowRef, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { mapLocation, voiceRecognition } from '../../stores/store.js'
import { getChinaMap, getProvinceMap, getCityMap, getRegionNames } from '../../api/requestFuntion.js'

const mapLocationStore = mapLocation()
const voiceRecognitionStore = voiceRecognition()
const emit = defineEmits(['region-change', 'map-level-change'])

const props = defineProps({
  cityData: {
    type: Array,
    default: () => [],
  },
})

const chartRef = ref(null)
const chartInstance = shallowRef(null)
const currentMapName = ref('china')
const currentAdcode = ref('100000')
const animateClass = ref('')

const heatmapData = ref([])

const timer = ref(null)
const regionNames = ref([])
const lastHighlightName = ref('')
const historyStack = ref([])

const ignoredRegions = [
  '台湾省',
  '香港特别行政区',
  '澳门特别行政区',
]

// --- 颜色配置 (对应 visualMap 的四个层级) ---
const colors = [
  '#00465a', // Low
  '#00759a', // Mid-Low
  '#0abff3', // Mid-High
  '#08e7de'  // High
]

// --- 工具函数：Hex 转 RGBA ---
const hexToRgba = (hex, opacity) => {
  let c = hex.substring(1).split('')
  if (c.length === 3) {
    c = [c[0], c[0], c[1], c[1], c[2], c[2]]
  }
  c = '0x' + c.join('')
  return 'rgba(' + [(c >> 16) & 255, (c >> 8) & 255, c & 255].join(',') + ',' + opacity + ')'
}

// --- 核心修改：生成带渐变样式的模拟数据 ---
const generateMockData = (geoJson) => {
  if (!geoJson || !geoJson.features) return []
  return geoJson.features.map(feature => {
    const value = Math.floor(Math.random() * 1000)
    
    // 1. 根据数值手动计算对应的基准颜色
    let baseColor = colors[0]
    if (value >= 750) baseColor = colors[3]
    else if (value >= 500) baseColor = colors[2]
    else if (value >= 250) baseColor = colors[1]

    return {
      name: feature.properties.name,
      value: value,
      // 2. 为每个板块单独注入渐变样式
      itemStyle: {
        // LinearGradient(x1, y1, x2, y2, stops)
        // 0,0,0,1 代表从图形上方到下方
        areaColor: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: hexToRgba(baseColor, 1) // 顶部：不透明 (100%)
          },
          {
            offset: 1,
            color: hexToRgba(baseColor, 0.2) // 底部：高透明 (20%)，实现渐变变淡效果
          }
        ])
      }
    }
  })
}

const loadMapData = async (mapName, adcode, direction = 'drill') => {
  animateClass.value = ''

  chartInstance.value.showLoading({
    text: '数据运算中...',
    color: '#08e7de', 
    textColor: '#08e7de',
    maskColor: 'rgba(0, 70, 90, 0.8)', 
  })

  try {
    let response
    let geoJson

    if (mapName === 'china') {
      response = await getChinaMap()
    } else if (currentMapName.value === 'china') {
      response = await getProvinceMap(adcode)
    } else {
      response = await getCityMap(adcode)
    }

    if (response) {
      geoJson = response
    } else {
      throw new Error('API返回数据格式错误')
    }

    if (!geoJson) {
      throw new Error('地图数据为空')
    }

    echarts.registerMap(mapName, geoJson)

    currentMapName.value = mapName
    currentAdcode.value = adcode
    
    // 生成数据 (包含渐变逻辑)
    heatmapData.value = generateMockData(geoJson)

    chartInstance.value.hideLoading()
    setOptions(mapName)

    nextTick(() => {
      if (direction === 'drill') {
        animateClass.value = 'map-drill-enter'
      } else if (direction === 'back') {
        animateClass.value = 'map-back-enter'
      } else {
        animateClass.value = ''
      }

      setTimeout(() => {
        animateClass.value = ''
      }, 800)
    })

    emit('map-level-change', mapName === 'china')

    return true
  } catch (error) {
    console.warn('无法加载地图数据:', error)
    chartInstance.value.hideLoading()
    return false
  }
}

// ... (startRandomHighlight, stopRandomHighlight, backToPrevious, loadProvinceAndHighlightCity 保持不变，代码略以节省篇幅，请保留原有的逻辑) ...
const startRandomHighlight = async () => {
  stopRandomHighlight()
  // ... (保留原有逻辑)
}

const stopRandomHighlight = () => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
  if (chartInstance.value && lastHighlightName.value) {
    chartInstance.value.dispatchAction({
      type: 'downplay',
      geoIndex: 0,
      name: lastHighlightName.value
    })
    chartInstance.value.dispatchAction({ type: 'hideTip' })
  }
}

const backToPrevious = async () => {
  if (historyStack.value.length === 0) return
  const prev = historyStack.value.pop()
  await loadMapData(prev.mapName, prev.adcode, 'back')
  emit('region-change', prev.mapName === 'china' ? '全国' : prev.mapName)
  emit('map-level-change', prev.mapName === 'china')
}

const loadProvinceAndHighlightCity = async (provinceName, cityName) => {
  stopRandomHighlight()
  const currentGeoJson = echarts.getMap(currentMapName.value).geoJson
  const feature = currentGeoJson.features.find(
    (f) => f.properties.name === provinceName
  )
  if (feature && feature.properties.adcode) {
    const nextAdcode = feature.properties.adcode
    historyStack.value.push({
      mapName: currentMapName.value,
      adcode: currentAdcode.value
    })
    const success = await loadMapData(provinceName, nextAdcode, 'drill')
    if (success) {
      mapLocationStore.setCurrentProvince(provinceName)
      mapLocationStore.setCurrentCity(cityName)
      emit('region-change', cityName)
      setTimeout(() => {
        if (chartInstance.value) {
          chartInstance.value.dispatchAction({
            type: 'showTip',
            seriesIndex: 0,
            name: cityName
          })
        }
      }, 500)
    } else {
      historyStack.value.pop()
    }
  }
}

defineExpose({
  backToPrevious,
  loadProvinceAndHighlightCity,
})

const initChart = async () => {
  if (!chartRef.value) return
  chartInstance.value = echarts.init(chartRef.value)
  await loadMapData('china', '100000', 'none')

  chartInstance.value.on('click', async (params) => {
    if (params.componentType === 'series' || params.componentType === 'geo') { 
      const clickedRegionName = params.name
      const currentGeoJson = echarts.getMap(currentMapName.value).geoJson
      const feature = currentGeoJson.features.find(
        (f) => f.properties.name === clickedRegionName
      )

      if (feature && feature.properties.adcode) {
        const nextAdcode = feature.properties.adcode
        historyStack.value.push({
          mapName: currentMapName.value,
          adcode: currentAdcode.value
        })

        stopRandomHighlight()

        const success = await loadMapData(clickedRegionName, nextAdcode, 'drill')

        if (success) {
           if (clickedRegionName !== 'china') {
             mapLocationStore.setCurrentCity(clickedRegionName)
           }
           emit('region-change', clickedRegionName)
        } else {
           historyStack.value.pop()
           console.log(`已到达最底层：${clickedRegionName}`)
           mapLocationStore.setCurrentCity(clickedRegionName)
           emit('region-change', clickedRegionName)
        }
      }
    }
  })
}

const setOptions = (mapName) => {
  if (!chartInstance.value) return

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0, 70, 90, 0.9)', 
      borderColor: '#60EFDB',
      borderWidth: 1,
      textStyle: { color: '#fff' },
      formatter: (params) => {
        if (isNaN(params.value)) return `${params.name}`
        return `${params.name}<br/>热度值: <span style="color:#08e7de;font-weight:bold;text-shadow:0 0 5px #08e7de;">${params.value}</span>`
      }
    },
    // visualMap 配置保留，用于显示左下角的图例
    // 注意：这里的 colors 顺序要和 generateMockData 里的逻辑对应
    visualMap: {
      orient: 'horizontal', // 【修改】设置为水平方向
      min: 0,
      max: 1000,
      left: 'center',       // 【修改】水平放置时，建议居中显示（原为 '20'）
      bottom: '30',         // 【微调】距离底部稍微抬高一点，避免贴底
      text: ['高', '低'],    // 文本位置会自动适应
      gap: 10,              // 两个手柄之间的距离
      itemWidth: 20,        // 图形的宽度
      itemHeight: 140,      // 图形的高度（水平模式下，这代表条状图的长度）
      calculable: true,
      textStyle: {
        color: '#60EFDB'
      },
      inRange: {
        color: colors 
      }
    }
    ,
    geo: {
      show: true,
      map: mapName,
      roam: true,
      zoom: 1.2,
      label: { show: false },
      itemStyle: {
        areaColor: 'rgba(0, 70, 90, 0.2)', 
        borderColor: '#60EFDB', 
        borderWidth: 1.5,
        shadowColor: '#0abff3', 
        shadowBlur: 15,
        shadowOffsetY: 5 
      },
      emphasis: {
        itemStyle: { areaColor: 'transparent' }
      }
    },
    series: [
      {
        type: 'map',
        map: mapName,
        geoIndex: 0,
        // 这里传入的 heatmapData 已经包含了每个板块的 itemStyle 渐变配置
        data: heatmapData.value, 
        emphasis: {
          label: { show: true, color: '#fff', fontSize: 14, fontWeight: 'bold' },
          itemStyle: {
            areaColor: '#BEF2E5', 
            borderColor: '#fff',
            borderWidth: 2,
            shadowBlur: 30,
            shadowColor: '#08e7de', 
          },
        },
        select: {
          itemStyle: { areaColor: '#0abff3' }
        }
      }
    ],
  }
  chartInstance.value.setOption(option, true)
}

const handleResize = () => chartInstance.value?.resize()

watch(() => voiceRecognitionStore.isEnabled, (newValue) => {
  if (newValue) {
    stopRandomHighlight()
  }
})

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  stopRandomHighlight()
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
  overflow: hidden;
  perspective: 1000px;
}

.echarts-box {
  width: 100%;
  height: 100%;
  transform-origin: center center;
}

@keyframes drillIn {
  0% {
    opacity: 0;
    transform: scale(1.5);
    filter: blur(8px);
  }
  100% {
    opacity: 1;
    transform: scale(1);
    filter: blur(0);
  }
}

@keyframes backIn {
  0% {
    opacity: 0;
    transform: scale(0.5);
    filter: blur(8px);
  }
  100% {
    opacity: 1;
    transform: scale(1);
    filter: blur(0);
  }
}

.map-drill-enter {
  animation: drillIn 0.8s cubic-bezier(0.25, 1, 0.5, 1) forwards;
}

.map-back-enter {
  animation: backIn 0.8s cubic-bezier(0.25, 1, 0.5, 1) forwards;
}
</style>