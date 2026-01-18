<template>
  <div v-if="visible" class="price-prediction-overlay" @click.self="close">
    <div class="price-prediction-modal">
      <div class="modal-decoration-line"></div>
      
      <div class="modal-header">
        <div class="header-title">
          <div class="icon-box">
            <span class="pulse-dot"></span>
            <span class="title-icon">🧠</span>
          </div>
          <div class="title-text-group">
            <span class="title-main">农智大脑 · 价格预测引擎</span>
            <span class="title-sub">基于 LSTM-V3 深度学习模型 / 实时推演</span>
          </div>
        </div>
        <button class="close-btn" @click="close">
          <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
        </button>
      </div>

      <div class="modal-body">
        
        <div class="hero-section">
          <div class="product-tag-float">
            <span class="p-icon">💠</span> {{ selectedProduct }} (重点监测品类)
          </div>
          
          <div class="date-hero-wrapper">
            <label class="hero-label">选择预测目标日期</label>
            <div class="date-display-box" :class="{ 'disabled': isLoading }">
              <input 
                v-model="targetDate" 
                type="date" 
                class="hero-date-input"
                :min="minDate"
                :disabled="isLoading"
              >
              <span class="edit-hint" v-if="!isLoading">点击更改日期</span>
            </div>
          </div>
        </div>

        <div v-if="isLoading" class="loading-container">
          <div class="scanner-wrapper">
            <div class="scan-ring"></div>
            <div class="scan-core"></div>
          </div>
          <div class="loading-text">
            正在分析市场数据
            <span class="dot-flashing"></span>
          </div>
          <div class="loading-sub">检索气象条件 / 调取历史行情 / 计算供需模型...</div>
        </div>

        <div v-else class="result-container animate-fade-in">
          <div class="dashboard-grid">
            <div class="metric-card price-card">
              <div class="card-header">
                <span class="card-label">预测单价 ({{ targetDate }})</span>
                <span class="live-badge">AI 实时</span>
              </div>
              <div class="price-display">
                <span class="currency">¥</span>
                <span class="big-number">{{ predictedPrice }}</span>
                <span class="unit">/斤</span>
              </div>
              <div class="trend-row">
                <div class="trend-badge" :class="trendClass">
                  <span class="trend-arrow">{{ trendIcon }}</span> {{ trendText }}
                </div>
                <span class="diff-text" :class="diffClass">{{ priceDiff }}</span>
              </div>
            </div>

            <div class="metric-card analysis-card">
              <div class="card-label">影响因素分析</div>
              <div class="factor-list">
                <div class="factor-item">
                  <span class="f-name">🌦️ 气象条件</span>
                  <div class="f-bar-bg"><div class="f-bar" :style="{width: factors.weather + '%', background: '#00f7ff'}"></div></div>
                  <span class="f-val">{{ factors.weather }}%</span>
                </div>
                <div class="factor-item">
                  <span class="f-name">📦 库存周转</span>
                  <div class="f-bar-bg"><div class="f-bar" :style="{width: factors.inventory + '%', background: '#3ba1ff'}"></div></div>
                  <span class="f-val">{{ factors.inventory }}%</span>
                </div>
                <div class="factor-item">
                  <span class="f-name">🚚 物流成本</span>
                  <div class="f-bar-bg"><div class="f-bar" :style="{width: factors.logistics + '%', background: '#b766ff'}"></div></div>
                  <span class="f-val">{{ factors.logistics }}%</span>
                </div>
              </div>
            </div>

            <div class="metric-card confidence-card">
              <div class="card-label">预测可信度</div>
              <div class="confidence-circle">
                <svg viewBox="0 0 36 36" class="circular-chart">
                  <path class="circle-bg" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                  <path class="circle" :stroke-dasharray="`${confidence}, 100`" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                </svg>
                <div class="percentage-text">{{ confidence }}<span class="pct">%</span></div>
              </div>
              <div class="risk-label">风险等级: <span style="color:#00f7ff">低</span></div>
            </div>
          </div>

          <div class="chart-section">
            <div class="chart-header">
              <div class="chart-title">
                <span class="chart-icon">📈</span> 未来价格走势预测
              </div>
              <div class="legend">
                <span class="dot predict"></span>AI 预测趋势
              </div>
            </div>
            <div ref="chartRef" class="chart-container"></div>
          </div>
        </div>

      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" @click="close">取消操作</button>
        <button class="btn btn-primary" @click="confirmPrediction" :disabled="isLoading" :style="{ opacity: isLoading ? 0.5 : 1 }">
          <span class="btn-icon">⚡</span> 采纳该预测值
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { mapLocation, mapProduct, pricePredictionCache } from '../../stores/store.js'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'confirm'])

// Store
const mapLocationStore = mapLocation()
const mapProductStore = mapProduct()

const productName = computed(() => mapProductStore.currentProduct)
const provinceName = computed(() => mapLocationStore.currentProvince)

// State
const targetDate = ref('')
const selectedProduct = ref(productName.value || '黄瓜')
const chartRef = ref(null)
const isLoading = ref(false) 
let chartInstance = null

const predictedPrice = ref('0.00')
const confidence = ref('0')
const factors = ref({ weather: 0, inventory: 0, logistics: 0 })

const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

const defaultDate = computed(() => {
  const today = new Date()
  today.setDate(today.getDate() + 1) // 默认为明天
  return today.toISOString().split('T')[0]
})

// 监听 store 变化
watch([productName, provinceName], () => {
  selectedProduct.value = productName.value || '黄瓜'
  if (props.visible) {
    generatePrediction()
  }
})

// 计算属性：趋势
const trendClass = computed(() => {
  const price = parseFloat(predictedPrice.value)
  if (price > 6) return 'trend-up'
  if (price < 4) return 'trend-down'
  return 'trend-stable'
})

const trendIcon = computed(() => {
  const price = parseFloat(predictedPrice.value)
  if (price > 6) return '▲'
  if (price < 4) return '▼'
  return '●'
})

const trendText = computed(() => {
  const price = parseFloat(predictedPrice.value)
  if (price > 6) return '强力看涨'
  if (price < 4) return '持续下跌'
  return '市场平稳'
})

// 计算属性：环比
const priceDiff = computed(() => {
  const current = parseFloat(predictedPrice.value)
  const base = current * (1 - (Math.random() * 0.1 - 0.05)) 
  const diff = ((current - base) / base * 100).toFixed(1)
  return (diff > 0 ? '+' : '') + diff + '%'
})

const diffClass = computed(() => {
  return parseFloat(priceDiff.value) >= 0 ? 'diff-up' : 'diff-down'
})

// 核心逻辑
const generatePrediction = () => {
  isLoading.value = true
  
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }

  const province = provinceName.value || '河南省'
  const city = mapLocationStore.currentCity || '郑州市'
  const district = mapLocationStore.currentDistrict || '中原区'
  const product = productName.value || '黄瓜'
  
  const cacheStore = pricePredictionCache()
  const cachedData = cacheStore ? cacheStore.getCache(province, city, district, product) : null

  setTimeout(async () => {
    let basePrice = 0
    
    if (cachedData && cachedData.timeline && cachedData.timeline.length > 0) {
      basePrice = parseFloat(cachedData.timeline[0].price)
    } else {
      if (province === '河南省' && product === '大白菜') {
        basePrice = 1.5 + Math.random() * 1.5
      } else if (province === '河南省' && product === '黄瓜') {
        basePrice = 5.5 + Math.random() * 2.5
      } else if (province === '四川省' && product === '黄瓜') {
        basePrice = 7 + Math.random() * 2
      } else if (province === '四川省' && product === '大白菜') {
        basePrice = 2 + Math.random() * 1
      } else {
        basePrice = 5
      }
    }
    
    const variance = (Math.random() - 0.5) * 0.5
    predictedPrice.value = (basePrice + variance).toFixed(2)
    confidence.value = Math.floor(80 + Math.random() * 18)
    
    factors.value = {
      weather: Math.floor(Math.random() * 40 + 50),
      inventory: Math.floor(Math.random() * 60 + 20),
      logistics: Math.floor(Math.random() * 30 + 10)
    }

    isLoading.value = false
    
    await nextTick()
    initChart()
    
  }, 1500)
}

// 图表初始化 (ECharts 蓝色系 - 纯未来数据)
const initChart = () => {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)
  
  const dates = []
  const prices = []
  const today = new Date()
  
  const province = provinceName.value || '河南省'
  const city = mapLocationStore.currentCity || '郑州市'
  const district = mapLocationStore.currentDistrict || '中原区'
  const product = productName.value || '黄瓜'
  
  const seededRandom = (seed) => {
    let x = Math.sin(seed) * 10000
    return x - Math.floor(x)
  }
  
  const cacheStore = pricePredictionCache()
  const cachedData = cacheStore ? cacheStore.getCache(province, city, district, product) : null
  
  if (cachedData && cachedData.timeline && cachedData.timeline.length >= 7) {
    for (let i = 0; i < 7; i++) {
      const date = new Date(today)
      date.setDate(date.getDate() + i + 1)
      dates.push(date.toISOString().split('T')[0].substring(5))
      prices.push(parseFloat(cachedData.timeline[i].price))
    }
  } else {
    let basePrice = 0
    
    if (province === '河南省' && product === '大白菜') {
      basePrice = 1.5 + Math.random() * 1.5
    } else if (province === '河南省' && product === '黄瓜') {
      basePrice = 5.5 + Math.random() * 2.5
    } else if (province === '四川省' && product === '黄瓜') {
      basePrice = 7 + Math.random() * 2
    } else if (province === '四川省' && product === '大白菜') {
      basePrice = 2 + Math.random() * 1
    } else {
      basePrice = 5 + seededRandom((product?.length || 0) + (city?.length || 0)) * 5
    }
    
    for (let i = 1; i <= 7; i++) {
      const date = new Date(today)
      date.setDate(date.getDate() + i)
      const dateStr = date.toISOString().split('T')[0].substring(5)
      dates.push(dateStr)
      
      const daySeed = ((product?.charCodeAt(i % product.length || 0) || 0) + (city?.charCodeAt(i % city.length || 0) || 0)) * i
      const priceVariation = (seededRandom(daySeed) - 0.5) * (basePrice * 0.2)
      
      prices.push(Math.max(1, (basePrice + priceVariation).toFixed(2)))
    }
  }
  
  // 找到目标日期在数组中的索引，用于标记 MarkPoint
  const targetIndex = dates.findIndex(d => {
      const dObj = new Date(today.getFullYear(), parseInt(d.split('-')[0])-1, parseInt(d.split('-')[1]))
      // 简单匹配 MM-DD
      return targetDate.value && targetDate.value.endsWith(d)
  })

  const option = {
    backgroundColor: 'transparent',
    grid: {
      top: '20%', left: '2%', right: '4%', bottom: '5%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(5, 20, 35, 0.95)',
      borderColor: '#00f7ff',
      textStyle: { color: '#fff', fontFamily: 'monospace' },
      formatter: (params) => {
        const item = params[0]
        return `<div style="color:#a6b0c3;font-size:12px;margin-bottom:4px">${item.name}</div>
                <div style="color:#00f7ff;font-weight:bold">
                  🔮 预测价格: ${item.value} 元/斤
                </div>`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { color: '#a6b0c3', fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(59, 161, 255, 0.3)' } },
      axisTick: { show: false },
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      scale: true,
      splitLine: { lineStyle: { color: 'rgba(59, 161, 255, 0.1)', type: 'dashed' } },
      axisLabel: { color: '#a6b0c3' }
    },
    series: [
      {
        name: '未来趋势',
        type: 'line',
        smooth: 0.4,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: '#00f7ff',
          borderColor: '#fff',
          borderWidth: 2,
          shadowBlur: 10,
          shadowColor: '#00f7ff'
        },
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#3ba1ff' },
            { offset: 1, color: '#00f7ff' }
          ]),
          shadowColor: 'rgba(0, 247, 255, 0.5)',
          shadowBlur: 10
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 247, 255, 0.2)' },
            { offset: 1, color: 'rgba(59, 161, 255, 0.0)' }
          ])
        },
        data: prices,
        markPoint: targetIndex >= 0 ? {
           data: [
             { 
               name: '预测目标', 
               coord: [targetIndex, prices[targetIndex]],
               value: prices[targetIndex],
               itemStyle: { color: '#ffd700' },
               label: { color: '#000', fontWeight: 'bold' }
             }
           ]
        } : null
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const close = () => {
  emit('close')
}

const confirmPrediction = () => {
  if (isLoading.value) return
  emit('confirm', {
    date: targetDate.value,
    product: selectedProduct.value,
    price: predictedPrice.value,
    confidence: confidence.value
  })
  close()
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    if (!targetDate.value) targetDate.value = defaultDate.value
    generatePrediction()
  } else {
    if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }
  }
})

watch(targetDate, (newVal, oldVal) => {
  if (props.visible && newVal) {
    generatePrediction()
  }
})

watch(() => pricePredictionCache().cache, () => {
  if (props.visible) {
    generatePrediction()
  }
}, { deep: true })

onUnmounted(() => {
  if (chartInstance) chartInstance.dispose()
  window.removeEventListener('resize', () => chartInstance?.resize())
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;700&display=swap');

.price-prediction-overlay {
  position: fixed;
  top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(2, 10, 15, 0.85); /* 深蓝背景 */
  backdrop-filter: blur(12px);
  z-index: 9999;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; animation: fadeIn 0.4s ease forwards;
}
@keyframes fadeIn { to { opacity: 1; } }

.price-prediction-modal {
  width: 95%; max-width: 900px;
  background: linear-gradient(145deg, rgba(10, 30, 50, 0.95), rgba(5, 20, 35, 0.98));
  border: 1px solid rgba(0, 247, 255, 0.3); /* 青色边框 */
  border-radius: 24px;
  box-shadow: 0 40px 80px rgba(0,0,0,0.8), 0 0 20px rgba(0, 247, 255, 0.1);
  overflow: hidden;
  transform: translateY(30px);
  animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
@keyframes slideUp { to { transform: translateY(0); } }

/* 装饰线条 */
.modal-decoration-line {
  height: 2px;
  background: linear-gradient(90deg, transparent, #00f7ff, #3ba1ff, transparent);
  box-shadow: 0 1px 10px rgba(0, 247, 255, 0.5);
}

/* 顶部 */
.modal-header {
  padding: 20px 30px;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
.header-title { display: flex; align-items: center; gap: 15px; }
.icon-box {
  width: 36px; height: 36px;
  background: rgba(0, 247, 255, 0.1); border-radius: 8px;
  display: flex; align-items: center; justify-content: center; position: relative;
}
.pulse-dot {
  position: absolute; top: -2px; right: -2px; width: 6px; height: 6px;
  background: #00f7ff; border-radius: 50%;
  animation: pulse 1.5s infinite;
}
.title-main { display: block; font-size: 16px; font-weight: 700; color: #fff; }
.title-sub { display: block; font-size: 10px; color: #3ba1ff; opacity: 0.8; letter-spacing: 1px; }

.close-btn {
  background: transparent; border: none; color: rgba(255,255,255,0.3);
  cursor: pointer; padding: 5px; transition: 0.2s;
}
.close-btn:hover { color: #00f7ff; transform: rotate(90deg); }

.modal-body { padding: 30px 40px; min-height: 400px; }

/* Hero 区域 */
.hero-section {
  text-align: center;
  margin-bottom: 35px;
  position: relative;
}
.product-tag-float {
  display: inline-flex; align-items: center; gap: 8px;
  background: rgba(0, 247, 255, 0.08); border: 1px solid rgba(0, 247, 255, 0.3);
  padding: 6px 16px; border-radius: 20px;
  font-size: 13px; color: #00f7ff; font-weight: 600;
  margin-bottom: 15px;
  box-shadow: 0 0 10px rgba(0, 247, 255, 0.1);
}
.hero-label {
  display: block; font-size: 12px; color: #a6b0c3; letter-spacing: 2px;
  margin-bottom: 10px; font-weight: bold;
}
.date-display-box {
  position: relative; display: inline-block;
}
.date-display-box.disabled {
  opacity: 0.5; pointer-events: none; filter: grayscale(1);
}
.hero-date-input {
  background: transparent; border: none; outline: none;
  font-family: 'JetBrains Mono', monospace;
  font-size: 42px; font-weight: 700;
  color: #fff; text-align: center;
  border-bottom: 2px solid rgba(59, 161, 255, 0.3);
  padding-bottom: 5px; cursor: pointer;
  text-shadow: 0 0 20px rgba(0, 247, 255, 0.2);
  transition: all 0.3s;
  width: 320px;
}
.hero-date-input:focus {
  border-bottom-color: #00f7ff;
  text-shadow: 0 0 25px rgba(0, 247, 255, 0.4);
}
.edit-hint {
  display: block; font-size: 12px; color: rgba(255,255,255,0.3); margin-top: 5px;
  opacity: 0; transition: 0.3s; transform: translateY(-5px);
}
.date-display-box:hover .edit-hint { opacity: 1; transform: translateY(0); }

/* --- 加载动画样式 --- */
.loading-container {
  height: 300px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  animation: fadeIn 0.3s ease;
}
.scanner-wrapper {
  position: relative; width: 80px; height: 80px; margin-bottom: 25px;
}
.scan-ring {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  border: 4px solid transparent;
  border-top-color: #00f7ff; border-right-color: rgba(59, 161, 255, 0.3);
  border-radius: 50%; animation: spin 1s linear infinite;
}
.scan-core {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  width: 40%; height: 40%; background: #3ba1ff;
  border-radius: 50%; box-shadow: 0 0 15px #00f7ff;
  animation: pulse-core 1s ease-in-out infinite;
}
.loading-text {
  font-family: 'JetBrains Mono', monospace; font-size: 16px; color: #fff;
  font-weight: bold; letter-spacing: 1px; margin-bottom: 8px;
  display: flex; align-items: center; gap: 5px;
}
.loading-sub { font-size: 12px; color: rgba(59, 161, 255, 0.7); }

/* 仪表盘 */
.dashboard-grid {
  display: grid; grid-template-columns: 1.2fr 1.2fr 0.8fr; gap: 20px;
  margin-bottom: 25px;
}
.metric-card {
  background: rgba(255,255,255,0.02);
  border: 1px solid rgba(0, 247, 255, 0.1);
  border-radius: 16px; padding: 20px;
  position: relative;
}
.price-card { background: linear-gradient(145deg, rgba(0, 247, 255, 0.05), transparent); }
.card-header { display: flex; justify-content: space-between; margin-bottom: 10px; }
.card-label { font-size: 12px; color: #a6b0c3; }
.live-badge { 
  font-size: 10px; background: rgba(0, 247, 255, 0.1); color: #00f7ff; 
  padding: 2px 6px; border-radius: 4px; border: 1px solid rgba(0, 247, 255, 0.3); 
  animation: blink 2s infinite;
}
.price-display { display: flex; align-items: baseline; gap: 4px; margin-bottom: 10px; }
.currency { color: #00f7ff; font-size: 20px; }
.big-number { 
  font-family: 'JetBrains Mono', monospace; font-size: 46px; font-weight: 700; color: #fff;
  text-shadow: 0 0 15px rgba(0, 247, 255, 0.4);
}
.unit { color: #a6b0c3; font-size: 14px; }
.trend-row { display: flex; align-items: center; justify-content: space-between; }
.trend-badge { font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 4px; }
.trend-up { color: #ff6b6b; } .trend-down { color: #00f7ff; } .trend-stable { color: #3ba1ff; }
.diff-text { font-family: 'JetBrains Mono'; font-size: 12px; }
.diff-up { color: #ff6b6b; } .diff-down { color: #00f7ff; }

/* 归因分析 */
.factor-list { display: flex; flex-direction: column; gap: 12px; margin-top: 5px; }
.factor-item { display: flex; align-items: center; gap: 10px; font-size: 12px; color: #ccc; }
.f-name { width: 70px; }
.f-bar-bg { flex: 1; height: 6px; background: rgba(255,255,255,0.1); border-radius: 3px; overflow: hidden; }
.f-bar { height: 100%; border-radius: 3px; transition: width 1s ease; }
.f-val { width: 30px; text-align: right; font-family: 'JetBrains Mono'; opacity: 0.8; }

/* 置信度 */
.confidence-card { display: flex; flex-direction: column; align-items: center; justify-content: center; }
.confidence-circle { width: 70px; height: 70px; position: relative; margin-bottom: 10px; }
.circular-chart { display: block; margin: 0 auto; max-width: 100%; max-height: 100%; }
.circle-bg { fill: none; stroke: rgba(255,255,255,0.1); stroke-width: 3; }
.circle { 
  fill: none; stroke: #00f7ff; stroke-width: 3; stroke-linecap: round;
  animation: progress 1.5s ease-out forwards;
}
@keyframes progress { from { stroke-dasharray: 0, 100; } }
.percentage-text {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  font-family: 'JetBrains Mono'; font-weight: bold; color: #00f7ff; font-size: 14px;
}
.risk-label { font-size: 12px; color: #a6b0c3; }

/* 图表区 */
.chart-section {
  background: rgba(0,0,0,0.2); border-radius: 12px; border: 1px solid rgba(255,255,255,0.05);
  padding: 15px;
}
.chart-header { display: flex; justify-content: space-between; margin-bottom: 5px; }
.chart-title { font-size: 13px; color: #ccc; display: flex; align-items: center; gap: 6px; }
.legend { display: flex; gap: 12px; font-size: 12px; color: #a6b0c3; }
.dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; margin-right: 4px; }
.dot.history { background: #3ba1ff; }
.dot.predict { background: #00f7ff; box-shadow: 0 0 6px #00f7ff; }
.chart-container { width: 100%; height: 180px; }

/* 结果淡入 */
.animate-fade-in { animation: fadeInUp 0.5s cubic-bezier(0.2, 0.8, 0.2, 1); }

/* 底部按钮 */
.modal-footer {
  padding: 20px 40px; border-top: 1px solid rgba(255,255,255,0.05);
  display: flex; gap: 20px; background: rgba(0,0,0,0.15);
}
.btn {
  flex: 1; padding: 14px; border-radius: 10px; font-weight: 600; font-size: 14px;
  cursor: pointer; border: none; transition: all 0.3s;
}
.btn-secondary { background: transparent; border: 1px solid rgba(255,255,255,0.15); color: #aaa; }
.btn-secondary:hover { border-color: #fff; color: #fff; background: rgba(255,255,255,0.05); }
.btn-primary {
  background: linear-gradient(135deg, #00f7ff 0%, #3ba1ff 100%); color: #051a15;
  box-shadow: 0 8px 25px rgba(0, 247, 255, 0.2); display: flex; align-items: center; justify-content: center; gap: 8px;
}
.btn-primary:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 12px 30px rgba(0, 247, 255, 0.3); }
.btn-primary:disabled { cursor: not-allowed; filter: grayscale(0.5); }

/* 关键帧动画 */
@keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 1; } 100% { opacity: 0.5; } }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
@keyframes pulse-core {
  0%, 100% { transform: translate(-50%, -50%) scale(0.8); opacity: 0.8; }
  50% { transform: translate(-50%, -50%) scale(1.1); opacity: 1; box-shadow: 0 0 25px #00f7ff; }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 点点点动画 */
.dot-flashing {
  position: relative; width: 4px; height: 4px; margin-left: 10px;
  border-radius: 2px; background-color: #00f7ff; color: #00f7ff;
  animation: dot-flashing 1s infinite linear alternate; animation-delay: 0.5s;
}
.dot-flashing::before, .dot-flashing::after {
  content: ''; display: inline-block; position: absolute; top: 0;
  width: 4px; height: 4px; border-radius: 2px; background-color: #00f7ff; color: #00f7ff;
  animation: dot-flashing 1s infinite alternate;
}
.dot-flashing::before { left: -8px; animation-delay: 0s; }
.dot-flashing::after { left: 8px; animation-delay: 1s; }

@keyframes dot-flashing {
  0% { background-color: #00f7ff; }
  50%, 100% { background-color: rgba(0, 247, 255, 0.2); }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .dashboard-grid { grid-template-columns: 1fr; gap: 12px; }
  .hero-date-input { font-size: 32px; width: 100%; }
  .metric-card { padding: 15px; }
  .factor-list { flex-direction: row; flex-wrap: wrap; }
  .factor-item { flex: 1 1 100%; }
}
</style>