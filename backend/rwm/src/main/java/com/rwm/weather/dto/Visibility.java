package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 能见度信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Visibility {
    /**
     * 能见度距离
     */
    private Double distance;
    
    /**
     * 距离单位（KILOMETERS或MILES）
     */
    private String unit;
    
    public Visibility() {}
    
    public Visibility(Double distance, String unit) {
        this.distance = distance;
        this.unit = unit;
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    @Override
    public String toString() {
        return "Visibility{" +
                "distance=" + distance +
                ", unit='" + unit + '\'' +
                '}';
    }
}
