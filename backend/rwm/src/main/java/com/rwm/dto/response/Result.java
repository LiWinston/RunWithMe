package com.rwm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一业务响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    private int code; // 0: 成功, 1: 失败
    
    private String message;
    
    private T data;
    
    public static <T> Result<T> ok() {
        return new Result<>(0, "Operation successful", null);
    }
    
    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "Operation successful", data);
    }
    
    public static <T> Result<T> ok(String message) {
        return new Result<>(0, message, null);
    }
    
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(0, message, data);
    }
    
    public static <T> Result<T> error() {
        return new Result<>(1, "Operation failed", null);
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(1, message, null);
    }
    
    public static <T> Result<T> error(String message, T data) {
        return new Result<>(1, message, data);
    }
    
    // 链式方法
    public Result<T> code(int code) {
        this.code = code;
        return this;
    }
    
    public Result<T> message(String message) {
        this.message = message;
        return this;
    }
    
    public Result<T> data(T data) {
        this.data = data;
        return this;
    }
    
    // 便捷判断方法
    public boolean isSuccess() {
        return this.code == 0;
    }
    
    public boolean isError() {
        return this.code != 0;
    }
}
