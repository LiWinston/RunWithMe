package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 厚度测量
 */
@Data
public class Thickness {
    
    /**
     * 厚度值
     */
    @JsonProperty("thickness")
    private Double thickness;
    
    /**
     * 测量单位
     */
    @JsonProperty("unit")
    private String unit;
}
