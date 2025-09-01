package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 显示日期时间
 */
@Data
public class DisplayDateTime {
    
    /**
     * 年份
     */
    @JsonProperty("year")
    private Integer year;
    
    /**
     * 月份
     */
    @JsonProperty("month")
    private Integer month;
    
    /**
     * 日期
     */
    @JsonProperty("day")
    private Integer day;
    
    /**
     * 小时
     */
    @JsonProperty("hours")
    private Integer hours;
    
    /**
     * UTC偏移量
     */
    @JsonProperty("utcOffset")
    private String utcOffset;
}
