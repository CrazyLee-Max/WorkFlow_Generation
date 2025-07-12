package com.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 意图识别响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentRecognitionResponse {
    
    /**
     * 是否为工作流生成意图
     */
    private boolean isWorkflowIntent;
    
    /**
     * 置信度 (0.0 - 1.0)
     */
    private double confidence;
    
    /**
     * 意图分类
     */
    private String intentCategory;
    
    /**
     * 识别原因或说明
     */
    private String reason;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 处理时间戳
     */
    private long timestamp;
}