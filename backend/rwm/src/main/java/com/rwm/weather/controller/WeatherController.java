package com.rwm.weather.controller;

import com.rwm.dto.response.Result;
import com.rwm.weather.dto.CurrentConditionsResponse;
import com.rwm.weather.dto.Location;
import com.rwm.weather.dto.UnitsSystem;
import com.rwm.weather.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * 天气API控制器
 */
@RestController
@RequestMapping("/api/weather")
@Validated
public class WeatherController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);
    
    private final WeatherService weatherService;
    
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    
    /**
     * 获取当前天气状况
     * 
     * @param latitude 纬度 (-90 到 90)
     * @param longitude 经度 (-180 到 180)
     * @param unitsSystem 单位制 (METRIC 或 IMPERIAL，可选，默认为METRIC)
     * @return 当前天气状况
     */
    @GetMapping("/current")
    public Result<CurrentConditionsResponse> getCurrentConditions(
            @RequestParam 
            @NotNull(message = "纬度不能为空")
            @DecimalMin(value = "-90.0", message = "纬度必须在-90到90之间")
            @DecimalMax(value = "90.0", message = "纬度必须在-90到90之间")
            Double latitude,
            
            @RequestParam 
            @NotNull(message = "经度不能为空")
            @DecimalMin(value = "-180.0", message = "经度必须在-180到180之间")
            @DecimalMax(value = "180.0", message = "经度必须在-180到180之间")
            Double longitude,
            
            @RequestParam(required = false, defaultValue = "METRIC") 
            UnitsSystem unitsSystem) {
        
        try {
            logger.info("Getting current weather conditions for latitude: {}, longitude: {}, units: {}", 
                       latitude, longitude, unitsSystem);
            
            CurrentConditionsResponse response = weatherService.getCurrentConditions(latitude, longitude, unitsSystem);
            
            return Result.ok("获取当前天气状况成功", response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return Result.error("请求参数无效: " + e.getMessage());
            
        } catch (Exception e) {
            logger.error("Failed to get current weather conditions", e);
            return Result.error("获取天气信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过位置对象获取当前天气状况
     * 
     * @param location 位置信息
     * @param unitsSystem 单位制 (METRIC 或 IMPERIAL，可选，默认为METRIC)
     * @return 当前天气状况
     */
    @PostMapping("/current")
    public Result<CurrentConditionsResponse> getCurrentConditionsByLocation(
            @RequestBody @Validated Location location,
            @RequestParam(required = false, defaultValue = "METRIC") UnitsSystem unitsSystem) {
        
        try {
            logger.info("Getting current weather conditions for location: {}, units: {}", location, unitsSystem);
            
            CurrentConditionsResponse response = weatherService.getCurrentConditions(location, unitsSystem);
            
            return Result.ok("获取当前天气状况成功", response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid location: {}", e.getMessage());
            return Result.error("位置信息无效: " + e.getMessage());
            
        } catch (Exception e) {
            logger.error("Failed to get current weather conditions for location: {}", location, e);
            return Result.error("获取天气信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("Weather API service is running");
    }
}
