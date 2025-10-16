package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
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
import com.example.myapplication.landr.loginapp.LoginActivity
import com.example.myapplication.landr.password.ChangePasswordRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var loadingProgress: ProgressBar
    private lateinit var messageText: TextView
    private lateinit var tokenManager: TokenManager
    
    private lateinit var ivToggleCurrentPassword: ImageView
    private lateinit var ivToggleNewPassword: ImageView
    private lateinit var ivToggleConfirmPassword: ImageView
    
    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        tokenManager = TokenManager.getInstance(this)
        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        etCurrentPassword = findViewById(R.id.et_current_password)
        etNewPassword = findViewById(R.id.et_new_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnChangePassword = findViewById(R.id.btn_change_password)
        loadingProgress = findViewById(R.id.loading_progress)
        messageText = findViewById(R.id.message_text)
        
        ivToggleCurrentPassword = findViewById(R.id.iv_toggle_current_password)
        ivToggleNewPassword = findViewById(R.id.iv_toggle_new_password)
        ivToggleConfirmPassword = findViewById(R.id.iv_toggle_confirm_password)
    }

    private fun setupClickListeners() {
        btnChangePassword.setOnClickListener {
            changePassword()
        }
        
        // Toggle password visibility for current password
        ivToggleCurrentPassword.setOnClickListener {
            isCurrentPasswordVisible = !isCurrentPasswordVisible
            togglePasswordVisibility(etCurrentPassword, isCurrentPasswordVisible)
        }
        
        // Toggle password visibility for new password
        ivToggleNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(etNewPassword, isNewPasswordVisible)
        }
        
        // Toggle password visibility for confirm password
        ivToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(etConfirmPassword, isConfirmPasswordVisible)
        }
    }
    
    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean) {
        if (isVisible) {
            // Show password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            // Hide password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        // Move cursor to end
        editText.setSelection(editText.text.length)
    }

    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // 验证输入
        if (currentPassword.isEmpty()) {
            showError("Please enter current password")
            return
        }

        if (newPassword.isEmpty()) {
            showError("Please enter new password")
            return
        }

        if (newPassword.length < 6 || newPassword.length > 20) {
            showError("Password must be 6-20 characters long")
            return
        }

        if (confirmPassword.isEmpty()) {
            showError("Please confirm new password")
            return
        }

        if (newPassword != confirmPassword) {
            showError("New passwords do not match")
            return
        }

        if (currentPassword == newPassword) {
            showError("New password must be different from current password")
            return
        }

        // 调用API
        showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )

                val response = RetrofitClient.api.changePassword(request)

                withContext(Dispatchers.Main) {
                    hideLoading()

                    if (response.isSuccessful && response.body()?.code == 0) {
                        showSuccessDialog()
                    } else {
                        val errorMsg = response.body()?.message ?: "Failed to change password"
                        showError(errorMsg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    if (e.message?.contains("401") == true ||
                        e.message?.contains("expired") == true ||
                        e.message?.contains("Unauthorized") == true) {
                        Toast.makeText(
                            this@ChangePasswordActivity,
                            "Session expired. Please login again.",
                            Toast.LENGTH_LONG
                        ).show()
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
        btnChangePassword.isEnabled = false
        messageText.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgress.visibility = View.GONE
        btnChangePassword.isEnabled = true
    }

    private fun showError(message: String) {
        messageText.text = message
        messageText.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        messageText.visibility = View.VISIBLE
    }

    private fun showSuccess(message: String) {
        messageText.text = message
        messageText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        messageText.visibility = View.VISIBLE
    }
    
    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Your password has been changed successfully!")
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

