package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 温度信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Temperature {
    /**
     * 温度值
     */
    private Double degrees;
    
    /**
     * 温度单位（CELSIUS或FAHRENHEIT）
     */
    private String unit;
    
    public Temperature() {}
    
    public Temperature(Double degrees, String unit) {
        this.degrees = degrees;
        this.unit = unit;
    }
    
    public Double getDegrees() {
        return degrees;
    }
    
    public void setDegrees(Double degrees) {
        this.degrees = degrees;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    @Override
    public String toString() {
        return "Temperature{" +
                "degrees=" + degrees +
                ", unit='" + unit + '\'' +
                '}';
    }
}
