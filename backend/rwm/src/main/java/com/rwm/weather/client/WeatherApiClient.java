package com.rwm.weather.client;

import com.rwm.weather.config.WeatherConfig;
import com.rwm.weather.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Google Maps Weather API 客户端
 */
@Component
public class WeatherApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherApiClient.class);
    
    private final RestTemplate restTemplate;
    private final WeatherConfig weatherConfig;
    
    public WeatherApiClient(@Qualifier("weatherRestTemplate") RestTemplate restTemplate,
                           WeatherConfig weatherConfig) {
        this.restTemplate = restTemplate;
        this.weatherConfig = weatherConfig;
    }
    
    /**
     * 获取当前天气状况
     * 
     * @param location 位置信息（纬度和经度）
     * @param unitsSystem 单位制（可选）
     * @return 当前天气状况
     */
    public CurrentConditionsResponse getCurrentConditions(Location location, UnitsSystem unitsSystem) {
        validateLocation(location);
        
        URI uri = buildCurrentConditionsUri(location, unitsSystem);
        
        if (weatherConfig.isEnableRequestLogging()) {
            logger.info("Requesting current conditions for location: {}, units: {}", location, unitsSystem);
            logger.debug("Request URI: {}", uri);
        }
        
        try {
            CurrentConditionsResponse response = restTemplate.getForObject(uri, CurrentConditionsResponse.class);
            
            if (weatherConfig.isEnableRequestLogging()) {
                logger.info("Successfully received current conditions response");
            }
            
            return response;
        } catch (Exception e) {
            logger.error("Failed to get current conditions for location: {}", location, e);
            throw new WeatherApiException("Failed to get current weather conditions", e);
        }
    }
    
    /**
     * 构建当前天气状况请求URI
     */
    private URI buildCurrentConditionsUri(Location location, UnitsSystem unitsSystem) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(weatherConfig.getBaseUrl())
                .path("/currentConditions:lookup")
                .queryParam("key", weatherConfig.getApiKey())
                .queryParam("location.latitude", location.getLatitude())
                .queryParam("location.longitude", location.getLongitude());
        
        if (unitsSystem != null) {
            builder.queryParam("unitsSystem", unitsSystem.name());
        }
        
        return builder.build().toUri();
    }
    
    /**
     * 验证位置参数
     */
    private void validateLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (location.getLatitude() == null || location.getLongitude() == null) {
            throw new IllegalArgumentException("Latitude and longitude cannot be null");
        }
        if (location.getLatitude() < -90 || location.getLatitude() > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (location.getLongitude() < -180 || location.getLongitude() > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
    }
}
