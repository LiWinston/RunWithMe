package com.rwm.weather.dto;

/**
 * Weather condition types as defined by Google Maps Weather API
 * 官方文档：https://developers.google.com/maps/documentation/weather/weather-condition-icons
 */
public enum WeatherConditionType {
    // 晴朗天气
    CLEAR,                          // 无云
    MOSTLY_CLEAR,                   // 周期性云
    PARTLY_CLOUDY,                  // 局部多云（有少量云）
    MOSTLY_CLOUDY,                  // 大部多云（多云多于晴）
    CLOUDY,                        // 阴天（全是云，没有阳光）
    
    // 风力条件
    WINDY,                         // 强风
    WIND_AND_RAIN,                 // 大风伴降水
    
    // 雨天条件 - 阵雨
    LIGHT_RAIN_SHOWERS,            // 间歇性小雨
    CHANCE_OF_SHOWERS,             // 或有间歇性降雨
    SCATTERED_SHOWERS,             // 间歇性降雨
    RAIN_SHOWERS,                  // 阵雨是指降雨持续时间短于雨，其特点是开始和停止时间突然，强度变化快
    HEAVY_RAIN_SHOWERS,            // 强阵雨
    
    // 雨天条件 - 持续降雨
    LIGHT_TO_MODERATE_RAIN,        // 降雨（小到中雨）
    MODERATE_TO_HEAVY_RAIN,        // 雨（中到大雨）
    RAIN,                          // 中雨
    LIGHT_RAIN,                    // 小雨
    HEAVY_RAIN,                    // 大雨
    RAIN_PERIODICALLY_HEAVY,       // 雨，偶有大雨
    
    // 雪天条件 - 阵雪
    LIGHT_SNOW_SHOWERS,            // 降雪强度不一，但持续时间较短的小雪
    CHANCE_OF_SNOW_SHOWERS,        // 或有阵雪
    SCATTERED_SNOW_SHOWERS,        // 短时间内降雪强度不一
    SNOW_SHOWERS,                  // 阵雪
    HEAVY_SNOW_SHOWERS,            // 强阵雪
    
    // 雪天条件 - 持续降雪
    LIGHT_TO_MODERATE_SNOW,        // 小到中雪
    MODERATE_TO_HEAVY_SNOW,        // 中到大雪
    SNOW,                          // 中雪
    LIGHT_SNOW,                    // 小雪
    HEAVY_SNOW,                    // 大雪
    SNOWSTORM,                     // 降雪，可能有雷电
    SNOW_PERIODICALLY_HEAVY,       // 雪，有时大雪
    HEAVY_SNOW_STORM,              // 大雪，可能伴有雷电
    BLOWING_SNOW,                  // 降雪，伴有强风
    
    // 混合降水
    RAIN_AND_SNOW,                 // 雨夹雪
    
    // 冰雹
    HAIL,                          // 冰雹
    HAIL_SHOWERS,                  // 短时间内以不同强度降落的冰雹
    
    // 雷暴
    THUNDERSTORM,                  // 雷暴
    THUNDERSHOWER,                 // 雷电交加的阵雨
    LIGHT_THUNDERSTORM_RAIN,       // 轻度雷暴夹雨
    SCATTERED_THUNDERSTORMS,       // 短时间内降雨强度不一的雷暴
    HEAVY_THUNDERSTORM             // 强雷暴
}
