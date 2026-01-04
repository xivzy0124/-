<template>
  <div class="chart-container">
    <div class="chart-title">农产品分类旭日图</div>
    <div id="Chart" style="width: 100%; height: 600px;"></div>
  </div>
</template>

<script>
import { getAllVegetableTypes } from '/src/api/requestFuntion.js'
import * as echarts from 'echarts'; 

export default {
  name: 'VegetableSunburst',
  data() {
    return {
      vegetableData: [] // 从后端获取的数据
    };
  },
  mounted() {
    // 组件挂载后获取数据
    this.fetchVegetableData();
  },
  methods: {
    async fetchVegetableData() {
      try {
        const response = await getAllVegetableTypes()
        if (response.data && response.data.code === '0') { 
          this.vegetableData = response.data.data;
          this.$nextTick(() => {
            this.renderChart();
          });
        } else {
          console.error('Error fetching data:', response.data.msg);
        }
      } catch (error) {
        console.error('API call failed:', error);
      }
    },
    transformDataForSunburst(data) {
      const root = { name: '', children: [] };
      const oneLevelMap = new Map();

      data.forEach(item => {
        if (!oneLevelMap.has(item.oneLevel)) {
          oneLevelMap.set(item.oneLevel, { name: item.oneLevel, children: [], value: 0 });
          root.children.push(oneLevelMap.get(item.oneLevel));
        }

        const oneLevelNode = oneLevelMap.get(item.oneLevel);
        let twoLevelNode = oneLevelNode.children.find(child => child.name === item.twoLevel);

        if (!twoLevelNode) {
          twoLevelNode = { name: item.twoLevel, children: [], value: 0 };
          oneLevelNode.children.push(twoLevelNode);
        }

        twoLevelNode.children.push({ name: item.varietyname, value: 1 });
        oneLevelNode.value += 1;
        twoLevelNode.value += 1;
      });

      return root;
    },
    renderChart() {
      const chartDom = document.getElementById('Chart');
      const myChart = echarts.init(chartDom);

      const transformedData = this.transformDataForSunburst(this.vegetableData);

      // 使用与ai.vue一致的配色方案
      const colorPalette = [
        '#1e88e5', '#43fc79', '#ff9800', '#fc6243', '#37b4fd',
        '#fcf843', '#43fce0', '#0d47a1', '#fcb943', '#3137fd'
      ];

      const options = {
        backgroundColor: '#f5f7fa',
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}'
        },
        series: {
          type: 'sunburst',
          data: [transformedData],
          radius: [20, "95%"],
          label: {
            rotate: 'radial',
            color: '#333'
          },
          itemStyle: {
            borderWidth: 1,
            borderColor: 'rgba(255,255,255,0.5)'
          },
          levels: [
            {},
            {
              r0: '20%',
              r: '35%',
              itemStyle: {
                color: colorPalette[0]
              },
              label: {
                rotate: 'tangential',
                fontSize: 14
              }
            },
            {
              r0: '35%',
              r: '70%',
              itemStyle: {
                color: colorPalette[2]
              }
            },
            {
              r0: '70%',
              r: '95%',
              itemStyle: {
                color: colorPalette[4]
              },
              label: {
                position: 'outside',
                padding: 3,
                silent: false
              }
            }
          ]
        }
      };

      myChart.setOption(options);
      
      // 响应窗口大小变化
      window.addEventListener('resize', function() {
        myChart.resize();
      });
    }
  }
};
</script>

<style scoped>
.chart-container {
  background-color: white;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.chart-title {
  font-size: 18px;
  margin-bottom: 15px;
  color: #1565c0;
  padding-bottom: 10px;
  border-bottom: 2px solid #e3f2fd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  #Chart {
    height: 400px !important;
  }
  
  .chart-title {
    font-size: 16px;
  }
}
</style>