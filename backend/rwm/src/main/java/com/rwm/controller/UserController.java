package com.rwm.controller;

import com.rwm.dto.FitnessGoal;
import com.rwm.dto.request.ChangePasswordRequest;
import com.rwm.dto.request.UpdateFitnessGoalRequest;
import com.rwm.dto.request.UpdateProfileRequest;
import com.rwm.dto.request.UpdateSloganRequest;
import com.rwm.dto.response.Result;
import com.rwm.entity.User;
import com.rwm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<Result<User>> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        User user = userService.findById(userId);
        
        if (user != null) {
            // 不返回密码字段
            user.setPassword(null);
            return ResponseEntity.ok(Result.ok("Get user info successful", user));
        } else {
            return ResponseEntity.ok(Result.error("User not found"));
        }
    }
    
    /**
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<Result<User>> updateCurrentUser(
            HttpServletRequest request,
            @RequestBody UpdateProfileRequest updateRequest) {
        try {
            Long userId = (Long) request.getAttribute("currentUserId");
            User updatedUser = userService.updateProfile(userId, updateRequest);
            
            if (updatedUser != null) {
                // 不返回密码字段
                updatedUser.setPassword(null);
                return ResponseEntity.ok(Result.ok("Profile updated successfully", updatedUser));
            } else {
                return ResponseEntity.ok(Result.error("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("Failed to update profile: " + e.getMessage()));
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public ResponseEntity<Result<String>> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            Long userId = (Long) request.getAttribute("currentUserId");
            
            // 验证两次新密码是否一致
            if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                return ResponseEntity.ok(Result.error("New password and confirm password do not match"));
            }
            
            // 调用service修改密码
            boolean success = userService.changePassword(
                userId, 
                changePasswordRequest.getCurrentPassword(), 
                changePasswordRequest.getNewPassword()
            );
            
            if (success) {
                return ResponseEntity.ok(Result.ok("Password changed successfully"));
            } else {
                return ResponseEntity.ok(Result.error("Current password is incorrect"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("Failed to change password: " + e.getMessage()));
        }
    }
    
    /**
     * 更新健身口号
     */
    @PutMapping("/slogan")
    public ResponseEntity<Result<FitnessGoal>> updateSlogan(
            HttpServletRequest request,
            @Valid @RequestBody UpdateSloganRequest sloganRequest) {
        try {
            Long userId = (Long) request.getAttribute("currentUserId");
            FitnessGoal fitnessGoal = userService.updateSlogan(userId, sloganRequest.getSlogan());
            return ResponseEntity.ok(Result.ok("Slogan updated successfully", fitnessGoal));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("Failed to update slogan: " + e.getMessage()));
        }
    }
    
    /**
     * 获取健身目标
     */
    @GetMapping("/fitness-goal")
    public ResponseEntity<Result<FitnessGoal>> getFitnessGoal(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("currentUserId");
            User user = userService.findById(userId);
            if (user != null) {
                FitnessGoal fitnessGoal = user.getFitnessGoal();
                if (fitnessGoal == null) {
                    fitnessGoal = new FitnessGoal();
                }
                return ResponseEntity.ok(Result.ok("Get fitness goal successful", fitnessGoal));
            } else {
                return ResponseEntity.ok(Result.error("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("Failed to get fitness goal: " + e.getMessage()));
        }
    }
    
    /**
     * 更新健身目标
     */
    @PutMapping("/fitness-goal")
    public ResponseEntity<Result<FitnessGoal>> updateFitnessGoal(
            HttpServletRequest request,
            @Valid @RequestBody UpdateFitnessGoalRequest goalRequest) {
        try {
            Long userId = (Long) request.getAttribute("currentUserId");
            FitnessGoal fitnessGoal = userService.updateFitnessGoal(userId, goalRequest);
            return ResponseEntity.ok(Result.ok("Fitness goal updated successfully", fitnessGoal));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("Failed to update fitness goal: " + e.getMessage()));
        }
    }
}
