package com.rwm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    private int code;
    
    private String message;
    
    private T data;
    
    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null);
    }
    
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }
    
    public static <T> Result<T> ok(String message) {
        return new Result<>(200, message, null);
    }
    
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data);
    }
    
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null);
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
    
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
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
        return this.code == 200;
    }
    
    public boolean isError() {
        return this.code != 200;
    }
    
    // 常用状态码快捷方法
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }
    
    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未授权", null);
    }
    
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }
    
    public static <T> Result<T> forbidden() {
        return new Result<>(403, "禁止访问", null);
    }
    
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }
    
    public static <T> Result<T> notFound() {
        return new Result<>(404, "资源未找到", null);
    }
    
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null);
    }
}
