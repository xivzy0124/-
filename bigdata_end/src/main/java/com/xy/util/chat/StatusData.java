package com.xy.util.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusData {
    private String status;
    
    // 错误信息
    private String error;
    
    // 答案内容
    private String answer;
    
    // 跟进问题列表
    @JsonProperty("follow_ups")
    private List<String> followUps;
}
