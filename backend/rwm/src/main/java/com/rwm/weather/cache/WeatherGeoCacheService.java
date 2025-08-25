package com.rwm.weather.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rwm.weather.dto.CurrentConditionsResponse;
import com.rwm.weather.dto.HourlyForecastResponse;
import com.rwm.weather.dto.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis Geo的天气缓存服务
 * 使用Redis TTL机制自动清理过期数据，无需定时任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherGeoCacheService {
    
    // Redis Key前缀
    private static final String GEO_KEY = "weather:geo:locations";
    private static final String CACHE_DATA_PREFIX = "weather:cache:data:";
    
    // 缓存配置
    private static final double CACHE_RADIUS_KM = 10.0; // 10公里范围内认为是相近位置
    private static final long CACHE_DURATION_MINUTES = 30; // 缓存数据30分钟
    private static final long GEO_CLEANUP_HOURS = 1; // Geo数据1小时清理一次
    
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 查找附近的天气缓存
     * @param location 查询位置
     * @return 如果找到附近的有效缓存，返回缓存条目；否则返回空
     */
    public Optional<WeatherCacheEntry> findNearbyCache(Location location) {
        try {
            log.debug("Searching for nearby weather cache for location: {}", location);
            
            // 在指定半径内查找附近的位置
            GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                stringRedisTemplate.opsForGeo().radius(GEO_KEY, 
                    new Circle(
                        new Point(location.getLongitude(), location.getLatitude()),
                        new Distance(CACHE_RADIUS_KM, Metrics.KILOMETERS)
                    ));
            
            if (results == null || results.getContent().isEmpty()) {
                log.debug("No nearby cached locations found");
                return Optional.empty();
            }
            
            // 查找最近的有效缓存
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results.getContent()) {
                String cacheKey = result.getContent().getName();
                log.debug("Found nearby location with cache key: {}, distance: {} km", 
                    cacheKey, result.getDistance().getValue());
                
                Optional<WeatherCacheEntry> cacheEntry = getCacheData(cacheKey);
                if (cacheEntry.isPresent()) {
                    log.debug("Found valid nearby cache entry");
                    return cacheEntry;
                }
            }
            
            log.debug("No valid nearby cache entries found");
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error while searching for nearby weather cache", e);
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
            
            // 保存地理位置信息（设置较长的TTL，定期清理）
            stringRedisTemplate.opsForGeo().add(GEO_KEY, 
                new Point(location.getLongitude(), location.getLatitude()), cacheKey);
            stringRedisTemplate.expire(GEO_KEY, GEO_CLEANUP_HOURS, TimeUnit.HOURS);
            
            // 保存缓存数据（设置30分钟TTL，让Redis自动清理）
            saveCacheData(cacheKey, entry);
            
            log.debug("Cached current weather conditions for location: {} with key: {}", location, cacheKey);
            
        } catch (Exception e) {
            log.error("Error while caching current weather conditions", e);
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
            
            // 保存地理位置信息（设置较长的TTL，定期清理）
            stringRedisTemplate.opsForGeo().add(GEO_KEY, 
                new Point(location.getLongitude(), location.getLatitude()), cacheKey);
            stringRedisTemplate.expire(GEO_KEY, GEO_CLEANUP_HOURS, TimeUnit.HOURS);
            
            // 保存缓存数据（设置30分钟TTL，让Redis自动清理）
            saveCacheData(cacheKey, entry);
            
            log.debug("Cached hourly forecast for location: {} with key: {}", location, cacheKey);
            
        } catch (Exception e) {
            log.error("Error while caching hourly forecast", e);
        }
    }

    /**
     * 清理所有缓存（主要用于测试）
     */
    public void clearAllCache() {
        try {
            // 删除地理位置索引
            stringRedisTemplate.delete(GEO_KEY);
            
            // 删除所有缓存数据（这里需要遍历所有可能的key，实际上Redis的TTL会自动清理）
            log.info("Cache cleared manually");
            
        } catch (Exception e) {
            log.error("Error while clearing cache", e);
        }
    }
    
    // 私有辅助方法
    
    private Optional<WeatherCacheEntry> getCacheData(String cacheKey) {
        try {
            String cacheDataKey = CACHE_DATA_PREFIX + cacheKey;
            String data = stringRedisTemplate.opsForValue().get(cacheDataKey);
            
            if (data == null) {
                log.debug("No cache data found for key: {}", cacheKey);
                return Optional.empty();
            }
            
            WeatherCacheEntry entry = objectMapper.readValue(data, WeatherCacheEntry.class);
            log.debug("Retrieved cache entry for key: {}", cacheKey);
            return Optional.of(entry);
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing cache data for key: {}", cacheKey, e);
            return Optional.empty();
        }
    }
    
    private void saveCacheData(String cacheKey, WeatherCacheEntry entry) {
        try {
            String cacheDataKey = CACHE_DATA_PREFIX + cacheKey;
            String data = objectMapper.writeValueAsString(entry);
            
            // 设置TTL，让Redis自动清理过期数据
            stringRedisTemplate.opsForValue().set(cacheDataKey, data, CACHE_DURATION_MINUTES, TimeUnit.MINUTES);
            
            log.debug("Saved cache data for key: {} with TTL: {} minutes", cacheKey, CACHE_DURATION_MINUTES);
            
        } catch (JsonProcessingException e) {
            log.error("Error serializing cache data for key: {}", cacheKey, e);
        }
    }
    
    private String generateCacheKey() {
        return UUID.randomUUID().toString();
    }
}
