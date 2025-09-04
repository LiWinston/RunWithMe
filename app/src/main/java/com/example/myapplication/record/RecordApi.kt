package com.example.myapplication.record

import com.example.myapplication.record.Workout
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RecordApi {
    @POST("/workouts/save")  // 这里对应后端的接口路径
    suspend fun saveWorkout(@Body workout: Workout): Response<Void>
}
