package com.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 工作流变量DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowVariable {
    
    /**
     * 变量名
     */
    private String name;
    
    /**
     * 变量类型
     */
    private String type;
    
    /**
     * 变量描述
     */
    private String description;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 是否必需
     */
    private boolean required;
    
    /**
     * 变量范围或约束
     */
    private String constraints;
}