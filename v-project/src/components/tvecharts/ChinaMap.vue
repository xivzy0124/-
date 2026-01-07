<template>
  <div class="map-container">
    <div ref="chartRef" class="echarts-box"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, shallowRef } from 'vue'
import * as echarts from 'echarts'
import { mapLocation } from '../../stores/store.js'

const mapLocationStore = mapLocation()
const emit = defineEmits(['region-change'])

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

// 用于记录历史记录，实现返回上一级功能
const historyStack = ref([])

// 加载地图数据
const loadMapData = async (mapName, adcode) => {
  chartInstance.value.showLoading({
    text: '数据加载中...',
    color: '#00f7ff',
    textColor: '#00f7ff',
    maskColor: 'rgba(10, 30, 60, 0.5)',
  })

  try {
    // 阿里云 DataV Atlas 数据源
    const url = `https://geo.datav.aliyun.com/areas_v3/bound/${adcode}_full.json`
    const response = await fetch(url)
    
    // 如果是区县级，可能没有下一级地图，API会返回404或其他错误
    if (!response.ok) {
        throw new Error('No child map data')
    }

    const geoJson = await response.json()

    // 注册地图
    echarts.registerMap(mapName, geoJson)
    
    // 更新当前状态
    currentMapName.value = mapName
    currentAdcode.value = adcode
    
    chartInstance.value.hideLoading()
    setOptions(mapName)
    
    return true // 加载成功
  } catch (error) {
    console.warn('无法加载下一级地图（可能已到达最底层区县）:', error)
    chartInstance.value.hideLoading()
    return false // 加载失败
  }
}

// 返回上一级
const backToPrevious = async () => {
  if (historyStack.value.length === 0) return
  
  const prev = historyStack.value.pop()
  await loadMapData(prev.mapName, prev.adcode)
  emit('region-change', prev.mapName === 'china' ? '全国' : prev.mapName)
}

defineExpose({
  backToPrevious,
})

// 初始化图表
const initChart = async () => {
  if (!chartRef.value) return
  chartInstance.value = echarts.init(chartRef.value)

  // 初始加载全国地图
  await loadMapData('china', '100000')

  // --- 核心修改：通用点击下钻逻辑 ---
  chartInstance.value.on('click', async (params) => {
    if (params.componentType === 'geo') {
      const clickedRegionName = params.name
      
      // 1. 获取当前已注册地图的 GeoJSON 数据
      const currentGeoJson = echarts.getMap(currentMapName.value).geoJson
      
      // 2. 在 GeoJSON 的 features 中查找点击区域的详细信息（包含 adcode）
      const feature = currentGeoJson.features.find(
        (f) => f.properties.name === clickedRegionName
      )
      
      if (feature && feature.properties.adcode) {
        const nextAdcode = feature.properties.adcode
        
        // 3. 记录当前状态到历史栈（为了能返回）
        historyStack.value.push({
          mapName: currentMapName.value,
          adcode: currentAdcode.value
        })

        // 4. 尝试加载下一级地图
        const success = await loadMapData(clickedRegionName, nextAdcode)

        if (success) {
           // A. 如果加载成功（说明有下一级地图，如省->市，或 市->区）
           // 更新 Store 和通知父组件
           if (clickedRegionName !== 'china') {
             // 简单的逻辑：如果是省份层级，存省；如果是市层级，存市
             // 这里统一发送当前选中的区域名称
             mapLocationStore.setCurrentCity(clickedRegionName)
           }
           emit('region-change', clickedRegionName)
        } else {
           // B. 如果加载失败（说明已经是区县级，或者API没有该区县的详细内部地图）
           // 此时不切换地图视图，但仍然算作"选中"了该区域
           
           // 回退历史栈（因为没跳过去）
           historyStack.value.pop() 
           
           // 仍然通知 Store 和父组件更新数据
           console.log(`已到达最底层：${clickedRegionName}`)
           mapLocationStore.setCurrentCity(clickedRegionName)
           emit('region-change', clickedRegionName)
           
           // 可选：在这里做一个高亮效果提示用户选中了区县
        }
      }
    }
  })
}

// 设置配置项
// 设置配置项
const setOptions = (mapName) => {
  if (!chartInstance.value) return

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      // ... 保持原有 tooltip 配置
      trigger: 'item',
      backgroundColor: 'rgba(0,0,0,0.8)',
      borderColor: '#45d0b2',
      borderWidth: 1,
      textStyle: { color: '#fff' },
      formatter: (params) => {
          return `${params.name}`
      }
    },
    geo: {
      map: mapName,
      roam: true,
      zoom: 1.2,
      // --- 修改开始：关闭默认标签显示 ---
      label: {
        show: false, // 这里改为 false
        color: '#7fffd4',
        fontSize: 10,
      },
      // --- 修改结束 ---
      
      itemStyle: {
        areaColor: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(69, 208, 178, 0.1)' },
            { offset: 1, color: 'rgba(0, 20, 60, 0.9)' },
          ],
        },
        borderColor: '#b9f8f4',
        borderWidth: 1.5,
        shadowColor: '#00ffff',
        shadowBlur: 10,
      },
      // 这里的 emphasis 设置保留，这样鼠标悬停时（高亮状态）仍然会显示名字
      emphasis: {
        label: { show: true, color: '#fff' }, 
        itemStyle: {
          areaColor: 'rgba(69, 208, 178, 0.8)',
          borderColor: '#ffffff',
          borderWidth: 2,
          shadowBlur: 20,
          shadowColor: '#fff',
        },
      },
      select: {
        itemStyle: { areaColor: '#2B91B7' }
      }
    },
    series: [],
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