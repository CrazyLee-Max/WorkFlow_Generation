package com.workflow.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * LangChain4j和DeepSeek配置类
 * 配置DeepSeek API相关参数和RestTemplate
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

    /**
     * 创建RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * DeepSeek配置Bean
     */
    @Bean
    @ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
    public DeepSeekConfig deepSeekConfig() {
        DeepSeekConfig config = new DeepSeekConfig();
        validateConfiguration(config);
        return config;
    }

    /**
     * DeepSeek配置类
     */
    @Data
    @ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
    public static class DeepSeekConfig {
        private String baseUrl = "https://api.deepseek.com/v1";
        private String apiKey;
        private String modelName = "deepseek-chat";
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
        private Integer timeout = 30;
    }

    /**
     * 验证DeepSeek配置
     */
    private void validateConfiguration(DeepSeekConfig config) {
        log.info("验证DeepSeek配置参数");
        
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("placeholder")) {
            log.warn("DeepSeek API Key未正确配置，请设置环境变量DEEPSEEK_API_KEY");
            log.warn("应用将启动，但AI功能将不可用，直到配置正确的API Key");
        }
        
        if (config.getBaseUrl() == null || config.getBaseUrl().trim().isEmpty()) {
            config.setBaseUrl("https://api.deepseek.com/v1");
            log.warn("使用默认DeepSeek API地址: {}", config.getBaseUrl());
        }
        
        if (config.getModelName() == null || config.getModelName().trim().isEmpty()) {
            config.setModelName("deepseek-chat");
            log.warn("使用默认模型: {}", config.getModelName());
        }
        
        if (config.getTemperature() == null || config.getTemperature() < 0 || config.getTemperature() > 2) {
            config.setTemperature(0.7);
            log.warn("使用默认temperature: {}", config.getTemperature());
        }
        
        if (config.getMaxTokens() == null || config.getMaxTokens() <= 0) {
            config.setMaxTokens(2000);
            log.warn("使用默认maxTokens: {}", config.getMaxTokens());
        }
        
        log.info("DeepSeek配置验证通过 - baseUrl: {}, modelName: {}, temperature: {}, maxTokens: {}", 
                config.getBaseUrl(), config.getModelName(), config.getTemperature(), config.getMaxTokens());
    }
}