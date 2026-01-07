<template>
  <div class="trend-section">
    <div class="chart-card layered-card">
      <div class="chart-header">
        <div class="main-chart-title">
          <div class="title-content">
            <div class="title-decor"></div>
            <span class="title-text">全国{{productName}}价格趋势</span>
          </div>
        </div>
      </div>
      <div id="quarter" ref="chartDom" class="chart-container"></div>
    </div>
  </div>
</template>


<script setup>
import * as echarts from 'echarts'
import { onMounted, watch, defineEmits, ref, onBeforeUnmount } from 'vue'
import { getQuarterDataByYear } from '/src/api/requestFuntion.js'

// Define emits for the component
const emit = defineEmits(['chartClick'])

// Props from parent component
const props = defineProps({
  productName: {
    type: String,
    default: '胡萝卜'
  }
})

let myChart = null
const trendData = ref([])
const chartDom = ref(null)

// 获取年度趋势数据
const fetchYearlyData = async () => {
  // 显示加载动画
  if (myChart) {
    myChart.showLoading({
      text: '加载数据中...',
      color: '#2db4a0',
      textColor: '#1565c0',
      maskColor: 'rgba(255, 255, 255, 0.8)',
    });
  }
  
  try {
    // 使用 byYear 接口获取年度数据
    const response = await getQuarterDataByYear({
      name: props.productName,
      start: 2010,  // 起始年份，可根据实际需求调整
      end: new Date().getFullYear()  // 当前年份
    })
    
    // 处理可能的数据结构
    if (response.data && response.data.data) {
      // 如果数据是嵌套在data字段中
      trendData.value = response.data.data
    } else if (Array.isArray(response.data)) {
      // 如果数据直接就是数组
      trendData.value = response.data
    } else {
      // 如果没有找到有效数据
      trendData.value = []
      console.warn('年度趋势API返回的数据结构不符合预期', response.data)
    }
    updateChart()
  } catch (error) {
    console.error('获取年度趋势数据失败：', error)
    trendData.value = []
    
    // 显示错误信息
    if (myChart) {
      myChart.hideLoading();
      myChart.setOption({
        title: {
          text: '数据加载失败',
          subtext: '请检查网络连接或稍后再试',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#1565c0',
            fontSize: 18
          },
          subtextStyle: {
            color: '#888',
            fontSize: 14
          }
        }
      });
    }
  } finally {
    // 隐藏加载动画
    if (myChart) {
      myChart.hideLoading();
    }
  }
}

// 颜色配置 - 使用蓝色系
const colorList = [
  new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: '#3137fd' },
    { offset: 1, color: '#37b4fd' }
  ]),
  new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: '#1565c0' },
    { offset: 1, color: '#43fce0' }
  ])
]

const option = {
	grid: {
		top: 60,    // 增加顶部间距
		bottom: 80, // 为dataZoom留出空间
		left: 65,
		right: 30
	},
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#e4f7dd',
    textStyle: {
      color: '#1565c0'
    }
  },
  dataZoom: [
    {
      type: 'slider',
      start: 0,
      end: 100
    },
    {
      start: 0,
      end: 100
    }
  ],
  legend: {
    data: ["平均价", "最大价"],
    textStyle: {
      color: '#1565c0'
    }
  },
  toolbox: {
    show: true,
    feature: {
      dataView: { show: true, readOnly: false },
      textStyle: { color: '#1565c0' },
      magicType: {
        show: true,
        type: ['line', 'bar'],
        iconStyle: { color: '#1565c0' }
      },
      restore: {
        show: true,
        iconStyle: { color: '#1565c0' }
      },
      saveAsImage: {
        show: true,
        iconStyle: { color: '#1565c0' }
      }
    },
    iconStyle: {
      borderColor: '#1565c0'
    }
  },
  calculable: true,
  xAxis: {
    type: 'category',
    axisLine: {
      lineStyle: {
        color: '#8cd7c7'
      }
    }
  },
  yAxis: {
    type: 'value',
    name: '元/公斤',
    splitLine: {
      lineStyle: {
        color: ['#e6f7f3']
      }
    }
  },
  series: [
    {
      name: "平均价",
      type: 'bar',
      barGap: '0%',
      data: [],
      label: {
        show: true,
        position: 'top',
        color: '#1565c0'
      },
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#3137fd' },
          { offset: 1, color: '#37b4fd' }
        ])
      }
    },
    {
      name: '最大价',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        type: 'solid'
      },
      itemStyle: {
        color: '#ffd93d'
      },
      lineStyle: {
        color: '#ffd93d',
        shadowColor: 'rgba(255, 217, 61, 0.3)',
        shadowBlur: 8
      },
      data: []
    }
  ]
}

const updateChart = () => {
  if (!trendData.value || !Array.isArray(trendData.value) || trendData.value.length === 0) {
    console.warn('无法更新图表：年度数据为空或格式不正确')
    return
  }

  try {
    option.series[0].data = trendData.value.map(e => [
      e.year,
      Math.floor(e.averagePrice * 100) / 100
    ])
    option.series[1].data = trendData.value.map(e => [
      e.year,
      Math.floor(e.avgMaxPrice * 100) / 100
    ])

    if (myChart) {
      myChart.setOption(option)
    }
  } catch (error) {
    console.error('更新图表时出错:', error)
    console.log('trendData值:', trendData.value)
  }
}

const initChart = () => {
  if (!chartDom.value) return
  
  if (myChart) {
    myChart.dispose()
  }
  
  myChart = echarts.init(chartDom.value)
  
  // Add click event to the chart
  myChart.on('click', (params) => {
    emit('chartClick', params)
  })
  
  updateChart()
}

// 响应窗口大小变化
const resizeHandler = () => {
  if (myChart) {
    myChart.resize()
  }
}

// Watch for changes in props to update chart
watch(
  () => trendData,
  () => updateChart(),
  { deep: true }
)

watch(
  () => props.productName,
  () => fetchYearlyData()
)

onMounted(() => {
  setTimeout(() => {
    initChart()
    fetchYearlyData()
    
    // 添加窗口大小变化监听
    window.addEventListener('resize', resizeHandler)
  }, 100)
})

onBeforeUnmount(() => {
  // 移除事件监听
  window.removeEventListener('resize', resizeHandler)
  
  // 释放图表资源
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
})
</script>

<style scoped>
    .trend-section {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    
    .chart-card {
      background: linear-gradient(135deg, #f5f9ff 0%, #e6f2ff 100%);
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(30, 136, 229, 0.15);
      padding: 20px;
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      border: 1px solid #e1efff;
    }
    
	/* 标题样式适配 */
	.chart-header {
	  margin-bottom: 16px;
	  flex-shrink: 0;
	}
	
	.main-chart-title {
	  display: flex;
	  justify-content: space-between;
	  align-items: center;
	  width: 100%;
	}
	
	.title-content {
	  display: flex;
	  align-items: center;
	}
	
	.title-decor {
	  width: 4px;
	  height: 24px;
	  background: linear-gradient(to bottom, #3137fd, #37b4fd);
	  border-radius: 2px;
	  margin-right: 12px;
	}
	
	.title-text {
	  font-size: 18px;
	  font-weight: 600;
	  color: #1565c0;
	  letter-spacing: 0.5px;
	}
	
	.chart-container {
	  width: 100%;
	  flex: 1;
	  min-height: 300px;
	}
	
	/* 响应式调整 */
	@media (max-width: 992px) {
	  .chart-card {
	    padding: 12px;
	  }
	  
	  .chart-container {
	    min-height: 250px;
	  }
	}
	
	@media (max-width: 768px) {
	  .chart-card {
	    padding: 10px;
	  }
	  
	  .main-chart-title {
	    flex-direction: column;
	    align-items: flex-start;
	    gap: 12px;
	  }
	  
	  .title-text {
	    font-size: 16px;
	  }
	  
	  .chart-container {
	    min-height: 200px;
	  }
	}
	
	@media (max-width: 576px) {
	  .chart-card {
	    padding: 8px;
	  }
	  
	  .title-text {
	    font-size: 14px;
	  }
	  
	  .title-decor {
	    height: 18px;
	  }
	}
	
	/* 图表工具提示样式 */
	:deep(.echarts-tooltip) {
	  background: rgba(255,255,255,0.95) !important;
	  border: 1px solid #e4f7dd !important;
	  box-shadow: 0 4px 12px rgba(42, 180, 160, 0.1) !important;
	  border-radius: 8px !important;
	}
	
	:deep(.echarts-tooltip .chart-tooltip-title) {
	  color: #1565c0;
	  font-weight: 600;
	  border-bottom: 1px solid #e4f7dd;
	  padding-bottom: 8px;
	  margin-bottom: 8px;
	}
	
	:deep(.echarts-tooltip .tooltip-item) {
	  color: #666;
	}
	
	/* 新增样式调整 */
	.trend-section {
	  margin-top: 20px;
	}
	
	.chart-header {
	  display: flex;
	  justify-content: space-between;
	  align-items: center;
	  margin-bottom: 20px;
	}
	
	.chart-container {
	  width: 100%;
	  height: 500px;
	}
	
	/* 响应式调整 */
	@media (max-width: 768px) {
	  .chart-container {
	    height: 300px;
	  }
	}
</style>