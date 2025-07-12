package com.workflow.config;

import com.workflow.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理应用中的各种异常，返回标准的API响应格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理请求体参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        
        log.warn("请求参数校验失败: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        ApiResponse<String> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(), 
                "参数校验失败: " + errorMessage
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理表单参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<String>> handleBindException(BindException ex) {
        log.warn("表单参数校验失败: {}", ex.getMessage());
        
        String errorMessage = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        ApiResponse<String> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(), 
                "参数校验失败: " + errorMessage
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理请求参数校验异常（@Validated注解）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolation(
            ConstraintViolationException ex) {
        
        log.warn("约束校验失败: {}", ex.getMessage());
        
        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        ApiResponse<String> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(), 
                "参数校验失败: " + errorMessage
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理参数类型转换异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        
        log.warn("参数类型转换失败: {}", ex.getMessage());
        
        String errorMessage = String.format(
                "参数 '%s' 的值 '%s' 无法转换为 %s 类型", 
                ex.getName(), 
                ex.getValue(), 
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知"
        );
        
        ApiResponse<String> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(), 
                errorMessage
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgument(
            IllegalArgumentException ex) {
        
        log.warn("非法参数异常: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(), 
                "参数错误: " + ex.getMessage()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(
            RuntimeException ex) {
        
        log.error("运行时异常: {}", ex.getMessage(), ex);
        
        ApiResponse<String> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                "服务内部错误: " + ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        log.error("未处理的异常: {}", ex.getMessage(), ex);
        
        ApiResponse<String> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                "系统异常，请联系管理员"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}