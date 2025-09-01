package com.example.myapplication.landr.loginapp.models

import com.google.gson.annotations.SerializedName

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long?,
    val tokenType: String?,
    val userInfo: UserInfo
)

data class UserInfo(
    val id: Long,
    val username: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?
)
