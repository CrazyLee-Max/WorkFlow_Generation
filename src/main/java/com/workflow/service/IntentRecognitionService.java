package com.workflow.service;

import com.workflow.dto.IntentRecognitionResponse;

/**
 * 意图识别服务接口
 * 负责判断用户输入是否为工作流生成意图
 */
public interface IntentRecognitionService {
    
    /**
     * 识别用户输入的意图
     * 
     * @param userInput 用户的自然语言描述
     * @param requestId 请求ID
     * @return 意图识别结果
     */
    IntentRecognitionResponse recognizeIntent(String userInput, String requestId);
}