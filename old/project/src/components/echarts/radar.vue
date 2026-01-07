<template>
	<div id="radar" class="radar-chart"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted,reactive,ref,watch } from 'vue'
import {radar} from '/src/api/requestFuntion.js'	
import { ElMessage } from 'element-plus'
import { definedPerson} from '/src/store/store.js';
let jobber= definedPerson()
let myChart = null

const queryData = reactive({
  province: "安徽省",
  greens : "大葱"
})

// 监听单个属性
watch(
  () => jobber.vegetable,
  (newValue, oldValue) => {
    queryData.province = jobber.province;
	queryData.greens = newValue;
	initData()
  }
);

const option = reactive({})

const getEcharts = () => {
  var chartDom = document.getElementById('radar')
  myChart = echarts.init(chartDom)

  option && myChart.setOption(option)
  window.onresize = () => {
    myChart.resize()
  }
}


// 数据处理函数（包含动态计算平均值）
const processData = (data) => {
  const variances = data.map(item => item.priceVariance);
  const avgVariance = variances.length > 0 
    ? variances.reduce((a, b) => a + b, 0) / variances.length
    : 0;
  const markets = data.map(item => item.marketname);
  const maxValue = Math.max(...variances, avgVariance) * 1.2;

  return {
    backgroundColor: 'transparent',
    title: {
      left: 'center',
      top: 20,
      textStyle: {
        color: '#333',
        fontSize: 16,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#2db4a0',
      borderWidth: 1,
      textStyle: {
        color: '#333'
      },
      formatter: (params) => {
        const dataIndex = params.dataIndex;
        const currentVar = variances[dataIndex];
        const diff = currentVar - avgVariance;
        const ratio = (Math.abs(diff) / avgVariance * 100).toFixed(1);
        
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 8px; color: #2db4a0;">${markets[dataIndex]}</div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
              <span>当前方差：</span>
              <span style="font-weight: bold;">${currentVar.toFixed(2)}</span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
              <span>行业平均：</span>
              <span style="font-weight: bold;">${avgVariance.toFixed(2)}</span>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span>差值：</span>
              <span style="font-weight: bold; color: ${diff > 0 ? '#ff4444' : '#4488ff'}">
                ${diff > 0 ? '+' : ''}${diff.toFixed(2)} (${ratio}%)
              </span>
            </div>
          </div>
        `;
      }
    },
    radar: {
      indicator: markets.map((name, index) => ({
        name: `${name}\n(方差: ${variances[index].toFixed(4)})`,
        max: maxValue,
        axisLabel: {
          color: variances[index] > avgVariance ? '#ff4444' : '#666',
          fontWeight: variances[index] > avgVariance ? 'bold' : 'normal',
          fontSize: 12,
          padding: [0, 0, 0, 0]
        },
        nameTextStyle: {
          color: '#666',
          fontSize: 12,
          padding: [0, 0, 0, 0]
        }
      })),
      splitNumber: 4,
      axisName: {
        color: '#666',
        backgroundColor: '#fff',
        borderRadius: 3,
        padding: [3, 5]
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(45, 180, 160, 0.05)', 'rgba(45, 180, 160, 0.1)']
        }
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(45, 180, 160, 0.2)'
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(45, 180, 160, 0.2)'
        }
      }
    },
    series: [{
      type: 'radar',
      data: [
        {
          value: variances,
          name: '市场价格方差',
          symbol: 'circle',
          symbolSize: 6,
          itemStyle: {
            color: ({ dataIndex }) => variances[dataIndex] > avgVariance ? '#ff4444' : '#4488ff'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(68,136,255,0.8)' },
              { offset: 1, color: 'rgba(68,136,255,0.2)' }
            ])
          },
          lineStyle: {
            width: 2,
            color: '#4488ff'
          },
          emphasis: {
            lineStyle: {
              width: 4
            },
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(68,136,255,0.5)'
            }
          }
        },
        {
          value: new Array(markets.length).fill(avgVariance),
          name: '行业平均线',
          symbol: 'none',
          lineStyle: {
            type: 'dashed',
            color: '#666',
            width: 1.5
          },
          itemStyle: {
            color: 'transparent'
          }
        }
      ],
      animation: true,
      animationDuration: 1000,
      animationEasing: 'cubicInOut'
    }],
    legend: {
      bottom: 10,
      data: ['市场价格方差', '行业平均线'],
      textStyle: {
        color: '#666'
      }
    }
  }
};

const initData =()=>{
	radar(queryData,"/market/anomaly").then(resp=>{
		if(resp.data.code=='0'){
			const newOption = processData(resp.data.data);
			Object.assign(option, newOption)

			if (myChart!=null){
				  myChart.dispose()
				  myChart = null
				  getEcharts()
			}
		}else
			ElMessage.error(resp.data.msg)
	})  
}

onMounted(() => {
	initData()
	setTimeout(() => {
    getEcharts()
  }, 1000)
})
</script>

<style scoped>
.radar-chart {
  width: 100%;
  height: 100%;
  min-height: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(42, 180, 160, 0.08);
  padding: 20px;
  transition: all 0.3s ease;
}

.radar-chart:hover {
  box-shadow: 0 6px 16px rgba(42, 180, 160, 0.12);
}

@media (max-width: 768px) {
  .radar-chart {
    min-height: 300px;
    padding: 10px;
  }
}
</style>
