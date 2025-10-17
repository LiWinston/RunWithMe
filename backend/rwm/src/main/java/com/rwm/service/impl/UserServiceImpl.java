package com.rwm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rwm.dto.FitnessGoal;
import com.rwm.dto.request.LoginRequest;
import com.rwm.dto.request.RegisterRequest;
import com.rwm.dto.request.UpdateFitnessGoalRequest;
import com.rwm.dto.request.UpdateProfileRequest;
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
    
    @Override
    public User updateProfile(Long userId, UpdateProfileRequest updateRequest) {
        // 查找用户
        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        // 如果更新邮箱，检查邮箱是否已被其他用户使用
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
            if (!updateRequest.getEmail().equals(user.getEmail())) {
                QueryWrapper<User> emailQuery = new QueryWrapper<>();
                emailQuery.eq("email", updateRequest.getEmail());
                emailQuery.ne("id", userId);
                User existingEmailUser = userMapper.selectOne(emailQuery);
                if (existingEmailUser != null) {
                    throw new RuntimeException("Email already in use");
                }
            }
        }
        
        // 更新用户信息
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getGender() != null) {
            user.setGender(updateRequest.getGender());
        }
        if (updateRequest.getAge() != null) {
            user.setAge(updateRequest.getAge());
        }
        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getFitnessLevel() != null) {
            user.setFitnessLevel(updateRequest.getFitnessLevel());
        }
        if (updateRequest.getHeight() != null) {
            user.setHeight(updateRequest.getHeight());
        }
        if (updateRequest.getWeight() != null) {
            user.setWeight(updateRequest.getWeight());
        }
        if (updateRequest.getWeeklyAvailability() != null) {
            user.setWeeklyAvailability(updateRequest.getWeeklyAvailability());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("用户资料更新成功: {}", user.getUsername());
            return user;
        } else {
            throw new RuntimeException("Failed to update user profile");
        }
    }
    
    @Override
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("修改密码，用户ID: {}", userId);
        
        // 查找用户
        User user = findById(userId);
        if (user == null) {
            log.error("用户不存在，ID: {}", userId);
            throw new RuntimeException("User not found");
        }
        
        // 验证当前密码是否正确
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("当前密码错误，用户: {}", user.getUsername());
            return false;
        }
        
        // 加密新密码
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        user.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("密码修改成功，用户: {}", user.getUsername());
            return true;
        } else {
            log.error("密码修改失败，用户: {}", user.getUsername());
            throw new RuntimeException("Failed to change password");
        }
    }
    
    @Override
    public FitnessGoal updateSlogan(Long userId, String slogan) {
        log.info("更新健身口号，用户ID: {}", userId);
        
        User user = findById(userId);
        if (user == null) {
            log.error("用户不存在，ID: {}", userId);
            throw new RuntimeException("User not found");
        }
        
        // 获取或创建FitnessGoal对象
        FitnessGoal fitnessGoal = user.getFitnessGoal();
        if (fitnessGoal == null) {
            fitnessGoal = new FitnessGoal();
        }
        
        // 更新slogan（保留其他字段）
        fitnessGoal.setSlogan(slogan);
        user.setFitnessGoal(fitnessGoal);
        user.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("健身口号更新成功，用户: {}, 口号: {}", user.getUsername(), slogan);
            return fitnessGoal;
        } else {
            log.error("健身口号更新失败，用户: {}", user.getUsername());
            throw new RuntimeException("Failed to update slogan");
        }
    }
    
    @Override
    public FitnessGoal updateFitnessGoal(Long userId, UpdateFitnessGoalRequest goalRequest) {
        log.info("更新健身目标，用户ID: {}", userId);
        
        User user = findById(userId);
        if (user == null) {
            log.error("用户不存在，ID: {}", userId);
            throw new RuntimeException("User not found");
        }
        
        // 获取或创建FitnessGoal对象
        FitnessGoal fitnessGoal = user.getFitnessGoal();
        if (fitnessGoal == null) {
            fitnessGoal = new FitnessGoal();
        }
        
        // 更新目标值（保留slogan和其他字段）
        if (goalRequest.getWeeklyDistanceKm() != null) {
            fitnessGoal.setWeeklyDistanceKm(goalRequest.getWeeklyDistanceKm());
        }
        if (goalRequest.getWeeklyWorkouts() != null) {
            fitnessGoal.setWeeklyWorkouts(goalRequest.getWeeklyWorkouts());
        }
        if (goalRequest.getTargetAvgPaceSecPerKm() != null) {
            fitnessGoal.setTargetAvgPaceSecPerKm(goalRequest.getTargetAvgPaceSecPerKm());
        }
        if (goalRequest.getWeeklyCalories() != null) {
            fitnessGoal.setWeeklyCalories(goalRequest.getWeeklyCalories());
        }
        
        user.setFitnessGoal(fitnessGoal);
        user.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("健身目标更新成功，用户: {}", user.getUsername());
            return fitnessGoal;
        } else {
            log.error("健身目标更新失败，用户: {}", user.getUsername());
            throw new RuntimeException("Failed to update fitness goal");
        }
    }
}
