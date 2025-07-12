package com.workflow.service;

/**
 * DeepSeek API服务接口
 * 提供与DeepSeek模型的聊天对话功能
 */
public interface DeepSeekApiService {
    
    /**
     * 调用DeepSeek聊天API
     * 
     * @param prompt 用户提示
     * @return AI响应内容
     */
    String chatCompletion(String prompt);
}