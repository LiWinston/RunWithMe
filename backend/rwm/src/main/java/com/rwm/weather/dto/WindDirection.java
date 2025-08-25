package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 风向信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WindDirection {
    /**
     * 风向角度（0-360度）
     */
    private Integer degrees;
    
    /**
     * 风向基本方向
     */
    private CardinalDirection cardinal;
    
    public WindDirection() {}
    
    public WindDirection(Integer degrees, CardinalDirection cardinal) {
        this.degrees = degrees;
        this.cardinal = cardinal;
    }
    
    public Integer getDegrees() {
        return degrees;
    }
    
    public void setDegrees(Integer degrees) {
        this.degrees = degrees;
    }
    
    public CardinalDirection getCardinal() {
        return cardinal;
    }
    
    public void setCardinal(CardinalDirection cardinal) {
        this.cardinal = cardinal;
    }
    
    @Override
    public String toString() {
        return "WindDirection{" +
                "degrees=" + degrees +
                ", cardinal=" + cardinal +
                '}';
    }
}
