package com.xy.service;

import com.xy.pojo.ChatRequest;
import com.xy.pojo.ChatResponse;

public interface DeepseekService {
    
    /**
     * 发送请求到DeepSeek API并获取回复
     * 
     * @param request 聊天请求
     * @return 聊天回复
     */
    ChatResponse chat(ChatRequest request);
    
    /**
     * 使用流式返回方式调用DeepSeek API
     * 
     * @param request 聊天请求
     * @return 带有回调处理的响应
     */
    void chatStream(ChatRequest request, StreamResponseCallback callback);
    
    /**
     * 流式返回的回调接口
     */
    interface StreamResponseCallback {
        void onMessage(String message);
        void onComplete(Integer tokenUsage);
        void onError(Exception e);
    }
} 