package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.workout.Workout
import com.example.myapplication.landr.TokenManager
import com.example.myapplication.landr.loginapp.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutListActivity : AppCompatActivity() {

    private lateinit var loadingProgress: ProgressBar
    private lateinit var errorMessage: TextView
    private lateinit var emptyState: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_list)

        tokenManager = TokenManager.getInstance(this)
        initViews()
        loadWorkouts()
    }

    private fun initViews() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        loadingProgress = findViewById(R.id.loading_progress)
        errorMessage = findViewById(R.id.error_message)
        emptyState = findViewById(R.id.empty_state)
        recyclerView = findViewById(R.id.workout_recycler_view)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadWorkouts() {
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

                val response = RetrofitClient.api.getUserWorkouts(userId, page = 1, size = 50)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val workouts = response.body()?.data
                        if (workouts != null && workouts.isNotEmpty()) {
                            displayWorkouts(workouts)
                        } else {
                            showEmptyState()
                        }
                    } else {
                        showError("Failed to load workout records")
                    }
                    hideLoading()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (e.message?.contains("401") == true ||
                        e.message?.contains("expired") == true ||
                        e.message?.contains("Unauthorized") == true) {
                        Toast.makeText(
                            this@WorkoutListActivity,
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

    private fun displayWorkouts(workouts: List<Workout>) {
        recyclerView.adapter = WorkoutAdapter(workouts)
        recyclerView.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
    }

    private fun showLoading() {
        loadingProgress.visibility = View.VISIBLE
        errorMessage.visibility = View.GONE
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgress.visibility = View.GONE
    }

    private fun showError(message: String) {
        errorMessage.text = message
        errorMessage.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        emptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun navigateToLogin() {
        tokenManager.clearTokens()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

