package com.rwm.weather.cache;

import com.rwm.weather.dto.CurrentConditionsResponse;
import com.rwm.weather.dto.HourlyForecastResponse;
import com.rwm.weather.dto.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoResults;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.data.redis.domain.geo.Point;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 天气地理位置缓存服务
 * 使用Redis的Geo数据类型实现基于地理位置的天气缓存
 */
@Service
public class WeatherGeoCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherGeoCacheService.class);
    
    // Redis Key前缀
    private static final String GEO_KEY = "weather:geo:locations";
    private static final String CACHE_DATA_PREFIX = "weather:cache:data:";
    
    // 缓存配置
    private static final double CACHE_RADIUS_KM = 10.0; // 10公里范围内认为是相近位置
    private static final long CACHE_DURATION_MINUTES = 30; // 缓存30分钟
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final GeoOperations<String, Object> geoOperations;
    
    public WeatherGeoCacheService(@Qualifier("weatherRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.geoOperations = redisTemplate.opsForGeo();
    }
    
    /**
     * 查找附近的天气缓存
     * @param location 查询位置
     * @return 如果找到附近的有效缓存，返回缓存条目；否则返回空
     */
    public Optional<WeatherCacheEntry> findNearbyCache(Location location) {
        try {
            logger.debug("Searching for nearby weather cache for location: {}", location);
            
            // 在指定半径内查找附近的位置
            GeoResults<org.springframework.data.redis.domain.geo.GeoLocation<Object>> results =
                geoOperations.radius(GEO_KEY, 
                    new Point(location.getLongitude(), location.getLatitude()),
                    new Distance(CACHE_RADIUS_KM, Metrics.KILOMETERS));
            
            if (results == null || results.getContent().isEmpty()) {
                logger.debug("No nearby cached locations found");
                return Optional.empty();
            }
            
            // 查找最近的有效缓存
            for (var result : results.getContent()) {
                String cacheKey = (String) result.getContent().getName();
                logger.debug("Found nearby location with cache key: {}, distance: {} km", 
                    cacheKey, result.getDistance().getValue());
                
                Optional<WeatherCacheEntry> cacheEntry = getCacheData(cacheKey);
                if (cacheEntry.isPresent() && !cacheEntry.get().isExpired()) {
                    logger.debug("Found valid nearby cache entry");
                    return cacheEntry;
                }
            }
            
            logger.debug("No valid nearby cache entries found");
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error while searching for nearby weather cache", e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存当前天气状况
     * @param location 位置
     * @param currentConditions 当前天气状况
     */
    public void cacheCurrentConditions(Location location, CurrentConditionsResponse currentConditions) {
        try {
            String cacheKey = generateCacheKey();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(CACHE_DURATION_MINUTES);
            
            WeatherCacheEntry entry = new WeatherCacheEntry(cacheKey, location, now, expiresAt);
            entry.setCurrentConditions(currentConditions);
            
            // 保存地理位置信息
            geoOperations.add(GEO_KEY, new Point(location.getLongitude(), location.getLatitude()), cacheKey);
            
            // 保存缓存数据
            saveCacheData(cacheKey, entry);
            
            logger.debug("Cached current weather conditions for location: {} with key: {}", location, cacheKey);
            
        } catch (Exception e) {
            logger.error("Error while caching current weather conditions", e);
        }
    }
    
    /**
     * 缓存每小时天气预报
     * @param location 位置
     * @param hourlyForecast 每小时天气预报
     */
    public void cacheHourlyForecast(Location location, HourlyForecastResponse hourlyForecast) {
        try {
            String cacheKey = generateCacheKey();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(CACHE_DURATION_MINUTES);
            
            WeatherCacheEntry entry = new WeatherCacheEntry(cacheKey, location, now, expiresAt);
            entry.setHourlyForecast(hourlyForecast);
            
            // 保存地理位置信息
            geoOperations.add(GEO_KEY, new Point(location.getLongitude(), location.getLatitude()), cacheKey);
            
            // 保存缓存数据
            saveCacheData(cacheKey, entry);
            
            logger.debug("Cached hourly forecast for location: {} with key: {}", location, cacheKey);
            
        } catch (Exception e) {
            logger.error("Error while caching hourly forecast", e);
        }
    }
    
    /**
     * 清理过期的缓存条目
     */
    public void cleanupExpiredCache() {
        try {
            logger.debug("Starting cleanup of expired weather cache entries");
            
            // 获取所有地理位置
            GeoResults<org.springframework.data.redis.domain.geo.GeoLocation<Object>> allLocations = 
                geoOperations.radius(GEO_KEY, 
                    new Point(0, 0), 
                    new Distance(Double.MAX_VALUE, Metrics.KILOMETERS));
            
            if (allLocations == null || allLocations.getContent().isEmpty()) {
                return;
            }
            
            int cleanedCount = 0;
            for (var result : allLocations.getContent()) {
                String cacheKey = (String) result.getContent().getName();
                Optional<WeatherCacheEntry> cacheEntry = getCacheData(cacheKey);
                
                if (cacheEntry.isEmpty() || cacheEntry.get().isExpired()) {
                    // 删除过期的地理位置和缓存数据
                    geoOperations.remove(GEO_KEY, cacheKey);
                    deleteCacheData(cacheKey);
                    cleanedCount++;
                    logger.debug("Cleaned up expired cache entry: {}", cacheKey);
                }
            }
            
            logger.info("Cleaned up {} expired weather cache entries", cleanedCount);
            
        } catch (Exception e) {
            logger.error("Error during cache cleanup", e);
        }
    }
    
    /**
     * 生成唯一的缓存键
     */
    private String generateCacheKey() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 保存缓存数据
     */
    private void saveCacheData(String cacheKey, WeatherCacheEntry entry) {
        String dataKey = CACHE_DATA_PREFIX + cacheKey;
        redisTemplate.opsForValue().set(dataKey, entry, CACHE_DURATION_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * 获取缓存数据
     */
    private Optional<WeatherCacheEntry> getCacheData(String cacheKey) {
        try {
            String dataKey = CACHE_DATA_PREFIX + cacheKey;
            Object data = redisTemplate.opsForValue().get(dataKey);
            if (data instanceof WeatherCacheEntry) {
                return Optional.of((WeatherCacheEntry) data);
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error while retrieving cache data for key: {}", cacheKey, e);
            return Optional.empty();
        }
    }
    
    /**
     * 删除缓存数据
     */
    private void deleteCacheData(String cacheKey) {
        String dataKey = CACHE_DATA_PREFIX + cacheKey;
        redisTemplate.delete(dataKey);
    }
}
