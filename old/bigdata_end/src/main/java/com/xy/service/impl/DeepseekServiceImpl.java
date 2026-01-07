package com.xy.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xy.config.DeepseekConfig;
import com.xy.pojo.ChatRequest;
import com.xy.pojo.ChatResponse;
import com.xy.pojo.DeepseekRequestDTO;
import com.xy.pojo.DeepseekResponseDTO;
import com.xy.service.DeepseekService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepseekServiceImpl implements DeepseekService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeepseekServiceImpl.class);
    private static final String CHAT_ENDPOINT = "/chat/completions";
    
    private final DeepseekConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public DeepseekServiceImpl(DeepseekConfig config, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            // 从请求参数构建DeepSeek API请求
            DeepseekRequestDTO apiRequest = buildDeepseekRequest(request);
            
            // 准备Http头部
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getKey());
            
            // 创建HTTP实体
            HttpEntity<DeepseekRequestDTO> httpEntity = new HttpEntity<>(apiRequest, headers);
            
            // 发送请求到DeepSeek API
            ResponseEntity<DeepseekResponseDTO> response = restTemplate.postForEntity(
                    config.getBaseUrl() + CHAT_ENDPOINT, 
                    httpEntity, 
                    DeepseekResponseDTO.class);
            
            // 处理API响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DeepseekResponseDTO apiResponse = response.getBody();
                
                // 提取回复文本
                String answer = apiResponse.getChoices().get(0).getMessage().getContent();
                Integer tokenUsage = apiResponse.getUsage().getTotalTokens();
                
                return new ChatResponse(answer, tokenUsage);
            } else {
                logger.error("调用DeepSeek API失败: {}", response.getStatusCode());
                return ChatResponse.error(500, "调用AI服务失败");
            }
        } catch (Exception e) {
            logger.error("调用DeepSeek API异常", e);
            return ChatResponse.error(500, "调用AI服务异常: " + e.getMessage());
        }
    }
    
    @Override
    public void chatStream(ChatRequest request, StreamResponseCallback callback) {
        try {
            // 实现流式调用的逻辑
            // 这里需要使用WebClient或其他支持流式响应的HTTP客户端
            // 由于实现较为复杂，此处仅提供非流式实现
            // 实际项目中需要根据需求完善此方法
            
            String errorMsg = "流式调用暂未实现";
            logger.warn(errorMsg);
            callback.onError(new UnsupportedOperationException(errorMsg));
        } catch (Exception e) {
            logger.error("流式调用DeepSeek API异常", e);
            callback.onError(e);
        }
    }
    
    /**
     * 构建DeepSeek API请求
     */
    private DeepseekRequestDTO buildDeepseekRequest(ChatRequest request) {
        DeepseekRequestDTO apiRequest = new DeepseekRequestDTO();
        
        // 设置模型名称
        apiRequest.setModel(config.getModel());
        
        // 设置温度参数
        apiRequest.setTemperature(request.getTemperature() != null 
                ? request.getTemperature() : config.getTemperature());
        
        // 设置最大生成token数
        apiRequest.setMaxTokens(request.getMaxTokens() != null 
                ? request.getMaxTokens() : config.getMaxTokens());
        
        // 设置是否流式返回
        apiRequest.setStream(request.getStream() != null ? request.getStream() : false);
        
        // 构建消息列表
        List<DeepseekRequestDTO.Message> messages = new ArrayList<>();
        
        // 添加系统提示（如果有）
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().trim().isEmpty()) {
            messages.add(new DeepseekRequestDTO.Message("system", request.getSystemPrompt()));
        }
        
        // 添加历史对话（如果有）
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            for (Map<String, String> msg : request.getHistory()) {
                String role = msg.getOrDefault("role", "user");
                String content = msg.getOrDefault("content", "");
                messages.add(new DeepseekRequestDTO.Message(role, content));
            }
        }
        
        // 添加当前用户查询
        messages.add(new DeepseekRequestDTO.Message("user", request.getQuery()));
        
        apiRequest.setMessages(messages);
        
        return apiRequest;
    }
} 