package com.rwm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rwm.dto.request.LoginRequest;
import com.rwm.dto.request.RegisterRequest;
import com.rwm.dto.response.LoginResponse;
import com.rwm.entity.User;
import com.rwm.mapper.UserMapper;
import com.rwm.service.UserService;
import com.rwm.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public User register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        User existingUser = findByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("Username already exists");
        }
        
        // 检查邮箱是否已存在（如果提供了邮箱）
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().trim().isEmpty()) {
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", registerRequest.getEmail());
            User existingEmailUser = userMapper.selectOne(emailQuery);
            if (existingEmailUser != null) {
                throw new RuntimeException("Email already registered");
            }
        }
        
        // 创建新用户
        User user = new User();
        BeanUtils.copyProperties(registerRequest, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(false);
        
        // 保存用户
        int result = userMapper.insert(user);
        if (result > 0) {
            log.info("用户注册成功: {}", user.getUsername());
            return user;
        } else {
            throw new RuntimeException("User registration failed");
        }
    }
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 查找用户
        User user = findByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("Invalid username or password");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        // 生成tokens
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
        
        // 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
        
        log.info("用户登录成功: {}", user.getUsername());
        return new LoginResponse(accessToken, refreshToken, 3600L, "Bearer", userInfo);
    }
    
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        try {
            // 验证刷新令牌
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                throw new RuntimeException("Invalid or expired refresh token");
            }
            
            // 从令牌中获取用户信息
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            
            // 查找用户
            User user = findById(userId);
            if (user == null || !user.getUsername().equals(username)) {
                throw new RuntimeException("User not found");
            }
            
            // 生成新的访问令牌
            String newAccessToken = jwtUtil.generateAccessToken(username, userId);
            
            // 构建用户信息
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
            );
            
            log.info("刷新令牌成功: {}", username);
            return new LoginResponse(newAccessToken, refreshToken, 3600L, "Bearer", userInfo);
            
        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed");
        }
    }
    
    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }
    
    @Override
    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }
}
