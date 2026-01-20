<template>
  <div class="side-decor right-panel">
    <div class="selector-section">
      <div class="selector-label">选择监测品种</div>
      <div class="tech-select-wrapper">
        <el-select
          v-model="selectedProduct"
          placeholder="请选择农产品"
          filterable
          popper-class="tech-select-dropdown-blue" 
          class="tech-select"
          @change="handleProductChange"
        >
          <el-option-group
            v-for="group in productOptions"
            :key="group.oneLevel"
            :label="group.oneLevel"
          >
            <el-option
              v-for="item in group.items"
              :key="item.varietyname"
              :label="item.varietyname"
              :value="item.varietyname"
            >
              <span class="option-item">
                <span class="option-category">{{ item.twoLevel || group.oneLevel }}</span>
                <span class="option-name">{{ item.varietyname }}</span>
              </span>
            </el-option>
          </el-option-group>
        </el-select>
      </div>
    </div>

    <div class="divider-line"></div>

    <div class="price-ranking-section">
      <div class="section-title">未来7天价格预测</div>
      <div class="ranking-list">
        <div
          v-for="(item, index) in provincePriceRanking"
          :key="item.day"
          class="ranking-item"
        >
          <div class="day-label">{{ item.day }}</div>
          <div class="price-info">
            <span class="price-value">{{ item.price }}</span>
            <span class="price-unit">元/斤</span>
          </div>
          <div class="probability-badge">
             <span class="probability-value">{{ (item.probability * 100).toFixed(0) }}%</span>
            <span class="probability-label">概率</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getAllVaegettableTypes } from '../../api/requestFuntion.js'
import { mapProduct, mapLocation, pricePredictionCache, getHardcodedData } from '../../stores/store.js'

const emit = defineEmits(['product-change'])

const mapProductStore = mapProduct()
const mapLocationStore = mapLocation()
const productOptions = ref([])
const selectedProduct = ref(mapProductStore.currentProduct)

const provincePriceRanking = ref([])

const seededRandom = (seed) => {
  let x = Math.sin(seed) * 10000
  return x - Math.floor(x)
}

const generatePredictionData = (productName, cityName) => {
  const province = mapLocationStore.currentProvince || '河南省'
  const city = mapLocationStore.currentCity || '郑州市'
  const district = mapLocationStore.currentDistrict || '中原区'
  
  const cacheStore = pricePredictionCache()
  
  const cachedData = cacheStore ? cacheStore.getCache(province, city, district, productName) : null
  
  if (cachedData && cachedData.sevenDayPrediction) {
    return cachedData.sevenDayPrediction
  }
  
  const hardcodedResult = getHardcodedData(province, city, district, productName)
  if (hardcodedResult && hardcodedResult.sevenDayPrediction) {
    cacheStore.setCache(province, city, district, productName, hardcodedResult)
    return hardcodedResult.sevenDayPrediction
  }
  
  let basePrice = 0
  
  if (province === '河南省' && productName === '大白菜') {
    basePrice = 1.5 + Math.random() * 1.5
  } else if (province === '河南省' && productName === '黄瓜') {
    basePrice = 5.5 + Math.random() * 2.5
  } else if (province === '四川省' && productName === '黄瓜') {
    basePrice = 7 + Math.random() * 2
  } else if (province === '四川省' && productName === '大白菜') {
    basePrice = 2 + Math.random() * 1
  } else {
    basePrice = 5 + seededRandom((productName?.length || 0) + (cityName?.length || 0)) * 5
  }
  
  const data = []
  const today = new Date()
  
  for (let i = 1; i <= 7; i++) {
    const date = new Date(today)
    date.setDate(date.getDate() + i)
    const dateStr = date.toISOString().split('T')[0].substring(5)
    
    const daySeed = ((productName?.charCodeAt(i % productName.length || 0) || 0) + (cityName?.charCodeAt(i % cityName.length || 0) || 0)) * i
    const priceVariation = (seededRandom(daySeed) - 0.5) * (basePrice * 0.2)
    const probability = 0.6 + seededRandom(daySeed + 100) * 0.35
    
    data.push({
      day: dateStr,
      price: Math.max(1, (basePrice + priceVariation).toFixed(1)),
      probability: Math.min(0.98, Math.max(0.65, probability))
    })
  }
  
  return data
}

const updateProvincePriceRanking = () => {
  const productName = selectedProduct.value || '大白菜'
  const cityName = mapLocationStore.currentCity || '郑州市'
  const predictionData = generatePredictionData(productName, cityName)
  provincePriceRanking.value = predictionData
}

const loadProductOptions = async () => {
  try {
    const data = await getAllVaegettableTypes()
    if (data) {
      const groupedData = {}
      data.forEach((item) => {
        if (!item.oneLevel) return
        if (!groupedData[item.oneLevel]) {
          groupedData[item.oneLevel] = {
            oneLevel: item.oneLevel,
            items: [],
          }
        }
        groupedData[item.oneLevel].items.push(item)
      })
      productOptions.value = Object.values(groupedData)

      if (
        !selectedProduct.value &&
        productOptions.value.length > 0 &&
        productOptions.value[0].items.length > 0
      ) {
        const firstItem = productOptions.value[0].items[0].varietyname
        handleProductChange(firstItem)
      }
    }
  } catch (error) {
    console.error('获取农产品分类数据失败：', error)
  }
}

const handleProductChange = (val) => {
  selectedProduct.value = val
  mapProductStore.setCurrentProduct(val)
  emit('product-change', val)
}

watch(
  () => mapProductStore.currentProduct,
  (newProduct) => {
    if (newProduct) {
      if (newProduct !== selectedProduct.value) {
        selectedProduct.value = newProduct
      }
      updateProvincePriceRanking()
    }
  }
)

watch(
  () => mapLocationStore.currentCity,
  (newCity) => {
    if (newCity) {
      updateProvincePriceRanking()
    }
  }
)

watch(
  () => pricePredictionCache().cache,
  () => {
    updateProvincePriceRanking()
  },
  { deep: true }
)

onMounted(() => {
  loadProductOptions()
  updateProvincePriceRanking()
})
</script>

<style scoped>
/* ==================== 
   布局与容器样式
   ==================== */
.side-decor {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  
  /* --- 修改：进一步缩窄 --- */
  width: 19%; 
  max-width: 260px; /* 限制最大宽度 */
  min-width: 230px; /* 保证最小可读性 */
  /* --------------------- */
  
  height: 95%; 
  z-index: 30;
  display: flex;
  flex-direction: column;
  gap: 8px; 
  pointer-events: auto;
}

.right-panel {
  right: 20px;
  border-right: 1px solid rgba(59, 161, 255, 0.3);
  padding-right: 15px;
  text-align: right;
  align-items: flex-end;
  background: linear-gradient(270deg, rgba(59, 161, 255, 0.08), transparent);
}

.selector-section {
  width: 100%;
  margin-bottom: 2px;
  flex-shrink: 0; 
}

.selector-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 4px;
  letter-spacing: 1px;
  text-align: right;
}

.selector-label::before {
  content: '▼';
  font-size: 10px;
  color: #3ba1ff; 
  margin-right: 4px;
}

.tech-select-wrapper {
  position: relative;
  width: 100%;
}

.tech-select {
  width: 100%;
}

.divider-line {
  width: 100%;
  height: 1px;
  background: repeating-linear-gradient(
    90deg,
    rgba(59, 161, 255, 0.4) 0,
    rgba(59, 161, 255, 0.4) 4px,
    transparent 4px,
    transparent 8px
  );
  margin: 4px 0;
  flex-shrink: 0;
}

/* --- 列表区域占满剩余空间 --- */
.price-ranking-section {
  width: 100%;
  margin: 0; 
  flex: 1; 
  display: flex;
  flex-direction: column;
  overflow: hidden; 
  min-height: 0;
}

.section-title {
  font-size: 13px;
  color: #B766FF; 
  font-weight: bold;
  margin-bottom: 8px;
  text-align: right;
  letter-spacing: 1px;
  text-shadow: 0 0 6px rgba(183, 102, 255, 0.4);
  flex-shrink: 0;
}

/* 列表容器 */
.ranking-list {
  flex: 1; 
  display: flex;
  flex-direction: column;
  justify-content: space-between; 
  gap: 4px; 
  overflow: hidden; 
}

/* 列表项：保持三列均分，缩小间隙适应窄屏 */
.ranking-item {
  display: grid;
  grid-template-columns: repeat(3, 1fr); 
  /* --- 修改：减小间隙 --- */
  gap: 2px; 
  align-items: center;
  
  /* --- 修改：减小左右内边距 --- */
  padding: 0 5px; 
  
  background: rgba(59, 161, 255, 0.05);
  border: 1px solid rgba(59, 161, 255, 0.15);
  border-radius: 4px;
  transition: all 0.3s ease;
  flex: 1; 
  min-height: 0; 
}

.ranking-item:hover {
  background: rgba(59, 161, 255, 0.15);
  border-color: rgba(59, 161, 255, 0.4);
  box-shadow: 0 0 15px rgba(59, 161, 255, 0.2);
  transform: translateX(-5px); 
}

/* 日期：Grid内左对齐 */
.day-label {
  justify-self: start; 
  text-align: left;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  white-space: nowrap; /* 防止日期换行 */
}

/* 价格：Grid内居中对齐 */
.price-info {
  justify-self: center; 
  display: flex;
  align-items: baseline;
  gap: 1px;
  white-space: nowrap; 
}

.price-value {
  /* --- 修改：字号微调，适应窄屏 --- */
  font-size: 16px; 
  font-weight: bold;
  color: #45d0b2; 
  font-family: 'Arial', sans-serif;
  text-shadow: 0 0 8px rgba(69, 208, 178, 0.4);
}

.price-unit {
  font-size: 10px;
  color: #45d0b2; 
  text-shadow: 0 0 8px rgba(69, 208, 178, 0.4);
}

/* 概率：Grid内右对齐 */
.probability-badge {
  justify-self: end; 
  
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(59, 161, 255, 0.1);
  border: 1px solid rgba(59, 161, 255, 0.25);
  border-radius: 3px;
  /* --- 修改：减小徽章内边距 --- */
  padding: 2px 4px;
  min-width: 45px; /* 减小最小宽度 */
  height: auto;
  padding-top: 3px;
  padding-bottom: 3px;
}

.probability-value {
  /* --- 修改：字号微调 --- */
  font-size: 12px;
  font-weight: bold;
  color: #3ba1ff;
  line-height: 1.1;
  text-shadow: 0 0 5px rgba(59, 161, 255, 0.4);
}

.probability-label {
  font-size: 9px;
  color: rgba(59, 161, 255, 0.7);
  margin-top: 1px;
}

/* ==================== 
   输入框样式覆盖 (科技蓝系)
   ==================== */
:deep(.el-input__wrapper) {
  background-color: transparent !important;
  border: 1px solid rgba(59, 161, 255, 0.3) !important;
  box-shadow: none !important;
  border-radius: 2px;
  padding: 6px 8px !important; /* 减小输入框内边距 */
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: rgba(59, 161, 255, 0.6) !important;
  box-shadow: 0 0 8px rgba(59, 161, 255, 0.2) !important;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #3ba1ff !important;
  box-shadow: 0 0 12px rgba(59, 161, 255, 0.3) !important;
}

:deep(.el-input__inner) {
  color: #fff !important;
  font-weight: 500;
  font-size: 13px; /* 字号微调 */
  text-align: right;
  font-family: 'Microsoft YaHei', sans-serif;
  background-color: transparent !important;
}

:deep(.el-select__wrapper) {
  background-color: transparent !important;
}

:deep(.el-input) {
  background-color: transparent !important;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(59, 161, 255, 0.5);
}

:deep(.el-select__caret) {
  color: #3ba1ff !important;
  font-size: 14px;
}
</style>

<style>
/* 下拉菜单容器 (科技蓝系) */
.tech-select-dropdown-blue.el-popper {
  background: rgba(2, 12, 30, 0.95) !important;
  border: 1px solid rgba(59, 161, 255, 0.3) !important;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.5) !important;
  backdrop-filter: blur(8px);
}

.tech-select-dropdown-blue .el-popper__arrow::before {
  background: rgba(2, 12, 30, 0.95) !important;
  border: 1px solid rgba(59, 161, 255, 0.3) !important;
}

.tech-select-dropdown-blue .el-select-group__title {
  color: #3ba1ff !important;
  font-size: 11px;
  border-bottom: 1px solid rgba(59, 161, 255, 0.2);
  padding: 8px 12px;
  background: rgba(59, 161, 255, 0.05);
  font-weight: bold;
}

.tech-select-dropdown-blue .el-select-dropdown__item {
  color: rgba(255, 255, 255, 0.8) !important;
  padding: 0 12px !important;
  height: 36px;
  line-height: 36px;
  font-size: 13px;
}

.tech-select-dropdown-blue .el-select-dropdown__item.hover,
.tech-select-dropdown-blue .el-select-dropdown__item:hover {
  background-color: rgba(59, 161, 255, 0.15) !important;
  color: #fff !important;
}

.tech-select-dropdown-blue .el-select-dropdown__item.selected {
  color: #3ba1ff !important;
  font-weight: bold;
  background: rgba(59, 161, 255, 0.1) !important;
  text-shadow: 0 0 5px rgba(59, 161, 255, 0.4);
}

.tech-select-dropdown-blue .option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.tech-select-dropdown-blue .option-category {
  font-size: 10px;
  color: rgba(59, 161, 255, 0.7);
  border: 1px solid rgba(59, 161, 255, 0.3);
  border-radius: 2px;
  padding: 0 4px;
  height: 16px;
  line-height: 14px;
}

.tech-select-dropdown-blue .option-name {
  font-size: 13px;
}
</style>