package com.rwm.controller;

import com.rwm.dto.request.UpdateProfileRequest;
import com.rwm.dto.response.Result;
import com.rwm.entity.User;
import com.rwm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
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
}
