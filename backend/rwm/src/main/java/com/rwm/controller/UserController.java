package com.rwm.controller;

import com.rwm.dto.response.Result;
import com.rwm.entity.User;
import com.rwm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            return ResponseEntity.ok(Result.ok("获取用户信息成功", user));
        } else {
            return ResponseEntity.badRequest().body(Result.notFound("用户不存在"));
        }
    }
}
