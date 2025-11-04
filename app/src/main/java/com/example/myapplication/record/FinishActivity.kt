package com.example.myapplication.record

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import android.view.View
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
    private var isSaving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)

        val tvDistance = findViewById<TextView>(R.id.tvDistance)
        val tvDuration = findViewById<TextView>(R.id.tvDuration)
        val tvCalories = findViewById<TextView>(R.id.tvCalories)
        val tvPace = findViewById<TextView>(R.id.tvPace)  // Display average pace (m/s)
        val tvSteps = findViewById<TextView>(R.id.tvSteps)
        val tvWorkoutType = findViewById<TextView>(R.id.tvWorkoutType)
        val ivWorkoutIcon = findViewById<ImageView>(R.id.ivWorkoutIcon)
        val btnDone = findViewById<Button>(R.id.btnDone)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Get passed data
        val distance = intent.getStringExtra("distance") ?: "0.00 m"
        val duration = intent.getStringExtra("duration") ?: "00:00:00"
        val calories = intent.getStringExtra("calories") ?: "0 kcal"
        val workoutType = intent.getStringExtra("workoutType") ?: "Running"

        tvDistance.text = distance
        tvDuration.text = duration
        tvCalories.text = calories
        
        // Calculate and display average pace in m/s
        val avgPaceMps = calculateAvgPaceMps(parseDistanceToMeters(distance), parseDuration(duration))
        tvPace.text = if (avgPaceMps > 0) {
            String.format("%.2f m/s", avgPaceMps)
        } else {
            "0.00 m/s"
        }
        
        // Display steps from ViewModel
        val steps = intent.getIntExtra("steps", 0)
        tvSteps.text = steps.toString()
        
        android.util.Log.d("FinishActivity", "Received steps: $steps")
        
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
            if (!isSaving) {
                // Save workout to database (asynchronous)
                saveWorkoutToDatabase(btnDone, progressBar)
            }
        }
    }

    /**
     * Save workout record to database (asynchronous processing)
     */
    private fun saveWorkoutToDatabase(btnDone: Button, progressBar: ProgressBar) {
        if (isSaving) return
        
        isSaving = true
        btnDone.isEnabled = false
        progressBar.visibility = View.VISIBLE
        
        val distance = intent.getStringExtra("distance") ?: "0.00 m"
        val duration = intent.getStringExtra("duration") ?: "00:00:00"
        val calories = intent.getStringExtra("calories") ?: "0 kcal"
        val steps = intent.getIntExtra("steps", 0)

        android.util.Log.d("FinishActivity", "Saving workout with steps: $steps")

        val dynamicData = workoutViewModel.getWorkoutDynamicData()
        
        // Create SimpleDateFormat with UTC timezone to match database serverTimezone=UTC
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        
        // Log the device timezone for debugging
        val deviceTimezone = java.util.TimeZone.getDefault().id
        android.util.Log.d("FinishActivity", "Device timezone: $deviceTimezone, Using UTC for backend")
        
        val endTime = java.util.Date()
        val endTimeStr = sdf.format(endTime)
        val durationSeconds = parseDuration(duration)?.toLong() ?: 0L
        val startTime = java.util.Date(System.currentTimeMillis() - durationSeconds * 1000)
        val startTimeStr = sdf.format(startTime)
        
        android.util.Log.d("FinishActivity", "Start time (UTC): $startTimeStr, End time (UTC): $endTimeStr")

        // 从登录态读取当前用户ID
        val userIdFromToken = TokenManager.getInstance(applicationContext).getUserId()
        val workoutRequest = WorkoutCreateRequest(
            userId = userIdFromToken,
            workoutType = "OUTDOOR_RUN",
            distance = parseDistance(distance),
            duration = parseDuration(duration),
            steps = steps,
            calories = parseCalories(calories),
            avgSpeed = calculateAvgSpeedKmh(parseDistance(distance), parseDuration(duration)),
            avgPace = calculateAvgPaceSecondsPerKm(parseDistance(distance), parseDuration(duration)),
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

        android.util.Log.d("FinishActivity", "Workout request created with steps: ${workoutRequest.steps}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.createWorkout(workoutRequest)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    isSaving = false
                    
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val workoutId = response.body()?.data?.id

                        android.util.Log.d("FinishActivity", "Workout saved successfully with ID: $workoutId")
                        
                        // Show save success message (JSON data saved in one go)
                        if (workoutId != null) {
                            showSaveSuccess(workoutId)
                        } else {
                            Toast.makeText(this@FinishActivity, "Workout saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                        
                        // Navigate back to home page after successful save
                        navigateBackToStart()
                    } else {
                        val errorMsg = response.body()?.message ?: "Save failed"
                        android.util.Log.e("FinishActivity", "Save failed: $errorMsg")
                        Toast.makeText(this@FinishActivity, "Save failed: $errorMsg", Toast.LENGTH_LONG).show()
                        btnDone.isEnabled = true  // Re-enable button on failure
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    isSaving = false
                    android.util.Log.e("FinishActivity", "Network error: ${e.message}", e)
                    Toast.makeText(this@FinishActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    btnDone.isEnabled = true  // Re-enable button on error
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

    // Helper function - parse distance string and convert to meters
    private fun parseDistanceToMeters(distanceStr: String): Double? {
        return try {
            val regex = Regex("""(\d+\.?\d*)\s*(m|km)""")
            val matchResult = regex.find(distanceStr)
            if (matchResult != null) {
                val value = matchResult.groupValues[1].toDouble()
                val unit = matchResult.groupValues[2]
                when (unit) {
                    "km" -> value * 1000.0     // Convert km to m
                    "m" -> value               // Already in m
                    else -> value
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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

    // Helper function - calculate average pace in m/s for display
    private fun calculateAvgPaceMps(distanceMeters: Double?, durationSeconds: Int?): Double {
        return if (distanceMeters != null && durationSeconds != null && durationSeconds > 0 && distanceMeters > 0) {
            distanceMeters / durationSeconds  // m/s
        } else {
            0.0
        }
    }

    // Helper function - calculate average speed in km/h for backend
    private fun calculateAvgSpeedKmh(distanceKm: Double?, durationSeconds: Int?): Double? {
        return if (distanceKm != null && durationSeconds != null && durationSeconds > 0 && distanceKm > 0) {
            (distanceKm / durationSeconds) * 3600  // km/h
        } else {
            null
        }
    }

    // Helper function - calculate average pace (seconds/km, backend expects seconds per km)
    private fun calculateAvgPaceSecondsPerKm(distanceKm: Double?, durationSeconds: Int?): Int? {
        return if (distanceKm != null && durationSeconds != null && distanceKm > 0) {
            // distance is now in km, so duration/distance gives seconds per km
            (durationSeconds / distanceKm).toInt()
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