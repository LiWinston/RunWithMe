package com.rwm.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Google Maps Weather API 配置
 */
@Configuration
@ConfigurationProperties(prefix = "google.maps.weather")
public class WeatherConfig {
    
    /**
     * Google Maps API密钥
     */
    private String apiKey;
    
    /**
     * API基础URL
     */
    private String baseUrl = "https://weather.googleapis.com/v1";
    
    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 5000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 10000;
    
    /**
     * 每秒查询速率限制
     */
    private int queryRateLimit = 50;
    
    /**
     * 默认单位制（METRIC：公制，IMPERIAL：英制）
     */
    private String defaultUnitsSystem = "METRIC";
    
    /**
     * 是否启用请求日志
     */
    private boolean enableRequestLogging = false;
    
    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public int getQueryRateLimit() {
        return queryRateLimit;
    }
    
    public void setQueryRateLimit(int queryRateLimit) {
        this.queryRateLimit = queryRateLimit;
    }
    
    public String getDefaultUnitsSystem() {
        return defaultUnitsSystem;
    }
    
    public void setDefaultUnitsSystem(String defaultUnitsSystem) {
        this.defaultUnitsSystem = defaultUnitsSystem;
    }
    
    public boolean isEnableRequestLogging() {
        return enableRequestLogging;
    }
    
    public void setEnableRequestLogging(boolean enableRequestLogging) {
        this.enableRequestLogging = enableRequestLogging;
    }
}
