package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 提供简单的测试接口，用于验证服务是否正常运行
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    
    /**
     * 简单的ping测试
     * 
     * @return pong响应
     */
    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        log.debug("收到ping请求");
        return ApiResponse.success("pong");
    }
    
    /**
     * 获取服务状态信息
     * 
     * @return 服务状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getStatus() {
        log.debug("收到状态查询请求");
        
        Map<String, Object> status = new HashMap<>();
        status.put("service", "workflow-generation-service");
        status.put("status", "running");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "1.0.0");
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("springProfile", System.getProperty("spring.profiles.active", "default"));
        
        return ApiResponse.success(status);
    }
    
    /**
     * 测试异常处理
     * 
     * @return 异常响应
     */
    @GetMapping("/error")
    public ApiResponse<String> testError() {
        log.debug("收到错误测试请求");
        throw new RuntimeException("这是一个测试异常");
    }
}