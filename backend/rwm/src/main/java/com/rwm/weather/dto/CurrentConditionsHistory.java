package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 当前天气历史数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentConditionsHistory {
    /**
     * 过去24小时温度变化
     */
    private Temperature temperatureChange;
    
    /**
     * 过去24小时最高温度
     */
    private Temperature maxTemperature;
    
    /**
     * 过去24小时最低温度
     */
    private Temperature minTemperature;
    
    /**
     * 过去24小时降水量
     */
    private PrecipitationQuantity qpf;
    
    public CurrentConditionsHistory() {}
    
    public Temperature getTemperatureChange() {
        return temperatureChange;
    }
    
    public void setTemperatureChange(Temperature temperatureChange) {
        this.temperatureChange = temperatureChange;
    }
    
    public Temperature getMaxTemperature() {
        return maxTemperature;
    }
    
    public void setMaxTemperature(Temperature maxTemperature) {
        this.maxTemperature = maxTemperature;
    }
    
    public Temperature getMinTemperature() {
        return minTemperature;
    }
    
    public void setMinTemperature(Temperature minTemperature) {
        this.minTemperature = minTemperature;
    }
    
    public PrecipitationQuantity getQpf() {
        return qpf;
    }
    
    public void setQpf(PrecipitationQuantity qpf) {
        this.qpf = qpf;
    }
    
    @Override
    public String toString() {
        return "CurrentConditionsHistory{" +
                "temperatureChange=" + temperatureChange +
                ", maxTemperature=" + maxTemperature +
                ", minTemperature=" + minTemperature +
                ", qpf=" + qpf +
                '}';
    }
}
