package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 降水类型枚举
 * 支持鲁棒性处理，对于未知的枚举值会返回 UNKNOWN
 */
public enum PrecipitationType {
    NONE,
    RAIN,
    LIGHT_RAIN,
    HEAVY_RAIN,
    SNOW,
    LIGHT_SNOW,
    HEAVY_SNOW,
    ICE,
    RAIN_AND_SNOW,
    DRIZZLE,
    SLEET,
    HAIL,
    /**
     * 未知降水类型（用作兜底）
     */
    UNKNOWN;
    
    private static final Logger logger = LoggerFactory.getLogger(PrecipitationType.class);
    
    /**
     * JSON 反序列化时的自定义处理
     * 如果遇到未知的枚举值，返回 UNKNOWN 而不是抛出异常
     */
    @JsonCreator
    public static PrecipitationType fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        
        try {
            return PrecipitationType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 记录未知的枚举值，但不抛出异常
            logger.warn("Unknown PrecipitationType value: '{}', using UNKNOWN as fallback", value);
            return UNKNOWN;
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
