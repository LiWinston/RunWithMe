package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 每小时天气预报响应
 */
@Data
public class HourlyForecastResponse {
    
    /**
     * 每小时预报数据列表
     */
    @JsonProperty("forecastHours")
    private List<HourlyForecast> forecastHours;
    
    /**
     * 时区信息
     */
    @JsonProperty("timeZone")
    private TimeZone timeZone;
    
    /**
     * 下一页令牌（用于分页）
     */
    @JsonProperty("nextPageToken")
    private String nextPageToken;
}
