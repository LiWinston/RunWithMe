package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 时区信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeZone {
    /**
     * 时区ID
     */
    private String id;
    
    public TimeZone() {}
    
    public TimeZone(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "TimeZone{" +
                "id='" + id + '\'' +
                '}';
    }
}
