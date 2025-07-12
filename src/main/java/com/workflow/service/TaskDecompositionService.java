package com.workflow.service;

import com.workflow.dto.TaskDecompositionResponse;

/**
 * 任务分解服务接口
 * 负责将用户的自然语言描述分解为具体的工作流步骤
 */
public interface TaskDecompositionService {
    
    /**
     * 分解任务为工作流步骤
     * 
     * @param userInput 用户输入的任务描述
     * @param requestId 请求ID
     * @return 任务分解响应
     */
    TaskDecompositionResponse decomposeTask(String userInput, String requestId);
}