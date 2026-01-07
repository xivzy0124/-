package com.xy.util.chat;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 最终返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    private String answer;
    private List<String> followUps = new ArrayList<>();
    private String error;
    
    // 会话ID
    private String conversationId;
    
    // 聊天ID
    private String chatId;

    public static Result error(String message) {
        Result result = new Result();
        result.setError(message);
        return result;
    }

}
