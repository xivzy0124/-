<template>
	<div>
		<!-- 导出按钮 -->
		<el-button 
			type="primary" 
			size="small" 
			icon="el-icon-document"
			@click="exportPDFReport" 
			:loading="isSaving">
			导出多角色分析报告 (PDF)
		</el-button>
	</div>
</template>

<script setup>
import { ref } from 'vue'
import { jsPDF } from 'jspdf'
import { ElMessage } from 'element-plus'
import { generatePriceAnalysisReport } from '/src/api/aiService.js'
import 'jspdf-autotable' // 导入自动表格插件，支持更好的布局

// 导入中文字体支持 - 这需要先安装字体
// npm install --save jspdf-font

const props = defineProps({
	productName: {
		type: String,
		required: true
	},
	year: {
		type: Number,
		required: true
	},
	provinceData: {
		type: Array,
		required: true
	}
})

// 状态管理
const isSaving = ref(false)

// 导出PDF报告
const exportPDFReport = async () => {
	try {
		isSaving.value = true
		ElMessage.info('正在通过AI生成多角色分析报告，请稍候...')
		
		// 调用AI服务生成报告内容
		const reportContent = await generatePriceAnalysisReport(
			props.productName,
			props.year,
			props.provinceData
		)
		
		// 如果没有获取到报告内容，则退出
		if (!reportContent) {
			ElMessage.error('AI未能生成报告内容')
			return
		}
		
		// 打印原始AI回复内容到控制台以便调试
		console.log('================ AI原始回复内容开始 ================');
		console.log(reportContent);
		console.log('================ AI原始回复内容结束 ================');
		
		// 处理和清理报告内容，去除Markdown标记
		const cleanedContent = cleanMarkdownFormatting(reportContent)
		
		ElMessage.info('内容生成完成，正在创建PDF文件...')
		
		// 创建使用Canvas创建PDF，支持中文
		// 注意：此方法可能会导致文本无法选择，但能正确显示中文
		const canvas = document.createElement('canvas');
		const ctx = canvas.getContext('2d');
		
		// 设置Canvas大小为A4纸张大小 (提高分辨率)
		const scale = 2.0; // 增加分辨率的比例因子
		const pageWidth = 595 * scale;
		const pageHeight = 842 * scale;
		canvas.width = pageWidth;
		canvas.height = pageHeight;
		
		// 绘制白色背景
		ctx.fillStyle = 'white';
		ctx.fillRect(0, 0, canvas.width, canvas.height);
		ctx.scale(scale, scale); // 应用缩放比例以提高字体清晰度
		
		// 转换为PDF
		const pdf = new jsPDF('p', 'pt', 'a4');
		
		// 使用Canvas添加内容
		drawReportToCanvas(canvas, cleanedContent, props.productName, props.year);
		
		// 将Canvas添加到PDF (高质量设置)
		const imgData = canvas.toDataURL('image/jpeg', 1.0);
		pdf.addImage(imgData, 'JPEG', 0, 0, 595, 842);
		
		// 如果需要多页
		const sections = splitContentIntoSections(cleanedContent);
		let currentPage = 1;
		const sectionsPerPage = 2; // 每页包含的段落数，根据实际情况调整
		
		for (let i = sectionsPerPage; i < sections.length; i += sectionsPerPage) {
			// 创建新的Canvas (保持高分辨率)
			const newCanvas = document.createElement('canvas');
			newCanvas.width = pageWidth;
			newCanvas.height = pageHeight;
			const newCtx = newCanvas.getContext('2d');
			
			// 绘制白色背景
			newCtx.fillStyle = 'white';
			newCtx.fillRect(0, 0, newCanvas.width, newCanvas.height);
			newCtx.scale(scale, scale); // 应用缩放比例
			
			// 绘制内容
			drawPageToCanvas(newCanvas, sections.slice(i, i + sectionsPerPage), currentPage + 1);
			currentPage++;
			
			// 添加新页
			pdf.addPage();
			const newImgData = newCanvas.toDataURL('image/jpeg', 1.0);
			pdf.addImage(newImgData, 'JPEG', 0, 0, 595, 842);
		}
		
		// 保存PDF
		pdf.save(`${props.productName}_${props.year}年多角色分析报告.pdf`);
		ElMessage.success('多角色分析报告已成功导出');
		
	} catch (error) {
		console.error('导出PDF报告出错:', error)
		ElMessage.error('报告生成过程中发生错误: ' + error.message)
	} finally {
		isSaving.value = false
	}
}

// 清理Markdown格式，处理AI生成内容中的特殊字符
const cleanMarkdownFormatting = (text) => {
	if (!text) return '';
	
	// 替换Markdown标题
	let cleaned = text
		.replace(/#{1,6}\s+/g, '') // 移除标题的 # 符号
		.replace(/\*\*/g, '') // 移除加粗的 ** 符号
		.replace(/\*/g, '') // 移除斜体的 * 符号
		.replace(/`{1,3}/g, '') // 移除代码块的 ` 符号
		.replace(/---+/g, '') // 移除分隔线
		.replace(/\n\s*\n/g, '\n\n') // 规范化空行
		.replace(/\[([^\]]+)\]\([^\)]+\)/g, '$1') // 将链接 [text](url) 转换为纯文本
		.replace(/~~([^~]+)~~/g, '$1') // 移除删除线
		.replace(/>\s+/g, '') // 移除引用符号 >
		.replace(/\||\+|---+|\|\s*\n/g, ' ') // 清理表格字符
	
	return cleaned;
}

// 将内容分成有意义的段落
const splitContentIntoSections = (text) => {
	if (!text) return [];
	
	// 根据部分标题和段落拆分内容
	const sections = [];
	const lines = text.split('\n');
	
	let currentSection = '';
	for (const line of lines) {
		if (line.trim() === '') {
			if (currentSection.trim() !== '') {
				sections.push(currentSection.trim());
				currentSection = '';
			}
			continue;
		}
		
		// 检测主要段落标题
		if (line.includes('部分') || 
			line.includes('执行摘要') || 
			line.includes('结论与展望')) {
			
			if (currentSection.trim() !== '') {
				sections.push(currentSection.trim());
			}
			currentSection = line;
		} else {
			currentSection += (currentSection ? '\n' : '') + line;
		}
	}
	
	// 添加最后一个段落
	if (currentSection.trim() !== '') {
		sections.push(currentSection.trim());
	}
	
	return sections;
}

// 在Canvas上绘制报告内容（第一页）
const drawReportToCanvas = (canvas, content, productName, year) => {
	const ctx = canvas.getContext('2d');
	const margin = 40;
	
	// 设置标题样式
	ctx.font = 'bold 24px "Microsoft YaHei", SimHei, Arial'; // 优先使用中文字体
	ctx.fillStyle = '#1565c0';
	
	// 标题居中
	const title = `${productName} ${year}年价格分析报告`;
	const titleWidth = ctx.measureText(title).width;
	ctx.fillText(title, (canvas.width/2 - titleWidth) / 2, margin + 40);
	
	// 副标题
	ctx.font = '14px "Microsoft YaHei", SimHei, Arial';
	ctx.fillStyle = '#1e88e5';
	const subtitle = `生成日期: ${new Date().toLocaleDateString()}`;
	const subtitleWidth = ctx.measureText(subtitle).width;
	ctx.fillText(subtitle, (canvas.width/2 - subtitleWidth) / 2, margin + 80);
	
	// 说明文字
	ctx.font = 'italic 12px "Microsoft YaHei", SimHei, Arial';
	ctx.fillStyle = '#666';
	const note = '(本报告包含面向研究人员、农户、经销商和消费者的市场分析)';
	const noteWidth = ctx.measureText(note).width;
	ctx.fillText(note, (canvas.width/2 - noteWidth) / 2, margin + 110);
	
	// 添加内容摘要
	ctx.font = '12px "Microsoft YaHei", SimHei, Arial';
	ctx.fillStyle = '#333';
	
	// 每行最多字符数根据字体大小调整
	const maxCharsPerLine = 60;
	let y = margin + 150;
	
	// 摘要部分
	const sections = splitContentIntoSections(content);
	if (sections.length > 0) {
		// 尝试找到执行摘要部分
		const summarySection = sections.find(s => s.includes('执行摘要')) || sections[0];
		const lines = splitTextIntoLines(summarySection, maxCharsPerLine);
		
		for (const line of lines.slice(0, 15)) { // 限制摘要行数
			ctx.fillText(line, margin, y);
			y += 20;
			
			if (y > canvas.height/2 - margin) break; // 防止超出页面
		}
	}
	
	// 添加页码
	ctx.font = '10px "Microsoft YaHei", SimHei, Arial';
	ctx.fillStyle = '#1e88e5';
	ctx.fillText(`第 1 页`, canvas.width/2 - 60, canvas.height/2 - 20);
	ctx.fillText(`农产品价格分析系统`, margin, canvas.height/2 - 20);
}

// 将文本分割成适合显示的行
const splitTextIntoLines = (text, maxCharsPerLine) => {
	if (!text) return [];
	
	const lines = [];
	const paragraphs = text.split('\n');
	
	for (const paragraph of paragraphs) {
		if (paragraph.trim() === '') {
			lines.push('');
			continue;
		}
		
		for (let i = 0; i < paragraph.length; i += maxCharsPerLine) {
			lines.push(paragraph.slice(i, i + maxCharsPerLine));
		}
	}
	
	return lines;
}

// 绘制内容页
const drawPageToCanvas = (canvas, sections, pageNum) => {
	const ctx = canvas.getContext('2d');
	const margin = 40;
	
	// 设置标题样式
	ctx.font = 'bold 16px "Microsoft YaHei", SimHei, Arial';
	ctx.fillStyle = '#1565c0';
	
	// 分析报告正文标题
	if (pageNum === 2) {
		ctx.fillText('详细分析报告', margin, margin + 30);
	}
	
	// 正文内容
	ctx.font = '12px "Microsoft YaHei", SimHei, Arial';
	ctx.fillStyle = '#333';
	let y = margin + 60;
	
	for (const section of sections) {
		// 检测是否是主要标题行
		const firstLine = section.split('\n')[0];
		
		// 标题行处理
		if (firstLine.includes('部分') || 
			firstLine.includes('执行摘要') || 
			firstLine.includes('结论与展望')) {
			
			ctx.font = 'bold 14px "Microsoft YaHei", SimHei, Arial';
			
			// 添加标题颜色
			if (firstLine.includes('专业市场分析')) {
				ctx.fillStyle = '#1e88e5'; // 蓝色
			} else if (firstLine.includes('农户生产建议')) {
				ctx.fillStyle = '#1e88e5'; // 蓝色
			} else if (firstLine.includes('经销商经营策略')) {
				ctx.fillStyle = '#1e88e5'; // 蓝色
			} else if (firstLine.includes('消费者指南')) {
				ctx.fillStyle = '#1e88e5'; // 蓝色
			} else if (firstLine.includes('结论与展望')) {
				ctx.fillStyle = '#1565c0'; // 深蓝色
			} else if (firstLine.includes('执行摘要')) {
				ctx.fillStyle = '#1565c0'; // 深蓝色
			} else {
				ctx.fillStyle = '#1565c0'; // 默认深蓝色
			}
			
			ctx.fillText(firstLine, margin, y);
			y += 25;
			
			// 恢复正文样式
			ctx.font = '12px "Microsoft YaHei", SimHei, Arial';
			ctx.fillStyle = '#333';
			
			// 处理标题后的内容
			const contentLines = section.split('\n').slice(1);
			if (contentLines.length > 0) {
				for (const line of splitTextIntoLines(contentLines.join('\n'), 60)) {
					ctx.fillText(line, margin, y);
					y += 20;
					
					if (y > canvas.height/2 - margin) break; // 防止超出页面
				}
			}
		} else {
			// 普通段落处理
			for (const line of splitTextIntoLines(section, 60)) {
				ctx.fillText(line, margin, y);
				y += 20;
				
				if (y > canvas.height/2 - margin) break; // 防止超出页面
			}
		}
		
		y += 15; // 段落间距
	}
	
	// 添加页码
	ctx.font = '10px "Microsoft YaHei", SimHei, Arial';
	ctx.fillStyle = '#1e88e5';
	ctx.fillText(`第 ${pageNum} 页`, canvas.width/2 - 60, canvas.height/2 - 20);
	ctx.fillText(`农产品价格分析系统`, margin, canvas.height/2 - 20);
}

// 将组件方法暴露给父组件
defineExpose({
	exportPDFReport
})
</script> 

<style scoped>
/* 按钮样式 */
.el-button {
	margin: 0 10px;
	background: linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%);
	border: none;
	border-radius: 6px;
	box-shadow: 0 2px 8px rgba(30, 136, 229, 0.3);
	transition: all 0.3s ease;
}

.el-button:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 12px rgba(30, 136, 229, 0.4);
}

.el-button:active {
	transform: translateY(0);
}
</style>