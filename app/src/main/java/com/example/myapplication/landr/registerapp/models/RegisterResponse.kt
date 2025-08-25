package com.example.myapplication.landr.registerapp.models

data class RegisterResponse(
    val status: Int,
    val message: String,
    val data: Any? = null
)