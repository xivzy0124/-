<template>
  <Head />
  <div class="dashboard-container">
    
   <!-- 下方布局 -->
   <div class="content-section">
     <!-- 顶部指标行 -->
     <el-row :gutter="20" class="metric-row">
       
       <el-col :xs="24" :sm="12" :md="6">
         <div class="metric-card">
           <div class="metric-icon">
             <el-icon :size="28"><Location /></el-icon>
           </div>
           <div class="metric-content">
             <div class="metric-value">{{provincenameCt}}</div>
             <div class="metric-label">省级</div>
             
           </div>
         </div>
       </el-col>
       
       <el-col :xs="24" :sm="12" :md="6">
         <div class="metric-card">
           <div class="metric-icon">
             <el-icon :size="28"><Shop /></el-icon>
           </div>
           <div class="metric-content">
             <div class="metric-value">{{areacodeCt}}</div>
             <div class="metric-label">批发市场</div>

           </div>
         </div>
       </el-col>
       
       <el-col :xs="24" :sm="12" :md="6">
         <div class="metric-card">
           <div class="metric-icon">
             <!-- <el-icon :size="28"><Crop /></el-icon> -->
              <img class="maishui-1" src="/public/麦穗一号.png" alt="麦穗" :size="28">
           </div>
           <div class="metric-content">
             <div class="metric-value">{{varietynameCt}}</div>
             <div class="metric-label">农产品种类</div>

           </div>
         </div>
       </el-col>
       
       <el-col :xs="24" :sm="12" :md="6">
         <div class="metric-card">
           <div class="metric-icon">
             <el-icon :size="28"><DataLine /></el-icon>
           </div>
           <div class="metric-content">
             <div class="metric-value">{{Math.trunc(total/100000000)}}<span style="font-size: 16px;">亿</span>{{Math.trunc(total%100000000/10000)}}<span style="font-size: 16px;">万</span></div>
             <div class="metric-label">信息数</div>

           </div>
         </div>
       </el-col>
     </el-row>
        
     <!-- 分割线 -->
     <div class="section-divider"></div>
        
     <!-- 主内容区 -->
     <el-row :gutter="20" class="main-content-row">
       <!-- 主图表区 -->
       <el-col :xs="24" :md="17" class="chart-col">
         <div class="main-chart" ref="chartRef">
           <Price></Price>
         </div>
       </el-col>
        
       <!-- 侧边栏 -->
       <el-col :xs="24" :md="7" class="side-col">
         <div class="side-panel" :style="sideStyle">
           <TopN/>
         </div>
       </el-col>
     </el-row>
   </div>
 </div>
</template>

<script setup>
import Price from '/src/components/echarts/Price.vue'
import TopN from '/src/components/exportTopN.vue'
import Head from '/src/components/head.vue'
import { onMounted, reactive, ref, onUnmounted, nextTick } from 'vue'
import axios from "axios"
import {getTitle} from '/src/api/requestFuntion.js'  
import * as echarts from 'echarts'
import { Location, Shop, Goods, DataLine, Crop } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

//当前主页展示蔬菜
const name = ref('大白菜')
//当前主页指标
//省
const provincenameCt =ref()
//市
const areacodeCt =ref()
//蔬菜种类
const varietynameCt =ref()
//信息条数
const total =ref()

// 添加窗口大小变化监听
const resizeObserver = ref(null)



onMounted(() => {
  getTitle({},"/user/getTitle").then(resp=>{
    if(resp.data.code=='0'){
      provincenameCt.value = resp.data.data[0].provincenameCt
      areacodeCt.value = resp.data.data[0].areacodeCt
      varietynameCt.value = resp.data.data[0].varietynameCt
      total.value = resp.data.data[0].total
      console.log("total",total.value)
      
      // 初始化小图表
      nextTick(() => {
        initMiniCharts()
      })
    }else
      ElMessage.error(resp.data.msg)
  })
  
})

// 监听窗口大小变化
const handleResize = () => {
  const charts = [provinceChart, marketChart, varietyChart, totalChart]
  charts.forEach(chart => {
    if (chart.value && chart.value.resize) {
      chart.value.resize()
    }
  })
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

</script>

<style scoped>
  .dashboard-container {
    background: white; /* 修改为白色背景 */
    min-height: 100vh;
    padding: 20px;
    display: flex;
    flex-direction: column;
  }
  

  .maishui-1 {
    width: 55px;
    height: 55px;
    object-fit: contain;
    filter: brightness(0) invert(1);
  }
  
  .content-section {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  
  .main-content-row {
    flex: 1;
    display: flex;
    margin-bottom: 20px;
    width: 100%;
  }
  
  .chart-col, .side-col {
    display: flex;
    flex-direction: column;
  }
  
  /* 指标卡片样式 - 保持蓝色背景 */
  .metric-row {
    margin-bottom: 20px;
  }
  
  .metric-card {
    background: linear-gradient(135deg, #1e88e5 100%); /* 卡片保持蓝色渐变 */
    backdrop-filter: blur(10px);
    border-radius: 16px;
    padding: 24px;
    margin-bottom: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: flex-start;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    height: 60%;
    position: relative;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.18);
  }
  
  .metric-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    background: linear-gradient(135deg, #2196f3 0%, #1565c0 100%);
  }
  
  .metric-icon {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 14px;
    padding: 14px;
    margin-right: 18px;
    color: white;
    flex-shrink: 0;
    transition: all 0.4s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
  }
  
  .metric-card:hover .metric-icon {
    transform: scale(1.1) rotate(5deg);
    background: rgba(255, 255, 255, 0.3);
    color: white;
  }
  
  .metric-card:hover .maishui-1 {
    filter: brightness(0) invert(1);
  }
  
  .metric-content {
    flex: 1;
    position: relative;
    z-index: 1;
  }
  
  .metric-value {
    font-size: 32px;
    font-weight: 700;
    color: white;
    line-height: 1.2;
    margin-bottom: 6px;
    letter-spacing: -0.5px;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
  }
  
  .metric-label {
    color: rgba(255, 255, 255, 0.9);
    font-size: 15px;
    font-weight: 500;
    margin-bottom: 10px;
  }
  
  .metric-chart {
    height: 45px;
    width: 100%;
    margin-top: 10px;
    opacity: 0.8;
  }
  
  /* 分割线样式 */
  .section-divider {
    height: 2px;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(0, 0, 0, 0.1), /* 改为深色以适应白色背景 */
      transparent
    );
    margin: 30px 0 40px;
  }
  
  /* 主图表区 */
  .main-chart {
    height: 520px;
    width: 100%;
    padding: 0;
    background: none !important;
    box-shadow: none !important;
    display: flex;
  }
  
  /* 侧边栏样式 */
  .side-panel {
    background: white; /* 侧边栏改为白色背景 */
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); /* 调整阴影颜色 */
    height: 100%;
    display: flex;
    flex-direction: column;
    transition: all 0.4s ease;
    border: 1px solid rgba(0, 0, 0, 0.08); /* 调整边框颜色 */
  }
  
  /* TopN组件样式修改 */
  :deep(.data-item-three) {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  
  :deep(.panel-title) {
    color: #303133; /* 改为深色以适应白色背景 */
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 24px;
    display: flex;
    align-items: center;
  }
  
  :deep(.title-icon) {
    display: inline-block;
    width: 4px;
    height: 18px;
    background: linear-gradient(to bottom, #64b5f6, #42a5f5);
    border-radius: 2px;
    margin-right: 10px;
  }
  
  :deep(.custom-table) {
    flex: 1;
    --el-table-border-color: rgba(0, 0, 0, 0.06); /* 调整边框颜色 */
    --el-table-header-bg-color: #f5f7fa; /* 调整表头背景色 */
    --el-table-row-hover-bg-color: #f5f7fa;
  }
  
  :deep(.el-table__header) {
    border-radius: 12px 12px 0 0;
  }
  
  :deep(.el-table__header th) {
    font-weight: 600;
    color: #606266; /* 调整表头文字颜色 */
    letter-spacing: 0.5px;
    background-color: #f5f7fa;
  }
  
  :deep(.el-table__row td) {
    background: white;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06); /* 调整边框颜色 */
  }
  
  :deep(.rank-badge) {
    width: 28px;
    height: 28px;
    border: none !important;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
  }
  
  :deep(.rank-badge.rank-1) {
    background: linear-gradient(45deg, #ff6b6b, #ff8787);
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
  }
  
  :deep(.rank-badge.rank-2) {
    background: linear-gradient(45deg, #ffd93d, #ffdf6b);
    box-shadow: 0 4px 12px rgba(255, 217, 61, 0.3);
  }
  
  :deep(.rank-badge.rank-3) {
    background: linear-gradient(45deg, #69db7c, #8ce99a);
    box-shadow: 0 4px 12px rgba(105, 219, 124, 0.3);
  }
  
  :deep(.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3)) {
    background: rgba(30, 136, 229, 0.3);
    color: #303133; /* 调整文字颜色 */
  }
  
  :deep(.market-name) {
    margin-left: 12px;
    color: #2d3748;
    font-size: 14px;
    font-weight: 500;
  }
  
  /* 进度条颜色适配 */
  :deep(.el-progress-bar__inner) {
    background: linear-gradient(90deg, #64b5f6, #42a5f5) !important;
  }
  
  /* 响应式调整 */
  @media (max-width: 992px) {
    .main-chart {
      height: 400px;
    }
    
    .side-panel {
      margin-top: 0;
      height: 400px;
    }
  }
  
  @media (max-width: 768px) {
    .dashboard-container {
      padding: 15px;
    }
    
    .main-content-row {
      flex-direction: column;
    }
    
    .chart-col, .side-col {
      width: 100%;
    }
    
    .main-chart {
      height: 350px;
    }
    
    .side-panel {
      margin-top: 20px;
      height: 350px;
    }
    
    .metric-card {
      padding: 20px;
    }
    
    .metric-value {
      font-size: 24px;
    }
    
    .metric-label {
      font-size: 13px;
    }
    
    :deep(.rank-badge) {
      width: 24px;
      height: 24px;
      font-size: 12px;
    }
    
    :deep(.market-name) {
      font-size: 13px;
    }
  }
</style>