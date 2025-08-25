package com.rwm.weather.service.impl;

import com.rwm.weather.cache.WeatherCacheEntry;
import com.rwm.weather.cache.WeatherGeoCacheService;
import com.rwm.weather.client.WeatherApiClient;
import com.rwm.weather.config.WeatherConfig;
import com.rwm.weather.dto.CurrentConditionsResponse;
import com.rwm.weather.dto.HourlyForecastResponse;
import com.rwm.weather.dto.Location;
import com.rwm.weather.dto.UnitsSystem;
import com.rwm.weather.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Weather 服务实现
 * 集成了基于地理位置的缓存功能
 */
@Service
public class WeatherServiceImpl implements WeatherService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);
    
    private final WeatherApiClient weatherApiClient;
    private final WeatherConfig weatherConfig;
    private final WeatherGeoCacheService cacheService;
    
    public WeatherServiceImpl(WeatherApiClient weatherApiClient, 
                             WeatherConfig weatherConfig,
                             WeatherGeoCacheService cacheService) {
        this.weatherApiClient = weatherApiClient;
        this.weatherConfig = weatherConfig;
        this.cacheService = cacheService;
    }
    
    @Override
    public CurrentConditionsResponse getCurrentConditions(Double latitude, Double longitude) {
        Location location = new Location(latitude, longitude);
        UnitsSystem unitsSystem = UnitsSystem.valueOf(weatherConfig.getDefaultUnitsSystem());
        return getCurrentConditions(location, unitsSystem);
    }
    
    @Override
    public CurrentConditionsResponse getCurrentConditions(Double latitude, Double longitude, UnitsSystem unitsSystem) {
        Location location = new Location(latitude, longitude);
        return getCurrentConditions(location, unitsSystem);
    }
    
    @Override
    public CurrentConditionsResponse getCurrentConditions(Location location) {
        UnitsSystem unitsSystem = UnitsSystem.valueOf(weatherConfig.getDefaultUnitsSystem());
        return getCurrentConditions(location, unitsSystem);
    }
    
    @Override
    public CurrentConditionsResponse getCurrentConditions(Location location, UnitsSystem unitsSystem) {
        logger.info("Getting current conditions for location: {}, units: {}", location, unitsSystem);
        
        // 首先检查缓存
        Optional<WeatherCacheEntry> cachedEntry = cacheService.findNearbyCache(location);
        if (cachedEntry.isPresent() && cachedEntry.get().getCurrentConditions() != null) {
            logger.info("Found cached current conditions for nearby location (within 10km)");
            return cachedEntry.get().getCurrentConditions();
        }
        
        try {
            // 缓存未命中，调用API获取数据
            logger.debug("Cache miss, fetching current conditions from API");
            CurrentConditionsResponse response = weatherApiClient.getCurrentConditions(location, unitsSystem);
            
            // 缓存结果
            cacheService.cacheCurrentConditions(location, response);
            
            logger.info("Successfully retrieved and cached current conditions for location: {}", location);
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to get current conditions for location: {}", location, e);
            throw e;
        }
    }
    
    @Override
    public HourlyForecastResponse getHourlyForecast(Double latitude, Double longitude) {
        return getHourlyForecast(latitude, longitude, UnitsSystem.valueOf(weatherConfig.getDefaultUnitsSystem()));
    }
    
    @Override
    public HourlyForecastResponse getHourlyForecast(Double latitude, Double longitude, UnitsSystem unitsSystem) {
        return getHourlyForecast(latitude, longitude, unitsSystem, null);
    }
    
    @Override
    public HourlyForecastResponse getHourlyForecast(Double latitude, Double longitude, UnitsSystem unitsSystem, Integer hours) {
        return getHourlyForecast(latitude, longitude, unitsSystem, hours, null, null);
    }
    
    @Override
    public HourlyForecastResponse getHourlyForecast(Double latitude, Double longitude, UnitsSystem unitsSystem, 
                                                   Integer hours, Integer pageSize, String pageToken) {
        Location location = new Location(latitude, longitude);
        return getHourlyForecast(location, unitsSystem, hours, pageSize, pageToken);
    }
    
    @Override
    public HourlyForecastResponse getHourlyForecast(Location location) {
        return getHourlyForecast(location, UnitsSystem.valueOf(weatherConfig.getDefaultUnitsSystem()));
    }
    
    @Override
    public HourlyForecastResponse getHourlyForecast(Location location, UnitsSystem unitsSystem) {
        return getHourlyForecast(location, unitsSystem, null, null, null);
    }
    
    @Override
    public HourlyForecastResponse getHourlyForecast(Location location, UnitsSystem unitsSystem, 
                                                   Integer hours, Integer pageSize, String pageToken) {
        logger.info("Getting hourly forecast for location: {}, units: {}, hours: {}, pageSize: {}", 
                   location, unitsSystem, hours, pageSize);
        
        // 首先检查缓存
        Optional<WeatherCacheEntry> cachedEntry = cacheService.findNearbyCache(location);
        if (cachedEntry.isPresent() && cachedEntry.get().getHourlyForecast() != null) {
            logger.info("Found cached hourly forecast for nearby location (within 10km)");
            return cachedEntry.get().getHourlyForecast();
        }
        
        try {
            // 缓存未命中，调用API获取数据
            logger.debug("Cache miss, fetching hourly forecast from API");
            HourlyForecastResponse response = weatherApiClient.getHourlyForecast(location, unitsSystem, hours, pageSize, pageToken);
            
            // 缓存结果
            cacheService.cacheHourlyForecast(location, response);
            
            logger.info("Successfully retrieved and cached hourly forecast for location: {}", location);
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to get hourly forecast for location: {}", location, e);
            throw e;
        }
    }
}
