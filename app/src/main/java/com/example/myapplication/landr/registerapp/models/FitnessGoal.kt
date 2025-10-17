package com.example.myapplication.landr.registerapp.models

data class FitnessGoal(
    val weeklyDistanceKm: Double? = null,
    val weeklyWorkouts: Int? = null,
    val targetAvgPaceSecPerKm: Int? = null,
    val weeklyCalories: Int? = null,
    val extras: Map<String, Any?>? = null
)