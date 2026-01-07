<template>
  <div class="tech-trend-chart">
    <div class="chart-container">
      <div class="chart-body">
        <div ref="chartDom" class="echarts-container"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, watch, defineEmits, ref, onBeforeUnmount, computed, nextTick } from 'vue'
import { getYearlyTrendByProvince } from '../../api/requestFuntion.js'
import { mapLocation, mapProduct } from '../../stores/store.js'

const emit = defineEmits(['chartClick'])
const mapLocationStore = mapLocation()
const mapProductStore = mapProduct()

const productName = computed(() => mapProductStore.currentProduct)
const provinceName = computed(() => mapLocationStore.currentProvince)

let myChart = null
const trendData = ref([])
const chartDom = ref(null)

// 核心修复1：更健壮的数据获取逻辑
const fetchYearlyData = async () => {
  console.log('=== fetchYearlyData 开始 ===')
  console.log('请求参数:', {
    provinceName: provinceName.value,
    varietyName: productName.value
  })
  
  if (myChart) {
    myChart.showLoading({
      text: '加载中...',
      color: '#45d0b2',
      textColor: '#45d0b2',
      maskColor: 'rgba(10, 30, 60, 0.8)',
    })
  }

  try {
    const response = await getYearlyTrendByProvince(
      provinceName.value,
      productName.value
    )

    console.log('=== API 响应 ===')
    console.log('响应类型:', typeof response)
    console.log('是否为数组:', Array.isArray(response))
    console.log('响应内容:', response)
    console.log('=== API 响应结束 ===')

    if (Array.isArray(response)) {
      trendData.value = response
      console.log('✓ 成功获取数据，条数:', response.length)
      updateChart()
    } else {
      console.warn('✗ API 返回的数据不是数组', response)
      trendData.value = []
      updateChart()
    }

  } catch (error) {
    console.error('=== API 请求失败 ===')
    console.error('错误:', error)
    console.error('错误消息:', error.message)
    console.error('=== API 请求失败结束 ===')
    trendData.value = []
    if (myChart) myChart.hideLoading()
  } finally {
    if (myChart) myChart.hideLoading()
    console.log('=== fetchYearlyData 结束 ===')
  }
}

// ECharts 配置项
const option = {
  grid: { top: 40, bottom: 40, left: 50, right: 30, containLabel: true },
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(10, 30, 60, 0.95)',
    borderColor: '#45d0b2',
    textStyle: { color: '#fff' }
  },
  dataZoom: [
    {
      type: 'slider',
      show: true,
      height: 12,
      bottom: 5,
      borderColor: 'rgba(69, 208, 178, 0.3)',
      handleStyle: { color: '#45d0b2' },
      textStyle: { color: '#fff' }
    },
    { type: 'inside' }
  ],
  legend: {
    data: ['平均价', '最大价'],
    textStyle: { color: '#fff' },
    top: 5
  },
  xAxis: {
    type: 'category',
    data: [], // 初始化为空
    axisLine: { lineStyle: { color: 'rgba(69, 208, 178, 0.5)' } },
    axisLabel: { color: '#fff', fontSize: 11 },
    axisTick: { alignWithLabel: true }
  },
  yAxis: {
    type: 'value',
    name: '元/公斤',
    nameTextStyle: { color: '#fff', padding: [0, 0, 0, 10] },
    splitLine: { lineStyle: { color: 'rgba(69, 208, 178, 0.1)', type: 'dashed' } },
    axisLabel: { color: '#fff' }
  },
  series: [
    {
      name: '平均价',
      type: 'bar',
      barMaxWidth: 30, // 防止数据少时柱子太宽
      data: [],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(69, 208, 178, 0.8)' },
          { offset: 1, color: 'rgba(38, 162, 147, 0.3)' }
        ]),
        borderRadius: [3, 3, 0, 0]
      }
    },
    {
      name: '最大价',
      type: 'line',
      smooth: true,
      data: [],
      itemStyle: { color: '#45d0b2' },
      lineStyle: { width: 3, shadowBlur: 10, shadowColor: 'rgba(69, 208, 178, 0.5)' }
    }
  ]
}

// 核心修复2：更新图表逻辑
const updateChart = () => {
  if (!myChart) return

  const data = trendData.value || []
  
  // 1. 处理 X 轴：强制转为字符串，确保 Category 轴正常渲染
  const xAxisData = data.map(item => String(item.year))
  
  // 2. 处理 Series 数据
  const avgData = data.map(item => parseFloat(item.averagePrice).toFixed(2))
  const maxData = data.map(item => parseFloat(item.avgMaxPrice).toFixed(2))

  // 3. 更新 Option 数据
  option.xAxis.data = xAxisData
  option.series[0].data = avgData
  option.series[1].data = maxData

  // 4. 处理 DataZoom：如果数据超过8条，默认显示最后50%
  if (data.length > 8) {
     option.dataZoom[0].start = 50
     option.dataZoom[0].end = 100
     option.dataZoom[1].start = 50
     option.dataZoom[1].end = 100
  } else {
     option.dataZoom[0].start = 0
     option.dataZoom[0].end = 100
     option.dataZoom[1].start = 0
     option.dataZoom[1].end = 100
  }

  // 5. 渲染 (关键：使用 notMerge=true，确保彻底更新)
  myChart.setOption(option, true)
}

const initChart = () => {
  if (!chartDom.value) return
  
  if (myChart) {
    myChart.dispose()
  }
  
  myChart = echarts.init(chartDom.value)
  myChart.on('click', (params) => emit('chartClick', params))
  
  // 如果此时已有数据，立即渲染
  if (trendData.value.length > 0) {
    updateChart()
  }
}

const resizeHandler = () => myChart?.resize()

// 监听条件变化
watch([provinceName, productName], () => {
  console.log('=== Watch 触发 ===')
  console.log('省份变化:', provinceName.value)
  console.log('产品变化:', productName.value)
  fetchYearlyData()
})

onMounted(() => {
  // 使用 nextTick 确保 DOM 已经完全渲染
  nextTick(() => {
    initChart()
    fetchYearlyData()
    window.addEventListener('resize', resizeHandler)
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler)
  myChart?.dispose()
})
</script>

<style scoped>
.tech-trend-chart {
  width: 100%;
  height: 100%;
}
.chart-container, .chart-body, .echarts-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
}
</style>