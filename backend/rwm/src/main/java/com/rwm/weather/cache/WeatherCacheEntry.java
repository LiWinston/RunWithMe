package com.rwm.weather.cache;

import com.rwm.weather.dto.CurrentConditionsResponse;
import com.rwm.weather.dto.HourlyForecastResponse;
import com.rwm.weather.dto.Location;

import java.time.LocalDateTime;

/**
 * 天气缓存条目
 * 包含天气数据和地理位置信息
 */
public class WeatherCacheEntry {
    
    private String cacheKey;
    private Location location;
    private CurrentConditionsResponse currentConditions;
    private HourlyForecastResponse hourlyForecast;
    private LocalDateTime cachedAt;
    private LocalDateTime expiresAt;
    
    public WeatherCacheEntry() {}
    
    public WeatherCacheEntry(String cacheKey, Location location, LocalDateTime cachedAt, LocalDateTime expiresAt) {
        this.cacheKey = cacheKey;
        this.location = location;
        this.cachedAt = cachedAt;
        this.expiresAt = expiresAt;
    }
    
    // 检查缓存是否过期
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    // Getters and Setters
    public String getCacheKey() {
        return cacheKey;
    }
    
    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public CurrentConditionsResponse getCurrentConditions() {
        return currentConditions;
    }
    
    public void setCurrentConditions(CurrentConditionsResponse currentConditions) {
        this.currentConditions = currentConditions;
    }
    
    public HourlyForecastResponse getHourlyForecast() {
        return hourlyForecast;
    }
    
    public void setHourlyForecast(HourlyForecastResponse hourlyForecast) {
        this.hourlyForecast = hourlyForecast;
    }
    
    public LocalDateTime getCachedAt() {
        return cachedAt;
    }
    
    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
