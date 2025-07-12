package com.workflow.service.Impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.config.LangChain4jConfig;
import com.workflow.service.DeepSeekApiService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * DeepSeek API服务实现类
 * 直接调用DeepSeek API进行聊天对话
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekApiServiceImpl implements DeepSeekApiService {
    
    private final RestTemplate restTemplate;
    private final LangChain4jConfig.DeepSeekConfig deepSeekConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 调用DeepSeek聊天API
     * 
     * @param prompt 用户提示
     * @return AI响应内容
     */
    @Override
    public String chatCompletion(String prompt) {
        log.info("开始调用DeepSeek API，prompt长度: {}", prompt.length());
        
        // 检查API Key是否配置
        if (deepSeekConfig.getApiKey() == null || 
            deepSeekConfig.getApiKey().contains("placeholder") ||
            deepSeekConfig.getApiKey().trim().isEmpty()) {
            log.error("DeepSeek API Key未配置");
            throw new RuntimeException("DeepSeek API Key未配置，请设置环境变量DEEPSEEK_API_KEY");
        }
        
        try {
            // 构建请求
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(deepSeekConfig.getModelName())
                    .messages(List.of(
                            ChatMessage.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .temperature(deepSeekConfig.getTemperature())
                    .maxTokens(deepSeekConfig.getMaxTokens())
                    .build();
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepSeekConfig.getApiKey());
            
            // 发送请求
            String requestBody = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            String url = deepSeekConfig.getBaseUrl() + "/chat/completions";
            log.debug("发送请求到: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            // 解析响应
            log.info("收到DeepSeek API响应，状态码: {}", response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("响应体: {}", response.getBody());
                
                ChatCompletionResponse chatResponse = objectMapper.readValue(
                        response.getBody(), ChatCompletionResponse.class);
                
                if (chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                    String content = chatResponse.getChoices().get(0).getMessage().getContent();
                    log.info("DeepSeek API调用成功，返回内容长度: {}", content.length());
                    return content;
                } else {
                    log.error("DeepSeek API响应中没有choices或choices为空");
                    throw new RuntimeException("DeepSeek API响应中没有有效的选择项");
                }
            }
            
            log.error("DeepSeek API响应异常，状态码: {}, 响应体: {}", 
                    response.getStatusCode(), response.getBody());
            throw new RuntimeException("DeepSeek API响应状态异常: " + response.getStatusCode());
            
        } catch (Exception e) {
            log.error("调用DeepSeek API失败: {}", e.getMessage(), e);
            throw new RuntimeException("DeepSeek API调用失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 聊天完成请求
     */
    @Data
    @lombok.Builder
    public static class ChatCompletionRequest {
        private String model;
        private List<ChatMessage> messages;
        private Double temperature;
        
        @JsonProperty("max_tokens")
        private Integer maxTokens;
    }
    
    /**
     * 聊天消息
     */
    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }
    
    /**
     * 聊天完成响应
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionResponse {
        private String id;
        private String object;
        private Long created;
        private String model;
        private List<Choice> choices;
        private Usage usage;
    }
    
    /**
     * 选择项
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Integer index;
        private ChatMessage message;
        
        @JsonProperty("finish_reason")
        private String finishReason;
    }
    
    /**
     * 使用统计
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        
        @JsonProperty("total_tokens")
        private Integer totalTokens;
        
        @JsonProperty("prompt_tokens_details")
        private Object promptTokensDetails;
        
        @JsonProperty("prompt_cache_hit_tokens")
        private Integer promptCacheHitTokens;
        
        @JsonProperty("prompt_cache_miss_tokens")
        private Integer promptCacheMissTokens;
    }
}