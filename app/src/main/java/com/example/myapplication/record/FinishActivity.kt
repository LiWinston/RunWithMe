package com.example.myapplication.record

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.MainActivity
import com.example.myapplication.landr.TokenManager
import com.example.myapplication.landr.RetrofitClient as LandrRetrofit
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
        val tvSpeed = findViewById<TextView>(R.id.tvPace)  // Use speed to fill pace field
        val tvSteps = findViewById<TextView>(R.id.tvSteps)
        val tvWorkoutType = findViewById<TextView>(R.id.tvWorkoutType)
        val ivWorkoutIcon = findViewById<ImageView>(R.id.ivWorkoutIcon)
        val btnDone = findViewById<Button>(R.id.btnDone)

        // Get passed data
        val distance = intent.getStringExtra("distance") ?: "0.00 m"
        val duration = intent.getStringExtra("duration") ?: "00:00:00"
        val calories = intent.getStringExtra("calories") ?: "0 kcal"
        val speed = intent.getStringExtra("speed") ?: "0.00 m/s"
        val workoutType = intent.getStringExtra("workoutType") ?: "Running"

        tvDistance.text = distance
        tvDuration.text = duration
        tvCalories.text = calories
        tvSpeed.text = speed  // Display speed directly
        
        // Display steps from ViewModel
        val steps = workoutViewModel.steps.value ?: 0
        tvSteps.text = steps.toString()
        
        // Set workout type and corresponding icon
        when (workoutType) {
            "Walking" -> {
                tvWorkoutType.text = "Walking"
                ivWorkoutIcon.setImageResource(R.drawable.walking)
            }
            "Brisk Walking" -> {
                tvWorkoutType.text = "Brisk Walking"
                ivWorkoutIcon.setImageResource(R.drawable.walking)
            }
            "Jogging" -> {
                tvWorkoutType.text = "Jogging"
                ivWorkoutIcon.setImageResource(R.drawable.jogging)
            }
            "Running" -> {
                tvWorkoutType.text = "Running"
                ivWorkoutIcon.setImageResource(R.drawable.running)
            }
            "Fast Running" -> {
                tvWorkoutType.text = "Fast Running"
                ivWorkoutIcon.setImageResource(R.drawable.running)
            }
            else -> {
                tvWorkoutType.text = workoutType
                ivWorkoutIcon.setImageResource(R.drawable.jogging) // Default to jogging icon
            }
        }

        btnDone.setOnClickListener {
            // Save workout to database (asynchronous)
            saveWorkoutToDatabase()

            // Navigate back to home page
            navigateBackToStart()
        }
    }

    /**
     * Save workout record to database (asynchronous processing)
     */
    private fun saveWorkoutToDatabase() {
        val distance = intent.getStringExtra("distance") ?: "0.00 m"
        val duration = intent.getStringExtra("duration") ?: "00:00:00"
        val calories = intent.getStringExtra("calories") ?: "0 kcal"
        val speed = intent.getStringExtra("speed") ?: "0.00 m/s"

        val dynamicData = workoutViewModel.getWorkoutDynamicData()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val endTimeStr = sdf.format(java.util.Date())
        val durationSeconds = parseDuration(duration)?.toLong() ?: 0L
        val startTimeStr = sdf.format(java.util.Date(System.currentTimeMillis() - durationSeconds * 1000))

        // 从登录态读取当前用户ID
        val userIdFromToken = TokenManager.getInstance(applicationContext).getUserId()
        val workoutRequest = WorkoutCreateRequest(
            userId = userIdFromToken,
            workoutType = "OUTDOOR_RUN",
            distance = parseDistance(distance),
            duration = parseDuration(duration),
            steps = intent.getIntExtra("steps", workoutViewModel.steps.value ?: 0),
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

                        // Show save success message (JSON data saved in one go)
                        if (workoutId != null) {
                            showSaveSuccess(workoutId)
                        } else {
                            Toast.makeText(this@FinishActivity, "Workout saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMsg = response.body()?.message ?: "Save failed"
                        Toast.makeText(this@FinishActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@FinishActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Navigate back to main page
     */
    private fun navigateBackToStart() {
        // Clear task stack and return to main page
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    // Helper function - parse distance string and convert to kilometers (backend expects km)
    private fun parseDistance(distanceStr: String): Double? {
        return try {
            val regex = Regex("""(\d+\.?\d*)\s*(m|km)""")
            val matchResult = regex.find(distanceStr)
            if (matchResult != null) {
                val value = matchResult.groupValues[1].toDouble()
                val unit = matchResult.groupValues[2]
                when (unit) {
                    "km" -> value              // Already in km
                    "m" -> value / 1000.0      // Convert m to km (backend stores in km)
                    else -> value / 1000.0
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


    // Helper function - parse duration string (e.g. "01:23:45" -> 5025 seconds)
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

    // Helper function - parse calories string (e.g. "120 kcal" -> 120.0)
    private fun parseCalories(caloriesStr: String): Double? {
        return try {
            val regex = Regex("""(\d+\.?\d*)\s*kcal""")
            val matchResult = regex.find(caloriesStr)
            matchResult?.groupValues?.get(1)?.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    // Helper function - parse speed string and convert to km/h (backend expects km/h)
    private fun parseSpeed(speedStr: String): Double? {
        return try {
            val regex = Regex("""(\d+\.?\d*)\s*(m/s|mps)""")
            val matchResult = regex.find(speedStr)
            if (matchResult != null) {
                val value = matchResult.groupValues[1].toDouble()
                value * 3.6  // Convert m/s to km/h (backend stores in km/h)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


    // Helper function - calculate average pace (seconds/km, backend expects seconds per km)
    private fun calculateAvgPace(distance: Double?, duration: Int?): Int? {
        return if (distance != null && duration != null && distance > 0) {
            // distance is now in km, so duration/distance gives seconds per km
            (duration / distance).toInt()
        } else {
            null
        }
    }

    // Helper function - check goal achievement (distance>=1km or duration>=15min)
    private fun checkGoalAchievement(distance: Double?, duration: Int?): Boolean {
        val distanceGoal = distance != null && distance >= 1.0 // distance is now in km
        val durationGoal = duration != null && duration >= 900 // 15 minutes = 900 seconds
        return distanceGoal || durationGoal
    }


    // Show save success message
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
                "Workout saved successfully! Contains ${totalDataPoints} data points", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@FinishActivity, "Workout saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}

// Frontend data transfer entity - adapted for new backend structure
data class WorkoutCreateRequest(
    val userId: Long,
    val workoutType: String = "OUTDOOR_RUN",
    val distance: Double?,
    val duration: Int?, // seconds
    val steps: Int?,
    val calories: Double?,
    val avgSpeed: Double?,
    val avgPace: Int?,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val startTime: String, // ISO format time
    val endTime: String?,
    val status: String = "COMPLETED",
    val visibility: String = "PRIVATE",
    val goalAchieved: Boolean = false,
    val groupId: Long? = null,
    val notes: String? = null,
    val weatherCondition: String? = null,
    val temperature: Double? = null,
    val latitude: Double? = null, // Latitude
    val longitude: Double? = null, // Longitude
    val workoutData: WorkoutDynamicData? = null // JSON dynamic data
)

// API response wrapper
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)