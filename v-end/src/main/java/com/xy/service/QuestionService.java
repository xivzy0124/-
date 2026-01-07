package com.xy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xy.util.chat.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionService {

    @Value("${coze.api.key}")
    private String apiKey;

    @Value("${coze.api.bot-id}")
    private String botId;

    @Value("${coze.api.base-url}")
    private String baseUrl;

    private final WebClient webClient;

    public QuestionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Result questionService(String questionText) {
        try {
            CozeApiResponse initialResponse = sendInitialRequest(questionText);
            if (initialResponse.getCode() != 0) {
                return Result.error(initialResponse.getMsg());
            }

            CozeApiData data = initialResponse.getData();
            if (data == null) {
                return Result.error("API 返回数据为空");
            }

            // 确保从 CozeApiData 中获取正确的字段（通过 @JsonProperty 映射）
            String conversationId = data.getConversationId();
            String chatId = data.getId();
            if (conversationId == null || chatId == null) {
                return Result.error("缺少会话或聊天ID");
            }

            return getQuestionAnswer(conversationId, chatId);

        } catch (Exception e) {
            return Result.error("服务异常: " + e.getMessage());
        }
    }

    private CozeApiResponse sendInitialRequest(String questionText) {
        // 构造 additional_messages 为 List<Map>（与 Python 一致）
        List<Map<String, String>> additionalMessages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", questionText);
        message.put("content_type", "text");
        additionalMessages.add(message);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bot_id", botId);
        requestBody.put("user_id", "jiangwp");
        requestBody.put("stream", false);
        requestBody.put("auto_save_history", true);
        requestBody.put("additional_messages", additionalMessages);

        return webClient.post()
                .uri(baseUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(CozeApiResponse.class)
                .block(Duration.ofSeconds(10));
    }

    private Result getQuestionAnswer(String conversationId, String chatId) {
        String statusUrl = baseUrl + "/retrieve?conversation_id=" + conversationId + "&chat_id=" + chatId;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("bot_id", botId);
        params.add("task_id", chatId);

        while (true) {
            // 轮询状态
            StatusResponse statusResponse = WebClient.builder().baseUrl(statusUrl).build().get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParams(params)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(StatusResponse.class)
                    .block(Duration.ofSeconds(5));

            if (statusResponse == null || statusResponse.getCode() != 0) {
                return Result.error("状态请求失败: " + (statusResponse != null ? statusResponse.getMsg() : "无响应"));
            }

            String status = statusResponse.getData().getStatus();
            if ("completed".equals(status)) {
                // 获取最终答案
                return fetchFinalAnswer(conversationId, chatId);
            } else {
                try {
                    Thread.sleep(1000); // 模拟Python的time.sleep(1)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Result.error("轮询被中断");
                }
            }
        }
    }

    private Result fetchFinalAnswer(String conversationId, String chatId) {
        String answerUrl = baseUrl + "/message/list?chat_id=" + chatId + "&conversation_id=" + conversationId;
        AnswerResponse answerResponse = webClient.get()
                .uri(answerUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(AnswerResponse.class)
                .block(Duration.ofSeconds(10));

        if (answerResponse == null || answerResponse.getCode() != 0) {
            return Result.error("获取答案失败: " + (answerResponse != null ? answerResponse.getMsg() : "无响应"));
        }

        Result result = processQuestionAnswer(answerResponse);
        // 保存会话ID和聊天ID
        result.setConversationId(conversationId);
        result.setChatId(chatId);
        return result;
    }

    private Result processQuestionAnswer(AnswerResponse response) {
        Result result = new Result();
        for (Item item : response.getData()) {
            if ("answer".equals(item.getType())) {
                result.setAnswer(item.getContent());
            } else if ("follow_up".equals(item.getType())) {
                result.getFollowUps().add(item.getContent());
            }
        }
        return result;
    }
    
    /**
     * 取消正在进行的聊天会话
     * @param conversationId 会话ID
     * @param chatId 聊天ID
     * @return 是否成功取消
     */
    public boolean cancelChat(String conversationId, String chatId) {
        try {
            String cancelUrl = baseUrl + "/cancel";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bot_id", botId);
            requestBody.put("conversation_id", conversationId);
            requestBody.put("chat_id", chatId);
            
            CozeApiResponse response = webClient.post()
                    .uri(cancelUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(CozeApiResponse.class)
                    .block(Duration.ofSeconds(5));
            
            return response != null && response.getCode() == 0;
        } catch (Exception e) {
            // 发生异常时，记录日志并返回取消失败
            System.err.println("取消聊天失败: " + e.getMessage());
            return false;
        }
    }
}
