package com.example.myapplication.record

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import com.example.myapplication.record.WorkoutViewModel

class FinishActivity : AppCompatActivity() {

    private val workoutViewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)

        val tvDistance = findViewById<TextView>(R.id.tvDistance)
        val tvDuration = findViewById<TextView>(R.id.tvDuration)
        val tvCalories = findViewById<TextView>(R.id.tvCalories)
        val tvSpeed = findViewById<TextView>(R.id.tvPace)  // 用 speed 填 pace 的格子
        val btnDone = findViewById<Button>(R.id.btnDone)

        // 取出传递过来的数据
        val distance = intent.getStringExtra("distance") ?: "0.00 miles"
        val duration = intent.getStringExtra("duration") ?: "00:00:00"
        val calories = intent.getStringExtra("calories") ?: "0 kcal"
        val speed = intent.getStringExtra("speed") ?: "0.00 mph"

        tvDistance.text = distance
        tvDuration.text = duration
        tvCalories.text = calories
        tvSpeed.text = speed  // 直接显示速度

        btnDone.setOnClickListener {
            // 暂时注释掉数据库操作，待异步处理
            // saveWorkoutToDatabase()

            // 直接返回到主页面
            navigateBackToStart()
        }
    }

    /**
     * 保存运动记录到数据库（暂时 mute，待异步处理）
     */
    private fun saveWorkoutToDatabase() {
        val distance = intent.getStringExtra("distance") ?: "0.00 miles"
        val duration = intent.getStringExtra("duration") ?: "00:00:00"
        val calories = intent.getStringExtra("calories") ?: "0 kcal"
        val speed = intent.getStringExtra("speed") ?: "0.00 mph"

        val dynamicData = workoutViewModel.getWorkoutDynamicData()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val endTimeStr = sdf.format(java.util.Date())
        val durationSeconds = parseDuration(duration)?.toLong() ?: 0L
        val startTimeStr = sdf.format(java.util.Date(System.currentTimeMillis() - durationSeconds * 1000))

        val workoutRequest = WorkoutCreateRequest(
            userId = 1L,
            workoutType = "OUTDOOR_RUN",
            distance = parseDistance(distance),
            duration = parseDuration(duration),
            steps = workoutViewModel.steps.value,
            calories = parseCalories(calories),
            avgSpeed = parseSpeed(speed),
            avgPace = calculateAvgPace(parseDistance(distance), parseDuration(duration)),
            avgHeartRate = workoutViewModel.heartRate.value?.takeIf { it > 0 },
            maxHeartRate = workoutViewModel.heartRate.value?.takeIf { it > 0 },
            startTime = startTimeStr,
            endTime = endTimeStr,
            status = "COMPLETED",
            visibility = "PRIVATE",
            goalAchieved = checkGoalAchievement(parseDistance(distance), parseDuration(duration)),
            notes = null,
            weatherCondition = "晴天",
            temperature = 25.0,
            latitude = if (dynamicData.route.isNotEmpty()) dynamicData.route.first().lat else 39.9042,
            longitude = if (dynamicData.route.isNotEmpty()) dynamicData.route.first().lng else 116.4074,
            workoutData = dynamicData
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.createWorkout(workoutRequest)
                runOnUiThread {
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val workoutId = response.body()?.data?.id

                        // 显示保存成功信息（JSON数据已经一次性保存）
                        if (workoutId != null) {
                            showSaveSuccess(workoutId)
                        } else {
                            Toast.makeText(this@FinishActivity, "运动记录保存成功！", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMsg = response.body()?.message ?: "保存失败"
                        Toast.makeText(this@FinishActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@FinishActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 返回到主页面
     */
    private fun navigateBackToStart() {
        // 清空任务栈，回到主页面
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    // 辅助函数 - 解析距离字符串 (如 "2.5 miles" -> 4.023 km)
    private fun parseDistance(distanceStr: String): Double? {
        return try {
            val regex = Regex("""(\d+\.?\d*)\s*(miles|km)""")
            val matchResult = regex.find(distanceStr)
            if (matchResult != null) {
                val value = matchResult.groupValues[1].toDouble()
                val unit = matchResult.groupValues[2]
                when (unit) {
                    "miles" -> value * 1.609344 // 转换为公里
                    "km" -> value
                    else -> value
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // 辅助函数 - 解析时长字符串 (如 "01:23:45" -> 5025 秒)
    private fun parseDuration(durationStr: String): Int? {
        return try {
            val parts = durationStr.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toInt()
                val minutes = parts[1].toInt()
                val seconds = parts[2].toInt()
                hours * 3600 + minutes * 60 + seconds
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // 辅助函数 - 解析卡路里字符串 (如 "120 kcal" -> 120.0)
    private fun parseCalories(caloriesStr: String): Double? {
        return try {
            val regex = Regex("""(\d+\.?\d*)\s*kcal""")
            val matchResult = regex.find(caloriesStr)
            matchResult?.groupValues?.get(1)?.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    // 辅助函数 - 解析速度字符串 (如 "5.2 mph" -> 8.369 km/h)
    private fun parseSpeed(speedStr: String): Double? {
        return try {
            val regex = Regex("""(\d+\.?\d*)\s*(mph|kmh|km/h)""")
            val matchResult = regex.find(speedStr)
            if (matchResult != null) {
                val value = matchResult.groupValues[1].toDouble()
                val unit = matchResult.groupValues[2]
                when (unit) {
                    "mph" -> value * 1.609344 // 转换为 km/h
                    "kmh", "km/h" -> value
                    else -> value
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // 辅助函数 - 计算平均配速 (秒/公里)
    private fun calculateAvgPace(distance: Double?, duration: Int?): Int? {
        return if (distance != null && duration != null && distance > 0) {
            (duration / distance).toInt()
        } else {
            null
        }
    }

    // 辅助函数 - 检查目标达成 (距离>=1km 或 时长>=15分钟)
    private fun checkGoalAchievement(distance: Double?, duration: Int?): Boolean {
        val distanceGoal = distance != null && distance >= 1.0
        val durationGoal = duration != null && duration >= 900 // 15分钟
        return distanceGoal || durationGoal
    }

    // 显示保存成功信息
    private fun showSaveSuccess(workoutId: Long) {
        val dynamicData = workoutViewModel.getWorkoutDynamicData()
        val totalDataPoints = dynamicData.route.size +
                dynamicData.speedSamples.size +
                dynamicData.heartRateSamples.size +
                dynamicData.elevationSamples.size +
                dynamicData.paceSamples.size +
                dynamicData.cadenceSamples.size

        if (totalDataPoints > 0) {
            Toast.makeText(this@FinishActivity,
                "运动记录保存成功！包含${totalDataPoints}个数据点", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@FinishActivity, "运动记录保存成功！", Toast.LENGTH_SHORT).show()
        }
    }
}

// 前端用来传输数据的实体 - 适配新的后端结构
data class WorkoutCreateRequest(
    val userId: Long,
    val workoutType: String = "OUTDOOR_RUN",
    val distance: Double?,
    val duration: Int?, // 秒
    val steps: Int?,
    val calories: Double?,
    val avgSpeed: Double?,
    val avgPace: Int?,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val startTime: String, // ISO格式时间
    val endTime: String?,
    val status: String = "COMPLETED",
    val visibility: String = "PRIVATE",
    val goalAchieved: Boolean = false,
    val groupId: Long? = null,
    val notes: String? = null,
    val weatherCondition: String? = null,
    val temperature: Double? = null,
    val latitude: Double? = null, // 纬度
    val longitude: Double? = null, // 经度
    val workoutData: WorkoutDynamicData? = null // JSON动态数据
)

// API响应包装
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)