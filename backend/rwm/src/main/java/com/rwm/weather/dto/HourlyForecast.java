package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 每小时天气预报数据
 */
@Data
public class HourlyForecast {
    
    /**
     * 时间间隔
     */
    @JsonProperty("interval")
    private TimeInterval interval;
    
    /**
     * 显示日期时间
     */
    @JsonProperty("displayDateTime")
    private DisplayDateTime displayDateTime;
    
    /**
     * 是否为白天
     */
    @JsonProperty("isDaytime")
    private Boolean isDaytime;
    
    /**
     * 天气状况
     */
    @JsonProperty("weatherCondition")
    private WeatherCondition weatherCondition;
    
    /**
     * 温度
     */
    @JsonProperty("temperature")
    private Temperature temperature;
    
    /**
     * 体感温度
     */
    @JsonProperty("feelsLikeTemperature")
    private Temperature feelsLikeTemperature;
    
    /**
     * 露点温度
     */
    @JsonProperty("dewPoint")
    private Temperature dewPoint;
    
    /**
     * 暑热指数
     */
    @JsonProperty("heatIndex")
    private Temperature heatIndex;
    
    /**
     * 风寒温度
     */
    @JsonProperty("windChill")
    private Temperature windChill;
    
    /**
     * 湿球温度
     */
    @JsonProperty("wetBulbTemperature")
    private Temperature wetBulbTemperature;
    
    /**
     * 相对湿度百分比
     */
    @JsonProperty("relativeHumidity")
    private Integer relativeHumidity;
    
    /**
     * 紫外线指数
     */
    @JsonProperty("uvIndex")
    private Integer uvIndex;
    
    /**
     * 降水信息
     */
    @JsonProperty("precipitation")
    private Precipitation precipitation;
    
    /**
     * 雷暴概率
     */
    @JsonProperty("thunderstormProbability")
    private Integer thunderstormProbability;
    
    /**
     * 气压
     */
    @JsonProperty("airPressure")
    private AirPressure airPressure;
    
    /**
     * 风力信息
     */
    @JsonProperty("wind")
    private Wind wind;
    
    /**
     * 能见度
     */
    @JsonProperty("visibility")
    private Visibility visibility;
    
    /**
     * 云量覆盖百分比
     */
    @JsonProperty("cloudCover")
    private Integer cloudCover;
    
    /**
     * 冰层厚度
     */
    @JsonProperty("iceThickness")
    private Thickness iceThickness;
}
