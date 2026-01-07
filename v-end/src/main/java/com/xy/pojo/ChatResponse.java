package com.xy.pojo;

public class ChatResponse {
    private Integer code;
    private String message;
    private ChatResult data;

    public static class ChatResult {
        private String answer;
        private Integer tokenUsage;

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public Integer getTokenUsage() {
            return tokenUsage;
        }

        public void setTokenUsage(Integer tokenUsage) {
            this.tokenUsage = tokenUsage;
        }
    }

    public ChatResponse() {
        this.code = 200;
        this.message = "success";
        this.data = new ChatResult();
    }

    public ChatResponse(String answer, Integer tokenUsage) {
        this.code = 200;
        this.message = "success";
        this.data = new ChatResult();
        this.data.answer = answer;
        this.data.tokenUsage = tokenUsage;
    }

    public static ChatResponse error(Integer code, String message) {
        ChatResponse response = new ChatResponse();
        response.code = code;
        response.message = message;
        return response;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ChatResult getData() {
        return data;
    }

    public void setData(ChatResult data) {
        this.data = data;
    }
} 