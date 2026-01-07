import axios from 'axios'

/**
 * 调用AI服务生成农产品价格分析报告
 * @param {String} productName - 农产品名称
 * @param {Number} year - 分析年份
 * @param {Array} provinceData - 省份价格数据
 * @returns {Promise<String>} - AI生成的报告内容
 */
export async function generatePriceAnalysisReport(productName, year, provinceData) {
  try {
    // 构建分析请求内容
    const content = JSON.stringify({
      productName,
      year,
      provinceData
    })
    
    // 设定AI角色和任务要求
    const systemPrompt = `你是一位农业经济分析专家，擅长撰写农产品价格分析报告。
请根据提供的数据，撰写一份综合性的市场价格分析报告，报告中需包含面向不同群体的分析视角。

报告整体格式要求：
1. 封面标题：年份+产品名+市场价格分析报告
2. 执行摘要：200字以内，概述主要发现，面向决策者，简洁专业的语言

3. 第一部分：专业市场分析（面向研究人员和政策制定者）
   - 使用专业经济学术语和数据分析方法
   - 全国市场供需概况
   - 价格形成机制分析
   - 区域价格差异原因探讨
   - 与上年度价格对比分析
   - 包含详细的数据引用和逻辑推导

4. 第二部分：农户生产建议（面向种植户和农民）
   - 使用简明易懂的语言，少用专业术语
   - 基于价格分析的种植决策建议
   - 最佳销售时机和渠道推荐
   - 区域选择和规模建议
   - 风险防范措施
   - 语气亲切实用，以"您"作为称呼

5. 第三部分：经销商经营策略（面向批发商和零售商）
   - 使用商业术语，注重实用性
   - 进货渠道和时机建议
   - 区域价差套利机会
   - 库存管理策略
   - 价格波动风险应对
   - 语气务实精明，注重商业利益

6. 第四部分：消费者指南（面向终端消费者）
   - 使用日常生活化语言
   - 最佳购买时机和地点
   - 价格与品质关系解读
   - 替代品推荐
   - 储存和食用建议
   - 语气友好亲切，注重实用性

7. 结论与展望：总结全文并对未来价格趋势做出预测

语言要求：根据不同部分面向的对象群体，调整语言风格，但整体保持专业、客观、数据驱动。
报告整体长度：2000-2500字，其中专业市场分析部分篇幅最大。

每个部分请添加明确的标题，并在内容上有所区隔，以便读者能清晰识别不同受众的内容。`

    // 准备请求数据
    const requestData = {
      query: `请基于以下${productName}在${year}年的省级价格数据，生成一份面向多类人群的综合市场价格分析报告：${content}`,
      systemPrompt: systemPrompt,
      temperature: 0.7,
      maxTokens: 3500
    }
    
    // 记录请求数据（仅开发调试用）
    console.log('发送AI请求，系统提示词长度:', systemPrompt.length)
    console.log('发送AI请求，完整请求数据:', JSON.stringify(requestData))
    
    // 发送请求到后端AI接口（修改为完整URL）
    const response = await axios.post('http://47.117.34.210:8080/api/ai/chat', requestData, {
      timeout: 180000, // 增加超时时间到180秒
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    // 记录响应结果
    console.log('AI响应状态:', response.status, response.statusText)
    console.log('AI响应数据:', response.data ? '接收到数据' : '无数据')
    
    // 返回AI生成的内容
    if (response.data && response.data.code === 200) {
      console.log('AI生成成功, 内容长度:', response.data.data.answer.length)
      return response.data.data.answer
    } else {
      console.error('AI响应错误:', response.data)
      throw new Error(response.data?.message || '生成报告失败')
    }
  } catch (error) {
    console.error('AI报告生成失败:', error)
    if (error.code === 'ECONNABORTED') {
      throw new Error('连接AI服务超时，请稍后再试')
    } else if (error.response) {
      console.error('服务器响应错误:', error.response.status, error.response.data)
      throw new Error(`服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`)
    } else if (error.request) {
      console.error('无响应错误:', error.request)
      throw new Error('无法连接到AI服务，请检查网络或服务器状态')
    } else {
      throw error
    }
  }
} 