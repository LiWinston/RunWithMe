package com.rwm.weather.controller;

import com.rwm.dto.response.Result;
import com.rwm.weather.cache.WeatherGeoCacheService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天气缓存管理控制器
 * 提供缓存管理的API端点
 */
@RestController
@RequestMapping("/api/weather/cache")
public class WeatherCacheController {
    
    private final WeatherGeoCacheService cacheService;
    
    public WeatherCacheController(WeatherGeoCacheService cacheService) {
        this.cacheService = cacheService;
    }
    
    /**
     * 手动清理过期缓存
     */
    @PostMapping("/cleanup")
    public Result<String> cleanupCache() {
        cacheService.cleanupExpiredCache();
        return Result.ok("Cache cleanup completed successfully");
    }
}
