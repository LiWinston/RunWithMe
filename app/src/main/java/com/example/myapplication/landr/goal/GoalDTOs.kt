package com.example.myapplication.landr.goal

import com.example.myapplication.landr.registerapp.models.FitnessGoal

data class UpdateSloganRequest(
    val slogan: String?
)

data class UpdateFitnessGoalRequest(
    val weeklyDistanceKm: Double?,
    val weeklyWorkouts: Int?,
    val targetAvgPaceSecPerKm: Int?,
    val weeklyCalories: Int?
)

data class FitnessGoalResponse(
    val code: Int,
    val message: String,
    val data: FitnessGoal?
) {
    val status: Int
        get() = code
}

