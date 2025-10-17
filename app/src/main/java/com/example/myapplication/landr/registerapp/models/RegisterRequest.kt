package com.example.myapplication.landr.registerapp.models

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null,
    val firstName: String,
    val lastName: String,
    val gender: String? = null,
    val age: Int? = null,
    val phoneNumber: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val fitnessGoal: FitnessGoal? = null,
    val fitnessLevel: String? = null,
    val weeklyAvailability: String? = null
)