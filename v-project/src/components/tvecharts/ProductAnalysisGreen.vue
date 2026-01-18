<template>
  <div class="analysis-card">
    <div class="chart-body">
      <div class="chart-section left-section">
        
        <div class="left-header-block">
          <div class="section-title">批发市场每日成交量监测</div>
          
          <div class="header-right-area">
            <div class="mini-stat-group">
               <span class="stat-item">
                 <span class="label">总</span>
                 <span class="value">{{ totalVolume.toLocaleString() }}</span>
               </span>
               <span class="divider">/</span>
               <span class="stat-item">
                 <span class="label">均</span>
                 <span class="value">{{ avgVolume.toLocaleString() }}</span>
               </span>
            </div>
            
            <div class="city-tag">{{ currentCityName }}</div>
          </div>
        </div>
        
        <div class="radar-container">
          <div ref="radarChartRef" class="echarts-box"></div>
          <div class="scan-ring"></div>
          <div class="radar-bg-grid"></div>
          <div class="corner-decor top-left"></div>
          <div class="corner-decor top-right"></div>
          <div class="corner-decor bottom-left"></div>
          <div class="corner-decor bottom-right"></div>
        </div>
        
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'
import { mapLocation } from '../../stores/store.js'

const mapLocationStore = mapLocation()
const radarChartRef = ref(null)
let radarChart = null
const currentCityName = ref('加载中...')
const currentGroupData = ref({ data: [] })

const totalVolume = computed(() => {
    return currentGroupData.value.data.reduce((sum, item) => sum + item.value, 0)
})

const avgVolume = computed(() => {
    if (currentGroupData.value.data.length === 0) return 0
    return Math.round(totalVolume.value / currentGroupData.value.data.length)
})

const getMarketData = (province, city) => {
  // 【数据更新】筛选了2025年依然活跃的“超级物流园”级市场
  const marketData = {
    '四川省': {
      '成都市': [
        // 彭州：四川最大的蔬菜集散中心（雨润系）
        { name: '四川雨润国际农产品交易中心', value: 9850, max: 12000 },
        // 双流白家：成都市区核心“菜篮子”，国企背景，极稳
        { name: '成都农产品中心批发市场', value: 8760, max: 12000 },
        // 龙泉驿：西南最大的水果集散地
        { name: '成都龙泉聚和国际果蔬交易中心', value: 5200, max: 12000 },
        // 青白江：西南最大的冷链/肉类/冻品枢纽
        { name: '成都银犁冷冻食品物流中心', value: 6300, max: 12000 },
        // 彭州：也就是原来的蒙阳市场，依然是主力
        { name: '四川国际农产品交易中心', value: 7100, max: 12000 }
      ]
    },
    '河南省': {
      '郑州市': [
        // 中牟：中原地区最大的农产品航母（万邦系垄断地位）
        { name: '河南万邦国际农产品物流城', value: 11800, max: 12000 },
        // 惠济区：中原最大的水产/冻品/牛羊肉物流港
        { name: '中原四季水产物流港', value: 7600, max: 12000 },
        // 区分细度：万邦的蔬菜交易区单独算一个巨型市场
        { name: '万邦千亩蔬菜交易专区', value: 8900, max: 12000 },
        // 管城区：江北最大的调味品/干货批发市场
        { name: '郑州信基调味食品城', value: 4500, max: 12000 },
        // 中牟：大蒜是大宗单品，有专门的国际交易市场
        { name: '河南中牟大蒜国际交易市场', value: 6200, max: 12000 }
      ]
    }
  }

  const data = marketData[province]?.[city] || marketData['河南省']['郑州市']
  return {
    city: `📍 ${province}·${city}`,
    data
  }
}

const getRadarOption = (groupData) => {
  const values = groupData.data.map(item => item.value)
  const indicator = groupData.data.map(item => ({ 
    name: item.name, 
    max: item.max 
  }))

  // 【核心修改】使用上一张图提取的色板
  const cyanMain = '#00f2ff'   // 高亮青
  const blueMain = '#009dff'   // 标准蓝
  const deepBlue = '#4e77ff'   // 深蓝
  const purple   = '#a855f7'   // 紫色点缀
  
  return {
    backgroundColor: 'transparent',
    grid: { top: 5, bottom: 5, left: 5, right: 5 },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0, 10, 20, 0.9)', // 深蓝黑背景
      borderColor: cyanMain,
      borderWidth: 1,
      textStyle: { color: '#fff' },
      formatter: (params) => {
        let html = `<div style="color:${cyanMain};font-weight:bold;margin-bottom:8px;font-size:14px;">${groupData.city}</div>`
        groupData.data.forEach((item) => {
           const percent = Math.round((item.value / item.max) * 100)
           html += `<div style="display:flex;justify-content:space-between;min-width:200px;margin-bottom:4px;">
                      <span style="color:#cbd5e1;font-size:12px;">${item.name.substring(0, 4)}...</span>
                      <span style="color:#fff;font-weight:bold;">${item.value.toLocaleString()} <span style="font-size:10px;color:${blueMain}">(${percent}%)</span></span>
                    </div>`
        })
        return html
      }
    },
    radar: {
      indicator: indicator,
      center: ['50%', '55%'], 
      radius: '72%', 
      shape: 'polygon',
      splitNumber: 4,
      axisName: {
        formatter: (val) => {
           const item = groupData.data.find(d => d.name === val)
           const value = item ? item.value.toLocaleString() : ''
           
           let displayName = val
           if (val.length > 6) {
             const mid = Math.ceil(val.length / 2)
             displayName = val.slice(0, mid) + '\n' + val.slice(mid)
           }

           return `{a|${displayName}}\n{b|${value} 吨}`
        },
        rich: {
           a: {
               color: '#e2e8f0', // 稍微灰一点的白
               fontWeight: 'bold', 
               fontSize: 11,     
               lineHeight: 14,
               align: 'center',
               padding: [0, 0, 4, 0],
               textShadowColor: 'rgba(0,0,0,0.8)', 
               textShadowBlur: 3
           },
           b: {
               color: cyanMain, // 数值用青色高亮
               fontSize: 12,      
               fontWeight: 'bold',
               fontFamily: 'Arial',
               align: 'center',
               textShadowColor: 'rgba(0,0,0,0.8)', 
               textShadowBlur: 3
           }
        }
      },
      axisNameGap: 8, 
      splitArea: {
        show: true,
        areaStyle: {
          // 网格背景：青蓝渐变交替，增加科技感
          color: [
            'rgba(0, 242, 255, 0.05)', 
            'rgba(78, 119, 255, 0.05)', 
            'transparent', 
            'transparent'
          ],
        }
      },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)', type: 'dashed' } },
      splitLine: { lineStyle: { color: 'rgba(0, 157, 255, 0.3)' } }
    },
    series: [
      {
        name: '市场成交量',
        type: 'radar',
        symbol: 'circle',
        symbolSize: 6,
        label: { show: false }, 
        itemStyle: {
          color: '#fff',
          borderColor: cyanMain,
          borderWidth: 2,
          shadowColor: cyanMain,
          shadowBlur: 10
        },
        lineStyle: {
          color: cyanMain,
          width: 2,
          shadowColor: 'rgba(0, 242, 255, 0.5)',
          shadowBlur: 10
        },
        areaStyle: {
          // 雷达图填充：青色 -> 紫色 渐变
          color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
            { offset: 0, color: 'rgba(34, 211, 238, 0.6)' }, // 青色
            { offset: 0.6, color: 'rgba(78, 119, 255, 0.4)' }, // 蓝色
            { offset: 1, color: 'rgba(168, 85, 247, 0.2)' }    // 紫色边缘
          ]),
          opacity: 0.8
        },
        data: [
          {
            value: values,
            name: '成交量'
          }
        ]
      }
    ]
  }
}

const updateChart = () => {
  if (!radarChart) return
  const group = getMarketData(mapLocationStore.currentProvince, mapLocationStore.currentCity)
  currentCityName.value = group.city
  currentGroupData.value = group
  radarChart.setOption(getRadarOption(group))
}

const initChart = () => {
  if (!radarChartRef.value) return
  radarChart = echarts.init(radarChartRef.value)
  updateChart()
  window.addEventListener('resize', () => radarChart?.resize())
}

watch(() => mapLocationStore.currentProvince, () => updateChart())
watch(() => mapLocationStore.currentCity, () => updateChart())

onMounted(() => {
  setTimeout(initChart, 200)
})

onUnmounted(() => {
  radarChart?.dispose()
})
</script>

<style scoped>
/* 容器背景修改为冷色调 */
.analysis-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  /* 背景：由深绿改为深蓝灰渐变，更符合科技感 */
  background: linear-gradient(135deg, rgba(2, 12, 20, 0.3) 0%, rgba(10, 25, 45, 0.3) 100%);
  border-radius: 8px;
  overflow: hidden;
  /* 边框：青色微光 */
  border: 1px solid rgba(0, 242, 255, 0.15);
  box-shadow: 0 0 15px rgba(0, 242, 255, 0.05);
}

.chart-body {
  flex: 1;
  display: flex;
  padding: 10px;
  gap: 10px;
  flex-direction: column; 
}

.left-section { flex: 1; display: flex; flex-direction: column; }

.left-header-block {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
  width: 100%;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #00f2ff; /* 标题改为青色 */
  padding-left: 12px;
  border-left: 4px solid #009dff;
  background: linear-gradient(90deg, rgba(0,157,255,0.15) 0%, transparent 100%);
  letter-spacing: 0.5px;
  flex-shrink: 0;
  text-shadow: 0 0 5px rgba(0, 242, 255, 0.3);
}

.header-right-area {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
}

.mini-stat-group {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    background: rgba(0, 157, 255, 0.1); /* 蓝色背景 */
    padding: 3px 10px;
    border-radius: 4px;
    border: 1px solid rgba(0, 157, 255, 0.3);
}

.stat-item .label {
    color: #94a3b8;
    margin-right: 3px;
    font-size: 11px;
}

.stat-item .value {
    color: #fff;
    font-weight: bold;
    font-family: 'Arial', sans-serif;
    font-size: 13px;
}

.divider { color: #475569; margin: 0 2px; }

.city-tag {
  font-size: 12px;
  color: #ffffff;
  /* 标签背景改为蓝色，文字白色 */
  background: linear-gradient(90deg, #009dff 0%, #0077ff 100%);
  padding: 3px 10px;
  border-radius: 4px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.radar-container {
  flex: 1; 
  width: 100%;
  min-height: 0;
  position: relative;
  /* 雷达图底座背景：稍微加深一点 */
  background: rgba(2, 6, 15, 0.3); 
  border: 1px solid rgba(0, 242, 255, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: inset 0 0 30px rgba(0, 157, 255, 0.05);
}

.echarts-box {
  width: 100%;
  height: 100%;
  z-index: 10;
}

.scan-ring {
  position: absolute;
  width: 280px; 
  height: 280px;
  border-radius: 50%;
  /* 扫描光效：改为青色 */
  background: conic-gradient(
    from 0deg,
    transparent 0deg,
    transparent 300deg,
    rgba(0, 242, 255, 0.05) 330deg,
    rgba(0, 242, 255, 0.3) 360deg
  );
  z-index: 5;
  animation: radarScan 6s linear infinite;
  pointer-events: none;
  filter: blur(2px);
}

@keyframes radarScan {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.radar-bg-grid {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  /* 背景网格线：改为青色微光 */
  background-image: 
    linear-gradient(rgba(0, 242, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 242, 255, 0.1) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 0;
  opacity: 0.3;
  mask-image: radial-gradient(circle at center, black 30%, transparent 80%);
}

.corner-decor {
    position: absolute;
    width: 20px;
    height: 20px;
    /* 角标：改为亮青色 */
    border-color: #00f2ff;
    border-style: solid;
    z-index: 2;
    opacity: 0.6;
    box-shadow: 0 0 8px rgba(0, 242, 255, 0.4);
}
.top-left { top: 0; left: 0; border-width: 2px 0 0 2px; border-top-left-radius: 4px; }
.top-right { top: 0; right: 0; border-width: 2px 2px 0 0; border-top-right-radius: 4px; }
.bottom-left { bottom: 0; left: 0; border-width: 0 0 2px 2px; border-bottom-left-radius: 4px; }
.bottom-right { bottom: 0; right: 0; border-width: 0 2px 2px 0; border-bottom-right-radius: 4px; }
</style>