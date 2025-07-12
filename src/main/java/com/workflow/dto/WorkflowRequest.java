package com.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 工作流生成请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRequest {
    
    /**
     * 用户的自然语言描述
     */
    @NotBlank(message = "描述不能为空")
    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;
    
    /**
     * 用户ID（可选）
     */
    private String userId;
    
    /**
     * 请求ID（用于追踪）
     */
    private String requestId;
}