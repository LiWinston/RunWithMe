package com.example.myapplication.landr.profile

data class UpdateProfileRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val phoneNumber: String? = null,
    val fitnessLevel: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val weeklyAvailability: String? = null
)

