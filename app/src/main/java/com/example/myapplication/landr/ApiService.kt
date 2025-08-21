package com.example.myapplication.landr
import com.example.myapplication.landr.registerapp.models.*
import com.example.myapplication.landr.loginapp.models.LoginRequest
import com.example.myapplication.landr.loginapp.models.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

}