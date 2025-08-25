package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 气压信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirPressure {
    /**
     * 平均海平面气压（毫巴）
     */
    private Double meanSeaLevelMillibars;
    
    public AirPressure() {}
    
    public AirPressure(Double meanSeaLevelMillibars) {
        this.meanSeaLevelMillibars = meanSeaLevelMillibars;
    }
    
    public Double getMeanSeaLevelMillibars() {
        return meanSeaLevelMillibars;
    }
    
    public void setMeanSeaLevelMillibars(Double meanSeaLevelMillibars) {
        this.meanSeaLevelMillibars = meanSeaLevelMillibars;
    }
    
    @Override
    public String toString() {
        return "AirPressure{" +
                "meanSeaLevelMillibars=" + meanSeaLevelMillibars +
                '}';
    }
}
