<template>
  <div class="sankey-container">
    <div class="sankey-chart" ref="sankeyChart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
// 注意：请确保这些路径在你项目中是正确的
import { calendar } from '../../api/requestFuntion.js'
import { mapLocation, mapProduct } from '../../stores/store.js'
import { ElMessage } from 'element-plus'

const sankeyChart = ref(null)
let myChart = null
const mapLocationStore = mapLocation()
const mapProductStore = mapProduct()

const queryData = {
  province: mapLocationStore.currentProvince,
}

// 配色保持不变，高亮色在深色视频背景上会很好看
const techColorPalette = [
  '#00f2ff', '#009dff', '#4e77ff', '#a855f7', '#22d3ee', '#818cf8'
];

const getUniqueColor = (index) => {
  return techColorPalette[index % techColorPalette.length];
}

const option = {
  // 【关键修改 1】确保 ECharts 画布完全透明
  backgroundColor: 'rgba(0,0,0,0)', 
  series: [
    {
      type: 'sankey',
      left: '5%',
      right: '18%', 
      top: '8%',
      bottom: '8%',
      nodeWidth: 10, // 稍微再变细一点，让背景露出来更多
      nodeGap: 18,
      draggable: false,
      layoutIterations: 32,
      data: [],
      links: [],
      lineStyle: {
        color: 'source',
        curveness: 0.5,
        // 【关键修改 2】稍微提高一点透明度，防止线条太淡在视频上看不清
        // 如果视频很花，可以设为 0.4；如果视频很暗，0.25 也可以
        opacity: 0.4, 
      },
      itemStyle: {
        borderWidth: 1,
        borderColor: 'rgba(255,255,255,0.6)', // 增强一点边框亮度
        color: 'transparent' // 尝试让节点本身半透明（可选），这里先保持原色
      },
      label: {
        color: '#fff', // 纯白字在视频上最清晰
        fontFamily: 'Microsoft YaHei',
        fontSize: 13, // 稍微加大一点字体
        fontWeight: 500, // 加粗一点
        distance: 12,
        // 添加文字阴影，防止视频背景太亮导致文字看不清
        textShadowColor: 'rgba(0,0,0,0.8)',
        textShadowBlur: 3,
        textShadowOffsetX: 1,
        textShadowOffsetY: 1
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { opacity: 0.8 },
        itemStyle: {
          shadowBlur: 20,
          shadowColor: '#ffffff'
        }
      },
    },
  ],
  tooltip: {
    trigger: 'item',
    // Tooltip 保持半透明深色背景，不受影响
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    borderColor: '#00f2ff',
    borderWidth: 1,
    textStyle: { color: '#fff' },
    formatter: (params) => {
      if (params.dataType === 'node') {
        return `<div style="padding:3px 8px;">📊 节点: <b>${params.name}</b></div>`
      }
      return `
        <div style="padding:5px;">
          <span style="color:#cbd5e1">流向：</span>${params.data.source} ➜ ${params.data.target}<br/>
          <span style="color:#00f2ff">交易量：</span><b style="font-size:1.1em">${params.data.value}</b> 吨
        </div>
      `
    },
  },
}

const transformToSankeyData = (rawData) => {
  const nodes = []
  const nodeMap = new Map()
  const links = []
  const linkMap = new Map()
  let colorIndex = 0;

  const addNode = (name, level) => {
    if (!nodeMap.has(name)) {
      const color = getUniqueColor(colorIndex++);
      let labelConfig = { position: 'right' };
      if (level === 0) {
        labelConfig = { position: 'left', distance: 15 };
      }
      nodes.push({
        name: name,
        itemStyle: { color: color },
        label: labelConfig
      });
      nodeMap.set(name, nodes.length - 1);
      return true;
    }
    return false;
  }

  rawData.forEach((item) => {
    addNode(item.oneLevel, 0);
    addNode(item.twoLevel, 1);
    addNode(item.varietyname, 2);
  })

  rawData.forEach((item) => {
    const k1 = `${item.oneLevel}|${item.twoLevel}`
    linkMap.set(k1, (linkMap.get(k1) || 0) + item.totalExportVolume)
    const k2 = `${item.twoLevel}|${item.varietyname}`
    linkMap.set(k2, (linkMap.get(k2) || 0) + item.totalExportVolume)
  })

  linkMap.forEach((value, key) => {
    const [source, target] = key.split('|')
    links.push({ source, target, value })
  })

  return { nodes, links }
}

const initData = async () => {
  queryData.province = mapLocationStore.currentProvince
  const mockData = [
    { oneLevel: `${queryData.province}中心`, twoLevel: '根茎类', varietyname: '土豆', totalExportVolume: 2200 },
    { oneLevel: `${queryData.province}中心`, twoLevel: '根茎类', varietyname: '胡萝卜', totalExportVolume: 1300 },
    { oneLevel: `${queryData.province}中心`, twoLevel: '柑橘类', varietyname: '橘子', totalExportVolume: 1700 },
    { oneLevel: `${queryData.province}中心`, twoLevel: '豆类', varietyname: '绿豆', totalExportVolume: 900 },
    { oneLevel: `${queryData.province}中心`, twoLevel: '叶菜类', varietyname: '菠菜', totalExportVolume: 950 },
  ]

  let finalData = null
  try {
    const resp = await calendar(queryData, '/user/calendar')
    if (resp && resp.length > 0) finalData = resp
  } catch (e) {}

  if (!finalData) finalData = mockData

  const { nodes, links } = transformToSankeyData(finalData)
  option.series[0].data = nodes
  option.series[0].links = links

  if (!myChart) initChart()
  else myChart.setOption(option, true)
}

const initChart = () => {
  if (!sankeyChart.value) return
  myChart = echarts.init(sankeyChart.value)
  myChart.setOption(option)
  myChart.on('click', (params) => {
    if (params.dataType === 'node') {
      const isLeaf = !option.series[0].links.some(l => l.source === params.name)
      if (isLeaf) {
        mapProductStore.setCurrentProduct(params.name)
        ElMessage.success(`定位品种: ${params.name}`)
      }
    }
  })
  window.addEventListener('resize', () => myChart && myChart.resize())
}

watch(() => mapLocationStore.currentProvince, () => { initData() })
onMounted(() => { nextTick(() => initData()) })
onUnmounted(() => { if (myChart) myChart.dispose() })
</script>

<style scoped>
/* 【重点修改样式】 
  1. 移除背景颜色 background
  2. 移除 backdrop-filter 模糊效果
*/
.sankey-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
  
  /* 设为完全透明，让父组件视频透出来 */
  background: transparent; 
  
  /* 移除模糊，否则视频会变糊 */
  backdrop-filter: none; 
  
  overflow: hidden;
  position: relative; /* 确保层级正确 */
}

.sankey-chart {
  width: 100%;
  height: 100%;
}
</style>