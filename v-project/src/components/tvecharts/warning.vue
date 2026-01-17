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
  const map = { high: '紧急风险', medium: '异常预警', low: ' 商机发现' };
  return map[level] || '未知状态';
};

const map = { high: '损失风险', medium: '运营预警', low: '利润商机' };



const allAlertData = ref([
  // ==========================================
  // Group 1: 四川黄瓜 (Sichuan Cucumber)
  // ==========================================
  { 
    // 🔴 High: 长期没太阳 -> 长得歪瓜裂枣 -> 超市拒收
    level: 'high', typeName: map.high, 
    message: '品质红线：受四川盆地长期寡照影响，本批黄瓜“弯瓜”和“大肚瓜”占比超40%，无法达到商超一级货标准，建议直接拒收，避免定级亏损。', 
    time: '2026-01-16 14:01:00', province: '四川省', product: '黄瓜' 
  },
  { 
    // 🔵 Low: 天气晴好 -> 产量大跌价 -> 抄底赚钱
    level: 'low', typeName: map.low, 
    message: '抄底良机：四川产区连续晴好产量激增，今日地头价已跌破红线，建议满仓进货，赌周末市场反弹。', 
    time: '2026-01-16 14:02:00', province: '四川省', product: '黄瓜' 
  },
  { 
    // 🟡 Medium: 暖冬早熟 -> 集中到货 -> 爆仓风险
    level: 'medium', typeName: map.medium, 
    message: '库容告急：暖冬导致四川黄瓜集中早熟，明日预计到货量翻倍，请提前腾空冷库，防止货物积压在过道。', 
    time: '2026-01-16 14:03:00', province: '四川省', product: '黄瓜' 
  },

  // ==========================================
  // Group 2: 四川大白菜 (Sichuan Cabbage)
  // ==========================================
  { 
    // 🔴 High: 雨水浸泡 -> 内腐 -> 客诉退货
    level: 'high', typeName: map.high, 
    message: '质量红线：受连日雨水浸泡影响，四川某批次大白菜内芯腐烂率高，存在极高退货风险，请立即停止销售。', 
    time: '2026-01-16 14:10:00', province: '四川省', product: '大白菜' 
  },
  { 
    // 🔵 Low: 竞品断货 -> 独家生意 -> 拉高利润
    level: 'low', typeName: map.low, 
    message: '市场机会：周边市场山东白菜因雪断档，本批四川货源将成市场独家，建议控制出货节奏，拉长高利期。', 
    time: '2026-01-16 14:11:00', province: '四川省', product: '大白菜' 
  },
  { 
    // 🟡 Medium: 山区降雨 -> 采摘受阻 -> 提前备货
    level: 'medium', typeName: map.medium, 
    message: '断货提醒：四川山区未来三天有中雨，采摘受阻，后续发货量将减半，请务必今天把明后天的量备齐。', 
    time: '2026-01-16 14:12:00', province: '四川省', product: '大白菜' 
  },

  // ==========================================
  // Group 3: 河南黄瓜 (Henan Cucumber)
  // ==========================================
  { 
    // 🔴 High: 天气转好 -> 价格跳水 -> 高位接盘风险
    level: 'high', typeName: map.high, 
    message: '高位预警：河南黄瓜前期因雨炒至高价，现天气转好预计价格将大跳水，切勿高价囤货，小心砸在手里。', 
    time: '2026-01-16 14:20:00', province: '河南省', product: '黄瓜' 
  },
  { 
    // 🔵 Low: 设备升级 -> 损耗极低 -> 实际利润高
    level: 'low', typeName: map.low, 
    message: '损耗利好：河南基地启用新预冷设备，发货前去水彻底，到货损耗率预计降低15%，到手净重更多。', 
    time: '2026-01-16 14:21:00', province: '河南省', product: '黄瓜' 
  },
  { 
    // 🟡 Medium: 雾霾封路 -> 晚到货 -> 调整排班
    level: 'medium', typeName: map.medium, 
    message: '物流延迟：河南境内大雾封路，运输车队预计晚到8小时，请通知档口人员调整接货和配送时间。', 
    time: '2026-01-16 14:22:00', province: '河南省', product: '黄瓜' 
  },

  // ==========================================
  // Group 4: 河南大白菜 (Henan Cabbage)
  // ==========================================
  { 
    // 🔴 High: 寒潮冻害 -> 绝收断货 -> 没货卖
    level: 'high', typeName: map.high, 
    message: '寒潮预警：河南产区明后天将迎强降温，田头无法采收，预计断货3天，建议今日不计成本全量锁货。', 
    time: '2026-01-16 14:30:00', province: '河南省', product: '大白菜' 
  },
  { 
    // 🔵 Low: 温差大 -> 口感甜 -> 溢价销售
    level: 'low', typeName: map.low, 
    message: '精品溢价：河南产区昼夜温差大，本批白菜口感极甜，市场稀缺，建议按精品菜定价，每斤可多赚0.3元。', 
    time: '2026-01-16 14:31:00', province: '河南省', product: '大白菜' 
  },
  { 
    // 🟡 Medium: 雪天路滑 -> 装车慢 -> 发货慢
    level: 'medium', typeName: map.medium, 
    message: '发货缓慢：河南产区降雪导致田间装车困难，今日发货速度仅为平时的50%，请耐心等待调度信息。', 
    time: '2026-01-16 14:32:00', province: '河南省', product: '大白菜' 
  }
]);



const alertData = computed(() => {
  const filtered = allAlertData.value.filter(item => {
    if (!props.province && !props.product) return true;
    if (props.province && item.province !== props.province) return false;
    if (props.product && item.product !== props.product) return false;
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