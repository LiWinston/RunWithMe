package com.example.myapplication.landr.workout

import java.math.BigDecimal

data class WorkoutListResponse(
    val code: Int,
    val message: String,
    val data: List<Workout>?
) {
    val status: Int
        get() = code
}

data class Workout(
    val id: Long,
    val userId: Long,
    val workoutType: String,  // OUTDOOR_RUN, TREADMILL, WALK, CYCLING, SWIMMING, OTHER
    val distance: BigDecimal?,  // km
    val duration: Int?,  // 秒
    val steps: Int?,
    val calories: BigDecimal?,
    val avgSpeed: BigDecimal?,  // km/h
    val avgPace: Int?,  // 秒/km
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val startTime: String,
    val endTime: String?,
    val status: String,  // STARTED, PAUSED, COMPLETED, STOPPED
    val visibility: String?,  // PUBLIC, GROUP, PRIVATE
    val goalAchieved: Boolean?,
    val groupId: Long?,
    val notes: String?,
    val weatherCondition: String?,
    val temperature: BigDecimal?,
    val createdAt: String?,
    val updatedAt: String?
)

