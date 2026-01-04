<template>
<!--  <div class="scatter-chart-container">
    <div class="chart-header">
      <h2>{{ title }}</h2>
    </div>
    <div class="chart-wrapper">
      <canvas ref="chartCanvas"></canvas>
   </div>
  </div> -->
  <canvas ref="chartCanvas"></canvas>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import {Chart} from 'chart.js/auto';

export default {
  name: 'ScatterChart',
  props: {
    // 图表标题
    title: {
      type: String,
      default: '散点图分析'
    },
    // X轴数据（方差）
    xData: {
      type: Array,
      required: true
    },
    // Y轴数据（销量）
    yData: {
      type: Array,
      required: true
    },
    // X轴标签
    xAxisLabel: {
      type: String,
      default: '方差'
    },
    // Y轴标签
    yAxisLabel: {
      type: String,
      default: '销量'
    },
    // 市场名称
    marketNames: {
      type: Array,
      required: true
    }
  },
  setup(props) {
    const chartCanvas = ref(null);
    let scatterChart = null;
    
    // 配色方案 - 遵循绘画美学设计
    const colorSchemes = [
      { 
        name: 'artistic', 
        label: '艺术风格',
        backgroundColor: [
          'rgba(142, 68, 173, 0.7)', 
          'rgba(41, 128, 185, 0.7)', 
          'rgba(39, 174, 96, 0.7)',
          'rgba(243, 156, 18, 0.7)',
          'rgba(231, 76, 60, 0.7)'
        ],
        borderColor: [
          'rgba(142, 68, 173, 1)', 
          'rgba(41, 128, 185, 1)', 
          'rgba(39, 174, 96, 1)',
          'rgba(243, 156, 18, 1)',
          'rgba(231, 76, 60, 1)'
        ]
      }
    ];
    
    const selectedColorScheme = ref('artistic');

    // 计算数据点大小（根据销量动态调整）
    const calculatePointSize = (yValues) => {
      const minY = Math.min(...yValues);
      const maxY = Math.max(...yValues);
      const baseRadius = 4; // 最小半径
      const maxRadius = 12; // 最大半径

      return yValues.map(y => {
        if (maxY === minY) return baseRadius; // 处理所有值相同的情况
        return baseRadius + (y - minY) * (maxRadius - baseRadius) / (maxY - minY);
      });
    };
    
    // 生成图表数据
    const generateChartData = () => {
      const currentScheme = colorSchemes.find(scheme => scheme.name === selectedColorScheme.value);
      const data = [];
      
      const minCount = Math.min(props.xData.length, props.yData.length);
      const pointSizes = calculatePointSize(props.yData.slice(0, minCount)); // 计算点大小

      for (let i = 0; i < minCount; i++) {
        const pointColorIndex = i % currentScheme.backgroundColor.length;
        data.push({
          x: props.xData[i],
          y: props.yData[i],
          marketname: props.marketNames ? props.marketNames[i] || `市场${i + 1}` : `市场${i + 1}`, // 添加默认值
          backgroundColor: currentScheme.backgroundColor[pointColorIndex],
          borderColor: currentScheme.borderColor[pointColorIndex],
          radius: pointSizes[i] // 添加半径属性
        });
      }
      
      return data;
    };
    
    // 创建图表
    const createChart = () => {
      const ctx = chartCanvas.value.getContext('2d');
      const chartData = generateChartData();
      
      scatterChart = new Chart(ctx, {
        type: 'scatter',
        data: {
          datasets: [{
            label: `${props.xAxisLabel} vs ${props.yAxisLabel}`,
            data: chartData,
            pointBackgroundColor: chartData.map(point => point.backgroundColor),
            pointBorderColor: chartData.map(point => point.borderColor),
            pointRadius: chartData.map(point => point.radius), // 使用动态半径
            pointHoverRadius: chartData.map(point => point.radius * 1.2), // 悬停时放大1.2倍
            pointBorderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          animation: {
            duration: 1000,
            easing: 'easeOutQuart'
          },
          scales: {
            x: {
              title: {
                display: true,
                text: props.xAxisLabel,
                font: {
                  size: 14,
                  weight: 'bold'
                }
              },
              grid: {
                color: 'rgba(200, 200, 200, 0.3)'
              }
            },
            y: {
              title: {
                display: true,
                text: props.yAxisLabel,
                font: {
                  size: 14,
                  weight: 'bold'
                }
              },
              grid: {
                color: 'rgba(200, 200, 200, 0.3)'
              }
            }
          },
          plugins: {
            tooltip: {
              callbacks: {
                label: function(context) {
                  const dataPoint = context.dataset.data[context.dataIndex];
                  return [
                    `市场: ${dataPoint.marketname}`,
                    `${props.xAxisLabel}: ${context.parsed.x.toFixed(2)}`,
                    `${props.yAxisLabel}: ${context.parsed.y}`
                  ];
                }
              }
            },
            legend: {
              display: false
            }
          }
        }
      });
    };
    
    // 更新图表
    const updateChart = () => {
      if (scatterChart) {
        scatterChart.destroy();
      }
      createChart();
    };
    
    onMounted(() => {
      createChart();
    });
    
    // 监听数据变化
    watch(
      [() => props.xData, () => props.yData, () => props.title], 
      () => {
        updateChart();
      },
      { deep: true }
    );
    
    return {
      chartCanvas,
      colorSchemes,
      selectedColorScheme,
      updateChart,
    };
  }
}
</script>

<style scoped>
/* 原有样式保持不变 */
.scatter-chart-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 16px;
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h2 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
}

.chart-controls {
  display: flex;
  gap: 16px;
  align-items: center;
}

.download-btn {
  padding: 6px 12px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.download-btn:hover {
  background-color: #2980b9;
}

.color-scheme-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-scheme-selector select {
  padding: 6px 8px;
  border-radius: 4px;
  border: 1px solid #ddd;
  font-size: 14px;
}

.chart-wrapper {
  width: 100%;
  height: 400px;
  position: relative;
}

.chart-info {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 14px;
  color: #7f8c8d;
}

.axis-info {
  display: flex;
  gap: 16px;
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .chart-controls {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .chart-wrapper {
    height: 300px;
  }
  
  .chart-info {
    flex-direction: column;
    gap: 8px;
  }
}
</style>