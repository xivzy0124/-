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

// ==================== 1. 定义高亮配色 ====================
const techColorPalette = [
  '#00f2ff', '#009dff', '#4e77ff', '#a855f7', '#22d3ee', '#818cf8',
  '#f472b6', '#34d399', '#facc15'
];

const getUniqueColor = (index) => {
  return techColorPalette[index % techColorPalette.length];
}

// ==================== 2. 定义丰富且真实的模拟数据 ====================

// 河南：中原粮仓，大宗蔬菜与菌类大省
const henanData = [
  // --- 葱蒜类（河南王牌） ---
  { oneLevel: '河南中心', twoLevel: '葱蒜类', varietyname: '杞县大蒜', totalExportVolume: 9200 }, 
  { oneLevel: '河南中心', twoLevel: '葱蒜类', varietyname: '中牟大蒜', totalExportVolume: 6500 },
  { oneLevel: '河南中心', twoLevel: '葱蒜类', varietyname: '正阳大葱', totalExportVolume: 3400 },
  
  // --- 根茎类（怀药之乡） ---
  { oneLevel: '河南中心', twoLevel: '根茎类', varietyname: '温县铁棍山药', totalExportVolume: 4800 },
  { oneLevel: '河南中心', twoLevel: '根茎类', varietyname: '焦作怀地黄', totalExportVolume: 2100 },
  { oneLevel: '河南中心', twoLevel: '根茎类', varietyname: '万滩红薯', totalExportVolume: 3600 },
  { oneLevel: '河南中心', twoLevel: '根茎类', varietyname: '开封胡萝卜', totalExportVolume: 2900 },

  // --- 茄果/辣椒类（豫东辣都） ---
  { oneLevel: '河南中心', twoLevel: '辣椒类', varietyname: '柘城三樱椒', totalExportVolume: 5800 }, 
  { oneLevel: '河南中心', twoLevel: '辣椒类', varietyname: '临颍小辣椒', totalExportVolume: 4500 },
  { oneLevel: '河南中心', twoLevel: '茄果类', varietyname: '西红柿', totalExportVolume: 3100 },

  // --- 菌菇类（西峡香菇甲天下） ---
  { oneLevel: '河南中心', twoLevel: '食用菌', varietyname: '西峡香菇', totalExportVolume: 6700 },
  { oneLevel: '河南中心', twoLevel: '食用菌', varietyname: '卢氏黑木耳', totalExportVolume: 2300 },
  { oneLevel: '河南中心', twoLevel: '食用菌', varietyname: '清丰白灵菇', totalExportVolume: 1200 },

  // --- 特色叶菜 ---
  { oneLevel: '河南中心', twoLevel: '叶菜类', varietyname: '淮阳黄花菜', totalExportVolume: 2600 },
  { oneLevel: '河南中心', twoLevel: '叶菜类', varietyname: '信阳毛尖(茶食)', totalExportVolume: 800 }, 
];

// 四川：天府之国，调味、喜湿作物与高山蔬菜
const sichuanData = [
  // --- 调味/香料类（川菜灵魂） ---
  { oneLevel: '四川中心', twoLevel: '调味菜', varietyname: '汉源花椒', totalExportVolume: 3200 },
  { oneLevel: '四川中心', twoLevel: '调味菜', varietyname: '威远七星椒', totalExportVolume: 4100 },
  { oneLevel: '四川中心', twoLevel: '调味菜', varietyname: '二荆条', totalExportVolume: 5600 },
  { oneLevel: '四川中心', twoLevel: '调味菜', varietyname: '郫县豆瓣原料', totalExportVolume: 4800 },
  { oneLevel: '四川中心', twoLevel: '根茎类', varietyname: '自贡仔姜', totalExportVolume: 2400 },

  // --- 腌制/茎菜类 ---
  { oneLevel: '四川中心', twoLevel: '茎菜类', varietyname: '青菜头(榨菜原料)', totalExportVolume: 7800 },
  { oneLevel: '四川中心', twoLevel: '茎菜类', varietyname: '彭州莴笋', totalExportVolume: 5200 },
  { oneLevel: '四川中心', twoLevel: '茎菜类', varietyname: '宜宾竹笋', totalExportVolume: 3600 },

  // --- 特色珍稀/高山蔬菜 ---
  { oneLevel: '四川中心', twoLevel: '特色山珍', varietyname: '雷波魔芋', totalExportVolume: 4300 },
  { oneLevel: '四川中心', twoLevel: '特色山珍', varietyname: '甘孜松茸', totalExportVolume: 600 }, 
  { oneLevel: '四川中心', twoLevel: '特色山珍', varietyname: '羊肚菌', totalExportVolume: 900 },

  // --- 时令叶菜 ---
  { oneLevel: '四川中心', twoLevel: '叶菜类', varietyname: '豌豆尖', totalExportVolume: 2800 }, 
  { oneLevel: '四川中心', twoLevel: '叶菜类', varietyname: '红油菜苔', totalExportVolume: 1900 },
  { oneLevel: '四川中心', twoLevel: '叶菜类', varietyname: '藤藤菜', totalExportVolume: 2100 },
];

// 默认兜底数据（其他省份用）
const defaultMockData = [
  { oneLevel: '农贸中心', twoLevel: '根茎类', varietyname: '土豆', totalExportVolume: 2200 },
  { oneLevel: '农贸中心', twoLevel: '根茎类', varietyname: '胡萝卜', totalExportVolume: 1300 },
  { oneLevel: '农贸中心', twoLevel: '柑橘类', varietyname: '橘子', totalExportVolume: 1700 },
  { oneLevel: '农贸中心', twoLevel: '豆类', varietyname: '绿豆', totalExportVolume: 900 },
  { oneLevel: '农贸中心', twoLevel: '叶菜类', varietyname: '菠菜', totalExportVolume: 950 },
];

// ==================== 3. ECharts 配置 ====================
const option = {
  backgroundColor: 'rgba(0,0,0,0)', // 完全透明
  series: [
    {
      type: 'sankey',
      left: '5%',
      right: '18%', 
      top: '5%',    // 上下留白调小一点，容纳更多数据
      bottom: '5%',
      nodeWidth: 10, 
      nodeGap: 14,  // 间隙调小，防止数据太多挤出画面
      draggable: false,
      layoutIterations: 32,
      data: [],
      links: [],
      lineStyle: {
        color: 'source',
        curveness: 0.5,
        opacity: 0.35, // 稍微透明一点，防止线条重叠太乱
      },
      itemStyle: {
        borderWidth: 1,
        borderColor: 'rgba(255,255,255,0.6)', 
        color: 'transparent' // 如果你想让节点也是空的，否则会填充上面的配色
      },
      label: {
        color: '#fff', 
        fontFamily: 'Microsoft YaHei',
        fontSize: 12, // 字体稍微改小适应密集数据
        fontWeight: 500, 
        distance: 12,
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

// ==================== 4. 数据转换逻辑 ====================
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
      // 根节点（Level 0）文字在左边
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

  // 构建节点
  rawData.forEach((item) => {
    addNode(item.oneLevel, 0);
    addNode(item.twoLevel, 1);
    addNode(item.varietyname, 2);
  })

  // 聚合连线值（防止重复连线）
  rawData.forEach((item) => {
    const k1 = `${item.oneLevel}|${item.twoLevel}`
    linkMap.set(k1, (linkMap.get(k1) || 0) + item.totalExportVolume)
    const k2 = `${item.twoLevel}|${item.varietyname}`
    linkMap.set(k2, (linkMap.get(k2) || 0) + item.totalExportVolume)
  })

  // 生成连线数组
  linkMap.forEach((value, key) => {
    const [source, target] = key.split('|')
    links.push({ source, target, value })
  })

  return { nodes, links }
}

// ==================== 5. 初始化与渲染逻辑 ====================
const initData = async () => {
  queryData.province = mapLocationStore.currentProvince
  const currentProv = queryData.province || ''

  // 智能匹配数据
  let targetMockData = defaultMockData
  if (currentProv.includes('河南')) {
    targetMockData = henanData
  } else if (currentProv.includes('四川')) {
    targetMockData = sichuanData
  }

  let finalData = null
  
  // 尝试调用接口，如果失败则静默使用模拟数据
  try {
    const resp = await calendar(queryData, '/user/calendar')
    if (resp && resp.length > 0) finalData = resp
  } catch (e) {
    // 接口错误不弹窗，直接降级
  }

  // 如果接口没数据，应用模拟数据
  if (!finalData) finalData = targetMockData

  const { nodes, links } = transformToSankeyData(finalData)
  option.series[0].data = nodes
  option.series[0].links = links

  if (!myChart) initChart()
  else {
    // notMerge: true 确保切换省份时清空旧画布，避免连线混乱
    myChart.setOption(option, true) 
  }
}

const initChart = () => {
  if (!sankeyChart.value) return
  myChart = echarts.init(sankeyChart.value)
  myChart.setOption(option)
  
  myChart.on('click', (params) => {
    if (params.dataType === 'node') {
      // 判断是否为叶子节点（没有作为 source 的连线）
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
.sankey-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
  background: transparent; /* 关键：透明背景 */
  backdrop-filter: none;   /* 关键：移除模糊 */
  overflow: hidden;
  position: relative;
}

.sankey-chart {
  width: 100%;
  height: 100%;
}
</style>