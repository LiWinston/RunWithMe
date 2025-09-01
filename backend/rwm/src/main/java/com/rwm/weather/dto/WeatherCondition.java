package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 天气状况信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherCondition {
    /**
     * 天气图标基础URI
     */
    private String iconBaseUri;
    
    /**
     * 天气描述
     */
    private WeatherDescription description;
    
    /**
     * 天气状况类型
     */
    private WeatherConditionType type;
    
    public WeatherCondition() {}
    
    public String getIconBaseUri() {
        return iconBaseUri;
    }
    
    public void setIconBaseUri(String iconBaseUri) {
        this.iconBaseUri = iconBaseUri;
    }
    
    public WeatherDescription getDescription() {
        return description;
    }
    
    public void setDescription(WeatherDescription description) {
        this.description = description;
    }
    
    public WeatherConditionType getType() {
        return type;
    }
    
    public void setType(WeatherConditionType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "WeatherCondition{" +
                "iconBaseUri='" + iconBaseUri + '\'' +
                ", description=" + description +
                ", type=" + type +
                '}';
    }
}
