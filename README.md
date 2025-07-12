# 工作流生成服务

基于SpringBoot + DeepSeek + LangChain4j的AI驱动工作流生成服务。

## 功能特性

### 1. 意图识别
- 自动识别用户输入是否为工作流生成意图
- 支持置信度评估
- 智能过滤非工作流相关请求

### 2. 任务分解
- 将自然语言描述分解为具体执行步骤
- 自动识别所需变量和类型
- 生成详细的执行计划和逻辑描述
- 支持循环、判断等复杂逻辑

## 技术栈

- **Java 17**
- **Spring Boot 3.2.0**
- **DeepSeek API** - AI模型服务
- **LangChain4j** - AI应用框架
- **Maven** - 依赖管理
- **Lombok** - 代码简化

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- DeepSeek API Key

### 2. 配置

#### 设置环境变量
```bash
export DEEPSEEK_API_KEY=your-deepseek-api-key
```

#### 或修改配置文件
编辑 `src/main/resources/application.yml`：
```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: your-deepseek-api-key
```

### 3. 运行

```bash
# 编译项目
mvn clean compile

# 运行服务
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动。

## API 接口

### 基础路径
所有API的基础路径为：`http://localhost:8080/api`

### 1. 完整工作流生成

**POST** `/workflow/generate`

请求体：
```json
{
  "description": "放水至水位1.5m",
  "userId": "user123",
  "requestId": "req-001"
}
```

响应：
- 如果不是工作流意图，返回意图识别结果
- 如果是工作流意图，返回任务分解结果

### 2. 仅意图识别

**POST** `/workflow/intent?description=用户描述`

响应示例：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "isWorkflowIntent": true,
    "confidence": 0.95,
    "intentCategory": "工作流生成",
    "reason": "描述包含明确的操作步骤和目标状态"
  }
}
```

### 3. 仅任务分解

**POST** `/workflow/decompose?description=用户描述`

响应示例：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "plan": "总体执行计划",
    "variables": [
      {
        "name": "targetLevel",
        "type": "double",
        "description": "目标水位",
        "defaultValue": "1.5",
        "required": true
      }
    ],
    "steps": [
      {
        "stepNumber": 1,
        "stepName": "初始化变量",
        "description": "设置目标水位和当前水位",
        "stepType": "action"
      }
    ],
    "logicDescription": "详细的执行逻辑说明"
  }
}
```

### 4. 健康检查

**GET** `/workflow/health`

### 5. API信息

**GET** `/workflow/info`

### 6. 测试接口

- **GET** `/test/ping` - 简单连通性测试
- **GET** `/test/status` - 服务状态信息
- **GET** `/test/error` - 异常处理测试

## 使用示例

### 示例1：水位控制

```bash
curl -X POST "http://localhost:8080/api/workflow/generate" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "放水至水位1.5m，需要监控水位变化，达到目标后自动停止"
  }'
```

### 示例2：意图识别

```bash
curl -X POST "http://localhost:8080/api/workflow/intent?description=今天天气怎么样"
```

## 配置说明

### DeepSeek配置

```yaml
langchain4j:
  open-ai:
    chat-model:
      base-url: https://api.deepseek.com/v1
      api-key: ${DEEPSEEK_API_KEY}
      model-name: deepseek-chat
      temperature: 0.7
      max-tokens: 2000
```

### 工作流配置

```yaml
workflow:
  intent:
    confidence-threshold: 0.8  # 意图识别置信度阈值
    max-retry: 3              # 最大重试次数
  decomposition:
    max-steps: 10             # 最大步骤数
    variable-limit: 20        # 变量数量限制
```

## 开发说明

### 项目结构

```
src/main/java/com/workflow/
├── WorkflowGenerationApplication.java  # 主启动类
├── controller/                         # 控制器层
│   ├── WorkflowController.java        # 工作流API
│   └── TestController.java            # 测试API
├── service/                           # 服务层
│   ├── WorkflowGenerationService.java # 主服务
│   ├── IntentRecognitionService.java  # 意图识别
│   ├── TaskDecompositionService.java  # 任务分解
│   └── DeepSeekApiService.java        # DeepSeek API客户端
├── dto/                               # 数据传输对象
│   ├── WorkflowRequest.java
│   ├── IntentRecognitionResponse.java
│   ├── TaskDecompositionResponse.java
│   └── ...
└── config/                            # 配置类
    ├── LangChain4jConfig.java
    └── GlobalExceptionHandler.java
```

### 扩展开发

1. **添加新的意图类型**：修改 `IntentRecognitionService` 中的提示模板
2. **优化任务分解**：调整 `TaskDecompositionService` 中的分解逻辑
3. **添加新的API**：在 `WorkflowController` 中添加新的端点

## 注意事项

1. **API Key安全**：请妥善保管DeepSeek API Key，不要提交到代码仓库
2. **请求限制**：注意DeepSeek API的调用频率限制
3. **错误处理**：所有API都有统一的错误处理机制
4. **日志记录**：重要操作都有详细的日志记录

## 故障排除

### 常见问题

1. **API Key未配置**
   ```
   错误：DeepSeek API Key未配置
   解决：设置环境变量DEEPSEEK_API_KEY
   ```

2. **网络连接问题**
   ```
   错误：DeepSeek API调用失败
   解决：检查网络连接和API地址
   ```

3. **参数校验失败**
   ```
   错误：参数校验失败
   解决：检查请求参数格式和长度限制
   ```

## 许可证

MIT License