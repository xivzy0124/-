<template>
  <div class="pdf-generator">
    <div v-if="isGenerating" class="generating-indicator">
      <el-progress type="circle" :percentage="generatingProgress"></el-progress>
      <p>正在生成报告，请稍候...</p>
    </div>
    
    <!-- 报告预览区域 -->
    <div ref="reportContent" class="report-template" v-show="false">
      <!-- 报告头部 -->
      <div class="report-header">
        <h1 class="report-title">{{ reportTitle }}</h1>
        <div class="report-meta">
          <p class="report-date">报告日期: {{ formattedDate }}</p>
        </div>
      </div>
      
      <!-- 摘要部分 -->
      <div class="report-abstract">
        <h2>摘要</h2>
        <p>本报告对{{ year }}年{{ productName }}的价格进行了分析。通过对各省份交易数据的统计分析，展示了{{ productName }}在全国范围内的价格分布、波动趋势及区域差异，为农产品市场监管和价格预测提供数据支持。</p>
      </div>
      
      <!-- 关键词部分 -->
      <div class="keywords">
        <p><strong>关键词：</strong>{{ productName }}; 价格分析; 区域差异; 农产品市场; {{ year }}年</p>
      </div>
      
      <!-- 正文部分 -->
      <div class="report-body">
        <h2>1. 引言</h2>
        <p>随着我国农业现代化进程的加快，农产品价格的波动和区域差异一直是市场关注的焦点。本报告通过对{{ year }}年{{ productName }}的价格数据进行全面分析，旨在了解其市场分布特点、价格变动规律，为相关部门制定政策和市场参与者决策提供参考。</p>
        
        <h2>2. 数据来源与方法</h2>
        <p>本报告的数据来源于全国农产品批发市场信息系统，采集了{{ year }}年各省市{{ productName }}的交易价格数据。分析方法主要包括描述性统计和比较分析，通过计算各省份的最高价格、平均价格和最低价格，展示{{ productName }}价格的区域差异。</p>
        
        <h2>3. {{ productName }}价格分析</h2>
        
        <h3>3.1 全国各省价格概览</h3>
        <div class="data-table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>省份</th>
                <th>最高价(元/公斤)</th>
                <th>平均价(元/公斤)</th>
                <th>最低价(元/公斤)</th>
                <th>价格波动</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(province, index) in provinceData" :key="index">
                <td>{{ province.provinceName }}</td>
                <td>{{ formatPrice(province.maxPrice) }}</td>
                <td>{{ formatPrice(province.avgPrice) }}</td>
                <td>{{ formatPrice(province.minPrice) }}</td>
                <td>{{ calculateFluctuation(province) }}%</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <h3>3.2 价格分布与区域差异</h3>
        <p>从上述数据可以看出，{{ productName }}在不同省份的价格存在明显差异。{{ highPriceAnalysis }} {{ lowPriceAnalysis }}</p>
        
        <h3>3.3 价格形成因素分析</h3>
        <p>{{ productName }}的价格差异主要受以下因素影响：</p>
        <ol>
          <li><strong>地理位置与种植条件：</strong>{{ productName }}的生长需要特定的自然条件，产地与销售地的距离影响运输成本。</li>
          <li><strong>供需关系：</strong>各地区消费习惯和市场需求不同，导致供需平衡点存在差异。</li>
          <li><strong>市场结构：</strong>批发市场的数量、规模和竞争程度会影响价格形成。</li>
          <li><strong>季节性因素：</strong>{{ productName }}的生产具有季节性，不同地区的产季有所不同。</li>
        </ol>
        
        <h2>4. 结论与建议</h2>
        <p>通过对{{ year }}年{{ productName }}价格的分析，我们得出以下结论：</p>
        <ol>
          <li>{{ productName }}全国平均价格为{{ formatPrice(averagePrice) }}元/公斤，最高省份是{{ highestProvince.provinceName }}（{{ formatPrice(highestProvince.avgPrice) }}元/公斤），最低省份是{{ lowestProvince.provinceName }}（{{ formatPrice(lowestProvince.avgPrice) }}元/公斤）。</li>
          <li>价格的区域差异主要受到物流成本、市场结构和消费习惯的影响。</li>
          <li>{{ priceConclusion }}</li>
        </ol>
        
        <p><strong>建议：</strong></p>
        <ol>
          <li>加强区域间物流基础设施建设，降低运输成本。</li>
          <li>完善农产品市场信息系统，提高价格透明度。</li>
          <li>推动产销区合作，建立稳定的供应链关系。</li>
          <li>鼓励发展保鲜技术，延长{{ productName }}的供应期。</li>
        </ol>
        
        <h2>5. 参考文献</h2>
        <ol class="references">
          <li>中国农产品批发市场统计年鉴({{ year }})</li>
          <li>农业农村部市场与信息化司. {{ productName }}产业发展报告({{ year }})</li>
          <li>国家统计局. {{ year }}年全国农产品价格指数分析</li>
        </ol>
      </div>
      
      <!-- 报告脚注 -->
      <div class="report-footer">
        <p>注：本报告基于公开数据分析生成，仅供参考。数据来源于全国农产品批发市场系统。</p>
        <p class="page-number">第<span class="page"></span>页</p>
      </div>
    </div>
  </div>
</template>

<script>
import { jsPDF } from 'jspdf'
import html2canvas from 'html2canvas'

export default {
  name: 'PriceReportPDF',
  props: {
    productName: { type: String, default: '大白菜' },
    provinceData: { type: Array, default: () => [] },
    year: { type: Number, default: () => new Date().getFullYear() }
  },
  data() {
    return {
      isGenerating: false,
      generatingProgress: 0
    }
  },
  computed: {
    reportTitle() {
      return `${this.year}年${this.productName}价格分析报告`
    },
    formattedDate() {
      const now = new Date()
      return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
    },
    averagePrice() {
      if (!this.provinceData || this.provinceData.length === 0) return 0
      const sum = this.provinceData.reduce((acc, curr) => acc + curr.avgPrice, 0)
      return sum / this.provinceData.length
    },
    highestProvince() {
      if (!this.provinceData || this.provinceData.length === 0) 
        return { provinceName: '未知', avgPrice: 0 }
      
      return this.provinceData.reduce((max, curr) => 
        max.avgPrice > curr.avgPrice ? max : curr, 
        { provinceName: '', avgPrice: 0 }
      )
    },
    lowestProvince() {
      if (!this.provinceData || this.provinceData.length === 0)
        return { provinceName: '未知', avgPrice: 0 }
      
      return this.provinceData.reduce((min, curr) => 
        (!min.avgPrice || min.avgPrice > curr.avgPrice) ? curr : min, 
        { provinceName: '', avgPrice: Number.MAX_VALUE }
      )
    },
    highPriceAnalysis() {
      if (!this.provinceData || this.provinceData.length === 0) return ''
      
      const highPriceProvinces = [...this.provinceData]
        .sort((a, b) => b.avgPrice - a.avgPrice)
        .slice(0, 3)
        .map(p => p.provinceName)
        .join('、')
      
      return `价格较高的地区主要集中在${highPriceProvinces}等省份，这些地区可能受到市场需求大、物流成本高等因素影响。`
    },
    lowPriceAnalysis() {
      if (!this.provinceData || this.provinceData.length === 0) return ''
      
      const lowPriceProvinces = [...this.provinceData]
        .sort((a, b) => a.avgPrice - b.avgPrice)
        .slice(0, 3)
        .map(p => p.provinceName)
        .join('、')
      
      return `而${lowPriceProvinces}等省份价格较低，可能是因为这些地区是${this.productName}的主产区，供应充足。`
    },
    priceConclusion() {
      if (!this.provinceData || this.provinceData.length === 0) return ''
      
      const fluctuations = this.provinceData.map(p => this.calculateFluctuation(p))
      const avgFluctuation = fluctuations.reduce((sum, val) => sum + parseFloat(val), 0) / fluctuations.length
      
      return `${this.productName}价格在各省份内部波动平均为${avgFluctuation.toFixed(2)}%，表明市场供需关系总体较为稳定，但仍需关注季节性价格变动。`
    }
  },
  methods: {
    formatPrice(price) {
      return parseFloat(price).toFixed(2)
    },
    calculateFluctuation(province) {
      if (!province.minPrice || province.minPrice === 0) return "0.00"
      const fluctuation = ((province.maxPrice - province.minPrice) / province.minPrice) * 100
      return fluctuation.toFixed(2)
    },
    async generatePDF() {
      if (!this.$refs.reportContent) return null
      
      try {
        this.isGenerating = true
        this.generatingProgress = 10
        
        // 创建PDF文档
        const pdf = new jsPDF('p', 'pt', 'a4')
        const pdfWidth = pdf.internal.pageSize.getWidth()
        const pdfHeight = pdf.internal.pageSize.getHeight()
        
        // 获取HTML内容
        const content = this.$refs.reportContent
        
        // 创建Canvas
        this.generatingProgress = 30
        const canvas = await html2canvas(content, {
          scale: 2,
          useCORS: true,
          logging: false,
          allowTaint: true
        })
        
        this.generatingProgress = 60
        
        // 计算内容高度和页数
        const contentWidth = canvas.width
        const contentHeight = canvas.height
        const ratio = contentWidth / pdfWidth
        const totalPages = Math.ceil(contentHeight / (pdfHeight * ratio))
        
        // 分页添加到PDF
        let position = 0
        
        for (let i = 0; i < totalPages; i++) {
          if (i > 0) {
            pdf.addPage()
          }
          
          const srcHeight = pdfHeight * ratio
          const imageData = canvas.toDataURL('image/jpeg', 1.0)
          
          // 计算当前页的高度
          const remainHeight = contentHeight - position
          const currentHeight = Math.min(remainHeight, srcHeight)
          
          // 添加内容到PDF
          pdf.addImage(
            imageData, 
            'JPEG', 
            0, 
            0,
            pdfWidth, 
            currentHeight / ratio,
            null,
            'FAST',
            0,
            -position / ratio
          )
          
          // 移动位置到下一页
          position += srcHeight
          
          this.generatingProgress = 60 + (i + 1) / totalPages * 35
        }
        
        this.generatingProgress = 95
        
        // 最终处理
        setTimeout(() => {
          this.generatingProgress = 100
          this.isGenerating = false
        }, 500)
        
        return pdf
      } catch (error) {
        console.error('生成PDF报告出错:', error)
        this.isGenerating = false
        return null
      }
    },
    async exportPDF() {
      console.log("exportPDF method called");
      const pdf = await this.generatePDF();
      if (pdf) {
        pdf.save(`${this.productName}_${this.year}年价格分析报告.pdf`);
        return true;
      }
      return false;
    }
  }
}
</script>

<style scoped>
.pdf-generator {
  width: 100%;
  height: 100%;
  position: relative;
}

.generating-indicator {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  z-index: 100;
}

.report-template {
  width: 21cm;
  padding: 2cm;
  margin: 0 auto;
  background: white;
  font-family: "Times New Roman", SimSun, serif;
  line-height: 1.5;
  color: #333;
}

.report-header {
  text-align: center;
  margin-bottom: 2cm;
}

.report-title {
  font-size: 20pt;
  font-weight: bold;
  margin-bottom: 0.5cm;
  color: #1565c0;
}

.report-meta {
  font-size: 12pt;
  margin-top: 1cm;
}

.report-abstract {
  margin-bottom: 1cm;
  text-indent: 2em;
}

.keywords {
  margin-bottom: 1.5cm;
}

.report-body h2 {
  font-size: 14pt;
  font-weight: bold;
  margin: 1cm 0 0.5cm;
  color: #1e88e5;
}

.report-body h3 {
  font-size: 12pt;
  font-weight: bold;
  margin: 0.8cm 0 0.4cm;
  color: #1e88e5;
}

.report-body p {
  text-indent: 2em;
  margin-bottom: 0.5cm;
}

.report-body ol, .report-body ul {
  margin-left: 2em;
  margin-bottom: 0.5cm;
}

.report-body li {
  margin-bottom: 0.2cm;
}

.data-table-container {
  margin: 1cm 0;
  width: 100%;
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ddd;
}

.data-table th, .data-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}

.data-table th {
  background: linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%);
  color: white;
  font-weight: bold;
}

.data-table tr:nth-child(even) {
  background-color: #f5f7fa;
}

.references li {
  margin-bottom: 0.3cm;
}

.report-footer {
  margin-top: 2cm;
  font-size: 10pt;
  color: #666;
  text-align: center;
}

.page-number {
  text-align: center;
  margin-top: 0.5cm;
}
</style>