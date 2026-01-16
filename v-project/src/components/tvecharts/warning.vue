<template>
  <div class="log-container">
    <div class="tech-grid"></div>
    
    <div class="log-scroll-area">
      <div 
        v-for="(item, index) in alertData" 
        :key="index" 
        class="log-card"
        :class="item.level"
        :style="{ '--i': index }"
      >
        <div class="status-bar"></div>
        
        <div class="log-inner">
          <div class="log-prefix">
            <div class="level-tag">{{ formatLevel(item.level) }}</div>
            <div class="log-icon">
              <span v-if="item.level === 'high'">⚡</span>
              <span v-else-if="item.level === 'medium'">⚠</span>
              <span v-else>ℹ</span>
            </div>
          </div>

          <div class="log-body">
            <div class="log-header">
              <span class="log-id">编号：{{ 1024 + index }}</span>
              <span class="log-time">{{ item.time.split(' ')[1] }}</span>
            </div>
            <div class="log-content" :title="item.message">{{ item.message }}</div>
          </div>
        </div>

        <div class="decor-line"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
// 此处建议保留您原有的 store 导入逻辑

const props = defineProps({
  province: { type: String, default: '' },
  product: { type: String, default: '' }
});

const formatLevel = (level) => {
  const map = { high: '紧急风险', medium: '异常预警', low: '常规记录' };
  return map[level] || '未知状态';
};

const allAlertData = ref([
  // ... 您之前的河南/四川大白菜与黄瓜数据保持不变 ...
  { level: 'high', message: '紧急预警：河南省大白菜产区受强冷空气影响，预计未来24小时将出现大面积霜冻灾害。', time: '2026-01-16 14:55:00', province: '河南省', product: '大白菜' },
  { level: 'medium', message: '物流提醒：河南省大白菜至京津冀运输通道受降雪影响，平均转运时间延迟约6小时。', time: '2026-01-16 14:40:22', province: '河南省', product: '大白菜' },
  { level: 'low', message: '生产同步：河南省大白菜示范基地完成土壤pH值检测，检测结果均处于最佳生长区间。', time: '2026-01-16 14:15:10', province: '河南省', product: '大白菜' },
  { level: 'high', message: '市场异动：河南省黄瓜批发价格单日涨幅达22%，已触及二级市场价格波动调控红线。', time: '2026-01-16 15:02:11', province: '河南省', product: '黄瓜' },
  { level: 'medium', message: '环境监控：河南省黄瓜大棚湿度持续高于85%，白粉病发生概率等级提升至“中高风险”。', time: '2026-01-16 14:45:30', province: '河南省', product: '黄瓜' },
  { level: 'low', message: '信息更新：河南省黄瓜主产区新增3处冷链预冷中心，预计提升田头保鲜能力15%。', time: '2026-01-16 14:20:05', province: '河南省', product: '黄瓜' },
  { level: 'high', message: '质量拦截：四川省某批次大白菜检出农药残留超标，已启动全渠道追踪拦截并下架处理。', time: '2026-01-16 15:10:45', province: '四川省', product: '大白菜' },
  { level: 'medium', message: '供应预警：四川省高山大白菜受持续性降雨影响，本周采收量预计较同期下降20%。', time: '2026-01-16 14:38:12', province: '四川省', product: '大白菜' },
  { level: 'low', message: '追溯系统：四川省大白菜全产业链数字化追溯码覆盖率已达98%，消费者扫码活跃度上升。', time: '2026-01-16 14:05:55', province: '四川省', product: '大白菜' },
  { level: 'high', message: '价格警报：四川省黄瓜田头收购价跌破种植成本价，建议启动政府临时性储备调控方案。', time: '2026-01-16 15:05:00', province: '四川省', product: '黄瓜' },
  { level: 'medium', message: '播报：四川省黄瓜上市期受暖冬天气影响提前7天，请各农贸市场做好库容调配准备。', time: '2026-01-16 14:50:33', province: '四川省', product: '黄瓜' },
  { level: 'low', message: '统计简报：四川省黄瓜出口贸易额本季度环比增长5.4%，东南亚市场订单需求旺盛。', time: '2026-01-16 14:10:12', province: '四川省', product: '黄瓜' }
]);

const alertData = computed(() => {
  const filtered = allAlertData.value.filter(item => {
    if (!item.province && !item.product) return true;
    if (item.province && item.province !== props.province) return false;
    if (item.product && item.product !== props.product) return false;
    return true;
  });
  return filtered.slice(0, 3);
});
</script>

<style scoped>
.log-container {
  --danger-red: #ff1744;
  --warning-orange: #ff9100;
  --info-blue: #00e5ff;
  --bg-deep: #050a0f;
  --border-color: rgba(255, 255, 255, 0.1);

  position: relative;
  width: 100%;
  height: 100%;
  background: var(--bg-deep);
  padding: 4px;
  box-sizing: border-box;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.tech-grid {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(0, 240, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.03) 1px, transparent 1px);
  background-size: 15px 15px;
  z-index: 1;
}

.log-scroll-area {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.log-card {
  position: relative;
  height: 33%;
  margin-bottom: 2px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-sizing: border-box;
  overflow: hidden;
}

.log-card:last-child {
  margin-bottom: 0;
}

.log-card.high { --current-color: var(--danger-red); }
.log-card.medium { --current-color: var(--warning-orange); }
.log-card.low { --current-color: var(--info-blue); }

.log-card.high {
  background: linear-gradient(90deg, rgba(255, 23, 68, 0.15), rgba(255, 23, 68, 0.02));
  border-color: rgba(255, 23, 68, 0.4);
}

.log-card.medium {
  background: linear-gradient(90deg, rgba(255, 145, 0, 0.12), rgba(255, 145, 0, 0.02));
  border-color: rgba(255, 145, 0, 0.4);
}

.log-card.low {
  background: linear-gradient(90deg, rgba(0, 229, 255, 0.1), rgba(0, 229, 255, 0.02));
  border-color: rgba(0, 229, 255, 0.4);
}

.status-bar {
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 5px;
  background: var(--current-color);
  box-shadow: 2px 0 12px var(--current-color);
}

.log-inner {
  display: flex;
  padding: 6px 12px;
  height: 100%;
  align-items: center;
  box-sizing: border-box;
}

.log-prefix {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  min-width: 70px;
}

.level-tag {
  font-size: 10px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 3px;
  background: var(--current-color);
  color: #000;
  margin-bottom: 3px;
  box-shadow: 0 0 8px var(--current-color);
}

.log-icon {
  font-size: 16px;
  color: var(--current-color);
  text-shadow: 0 0 10px var(--current-color);
}

.log-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.log-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 2px;
}

.log-id, .log-time {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
}

.log-content {
  color: #ffffff;
  font-size: 12px;
  line-height: 1.3;
  letter-spacing: 0.3px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

.log-card {
  animation: fadeIn 0.4s ease forwards;
  animation-delay: calc(var(--i) * 0.05s);
}

.log-card.high {
  animation: fadeIn 0.4s ease forwards, pulse-red 2s infinite alternate;
}

@keyframes pulse-red {
  from { box-shadow: inset 0 0 0px rgba(255, 23, 68, 0); }
  to { box-shadow: inset 0 0 15px rgba(255, 23, 68, 0.2); }
}

.decor-line {
  position: absolute;
  bottom: 0; right: 0;
  width: 25%; height: 1px;
  background: linear-gradient(90deg, transparent, var(--current-color));
  opacity: 0.3;
}
</style>