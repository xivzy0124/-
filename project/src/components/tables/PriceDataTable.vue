<template>
  <div class="price-table-container">
    <div class="table-header">
      <div class="header-title">{{ productName }} 价格数据表</div>
      <div class="header-controls">
        <el-button-group>
          <el-button 
            size="small" 
            :type="sortBy === 'provinceName' ? 'primary' : 'default'"
            @click="setSorting('provinceName')">
            按省份
          </el-button>
          <el-button 
            size="small" 
            :type="sortBy === 'avgPrice' ? 'primary' : 'default'"
            @click="setSorting('avgPrice')">
            平均价格
          </el-button>
          <el-button 
            size="small" 
            :type="sortBy === 'maxPrice' ? 'primary' : 'default'"
            @click="setSorting('maxPrice')">
            最高价格
          </el-button>
          <el-button 
            size="small" 
            :type="sortBy === 'minPrice' ? 'primary' : 'default'"
            @click="setSorting('minPrice')">
            最低价格
          </el-button>
        </el-button-group>

        <el-switch
          v-model="sortDirection"
          active-text="升序"
          inactive-text="降序"
          active-value="asc"
          inactive-value="desc"
        />
      </div>
    </div>

    <el-table
      :data="sortedData"
      stripe
      border
      size="small"
      highlight-current-row
      @row-click="handleRowClick"
      :max-height="400"
      class="price-table"
    >
      <el-table-column
        prop="provinceName"
        label="省份"
        width="120"
      >
        <template #default="scope">
          {{ scope.row.provinceName ? scope.row.provinceName.replace('省', '') : '' }}
        </template>
      </el-table-column>
      <el-table-column
        prop="avgPrice"
        label="平均价格(元/公斤)"
        width="150"
      >
        <template #default="scope">
          <span>{{ scope.row.avgPrice ? formatPrice(scope.row.avgPrice) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="maxPrice"
        label="最高价格(元/公斤)"
        width="150"
      >
        <template #default="scope">
          <span>{{ scope.row.maxPrice ? formatPrice(scope.row.maxPrice) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="minPrice"
        label="最低价格(元/公斤)"
        width="150"
      >
        <template #default="scope">
          <span>{{ scope.row.minPrice ? formatPrice(scope.row.minPrice) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="价格区间"
        min-width="180"
      >
        <template #default="scope">
          <div class="price-range">
            <div class="price-bar">
              <div 
                class="price-bar-fill" 
                :style="{ 
                  left: `${getBarPosition(scope.row.minPrice, scope.row)}%`,
                  width: `${getBarWidth(scope.row.minPrice, scope.row.maxPrice, scope.row)}%` 
                }"
              ></div>
              <el-tooltip content="均价" placement="top" :enterable="false">
                <div 
                  class="price-bar-avg" 
                  :style="{ left: `${getBarPosition(scope.row.avgPrice, scope.row)}%` }"
                ></div>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  productName: {
    type: String,
    default: '大白菜'
  },
  provinceData: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['province-selected'])

// State
const sortBy = ref('avgPrice')
const sortDirection = ref('desc')
const selectedProvince = ref(null)

// Compute min and max across all data for the price bar
const dataExtremes = computed(() => {
  if (!props.provinceData || props.provinceData.length === 0) {
    return { min: 0, max: 0 }
  }
  
  let min = Infinity
  let max = -Infinity
  
  props.provinceData.forEach(province => {
    if (province.minPrice < min) min = province.minPrice
    if (province.maxPrice > max) max = province.maxPrice
  })
  
  return { min, max }
})

// Helper functions
const formatPrice = (price) => {
  return Number(price).toFixed(2)
}

// Visually represent the price range in the bar
const getBarPosition = (price, row) => {
  const { min, max } = dataExtremes.value
  const range = max - min
  if (range === 0) return 0
  
  return ((price - min) / range) * 100
}

const getBarWidth = (minPrice, maxPrice, row) => {
  const { min, max } = dataExtremes.value
  const range = max - min
  if (range === 0) return 0
  
  return ((maxPrice - minPrice) / range) * 100
}

// Sort the province data
const sortedData = computed(() => {
  if (!props.provinceData) return []
  
  return [...props.provinceData].sort((a, b) => {
    let comparison = 0
    
    // For string properties (province name)
    if (sortBy.value === 'provinceName') {
      const aName = a.provinceName.replace('省', '')
      const bName = b.provinceName.replace('省', '')
      comparison = aName.localeCompare(bName)
    } else {
      // For numeric properties
      comparison = a[sortBy.value] - b[sortBy.value]
    }
    
    // Apply sort direction
    return sortDirection.value === 'asc' ? comparison : -comparison
  })
})

// Event handlers
const setSorting = (column) => {
  if (sortBy.value === column) {
    // Toggle direction if clicking the same column
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    // Set new column and default to descending for prices, ascending for names
    sortBy.value = column
    sortDirection.value = column === 'provinceName' ? 'asc' : 'desc'
  }
}

const handleRowClick = (row) => {
  selectedProvince.value = row
  emit('province-selected', row)
}
</script>

<style scoped>
.price-table-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 16px;
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a7c6c;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.price-table {
  width: 100%;
}

.price-range {
  height: 20px;
  display: flex;
  align-items: center;
}

.price-bar {
  width: 100%;
  height: 6px;
  background-color: #f0f0f0;
  border-radius: 3px;
  position: relative;
}

.price-bar-fill {
  height: 100%;
  background-color: #2db4a0;
  border-radius: 3px;
  position: absolute;
  top: 0;
}

.price-bar-avg {
  position: absolute;
  top: -4px;
  width: 3px;
  height: 14px;
  background-color: #f56c6c;
  border-radius: 2px;
  transform: translateX(-50%);
}

@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-controls {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style> 