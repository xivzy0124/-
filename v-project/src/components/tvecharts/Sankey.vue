<template>
  <div class="sankey-container tech-bg">
    <div class="sankey-chart" ref="sankeyChart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { calendar } from '../../api/requestFuntion.js'
import { mapCity } from '../../stores/store.js'
import { ElMessage } from 'element-plus'

const sankeyChart = ref(null)
let myChart = null
const mapCityStore = mapCity()

const queryData = {
  province: mapCityStore.currentProvince,
}

// --- 1. 修改配色方案为科技霓虹色 ---
const techColorPalette = [
  '#00ffff', // 青色
  '#0099ff', // 天蓝
  '#3366ff', // 湛蓝
  '#9900ff', // 紫罗兰
  '#ff00ff', // 洋红
  '#ff3399', // 荧光粉
  '#ff6600', // 亮橙
  '#ffff00', // 荧光黄
  '#66ff66', // 荧光绿
  '#00ff99', // 青绿
  '#FFFFFF'  // 纯白点缀
];

// 获取颜色的辅助函数，循环使用色板
const getUniqueColor = (index) => {
  return techColorPalette[index % techColorPalette.length];
}

// --- 修改结束 ---

const option = {
  // --- 2. 修改背景色为深空蓝/黑 ---
  backgroundColor: '#050d19', // 或者用透明，依赖外部容器背景
  // --- 修改结束 ---
  series: [
    {
      type: 'sankey',
      left: '5%',
      right: '5%',
      top: '10%', // 稍微调整顶部距离
      bottom: '10%',
      nodeWidth: 25, // 稍微加宽节点
      nodeGap: 16, // 增加节点间距
      data: [],
      links: [],
      // --- 3. 修改线条样式，增加发光效果 ---
      lineStyle: {
        color: 'source', // 颜色跟随源节点，形成渐变流光
        curveness: 0.6, // 稍微增加曲率
        opacity: 0.4, // 提高一点基础透明度
        shadowColor: 'rgba(0, 255, 255, 0.5)', // 线条发光色
        shadowBlur: 10 // 发光模糊度
      },
      // --- 4. 修改节点样式，增加强烈的科技感边框和发光 ---
      itemStyle: {
        borderWidth: 2,
        borderColor: '#00ffff', // 统一使用青色高亮边框
        shadowColor: 'rgba(0, 255, 255, 0.8)', // 强烈的外发光
        shadowBlur: 20
      },
      // --- 5. 修改标签样式，增加文字发光 ---
      label: {
        color: '#ffffff',
        fontFamily: '"Orbitron", "Microsoft YaHei", Arial, sans-serif', // 尝试使用科技感字体
        fontSize: 12,
        fontWeight: 600,
        textShadowColor: '#00ffff', // 文字发光
        textShadowBlur: 5,
        formatter: '{b}'
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          opacity: 0.8,
          shadowBlur: 20, // 高亮时增强发光
          shadowColor: 'rgba(255, 255, 255, 0.8)'
        },
        itemStyle: {
          shadowBlur: 30,
          shadowColor: '#ffffff', // 高亮时变白光
          borderColor: '#ffffff'
        },
      },
    },
  ],
  // --- 6. 修改提示框样式以匹配科技风 ---
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(5, 13, 25, 0.9)', // 深色背景
    borderColor: '#00ffff', // 青色边框
    borderWidth: 1,
    padding: [10, 15],
    textStyle: {
      color: '#fff',
      fontSize: 13,
      textShadowColor: 'rgba(0, 255, 255, 0.5)',
      textShadowBlur: 2
    },
    formatter: (params) => {
      if (params.dataType === 'node') {
        // 标题使用霓虹色
        return `<div style="color: #00ffff; font-weight: bold; font-size: 14px; margin-bottom: 5px;">⬢ ${params.name}</div>`
      } else if (params.dataType === 'link') {
        return `
          <div style="color: #0099ff; margin-bottom: 6px;">▶ ${params.data.source} → ${params.data.target}</div>
          <div style="color: #ff00ff; font-weight: bold;">⚡ 交易量: <span style="color:#ffffff; font-family: monospace; font-size: 1.1em;">${params.data.value.toLocaleString()}</span> 吨</div>
        `
      }
      return ''
    },
  },
}

// --- 数据转换函数修改 ---
// 移除旧的 HSL 生成代码，使用新的 getUniqueColor
const transformToSankeyData = (rawData) => {
  const nodes = []
  const nodeMap = new Map()
  const links = []
  const linkMap = new Map()
  const vegetables = []

  let colorIndex = 0;

  // 辅助函数：添加节点
  const addNode = (name) => {
    if (!nodeMap.has(name)) {
      const color = getUniqueColor(colorIndex++);
      nodeMap.set(name, nodes.length)
      nodes.push({
        name: name,
        // 这里只设置填充色，边框和阴影由全局 itemStyle 控制
        itemStyle: {
          color: color,
          // 如果希望边框颜色也跟随自身颜色而不是统一青色，取消下面注释，并注释掉全局配置中的 borderColor
          // borderColor: color,
          // shadowColor: color
        },
      })
      return true;
    }
    return false;
  }

  rawData.forEach((item) => {
    addNode(item.oneLevel);
    addNode(item.twoLevel);
    if (addNode(item.varietyname)) {
        vegetables.push(item.varietyname)
    }
  })

  rawData.forEach((item) => {
    // Level 1 -> Level 2
    const keyLv1ToLv2 = `${item.oneLevel}|${item.twoLevel}`
    linkMap.set(keyLv1ToLv2, (linkMap.get(keyLv1ToLv2) || 0) + item.totalExportVolume)

    // Level 2 -> Level 3 (Variety)
    const keyLv2ToLv3 = `${item.twoLevel}|${item.varietyname}`
    linkMap.set(keyLv2ToLv3, (linkMap.get(keyLv2ToLv3) || 0) + item.totalExportVolume)
  })

  linkMap.forEach((value, key) => {
    const [sourceName, targetName] = key.split('|')
    links.push({
      source: sourceName,
      target: targetName,
      value: value,
    })
  })

  return { nodes, links, vegetables }
}
// --- 数据转换函数修改结束 ---

const initData = async () => {
  console.log('=== 桑吉图初始化日志 ===')
  queryData.province = mapCityStore.currentProvince
  console.log('1. 当前省份：', queryData.province)

  // 初始化mock数据（确保始终有数据显示）
  // 稍微增加一些mock数据以展示效果
  const baseMockData = [
    { oneLevel: `${queryData.province}农产数据中心`, twoLevel: '蔬菜类监测点', varietyname: '大白菜 (Core)', totalExportVolume: 1500 },
    { oneLevel: `${queryData.province}农产数据中心`, twoLevel: '蔬菜类监测点', varietyname: '萝卜 (Node-A)', totalExportVolume: 800 },
    { oneLevel: `${queryData.province}农产数据中心`, twoLevel: '蔬菜类监测点', varietyname: '辣椒 (Node-B)', totalExportVolume: 1200 },
    { oneLevel: `${queryData.province}农产数据中心`, twoLevel: '水果类监测点', varietyname: '苹果 (Core)', totalExportVolume: 1400 },
    { oneLevel: `${queryData.province}农产数据中心`, twoLevel: '水果类监测点', varietyname: '梨 (Node-C)', totalExportVolume: 600 },
    { oneLevel: `${queryData.province}农产数据中心`, twoLevel: '谷物类战略储备', varietyname: '小麦 (Main)', totalExportVolume: 2000 },
    { oneLevel: `${queryData.province}农产数据中心`, twoLevel: '谷物类战略储备', varietyname: '玉米 (Main)', totalExportVolume: 2500 },
     { oneLevel: '外部协同节点', twoLevel: '谷物类战略储备', varietyname: '大豆 (Import)', totalExportVolume: 1800 },
  ]

  let finalData = baseMockData
  let dataSource = 'mock数据'

  // 尝试获取真实数据，但即使失败也不影响显示
  try {
    // 模拟API延迟，让效果更明显
    // await new Promise(resolve => setTimeout(resolve, 500));
    const resp = await calendar(queryData, '/user/calendar')
    console.log('2. API返回：', resp)

    if (resp.data.code == '0' && resp.data.data && resp.data.data.length > 0) {
      finalData = resp.data.data
      dataSource = '真实数据'
      console.log('3. 成功获取真实数据：', finalData.length, '条')
    } else {
      console.log('3. API返回空数据，继续使用mock数据')
    }
  } catch (apiError) {
    console.log('3. API请求失败，继续使用mock数据：', apiError.message)
  }

  // 转换数据为桑吉图格式
  const sankeyData = transformToSankeyData(finalData)
  console.log('4. 数据转换完成：', {
    节点数: sankeyData.nodes.length,
    连接线数: sankeyData.links.length,
    数据源: dataSource,
  })

  // 更新图表配置
  option.series[0].data = sankeyData.nodes
  option.series[0].links = sankeyData.links

  // 确保图表实例存在
  if (!myChart) {
    console.log('5. 图表实例不存在，重新初始化')
    initChart()
  }

  // 设置图表数据
  if (myChart) {
    console.log('6. 设置图表数据...')
    myChart.setOption(option, true)
    console.log('✅ 7. 桑吉图显示成功！')
  } else {
    console.error('❌ 7. 图表实例创建失败！')
  }
}

const initChart = () => {
  if (!sankeyChart.value) {
    console.error('桑吉图容器未找到')
    return
  }

  // 销毁旧实例（如果存在）
  if (myChart) {
    myChart.dispose()
  }

  // 确保容器有明确的高度
  sankeyChart.value.style.height = '100%'
  sankeyChart.value.style.width = '100%'

  // 创建新实例，使用 dark 主题（如果 echarts 引入了 dark 主题包）
  // 这里直接在 option 里配置了背景色，所以不用特定主题
  myChart = echarts.init(sankeyChart.value)
  console.log('桑吉图实例已创建')

  // 设置初始配置
  myChart.setOption(option)

  // 添加点击事件
  myChart.on('click', (params) => {
    if (params.dataType === 'node') {
      // 为了检查是否是蔬菜，这里需要一个简化的查找方式。
      // 在真实应用中，你可能需要更严谨的判断逻辑。
      const isVegetable = option.series[0].data.some(node => 
          node.name === params.name && 
          // 这里只是一个简单的假设，基于你之前的 transform 逻辑
          // 实际需要根据业务逻辑判断点击的是否是最后一级的品种名
          !option.series[0].links.some(link => link.source === params.name)
      );

      if (isVegetable) {
        console.log('点击了品种节点:', params.name);
        mapCityStore.setCurrentProduct(params.name)
        ElMessage.success(`已选择品种: ${params.name}`)
      }
    }
  })

  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  if (myChart) {
    myChart.resize()
  }
}

watch(
  () => mapCityStore.currentProvince,
  () => {
    initData()
  },
)

onMounted(() => {
  nextTick(async () => {
    initChart()
    // 确保图表实例创建后再加载数据
    await initData()
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
})
</script>

<style scoped>
.sankey-container {
  width: 100%;
  height: 100%;
  position: relative;
  min-height: 300px;
  /* 添加一个微妙的科技感网格背景 */
  background-image: 
    linear-gradient(rgba(0, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
  background-color: #050d19; /* 确保在图表加载前也有背景 */
}

/* 可选：添加一个四周的暗角效果 */
.sankey-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, transparent 30%, rgba(0, 0, 0, 0.6) 100%);
  pointer-events: none; /* 确保不影响鼠标交互 */
}

.sankey-chart {
  width: 100%;
  height: 100%;
  min-height: 300px;
}

@media (max-width: 768px) {
  .sankey-container,
  .sankey-chart {
    min-height: 400px;
  }
}
</style>