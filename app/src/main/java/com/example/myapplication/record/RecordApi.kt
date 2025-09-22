package com.example.myapplication.record

import retrofit2.Response
import retrofit2.http.*

interface RecordApi {
    
    // 创建运动记录
    @POST("/api/workouts")
    suspend fun createWorkout(@Body request: WorkoutCreateRequest): Response<ApiResponse<Workout>>
    
    // 获取用户运动记录列表
    @GET("/api/workouts/user/{userId}")
    suspend fun getUserWorkouts(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<List<Workout>>>
    
    // 更新运动状态
    @PUT("/api/workouts/{workoutId}/status")
    suspend fun updateWorkoutStatus(
        @Path("workoutId") workoutId: Long,
        @Query("status") status: String
    ): Response<ApiResponse<Workout>>
    
    // 获取今日目标达成状态
    @GET("/api/workouts/user/{userId}/today-goal")
    suspend fun getTodayGoalStatus(@Path("userId") userId: Long): Response<ApiResponse<Map<String, Any>>>
    
    // History API - 获取今日统计
    @GET("/api/history/today/{userId}")
    suspend fun getTodayStats(@Path("userId") userId: Long): Response<ApiResponse<Map<String, Any>>>
    
    // History API - 获取本周统计
    @GET("/api/history/week/{userId}")
    suspend fun getWeekStats(@Path("userId") userId: Long): Response<ApiResponse<Map<String, Any>>>
    
    // History API - 获取本月统计
    @GET("/api/history/month/{userId}")
    suspend fun getMonthStats(@Path("userId") userId: Long): Response<ApiResponse<Map<String, Any>>>
    
    // History API - 获取今日运动记录
    @GET("/api/history/workouts/today/{userId}")
    suspend fun getTodayWorkouts(@Path("userId") userId: Long): Response<ApiResponse<List<Workout>>>
    
    // History API - 获取本周运动记录
    @GET("/api/history/workouts/week/{userId}")
    suspend fun getWeekWorkouts(@Path("userId") userId: Long): Response<ApiResponse<List<Workout>>>
    
    // History API - 获取本月运动记录
    @GET("/api/history/workouts/month/{userId}")
    suspend fun getMonthWorkouts(@Path("userId") userId: Long): Response<ApiResponse<List<Workout>>>
    
    // History API - 获取本周图表数据
    @GET("/api/history/chart/week/{userId}")
    suspend fun getWeekChart(@Path("userId") userId: Long): Response<ApiResponse<Map<String, Any>>>
    
    // History API - 获取本月图表数据
    @GET("/api/history/chart/month/{userId}")
    suspend fun getMonthChart(@Path("userId") userId: Long): Response<ApiResponse<Map<String, Any>>>
}

// Workout数据类（与后端对应）
data class Workout(
    val id: Long,
    val userId: Long,
    val workoutType: String,
    val distance: String?, // 对应BigDecimal
    val duration: Int?,
    val steps: Int?,
    val calories: String?, // 对应BigDecimal
    val avgSpeed: String?, // 对应BigDecimal
    val avgPace: Int?,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val startTime: String,
    val endTime: String?,
    val status: String,
    val visibility: String,
    val goalAchieved: Boolean,
    val groupId: Long?,
    val notes: String?,
    val weatherCondition: String?,
    val temperature: String?, // 对应BigDecimal
    val createdAt: String,
    val updatedAt: String
)
