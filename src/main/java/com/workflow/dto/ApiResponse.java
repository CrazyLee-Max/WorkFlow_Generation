package com.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 通用API响应包装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    
    /**
     * 响应状态码
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 时间戳
     */
    private long timestamp;
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 成功响应（带请求ID）
     */
    public static <T> ApiResponse<T> success(T data, String requestId) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("成功")
                .data(data)
                .requestId(requestId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 错误响应（带请求ID）
     */
    public static <T> ApiResponse<T> error(int code, String message, String requestId) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .requestId(requestId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}