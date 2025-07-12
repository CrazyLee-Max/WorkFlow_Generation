package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.service.WorkflowGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 工作流生成控制器
 * 提供工作流生成相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
@Validated
public class WorkflowController {
    
    private final WorkflowGenerationService workflowGenerationService;
    
    /**
     * 生成工作流
     * 完整的工作流生成流程，包括意图识别和任务分解
     * 
     * @param request 工作流生成请求
     * @return 工作流生成结果
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<?>> generateWorkflow(
            @Valid @RequestBody WorkflowRequest request) {
        
        log.info("收到工作流生成请求，description: {}, userId: {}", 
                request.getDescription(), request.getUserId());
        
        ApiResponse<?> response = workflowGenerationService.processWorkflowRequest(request);
        
        // 根据响应码决定HTTP状态
        if (response.getCode() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getCode()).body(response);
        }
    }
    
    /**
     * 仅进行意图识别
     * 判断用户输入是否为工作流生成意图
     * 
     * @param description 用户的自然语言描述
     * @return 意图识别结果
     */
    @PostMapping("/intent")
    public ResponseEntity<ApiResponse<IntentRecognitionResponse>> recognizeIntent(
            @RequestParam @NotBlank(message = "描述不能为空") 
            @Size(max = 2000, message = "描述长度不能超过2000字符") 
            String description) {
        
        log.info("收到意图识别请求，description: {}", description);
        
        ApiResponse<IntentRecognitionResponse> response = 
                workflowGenerationService.recognizeIntentOnly(description);
        
        if (response.getCode() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getCode()).body(response);
        }
    }
    
    /**
     * 仅进行任务分解
     * 跳过意图识别，直接进行任务分解
     * 
     * @param description 用户的自然语言描述
     * @return 任务分解结果
     */
    @PostMapping("/decompose")
    public ResponseEntity<ApiResponse<TaskDecompositionResponse>> decomposeTask(
            @RequestParam @NotBlank(message = "描述不能为空") 
            @Size(max = 2000, message = "描述长度不能超过2000字符") 
            String description) {
        
        log.info("收到任务分解请求，description: {}", description);
        
        ApiResponse<TaskDecompositionResponse> response = 
                workflowGenerationService.decomposeTaskOnly(description);
        
        if (response.getCode() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getCode()).body(response);
        }
    }
    
    /**
     * 健康检查接口
     * 
     * @return 服务健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.debug("收到健康检查请求");
        
        ApiResponse<String> response = workflowGenerationService.getHealthStatus();
        
        if (response.getCode() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getCode()).body(response);
        }
    }
    
    /**
     * 获取API文档信息
     * 
     * @return API使用说明
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<String>> getApiInfo() {
        log.debug("收到API信息请求");
        
        String apiInfo = """
                工作流生成服务 API 说明：
                
                1. POST /api/workflow/generate
                   - 完整的工作流生成，包括意图识别和任务分解
                   - 请求体：WorkflowRequest JSON
                   - 如果不是工作流意图，返回意图识别结果
                   - 如果是工作流意图，返回任务分解结果
                
                2. POST /api/workflow/intent?description=用户描述
                   - 仅进行意图识别
                   - 参数：description（用户的自然语言描述）
                   - 返回：IntentRecognitionResponse
                
                3. POST /api/workflow/decompose?description=用户描述
                   - 仅进行任务分解（跳过意图识别）
                   - 参数：description（用户的自然语言描述）
                   - 返回：TaskDecompositionResponse
                
                4. GET /api/workflow/health
                   - 健康检查
                   - 返回：服务状态信息
                
                5. GET /api/workflow/info
                   - 获取API使用说明
                   - 返回：本说明文档
                
                注意事项：
                - 所有接口都返回统一的ApiResponse格式
                - 描述长度限制为2000字符
                - 需要配置DEEPSEEK_API_KEY环境变量
                """;
        
        return ResponseEntity.ok(ApiResponse.success(apiInfo));
    }
}