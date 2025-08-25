package com.example.myapplication.landr.loginapp.models

data class LoginResponse(
    val status: Int,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

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