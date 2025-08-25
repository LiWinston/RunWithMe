package com.rwm.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Weather API HTTP客户端配置
 */
@Configuration
public class WeatherHttpConfig {
    
    private final WeatherConfig weatherConfig;
    
    public WeatherHttpConfig(WeatherConfig weatherConfig) {
        this.weatherConfig = weatherConfig;
    }
    
    /**
     * 配置Weather API专用的RestTemplate
     */
    @Bean("weatherRestTemplate")
    public RestTemplate weatherRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(weatherConfig.getConnectTimeout());
        factory.setReadTimeout(weatherConfig.getReadTimeout());
        
        return new RestTemplate(factory);
    }
}
