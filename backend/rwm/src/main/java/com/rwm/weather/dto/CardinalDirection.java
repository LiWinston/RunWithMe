package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 风向枚举
 * 支持鲁棒性处理，对于未知的枚举值会返回 UNKNOWN
 */
public enum CardinalDirection {
    NORTH,
    NORTH_NORTHEAST,
    NORTHEAST,
    EAST_NORTHEAST,
    EAST,
    EAST_SOUTHEAST,
    SOUTHEAST,
    SOUTH_SOUTHEAST,
    SOUTH,
    SOUTH_SOUTHWEST,
    SOUTHWEST,
    WEST_SOUTHWEST,
    WEST,
    WEST_NORTHWEST,
    NORTHWEST,
    NORTH_NORTHWEST,
    
    /**
     * 未知风向（用作兜底）
     */
    UNKNOWN;
    
    private static final Logger logger = LoggerFactory.getLogger(CardinalDirection.class);
    
    /**
     * JSON 反序列化时的自定义处理
     * 如果遇到未知的枚举值，返回 UNKNOWN 而不是抛出异常
     */
    @JsonCreator
    public static CardinalDirection fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        
        try {
            return CardinalDirection.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 记录未知的枚举值，但不抛出异常
            logger.warn("Unknown CardinalDirection value: '{}', using UNKNOWN as fallback", value);
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
