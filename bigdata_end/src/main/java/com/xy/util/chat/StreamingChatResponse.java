package com.xy.util.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于流式响应的数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamingChatResponse {
    private String type; // "chunk", "error", "done", "cancel"
    private String content;
    private String conversationId;
    private String chatId;
    private boolean done;
    
    public static StreamingChatResponse chunk(String content, String conversationId, String chatId) {
        StreamingChatResponse response = new StreamingChatResponse();
        response.setType("chunk");
        response.setContent(content);
        response.setConversationId(conversationId);
        response.setChatId(chatId);
        response.setDone(false);
        return response;
    }
    
    public static StreamingChatResponse error(String errorMessage) {
        StreamingChatResponse response = new StreamingChatResponse();
        response.setType("error");
        response.setContent(errorMessage);
        response.setDone(true);
        return response;
    }
    
    public static StreamingChatResponse done(String conversationId, String chatId) {
        StreamingChatResponse response = new StreamingChatResponse();
        response.setType("done");
        response.setConversationId(conversationId);
        response.setChatId(chatId);
        response.setDone(true);
        return response;
    }
    
    public static StreamingChatResponse cancel() {
        StreamingChatResponse response = new StreamingChatResponse();
        response.setType("cancel");
        response.setContent("请求已取消");
        response.setDone(true);
        return response;
    }
} 