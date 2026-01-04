<template>
	<div class="chart-container">
		<div class="chart-header">
			<span class="chart-title">各省{{ productName }}价格详情</span>
			<div class="chart-tools">
				<el-tooltip content="刷新数据" placement="top">
					<el-button size="small" icon="el-icon-refresh" circle @click="fetchProvinceData"></el-button>
				</el-tooltip>
			</div>
		</div>
		<div id="yesterdayProvince" ref="chartDom" class="chart-body"></div>
	</div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, watch, ref, defineExpose, onBeforeUnmount } from 'vue'
import { getVegetableProvincePrice } from '/src/api/requestFuntion.js'

// Props from parent component
const props = defineProps({
	productName: {
		type: String,
		default: '大白菜'
	},
	year: {
		type: Number,
		default: () => new Date().getFullYear()
	}
})

// Emits for parent communication
const emit = defineEmits(['data-loaded', 'highlight-province'])

let myChart = null
const provinceData = ref([])
const chartDom = ref(null)

const option = {
	tooltip: {
		trigger: 'axis'
	},
	dataZoom: [
		{
			type: 'slider',
			start: 0,
			end: 80
		},
		{
			start: 0,
			end: 80
		}
	],
	legend: {
		data: ['最高价格', '平均价','最小价格']
	},
	toolbox: {
		show: true,
		feature: {
			dataView: { show: true, readOnly: false },
			magicType: { show: true, type: ['line', 'bar'] },
			restore: { show: true },
			saveAsImage: { show: true }
		}
	},
	calculable: true,
	xAxis: [
		{
			type: 'category',
		}
	],
	yAxis: [
		{
			type: 'value',
			name:'元/公斤'
		}
	],
	series: [
		{
			name: '最高价格',
			type: 'bar',
			barGap:'0%',
			data: [],
			itemStyle: {
				color: '#1e88e5' // 蓝色
			},
			markLine: {
				data: [{ type: 'average', name: 'Avg' }]
			}
		},
		{
			name: '平均价',
			type: 'bar',
			data: [],
			itemStyle: {
				color: '#64b5f6' // 浅蓝色
			},
			markLine: {
				data: [{ type: 'average', name: 'Avg' }]
			}
		},
		{
			name: '最小价格',
			type: 'bar',
			data: [],
			itemStyle: {
				color: '#bbdefb' // 更浅的蓝色
			},
			markLine: {
				data: [{ type: 'average', name: 'Avg' }]
			}
		}
	]
}

// 省份价格数据获取函数
const getDishPricesByProvince = async (dishName, year) => {
	return await getVegetableProvincePrice(dishName, year)
}

const fetchProvinceData = async () => {
	// 添加loading状态
	const loadingInstance = echarts.init(chartDom.value);
	loadingInstance.showLoading('default', {
		text: '加载数据中...',
		color: '#1e88e5', // 蓝色
		textColor: '#1565c0', // 深蓝色
		maskColor: 'rgba(255, 255, 255, 0.8)',
	});
	
	try {
		const response = await getDishPricesByProvince(props.productName, props.year)
		// 确保获取到的是数组数据
		// 后端接口可能返回 { code: "0", data: [...], msg: "操作成功" } 的结构
		if (response.data && response.data.data) {
			// 如果数据是嵌套在data字段中
			provinceData.value = response.data.data
		} else if (Array.isArray(response.data)) {
			// 如果数据直接就是数组
			provinceData.value = response.data
		} else {
			// 如果没有找到有效数据，设置为空数组
			provinceData.value = []
			console.warn('API返回的数据结构不符合预期', response.data)
		}
		
		updateChart()
		
		// 向父组件发送数据加载完成事件
		emit('data-loaded', provinceData.value)
	} catch (error) {
		console.error('获取省份价格数据失败:', error)
		provinceData.value = []
		
		// 显示错误信息
		if (myChart) {
			myChart.hideLoading();
			myChart.clear();
			myChart.setOption({
				title: {
					text: '数据加载失败',
					subtext: '请检查网络连接或稍后再试',
					left: 'center',
					top: 'center',
					textStyle: {
						color: '#1565c0', // 深蓝色
						fontSize: 18
					},
					subtextStyle: {
						color: '#888',
						fontSize: 14
					}
				}
			});
		}
		
		// 向父组件发送空数据，表示加载失败
		emit('data-loaded', []);
	} finally {
		// 无论成功或失败，都隐藏加载动画
		if (loadingInstance && loadingInstance !== myChart) {
			loadingInstance.hideLoading();
		}
		if (myChart) {
			myChart.hideLoading();
		}
	}
}

const updateChart = () => {
	if (!provinceData.value || !Array.isArray(provinceData.value) || provinceData.value.length === 0) {
		console.warn('无法更新图表：省份数据为空或格式不正确')
		return
	}

	try {
		option.series[0].data = provinceData.value.map(e => [e.provinceName.replace("省",""), e.maxPrice])
		option.series[1].data = provinceData.value.map(e => [e.provinceName.replace("省",""), e.avgPrice])
		option.series[2].data = provinceData.value.map(e => [e.provinceName.replace("省",""), e.minPrice])
		
		if (myChart) {
			myChart.setOption(option)
		}
	} catch (error) {
		console.error('更新图表时出错:', error)
		console.log('provinceData值:', provinceData.value)
	}
}

// 高亮显示特定省份
const highlightProvince = (provinceName) => {
	if (!myChart || !provinceName) return
	
	// 清除之前的强调效果
	myChart.dispatchAction({
		type: 'downplay'
	})
	
	// 查找省份对应的序号
	const provinceIndex = provinceData.value.findIndex(
		p => p.provinceName.replace("省","") === provinceName.replace("省","")
	)
	
	if (provinceIndex !== -1) {
		// 高亮该省份的所有系列
		myChart.dispatchAction({
			type: 'highlight',
			seriesIndex: [0, 1, 2],
			dataIndex: provinceIndex
		})
		
		// 将视图定位到该省份
		myChart.dispatchAction({
			type: 'showTip',
			seriesIndex: 0,
			dataIndex: provinceIndex
		})
	}
}

const initChart = () => {
	if (!chartDom.value) return
	
	if (myChart) {
		myChart.dispose()
	}
	
	myChart = echarts.init(chartDom.value)
	
	// 添加点击事件监听
	myChart.on('click', (params) => {
		const provinceName = params.name
		emit('highlight-province', {
			provinceName,
			seriesName: params.seriesName
		})
	})
	
	updateChart()
}

// 响应窗口大小变化
const resizeHandler = () => {
	if (myChart) {
		myChart.resize()
	}
}

// Watch for changes in props to update chart
watch(
	() => props.year,
	() => fetchProvinceData(),
	{ immediate: true }
)

watch(
	() => props.productName,
	() => fetchProvinceData()
)

onMounted(() => {
	setTimeout(() => {
		initChart()
		fetchProvinceData()
		
		// 添加窗口大小变化监听
		window.addEventListener('resize', resizeHandler)
	}, 100)
})

onBeforeUnmount(() => {
	// 移除窗口大小变化监听
	window.removeEventListener('resize', resizeHandler)
	
	// 释放图表资源
	if (myChart) {
		myChart.dispose()
		myChart = null
	}
})

// 对外暴露方法，允许父组件调用
defineExpose({
	highlightProvince,
	fetchProvinceData,
	getProvinceData: () => provinceData.value
})
</script>

<style scoped>
.chart-container {
	background-color: #fff;
	border-radius: 10px;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
	padding: 16px;
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.chart-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16px;
	flex-shrink: 0;
}

.chart-title {
	font-size: 16px;
	font-weight: 600;
	color: #1565c0; /* 深蓝色 */
}

.chart-body {
	width: 100%;
	flex: 1;
	min-height: 300px;
}

@media (max-width: 992px) {
	.chart-container {
		padding: 12px;
	}
	
	.chart-body {
		min-height: 250px;
	}
}

@media (max-width: 768px) {
	.chart-container {
		padding: 10px;
	}
	
	.chart-title {
		font-size: 14px;
	}
	
	.chart-body {
		min-height: 200px;
	}
}

@media (max-width: 576px) {
	.chart-container {
		padding: 8px;
	}
}
</style>