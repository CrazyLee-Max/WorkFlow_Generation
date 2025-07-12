package com.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.dto.TaskDecompositionResponse;
import com.workflow.dto.WorkflowVariable;
import com.workflow.dto.WorkflowStep;
import com.workflow.service.DeepSeekApiService;
import com.workflow.service.TaskDecompositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 任务分解服务实现类
 * 将用户的工作流描述分解为具体的执行步骤和所需变量
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDecompositionServiceImpl implements TaskDecompositionService {
    
    private final DeepSeekApiService deepSeekApiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${workflow.decomposition.max-steps:10}")
    private int maxSteps;
    
    @Value("${workflow.decomposition.variable-limit:20}")
    private int variableLimit;
    
    /**
     * 任务分解提示模板
     */
    private static final String TASK_DECOMPOSITION_PROMPT = """
        你是一个专业的工作流设计师，擅长将复杂的自然语言描述分解为具体的可执行步骤。
        
        用户描述："{{userInput}}"
        
        请按照以下要求进行任务分解：
        
        1. **任务规划(plan)**：
           - 分析用户需求，制定总体执行计划
           - 识别关键的控制逻辑（循环、判断、顺序执行等）
           - 说明整体的执行流程
        
        2. **变量定义(variables)**：
           - 识别所有需要的变量，确保变量间的关联性和一致性
           - 对于相关联的变量（如员工姓名和员工ID），要建立明确的关联关系
           - 为每个变量指定合适的类型（如：double, boolean, int, String等）
           - 提供清晰的变量描述，说明变量的业务含义
           - 设置合理的默认值和约束条件
           - 对于主键变量（如ID类），要定义格式规范
           - 对于状态变量，要明确依赖关系
        
        3. **执行步骤(steps)**：
           - 将任务分解为具体的执行步骤
           - 每个步骤必须是原子性的，能在单个节点完成
           - 避免复合操作，将复杂任务拆分为多个简单步骤
           - 每个步骤只执行一个明确的操作或判断
           - 每个步骤要有明确的动作和目标
           - 标识步骤类型（action, condition, loop等）
           - 定义步骤间的依赖关系
           - 对于循环和判断，要明确条件表达式
           - 确保步骤中使用的变量在variables中已定义
        
        4. **逻辑描述(logicDescription)**：
           - 详细说明代码的执行逻辑
           - 强调每个步骤的原子性和单节点执行特性
           - 解释循环、判断的具体实现方式
           - 说明异常处理和边界条件
           - 描述变量间的关联验证逻辑
        
        请按照以下JSON格式返回结果：
        {
            "plan": "总体执行计划的描述",
            "variables": [
                {
                    "name": "变量名",
                    "type": "变量类型",
                    "description": "变量描述",
                    "defaultValue": "默认值",
                    "required": true/false,
                    "constraints": "约束条件"
                }
            ],
            "steps": [
                {
                    "stepNumber": 1,
                    "stepName": "步骤名称",
                    "description": "步骤描述",
                    "stepType": "action/condition/loop",
                    "action": "具体动作",
                    "condition": "条件表达式（如果是判断步骤）",
                    "involvedVariables": ["相关变量列表"],
                    "parameters": {"参数键值对"},
                    "prerequisites": ["前置步骤编号"],
                    "isLoop": true/false,
                    "loopCondition": "循环条件（如果是循环步骤）"
                }
            ],
            "logicDescription": "详细的代码逻辑说明",
            "executionOrder": "执行顺序的说明",
            "estimatedDuration": 预估执行时间秒数,
            "complexityLevel": 复杂度等级1-5
        }
        
        示例（放水至水位1.5m）：
        - 变量：valveStatus(boolean), waterLevel(double), targetLevel(double)
        - 步骤：1.初始化变量 2.打开阀门 3.循环监测水位 4.判断是否达到目标 5.关闭阀门
        - 逻辑：使用while循环持续监测，当水位>=目标值时退出循环
        
        注意：
        1. 步骤要具体可执行，避免过于抽象
        2. 变量类型要准确，约束要合理
        3. 循环和判断条件要明确可判断
        4. 考虑异常情况和边界条件
        5. 步骤要用到变量，不要定义无关变量
        6. 变量名和步骤要用中文
        """;
    
    /**
     * 分解任务为具体的执行步骤
     * 
     * @param userInput 用户的自然语言描述
     * @param requestId 请求ID
     * @return 任务分解结果
     */
    @Override
    public TaskDecompositionResponse decomposeTask(String userInput, String requestId) {
        log.info("开始任务分解，requestId: {}, userInput: {}", requestId, userInput);
        
        try {
            // 构建提示
            String prompt = TASK_DECOMPOSITION_PROMPT.replace("{{userInput}}", userInput);
            
            // 调用DeepSeek模型
            String response = deepSeekApiService.chatCompletion(prompt);
            log.debug("DeepSeek模型响应: {}", response);
            
            // 解析响应
            TaskDecompositionResponse result = parseDecompositionResponse(response, requestId);
            
            log.info("任务分解完成，requestId: {}, 变量数量: {}, 步骤数量: {}", 
                    requestId, result.getVariables().size(), result.getSteps().size());
            
            return result;
            
        } catch (Exception e) {
            log.error("任务分解失败，requestId: {}, error: {}", requestId, e.getMessage(), e);
            
            // 返回默认的错误响应
            return createErrorResponse(requestId, e.getMessage());
        }
    }
    
    /**
     * 解析DeepSeek模型的任务分解响应
     */
    private TaskDecompositionResponse parseDecompositionResponse(String response, String requestId) {
        try {
            // 清理响应格式
            response = cleanJsonResponse(response);
            
            // 简化的解析逻辑（实际项目中应使用Jackson等专业JSON库）
            String plan = extractStringValue(response, "plan");
            String logicDescription = extractStringValue(response, "logicDescription");
            String executionOrder = extractStringValue(response, "executionOrder");
            int estimatedDuration = extractIntValue(response, "estimatedDuration", 60);
            int complexityLevel = extractIntValue(response, "complexityLevel", 3);
            
            // 解析变量列表
            List<WorkflowVariable> variables = parseVariables(response);
            
            // 解析步骤列表
            List<WorkflowStep> steps = parseSteps(response);
            
            return TaskDecompositionResponse.builder()
                    .plan(plan)
                    .variables(variables)
                    .steps(steps)
                    .logicDescription(logicDescription)
                    .executionOrder(executionOrder)
                    .requestId(requestId)
                    .timestamp(System.currentTimeMillis())
                    .estimatedDuration(estimatedDuration)
                    .complexityLevel(complexityLevel)
                    .build();
                    
        } catch (Exception e) {
            log.warn("解析任务分解响应失败，使用默认值，error: {}", e.getMessage());
            return createDefaultResponse(requestId);
        }
    }
    
    /**
     * 清理JSON响应格式
     */
    private String cleanJsonResponse(String response) {
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }
    
    /**
     * 从JSON响应中提取字符串值
     */
    private String extractStringValue(String response, String key) {
        try {
            String pattern = "\"" + key + "\": \"";
            int start = response.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                int end = response.indexOf("\"", start);
                if (end != -1) {
                    return response.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.warn("提取字符串值失败，key: {}, error: {}", key, e.getMessage());
        }
        return "未能解析" + key;
    }
    
    /**
     * 从JSON响应中提取整数值
     */
    private int extractIntValue(String response, String key, int defaultValue) {
        try {
            String pattern = "\"" + key + "\": ";
            int start = response.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                int end = response.indexOf(",", start);
                if (end == -1) {
                    end = response.indexOf("}", start);
                }
                if (end != -1) {
                    String valueStr = response.substring(start, end).trim();
                    return Integer.parseInt(valueStr);
                }
            }
        } catch (Exception e) {
            log.warn("提取整数值失败，key: {}, error: {}", key, e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * 解析变量列表
     */
    private List<WorkflowVariable> parseVariables(String response) {
        List<WorkflowVariable> variables = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode variablesNode = rootNode.get("variables");
            
            if (variablesNode != null && variablesNode.isArray()) {
                for (JsonNode variableNode : variablesNode) {
                    WorkflowVariable variable = WorkflowVariable.builder()
                            .name(getStringValue(variableNode, "name", "unknown"))
                            .type(getStringValue(variableNode, "type", "String"))
                            .description(getStringValue(variableNode, "description", "无描述"))
                            .defaultValue(getStringValue(variableNode, "defaultValue", ""))
                            .required(getBooleanValue(variableNode, "required", true))
                            .constraints(getStringValue(variableNode, "constraints", "无约束"))
                            .build();
                    variables.add(variable);
                }
            }
            
            log.debug("成功解析变量列表，数量: {}", variables.size());
            
        } catch (Exception e) {
            log.warn("解析变量列表失败，使用默认变量: {}", e.getMessage());
            // 返回默认变量
            variables.add(WorkflowVariable.builder()
                    .name("defaultVariable")
                    .type("String")
                    .description("默认变量")
                    .defaultValue("")
                    .required(false)
                    .constraints("无约束")
                    .build());
        }
        
        return variables;
    }
    
    /**
     * 解析步骤列表
     */
    private List<WorkflowStep> parseSteps(String response) {
        List<WorkflowStep> steps = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode stepsNode = rootNode.get("steps");
            
            if (stepsNode != null && stepsNode.isArray()) {
                for (JsonNode stepNode : stepsNode) {
                    WorkflowStep step = WorkflowStep.builder()
                            .stepNumber(getIntValue(stepNode, "stepNumber", steps.size() + 1))
                            .stepName(getStringValue(stepNode, "stepName", "未命名步骤"))
                            .description(getStringValue(stepNode, "description", "无描述"))
                            .stepType(getStringValue(stepNode, "stepType", "action"))
                            .action(getStringValue(stepNode, "action", "process"))
                            .condition(getStringValue(stepNode, "condition", null))
                            .involvedVariables(parseStringList(stepNode, "involvedVariables"))
                            .parameters(parseParametersMap(stepNode, "parameters"))
                            .prerequisites(parseIntegerList(stepNode, "prerequisites"))
                            .isLoop(getBooleanValue(stepNode, "isLoop", false))
                            .loopCondition(getStringValue(stepNode, "loopCondition", null))
                            .build();
                    steps.add(step);
                }
            }
            
            log.debug("成功解析步骤列表，数量: {}", steps.size());
            
        } catch (Exception e) {
            log.warn("解析步骤列表失败，使用默认步骤: {}", e.getMessage());
            // 返回默认步骤
            steps.add(WorkflowStep.builder()
                    .stepNumber(1)
                    .stepName("默认步骤")
                    .description("默认步骤描述")
                    .stepType("action")
                    .action("process")
                    .involvedVariables(new ArrayList<>())
                    .parameters(new HashMap<>())
                    .prerequisites(new ArrayList<>())
                    .isLoop(false)
                    .build());
        }
        
        return steps;
    }
    
    /**
     * 从JSON节点获取字符串值
     */
    private String getStringValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : defaultValue;
    }
    
    /**
     * 从JSON节点获取布尔值
     */
    private boolean getBooleanValue(JsonNode node, String fieldName, boolean defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asBoolean() : defaultValue;
    }
    
    /**
     * 从JSON节点获取整数值
     */
    private int getIntValue(JsonNode node, String fieldName, int defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asInt() : defaultValue;
    }
    
    /**
     * 解析字符串列表
     */
    private List<String> parseStringList(JsonNode node, String fieldName) {
        List<String> result = new ArrayList<>();
        JsonNode arrayNode = node.get(fieldName);
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                result.add(item.asText());
            }
        }
        return result;
    }
    
    /**
     * 解析整数列表
     */
    private List<Integer> parseIntegerList(JsonNode node, String fieldName) {
        List<Integer> result = new ArrayList<>();
        JsonNode arrayNode = node.get(fieldName);
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                result.add(item.asInt());
            }
        }
        return result;
    }
    
    /**
     * 解析参数映射
     */
    private Map<String, Object> parseParametersMap(JsonNode node, String fieldName) {
        Map<String, Object> result = new HashMap<>();
        JsonNode parametersNode = node.get(fieldName);
        if (parametersNode != null && parametersNode.isObject()) {
            parametersNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode valueNode = entry.getValue();
                if (valueNode.isTextual()) {
                    result.put(key, valueNode.asText());
                } else if (valueNode.isNumber()) {
                    result.put(key, valueNode.asText());
                } else if (valueNode.isBoolean()) {
                    result.put(key, valueNode.asBoolean());
                } else {
                    result.put(key, valueNode.toString());
                }
            });
        }
        return result;
    }
    
    /**
     * 创建错误响应
     */
    private TaskDecompositionResponse createErrorResponse(String requestId, String errorMessage) {
        return TaskDecompositionResponse.builder()
                .plan("任务分解失败: " + errorMessage)
                .variables(new ArrayList<>())
                .steps(new ArrayList<>())
                .logicDescription("由于错误无法生成逻辑描述")
                .executionOrder("无法确定执行顺序")
                .requestId(requestId)
                .timestamp(System.currentTimeMillis())
                .estimatedDuration(0)
                .complexityLevel(0)
                .build();
    }
    
    /**
     * 创建默认响应
     */
    private TaskDecompositionResponse createDefaultResponse(String requestId) {
        return TaskDecompositionResponse.builder()
                .plan("基于用户输入生成的基础工作流计划")
                .variables(parseVariables(""))
                .steps(parseSteps(""))
                .logicDescription("包含初始化、执行、监测和完成四个主要阶段")
                .executionOrder("按步骤编号顺序执行，支持循环和条件判断")
                .requestId(requestId)
                .timestamp(System.currentTimeMillis())
                .estimatedDuration(60)
                .complexityLevel(3)
                .build();
    }
}
