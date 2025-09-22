package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 天气单位制枚举
 * 支持鲁棒性处理，对于未知的枚举值会返回 METRIC
 */
public enum UnitsSystem {
    /**
     * 公制单位（摄氏度、公里/小时、毫米等）
     */
    METRIC,
    
    /**
     * 英制单位（华氏度、英里/小时、英寸等）
     */
    IMPERIAL;
    
    private static final Logger logger = LoggerFactory.getLogger(UnitsSystem.class);
    
    /**
     * JSON 反序列化时的自定义处理
     * 如果遇到未知的枚举值，返回 METRIC 作为默认值
     */
    @JsonCreator
    public static UnitsSystem fromString(String value) {
        if (value == null) {
            return METRIC;
        }
        
        try {
            return UnitsSystem.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 记录未知的枚举值，但不抛出异常
            logger.warn("Unknown UnitsSystem value: '{}', using METRIC as fallback", value);
            return METRIC;
        }
    }
    
    /**
     * JSON 序列化时使用原始枚举名称
     */
    @JsonValue
    public String getValue() {
        return this.name();
    }
}
