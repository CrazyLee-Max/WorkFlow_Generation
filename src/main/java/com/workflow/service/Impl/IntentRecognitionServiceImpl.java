package com.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.dto.IntentRecognitionResponse;
import com.workflow.service.DeepSeekApiService;
import com.workflow.service.IntentRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 意图识别服务实现类
 * 使用DeepSeek模型判断用户输入是否为工作流生成意图
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentRecognitionServiceImpl implements IntentRecognitionService {
    
    private final DeepSeekApiService deepSeekApiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${workflow.intent.confidence-threshold:0.8}")
    private double confidenceThreshold;
    
    /**
     * 意图识别提示模板
     */
    private static final String INTENT_RECOGNITION_PROMPT = """
        你是一个专业的意图识别助手，专门用于判断用户的自然语言描述是否表达了生成工作流的意图。
        
        工作流生成意图的特征包括但不限于：
        1. 描述了一系列需要按顺序执行的操作步骤
        2. 包含条件判断、循环控制等逻辑结构
        3. 涉及变量创建、状态监控、自动化控制等
        4. 描述了业务流程、操作流程或控制流程
        5. 使用了"流程"、"步骤"、"自动化"、"控制"等关键词
        
        非工作流生成意图的例子：
        1. 简单的问答或咨询
        2. 单一的操作指令
        3. 纯粹的信息查询
        4. 闲聊或无关内容
        
        请分析以下用户输入，判断是否为工作流生成意图：
        
        用户输入："{{userInput}}"
        
        请按照以下JSON格式返回结果：
        {
            "isWorkflowIntent": true/false,
            "confidence": 0.0-1.0,
            "intentCategory": "工作流生成" 或 "其他",
            "reason": "判断理由的详细说明"
        }
        
        注意：
        1. confidence表示判断的置信度，范围0.0-1.0
        2. 只有当confidence >= 0.8时，才认为是明确的工作流生成意图
        3. reason要详细说明判断的依据
        """;
    
    /**
     * 识别用户输入的意图
     * 
     * @param userInput 用户的自然语言描述
     * @param requestId 请求ID
     * @return 意图识别结果
     */
    @Override
    public IntentRecognitionResponse recognizeIntent(String userInput, String requestId) {
        log.info("开始意图识别，requestId: {}, userInput: {}", requestId, userInput);
        
        try {
            // 构建提示
            String prompt = INTENT_RECOGNITION_PROMPT.replace("{{userInput}}", userInput);
            
            // 调用DeepSeek模型
            String response = deepSeekApiService.chatCompletion(prompt);
            log.debug("DeepSeek模型响应: {}", response);
            
            // 解析响应
            IntentRecognitionResponse result = parseIntentResponse(response, requestId);
            
            log.info("意图识别完成，requestId: {}, isWorkflowIntent: {}, confidence: {}", 
                    requestId, result.isWorkflowIntent(), result.getConfidence());
            
            return result;
            
        } catch (Exception e) {
            log.error("意图识别失败，requestId: {}, error: {}", requestId, e.getMessage(), e);
            
            // 返回默认的错误响应
            return IntentRecognitionResponse.builder()
                    .isWorkflowIntent(false)
                    .confidence(0.0)
                    .intentCategory("错误")
                    .reason("意图识别过程中发生错误: " + e.getMessage())
                    .requestId(requestId)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }
    
    /**
     * 解析DeepSeek模型的响应
     * 
     * @param response 模型响应
     * @param requestId 请求ID
     * @return 解析后的意图识别结果
     */
    private IntentRecognitionResponse parseIntentResponse(String response, String requestId) {
        try {
            // 清理响应格式
            String cleanedResponse = cleanJsonResponse(response);
            log.debug("清理后的JSON响应: {}", cleanedResponse);
            
            // 使用Jackson解析JSON
            JsonNode jsonNode = objectMapper.readTree(cleanedResponse);
            
            boolean isWorkflowIntent = jsonNode.path("isWorkflowIntent").asBoolean(false);
            double confidence = jsonNode.path("confidence").asDouble(0.0);
            String intentCategory = jsonNode.path("intentCategory").asText("未知");
            String reason = jsonNode.path("reason").asText("无法获取判断理由");
            
            // 应用置信度阈值
            boolean finalWorkflowIntent = isWorkflowIntent && confidence >= confidenceThreshold;
            
            log.debug("解析结果: isWorkflowIntent={}, confidence={}, threshold={}, final={}", 
                    isWorkflowIntent, confidence, confidenceThreshold, finalWorkflowIntent);
            
            return IntentRecognitionResponse.builder()
                    .isWorkflowIntent(finalWorkflowIntent)
                    .confidence(confidence)
                    .intentCategory(intentCategory)
                    .reason(reason)
                    .requestId(requestId)
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("解析意图识别响应失败，requestId: {}, response: {}, error: {}", 
                    requestId, response, e.getMessage(), e);
            
            return IntentRecognitionResponse.builder()
                    .isWorkflowIntent(false)
                    .confidence(0.0)
                    .intentCategory("解析错误")
                    .reason("JSON解析失败: " + e.getMessage())
                    .requestId(requestId)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }
    
    /**
     * 清理JSON响应格式
     */
    private String cleanJsonResponse(String response) {
        if (response == null) {
            return "{}";
        }
        
        String cleaned = response.trim();
        
        // 移除markdown代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        return cleaned.trim();
    }
}
