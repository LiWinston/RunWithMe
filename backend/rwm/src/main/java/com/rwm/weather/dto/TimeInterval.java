package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

/**
 * 时间间隔
 */
@Data
public class TimeInterval {
    
    /**
     * 开始时间
     */
    @JsonProperty("startTime")
    private Instant startTime;
    
    /**
     * 结束时间
     */
    @JsonProperty("endTime")
    private Instant endTime;
}
