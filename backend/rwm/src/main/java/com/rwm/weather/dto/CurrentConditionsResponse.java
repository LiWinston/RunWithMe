package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 当前天气状况响应
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentConditionsResponse {
    /**
     * 当前时间
     */
    private String currentTime;
    
    /**
     * 时区信息
     */
    private TimeZone timeZone;
    
    /**
     * 是否为白天
     */
    private Boolean isDaytime;
    
    /**
     * 天气状况
     */
    private WeatherCondition weatherCondition;
    
    /**
     * 当前温度
     */
    private Temperature temperature;
    
    /**
     * 体感温度
     */
    private Temperature feelsLikeTemperature;
    
    /**
     * 露点温度
     */
    private Temperature dewPoint;
    
    /**
     * 暑热指数
     */
    private Temperature heatIndex;
    
    /**
     * 风寒温度
     */
    private Temperature windChill;
    
    /**
     * 湿球温度
     */
    private Temperature wetBulbTemperature;
    
    /**
     * 相对湿度（百分比）
     */
    private Integer relativeHumidity;
    
    /**
     * 紫外线指数
     */
    private Integer uvIndex;
    
    /**
     * 降水信息
     */
    private Precipitation precipitation;
    
    /**
     * 雷暴概率（百分比）
     */
    private Integer thunderstormProbability;
    
    /**
     * 气压信息
     */
    private AirPressure airPressure;
    
    /**
     * 风信息
     */
    private Wind wind;
    
    /**
     * 能见度
     */
    private Visibility visibility;
    
    /**
     * 云量覆盖率（百分比）
     */
    private Integer cloudCover;
    
    /**
     * 冰层厚度
     */
    private IceThickness iceThickness;
    
    /**
     * 过去24小时的历史数据
     */
    private CurrentConditionsHistory currentConditionsHistory;
    
    public CurrentConditionsResponse() {}
    
    // Getters and Setters
    public String getCurrentTime() {
        return currentTime;
    }
    
    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    public Boolean getIsDaytime() {
        return isDaytime;
    }
    
    public void setIsDaytime(Boolean isDaytime) {
        this.isDaytime = isDaytime;
    }
    
    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }
    
    public void setWeatherCondition(WeatherCondition weatherCondition) {
        this.weatherCondition = weatherCondition;
    }
    
    public Temperature getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }
    
    public Temperature getFeelsLikeTemperature() {
        return feelsLikeTemperature;
    }
    
    public void setFeelsLikeTemperature(Temperature feelsLikeTemperature) {
        this.feelsLikeTemperature = feelsLikeTemperature;
    }
    
    public Temperature getDewPoint() {
        return dewPoint;
    }
    
    public void setDewPoint(Temperature dewPoint) {
        this.dewPoint = dewPoint;
    }
    
    public Temperature getHeatIndex() {
        return heatIndex;
    }
    
    public void setHeatIndex(Temperature heatIndex) {
        this.heatIndex = heatIndex;
    }
    
    public Temperature getWindChill() {
        return windChill;
    }
    
    public void setWindChill(Temperature windChill) {
        this.windChill = windChill;
    }
    
    public Temperature getWetBulbTemperature() {
        return wetBulbTemperature;
    }
    
    public void setWetBulbTemperature(Temperature wetBulbTemperature) {
        this.wetBulbTemperature = wetBulbTemperature;
    }
    
    public Integer getRelativeHumidity() {
        return relativeHumidity;
    }
    
    public void setRelativeHumidity(Integer relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }
    
    public Integer getUvIndex() {
        return uvIndex;
    }
    
    public void setUvIndex(Integer uvIndex) {
        this.uvIndex = uvIndex;
    }
    
    public Precipitation getPrecipitation() {
        return precipitation;
    }
    
    public void setPrecipitation(Precipitation precipitation) {
        this.precipitation = precipitation;
    }
    
    public Integer getThunderstormProbability() {
        return thunderstormProbability;
    }
    
    public void setThunderstormProbability(Integer thunderstormProbability) {
        this.thunderstormProbability = thunderstormProbability;
    }
    
    public AirPressure getAirPressure() {
        return airPressure;
    }
    
    public void setAirPressure(AirPressure airPressure) {
        this.airPressure = airPressure;
    }
    
    public Wind getWind() {
        return wind;
    }
    
    public void setWind(Wind wind) {
        this.wind = wind;
    }
    
    public Visibility getVisibility() {
        return visibility;
    }
    
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
    
    public Integer getCloudCover() {
        return cloudCover;
    }
    
    public void setCloudCover(Integer cloudCover) {
        this.cloudCover = cloudCover;
    }
    
    public IceThickness getIceThickness() {
        return iceThickness;
    }
    
    public void setIceThickness(IceThickness iceThickness) {
        this.iceThickness = iceThickness;
    }
    
    public CurrentConditionsHistory getCurrentConditionsHistory() {
        return currentConditionsHistory;
    }
    
    public void setCurrentConditionsHistory(CurrentConditionsHistory currentConditionsHistory) {
        this.currentConditionsHistory = currentConditionsHistory;
    }
    
    @Override
    public String toString() {
        return "CurrentConditionsResponse{" +
                "currentTime='" + currentTime + '\'' +
                ", timeZone=" + timeZone +
                ", isDaytime=" + isDaytime +
                ", weatherCondition=" + weatherCondition +
                ", temperature=" + temperature +
                ", relativeHumidity=" + relativeHumidity +
                ", uvIndex=" + uvIndex +
                '}';
    }
}
