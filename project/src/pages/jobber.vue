<template>
  <div class="content-section">
	<Head />

	<div class="trend-section">
	  <div class="chart-card layered-card">
	    <div class="chart-header">
	      <span class="chart-title">{{province}}农产品流通分析</span>
	      <el-select 
	        v-model="province"
	        size="small"
	        style="width: 120px"
	        popper-class="blue-select"
			@change="jobber.province = province"
	      >
	        <el-option
	            v-for="item in provinceOptions"
	            :key="item"
	            :label="item"
	            :value="item"
	          />
	      </el-select>
	    </div>
		<Calendar 
		:class="calendar-container"
		 :province="province" 
		 @vegetable-update="handleVegetableUpdate"></Calendar>
	  </div>
	</div>

    <!-- 主分析区 -->
    <el-row :gutter="16" class="main-analysis">
      <el-col :xs="24" :md="12">
        <div class="chart-card layered-card">
         <div class="chart-header">
            <span class="chart-title">{{jobber.vegetable}}批发市场对比</span>
          </div>
          <Radar class="chart-container" />
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="chart-card layered-card">
          <div class="chart-header">
            <span class="chart-title">{{jobber.vegetable}}市场均价与方差关联分析</span>
          </div>
          <Test class="chart-container"/>
        </div>
      </el-col>
    </el-row>

  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import { Crop, Coin, Clock } from '@element-plus/icons-vue'
import Quarter from '/src/components/echarts/quarter.vue'
import Calendar from '/src/components/echarts/calendar.vue'
import Radar from '/src/components/echarts/radar.vue'
import Test from '/src/components/echarts/test.vue'
import Head from '/src/components/head.vue'
import {getProvince} from '/src/api/requestFuntion.js'
import { definedPerson} from '/src/store/store.js';
import { ElMessage } from 'element-plus'

let jobber= definedPerson()
const province = ref('安徽省')
const provinceOptions =ref([])

const handleVegetableUpdate = (vegetables) => {
	console.log("******",vegetables)
	jobber.vegetable = vegetables[5]
};


// 数据卡片
const cards = ref([
  { icon: Crop, label: '当前种植面积', value: '8,642 公顷', color: '#1e88e5' },
  { icon: Coin, label: '季度总产值', value: '¥2.36 亿', color: '#1565c0' }
])

const cardStyle = (index) => ({
  '--card-color': cards.value[index].color,
  '--card-rgb': hexToRgb(cards.value[index].color)
})

const hexToRgb = (hex) => {
  const r = parseInt(hex.slice(1,3), 16)
  const g = parseInt(hex.slice(3,5), 16)
  const b = parseInt(hex.slice(5,7), 16)
  return `${r},${g},${b}`
}


onMounted(() => {
	getProvince({},"/user/getProvince").then(resp=>{
		if(resp.data.code=='0'){
			provinceOptions.value = resp.data.data
		}else
			ElMessage.error(resp.data.msg)
	})
})
</script>

<style scoped>
.content-section {
  background-color: #f5f7fa;
  padding: 20px;
  min-height: 100vh;
}

/*雷达图样式*/
.chart-container {
  height: calc(100% - 60px);
  margin-top: -16px;

  :deep(.radar-chart) {
    .radar-axis-line {
      stroke: #e4e7ed !important;
    }
    
    .radar-series-polygon {
      fill: rgba(30, 136, 229, 0.1) !important;
      stroke: #1e88e5 !important;
    }

    .radar-axis-text {
      fill: #666;
      font-size: 12px;
    }

    .echarts-tooltip {
      background: rgba(255,255,255,0.95) !important;
      border: 1px solid #e3f2fd !important;
      border-radius: 8px !important;
      box-shadow: 0 4px 12px rgba(30, 136, 229, 0.15) !important;
      
      &-title {
        color: #1565c0 !important;
        font-weight: 600;
      }
    }
  }
}
	
.calendar-container {
  margin-top: -16px;
  padding: 16px;
  height: calc(100% - 60px); /* 留出标题空间 */
  

  :deep(.series path) {
    opacity: 0.6;
    transition: opacity 0.3s;
    
    &:hover {
      opacity: 1;
    }
  }
  

  :deep(.echarts-tooltip) {
    background: rgba(255,255,255,0.95) !important;
    border-radius: 8px !important;
    box-shadow: 0 4px 12px rgba(30, 136, 229, 0.15) !important;
    border: 1px solid #e3f2fd;
  }
}
	

.fullscreen-calendar {
  height: calc(100% - 72px); /* 72px为header高度 */
  margin-top: -16px;
}

@media (max-width: 768px) {
  .trend-section .layered-card {
    min-height: 400px;
  }
  
  .fullscreen-calendar {
    height: calc(100% - 88px);
  }
}
	
	
.layered-card .chart-container {
  margin-top: -20px; /* 调整间距 */
  z-index: 1; /* 确保悬浮效果 */
}
	
	
	
.card-row, .chart-group, .main-analysis, .trend-section {
	margin: 12px 0;
}
/* 数据卡片 */
.data-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e3f2fd;
  transition: transform 0.3s ease;
}

.data-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 48px;
  height: 48px;
  background: rgba(var(--card-rgb), 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon svg {
  width: 24px;
  height: 24px;
  color: var(--card-color);
}

.card-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--card-color);
  line-height: 1.4;
}

.card-label {
  color: #666;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.card-wave {
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 24px;
  background: linear-gradient(
    90deg,
    rgba(var(--card-rgb), 0.15) 0%,
    rgba(var(--card-rgb), 0.05) 100%
  );
  clip-path: polygon(0 100%, 100% 0, 100% 100%);
}

/* 图表通用样式 */
.chart-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e3f2fd;
  transition: transform 0.3s ease;
}

.chart-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.layered-card {
  position: relative;
  overflow: hidden;
}

.layered-card::after {
  content: '';
  position: absolute;
  bottom: -30px;
  right: -30px;
  width: 80px;
  height: 80px;
  background: rgba(30, 136, 229, 0.05);
  border-radius: 50%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  color: #1565c0;
  font-size: 18px;
  font-weight: 600;
  position: relative;
  padding-left: 12px;
}

.chart-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%);
  border-radius: 2px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .content-section {
    padding: 15px;
  }
  
  .card-row .el-col {
    margin-bottom: 12px;
  }
  
  .chart-card {
    padding: 16px;
    margin-bottom: 16px;
  }
  
  .chart-title {
    font-size: 16px;
  }
}

/* 自定义下拉菜单样式 */
:deep(.blue-select) .el-select-dropdown__item {
  color: #333;
}

:deep(.blue-select) .el-select-dropdown__item.hover,
:deep(.blue-select) .el-select-dropdown__item:hover {
  background: rgba(30, 136, 229, 0.1);
}

:deep(.blue-select) .el-select-dropdown__item.selected {
  color: #1e88e5;
  background: rgba(30, 136, 229, 0.1);
}

/* 自定义Element UI组件样式 */
:deep(.el-select .el-input__inner) {
  border-radius: 6px;
  border: 1px solid #e3f2fd;
  height: 32px;
  line-height: 32px;
}

:deep(.el-select .el-input__inner:focus) {
  border-color: #1e88e5;
  box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.2);
}

:deep(.el-select-dropdown) {
  border-radius: 6px;
  border: 1px solid #e3f2fd;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>