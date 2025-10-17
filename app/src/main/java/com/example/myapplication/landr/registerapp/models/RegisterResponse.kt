package com.example.myapplication.landr.registerapp.models

data class RegisterResponse(
    val code: Int,  // 0: 成功, 1: 失败
    val message: String,
    val data: Any? = null
)