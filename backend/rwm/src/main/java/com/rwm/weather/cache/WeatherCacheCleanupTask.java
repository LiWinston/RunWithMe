package com.rwm.weather.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 天气缓存清理定时任务
 */
@Component
public class WeatherCacheCleanupTask {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherCacheCleanupTask.class);
    
    private final WeatherGeoCacheService cacheService;
    
    public WeatherCacheCleanupTask(WeatherGeoCacheService cacheService) {
        this.cacheService = cacheService;
    }
    
    /**
     * 每15分钟清理一次过期的缓存条目
     */
    @Scheduled(fixedRate = 15 * 60 * 1000) // 15分钟
    public void cleanupExpiredCache() {
        logger.debug("Running weather cache cleanup task");
        cacheService.cleanupExpiredCache();
    }
}
