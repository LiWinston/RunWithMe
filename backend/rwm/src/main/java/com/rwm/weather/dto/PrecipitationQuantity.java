package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 降水量信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrecipitationQuantity {
    /**
     * 降水量数值
     */
    private Double quantity;
    
    /**
     * 降水量单位（MILLIMETERS或INCHES）
     */
    private String unit;
    
    public PrecipitationQuantity() {}
    
    public PrecipitationQuantity(Double quantity, String unit) {
        this.quantity = quantity;
        this.unit = unit;
    }
    
    public Double getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    @Override
    public String toString() {
        return "PrecipitationQuantity{" +
                "quantity=" + quantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}
