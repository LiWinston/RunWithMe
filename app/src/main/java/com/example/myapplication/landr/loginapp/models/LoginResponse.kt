package com.example.myapplication.landr.loginapp.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("code")
    val status: Int,
    val message: String,
    val data: LoginData?
)


