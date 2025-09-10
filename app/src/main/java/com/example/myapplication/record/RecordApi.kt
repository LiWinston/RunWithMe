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
    
    // 保存运动路线轨迹
    @POST("/api/workouts/{workoutId}/route")
    suspend fun saveWorkoutRoute(
        @Path("workoutId") workoutId: Long,
        @Body routes: List<WorkoutRoute>
    ): Response<ApiResponse<String>>
    
    // 获取运动路线轨迹
    @GET("/api/workouts/{workoutId}/route")
    suspend fun getWorkoutRoute(@Path("workoutId") workoutId: Long): Response<ApiResponse<List<WorkoutRoute>>>
}

// 运动路线轨迹数据类
data class WorkoutRoute(
    val id: Long? = null,
    val workoutId: Long? = null,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Double? = null,
    val speed: Double? = null,
    val heartRate: Int? = null,
    val timestamp: String,
    val sequenceOrder: Int
)
