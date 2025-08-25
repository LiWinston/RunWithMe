package com.rwm.weather.service.impl;

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

/**
 * Weather 服务实现
 */
@Service
public class WeatherServiceImpl implements WeatherService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);
    
    private final WeatherApiClient weatherApiClient;
    private final WeatherConfig weatherConfig;
    
    public WeatherServiceImpl(WeatherApiClient weatherApiClient, WeatherConfig weatherConfig) {
        this.weatherApiClient = weatherApiClient;
        this.weatherConfig = weatherConfig;
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
        
        try {
            CurrentConditionsResponse response = weatherApiClient.getCurrentConditions(location, unitsSystem);
            
            logger.info("Successfully retrieved current conditions for location: {}", location);
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
        
        try {
            HourlyForecastResponse response = weatherApiClient.getHourlyForecast(location, unitsSystem, hours, pageSize, pageToken);
            
            logger.info("Successfully retrieved hourly forecast for location: {}", location);
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to get hourly forecast for location: {}", location, e);
            throw e;
        }
    }
}
