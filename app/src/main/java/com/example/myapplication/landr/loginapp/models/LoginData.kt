package com.example.myapplication.landr.loginapp.models

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)
