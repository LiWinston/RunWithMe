package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 风信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Wind {
    /**
     * 风向
     */
    private WindDirection direction;
    
    /**
     * 风速
     */
    private WindSpeed speed;
    
    /**
     * 阵风
     */
    private WindSpeed gust;
    
    public Wind() {}
    
    public WindDirection getDirection() {
        return direction;
    }
    
    public void setDirection(WindDirection direction) {
        this.direction = direction;
    }
    
    public WindSpeed getSpeed() {
        return speed;
    }
    
    public void setSpeed(WindSpeed speed) {
        this.speed = speed;
    }
    
    public WindSpeed getGust() {
        return gust;
    }
    
    public void setGust(WindSpeed gust) {
        this.gust = gust;
    }
    
    @Override
    public String toString() {
        return "Wind{" +
                "direction=" + direction +
                ", speed=" + speed +
                ", gust=" + gust +
                '}';
    }
}
