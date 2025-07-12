package com.workflow.service;

import com.workflow.dto.*;

/**
 * 工作流生成服务接口
 * 整合意图识别和任务分解功能，提供完整的工作流生成流程
 */
public interface WorkflowGenerationService {
    
    /**
     * 处理工作流生成请求
     * 
     * @param request 工作流生成请求
     * @return API响应，包含意图识别结果或任务分解结果
     */
    ApiResponse<?> processWorkflowRequest(WorkflowRequest request);
    
    /**
     * 仅进行意图识别
     * 
     * @param description 用户描述
     * @return 意图识别结果
     */
    ApiResponse<IntentRecognitionResponse> recognizeIntentOnly(String description);
    
    /**
     * 仅进行任务分解（跳过意图识别）
     * 
     * @param description 用户描述
     * @return 任务分解结果
     */
    ApiResponse<TaskDecompositionResponse> decomposeTaskOnly(String description);
    
    /**
     * 获取服务健康状态
     * 
     * @return 健康状态信息
     */
    ApiResponse<String> getHealthStatus();
}