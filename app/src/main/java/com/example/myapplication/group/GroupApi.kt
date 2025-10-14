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
    val week: Int?,
    val score: Int?,
    val weeklyProgress: Int?,
    val weeklyGoal: Int?,
    val couponCount: Int?,
    val memberCount: Int?,
    val isOwner: Boolean?
)

data class JoinGroupBody(val groupId: Long, val inviterUserId: Long? = null)
data class ModerateBody(val applicationId: Long, val approve: Boolean, val reason: String? = null)
data class MemberInteractBody(val targetUserId: Long, val action: String) // action: LIKE or REMIND

data class ApplicationItem(
    val id: Long,
    val userId: Long,
    val userName: String,
    val groupId: Long,
    val groupName: String,
    val timestamp: Long,
    val status: String
)

data class GroupMemberInfo(
    val userId: Long,
    val name: String,
    val weeklyLikeCount: Int,
    val weeklyRemindCount: Int,
    val completedThisWeek: Boolean,
    val isSelf: Boolean,
    val weeklyDistanceKmDone: Double?,
    val weeklyDistanceKmGoal: Double?,
    val progressPercent: Int?
)

data class NotificationItem(
    val id: Long,
    val userId: Long,
    val type: String,
    val title: String?,
    val content: String?,
    val read: Boolean,
    val createdAt: String?
)

interface GroupApi {
    @POST("/api/group/create")
    fun create(@Body body: CreateGroupBody): Call<Result<GroupInfo>>

    @GET("/api/group/me")
    fun myGroup(): Call<Result<GroupInfo>>

    @POST("/api/group/leave")
    fun leave(): Call<Result<String>>

    @POST("/api/group/join")
    fun join(@Body body: JoinGroupBody): Call<Result<Any>>

    @GET("/api/group/applications/received")
    fun receivedApplications(): Call<Result<List<ApplicationItem>>>

    @POST("/api/group/applications/moderate")
    fun moderate(@Body body: ModerateBody): Call<Result<String>>

    @POST("/api/group/members/interact")
    fun interact(@Body body: MemberInteractBody): Call<Result<String>>

    @POST("/api/group/weekly/complete")
    fun weeklyComplete(): Call<Result<String>>

    @GET("/api/group/members")
    fun listMembers(): Call<Result<List<GroupMemberInfo>>>

    @GET("/api/group/notifications")
    fun notifications(): Call<Result<List<NotificationItem>>>
}
