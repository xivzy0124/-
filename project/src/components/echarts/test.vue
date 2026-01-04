<template>
  <div class="container">
    <!-- 筛选区域 -->
<!--    <div class="filter-area">
      <div class="filter-item">
        <label>省份：</label>
        <select v-model="ScatteData.provincename" @change="generateRandomData">
          <option value="安徽省">安徽省</option>
          <option value="江苏省">江苏省</option>
          <option value="浙江省">浙江省</option>
        </select>
      </div>
      
      <div class="filter-item">
        <label>蔬菜：</label>
        <select v-model="ScatteData.marketname" @change="generateRandomData">
          <option value="莴笋">莴笋</option>
          <option value="白菜">白菜</option>
          <option value="萝卜">萝卜</option>
        </select>
      </div>
    </div> -->

    <!-- 图表标题 -->
    <!-- <h2 class="chart-title">{{ chartTitle }}</h2> -->
    
    <!-- 图表区域 -->
    <div class="chart-container">
      <ScatterChart 
        :xData="xData" 
        :yData="yData" 
        :xAxisLabel="xAxisLabel"
        :yAxisLabel="yAxisLabel"
        :marketNames="marketNames"
      />
    </div>
  </div>
</template>

<script>
import { ref, onMounted, reactive,watch } from 'vue';
import ScatterChart from './scatterVariance.vue';
import { selectScatterVa } from '/src/api/requestFuntion.js';
import { definedPerson} from '/src/store/store.js';

const ScatteData = reactive({
  provincename: "安徽省",
  marketname : "莴笋",
  flag: "scatter"
});

export default {
  name: 'App',
  components: {
    ScatterChart
  },
  setup() {
	const jobber= definedPerson()
    const xData = ref([]);
    const yData = ref([]);
    const marketNames = ref([]);
    const xAxisLabel = ref('方差');
    const yAxisLabel = ref('均价');
    const chartTitle = ref('安徽省莴苣方差/分布散点分析图');

    const generateRandomData = async () => {
      chartTitle.value = `${ScatteData.provincename}${ScatteData.marketname}方差/均价分布散点分析图`;
      try {
        const resp = await selectScatterVa(ScatteData, "/user/byScatter");
        if (resp.status === 200) {
          const newXData = [];
          const newYData = [];
          const newMarketNames = [];
          resp.data.data.forEach(item => {
            newXData.push(item.priceVariance);
            newYData.push(item.averagePrice);
            newMarketNames.push(item.marketname);
          });
          xData.value = newXData;
          yData.value = newYData;
          marketNames.value = newMarketNames;
        } else {
          console.error('数据获取失败:', resp.message);
        }
      } catch (error) {
        console.error('请求错误:', error);
      }
    };

    onMounted(() => {
      generateRandomData();
    });
	watch(
	  () => jobber.vegetable,
	  (newValue, oldValue) => {
	    ScatteData.provincename = jobber.province;
		ScatteData.marketname = newValue;
		generateRandomData()
	  }
	)
    return {
      xData,
      yData,
      marketNames,
      xAxisLabel,
      yAxisLabel,
      chartTitle,
      ScatteData,
      generateRandomData
    };
  }
};
</script>

<style scoped>
/* 全局样式 */
:root {
  --primary-color: #1e88e5; /* 蓝色主色调 */
  --bg-color: #f5f7fa; /* 背景色 */
  --text-color: #333; /* 文本主色 */
  --border-color: #e0e0e0; /* 边框色 */
}

/* 移除全局样式定义 */
.container {
  max-width: 100%; /* 改为继承父容器宽度 */
  padding: 0; /* 移除内边距 */
}


/* 筛选区域适配 */
.filter-area {
  background-color: rgba(255,255,255,0.98);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 使用主界面阴影 */
  margin-bottom: 16px;
  border-radius: 10px;
  padding: 16px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-item label {
  font-weight: 500;
  color: var(--text-color);
}

/* 下拉框样式 */
select {
  padding: 8px 15px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 14px;
  background-color: white;
  color: var(--text-color);
  cursor: pointer;
  transition: border-color 0.3s ease;
}

select:hover {
  border-color: var(--primary-color);
}

select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.2);
}

/* 图表标题样式统一 */
.chart-title {
  color: #1565c0;
  font-size: 16px;
  font-weight: 600;
  padding-bottom: 8px;
  margin-bottom: 16px;
  border-bottom: 2px solid #1e88e5;
}

/* 图表容器样式适配 */
.chart-container {
  background-color: transparent;
  box-shadow: none;
  padding: 0;
  min-height: 400px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-area {
    flex-direction: column;
    align-items: flex-start;
    padding: 15px;
  }
}
/* 下拉框样式继承element-plus */
select {
  width: 120px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px 15px;
  font-size: 14px;
  color: #606266;
  background-color: #fff;
  background-image: none;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}

select:focus {
  outline: none;
  border-color: #1e88e5;
}
</style>