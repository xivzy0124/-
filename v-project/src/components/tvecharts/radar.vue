<template>
  <div id="radar" class="radar-chart"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, reactive, ref, watch } from 'vue'
import { radar } from '/src/api/requestFuntion.js'
import { ElMessage } from 'element-plus'
import { mapCity } from '/src/stores/store.js'

const mapCityStore = mapCity()
let myChart = null

const queryData = reactive({
  province: '北京',
  greens: '鸡蛋',
})

watch(
  () => mapCityStore.currentProvince,
  (newValue) => {
    queryData.province = newValue
    initData()
  },
)

watch(
  () => mapCityStore.currentProduct,
  (newValue) => {
    queryData.greens = newValue
    initData()
  },
)

const option = reactive({})

const getEcharts = () => {
  var chartDom = document.getElementById('radar')
  myChart = echarts.init(chartDom)

  option && myChart.setOption(option)
  window.onresize = () => {
    myChart.resize()
  }
}

const processData = (data) => {
  const variances = data.map((item) => item.priceVariance)
  const avgVariance =
    variances.length > 0 ? variances.reduce((a, b) => a + b, 0) / variances.length : 0
  const markets = data.map((item) => item.marketname)
  const maxValue = Math.max(...variances, avgVariance) * 1.2

  return {
    backgroundColor: 'transparent',
    title: {
      left: 'center',
      top: 20,
      textStyle: {
        color: '#00ff9f',
        fontSize: 16,
        fontWeight: 'normal',
        textShadow: '0 0 10px rgba(0,255,159,0.5)',
      },
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0,195,85,0.9)',
      borderColor: '#00e676',
      borderWidth: 1,
      textStyle: {
        color: '#fff',
      },
      formatter: (params) => {
        const dataIndex = params.dataIndex
        const currentVar = variances[dataIndex]
        const diff = currentVar - avgVariance
        const ratio = ((Math.abs(diff) / avgVariance) * 100).toFixed(1)

        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 8px; color: #00ff9f;">${markets[dataIndex]}</div>
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
              <span style="font-weight: bold; color: ${diff > 0 ? '#ff6b6b' : '#69f0ae'}">
                ${diff > 0 ? '+' : ''}${diff.toFixed(2)} (${ratio}%)
              </span>
            </div>
          </div>
        `
      },
    },
    radar: {
      indicator: markets.map((name, index) => ({
        name: `${name}\n(方差: ${variances[index].toFixed(4)})`,
        max: maxValue,
        axisLabel: {
          color: variances[index] > avgVariance ? '#ff6b6b' : '#00e676',
          fontWeight: variances[index] > avgVariance ? 'bold' : 'normal',
          fontSize: 12,
          padding: [0, 0, 0, 0],
        },
        nameTextStyle: {
          color: '#00ff9f',
          fontSize: 12,
          padding: [0, 0, 0, 0],
          textShadow: '0 0 5px rgba(0,255,159,0.3)',
        },
      })),
      splitNumber: 4,
      axisName: {
        color: '#00ff9f',
        backgroundColor: 'rgba(0,195,85,0.1)',
        borderRadius: 3,
        padding: [3, 5],
        borderColor: '#00e676',
        borderWidth: 1,
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(0,255,159,0.05)', 'rgba(0,255,159,0.1)'],
        },
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(0,230,118,0.4)',
        },
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0,230,118,0.3)',
        },
      },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: variances,
            name: '市场价格方差',
            symbol: 'circle',
            symbolSize: 8,
            itemStyle: {
              color: ({ dataIndex }) =>
                variances[dataIndex] > avgVariance ? '#ff6b6b' : '#00ff9f',
              borderColor: '#fff',
              borderWidth: 1,
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(0,255,159,0.8)' },
                { offset: 1, color: 'rgba(0,255,159,0.2)' },
              ]),
            },
            lineStyle: {
              width: 2,
              color: '#00ff9f',
              shadowColor: 'rgba(0,255,159,0.5)',
              shadowBlur: 10,
            },
            emphasis: {
              lineStyle: {
                width: 4,
                shadowColor: 'rgba(0,255,159,0.8)',
                shadowBlur: 15,
              },
              itemStyle: {
                shadowBlur: 15,
                shadowColor: 'rgba(0,255,159,0.8)',
              },
            },
          },
          {
            value: new Array(markets.length).fill(avgVariance),
            name: '行业平均线',
            symbol: 'none',
            lineStyle: {
              type: 'dashed',
              color: '#00e676',
              width: 1.5,
            },
            itemStyle: {
              color: 'transparent',
            },
          },
        ],
        animation: true,
        animationDuration: 1000,
        animationEasing: 'cubicInOut',
      },
    ],
    legend: {
      bottom: 10,
      data: ['市场价格方差', '行业平均线'],
      textStyle: {
        color: '#00ff9f',
        textShadow: '0 0 5px rgba(0,255,159,0.3)',
      },
      itemStyle: {
        color: '#00e676',
      },
    },
  }
}

const mockDataSets = [
  [
    { marketname: '北京新发地', priceVariance: 0.12 },
    { marketname: '上海江桥', priceVariance: 0.08 },
    { marketname: '广州江南', priceVariance: 0.15 },
    { marketname: '深圳海吉星', priceVariance: 0.06 },
    { marketname: '杭州良渚', priceVariance: 0.1 },
  ],
  [
    { marketname: '成都益民', priceVariance: 0.18 },
    { marketname: '武汉白沙洲', priceVariance: 0.14 },
    { marketname: '南京众彩', priceVariance: 0.09 },
    { marketname: '西安欣桥', priceVariance: 0.11 },
    { marketname: '重庆双福', priceVariance: 0.07 },
  ],
  [
    { marketname: '长沙黄兴', priceVariance: 0.05 },
    { marketname: '合肥周谷堆', priceVariance: 0.13 },
    { marketname: '南昌深圳', priceVariance: 0.16 },
    { marketname: '贵阳石板', priceVariance: 0.04 },
    { marketname: '昆明王旗营', priceVariance: 0.19 },
  ],
]

const getRandomData = () => {
  const randomIndex = Math.floor(Math.random() * mockDataSets.length)
  return mockDataSets[randomIndex]
}

const initData = () => {
  const mockData = getRandomData()
  const newOption = processData(mockData)
  Object.assign(option, newOption)

  if (myChart != null) {
    myChart.dispose()
    myChart = null
    getEcharts()
  }
}

onMounted(() => {
  queryData.province = mapCityStore.currentProvince || '安徽省'
  queryData.greens = mapCityStore.currentProduct || '大葱'
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
  background: linear-gradient(135deg, rgba(0, 195, 85, 0.1) 0%, rgba(0, 230, 118, 0.05) 100%);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 230, 118, 0.15);
  border: 1px solid rgba(0, 255, 159, 0.3);
  padding: 20px;
  transition: all 0.3s ease;
}

.radar-chart:hover {
  box-shadow: 0 6px 25px rgba(0, 230, 118, 0.2);
  border-color: rgba(0, 255, 159, 0.5);
}

@media (max-width: 768px) {
  .radar-chart {
    min-height: 300px;
    padding: 10px;
  }
}
</style>
