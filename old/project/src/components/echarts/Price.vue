<template>
  <el-card class="chart-card" :body-style="{ padding: '20px' }">
    <template #header>
      <div class="card-header">
        <div class="chart-title">
          <!-- 标题部分 -->
          <div class="title-left">
            <span class="title-text">全国{{queryData.name}}价格波动趋势图</span>
            <div class="title-decoration-line"></div>
          </div>
          
          <el-select 
            v-model="queryData.name"
            placeholder="请选择农产品" 
            filterable 
            class="vegetable-select"
            @change="user.vegetable=queryData.name;
            changeEcharts()"
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
                <span>{{ item.twoLevel }} - {{ item.varietyname }}</span>
              </el-option>
            </el-option-group>
          </el-select>
        </div>
      </div>
    </template>
    <div id="price" ref="chartContainer" class="chart-container"></div>
  </el-card>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, reactive, ref, onBeforeUnmount } from 'vue'
import axios from "axios"
import { selectDayByName, selectKind } from '/src/api/requestFuntion.js'	
import { ElMessage } from 'element-plus'
import { userVegetable } from '/src/store/store.js';

const user = userVegetable()
let myChart = null
const productOptions = ref([])
const chartContainer = ref(null)

// 加载蔬菜分类数据
const loadProductOptions = async () => {
    try {
        const response = await axios.get('http://47.117.34.210:8080/vegetable/type2/all')
        if (response.data && response.data.data) {
            // 重组数据结构为 el-select 所需的分组格式
            const groupedData = {}
            
            response.data.data.forEach(item => {
                if (!groupedData[item.oneLevel]) {
                    groupedData[item.oneLevel] = {
                        oneLevel: item.oneLevel,
                        items: []
                    }
                }
                
                groupedData[item.oneLevel].items.push(item)
            })
            
            productOptions.value = Object.values(groupedData)
        }
    } catch (error) {
        console.error('获取农产品分类数据失败：', error)
    }
}

const queryData = reactive({
  name: "大白菜"
})


const option = reactive({
  // 背景透明适配父级
  backgroundColor: 'transparent',
  // 颜色系统统一 - 改为蓝色渐变风格
  color: ['#3137fd', '#37b4fd', '#43fce0'],
  grid: {
    left: '3%',
    right: '4%',
    bottom: '16%', // 为dataZoom留出空间
    top: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'time',
    boundaryGap: false,
    axisLabel: {
      formatter: '{yyyy}-{MM}-{dd}', // 显示月-日 时:分
      interval: 15,
      color: '#5a7a9b'
    },
    axisLine: {
      lineStyle: {
        color: '#8cd7c7'
      }
    },
    axisTick: {
      lineStyle: {
        color: '#cce6ff'
      }
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    name: '元/公斤',
    nameTextStyle: {
      color: '#5a9b8f',
      fontSize: 12
    },
    axisLabel: {
      color: '#5a7a9b'
    },
    splitLine: {
      lineStyle: {
        color: ['#e6f2fa'],
        type: 'dashed'
      }
    }
  },
  legend: {
    orient: 'horizontal',
    top: '2%',
    right: '5%',
    data: [queryData.name],
    textStyle: {
      color: '#1565c0',
      fontWeight: 'bold'
    },
    itemStyle: {
      color: '#1e88e5'
    }
  },
  //工具项
  toolbox: {
    right: 20,
    top: 10,
    feature: {
      saveAsImage: {
        title: '保存为图片',
        pixelRatio: 2
      }
    }
  },	
  //提示框
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(30,136,229,0.9)',
    borderColor: '#1e88e5',
    textStyle: {
      color: '#fff'
    },
    axisPointer: {
      type: 'line',
      lineStyle: {
        color: '#1e88e5',
        width: 1,
        type: 'dashed'
      }
    },
    formatter: (params) => {
      const data = params[0]; // 获取第一个系列的数据
      return `
        <div style="text-align: left; font-size: 14px;">
          <div style="font-weight: bold; margin-bottom: 5px;">${data.data[0]}</div>
          ${data.marker} ${data.seriesName}: <span style="font-weight: bold;">${data.data[1]} 元/公斤</span>
        </div>
      `;
    }
  },
  //缩放配置
  dataZoom: [
    {
      type: 'slider',
      start: 30,
      end: 70,
      xAxisIndex: 0,
      backgroundColor: 'rgba(233,245,253,0.5)',
      fillerColor: 'rgba(30,136,229,0.2)',
      borderColor: '#cce6ff',
      textStyle: {
        color: '#5a7a9b'
      }
    }
  ],
  series: [
    {
      name: '',
      data: [['2008-04-16', 2], ['2008-04-23', 1.6]],
      type: 'line',
      smooth: true,
      showSymbol: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 3,
        shadowColor: 'rgba(49,55,253,0.3)',
        shadowBlur: 10,
        shadowOffsetY: 5
      },
      itemStyle: {
        color: '#3137fd',
        borderColor: '#fff',
        borderWidth: 1
      },
      // 优化区域渐变
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(49,55,253,0.4)' },
            { offset: 0.7, color: 'rgba(55,180,253,0.2)' },
            { offset: 1, color: 'rgba(228,247,221,0.05)' }
          ]
        }
      }
    }
  ]
})

const changeEcharts = ()=>{
	selectDayByName(queryData,"/user/day").then(resp=>{
		if(resp.data.code=='0'){
			console.log(resp.data.data)
			option.series[0].data=resp.data.data.map(e=>[e.reporttime,e.middleprice])
			option.legend.data=[queryData.name]
			option.series[0].name = queryData.name
			if (myChart!=null){
				  myChart.dispose()
				  myChart = null
				  getEcharts()
			}
		}else
			ElMessage.error(resp.data.msg)
	})
}

const getEcharts = () => {
  if (!chartContainer.value) return
  var chartDom = chartContainer.value
  myChart = echarts.init(chartDom)

  option && myChart.setOption(option)
}

// 图表尺寸调整
const resizeHandler = () => {
  if (myChart) {
    myChart.resize()
  }
}

onMounted(() => {
  changeEcharts()
  selectKind({}, '/vegetable/type2/all').then(resp => {
    // 重组数据结构为 el-select 所需的分组格式
    const groupedData = {}
    if (resp.data.code == '0') {
      resp.data.data.forEach(item => {
        if (!groupedData[item.oneLevel]) {
          groupedData[item.oneLevel] = {
            oneLevel: item.oneLevel,
            items: []
          }
        }
        
        groupedData[item.oneLevel].items.push(item)
      })
      productOptions.value = Object.values(groupedData)
    } else {
      ElMessage.error(resp.data.msg)
    }
  })
  
  // 初始化图表
  setTimeout(() => {
    getEcharts()
  }, 300)
  
  // 添加resize事件监听
  window.addEventListener('resize', resizeHandler)
})

onBeforeUnmount(() => {
  // 移除resize监听
  window.removeEventListener('resize', resizeHandler)
  
  // 销毁图表实例
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
})
</script>

<style scoped>
/* 整体容器 */
.chart-card {
  background: linear-gradient(135deg, #f5f9ff 0%, #e6f2ff 100%) !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 20px rgba(30, 136, 229, 0.15) !important;
  border: 1px solid #e1efff;
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 标题和卡片头部 */
.card-header {
  width: 100%;
  background: linear-gradient(90deg, rgba(233,245,253,0.8), rgba(220,240,255,0.6));
  padding: 5px 0;
}

.chart-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  position: relative;
  flex-wrap: wrap;
  gap: 15px;
}

.title-left {
  position: relative;
  flex: 1;
  min-width: 200px;
  padding-bottom: 12px;
  display: flex;
  flex-direction: column;
}

.title-text {
  font-size: 20px;
  font-weight: 600;
  color: #1565c0;
  letter-spacing: 1px;
  display: inline-block;
  padding: 0 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.title-decoration-line {
  position: absolute;
  bottom: 0;
  left: 14%;
  width: 70%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #1e88e5, transparent);
  opacity: 0.8;
}

/* 选择器样式 */
.vegetable-select {
  width: 220px;
  margin-left: 10px;
  align-self: flex-start;
}

:deep(.vegetable-select .el-input__inner) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #cce6ff;
  border-radius: 6px;
  color: #1565c0;
  font-weight: 500;
  box-shadow: 0 2px 5px rgba(30, 136, 229, 0.1);
}

:deep(.vegetable-select .el-select__caret) {
  color: #1e88e5;
}

:deep(.vegetable-select:hover .el-input__inner) {
  border-color: #1e88e5;
}

/* 图表容器 */
.chart-container {
  flex: 1;
  width: 100%;
  min-height: 350px;
  padding: 10px 0;
}

/* 卡片头部样式重写 */
:deep(.el-card__header) {
  border-bottom: 1px solid rgba(204, 230, 255, 0.8) !important;
  padding: 15px 20px !important;
  background: linear-gradient(
    90deg,
    rgba(233, 245, 253, 0.8),
    rgba(220, 240, 255, 0.6)
  ) !important;
}

:deep(.el-card__body) {
  flex: 1;
  padding: 15px !important;
  display: flex;
  overflow: hidden;
}

/* ECharts 视觉优化 */
:deep(.echarts-tooltip) {
  border-radius: 8px !important;
  box-shadow: 0 4px 15px rgba(30, 136, 229, 0.2) !important;
  border: 1px solid #cce6ff !important;
  backdrop-filter: blur(4px);
}

:deep(.dataZoom-slider) {
  background: rgba(233, 245, 253, 0.6) !important;
  border: 1px solid #cce6ff !important;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .chart-title {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .title-left {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .vegetable-select {
    margin-left: 20px;
    margin-bottom: 10px;
  }
}

@media (max-width: 768px) {
  .title-text {
    font-size: 18px;
    padding: 0 15px;
  }
  
  .vegetable-select {
    width: 180px;
  }
  
  :deep(.el-card__header) {
    padding: 12px 15px !important;
  }
  
  :deep(.el-card__body) {
    padding: 10px !important;
  }
}

@media (max-width: 576px) {
  .title-text {
    font-size: 16px;
    padding: 0 10px;
  }
  
  .vegetable-select {
    width: 160px;
    margin-left: 10px;
  }
}
</style>