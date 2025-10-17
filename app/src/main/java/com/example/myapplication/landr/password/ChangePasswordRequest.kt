package com.example.myapplication.landr.password

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class ChangePasswordResponse(
    val code: Int,
    val message: String,
    val data: String?
) {
    val status: Int
        get() = code
}

