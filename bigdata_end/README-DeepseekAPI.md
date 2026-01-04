# DeepSeek AI API 集成

本项目集成了 DeepSeek 的 AI API 服务，为前端提供了简单易用的聊天接口。

## 配置说明

### 修改 application.yaml

在 `application.yaml` 中添加以下配置：

```yaml
deepseek:
  api:
    key: "your-deepseek-api-key"  # 替换为实际的DeepSeek API Key
    base-url: "https://api.deepseek.com"
    model: "deepseek-chat"  # 默认使用DeepSeek-V3模型
    temperature: 0.7  # 控制生成文本的随机性
    max-tokens: 2000  # 最大生成token数
```

## API 接口说明

### 1. 聊天接口

- **URL:** `/api/ai/chat`
- **Method:** POST
- **Content-Type:** application/json

#### 请求参数

```json
{
  "query": "你好，请介绍一下农副产品价格的影响因素",
  "systemPrompt": "你是一个农业专家，精通农副产品市场分析。请以专业的口吻回答问题。",
  "history": [
    {
      "role": "user", 
      "content": "农产品包括哪些类别？"
    },
    {
      "role": "assistant", 
      "content": "农产品主要包括粮食作物、经济作物、蔬菜水果、畜禽产品、水产品等类别。"
    }
  ],
  "temperature": 0.7,
  "maxTokens": 2000,
  "stream": false
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| query | String | 是 | 用户提问内容 |
| systemPrompt | String | 否 | 系统提示语，设定AI的角色和行为方式 |
| history | Array | 否 | 历史对话记录，包含role和content字段 |
| temperature | Double | 否 | 温度参数，控制回答的随机性，越高越随机 |
| maxTokens | Integer | 否 | 最大生成token数 |
| stream | Boolean | 否 | 是否使用流式返回 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "answer": "农副产品价格的影响因素主要包括以下几个方面：\n\n1. 供需关系：这是决定价格的基本因素。当供应减少或需求增加时，价格上涨；反之则下降。\n\n2. 季节性因素：许多农产品具有明显的季节性生产特点，导致价格呈现季节性波动。\n\n3. 天气和自然灾害：干旱、洪水、病虫害等会影响产量和质量，进而影响价格。\n\n4. 生产成本：包括种子、肥料、人工、土地成本等，这些成本的变动会传导到最终产品价格。\n\n5. 政策因素：如农业补贴政策、最低收购价政策等，都会对价格形成产生影响。\n\n6. 国际市场：全球农产品贸易和国际市场价格变动也会影响国内价格。\n\n7. 物流和储存：运输成本和储存条件也是影响价格的重要因素。\n\n8. 消费者偏好：消费习惯和偏好的变化会影响需求，进而影响价格。",
    "tokenUsage": 421
  }
}
```

| 字段名 | 类型 | 说明 |
|-------|------|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 状态信息 |
| data.answer | String | AI回答内容 |
| data.tokenUsage | Integer | 本次对话消耗的token数量 |

### 2. 流式聊天接口

- **URL:** `/api/ai/chat/stream`
- **Method:** POST
- **Content-Type:** application/json
- **Response:** text/event-stream

#### 请求参数

与普通聊天接口相同，但会忽略请求中的stream参数，强制使用流式返回。

#### 响应说明

流式接口使用 Server-Sent Events (SSE) 格式返回数据。客户端会收到多个事件，每个事件包含部分回答内容。

当完成回答后，会收到一个名为 `complete` 的事件，其数据为本次对话消耗的token数量。

### 3. 健康检查接口

- **URL:** `/api/ai/health`
- **Method:** GET

用于检查AI服务是否正常运行。

## 前端调用示例

### 使用axios进行普通请求

```javascript
import axios from 'axios';

async function chatWithAI() {
  try {
    const response = await axios.post('/api/ai/chat', {
      query: '你好，请介绍一下农副产品价格的影响因素',
      systemPrompt: '你是一个农业专家，精通农副产品市场分析。',
      temperature: 0.7
    });
    
    console.log(response.data.data.answer);
  } catch (error) {
    console.error('AI聊天请求失败:', error);
  }
}
```

### 使用EventSource处理流式请求

```javascript
function streamChatWithAI() {
  const requestData = {
    query: '你好，请介绍一下农副产品价格的影响因素',
    systemPrompt: '你是一个农业专家，精通农副产品市场分析。'
  };
  
  // 首先发送POST请求以获取EventSource URL
  fetch('/api/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestData)
  }).then(response => {
    const eventSource = new EventSource(response.url);
    let fullResponse = '';
    
    // 普通消息事件
    eventSource.onmessage = (event) => {
      const chunk = event.data;
      fullResponse += chunk;
      // 实时更新UI
      document.getElementById('response').textContent = fullResponse;
    };
    
    // 完成事件
    eventSource.addEventListener('complete', (event) => {
      const tokenUsage = parseInt(event.data);
      console.log(`回答完成，消耗token: ${tokenUsage}`);
      eventSource.close();
    });
    
    // 错误处理
    eventSource.onerror = (error) => {
      console.error('流式处理错误:', error);
      eventSource.close();
    };
  }).catch(error => {
    console.error('启动流式对话失败:', error);
  });
}
``` 