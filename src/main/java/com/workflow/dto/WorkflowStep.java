package com.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 工作流步骤DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStep {
    
    /**
     * 步骤序号
     */
    private int stepNumber;
    
    /**
     * 步骤名称
     */
    private String stepName;
    
    /**
     * 步骤描述
     */
    private String description;
    
    /**
     * 步骤类型（如：action, condition, loop等）
     */
    private String stepType;
    
    /**
     * 执行动作
     */
    private String action;
    
    /**
     * 条件表达式（用于判断步骤）
     */
    private String condition;
    
    /**
     * 涉及的变量
     */
    private List<String> involvedVariables;
    
    /**
     * 步骤参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 前置步骤
     */
    private List<Integer> prerequisites;
    
    /**
     * 是否为循环步骤
     */
    private boolean isLoop;
    
    /**
     * 循环条件
     */
    private String loopCondition;
}