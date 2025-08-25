package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 降水概率信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrecipitationProbability {
    /**
     * 降水概率百分比
     */
    private Integer percent;
    
    /**
     * 降水类型
     */
    private PrecipitationType type;
    
    public PrecipitationProbability() {}
    
    public PrecipitationProbability(Integer percent, PrecipitationType type) {
        this.percent = percent;
        this.type = type;
    }
    
    public Integer getPercent() {
        return percent;
    }
    
    public void setPercent(Integer percent) {
        this.percent = percent;
    }
    
    public PrecipitationType getType() {
        return type;
    }
    
    public void setType(PrecipitationType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "PrecipitationProbability{" +
                "percent=" + percent +
                ", type=" + type +
                '}';
    }
}
