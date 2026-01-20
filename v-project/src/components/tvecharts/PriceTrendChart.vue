<template>
  <div class="price-trend-card">
    <div class="left-header-block">
      <div class="section-title">
        <span class="title-main">{{ currentProductName }}</span>
        <span class="title-sub"> // 价格波动预测分析</span>
      </div>
      <div class="header-right-area">
        <span class="static-dot"></span>
        <span class="status-text">AI 实时推演</span>
      </div>
    </div>
    
    <div ref="priceChartRef" class="echarts-box"></div>

    <div v-if="loading" class="state-mask">
      <div class="loading-spinner"></div>
      <div class="state-text">AI 模型运算中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { mapProduct, mapLocation, pricePredictionCache, getHardcodedData } from '../../stores/store.js'

const mapProductStore = mapProduct()
const mapLocationStore = mapLocation()

const priceChartRef = ref(null)
let priceChart = null

const loading = ref(false)
const currentProductName = ref('未选择')

let currentXData = []
let currentYData = []

// 配置项生成函数
const getPriceOption = (xData, yData) => {
  const cyanColor = '#00f2ff';
  const gridColor = 'rgba(0, 157, 255, 0.2)';

  return {
    backgroundColor: 'transparent',
    animationDuration: 2000, 
    animationEasing: 'cubicOut', 
    grid: { top: '15%', left: '2%', right: '5%', bottom: '5%', containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(2, 12, 20, 0.9)',
      borderColor: cyanColor,
      textStyle: { color: '#fff', fontSize: 12 },
      axisPointer: { type: 'line', lineStyle: { color: cyanColor, type: 'dashed' } },
      formatter: (params) => {
        const p = params[0];
        return `<div style="color:${cyanColor};font-weight:bold;margin-bottom:4px;">${p.axisValue}</div>
                <div style="color:#fff">预测均价：<span style="font-size:16px;font-weight:bold;color:${cyanColor}">${p.data}</span> 元/kg</div>`
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLabel: {
        color: '#94a3b8',
        fontSize: 10,
        formatter: (val) => (val ? val.substring(5) : ''),
      },
      axisLine: { lineStyle: { color: gridColor } },
    },
    yAxis: {
      type: 'value',
      scale: true,
      axisLabel: { color: '#94a3b8', fontSize: 10 },
      splitLine: { lineStyle: { color: gridColor, type: 'dashed' } },
    },
    series: [
      {
        name: '预测均价',
        type: 'line',
        smooth: 0.4,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        endLabel: {
          show: true,
          formatter: '{c} 元',
          color: cyanColor,
          fontWeight: 'bold',
          distance: 10,
          fontSize: 12
        },
        itemStyle: { 
          color: '#000', 
          borderColor: cyanColor, 
          borderWidth: 2 
        },
        lineStyle: { 
          width: 2, 
          color: cyanColor,
          shadowColor: 'rgba(0, 242, 255, 0.5)',
          shadowBlur: 10
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 242, 255, 0.4)' },
            { offset: 1, color: 'rgba(0, 242, 255, 0)' },
          ]),
        },
        data: yData,
        markLine: {
          symbol: ['none', 'none'],
          label: { show: false },
          data: [
            { type: 'max', name: '最高' },
            { type: 'min', name: '最低' }
          ],
          lineStyle: {
            color: 'rgba(255, 255, 255, 0.3)',
            type: 'dotted'
          }
        }
      },
    ],
  }
}

// 获取/生成数据
const fetchData = async () => {
  const productName = mapProductStore.currentProduct || '大白菜'
  currentProductName.value = productName
  loading.value = true 

  const province = mapLocationStore.currentProvince || '河南省'
  const city = mapLocationStore.currentCity || '郑州市'
  const district = mapLocationStore.currentDistrict || '中原区'
  
  const cacheStore = pricePredictionCache()
  const cachedData = cacheStore ? cacheStore.getCache(province, city, district, productName) : null

  setTimeout(() => {
    try {
      const dates = []
      const prices = []
      const today = new Date()
      
      if (cachedData && cachedData.sevenDayPrediction) {
          for (let i = 0; i < 7; i++) {
            const d = new Date(today)
            d.setDate(d.getDate() + i + 1)
            dates.push(d.toISOString().split('T')[0])
            
            if (cachedData.sevenDayPrediction[i]) {
              prices.push(parseFloat(cachedData.sevenDayPrediction[i].price))
            }
          }
        } else {
          const hardcodedResult = getHardcodedData(province, city, district, productName)
          if (hardcodedResult) {
            if (hardcodedResult.sevenDayPrediction) {
              for (let i = 0; i < 7; i++) {
                const d = new Date(today)
                d.setDate(d.getDate() + i + 1)
                dates.push(d.toISOString().split('T')[0])
                
                if (hardcodedResult.sevenDayPrediction[i]) {
                  prices.push(parseFloat(hardcodedResult.sevenDayPrediction[i].price))
                }
              }
            }
            cacheStore.setCache(province, city, district, productName, hardcodedResult)
          } else {
          let minPrice, maxPrice, startPrice;

          if (productName.includes('白菜')) {
            minPrice = 2.0; maxPrice = 3.5; startPrice = 2.8;
          } else if (productName.includes('黄瓜')) {
            minPrice = 6.0; maxPrice = 8.0; startPrice = 7.2;
          } else {
            minPrice = 3.0; maxPrice = 6.0; startPrice = 4.5;
          }

          let currentPrice = startPrice;

          for (let i = 0; i < 30; i++) {
            const d = new Date(today)
            d.setDate(d.getDate() + i + 1) 
            dates.push(d.toISOString().split('T')[0])

            const change = (Math.random() - 0.5) * 0.6; 
            currentPrice += change;

            if (currentPrice < minPrice) currentPrice = minPrice + Math.random() * 0.2;
            if (currentPrice > maxPrice) currentPrice = maxPrice - Math.random() * 0.2;

            prices.push(Number(currentPrice.toFixed(2)))
          }
        }
      }

      currentXData = dates
      currentYData = prices

      if (priceChart) {
        priceChart.clear(); 
        priceChart.setOption(getPriceOption(currentXData, currentYData), true); 
      }
      
    } catch (error) {
      console.error('计算失败', error)
    } finally {
      loading.value = false
    }
  }, 600) 
}

const initChart = () => {
  if (!priceChartRef.value) return
  priceChart = echarts.init(priceChartRef.value)
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  priceChart?.resize()
}

watch(() => mapProductStore.currentProduct, () => fetchData())
watch(() => mapLocationStore.currentProvince, () => fetchData())
watch(() => pricePredictionCache().cache, () => fetchData(), { deep: true })

onMounted(() => {
  setTimeout(() => {
    initChart()
    fetchData()
  }, 200)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  priceChart?.dispose()
})
</script>

<style scoped>
.price-trend-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  background: linear-gradient(135deg, rgba(2, 12, 20, 0.3) 0%, rgba(10, 25, 45, 0.3) 100%);
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 242, 255, 0.1);
  box-shadow: 0 0 15px rgba(0, 242, 255, 0.05);
}

.left-header-block {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 10px 5px 10px;
  width: 100%;
}

/* 融合后的标题样式，与 radar.vue 中的 section-title 保持一致 */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #00f2ff; /* 青色 */
  padding-left: 12px;
  border-left: 4px solid #009dff;
  background: linear-gradient(90deg, rgba(0,157,255,0.15) 0%, transparent 100%);
  letter-spacing: 0.5px;
  flex-shrink: 0;
  text-shadow: 0 0 5px rgba(0, 242, 255, 0.3);
  
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.title-main {
  font-size: 15px;
  font-weight: bold;
}

.title-sub {
  font-size: 12px;
  color: #94a3b8;
  font-weight: normal;
}

.header-right-area {
  display: flex;
  align-items: center;
  gap: 6px;
}

.static-dot {
  width: 6px;
  height: 6px;
  background-color: #009dff; 
  border-radius: 50%;
  box-shadow: 0 0 5px #009dff;
}

.status-text {
  font-size: 11px;
  color: #94a3b8;
}

.echarts-box {
  width: 100%;
  flex: 1;
  min-height: 180px;
}

.state-mask {
  position: absolute;
  inset: 0;
  background: rgba(2, 6, 15, 0.85);
  backdrop-filter: blur(4px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 8px;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid rgba(0, 242, 255, 0.2);
  border-top-color: #00f2ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  box-shadow: 0 0 10px rgba(0, 242, 255, 0.2);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.state-text {
  color: #00f2ff;
  margin-top: 10px;
  font-size: 13px;
  letter-spacing: 1px;
  text-shadow: 0 0 5px rgba(0, 242, 255, 0.3);
}
</style>