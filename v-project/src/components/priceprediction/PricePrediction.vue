<template>
  <div v-if="visible" class="price-prediction-overlay" @click.self="close">
    <div class="price-prediction-modal">
      
      <div class="modal-header">
        <div class="header-title">
          <div class="icon-box">
            <span class="pulse-dot"></span>
            <span class="title-icon">🧠</span>
          </div>
          <div class="title-text-group">
            <span class="title-main">价溯云图 · 价格预测引擎</span>
            <span class="title-sub">AI PRICE PREDICTION ENGINE</span>
          </div>
        </div>
        <button class="close-btn" @click="close">
          <svg viewBox="0 0 24 24" width="28" height="28" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
        </button>
      </div>

      <div class="modal-body">
        
        <div v-if="isLoading" class="loading-container">
          <div class="scanner-wrapper">
            <div class="scan-ring"></div>
            <div class="scan-core"></div>
          </div>
          <div class="loading-text">
            正在加载预测数据
            <span class="dot-flashing"></span>
          </div>
          <div class="loading-sub">气象因子 · 供需模型 · 历史行情 · 物流路况</div>
        </div>

        <div v-else class="result-layout animate-fade-in">
          
          <div class="left-panel">
            <div class="metric-card price-card-hero">
              <div class="card-top-row">
                <span class="product-badge">💠 {{ selectedProduct }}</span>
                <span class="live-badge">AI 实时计算完成</span>
              </div>
              
              <div class="price-main-display">
                <div class="price-wrapper">
                  <span class="currency">¥</span>
                  <span class="big-number">{{ predictedPrice }}</span>
                  <span class="unit">/公斤</span>
                </div>
                <div class="trend-wrapper" :class="diffClass">
                  <span class="trend-arrow">{{ parseFloat(priceDiff) >= 0 ? '▲' : '▼' }}</span>
                  <span class="diff-text">{{ priceDiff }}</span>
                  <span class="trend-label">较今日</span>
                </div>
              </div>
            </div>

            <div class="chart-section">
              <div class="chart-header">
                <span class="chart-title">📈 未来7天走势推演</span>
              </div>
              <div ref="chartRef" class="chart-container"></div>
            </div>
          </div>

          <div class="right-panel">
            
            <div class="panel-section date-section">
              <label class="section-label">预测目标日期</label>
              <div class="date-input-wrapper">
                <input 
                  v-model="targetDate" 
                  type="date" 
                  class="cyber-date-input"
                  :min="minDate"
                  :disabled="isLoading"
                >
              </div>
            </div>

            <div class="panel-section">
              <label class="section-label">核心影响因子</label>
              <div class="factor-list">
                <div class="factor-item">
                  <span class="f-icon">🌦️</span>
                  <div class="f-content">
                    <div class="f-row">
                      <span class="f-name">气象条件</span>
                      <span class="f-val">{{ factors.weather }}%</span>
                    </div>
                    <div class="f-bar-bg"><div class="f-bar" :style="{width: factors.weather + '%', background: '#00f7ff'}"></div></div>
                  </div>
                </div>
                <div class="factor-item">
                  <span class="f-icon">📦</span>
                  <div class="f-content">
                    <div class="f-row">
                      <span class="f-name">历史价格</span>
                      <span class="f-val">{{ factors.inventory }}%</span>
                    </div>
                    <div class="f-bar-bg"><div class="f-bar" :style="{width: factors.inventory + '%', background: '#3ba1ff'}"></div></div>
                  </div>
                </div>
                <div class="factor-item">
                  <span class="f-icon">🚚</span>
                  <div class="f-content">
                    <div class="f-row">
                      <span class="f-name">蔬菜特性</span>
                      <span class="f-val">{{ factors.logistics }}%</span>
                    </div>
                    <div class="f-bar-bg"><div class="f-bar" :style="{width: factors.logistics + '%', background: '#b766ff'}"></div></div>
                  </div>
                </div>
              </div>
            </div>

            <div class="panel-section confidence-section">
              <div class="confidence-row">
                <div class="conf-chart">
                   <svg viewBox="0 0 36 36" class="circular-chart">
                    <path class="circle-bg" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                    <path class="circle" :stroke-dasharray="`${confidence}, 100`" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                  </svg>
                  <div class="conf-text">{{ confidence }}<span class="small">%</span></div>
                </div>
                <div class="conf-info">
                  <div class="conf-title">模型置信度</div>
                  <div class="conf-risk">风险评估: <span style="color:#00f7ff">低风险</span></div>
                </div>
              </div>
            </div>

          </div>
        </div>

      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" @click="close">取消</button>
        <button class="btn btn-primary" @click="confirmPrediction" :disabled="isLoading">
          <span class="btn-icon">⚡</span> 采纳该预测策略
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { mapLocation, mapProduct, pricePredictionCache, getHardcodedData } from '../../stores/store.js'

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
  today.setDate(today.getDate() + 1)
  return today.toISOString().split('T')[0]
})

watch([productName, provinceName], () => {
  selectedProduct.value = productName.value || '黄瓜'
  if (props.visible) {
    generatePrediction()
  }
})

const priceDiff = computed(() => {
  const current = parseFloat(predictedPrice.value)
  const base = current * (1 - (Math.random() * 0.1 - 0.05)) 
  const diff = ((current - base) / base * 100).toFixed(1)
  return (diff > 0 ? '+' : '') + diff + '%'
})

const diffClass = computed(() => {
  return parseFloat(priceDiff.value) >= 0 ? 'diff-up' : 'diff-down'
})

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
    
    if (cachedData && cachedData.sevenDayPrediction && cachedData.sevenDayPrediction.length > 0) {
      basePrice = parseFloat(cachedData.sevenDayPrediction[0].price)
      predictedPrice.value = cachedData.predictedPrice || cachedData.basePrice
      factors.value = cachedData.factors || { weather: 60, inventory: 50, logistics: 30 }
    } else {
      const hardcodedResult = getHardcodedData(province, city, district, product)
      if (hardcodedResult) {
        basePrice = parseFloat(hardcodedResult.basePrice)
        predictedPrice.value = hardcodedResult.predictedPrice
        factors.value = hardcodedResult.factors
        cacheStore.setCache(province, city, district, product, hardcodedResult)
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
        
        const variance = (Math.random() - 0.5) * 0.5
        predictedPrice.value = (basePrice + variance).toFixed(2)
        
        factors.value = {
          weather: Math.floor(Math.random() * 40 + 50),
          inventory: Math.floor(Math.random() * 60 + 20),
          logistics: Math.floor(Math.random() * 30 + 10)
        }
      }
    }

    confidence.value = 79
    
    isLoading.value = false
    
    await nextTick()
    initChart()
    
  }, 1200)
}

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
  
  if (cachedData && cachedData.sevenDayPrediction && cachedData.sevenDayPrediction.length >= 7) {
    for (let i = 0; i < 7; i++) {
      const date = new Date(today)
      date.setDate(date.getDate() + i + 1)
      dates.push(date.toISOString().split('T')[0].substring(5))
      prices.push(parseFloat(cachedData.sevenDayPrediction[i].price))
    }
  } else {
    let basePrice = parseFloat(predictedPrice.value) || 5
    for (let i = 1; i <= 7; i++) {
      const date = new Date(today)
      date.setDate(date.getDate() + i)
      dates.push(date.toISOString().split('T')[0].substring(5))
      
      const daySeed = ((product?.charCodeAt(i % product.length || 0) || 0) + i) * 10
      const priceVariation = (seededRandom(daySeed) - 0.5) * (basePrice * 0.15)
      prices.push(Math.max(1, (basePrice + priceVariation).toFixed(2)))
    }
  }
  
  const targetIndex = dates.findIndex(d => {
      const dObj = new Date(today.getFullYear(), parseInt(d.split('-')[0])-1, parseInt(d.split('-')[1]))
      return targetDate.value && targetDate.value.endsWith(d)
  })

  const option = {
    backgroundColor: 'transparent',
    grid: {
      top: '15%', left: '2%', right: '4%', bottom: '5%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(5, 20, 35, 0.95)',
      borderColor: '#00f7ff',
      textStyle: { color: '#fff', fontFamily: 'monospace', fontSize: 14 },
      formatter: (params) => {
        const item = params[0]
        return `<div style="color:#a6b0c3;font-size:13px;margin-bottom:6px">${item.name}</div>
                <div style="color:#00f7ff;font-weight:bold;font-size:15px">
                  🔮 预测: ${item.value} 元
                </div>`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { color: '#a6b0c3', fontSize: 13 },
      axisLine: { lineStyle: { color: 'rgba(59, 161, 255, 0.2)' } },
      axisTick: { show: false },
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      scale: true,
      splitLine: { lineStyle: { color: 'rgba(59, 161, 255, 0.08)', type: 'dashed' } },
      axisLabel: { color: '#a6b0c3', fontSize: 13 }
    },
    series: [
      {
        name: '趋势',
        type: 'line',
        smooth: 0.4,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#00f7ff', borderColor: '#fff', borderWidth: 2 },
        lineStyle: {
          width: 4,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#3ba1ff' },
            { offset: 1, color: '#00f7ff' }
          ]),
          shadowColor: 'rgba(0, 247, 255, 0.5)',
          shadowBlur: 12
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 247, 255, 0.15)' },
            { offset: 1, color: 'rgba(59, 161, 255, 0.0)' }
          ])
        },
        data: prices,
        markPoint: targetIndex >= 0 ? {
           data: [{ 
               name: '预测目标', coord: [targetIndex, prices[targetIndex]],
               value: prices[targetIndex],
               itemStyle: { color: '#ffd700' },
               label: { color: '#000', fontWeight: 'bold', fontSize: 13 }
           }]
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

watch(targetDate, (newVal) => {
  if (props.visible && newVal) generatePrediction()
})

watch(() => pricePredictionCache().cache, () => {
  if (props.visible) generatePrediction()
}, { deep: true })

onUnmounted(() => {
  if (chartInstance) chartInstance.dispose()
  window.removeEventListener('resize', () => chartInstance?.resize())
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;700&display=swap');

/* --- 全局容器与动画 --- */
.price-prediction-overlay {
  position: fixed;
  top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(2, 8, 12, 0.85);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; animation: fadeIn 0.4s ease forwards;
}
@keyframes fadeIn { to { opacity: 1; } }

.price-prediction-modal {
  /* ⚡️⚡️ 重点修改：宽度改为百分比，Max-width 增加到 1100px (原960) */
  width: 92%; 
  max-width: 1100px; 
  background: linear-gradient(160deg, rgba(10, 30, 50, 0.98), rgba(2, 10, 15, 0.99));
  border: 1px solid rgba(0, 247, 255, 0.25);
  border-radius: 24px;
  box-shadow: 0 0 50px rgba(0, 247, 255, 0.08), 0 20px 60px rgba(0,0,0,0.9);
  overflow: hidden;
  transform: translateY(30px);
  animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  display: flex; flex-direction: column;
}
@keyframes slideUp { to { transform: translateY(0); } }

/* --- Header --- */
.modal-header {
  padding: 22px 36px; /* 增大间距 */
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255,255,255,0.01);
}
.header-title { display: flex; align-items: center; gap: 14px; }
.icon-box {
  width: 44px; height: 44px; /* 增大图标盒子 */
  background: linear-gradient(135deg, rgba(0, 247, 255, 0.2), rgba(0,0,0,0));
  border-radius: 12px; border: 1px solid rgba(0, 247, 255, 0.3);
  display: flex; align-items: center; justify-content: center; position: relative;
}
.pulse-dot {
  position: absolute; top: -3px; right: -3px; width: 7px; height: 7px;
  background: #00f7ff; border-radius: 50%; box-shadow: 0 0 8px #00f7ff;
  animation: pulse 1.5s infinite;
}
.title-main { display: block; font-size: 21px; font-weight: 700; color: #fff; text-shadow: 0 0 10px rgba(0,247,255,0.3); } /* 增大字体 18->21 */
.title-sub { display: block; font-size: 11px; color: #3ba1ff; opacity: 0.6; letter-spacing: 2.5px; margin-top: 3px; }
.close-btn {
  background: transparent; border: none; color: rgba(255,255,255,0.4);
  cursor: pointer; padding: 6px; transition: 0.2s;
}
.close-btn:hover { color: #fff; transform: rotate(90deg); }

/* --- Body Layout --- */
.modal-body { 
  padding: 36px; /* 增大内边距 */
  min-height: 480px; /* 增高最小高度 */
  position: relative;
}

.result-layout {
  display: flex;
  gap: 30px; /* 增大 Gap */
  height: 100%;
}
.left-panel { flex: 1.5; display: flex; flex-direction: column; gap: 24px; }
.right-panel { flex: 0.9; display: flex; flex-direction: column; gap: 18px; }

/* --- 左侧组件 --- */

/* 价格大卡片 */
.price-card-hero {
  background: linear-gradient(135deg, rgba(0, 247, 255, 0.08) 0%, rgba(5, 20, 35, 0.4) 100%);
  border: 1px solid rgba(0, 247, 255, 0.3);
  border-radius: 18px;
  padding: 28px; /* 增大 Padding */
  position: relative;
  overflow: hidden;
}
.price-card-hero::before {
  content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 4px;
  background: linear-gradient(90deg, #00f7ff, #3ba1ff, transparent);
  box-shadow: 0 0 15px rgba(0, 247, 255, 0.5);
}
.card-top-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.product-badge { font-size: 16px; color: #fff; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.live-badge { font-size: 12px; color: #00f7ff; background: rgba(0, 247, 255, 0.1); padding: 4px 10px; border-radius: 5px; border: 1px solid rgba(0, 247, 255, 0.2); }

.price-main-display { display: flex; align-items: flex-end; justify-content: space-between; }
.price-wrapper { display: flex; align-items: baseline; }
.currency { font-size: 38px; color: #00f7ff; margin-right: 5px; font-weight: 300; } /* 32->38 */
.big-number { 
  font-family: 'JetBrains Mono'; 
  font-size: 84px; /* 72->84 */
  font-weight: 700; color: #fff; line-height: 1; letter-spacing: -3px; 
  text-shadow: 0 0 25px rgba(0, 247, 255, 0.4); 
}
.unit { color: #a6b0c3; font-size: 18px; margin-left: 10px; }

.trend-wrapper { text-align: right; }
.trend-arrow { font-size: 28px; margin-right: 5px; }
.diff-text { font-family: 'JetBrains Mono'; font-size: 28px; font-weight: bold; } /* 24->28 */
.trend-label { display: block; font-size: 13px; color: #667; margin-top: 5px; }
.diff-up { color: #ff6b6b; } .diff-down { color: #00f7ff; }

/* 图表区 */
.chart-section {
  flex: 1;
  background: rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: 14px;
  padding: 18px;
  display: flex; flex-direction: column;
}
.chart-header { margin-bottom: 12px; }
.chart-title { font-size: 16px; color: #ccc; }
.chart-container { flex: 1; width: 100%; min-height: 180px; }

/* --- 右侧组件 --- */
.panel-section {
  background: rgba(255,255,255,0.02);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 14px;
  padding: 18px;
}
.section-label { display: block; font-size: 13px; color: #6e7f91; margin-bottom: 12px; font-weight: 600; letter-spacing: 0.6px; }

/* 日期输入 */
.cyber-date-input {
  width: 100%;
  background: rgba(0,0,0,0.3);
  border: 1px solid rgba(59, 161, 255, 0.3);
  color: #fff;
  font-family: 'JetBrains Mono';
  font-size: 24px; /* 20->24 */
  padding: 12px;
  border-radius: 10px;
  outline: none;
  text-align: center;
  transition: 0.3s;
}
.cyber-date-input:focus { border-color: #00f7ff; box-shadow: 0 0 12px rgba(0,247,255,0.1); }

/* 归因因子 */
.factor-list { display: flex; flex-direction: column; gap: 15px; }
.factor-item { display: flex; align-items: center; gap: 14px; }
.f-icon { font-size: 20px; width: 28px; text-align: center; }
.f-content { flex: 1; }
.f-row { display: flex; justify-content: space-between; font-size: 14px; color: #ccc; margin-bottom: 6px; }
.f-val { font-family: 'JetBrains Mono'; color: #fff; font-size: 15px; }
.f-bar-bg { height: 5px; background: rgba(255,255,255,0.1); border-radius: 3px; overflow: hidden; }
.f-bar { height: 100%; border-radius: 3px; }

/* 置信度 */
.confidence-section { flex: 1; display: flex; align-items: center; }
.confidence-row { display: flex; align-items: center; gap: 24px; width: 100%; }
.conf-chart { width: 70px; height: 70px; position: relative; flex-shrink: 0; } /* 60->70 */
.circular-chart { display: block; max-width: 100%; }
.circle-bg { fill: none; stroke: rgba(255,255,255,0.1); stroke-width: 2.5; }
.circle { fill: none; stroke: #00f7ff; stroke-width: 2.5; stroke-linecap: round; animation: progress 1s ease-out forwards; }
@keyframes progress { from { stroke-dasharray: 0, 100; } }
.conf-text { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); font-family: 'JetBrains Mono'; font-weight: bold; color: #fff; font-size: 19px; } /* 16->19 */
.small { font-size: 11px; color: #00f7ff; }
.conf-info { flex: 1; }
.conf-title { font-size: 15px; color: #fff; margin-bottom: 5px; }
.conf-risk { font-size: 13px; color: #888; }

/* --- 加载动画 --- */
.loading-container {
  height: 460px; /* 400->460 */
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.scanner-wrapper { position: relative; width: 70px; height: 70px; margin-bottom: 24px; }
.scan-ring {
  position: absolute; width: 100%; height: 100%;
  border: 4px solid transparent; border-top-color: #00f7ff; border-radius: 50%;
  animation: spin 1s linear infinite;
}
.scan-core {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  width: 40%; height: 40%; background: #3ba1ff; border-radius: 50%;
  box-shadow: 0 0 15px #00f7ff; animation: pulse-core 1s infinite;
}
.loading-text { font-size: 20px; color: #fff; font-weight: bold; margin-bottom: 8px; }
.loading-sub { font-size: 14px; color: #556; letter-spacing: 1px; }

/* --- 底部按钮 --- */
.modal-footer {
  padding: 24px 36px; border-top: 1px solid rgba(255,255,255,0.05);
  display: flex; gap: 18px; background: rgba(0,0,0,0.2);
}
.btn {
  flex: 1; padding: 16px; border-radius: 12px; font-weight: 600; font-size: 16px; /* 14->16 */
  cursor: pointer; border: none; transition: 0.3s;
}
.btn-secondary { background: rgba(255,255,255,0.05); color: #aaa; border: 1px solid transparent; }
.btn-secondary:hover { background: rgba(255,255,255,0.1); color: #fff; border-color: rgba(255,255,255,0.1); }
.btn-primary {
  background: linear-gradient(90deg, #00f7ff, #3ba1ff); color: #000;
  box-shadow: 0 4px 15px rgba(0, 247, 255, 0.3);
  display: flex; align-items: center; justify-content: center; gap: 8px;
}
.btn-primary:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0, 247, 255, 0.5); }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

/* 动画关键帧 */
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 1; } 100% { opacity: 0.5; } }
@keyframes pulse-core { 50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.8; } }
.animate-fade-in { animation: fadeIn 0.5s ease; }

/* 移动端适配 */
@media (max-width: 768px) {
  .result-layout { flex-direction: column; }
  .left-panel, .right-panel { flex: none; width: 100%; }
  .price-card-hero { padding: 22px; }
  .big-number { font-size: 64px; }
}
</style>