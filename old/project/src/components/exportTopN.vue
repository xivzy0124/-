<template>
  <div class="data-item-three">
    <div class="panel-title">
      <span class="title-icon"></span>
      {{queryData.name}}交易量最高排名
    </div>
    
    <el-table
      :data="rank"
      class="custom-table"
      :header-cell-style="{ 
        background: 'linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%)',
        color: 'white',
        borderRadius: '8px'
      }"
      height="calc(100% - 40px)"
    >
      <el-table-column 
        label="批发市场名称" 
        min-width="60%"
      >
        <template #default="scope">
          <div class="rank-item">
            <el-tag
              :class="['rank-badge', `rank-${scope.$index+1}`]"
              effect="dark"
              round
            >
              {{scope.$index+1}}
            </el-tag>
            <span class="market-name">{{scope.row.marketname}}</span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column 
        label="交易量" 
        prop="tradingvolume" 
        min-width="40%"
        align="right"
      />
    </el-table>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted,reactive,ref,watch } from 'vue'
import {exportTop5} from '/src/api/requestFuntion.js'	
import { ElMessage } from 'element-plus'
import { userVegetable} from '/src/store/store.js';

const user = userVegetable()
const map={0:"danger",1:"warning",2:"success"}
const rank = ref()
const disabled = ref(false)
const queryData = reactive({
  name: "大白菜"
})

// 监听单个属性
watch(
  () => user.vegetable,
  (newValue, oldValue) => {
	queryData.name = newValue;
	exportTop5(queryData,"/export/topN").then(resp=>{
		if(resp.data.code=='0'){
			rank.value=resp.data.data
			console.log('*******',resp.data.data)
		}else
			ElMessage.error(resp.data.msg)
	})
  }
);

onMounted(() => {
	exportTop5(queryData,"/export/topN").then(resp=>{
		if(resp.data.code=='0'){
			rank.value=resp.data.data
			console.log(resp.data.data)
		}else
			ElMessage.error(resp.data.msg)
	})  
	disabled.value = true
	setTimeout(() => {
	  disabled.value = false
	}, 1500)
})
</script>

<style scoped>
.data-item-three {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 16px;
}

/* 标题样式统一 */
.panel-title {
  color: #1565c0;
  font-size: 16px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  font-weight: 500;
}

.title-icon {
  display: inline-block;
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%);
  border-radius: 2px;
  margin-right: 8px;
  flex-shrink: 0;
}

/* 表格样式优化 */
.custom-table {
  --el-table-border-color: rgba(30, 136, 229, 0.1);
  --el-table-header-bg-color: #1e88e5;
  --el-table-row-hover-bg-color: rgba(227, 242, 253, 0.3);
  flex: 1;
  overflow: hidden;
}

:deep(.el-table__header) {
  border-radius: 8px 8px 0 0;
}

:deep(.el-table__header th) {
  font-weight: 500;
}

:deep(.el-table__row td) {
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid rgba(30, 136, 229, 0.08);
}

/* 排名标签样式 */
.rank-item {
  display: flex;
  align-items: flex-start;
  padding: 5px 0;
}

.rank-badge {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none !important;
  flex-shrink: 0;
  margin-top: 2px;
}
  
:deep(.rank-badge.rank-1) {
  background: linear-gradient(45deg, #f44336, #ff7961);
}

:deep(.rank-badge.rank-2) {
  background: linear-gradient(45deg, #ff9800, #ffb74d);
}

:deep(.rank-badge.rank-3) {
  background: linear-gradient(45deg, #4caf50, #81c784);
}

:deep(.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3)) {
  background: rgba(30, 136, 229, 0.15);
  color: #1565c0;
}

.market-name {
  margin-left: 12px;
  color: #444;
  font-size: 14px;
  word-break: break-all;
  word-wrap: break-word;
  white-space: normal;
  line-height: 1.4;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .panel-title {
    font-size: 14px;
    margin-bottom: 15px;
  }
  
  .rank-badge {
    width: 20px;
    height: 20px;
    font-size: 12px;
  }
  
  .market-name {
    font-size: 13px;
    margin-left: 8px;
  }
}

@media (max-width: 576px) {
  .panel-title {
    font-size: 13px;
  }
  
  .rank-badge {
    width: 18px;
    height: 18px;
    font-size: 11px;
  }
  
  .market-name {
    font-size: 12px;
    margin-left: 6px;
  }
}
</style>