import * as echarts from 'echarts'
export function getEcharts(id,option){
  var chartDom = document.getElementById(id)
  var myChart = echarts.init(chartDom)

  option && myChart.setOption(option)
  window.onresize = () => {
    myChart.resize()
  }
}

export const colorList = [
    '#c23531', // 原红色
    '#2f4554', // 原深蓝
    '#61a0a8', // 原蓝绿
    '#d48265', // 原橙
    '#91c7ae', // 原浅绿
    '#749f83', // 原橄榄绿
    '#ca8622', // 原土黄
    // 新增颜色（从第8到15）
    '#6e7074', // 中性灰蓝
    '#bda29a', // 灰粉调
    '#446888', // 沉稳湖蓝
    '#8a7ca8', // 薰衣草紫
    '#dd6b66', // 珊瑚粉
    '#4b8a7e', // 深青绿
    '#e098c7', // 浅紫粉
    '#5ab1ef', // 明快天蓝
    '#3d8d79', // 翡翠绿
    '#bf7334', // 深橙棕
    '#9c8d4a'  // 橄榄金
];