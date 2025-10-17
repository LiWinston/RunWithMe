package com.example.myapplication.landr.loginapp.models

/**
 * FitnessGoal data class matching backend JSON structure
 * Stored as JSON in User.fitnessGoal field
 */
data class FitnessGoal(
    val weeklyDistanceKm: Double?,
    val weeklyWorkouts: Int?,
    val targetAvgPaceSecPerKm: Int?,
    val weeklyCalories: Int?,
    val slogan: String?,
    val extras: Any?
)

