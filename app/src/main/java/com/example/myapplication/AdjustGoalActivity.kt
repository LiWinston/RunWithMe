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

    private lateinit var spinnerWeeklyDistance: Spinner
    private lateinit var btnSaveGoal: Button
    private lateinit var loadingProgress: ProgressBar
    private lateinit var messageText: TextView
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust_goal)

        tokenManager = TokenManager.getInstance(this)
        initViews()
        setupWeeklyDistanceSpinner()
        loadCurrentGoal()
    }

    private fun initViews() {
        findViewById<android.widget.ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        spinnerWeeklyDistance = findViewById(R.id.spinner_weekly_distance)
        btnSaveGoal = findViewById(R.id.btn_save_goal)
        loadingProgress = findViewById(R.id.loading_progress)
        messageText = findViewById(R.id.message_text)

        btnSaveGoal.setOnClickListener { saveGoal() }
    }

    private fun setupWeeklyDistanceSpinner() {
        // Weekly distance options in km
        val distanceOptions = arrayOf(
            "5 km",
            "10 km",
            "15 km",
            "20 km",
            "25 km",
            "30 km",
            "40 km",
            "50 km"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, distanceOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWeeklyDistance.adapter = adapter
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
                            goal.weeklyDistanceKm?.let { distance ->
                                // Set spinner selection based on current goal
                                val position = when (distance.toInt()) {
                                    5 -> 0
                                    10 -> 1
                                    15 -> 2
                                    20 -> 3
                                    25 -> 4
                                    30 -> 5
                                    40 -> 6
                                    50 -> 7
                                    else -> 1 // default to 10km
                                }
                                spinnerWeeklyDistance.setSelection(position)
                            }
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
        // Get selected weekly distance from spinner
        val distance = when (spinnerWeeklyDistance.selectedItemPosition) {
            0 -> 5.0
            1 -> 10.0
            2 -> 15.0
            3 -> 20.0
            4 -> 25.0
            5 -> 30.0
            6 -> 40.0
            7 -> 50.0
            else -> 10.0
        }

        showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UpdateFitnessGoalRequest(
                    weeklyDistanceKm = distance,
                    weeklyWorkouts = null,
                    targetAvgPaceSecPerKm = null,
                    weeklyCalories = null
                )
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
            .setMessage("Your weekly distance goal has been updated successfully!")
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

