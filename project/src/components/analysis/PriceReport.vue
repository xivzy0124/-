<template>
  <div class="price-report-container">
    <div class="report-header">
      <div class="report-icon"><i class="el-icon-data-analysis"></i></div>
      <div class="report-title">{{ productName }} 市场价格分析简报</div>
    </div>
    <div class="report-content">
      <div class="report-summary" v-html="reportContent"></div>
      <div class="price-indicators">
        <div class="indicator">
          <span class="indicator-label">全国平均价</span>
          <span class="indicator-value">{{ formatPrice(averagePrice) }} 元/公斤</span>
          <span class="indicator-trend" :class="priceTrend.class">
            {{ priceTrend.text }}
          </span>
        </div>
        <div class="indicator">
          <span class="indicator-label">价格最高省份</span>
          <span class="indicator-value">{{ highestProvince.name }}</span>
          <span class="indicator-data">{{ formatPrice(highestProvince.price) }} 元/公斤</span>
        </div>
        <div class="indicator">
          <span class="indicator-label">价格最低省份</span>
          <span class="indicator-value">{{ lowestProvince.name }}</span>
          <span class="indicator-data">{{ formatPrice(lowestProvince.price) }} 元/公斤</span>
        </div>
        <div class="indicator">
          <span class="indicator-label">区域价差</span>
          <span class="indicator-value">{{ formatPrice(priceDifference) }} 元/公斤</span>
          <span class="indicator-data">({{ formatPercent(priceDifferencePercent) }})</span>
        </div>
      </div>
    </div>
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
  },
  year: {
    type: Number,
    default: () => new Date().getFullYear()
  }
})

// Computed values from data
const averagePrice = computed(() => {
  if (!props.provinceData || props.provinceData.length === 0) return 0
  
  const sum = props.provinceData.reduce((acc, province) => acc + province.avgPrice, 0)
  return sum / props.provinceData.length
})

const highestProvince = computed(() => {
  if (!props.provinceData || props.provinceData.length === 0) 
    return { name: '无数据', price: 0 }
  
  const highest = props.provinceData.reduce((prev, current) => 
    (prev.avgPrice > current.avgPrice) ? prev : current)
  
  return { 
    name: highest.provinceName.replace('省', ''), 
    price: highest.avgPrice 
  }
})

const lowestProvince = computed(() => {
  if (!props.provinceData || props.provinceData.length === 0) 
    return { name: '无数据', price: 0 }
  
  const lowest = props.provinceData.reduce((prev, current) => 
    (prev.avgPrice < current.avgPrice) ? prev : current)
  
  return { 
    name: lowest.provinceName.replace('省', ''), 
    price: lowest.avgPrice 
  }
})

const priceDifference = computed(() => {
  return highestProvince.value.price - lowestProvince.value.price
})

const priceDifferencePercent = computed(() => {
  if (lowestProvince.value.price === 0) return 0
  return (priceDifference.value / lowestProvince.value.price) * 100
})

// Mock data for price trend (in a real app, this would come from historical data)
const priceTrend = computed(() => {
  // Simplified logic - in a real app this would compare with historical data
  const trendValue = Math.random() * 10 - 5 // Random value between -5 and 5
  
  if (trendValue > 2) {
    return { text: '↑ 上涨明显', class: 'trend-up' }
  } else if (trendValue < -2) {
    return { text: '↓ 下降明显', class: 'trend-down' }
  } else {
    return { text: '→ 价格平稳', class: 'trend-stable' }
  }
})

// Report templates
const templates = [
  {
    id: 'price-rising',
    condition: () => priceTrend.value.class === 'trend-up',
    template: `<p>${props.year}年<strong>${props.productName}</strong>价格呈<span class="highlight-up">上涨趋势</span>，全国平均价格达到<strong>{{averagePrice}}</strong>元/公斤。</p>
               <p>其中<strong>{{highestProvince.name}}</strong>价格最高，达到<strong>{{highestProvince.price}}</strong>元/公斤；<strong>{{lowestProvince.name}}</strong>价格最低，为<strong>{{lowestProvince.price}}</strong>元/公斤。区域间价格差异为<strong>{{priceDifferencePercent}}%</strong>。</p>
               <p>建议关注价格持续上涨的原因，可能与季节性供应减少、物流成本增加或需求增长有关。</p>`
  },
  {
    id: 'price-falling',
    condition: () => priceTrend.value.class === 'trend-down',
    template: `<p>${props.year}年<strong>${props.productName}</strong>价格呈<span class="highlight-down">下降趋势</span>，全国平均价格为<strong>{{averagePrice}}</strong>元/公斤。</p>
               <p>各省中<strong>{{highestProvince.name}}</strong>价格仍然保持较高水平，为<strong>{{highestProvince.price}}</strong>元/公斤；<strong>{{lowestProvince.name}}</strong>价格降至<strong>{{lowestProvince.price}}</strong>元/公斤，价格差异率为<strong>{{priceDifferencePercent}}%</strong>。</p>
               <p>价格下降可能与市场供应充足、季节性丰收或消费需求疲软有关，建议密切关注市场动态。</p>`
  },
  {
    id: 'price-stable',
    condition: () => priceTrend.value.class === 'trend-stable',
    template: `<p>${props.year}年<strong>${props.productName}</strong>价格总体<span class="highlight-stable">保持稳定</span>，全国平均价格为<strong>{{averagePrice}}</strong>元/公斤。</p>
               <p>价格最高的省份是<strong>{{highestProvince.name}}</strong>，为<strong>{{highestProvince.price}}</strong>元/公斤；最低的是<strong>{{lowestProvince.name}}</strong>，为<strong>{{lowestProvince.price}}</strong>元/公斤。区域价格差异率为<strong>{{priceDifferencePercent}}%</strong>。</p>
               <p>市场供需平衡，价格波动较小，预计短期内价格将保持在当前水平。</p>`
  },
  {
    id: 'high-variance',
    condition: () => priceDifferencePercent.value > 50,
    template: `<p>${props.year}年<strong>${props.productName}</strong>各地区价格<span class="highlight-warning">差异显著</span>，全国均价为<strong>{{averagePrice}}</strong>元/公斤。</p>
               <p><strong>{{highestProvince.name}}</strong>价格高达<strong>{{highestProvince.price}}</strong>元/公斤，而<strong>{{lowestProvince.name}}</strong>仅为<strong>{{lowestProvince.price}}</strong>元/公斤，价格差距达<strong>{{priceDifferencePercent}}%</strong>。</p>
               <p>如此大的价格差异可能与区域供应链差异、地方消费习惯或物流成本有关，建议分析区域市场特点。</p>`
  },
  {
    id: 'default',
    condition: () => true,
    template: `<p>${props.year}年<strong>${props.productName}</strong>全国市场价格分析显示，平均价格为<strong>{{averagePrice}}</strong>元/公斤。</p>
               <p>价格最高的省份是<strong>{{highestProvince.name}}</strong>，为<strong>{{highestProvince.price}}</strong>元/公斤；最低的是<strong>{{lowestProvince.name}}</strong>，为<strong>{{lowestProvince.price}}</strong>元/公斤。</p>
               <p>建议关注价格变动趋势和区域差异，及时调整经营策略。</p>`
  }
]

// Select appropriate template based on data
const selectTemplate = () => {
  for (const template of templates) {
    if (template.condition()) {
      return template.template
    }
  }
  return templates[templates.length - 1].template
}

// Format the selected template with actual data
const formatTemplate = (template) => {
  return template
    .replace(/{{averagePrice}}/g, formatPrice(averagePrice.value))
    .replace(/{{highestProvince.name}}/g, highestProvince.value.name)
    .replace(/{{highestProvince.price}}/g, formatPrice(highestProvince.value.price))
    .replace(/{{lowestProvince.name}}/g, lowestProvince.value.name)
    .replace(/{{lowestProvince.price}}/g, formatPrice(lowestProvince.value.price))
    .replace(/{{priceDifference}}/g, formatPrice(priceDifference.value))
    .replace(/{{priceDifferencePercent}}/g, formatPercent(priceDifferencePercent.value))
}

// Helper functions for formatting
const formatPrice = (price) => {
  return Number(price).toFixed(2)
}

const formatPercent = (percent) => {
  return Number(percent).toFixed(1) + '%'
}

// Final report content
const reportContent = computed(() => {
  const template = selectTemplate()
  return formatTemplate(template)
})
</script>

<style scoped>
.price-report-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 16px;
  margin-bottom: 20px;
}

.report-header {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.report-icon {
  font-size: 20px;
  color: #1e88e5;
  margin-right: 10px;
}

.report-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e88e5;
}

.report-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-summary {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.report-summary p {
  margin-bottom: 8px;
}

.report-summary strong {
  font-weight: 600;
  color: #1e88e5;
}

.highlight-up {
  color: #f56c6c;
  font-weight: 600;
}

.highlight-down {
  color: #67c23a;
  font-weight: 600;
}

.highlight-stable {
  color: #909399;
  font-weight: 600;
}

.highlight-warning {
  color: #e6a23c;
  font-weight: 600;
}

.price-indicators {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 8px;
}

.indicator {
  background-color: #f8f8f8;
  border-radius: 6px;
  padding: 10px 14px;
  min-width: 150px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.indicator-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.indicator-value {
  font-size: 16px;
  font-weight: 600;
  color: #1e88e5;
}

.indicator-data {
  font-size: 14px;
  color: #606266;
  margin-top: 2px;
}

.indicator-trend {
  font-size: 12px;
  margin-top: 4px;
}

.trend-up {
  color: #f56c6c;
}

.trend-down {
  color: #67c23a;
}

.trend-stable {
  color: #909399;
}

@media (max-width: 768px) {
  .price-indicators {
    flex-direction: column;
  }
}
</style> 