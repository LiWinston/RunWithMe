package com.example.myapplication.landr.profile

data class UserProfileResponse(
    val code: Int,  // 后端使用 code 而不是 status
    val message: String,
    val data: UserProfile?
) {
    // 添加便捷属性以兼容旧代码
    val status: Int
        get() = code
}

data class UserProfile(
    val id: Long,
    val username: String,
    val email: String?,
    val firstName: String,
    val lastName: String,
    val gender: String?,
    val age: Int?,
    val phoneNumber: String?,
    val fitnessLevel: String?,
    val height: Double?,
    val weight: Double?,
    val fitnessGoal: FitnessGoal?,
    val weeklyAvailability: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class FitnessGoal(
    val goal: String?,
    val targetWeight: Double?,
    val targetDate: String?
)

