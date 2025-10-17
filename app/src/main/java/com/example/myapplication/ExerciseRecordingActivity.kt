package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.workout.WorkoutStats
import com.example.myapplication.landr.TokenManager
import com.example.myapplication.landr.loginapp.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class ExerciseRecordingActivity : AppCompatActivity() {

    private lateinit var loadingProgress: ProgressBar
    private lateinit var errorMessage: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var tvTotalCalories: TextView
    private lateinit var tvTotalWorkouts: TextView
    private lateinit var btnViewRecords: Button
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_recording)

        tokenManager = TokenManager.getInstance(this)
        initViews()
        setupClickListeners()
        loadWorkoutStats()
    }

    private fun initViews() {
        findViewById<android.widget.ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
        
        loadingProgress = findViewById(R.id.loading_progress)
        errorMessage = findViewById(R.id.error_message)
        tvTotalDistance = findViewById(R.id.tv_total_distance)
        tvTotalDuration = findViewById(R.id.tv_total_duration)
        tvTotalCalories = findViewById(R.id.tv_total_calories)
        tvTotalWorkouts = findViewById(R.id.tv_total_workouts)
        btnViewRecords = findViewById(R.id.btn_view_records)
    }

    private fun setupClickListeners() {
        btnViewRecords.setOnClickListener {
            val intent = Intent(this, WorkoutListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadWorkoutStats() {
        showLoading()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get current user ID from token
                val userId = tokenManager.getUserId()
                if (userId == -1L) {
                    withContext(Dispatchers.Main) {
                        showError("User not logged in")
                        navigateToLogin()
                    }
                    return@launch
                }

                val response = RetrofitClient.api.getUserWorkoutStats(userId)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val stats = response.body()?.data
                        if (stats != null) {
                            displayStats(stats)
                        } else {
                            showError("No workout data available")
                        }
                    } else {
                        showError("Failed to load workout statistics")
                    }
                    hideLoading()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (e.message?.contains("401") == true ||
                        e.message?.contains("expired") == true ||
                        e.message?.contains("Unauthorized") == true) {
                        Toast.makeText(
                            this@ExerciseRecordingActivity,
                            "Session expired. Please login again.",
                            Toast.LENGTH_LONG
                        ).show()
                        navigateToLogin()
                    } else {
                        showError("Network error: ${e.message}")
                    }
                    hideLoading()
                }
            }
        }
    }

    private fun displayStats(stats: WorkoutStats) {
        // Total Distance
        val distance = stats.totalDistance ?: BigDecimal.ZERO
        tvTotalDistance.text = String.format("%.2f km", distance)

        // Total Duration (convert seconds to hours and minutes)
        val totalSeconds = stats.totalDuration ?: 0
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        tvTotalDuration.text = String.format("%dh %dm", hours, minutes)

        // Total Calories
        val calories = stats.totalCalories ?: BigDecimal.ZERO
        tvTotalCalories.text = String.format("%.0f kcal", calories)

        // Total Workouts
        tvTotalWorkouts.text = String.format("%d times", stats.totalWorkouts)
    }

    private fun showLoading() {
        loadingProgress.visibility = View.VISIBLE
        errorMessage.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgress.visibility = View.GONE
    }

    private fun showError(message: String) {
        errorMessage.text = message
        errorMessage.visibility = View.VISIBLE
    }

    private fun navigateToLogin() {
        tokenManager.clearTokens()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

