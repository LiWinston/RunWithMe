package com.example.myapplication.landr.loginapp.models
data class User(
    val id: Long,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val gender: String?,
    val age: Int?,
    val phoneNumber: String?,
    val height: Double?,
    val weight: Double?,
    val fitnessGoal: String?,
    val fitnessLevel: String?,
    val weeklyAvailability: String?
)