<template>
	<div id="calendar"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted,reactive,watch } from 'vue'
import {calendar} from '/src/api/requestFuntion.js'	
import { ElMessage } from 'element-plus'
import { definedPerson} from '/src/store/store.js';

const jobber= definedPerson()
const emit = defineEmits(['vegetable-update']);
let myChart = null

const props = defineProps({
  province: {
    type: String,
    required: true
  }
});

let vegetable =[]

const queryData = reactive({
  province: "安徽省"
})

let option = reactive(
{
  backgroundColor: '#fff',
  series: [
    {
		tooltip: {
		trigger: 'item',
		triggerOn: 'click'
		// ,
	    // formatter: "{b} {c} 公斤"
	  },
      type: 'sankey',
      left: 50.0,
      top: 20.0,
      right: 150.0,
      bottom: 25.0,
      data: [],
      links: [],
      lineStyle: {
        color: 'source',
        curveness: 0.5
      },
      itemStyle: {
        color: '#1f77b4',
        borderColor: '#1f77b4'
      },
      label: {
        color: 'rgba(0,0,0,0.7)',
        fontFamily: 'Arial',
        fontSize: 10
      }
    }
  ],
  tooltip: {
    trigger: 'item'
  }
});


const getEcharts = () => {
  var chartDom = document.getElementById('calendar')
	myChart = echarts.init(chartDom)
	
	myChart.on('click', (params) => {
	    if (params.dataType === 'node' && vegetable.indexOf(params.name)!=-1) {
			jobber.vegetable = params.name
	    }
	  })

  option && myChart.setOption(option)
  window.onresize = () => {
    myChart.resize()
  }
}

function transformToSankeyData(rawData) {
  const nodes = [];
  vegetable=[];
  const nodeMap = new Map();
  const links = [];
  const linkMap = new Map();
  
  
    // 动态生成40+种不重复颜色（HSL色轮均匀分布）
    const generateColorPalette = (count) => {
      const colors = [];
      for (let i = 0; i < count; i++) {
        const hue = Math.round((i * 360) / count); // 色相均匀分布
        colors.push(`hsl(${hue}, 70%, 50%)`);      // 固定饱和度70%、亮度50%
      }
      return colors;
    };
    
    // 生成40种基础色 + 扩展备用色（共60种）
    const colorPalette = [
      ...generateColorPalette(40),  // 基础40色
      ...generateColorPalette(20).map(c => c.replace('70%', '50%'))  // 扩展20种低饱和度色
    ];
  
    // 1. 提取节点并分配颜色
    const getUniqueColor = (index) => {
      // 如果超过调色板长度，动态生成新颜色
      if (index >= colorPalette.length) {
        const hue = Math.round(Math.random() * 360);
        return `hsl(${hue}, 60%, 45%)`;
      }
      return colorPalette[index];
    };
	
  // 1. 提取唯一节点
  rawData.forEach(item => {
	const color = getUniqueColor(nodes.length);
    // 提取 lv1
    if (!nodeMap.has(item.oneLevel)) {
      nodeMap.set(item.oneLevel, nodes.length);
      nodes.push({ name: item.oneLevel,
	  itemStyle: {
          color: color,
          borderColor: color
        }});
    }

    // 提取 lv2
    if (!nodeMap.has(item.twoLevel)) {
      nodeMap.set(item.twoLevel, nodes.length);
      nodes.push({ name: item.twoLevel,
	  itemStyle: {
          color: color,
          borderColor: color
        } });
    }

    // 提取 lv3
    if (!nodeMap.has(item.varietyname)) {
      nodeMap.set(item.varietyname, nodes.length);
      nodes.push({ name: item.varietyname ,
	  itemStyle: {
          color: color,
          borderColor: color
        }});
	  
	  vegetable.push(item.varietyname);
    }
  });
  //提交父组件
  emit('vegetable-update', vegetable);
	
  // 2. 统计链接关系，使用后端传入的 value
  rawData.forEach(item => {
    // lv1 → lv2 的链接
    const keyLv1ToLv2 = `${item.oneLevel}|${item.twoLevel}`;
    linkMap.set(
      keyLv1ToLv2,
      (linkMap.get(keyLv1ToLv2) || 0) + item.totalExportVolume // 累加 value
    );

    // lv2 → lv3 的链接
    const keyLv2ToLv3 = `${item.twoLevel}|${item.varietyname}`;
    linkMap.set(
      keyLv2ToLv3,
      (linkMap.get(keyLv2ToLv3) || 0) + item.totalExportVolume // 累加 value
    );
  });

  // 3. 构建 links 数组
  linkMap.forEach((value, key) => {
    const [sourceName, targetName] = key.split('|');
    links.push({
      source: sourceName,
      target: targetName,
      value: value
    });
  });

  return { nodes, links };
}

const initData = ()=>{
	calendar(queryData,"/user/calendar").then(resp=>{
		if(resp.data.code=='0'){
			// 使用示例
			const sankeyData = transformToSankeyData(resp.data.data);
			option.series[0].data = sankeyData.nodes;
			option.series[0].links = sankeyData.links;
			console.log(sankeyData);
			if (myChart!=null){
				  myChart.dispose()
				  myChart = null
				  getEcharts()
			}
		}else
			ElMessage.error(resp.data.msg)
	}) 
}
// 监听 props 变化
watch(() => props.province, (newVal) => {
	queryData.province = newVal
	initData()
});

onMounted(() => {
	initData()
	setTimeout(() => {
    getEcharts()
  }, 1000)
})
</script>

<style scoped>
	#calendar {
	  width: 100%;
	  height: 100%;
	  min-height: 500px;
	}
	
	.chart-header {
	  padding: 0 0 16px 0;
	}
	@media (max-width: 768px) {
	  #calendar {
	    min-height: 400px;
	  }
	}
</style>