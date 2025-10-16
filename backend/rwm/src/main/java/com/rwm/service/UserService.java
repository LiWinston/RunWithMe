package com.rwm.service;

import com.rwm.dto.request.LoginRequest;
import com.rwm.dto.request.RegisterRequest;
import com.rwm.dto.request.UpdateProfileRequest;
import com.rwm.dto.response.LoginResponse;
import com.rwm.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    User register(RegisterRequest registerRequest);
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 刷新访问令牌
     */
    LoginResponse refreshToken(String refreshToken);
    
    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);
    
    /**
     * 根据用户ID查找用户
     */
    User findById(Long userId);
    
    /**
     * 更新用户资料
     */
    User updateProfile(Long userId, UpdateProfileRequest updateRequest);
}
