package com.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 工作流生成服务主启动类
 * 集成DeepSeek模型和LangChain4j框架，提供AI驱动的工作流生成服务
 */
@SpringBootApplication
public class WorkflowGenerationApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowGenerationApplication.class, args);
    }
}