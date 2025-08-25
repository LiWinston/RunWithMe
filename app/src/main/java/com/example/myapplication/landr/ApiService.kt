package com.example.myapplication.landr
import com.example.myapplication.landr.registerapp.models.*
import com.example.myapplication.landr.loginapp.models.LoginRequest
import com.example.myapplication.landr.loginapp.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") accessToken: String): Response<Any>
}