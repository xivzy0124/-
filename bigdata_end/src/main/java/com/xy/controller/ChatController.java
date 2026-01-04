package com.xy.controller;

import com.xy.service.QuestionService;
import com.xy.util.chat.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final QuestionService questionService;
    
    // 存储会话上下文（简单实现，生产环境应使用Redis或数据库）
    private final Map<String, Map<String, String>> activeSessions = new ConcurrentHashMap<>();

    public ChatController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * 用户发送消息并获取AI回复。
     * 该接口用于与AI进行对话，支持上下文管理。
     *
     * @param request 请求体，Map结构，必须包含以下字段：
     *                - message (String, 必填)：用户输入内容
     *                - user_id (String, 选填)：用户唯一标识，默认default_user
     * @return ResponseEntity<Map<String, Object>>
     *         - success: true/false
     *         - response: AI回复内容（成功时）
     *         - follow_ups: 跟进问题（可选）
     *         - message: 错误信息（失败时）
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String userInput = request.get("message");
        String userId = request.getOrDefault("user_id", "default_user");
        
        System.out.println("用户 [" + userId + "] 发送消息: " + userInput);
        
        Result result = questionService.questionService(userInput);
        
        // 保存会话信息（如果有）
        if (result.getConversationId() != null && result.getChatId() != null) {
            Map<String, String> sessionInfo = new HashMap<>();
            sessionInfo.put("conversation_id", result.getConversationId());
            sessionInfo.put("chat_id", result.getChatId());
            activeSessions.put(userId, sessionInfo);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        if (result.getError() != null) {
            // 处理错误情况
            response.put("success", false);
            response.put("message", result.getError());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        
        // 正常返回
        response.put("success", true);
        response.put("response", result.getAnswer());
        
        // 如果有跟进问题，也添加到响应中
        if (result.getFollowUps() != null && !result.getFollowUps().isEmpty()) {
            response.put("follow_ups", result.getFollowUps());
        }
        System.out.println("response:"+response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * 取消正在进行的聊天。
     * 该接口用于取消指定会话的AI对话。
     *
     * @param request 请求体，Map结构，必须包含以下字段：
     *                - user_id (String, 选填)：用户唯一标识，默认default_user
     *                - conversation_id (String, 选填)：会话ID
     *                - chat_id (String, 选填)：聊天ID
     * @return ResponseEntity<Map<String, Object>>
     *         - success: true/false
     *         - message: 操作结果说明
     */
    @PostMapping("/chat/cancel")
    public ResponseEntity<Map<String, Object>> cancelChat(@RequestBody Map<String, String> request) {
        String userId = request.getOrDefault("user_id", "default_user");
        String conversationId = request.get("conversation_id");
        String chatId = request.get("chat_id");
        
        Map<String, Object> response = new HashMap<>();
        
        // 如果请求中未指定会话ID和聊天ID，尝试从保存的会话中获取
        if ((conversationId == null || chatId == null) && activeSessions.containsKey(userId)) {
            Map<String, String> sessionInfo = activeSessions.get(userId);
            conversationId = sessionInfo.get("conversation_id");
            chatId = sessionInfo.get("chat_id");
        }
        
        // 如果仍然无法获取会话ID和聊天ID，返回错误
        if (conversationId == null || chatId == null) {
            response.put("success", false);
            response.put("message", "无法取消聊天：未指定会话ID和聊天ID");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        // 尝试取消聊天
        boolean canceled = questionService.cancelChat(conversationId, chatId);
        
        if (canceled) {
            response.put("success", true);
            response.put("message", "聊天已成功取消");
            
            // 移除已取消的会话
            if (activeSessions.containsKey(userId)) {
                activeSessions.remove(userId);
            }
        } else {
            response.put("success", false);
            response.put("message", "取消聊天失败");
        }
        System.out.println("response:"+response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * 获取用户活跃会话信息。
     * 该接口用于查询当前用户是否有未结束的AI会话。
     *
     * @param userId 用户唯一标识（String，选填，默认default_user）
     * @return ResponseEntity<Map<String, Object>>
     *         - success: true
     *         - has_active_session: 是否有活跃会话（boolean）
     *         - session_info: 会话信息（Map）
     */
    @GetMapping("/chat/session")
    public ResponseEntity<Map<String, Object>> getSessionInfo(@RequestParam(defaultValue = "default_user") String userId) {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, String> sessionInfo = activeSessions.getOrDefault(userId, Collections.emptyMap());
        System.out.println("sessionInfo:"+sessionInfo);
        
        response.put("success", true);
        response.put("has_active_session", !sessionInfo.isEmpty());
        response.put("session_info", sessionInfo);
        System.out.println("response:"+response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
