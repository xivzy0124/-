<template>
  <div class="floating-chat-wrapper" 
       :style="transformStyle"
       @mousedown="handleMouseDown"
       @transitionend="handleTransitionEnd">
    <div class="floating-chat" 
         :class="{ 'expanded': isExpanded }">
      <!-- Floating Ball -->
      <div class="floating-ball" 
           v-if="!isExpanded"
           @click="expandChat">
        <el-icon><ChatDotRound /></el-icon>
      </div>

      <!-- Chat Interface -->
      <div class="chat-container" 
           v-show="isExpanded" 
           :style="containerStyle">
        <div class="chat-header">
          <span>AI 助手</span>
          <div class="header-actions">
            <el-button type="text" @click.stop="minimizeChat">
              <el-icon><Minus /></el-icon>
            </el-button>
            <el-button type="text" @click.stop="closeChat">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <div v-for="(message, index) in messages" :key="index" 
               :class="['message', message.type]">
            <div class="message-content" v-if="!message.isChart" v-html="message.content"></div>
            <div class="message-content chart-content" v-else>
              <div v-html="message.content.replace(/<script[\s\S]*?<\/script>/g, '')"></div>
              <div :id="'chart-' + index" class="chart-container"></div>
            </div>
          </div>
          <div v-if="isLoading" class="message ai">
            <div class="message-content">
              <el-skeleton :rows="3" animated />
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="userInput"
            type="textarea"
            :rows="2"
            placeholder="请输入您的问题..."
            @keyup.enter.ctrl="sendMessage"
          />
          <el-button type="primary" @click="sendMessage" :loading="isLoading">
            发送
          </el-button>
        </div>
      </div>
    </div>

    <!-- 调整大小的手柄 -->
    <div class="resize-handle resize-handle-e" @mousedown.stop="startResize('e')"></div>
    <div class="resize-handle resize-handle-s" @mousedown.stop="startResize('s')"></div>
    <div class="resize-handle resize-handle-se" @mousedown.stop="startResize('se')"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, computed, watch } from 'vue'
import { ChatDotRound, Close, Minus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const isExpanded = ref(false)
const userInput = ref('')
const messages = ref([])
const isLoading = ref(false)
const messagesContainer = ref(null)
const position = ref({ x: 0, y: 0 })
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })
const isResizing = ref(false)
const resizeDirection = ref('')
const startSize = ref({ width: 400, height: 600 })
const currentSize = ref({ width: 400, height: 600 })
const dragStartPosition = ref({ x: 0, y: 0 })
const isTransitioning = ref(false)
const dragStartTime = ref(0)

// 计算transform样式
const transformStyle = computed(() => ({
  transform: `translate(${position.value.x}px, ${position.value.y}px)`,
  transition: isTransitioning.value ? 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)' : 'none'
}))

// 计算容器样式
const containerStyle = computed(() => ({
  width: `${currentSize.value.width}px`,
  height: `${currentSize.value.height}px`,
  transition: isResizing.value ? 'none' : 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1), height 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
}))

// 展开聊天窗口
const expandChat = () => {
  isTransitioning.value = true
  isExpanded.value = true
  nextTick(() => {
    scrollToBottom()
    setTimeout(() => {
      isTransitioning.value = false
    }, 300)
  })
}

// 最小化聊天窗口
const minimizeChat = () => {
  isTransitioning.value = true
  isExpanded.value = false
  setTimeout(() => {
    isTransitioning.value = false
  }, 300)
}

// 关闭聊天窗口
const closeChat = () => {
  if (messages.value.length === 0) {
    minimizeChat()
    return
  }
  
  ElMessageBox.confirm(
    '确定要关闭聊天窗口吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    minimizeChat()
    messages.value = []
  }).catch(() => {})
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return

  messages.value.push({
    type: 'user',
    content: userInput.value
  })

  const userMessage = userInput.value
  userInput.value = ''
  isLoading.value = true

  try {
    const response = await fetch(`http://localhost:5678/webhook/b35cfb65-5079-4a8f-a633-fcdd1004629f?chatInput=${encodeURIComponent(userMessage)}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const data = await response.json()
    
    if (data && data[0] && data[0].output) {
      const content = data[0].output
      // 检查是否包含图表代码
      const isChart = content.includes('echarts') || content.includes('chart')
      
      messages.value.push({
        type: 'ai',
        content: content,
        isChart: isChart
      })

      // 如果是图表，等待 DOM 更新后初始化图表
      if (isChart) {
        await nextTick()
        const chartIndex = messages.value.length - 1
        const chartDom = document.getElementById(`chart-${chartIndex}`)
        if (chartDom) {
          // 提取并执行图表代码
          const scriptContent = content.match(/<script>([\s\S]*?)<\/script>/)?.[1]
          if (scriptContent) {
            try {
              // 创建一个新的函数来执行图表代码，并传入 echarts
              const initChart = new Function('chartDom', 'echarts', scriptContent)
              initChart(chartDom, echarts)
            } catch (error) {
              console.error('图表初始化错误:', error)
            }
          }
        }
      }
    } else {
      throw new Error('Invalid response format')
    }

    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('请求错误:', error)
    ElMessage.error('请求失败，请稍后重试')
    messages.value.push({
      type: 'ai',
      content: '抱歉，我遇到了一些问题，请稍后再试。'
    })
  } finally {
    isLoading.value = false
  }
}

// 监听消息变化，处理图表
watch(() => messages.value, async (newMessages) => {
  await nextTick()
  newMessages.forEach((message, index) => {
    if (message.isChart) {
      const chartDom = document.getElementById(`chart-${index}`)
      if (chartDom) {
        const scriptContent = message.content.match(/<script>([\s\S]*?)<\/script>/)?.[1]
        if (scriptContent) {
          try {
            const initChart = new Function('chartDom', 'echarts', scriptContent)
            initChart(chartDom, echarts)
          } catch (error) {
            console.error('图表初始化错误:', error)
          }
        }
      }
    }
  })
}, { deep: true })

// 拖拽处理
const handleMouseDown = (e) => {
  if (e.target.closest('.el-button') || e.target.closest('.el-input') || e.target.closest('.resize-handle')) return
  startDrag(e)
}

const handleContainerMouseDown = (e) => {
  if (e.target.closest('.el-button') || e.target.closest('.el-input') || e.target.closest('.resize-handle')) return
  startDrag(e)
}

const startDrag = (e) => {
  isDragging.value = true
  dragStartTime.value = Date.now()
  const rect = e.currentTarget.getBoundingClientRect()
  dragOffset.value = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
  dragStartPosition.value = { ...position.value }
}

const handleMouseMove = (e) => {
  if (isDragging.value) {
    const x = e.clientX - dragOffset.value.x
    const y = e.clientY - dragOffset.value.y
    
    const maxX = window.innerWidth - (isExpanded.value ? currentSize.value.width : 60)
    const maxY = window.innerHeight - (isExpanded.value ? currentSize.value.height : 60)
    
    position.value = {
      x: Math.min(Math.max(0, x), maxX),
      y: Math.min(Math.max(0, y), maxY)
    }
  }
  
  if (isResizing.value) {
    const minWidth = 300
    const minHeight = 400
    const maxWidth = window.innerWidth - position.value.x
    const maxHeight = window.innerHeight - position.value.y
    
    if (resizeDirection.value.includes('e')) {
      currentSize.value.width = Math.min(Math.max(minWidth, e.clientX - position.value.x), maxWidth)
    }
    if (resizeDirection.value.includes('s')) {
      currentSize.value.height = Math.min(Math.max(minHeight, e.clientY - position.value.y), maxHeight)
    }
  }
}

const handleMouseUp = () => {
  if (isDragging.value) {
    const dragDistance = Math.sqrt(
      Math.pow(position.value.x - dragStartPosition.value.x, 2) +
      Math.pow(position.value.y - dragStartPosition.value.y, 2)
    )
    
    const dragDuration = Date.now() - dragStartTime.value
    
    // 如果是短距离拖动且时间很短，可能是点击事件
    if (dragDistance < 5 && dragDuration < 200 && !isExpanded.value) {
      expandChat()
    }
  }
  
  isDragging.value = false
  isResizing.value = false
  resizeDirection.value = ''
}

// 开始调整大小
const startResize = (direction) => {
  isResizing.value = true
  resizeDirection.value = direction
  startSize.value = { ...currentSize.value }
}

// 处理过渡结束
const handleTransitionEnd = () => {
  isTransitioning.value = false
}

onMounted(() => {
  position.value = {
    x: window.innerWidth - 80,
    y: window.innerHeight - 80
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
})
</script>

<style scoped>
.floating-chat-wrapper {
  position: fixed;
  z-index: 1000;
  will-change: transform;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: move;
}

.floating-chat {
  position: relative;
  width: 100%;
  height: 100%;
}

.floating-ball {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #1e88e5, #0d47a1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(30, 136, 229, 0.3);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(4px);
}

.floating-ball:hover {
  transform: scale(1.1) translateY(-2px);
  box-shadow: 0 6px 24px rgba(30, 136, 229, 0.4);
}

.floating-ball:active {
  transform: scale(0.95);
}

.floating-ball .el-icon {
  font-size: 24px;
  color: white;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.chat-container {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: move;
}

.chat-container:active {
  cursor: grabbing;
}

.chat-header {
  padding: 16px;
  background: linear-gradient(135deg, #1e88e5, #0d47a1);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  user-select: none;
  position: relative;
  z-index: 2;
}

.chat-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.header-actions .el-button {
  color: white;
  padding: 6px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.header-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.header-actions .el-button:active {
  transform: translateY(0);
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: rgba(245, 247, 250, 0.8);
  scroll-behavior: smooth;
  min-height: 200px;
  cursor: default;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.15);
}

.message {
  margin-bottom: 16px;
  max-width: 85%;
  animation: messageSlide 0.3s ease-out;
}

.message.user {
  margin-left: auto;
}

.message.user .message-content {
  background: linear-gradient(135deg, #1e88e5, #0d47a1);
  color: white;
  border-radius: 16px 16px 0 16px;
  box-shadow: 0 2px 8px rgba(30, 136, 229, 0.2);
}

.message.ai {
  margin-right: auto;
}

.message.ai .message-content {
  background: white;
  border-radius: 16px 16px 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message-content {
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  word-break: break-word;
  line-height: 1.5;
  font-size: 14px;
}

.message .chart-content {
  width: 100%;
  max-width: 100%;
  padding: 0;
  background: transparent;
  box-shadow: none;
}

.message .chart-content .chart-container {
  width: 100%;
  height: 400px;
  margin-top: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.chat-input {
  padding: 16px;
  background: white;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 12px;
  min-height: 80px;
  position: relative;
  z-index: 2;
  cursor: default;
}

.chat-input .el-input {
  flex: 1;
}

.chat-input .el-textarea__inner {
  border-radius: 12px;
  padding: 12px;
  resize: none;
  border: 1px solid rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.chat-input .el-textarea__inner:focus {
  border-color: #1e88e5;
  box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.1);
}

.chat-input .el-button {
  border-radius: 12px;
  padding: 12px 24px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #1e88e5, #0d47a1);
  border: none;
}

.chat-input .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.3);
}

.chat-input .el-button:active {
  transform: translateY(0);
}

.resize-handle {
  position: absolute;
  background: transparent;
  z-index: 100;
  transition: background-color 0.2s ease;
}

.resize-handle-e {
  right: 0;
  top: 0;
  width: 8px;
  height: 100%;
  cursor: e-resize;
}

.resize-handle-e:hover {
  background: rgba(30, 136, 229, 0.1);
}

.resize-handle-s {
  bottom: 0;
  left: 0;
  width: 100%;
  height: 8px;
  cursor: s-resize;
}

.resize-handle-s:hover {
  background: rgba(30, 136, 229, 0.1);
}

.resize-handle-se {
  right: 0;
  bottom: 0;
  width: 16px;
  height: 16px;
  cursor: se-resize;
}

.resize-handle-se:hover {
  background: rgba(30, 136, 229, 0.1);
}

.floating-chat.expanded {
  animation: expand 0.3s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes expand {
  from {
    transform: scale(0.8) translateY(20px);
    opacity: 0;
  }
  to {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}

@keyframes messageSlide {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>