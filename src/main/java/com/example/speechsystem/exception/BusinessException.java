package com.example.speechsystem.exception;

/**
 * 自定义业务异常类
 * 用于处理业务逻辑中的可预见异常
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}