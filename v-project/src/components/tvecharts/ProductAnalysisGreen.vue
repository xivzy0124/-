<template>
  <div class="analysis-card">
    <div class="card-header">
      <div class="header-title">
        <span class="product-name">{{ currentProductName }}</span>
        <span class="sub-text"> // 价格趋势 & 交易排行监测</span>
      </div>
    </div>

    <div class="chart-body">
      <div class="chart-section left-section">
        <div class="section-title">近30日价格走势</div>
        <div ref="priceChartRef" class="echarts-box"></div>
      </div>

      <div class="divider-v"></div>

      <div class="chart-section right-section">
        <div class="section-title">实时交易量TOP10</div>
        <div ref="rankChartRef" class="echarts-box"></div>
      </div>

      <div v-if="loading" class="state-mask">
        <div class="loading-spinner"></div>
        <div class="state-text">数据同步中...</div>
      </div>

      <div v-if="!loading && isAllEmpty" class="state-mask">
        <div class="state-text">暂无 {{ currentProductName }} 相关数据</div>
        <button class="retry-btn" @click="fetchData">重试</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, reactive } from 'vue'
import * as echarts from 'echarts'
import { mapProduct } from '../../stores/store.js'

const mapProductStore = mapProduct()

const priceChartRef = ref(null)
const rankChartRef = ref(null)
let priceChart = null
let rankChart = null

const loading = ref(false)
const isAllEmpty = ref(false)
const currentProductName = ref('未选择')

// ================= ECharts 配置 =================

const getPriceOption = (xData, yData) => {
  return {
    backgroundColor: 'transparent',
    grid: { top: '15%', left: '2%', right: '4%', bottom: '5%', containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(4, 20, 15, 0.9)',
      borderColor: '#42e3a4',
      textStyle: { color: '#fff', fontSize: 12 },
      axisPointer: { type: 'line', lineStyle: { color: '#42e3a4', type: 'dashed' } },
      formatter: (params) => {
        const item = params[0]
        return `<div style="color:#aaa">${item.name}</div>
                <div style="font-weight:bold; color:#42e3a4">
                  ${item.marker} 均价: ${item.value} 元/kg
                </div>`
      },
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLabel: {
        color: 'rgba(255,255,255,0.6)',
        fontSize: 10,
        formatter: (val) => (val ? val.substring(5) : ''),
      },
      axisLine: { lineStyle: { color: 'rgba(66, 227, 164, 0.3)' } },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      name: '元/kg',
      nameTextStyle: { color: 'rgba(66, 227, 164, 0.6)', padding: [0, 0, 0, -10] },
      axisLabel: { color: 'rgba(255,255,255,0.6)', fontSize: 10 },
      splitLine: { lineStyle: { color: 'rgba(66, 227, 164, 0.1)', type: 'dashed' } },
    },
    series: [
      {
        name: '价格',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#00ff9d', borderColor: '#fff' },
        lineStyle: { width: 2, color: '#42e3a4', shadowBlur: 5, shadowColor: '#42e3a4' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(66, 227, 164, 0.4)' },
            { offset: 1, color: 'rgba(66, 227, 164, 0.01)' },
          ]),
        },
        data: yData,
      },
    ],
  }
}

const getRankOption = (data) => {
  const topData = data.slice(0, 10)
  const yData = topData.map((item) => item.name)
  const xData = topData.map((item) => item.value)

  return {
    backgroundColor: 'transparent',
    grid: { top: '5%', left: '2%', right: '12%', bottom: '2%', containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(4, 20, 15, 0.9)',
      borderColor: '#42e3a4',
      textStyle: { color: '#fff' },
      formatter: '{b}<br/><span style="color:#42e3a4">●</span> 交易量: {c} 吨',
    },
    xAxis: { type: 'value', splitLine: { show: false }, axisLabel: { show: false } },
    yAxis: {
      type: 'category',
      data: yData,
      inverse: true,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#fff', fontSize: 10, width: 70, overflow: 'truncate' },
    },
    series: [
      {
        type: 'bar',
        barWidth: 8,
        data: xData,
        itemStyle: {
          borderRadius: [0, 4, 4, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: 'rgba(66, 227, 164, 0.2)' },
            { offset: 1, color: '#00ff9d' },
          ]),
        },
        label: { show: true, position: 'right', color: '#42e3a4', fontSize: 10, formatter: '{c}' },
      },
    ],
  }
}

// ================= 假数据生成器 (核心修改) =================

// 辅助函数：生成最近30天的日期
const generateDates = () => {
  const dates = []
  const today = new Date()
  for (let i = 29; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    // 格式化为 YYYY-MM-DD
    const dateStr = d.toISOString().split('T')[0]
    dates.push(dateStr)
  }
  return dates
}

// 生成随机价格数据 (根据不同的基础价格波动)
const generateRandomPrice = (basePrice = 2.0) => {
  const dates = generateDates()
  return dates.map((date) => ({
    reporttime: date,
    // 价格在 basePrice 上下浮动 30%
    middleprice: Number((basePrice * (0.85 + Math.random() * 0.3)).toFixed(2)),
  }))
}

// 生成随机销量数据
const generateRandomVolume = () => {
  const markets = [
    '北京新发地',
    '上海江桥',
    '广州江南',
    '深圳海吉星',
    '杭州良渚',
    '成都益民',
    '武汉白沙洲',
    '南京众彩',
    '西安欣桥',
    '重庆双福',
    '长沙马王堆',
    '郑州万邦',
    '昆明王旗营',
    '沈阳盛发',
  ]

  // 随机取10个市场
  const shuffled = markets.sort(() => 0.5 - Math.random()).slice(0, 10)

  // 基础销量 300 ~ 1300
  return shuffled
    .map((market) => ({
      market: market,
      volume: Math.floor(300 + Math.random() * 1000),
    }))
    .sort((a, b) => b.volume - a.volume)
}

// 模拟三套不同的数据风格 (低价/中价/高价)
const mockDataStyles = [
  { priceBase: 1.5 }, // 便宜的菜 (如白菜)
  { priceBase: 4.5 }, // 中等的菜 (如西红柿)
  { priceBase: 8.0 }, // 贵的菜 (如姜蒜)
]

// ================= 数据获取逻辑 =================

const fetchData = async () => {
  const productName = mapProductStore.currentProduct || '大白菜'
  currentProductName.value = productName
  loading.value = true
  isAllEmpty.value = false

  try {
    let priceX = []
    let priceY = []

    const style = mockDataStyles[Math.floor(Math.random() * mockDataStyles.length)]
    const mockData = generateRandomPrice(style.priceBase)

    priceX = mockData.map((item) => item.reporttime)
    priceY = mockData.map((item) => item.middleprice)

    if (priceChart) {
      priceChart.setOption(getPriceOption(priceX, priceY), true)
    }

    let chartData = []
    const mockDataV = generateRandomVolume()
    chartData = mockDataV.map((item) => ({
      name: item.market,
      value: item.volume,
    }))

    if (rankChart) {
      rankChart.setOption(getRankOption(chartData), true)
    }
  } catch (error) {
    console.error('数据生成错误', error)

    const style = mockDataStyles[Math.floor(Math.random() * mockDataStyles.length)]
    const mockP = generateRandomPrice(style.priceBase)
    priceChart?.setOption(
      getPriceOption(
        mockP.map((i) => i.reporttime),
        mockP.map((i) => i.middleprice),
      ),
      true,
    )

    const mockV = generateRandomVolume()
    rankChart?.setOption(
      getRankOption(mockV.map((i) => ({ name: i.market, value: i.volume }))),
      true,
    )
  } finally {
    loading.value = false
  }
}

// ================= 初始化 =================

const initCharts = () => {
  if (!priceChartRef.value || !rankChartRef.value) return
  if (priceChartRef.value.clientHeight === 0) {
    setTimeout(initCharts, 200)
    return
  }
  priceChart = echarts.init(priceChartRef.value)
  rankChart = echarts.init(rankChartRef.value)
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  priceChart?.resize()
  rankChart?.resize()
}

watch(
  () => mapProductStore.currentProduct,
  (newVal) => {
    if (newVal) fetchData()
  },
)

onMounted(() => {
  setTimeout(() => {
    initCharts()
    fetchData()
  }, 200)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  priceChart?.dispose()
  rankChart?.dispose()
})
</script>

<style scoped>
.analysis-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 0 20px rgba(66, 227, 164, 0.05);
}

.card-header {
  padding: 10px 10px 5px 10px;
  border-bottom: 1px dashed rgba(66, 227, 164, 0.3);
  flex-shrink: 0;
}

.product-name {
  font-size: 18px;
  font-weight: bold;
  color: #00ff9d;
  text-shadow: 0 0 8px rgba(0, 255, 157, 0.4);
  margin-right: 10px;
}

.sub-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.chart-body {
  flex: 1;
  display: flex;
  position: relative;
  padding: 10px 5px;
  min-height: 0;
}

.chart-section {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.left-section {
  flex: 3;
}
.right-section {
  flex: 2;
}

.divider-v {
  width: 1px;
  background: linear-gradient(to bottom, transparent, rgba(66, 227, 164, 0.3), transparent);
  margin: 0 8px;
}

.section-title {
  font-size: 11px;
  color: rgba(66, 227, 164, 0.8);
  padding-left: 10px;
  margin-bottom: 5px;
  border-left: 2px solid #00ff9d;
}

.echarts-box {
  flex: 1;
  width: 100%;
  /* 修正：给予最小高度，防止图表渲染高度为0 */
  min-height: 180px;
}

/* 遮罩样式保持不变 */
.state-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(11, 19, 37, 0.85);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 20;
  backdrop-filter: blur(4px);
}
.retry-btn {
  margin-top: 10px;
  background: transparent;
  border: 1px solid #42e3a4;
  color: #42e3a4;
  padding: 4px 12px;
  cursor: pointer;
  font-size: 12px;
}
.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(66, 227, 164, 0.2);
  border-top-color: #00ff9d;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
.state-text {
  margin-top: 10px;
  font-size: 12px;
  color: #42e3a4;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
