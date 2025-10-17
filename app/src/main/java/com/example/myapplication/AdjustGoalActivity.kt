package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.TokenManager
import com.example.myapplication.landr.goal.UpdateFitnessGoalRequest
import com.example.myapplication.landr.loginapp.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdjustGoalActivity : AppCompatActivity() {

    private lateinit var etWeeklyDistance: EditText
    private lateinit var etWeeklyWorkouts: EditText
    private lateinit var etTargetPace: EditText
    private lateinit var etWeeklyCalories: EditText
    private lateinit var btnSaveGoal: Button
    private lateinit var loadingProgress: ProgressBar
    private lateinit var messageText: TextView
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust_goal)

        tokenManager = TokenManager.getInstance(this)
        initViews()
        loadCurrentGoal()
    }

    private fun initViews() {
        findViewById<android.widget.ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        etWeeklyDistance = findViewById(R.id.et_weekly_distance)
        etWeeklyWorkouts = findViewById(R.id.et_weekly_workouts)
        etTargetPace = findViewById(R.id.et_target_pace)
        etWeeklyCalories = findViewById(R.id.et_weekly_calories)
        btnSaveGoal = findViewById(R.id.btn_save_goal)
        loadingProgress = findViewById(R.id.loading_progress)
        messageText = findViewById(R.id.message_text)

        btnSaveGoal.setOnClickListener { saveGoal() }
    }

    private fun loadCurrentGoal() {
        showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getFitnessGoal()
                withContext(Dispatchers.Main) {
                    hideLoading()
                    if (response.isSuccessful && response.body()?.code == 0) {
                        response.body()?.data?.let { goal ->
                            goal.weeklyDistanceKm?.let { etWeeklyDistance.setText(it.toString()) }
                            goal.weeklyWorkouts?.let { etWeeklyWorkouts.setText(it.toString()) }
                            goal.targetAvgPaceSecPerKm?.let { etTargetPace.setText((it / 60.0).toString()) }
                            goal.weeklyCalories?.let { etWeeklyCalories.setText(it.toString()) }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    if (e.message?.contains("401") == true) navigateToLogin()
                }
            }
        }
    }

    private fun saveGoal() {
        val distanceText = etWeeklyDistance.text.toString().trim()
        val workoutsText = etWeeklyWorkouts.text.toString().trim()
        val paceText = etTargetPace.text.toString().trim()
        val caloriesText = etWeeklyCalories.text.toString().trim()

        val distance = distanceText.toDoubleOrNull()
        val workouts = workoutsText.toIntOrNull()
        val paceMinutes = paceText.toDoubleOrNull()
        val pace = paceMinutes?.let { (it * 60).toInt() }
        val calories = caloriesText.toIntOrNull()

        if (distance == null && workouts == null && pace == null && calories == null) {
            showError("Please enter at least one goal")
            return
        }

        showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UpdateFitnessGoalRequest(distance, workouts, pace, calories)
                val response = RetrofitClient.api.updateFitnessGoal(request)

                withContext(Dispatchers.Main) {
                    hideLoading()
                    if (response.isSuccessful && response.body()?.code == 0) {
                        showSuccessDialog()
                    } else {
                        showError(response.body()?.message ?: "Failed to update goal")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    if (e.message?.contains("401") == true) {
                        navigateToLogin()
                    } else {
                        showError("Network error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun showLoading() {
        loadingProgress.visibility = View.VISIBLE
        btnSaveGoal.isEnabled = false
        messageText.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgress.visibility = View.GONE
        btnSaveGoal.isEnabled = true
    }

    private fun showError(message: String) {
        messageText.text = message
        messageText.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        messageText.visibility = View.VISIBLE
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Your fitness goals have been updated successfully!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToLogin() {
        tokenManager.clearTokens()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

