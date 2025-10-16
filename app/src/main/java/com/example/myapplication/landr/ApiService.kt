package com.example.myapplication.landr
import com.example.myapplication.landr.registerapp.models.*
import com.example.myapplication.landr.loginapp.models.LoginRequest
import com.example.myapplication.landr.loginapp.models.LoginResponse
import com.example.myapplication.landr.profile.UpdateProfileRequest
import com.example.myapplication.landr.profile.UserProfileResponse
import com.example.myapplication.landr.workout.WorkoutStatsResponse
import com.example.myapplication.landr.workout.WorkoutListResponse
import com.example.myapplication.landr.password.ChangePasswordRequest
import com.example.myapplication.landr.password.ChangePasswordResponse
import com.example.myapplication.landr.goal.FitnessGoalResponse
import com.example.myapplication.landr.goal.UpdateFitnessGoalRequest
import com.example.myapplication.landr.goal.UpdateSloganRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") accessToken: String): Response<Any>
    
    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>
    
    @PUT("api/user/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): Response<UserProfileResponse>
    
    @PUT("api/user/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>
    
    // Goal APIs
    @GET("api/user/fitness-goal")
    suspend fun getFitnessGoal(): Response<FitnessGoalResponse>
    
    @PUT("api/user/slogan")
    suspend fun updateSlogan(@Body request: UpdateSloganRequest): Response<FitnessGoalResponse>
    
    @PUT("api/user/fitness-goal")
    suspend fun updateFitnessGoal(@Body request: UpdateFitnessGoalRequest): Response<FitnessGoalResponse>
    
    // Workout APIs
    @GET("api/workouts/user/{userId}/stats")
    suspend fun getUserWorkoutStats(@Path("userId") userId: Long): Response<WorkoutStatsResponse>
    
    @GET("api/workouts/user/{userId}")
    suspend fun getUserWorkouts(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<WorkoutListResponse>
}