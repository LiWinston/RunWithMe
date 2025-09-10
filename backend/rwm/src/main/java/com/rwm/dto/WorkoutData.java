package com.rwm.dto;

import lombok.Data;
import java.util.List;

/**
 * 运动动态数据DTO - 对应JSON结构
 * 存储运动过程中的所有时序数据
 */
@Data
public class WorkoutData {
    
    /**
     * GPS路线轨迹点
     */
    private List<RoutePoint> route;
    
    /**
     * 速度取样序列
     */
    private List<SpeedSample> speedSamples;
    
    /**
     * 心率取样序列
     */
    private List<HeartRateSample> heartRateSamples;
    
    /**
     * 海拔取样序列
     */
    private List<ElevationSample> elevationSamples;
    
    /**
     * 配速取样序列
     */
    private List<PaceSample> paceSamples;
    
    /**
     * 步频取样序列
     */
    private List<CadenceSample> cadenceSamples;
    
    /**
     * GPS精度记录
     */
    private List<AccuracySample> locationAccuracy;
    
    /**
     * GPS路线点
     */
    @Data
    public static class RoutePoint {
        private Double lat;        // 纬度
        private Double lng;        // 经度
        private Double altitude;   // 海拔(米)
        private String timestamp;  // ISO时间戳
        private Integer sequence;  // 序列号
    }
    
    /**
     * 速度取样点
     */
    @Data
    public static class SpeedSample {
        private Double speed;      // 速度(km/h)
        private String timestamp;  // ISO时间戳
    }
    
    /**
     * 心率取样点
     */
    @Data
    public static class HeartRateSample {
        private Integer heartRate; // 心率(bpm)
        private String timestamp;  // ISO时间戳
    }
    
    /**
     * 海拔取样点
     */
    @Data
    public static class ElevationSample {
        private Double elevation;  // 海拔(米)
        private String timestamp;  // ISO时间戳
    }
    
    /**
     * 配速取样点
     */
    @Data
    public static class PaceSample {
        private Integer pace;      // 配速(秒/公里)
        private String timestamp;  // ISO时间戳
    }
    
    /**
     * 步频取样点
     */
    @Data
    public static class CadenceSample {
        private Integer cadence;   // 步频(步/分钟)
        private String timestamp;  // ISO时间戳
    }
    
    /**
     * GPS精度取样点
     */
    @Data
    public static class AccuracySample {
        private Double accuracy;   // GPS精度(米)
        private String timestamp;  // ISO时间戳
    }
}
