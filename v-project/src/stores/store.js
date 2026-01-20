import { defineStore } from 'pinia'
import provinceCapitalMap from '../assets/json/provinceCapitalMap.json'

const generateDates = (days) => {
  const dates = []
  const today = new Date()
  for (let i = 1; i <= days; i++) {
    const date = new Date(today)
    date.setDate(date.getDate() + i)
    dates.push(date.toISOString().split('T')[0].substring(5))
  }
  return dates
}

const generateTimeline = (basePrice, trend, days) => {
  const timeline = []
  const today = new Date()
  let currentPrice = basePrice

  for (let i = 1; i <= days; i++) {
    const date = new Date(today)
    date.setDate(date.getDate() + i)
    const dateStr = date.toISOString().split('T')[0].substring(5)

    let change = trend === '上升' ? (Math.random() * 0.4) - 0.1 : (Math.random() * 0.4) - 0.3
    currentPrice = Math.max(0.5, currentPrice + change)

    timeline.push({
      date: dateStr,
      price: currentPrice.toFixed(2),
      height: Math.min(100, Math.max(20, (currentPrice / (basePrice * 1.5)) * 100))
    })
  }

  return timeline
}

const hardcodedData = {
  '河南省-郑州市--大白菜': {
    province: '河南省',
    city: '郑州市',
    district: '中原区',
    product: '大白菜',
    basePrice: '2.35',
    predictedPrice: '2.68',
    trend: '上升',
    trendClass: 'trend-up',
    confidence: '87.5%',
    details: [
      { label: '市场供需', value: '供不应求', valueClass: '' },
      { label: '季节因素', value: '旺季效应', valueClass: '' },
      { label: '物流成本', value: '平稳', valueClass: '' },
      { label: '操作建议', value: '建议囤货', valueClass: 'action-buy' }
    ],
    timeline: generateTimeline(2.35, '上升', 7),
    factors: { weather: 78, inventory: 65, logistics: 45 },
    sevenDayPrediction: [
      { day: '01-20', price: '2.4', probability: 0.85 },
      { day: '01-21', price: '2.5', probability: 0.82 },
      { day: '01-22', price: '2.6', probability: 0.79 },
      { day: '01-23', price: '2.5', probability: 0.83 },
      { day: '01-24', price: '2.7', probability: 0.77 },
      { day: '01-25', price: '2.6', probability: 0.81 },
      { day: '01-26', price: '2.8', probability: 0.75 }
    ],
    thirtyDayTrend: generateTimeline(2.35, '上升', 30)
  },
  '四川省-成都市--黄瓜': {
    province: '四川省',
    city: '成都市',
    district: '武侯区',
    product: '黄瓜',
    basePrice: '7.25',
    predictedPrice: '6.82',
    trend: '下降',
    trendClass: 'trend-down',
    confidence: '91.2%',
    details: [
      { label: '市场供需', value: '供应充足', valueClass: '' },
      { label: '季节因素', value: '季节性回落', valueClass: '' },
      { label: '物流成本', value: '平稳', valueClass: '' },
      { label: '操作建议', value: '随用随采', valueClass: 'action-wait' }
    ],
    timeline: generateTimeline(7.25, '下降', 7),
    factors: { weather: 62, inventory: 85, logistics: 35 },
    sevenDayPrediction: [
      { day: '01-20', price: '6.8', probability: 0.88 },
      { day: '01-21', price: '7.0', probability: 0.86 },
      { day: '01-22', price: '6.9', probability: 0.84 },
      { day: '01-23', price: '7.0', probability: 0.82 },
      { day: '01-24', price: '6.7', probability: 0.80 },
      { day: '01-25', price: '6.6', probability: 0.78 },
      { day: '01-26', price: '6.8', probability: 0.76 }
    ],
    thirtyDayTrend: generateTimeline(7.25, '下降', 30)
  }
}

export const getHardcodedData = (province, city, district, product) => {
  const key = `${province}-${city}-${district}-${product}`
  const keyWithoutDistrict = `${province}-${city}--${product}`

  if (hardcodedData[key]) {
    return hardcodedData[key]
  }

  if (hardcodedData[keyWithoutDistrict]) {
    return hardcodedData[keyWithoutDistrict]
  }

  return null
}

export const getAllHardcodedKeys = () => {
  return Object.keys(hardcodedData)
}

export { hardcodedData }

// ==========================================
// 1. 地理位置 Store (管理地图联动)
// ==========================================
export const mapLocation = defineStore('mapLocationPinia', {
  state: () => {
    return {
      currentProvince: '河南省', // 省
      currentCity: '郑州市',     // 市
      currentDistrict: '',     // 区县 (默认为空)
    }
  },
  actions: {
    // 设置省份 -> 自动设置为对应省会城市，重置区
    setCurrentProvince(province) {
      this.currentProvince = province
      this.currentCity = provinceCapitalMap[province]?.name || ''
      this.currentDistrict = ''
    },

    // 设置城市 -> 自动重置区
    setCurrentCity(city) {
      this.currentCity = city
      this.currentDistrict = ''
    },

    // 设置区县
    setCurrentDistrict(district) {
      this.currentDistrict = district
    },

    // 重置回默认状态
    resetLocation() {
      this.currentProvince = '河南省'
      this.currentCity = '郑州市'
      this.currentDistrict = ''
    },
  },
})

// ==========================================
// 2. 农作物/蔬菜 Store (管理业务筛选)
// ==========================================
export const mapProduct = defineStore('mapProductPinia', {
  state: () => {
    return {
      currentProduct: '大白菜', // 当前选中的蔬菜
    }
  },
  actions: {
    // 设置农作物
    setCurrentProduct(product) {
      this.currentProduct = product
    },

    // 重置农作物 (可选)
    resetProduct() {
      this.currentProduct = '大白菜'
    }
  },
})

// ==========================================
// 3. 价格预测数据缓存 Store (管理预测数据)
// ==========================================
export const pricePredictionCache = defineStore('pricePredictionCachePinia', {
  state: () => {
    return {
      cache: {}, // 缓存数据，key格式: "省-市-区-蔬菜名"
    }
  },
  actions: {
    // 生成缓存key
    generateCacheKey(province, city, district, productName) {
      return `${province}-${city}-${district}-${productName}`
    },

    // 获取缓存数据
    getCache(province, city, district, productName) {
      const key = this.generateCacheKey(province, city, district, productName)
      return this.cache[key] || null
    },

    // 设置缓存数据
    setCache(province, city, district, productName, data) {
      const key = this.generateCacheKey(province, city, district, productName)
      this.cache[key] = data
    },

    // 清除指定缓存
    clearCache(province, city, district, productName) {
      const key = this.generateCacheKey(province, city, district, productName)
      delete this.cache[key]
    },

    // 清除所有缓存
    clearAllCache() {
      this.cache = {}
    }
  },
  persist: true // 启用持久化存储
})

// ==========================================
// 4. 语音播报 Store (管理语音播报状态)
// ==========================================
export const voiceBroadcast = defineStore('voiceBroadcastPinia', {
  state: () => {
    return {
      isEnabled: false, // 语音播报是否开启，默认为未开启
    }
  },
  actions: {
    // 切换语音播报状态
    toggleVoiceBroadcast() {
      this.isEnabled = !this.isEnabled
    },

    // 开启语音播报
    enableVoiceBroadcast() {
      this.isEnabled = true
    },

    // 关闭语音播报
    disableVoiceBroadcast() {
      this.isEnabled = false
    },
  },
  persist: true // 启用持久化存储
})

// ==========================================
// 5. 语音识别 Store (管理语音识别状态)
// ==========================================
export const voiceRecognition = defineStore('voiceRecognitionPinia', {
  state: () => {
    return {
      isEnabled: false, // 语音识别是否开启，默认为未开启
      isListening: false, // 是否正在监听
      result: '', // 识别结果
    }
  },
  actions: {
    // 切换语音识别状态
    toggleVoiceRecognition() {
      this.isEnabled = !this.isEnabled
    },

    // 开启语音识别
    enableVoiceRecognition() {
      this.isEnabled = true
    },

    // 关闭语音识别
    disableVoiceRecognition() {
      this.isEnabled = false
    },

    // 开始监听
    startListening() {
      this.isListening = true
      this.result = ''
    },

    // 停止监听
    stopListening() {
      this.isListening = false
    },

    // 设置识别结果
    setResult(text) {
      this.result = text
    },

    // 清空识别结果
    clearResult() {
      this.result = ''
    },
  },
  persist: true // 启用持久化存储
})



