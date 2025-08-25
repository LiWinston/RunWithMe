package com.rwm.weather.service;

import com.rwm.weather.dto.CurrentConditionsResponse;
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
}
