package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 风速信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WindSpeed {
    /**
     * 风速值
     */
    private Double value;
    
    /**
     * 风速单位（KILOMETERS_PER_HOUR或MILES_PER_HOUR）
     */
    private String unit;
    
    public WindSpeed() {}
    
    public WindSpeed(Double value, String unit) {
        this.value = value;
        this.unit = unit;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    @Override
    public String toString() {
        return "WindSpeed{" +
                "value=" + value +
                ", unit='" + unit + '\'' +
                '}';
    }
}
