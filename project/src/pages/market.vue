<template>
	<div class="market-container">
		<Head />
		
			<div class="selection-panel">
				<div class="selection-header">
					<div class="title-bar">
						<div class="bar-decor"></div>
						<h2>农产品价格分析</h2>
					</div>
					<div class="selection-description">
						选择农产品查看全国价格趋势，点击年份可查看各省价格详情
					</div>
				</div>
				
				<div class="selection-controls">
					<el-select v-model="selectedProduct" placeholder="请选择农产品" filterable class="product-select">
						<el-option-group v-for="group in productOptions" :key="group.oneLevel" :label="group.oneLevel">
							<el-option
								v-for="item in group.items"
								:key="item.varietyname"
								:label="item.varietyname"
								:value="item.varietyname"
							>
								<span>{{ item.twoLevel }} - {{ item.varietyname }}</span>
							</el-option>
						</el-option-group>
					</el-select>
					
					<!-- AI报告生成器组件 -->
					<!-- <price-report-generator
						v-if="showProvinceView"
						:product-name="selectedProduct"
						:year="selectedYear"
						:province-data="provinceData"
						ref="pdfReportRef"
					/> -->
				</div>
			</div>
			
			<!-- Yearly view (default view) -->
			<div v-if="!showProvinceView" class="yearly-chart">
				<quarter 
					:productName="selectedProduct" 
					@chartClick="handleYearClick"
				/>
			</div>
			
			<!-- Province view (after year click) -->
			<div v-else class="province-view">
				<div class="view-header">
					<el-button @click="showProvinceView = false" size="small" type="primary" icon="el-icon-back">
						返回年度趋势
					</el-button>
					<span class="year-indicator">{{ selectedYear }}年 {{ selectedProduct }} 价格分析</span>
				</div>
				
				<!-- Analytics dashboard layout for province view -->
				<div class="dashboard-grid">
					<!-- Market analysis report -->
					<div class="dashboard-item analytics-report">
						<price-report 
							:productName="selectedProduct" 
							:provinceData="provinceData" 
							:year="selectedYear"
						/>
					</div>
					
					<!-- Price chart visualization -->
					<div class="dashboard-item price-chart">
						<yesterday-province-price 
							:productName="selectedProduct" 
							:year="selectedYear"
							ref="provinceChart"
							@highlight-province="handleChartProvinceHighlight"
							@data-loaded="provinceData = $event"
						/>
					</div>
					
					<!-- Data table -->
					<div class="dashboard-item price-table">
						<price-data-table 
							:productName="selectedProduct"
							:provinceData="provinceData"
							@province-selected="handleProvinceSelect"
						/>
					</div>
				</div>
			</div>
		</div>
	
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import Head from '/src/components/head.vue'
import Quarter from '/src/components/echarts/quarter.vue'
import YesterdayProvincePrice from '/src/components/echarts/yesterdayProvincePrice.vue'
import PriceReport from '/src/components/analysis/PriceReport.vue'
import PriceDataTable from '/src/components/tables/PriceDataTable.vue'
import PriceReportGenerator from '/src/components/reports/PriceReportGenerator.vue'
import { ElMessage } from 'element-plus'
import { getAllVegetableTypes, getVegetableProvincePrice } from '/src/api/requestFuntion.js'

// 状态管理
const selectedProduct = ref('大白菜')
const selectedYear = ref(new Date().getFullYear())
const showProvinceView = ref(false)
const productOptions = ref([])
const provinceData = ref([])
const provinceChart = ref(null)
const pdfReportRef = ref(null)

// 加载蔬菜分类数据
const loadProductOptions = async () => {
	try {
		const response = await getAllVegetableTypes()
		if (response.data && response.data.data) {
			// 重组数据结构为 el-select 所需的分组格式
			const groupedData = {}
			
			response.data.data.forEach(item => {
				if (!groupedData[item.oneLevel]) {
					groupedData[item.oneLevel] = {
						oneLevel: item.oneLevel,
						items: []
					}
				}
				
				groupedData[item.oneLevel].items.push(item)
			})
			
			productOptions.value = Object.values(groupedData)
		}
	} catch (error) {
		console.error('获取农产品分类数据失败：', error)
	}
}

// 获取省份价格数据
const fetchProvinceData = async () => {
	// 显示加载状态
	ElMessage.info({
		message: '加载省份价格数据中...',
		duration: 0,
		showClose: true,
	});
	
	try {
		const response = await getVegetableProvincePrice(selectedProduct.value, selectedYear.value)
		
		// 处理响应数据
		if (response.data && response.data.data) {
			provinceData.value = response.data.data
		} else if (Array.isArray(response.data)) {
			provinceData.value = response.data
		} else {
			provinceData.value = []
			console.warn('API返回的数据结构不符合预期', response.data)
			ElMessage.warning('获取数据格式异常，请稍后再试')
		}
		
		// 关闭loading消息
		ElMessage.closeAll();
		
		// 检查数据是否为空
		if (provinceData.value.length === 0) {
			ElMessage.warning(`未找到${selectedYear.value}年${selectedProduct.value}的省份价格数据`)
		}
	} catch (error) {
		console.error('获取省份价格数据失败:', error)
		provinceData.value = []
		
		// 关闭loading消息
		ElMessage.closeAll();
		
		// 显示错误消息
		if (error.code === 'ECONNABORTED') {
			ElMessage.error('请求超时，服务器响应时间过长')
		} else {
			ElMessage.error('获取省份价格数据失败，请稍后再试')
		}
	}
}

// 处理年份点击事件
const handleYearClick = async (params) => {
	// 从点击的年份中提取年份数值
	const clickedYear = params.name
	
	// 如果是仅年份格式（如"2020"）
	if (/^\d{4}$/.test(clickedYear)) {
		selectedYear.value = parseInt(clickedYear)
	} else {
		// 尝试从其他格式中提取年份（如有其他格式）
		const match = clickedYear.match(/(\d{4})/)
		if (match) {
			selectedYear.value = parseInt(match[1])
		}
	}
	
	// 切换到省份视图
	showProvinceView.value = true
	
	// 获取数据
	await fetchProvinceData()
}

// 处理省份选择
const handleProvinceSelect = (province) => {
	console.log('Selected province:', province)
	// 通过引用调用图表组件的高亮方法
	if (provinceChart.value) {
		provinceChart.value.highlightProvince(province.provinceName)
	}
}

// 当图表中选择了省份时的处理函数
const handleChartProvinceHighlight = (data) => {
	// 在这里可以做其他操作，如显示更详细的省份信息等
	console.log('Chart highlighted province:', data)
}

// 导出PDF报告
const exportPDFReport = () => {
	if (pdfReportRef.value) {
		pdfReportRef.value.exportPDFReport()
	} else {
		ElMessage.error('报告组件未加载')
	}
}

// 监听农产品选择变化
watch(selectedProduct, () => {
	showProvinceView.value = false // 重置视图为年度趋势
})

// 监听省份视图状态变化，确保数据加载
watch(showProvinceView, (newValue) => {
	if (newValue) {
		fetchProvinceData()
	}
})

// 初始化
onMounted(() => {
	loadProductOptions()
})
</script>

<style scoped>
.market-container {
	padding: 0;
	background-color: #f5f7fa;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	width: 100%;
	height: 100vh;
	overflow: hidden;
}

.content-section {
	width: 100%;
	max-width: 100%;
	margin: 0;
	background: white;
	border-radius: 10px;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
	padding: 20px;
	flex: 1;
	display: flex;
	flex-direction: column;
	overflow: auto;
	margin: 15px;
}

.selection-panel {
	margin-bottom: 20px;
	padding: 20px;
	border-radius: 10px;
	background-color: white;
	border: 1px solid #e3f2fd;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
	flex-shrink: 0;
}

.selection-header {
	margin-bottom: 20px;
}

.selection-controls {
	display: flex;
	align-items: center;
	justify-content: space-between;
	flex-wrap: wrap;
	gap: 15px;
}

.title-bar {
	display: flex;
	align-items: center;
	margin-bottom: 12px;
}

.bar-decor {
	width: 4px;
	height: 22px;
	background: linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%);
	border-radius: 2px;
	margin-right: 12px;
}

.selection-header h2 {
	font-size: 20px;
	color: #1565c0;
	margin: 0;
	font-weight: bold;
}

.selection-description {
	font-size: 14px;
	color: #666;
	margin-left: 16px;
}

.product-select {
	width: 320px;
	margin-left: 16px;
}

.yearly-chart {
	flex: 1;
	display: flex;
	flex-direction: column;
	min-height: 450px;
	background: white;
	border-radius: 10px;
	padding: 15px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.province-view {
	flex: 1;
	display: flex;
	flex-direction: column;
	min-height: 550px;
}

.view-header {
	display: flex;
	align-items: center;
	margin-bottom: 20px;
	padding: 15px;
	border-radius: 8px;
	background: linear-gradient(135deg, #f5f7fa 0%, #e3f2fd 100%);
	flex-shrink: 0;
}

.year-indicator {
	margin-left: 15px;
	font-size: 18px;
	color: #1565c0;
	font-weight: bold;
}

/* Dashboard grid layout */
.dashboard-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	grid-template-rows: auto auto;
	gap: 20px;
	grid-template-areas:
		"report chart"
		"table table";
	flex: 1;
	overflow: auto;
}

.dashboard-item {
	background: #fff;
	border-radius: 10px;
	overflow: hidden;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
	height: 100%;
	border: 1px solid #e3f2fd;
}

.analytics-report {
	grid-area: report;
	min-height: 420px;
}

.price-chart {
	grid-area: chart;
	min-height: 420px;
}

.price-table {
	grid-area: table;
	min-height: 350px;
}

/* Responsive adjustments */
@media (max-width: 991px) {
	.dashboard-grid {
		grid-template-columns: 1fr;
		grid-template-rows: auto auto auto;
		grid-template-areas:
			"report"
			"chart"
			"table";
		gap: 15px;
	}
	
	.analytics-report,
	.price-chart {
		min-height: 380px;
	}
}

@media (max-width: 768px) {
	.content-section {
		padding: 15px;
		margin: 10px;
	}
	
	.product-select {
		width: 100%;
		margin-left: 0;
	}
	
	.selection-panel {
		padding: 15px;
	}
	
	.analytics-report,
	.price-chart {
		min-height: 350px;
	}
	
	.price-table {
		min-height: 320px;
	}
	
	.selection-header h2 {
		font-size: 18px;
	}
	
	.year-indicator {
		font-size: 16px;
	}
}

@media (max-width: 576px) {
	.content-section {
		padding: 10px;
		margin: 8px;
	}
	
	.selection-panel {
		padding: 12px;
	}
	
	.view-header {
		padding: 12px;
		flex-direction: column;
		align-items: flex-start;
		gap: 10px;
	}
	
	.year-indicator {
		margin-left: 0;
	}
	
	.dashboard-grid {
		gap: 12px;
	}
	
	.analytics-report,
	.price-chart {
		min-height: 320px;
	}
	
	.price-table {
		min-height: 300px;
	}
}

/* 自定义Element UI组件样式 */
:deep(.el-select .el-input__inner) {
	border-radius: 6px;
	border: 1px solid #e3f2fd;
	height: 40px;
	line-height: 40px;
}

:deep(.el-select .el-input__inner:focus) {
	border-color: #1e88e5;
	box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.2);
}

:deep(.el-select-dropdown) {
	border-radius: 6px;
	border: 1px solid #e3f2fd;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-select-dropdown__item) {
	color: #333;
}

:deep(.el-select-dropdown__item.selected) {
	color: #1e88e5;
	background-color: #e3f2fd;
}

:deep(.el-select-dropdown__item:hover) {
	background-color: #f5f7fa;
}

:deep(.el-button--primary) {
	background: linear-gradient(135deg, #1e88e5 0%, #1565c0 100%);
	border: none;
	border-radius: 6px;
}

:deep(.el-button--primary:hover) {
	background: linear-gradient(135deg, #1976d2 0%, #0d47a1 100%);
	box-shadow: 0 4px 8px rgba(30, 136, 229, 0.3);
}
</style>