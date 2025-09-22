package com.rwm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 运动统计响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutStatsResponse {
    
    // 总体统计
    private int totalWorkouts;
    private BigDecimal totalDistance;
    private Integer totalDuration; // 秒
    private BigDecimal totalCalories;
    private Integer totalSteps;
    
    // 本周统计
    private int weeklyWorkouts;
    private BigDecimal weeklyDistance;
    private Integer weeklyDuration;
    private BigDecimal weeklyCalories;
    
    // 本月统计
    private int monthlyWorkouts;
    private BigDecimal monthlyDistance;
    private Integer monthlyDuration;
    private BigDecimal monthlyCalories;
    
    // 平均数据
    private BigDecimal avgDistance;
    private Integer avgDuration;
    private BigDecimal avgSpeed;
    private Integer avgHeartRate;
    
    // 最佳记录
    private BigDecimal bestDistance;
    private Integer bestDuration;
    private BigDecimal bestSpeed;
    private Integer bestHeartRate;
    
    // 目标达成
    private int streakDays; // 连续签到天数
    private boolean todayGoalAchieved; // 今日目标达成
    private int weeklyGoalAchieved; // 本周目标达成天数
    private int monthlyGoalAchieved; // 本月目标达成天数
    
    // 最近活动
    private LocalDate lastWorkoutDate;
    private String mostFrequentWorkoutType;
    
    // 运动类型分布
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutTypeStats {
        private String workoutType;
        private int count;
        private BigDecimal totalDistance;
        private Integer totalDuration;
    }
}
