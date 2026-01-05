<template>
  <div class="side-decor right-panel">
    <div class="panel-title">农产品交易量监测</div>

    <div class="selector-section">
      <div class="selector-label">选择监测品种</div>
      <div class="tech-select-wrapper">
        <el-select
          v-model="selectedProduct"
          placeholder="请选择农产品"
          filterable
          popper-class="tech-select-dropdown-green"
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

    <div class="weather-monitor">
      <slot name="content"></slot>
    </div>

    <div class="v-ruler">
      <span v-for="i in 8" :key="i" class="tick"></span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllVegetableTypes } from '../../api/requestFuntion.js'
import { mapCity } from '../../stores/store.js'

const emit = defineEmits(['product-change'])

const mapCityStore = mapCity()
const productOptions = ref([])
const selectedProduct = ref(mapCityStore.currentProduct)

// 1. 加载数据的逻辑
const loadProductOptions = async () => {
  try {
    const response = await getAllVegetableTypes()
    if (response.data && response.data.data) {
      const groupedData = {}
      response.data.data.forEach((item) => {
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

// 2. 处理选中变化
const handleProductChange = (val) => {
  selectedProduct.value = val
  mapCityStore.setCurrentProduct(val)
  emit('product-change', val)
}

onMounted(() => {
  loadProductOptions()
})
</script>

<style scoped>
/* ==================== 
   布局与容器样式 (绿色风)
   ==================== */
.side-decor {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 22%;
  max-width: 300px;
  height: 85%;
  z-index: 30;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: auto;
}

.right-panel {
  right: 20px;
  /* 绿色边框 */
  border-right: 1px solid rgba(66, 227, 164, 0.3);
  padding-right: 15px;
  text-align: right;
  align-items: flex-end;
  /* 绿色渐变背景 */
  background: linear-gradient(270deg, rgba(66, 227, 164, 0.08), transparent);
}

.panel-title {
  font-size: 18px;
  /* 标题颜色：亮绿色 */
  color: #42e3a4;
  font-weight: bold;
  letter-spacing: 1px;
  text-shadow: 0 0 8px rgba(66, 227, 164, 0.6);
  white-space: nowrap;
  width: 100%;
  text-align: right;
  margin-bottom: 12px;
  border-bottom: 2px solid rgba(66, 227, 164, 0.1);
  padding-bottom: 8px;
}

.selector-section {
  width: 100%;
  margin-bottom: 10px;
}

.selector-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
  letter-spacing: 1px;
  text-align: right;
}

.selector-label::before {
  content: '▼';
  font-size: 10px;
  color: #42e3a4;
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
  /* 绿色虚线分割 */
  background: repeating-linear-gradient(
    90deg,
    rgba(66, 227, 164, 0.4) 0,
    rgba(66, 227, 164, 0.4) 4px,
    transparent 4px,
    transparent 8px
  );
  margin: 10px 0;
}

.weather-monitor {
  flex: 1;
  overflow: hidden;
  width: 100%;
}

.v-ruler {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  opacity: 0.5;
  padding-top: 10px;
  align-items: flex-end;
}

.tick {
  width: 8px;
  height: 2px;
  background: #42e3a4;
  box-shadow: 0 0 5px rgba(66, 227, 164, 0.5);
}

/* ==================== 
   输入框样式强力覆盖 
   ==================== */
:deep(.el-input__wrapper) {
  background-color: transparent !important;
  border: 1px solid rgba(66, 227, 164, 0.3) !important;
  box-shadow: none !important;
  border-radius: 2px;
  padding: 6px 12px !important;
  transition: all 0.3s ease;
}

/* 鼠标悬停时的输入框 */
:deep(.el-input__wrapper:hover) {
  border-color: rgba(66, 227, 164, 0.5) !important;
  box-shadow: 0 0 8px rgba(66, 227, 164, 0.15) !important;
}

/* 选中/聚焦时的输入框 */
:deep(.el-input__wrapper.is-focus) {
  border-color: rgba(66, 227, 164, 0.7) !important;
  box-shadow: 0 0 12px rgba(66, 227, 164, 0.2) !important;
}

/* 输入文字样式 */
:deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9) !important;
  font-weight: 500;
  font-size: 14px;
  text-align: right;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 占位符颜色 */
:deep(.el-input__inner::placeholder) {
  color: rgba(66, 227, 164, 0.4);
}

/* 右侧箭头图标 */
:deep(.el-select__caret) {
  color: rgba(66, 227, 164, 0.7) !important;
  font-size: 14px;
}
</style>

<style>
/* 下拉菜单容器 */
.tech-select-dropdown-green.el-popper {
  background: rgba(4, 20, 15, 0.9) !important;
  border: 1px solid rgba(66, 227, 164, 0.3) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3) !important;
  backdrop-filter: blur(8px);
}

/* 隐藏原本的小三角箭头 */
.tech-select-dropdown-green .el-popper__arrow::before {
  background: rgba(4, 20, 15, 0.9) !important;
  border: 1px solid rgba(66, 227, 164, 0.3) !important;
}

/* 分组标题 */
.tech-select-dropdown-green .el-select-group__title {
  color: rgba(66, 227, 164, 0.8) !important;
  font-size: 11px;
  border-bottom: 1px solid rgba(66, 227, 164, 0.15);
  padding: 8px 12px;
  background: rgba(66, 227, 164, 0.05);
  font-weight: 500;
}

/* 选项样式 */
.tech-select-dropdown-green .el-select-dropdown__item {
  color: rgba(255, 255, 255, 0.85) !important;
  padding: 0 12px !important;
  height: 36px;
  line-height: 36px;
  font-size: 13px;
}

/* 选项悬停 */
.tech-select-dropdown-green .el-select-dropdown__item.hover,
.tech-select-dropdown-green .el-select-dropdown__item:hover {
  background-color: rgba(66, 227, 164, 0.12) !important;
}

/* 选中状态 */
.tech-select-dropdown-green .el-select-dropdown__item.selected {
  color: #42e3a4 !important;
  font-weight: 500;
  background: rgba(66, 227, 164, 0.1) !important;
}

/* 自定义选项内部布局 */
.tech-select-dropdown-green .option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

/* 选项左侧的小标签 */
.tech-select-dropdown-green .option-category {
  font-size: 10px;
  color: rgba(66, 227, 164, 0.7);
  border: 1px solid rgba(66, 227, 164, 0.3);
  border-radius: 2px;
  padding: 0 4px;
  height: 16px;
  line-height: 14px;
}

.tech-select-dropdown-green .option-name {
  font-size: 13px;
}
</style>
