package com.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 任务分解响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDecompositionResponse {
    
    /**
     * 任务规划描述
     */
    private String plan;
    
    /**
     * 需要创建的变量列表
     */
    private List<WorkflowVariable> variables;
    
    /**
     * 工作流步骤列表
     */
    private List<WorkflowStep> steps;
    
    /**
     * 代码逻辑描述
     */
    private String logicDescription;
    
    /**
     * 执行顺序说明
     */
    private String executionOrder;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 处理时间戳
     */
    private long timestamp;
    
    /**
     * 预估执行时间（秒）
     */
    private int estimatedDuration;
    
    /**
     * 复杂度评级（1-5）
     */
    private int complexityLevel;
}