package com.rwm.filter;

import com.rwm.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    // 不需要认证的URL路径
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // 检查是否为排除路径
        if (isExcludedPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "No valid authentication token provided");
            return;
        }
        
        String token = authHeader.substring(7); // 移除 "Bearer " 前缀
        
        try {
            // 验证访问令牌
            if (!jwtUtil.validateAccessToken(token)) {
                sendUnauthorizedResponse(response, "Invalid or expired access token");
                return;
            }
            
            // 从token中获取用户信息并设置到请求属性中
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            request.setAttribute("currentUsername", username);
            request.setAttribute("currentUserId", userId);
            
            log.debug("JWT认证成功，用户: {}, ID: {}", username, userId);
            
        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Token parsing failed");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 检查是否为排除路径
     */
    private boolean isExcludedPath(String requestURI) {
        return EXCLUDED_PATHS.stream().anyMatch(requestURI::startsWith);
    }
    
    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"code\": 401, \"message\": \"%s\", \"data\": null}", message
        ));
    }
}
