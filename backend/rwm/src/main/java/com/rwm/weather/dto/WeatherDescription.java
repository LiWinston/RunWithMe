package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 天气描述信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDescription {
    /**
     * 天气描述文本
     */
    private String text;
    
    /**
     * 语言代码
     */
    private String languageCode;
    
    public WeatherDescription() {}
    
    public WeatherDescription(String text, String languageCode) {
        this.text = text;
        this.languageCode = languageCode;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getLanguageCode() {
        return languageCode;
    }
    
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    
    @Override
    public String toString() {
        return "WeatherDescription{" +
                "text='" + text + '\'' +
                ", languageCode='" + languageCode + '\'' +
                '}';
    }
}
