package com.example.myapplication.landr.loginapp.models

data class LoginResponse(
    val status: Int,
    val message: String,
    val data: LoginData?
)


