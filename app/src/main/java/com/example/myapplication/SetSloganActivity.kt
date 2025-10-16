package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.TokenManager
import com.example.myapplication.landr.goal.UpdateSloganRequest
import com.example.myapplication.landr.loginapp.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetSloganActivity : AppCompatActivity() {

    private lateinit var tvCurrentSlogan: TextView
    private lateinit var etSlogan: EditText
    private lateinit var btnSaveSlogan: Button
    private lateinit var loadingProgress: ProgressBar
    private lateinit var messageText: TextView
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_slogan)

        tokenManager = TokenManager.getInstance(this)
        initViews()
        loadCurrentSlogan()
    }

    private fun initViews() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        tvCurrentSlogan = findViewById(R.id.tv_current_slogan)
        etSlogan = findViewById(R.id.et_slogan)
        btnSaveSlogan = findViewById(R.id.btn_save_slogan)
        loadingProgress = findViewById(R.id.loading_progress)
        messageText = findViewById(R.id.message_text)

        btnSaveSlogan.setOnClickListener {
            saveSlogan()
        }
    }

    private fun loadCurrentSlogan() {
        showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getFitnessGoal()

                withContext(Dispatchers.Main) {
                    hideLoading()
                    
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val slogan = response.body()?.data?.slogan
                        if (!slogan.isNullOrEmpty()) {
                            tvCurrentSlogan.text = slogan
                            tvCurrentSlogan.setTextColor(resources.getColor(android.R.color.black, null))
                            etSlogan.setText(slogan)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    if (e.message?.contains("401") == true ||
                        e.message?.contains("expired") == true ||
                        e.message?.contains("Unauthorized") == true) {
                        navigateToLogin()
                    }
                }
            }
        }
    }

    private fun saveSlogan() {
        val slogan = etSlogan.text.toString().trim()

        if (slogan.isEmpty()) {
            showError("Please enter a slogan")
            return
        }

        showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UpdateSloganRequest(slogan)
                val response = RetrofitClient.api.updateSlogan(request)

                withContext(Dispatchers.Main) {
                    hideLoading()

                    if (response.isSuccessful && response.body()?.code == 0) {
                        showSuccessDialog()
                    } else {
                        val errorMsg = response.body()?.message ?: "Failed to update slogan"
                        showError(errorMsg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    if (e.message?.contains("401") == true ||
                        e.message?.contains("expired") == true ||
                        e.message?.contains("Unauthorized") == true) {
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
        btnSaveSlogan.isEnabled = false
        messageText.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgress.visibility = View.GONE
        btnSaveSlogan.isEnabled = true
    }

    private fun showError(message: String) {
        messageText.text = message
        messageText.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        messageText.visibility = View.VISIBLE
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Your slogan has been updated successfully!")
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

