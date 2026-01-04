package com.xy.util.chat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CozeApiData {
    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("id")
    private String id;

    // 新增：映射 JSON 中的 "bot_id" 字段
    @JsonProperty("bot_id")
    private String botId;

    // getter 和 setter 方法
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }
}