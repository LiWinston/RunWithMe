package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 冰层厚度信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IceThickness {
    /**
     * 冰层厚度
     */
    private Double thickness;
    
    /**
     * 厚度单位（MILLIMETERS或INCHES）
     */
    private String unit;
    
    public IceThickness() {}
    
    public IceThickness(Double thickness, String unit) {
        this.thickness = thickness;
        this.unit = unit;
    }
    
    public Double getThickness() {
        return thickness;
    }
    
    public void setThickness(Double thickness) {
        this.thickness = thickness;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    @Override
    public String toString() {
        return "IceThickness{" +
                "thickness=" + thickness +
                ", unit='" + unit + '\'' +
                '}';
    }
}
