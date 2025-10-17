package com.example.myapplication.landr.workout

import java.math.BigDecimal

data class WorkoutStatsResponse(
    val code: Int,
    val message: String,
    val data: WorkoutStats?
) {
    val status: Int
        get() = code
}

data class WorkoutStats(
    // 总体统计
    val totalWorkouts: Int,
    val totalDistance: BigDecimal?,
    val totalDuration: Int?,  // 秒
    val totalCalories: BigDecimal?,
    val totalSteps: Int?,
    
    // 本周统计
    val weeklyWorkouts: Int?,
    val weeklyDistance: BigDecimal?,
    val weeklyDuration: Int?,
    val weeklyCalories: BigDecimal?,
    
    // 本月统计
    val monthlyWorkouts: Int?,
    val monthlyDistance: BigDecimal?,
    val monthlyDuration: Int?,
    val monthlyCalories: BigDecimal?,
    
    // 平均数据
    val avgDistance: BigDecimal?,
    val avgDuration: Int?,
    val avgSpeed: BigDecimal?,
    val avgHeartRate: Int?,
    
    // 最佳记录
    val bestDistance: BigDecimal?,
    val bestDuration: Int?,
    val bestSpeed: BigDecimal?,
    val bestHeartRate: Int?,
    
    // 目标达成
    val streakDays: Int?,
    val todayGoalAchieved: Boolean?,
    val weeklyGoalAchieved: Int?,
    val monthlyGoalAchieved: Int?,
    
    // 最近活动
    val lastWorkoutDate: String?,
    val mostFrequentWorkoutType: String?
)

