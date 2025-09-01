package com.rwm.weather.service;

import com.rwm.weather.dto.CurrentConditionsResponse;
import com.rwm.weather.dto.HourlyForecastResponse;
import com.rwm.weather.dto.Location;
import com.rwm.weather.dto.UnitsSystem;

/**
 * Weather 服务接口
 */
public interface WeatherService {
    
    /**
     * 获取当前天气状况
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @return 当前天气状况
     */
    CurrentConditionsResponse getCurrentConditions(Double latitude, Double longitude);
    
    /**
     * 获取当前天气状况
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @param unitsSystem 单位制
     * @return 当前天气状况
     */
    CurrentConditionsResponse getCurrentConditions(Double latitude, Double longitude, UnitsSystem unitsSystem);
    
    /**
     * 获取当前天气状况
     * 
     * @param location 位置信息
     * @return 当前天气状况
     */
    CurrentConditionsResponse getCurrentConditions(Location location);
    
    /**
     * 获取当前天气状况
     * 
     * @param location 位置信息
     * @param unitsSystem 单位制
     * @return 当前天气状况
     */
    CurrentConditionsResponse getCurrentConditions(Location location, UnitsSystem unitsSystem);
    
    /**
     * 获取每小时天气预报
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @return 每小时天气预报
     */
    HourlyForecastResponse getHourlyForecast(Double latitude, Double longitude);
    
    /**
     * 获取每小时天气预报
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @param unitsSystem 单位制
     * @return 每小时天气预报
     */
    HourlyForecastResponse getHourlyForecast(Double latitude, Double longitude, UnitsSystem unitsSystem);
    
    /**
     * 获取每小时天气预报
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @param unitsSystem 单位制
     * @param hours 预报小时数 (1-240, 默认240)
     * @return 每小时天气预报
     */
    HourlyForecastResponse getHourlyForecast(Double latitude, Double longitude, UnitsSystem unitsSystem, Integer hours);
    
    /**
     * 获取每小时天气预报
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @param unitsSystem 单位制
     * @param hours 预报小时数 (1-240, 默认240)
     * @param pageSize 每页返回的小时数 (1-240, 默认24)
     * @param pageToken 分页令牌
     * @return 每小时天气预报
     */
    HourlyForecastResponse getHourlyForecast(Double latitude, Double longitude, UnitsSystem unitsSystem, 
                                           Integer hours, Integer pageSize, String pageToken);
    
    /**
     * 获取每小时天气预报
     * 
     * @param location 位置信息
     * @return 每小时天气预报
     */
    HourlyForecastResponse getHourlyForecast(Location location);
    
    /**
     * 获取每小时天气预报
     * 
     * @param location 位置信息
     * @param unitsSystem 单位制
     * @return 每小时天气预报
     */
    HourlyForecastResponse getHourlyForecast(Location location, UnitsSystem unitsSystem);
    
    /**
     * 获取每小时天气预报
     * 
     * @param location 位置信息
     * @param unitsSystem 单位制
     * @param hours 预报小时数 (1-240, 默认240)
     * @param pageSize 每页返回的小时数 (1-240, 默认24)
     * @param pageToken 分页令牌
     * @return 每小时天气预报
     */
    HourlyForecastResponse getHourlyForecast(Location location, UnitsSystem unitsSystem, 
                                           Integer hours, Integer pageSize, String pageToken);
}
