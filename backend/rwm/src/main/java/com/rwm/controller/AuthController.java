package com.rwm.controller;

import com.rwm.dto.request.LoginRequest;
import com.rwm.dto.request.RegisterRequest;
import com.rwm.dto.response.Result;
import com.rwm.dto.response.LoginResponse;
import com.rwm.entity.User;
import com.rwm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Result<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.register(registerRequest);
            return ResponseEntity.ok(Result.ok("Registration successful", "User " + user.getUsername() + " registered successfully"));
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = userService.login(loginRequest);
            return ResponseEntity.ok(Result.ok("Login successful", loginResponse));
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
    
    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<Result<LoginResponse>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error("Refresh token cannot be empty"));
            }
            
            LoginResponse loginResponse = userService.refreshToken(refreshToken);
            return ResponseEntity.ok(Result.ok("Token refresh successful", loginResponse));
        } catch (Exception e) {
            log.error("令牌刷新失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
    
    /**
     * 登出（客户端删除token即可，服务端可以选择将token加入黑名单）
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<String>> logout() {
        // 这里可以实现token黑名单逻辑，暂时简单返回成功
        return ResponseEntity.ok(Result.ok("Logout successful"));
    }
}
