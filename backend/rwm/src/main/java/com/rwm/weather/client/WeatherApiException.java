package com.rwm.weather.client;

/**
 * Weather API 异常类
 */
public class WeatherApiException extends RuntimeException {
    
    public WeatherApiException(String message) {
        super(message);
    }
    
    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
