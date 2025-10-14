package com.example.myapplication.group

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class Result<T>(val code: Int, val message: String, val data: T?)

data class CreateGroupBody(val name: String, val memberLimit: Int? = 6)
data class GroupInfo(
    val id: Long,
    val name: String,
    val week: Int,
    val score: Int,
    val weeklyProgress: Int,
    val weeklyGoal: Int,
    val couponCount: Int,
    val memberCount: Int,
    val isOwner: Boolean
)

interface GroupApi {
    @POST("/api/group/create")
    fun create(@Body body: CreateGroupBody): Call<Result<GroupInfo>>

    @GET("/api/group/me")
    fun myGroup(): Call<Result<GroupInfo>>
}
