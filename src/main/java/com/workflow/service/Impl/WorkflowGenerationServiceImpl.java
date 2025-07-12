package com.workflow.service.Impl;

import com.workflow.dto.*;
import com.workflow.service.IntentRecognitionService;
import com.workflow.service.TaskDecompositionService;
import com.workflow.service.WorkflowGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 工作流生成服务实现类
 * 整合意图识别和任务分解功能，提供完整的工作流生成流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowGenerationServiceImpl implements WorkflowGenerationService {
    
    private final IntentRecognitionService intentRecognitionService;
    private final TaskDecompositionService taskDecompositionService;
    
    /**
     * 处理工作流生成请求
     * 
     * @param request 工作流生成请求
     * @return API响应，包含意图识别结果或任务分解结果
     */
    @Override
    public ApiResponse<?> processWorkflowRequest(WorkflowRequest request) {
        String requestId = generateRequestId(request);
        log.info("开始处理工作流生成请求，requestId: {}, description: {}", 
                requestId, request.getDescription());
        
        try {
            // 第一步：意图识别
            IntentRecognitionResponse intentResult = intentRecognitionService
                    .recognizeIntent(request.getDescription(), requestId);
            
            // 如果不是工作流生成意图，直接返回意图识别结果
            if (!intentResult.isWorkflowIntent()) {
                log.info("用户输入不是工作流生成意图，requestId: {}, confidence: {}", 
                        requestId, intentResult.getConfidence());
                
                return ApiResponse.success(intentResult, requestId);
            }
            
            log.info("确认为工作流生成意图，开始任务分解，requestId: {}, confidence: {}", 
                    requestId, intentResult.getConfidence());
            
            // 第二步：任务分解
            TaskDecompositionResponse decompositionResult = taskDecompositionService
                    .decomposeTask(request.getDescription(), requestId);
            
            log.info("工作流生成完成，requestId: {}, 变量数: {}, 步骤数: {}", 
                    requestId, 
                    decompositionResult.getVariables().size(), 
                    decompositionResult.getSteps().size());
            
            return ApiResponse.success(decompositionResult, requestId);
            
        } catch (Exception e) {
            log.error("处理工作流生成请求失败，requestId: {}, error: {}", 
                    requestId, e.getMessage(), e);
            
            return ApiResponse.error(500, "工作流生成失败: " + e.getMessage(), requestId);
        }
    }
    
    /**
     * 仅进行意图识别
     * 
     * @param description 用户描述
     * @return 意图识别结果
     */
    @Override
    public ApiResponse<IntentRecognitionResponse> recognizeIntentOnly(String description) {
        String requestId = UUID.randomUUID().toString();
        log.info("执行单独的意图识别，requestId: {}, description: {}", requestId, description);
        
        try {
            IntentRecognitionResponse result = intentRecognitionService
                    .recognizeIntent(description, requestId);
            
            return ApiResponse.success(result, requestId);
            
        } catch (Exception e) {
            log.error("意图识别失败，requestId: {}, error: {}", requestId, e.getMessage(), e);
            return ApiResponse.error(500, "意图识别失败: " + e.getMessage(), requestId);
        }
    }
    
    /**
     * 仅进行任务分解（跳过意图识别）
     * 
     * @param description 用户描述
     * @return 任务分解结果
     */
    @Override
    public ApiResponse<TaskDecompositionResponse> decomposeTaskOnly(String description) {
        String requestId = UUID.randomUUID().toString();
        log.info("执行单独的任务分解，requestId: {}, description: {}", requestId, description);
        
        try {
            TaskDecompositionResponse result = taskDecompositionService
                    .decomposeTask(description, requestId);
            
            return ApiResponse.success(result, requestId);
            
        } catch (Exception e) {
            log.error("任务分解失败，requestId: {}, error: {}", requestId, e.getMessage(), e);
            return ApiResponse.error(500, "任务分解失败: " + e.getMessage(), requestId);
        }
    }
    
    /**
     * 获取服务健康状态
     * 
     * @return 健康状态信息
     */
    @Override
    public ApiResponse<String> getHealthStatus() {
        try {
            // 简单的健康检查
            log.debug("执行健康检查");
            
            return ApiResponse.success("工作流生成服务运行正常");
            
        } catch (Exception e) {
            log.error("健康检查失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, "服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 生成请求ID
     * 
     * @param request 请求对象
     * @return 请求ID
     */
    private String generateRequestId(WorkflowRequest request) {
        if (request.getRequestId() != null && !request.getRequestId().trim().isEmpty()) {
            return request.getRequestId();
        }
        return UUID.randomUUID().toString();
    }
}
