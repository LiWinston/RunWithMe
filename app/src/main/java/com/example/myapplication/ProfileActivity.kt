package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.TokenManager
import com.example.myapplication.landr.loginapp.LoginActivity
import com.example.myapplication.landr.profile.UpdateProfileRequest
import com.example.myapplication.landr.profile.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var etEmail: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etAge: EditText
    private lateinit var etPhone: EditText
    private lateinit var spinnerFitnessLevel: Spinner
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView

    private var currentProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupSpinners()
        setupClickListeners()
        loadUserProfile()
    }

    private fun initViews() {
        tvUsername = findViewById(R.id.tv_username)
        etEmail = findViewById(R.id.et_email)
        etFirstName = findViewById(R.id.et_first_name)
        etLastName = findViewById(R.id.et_last_name)
        spinnerGender = findViewById(R.id.spinner_gender)
        etAge = findViewById(R.id.et_age)
        etPhone = findViewById(R.id.et_phone)
        spinnerFitnessLevel = findViewById(R.id.spinner_fitness_level)
        etHeight = findViewById(R.id.et_height)
        etWeight = findViewById(R.id.et_weight)
        btnSave = findViewById(R.id.btn_save)
        progressBar = findViewById(R.id.progress_bar)
        scrollView = findViewById(R.id.scroll_view)
    }

    private fun setupSpinners() {
        // Gender Spinner
        val genderOptions = arrayOf("Select Gender", "MALE", "FEMALE", "OTHER")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter
        // Disable gender editing
        spinnerGender.isEnabled = false

        // Fitness Level Spinner
        val fitnessOptions = arrayOf("Select Fitness Level", "BEGINNER", "INTERMEDIATE", "ADVANCED")
        val fitnessAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fitnessOptions)
        fitnessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFitnessLevel.adapter = fitnessAdapter
    }

    private fun setupClickListeners() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserProfile() {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getUserProfile()

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    // 添加详细日志
                    android.util.Log.d("ProfileActivity", "Response successful: ${response.isSuccessful}")
                    android.util.Log.d("ProfileActivity", "Response code: ${response.code()}")
                    android.util.Log.d("ProfileActivity", "Response body: ${response.body()}")
                    android.util.Log.d("ProfileActivity", "Status: ${response.body()?.status}")
                    android.util.Log.d("ProfileActivity", "Message: ${response.body()?.message}")
                    android.util.Log.d("ProfileActivity", "Data: ${response.body()?.data}")

                    if (response.isSuccessful && response.body()?.status == 0) {
                        val profile = response.body()?.data
                        if (profile != null) {
                            currentProfile = profile
                            displayProfile(profile)
                        } else {
                            showError("Failed to load profile data. Response status: ${response.body()?.status}, message: ${response.body()?.message}")
                        }
                    } else {
                        showError("Failed to load profile: ${response.body()?.message ?: "Unknown error"}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    android.util.Log.e("ProfileActivity", "Error loading profile", e)
                    
                    // 如果是token过期，跳转到登录页面
                    if (e.message?.contains("401") == true || 
                        e.message?.contains("expired") == true ||
                        e.message?.contains("Unauthorized") == true) {
                        Toast.makeText(this@ProfileActivity, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
                        navigateToLogin()
                    } else {
                        showError("Network error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun displayProfile(profile: UserProfile) {
        // Basic Information
        tvUsername.text = profile.username
        etEmail.setText(profile.email ?: "")
        etFirstName.setText(profile.firstName)
        etLastName.setText(profile.lastName)

        // Gender
        when (profile.gender) {
            "MALE" -> spinnerGender.setSelection(1)
            "FEMALE" -> spinnerGender.setSelection(2)
            "OTHER" -> spinnerGender.setSelection(3)
            else -> spinnerGender.setSelection(0)
        }

        // Personal Information
        etAge.setText(profile.age?.toString() ?: "")
        etPhone.setText(profile.phoneNumber ?: "")

        // Fitness Information
        when (profile.fitnessLevel) {
            "BEGINNER" -> spinnerFitnessLevel.setSelection(1)
            "INTERMEDIATE" -> spinnerFitnessLevel.setSelection(2)
            "ADVANCED" -> spinnerFitnessLevel.setSelection(3)
            else -> spinnerFitnessLevel.setSelection(0)
        }

        etHeight.setText(profile.height?.toString() ?: "")
        etWeight.setText(profile.weight?.toString() ?: "")
    }

    private fun saveProfile() {
        // Validate inputs
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showError("First name and last name are required")
            return
        }

        // Get selected values
        val email = etEmail.text.toString().trim().takeIf { it.isNotEmpty() }
        // Gender is not editable, use current profile value
        val gender = currentProfile?.gender

        val age = etAge.text.toString().trim().toIntOrNull()
        val phone = etPhone.text.toString().trim().takeIf { it.isNotEmpty() }

        val fitnessLevel = if (spinnerFitnessLevel.selectedItemPosition > 0) {
            spinnerFitnessLevel.selectedItem.toString()
        } else null

        val height = etHeight.text.toString().trim().toDoubleOrNull()
        val weight = etWeight.text.toString().trim().toDoubleOrNull()

        // Create update request
        val updateRequest = UpdateProfileRequest(
            email = email,
            firstName = firstName,
            lastName = lastName,
            gender = gender,
            age = age,
            phoneNumber = phone,
            fitnessLevel = fitnessLevel,
            height = height,
            weight = weight
        )

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.updateUserProfile(updateRequest)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful && response.body()?.status == 0) {
                        val updatedProfile = response.body()?.data
                        if (updatedProfile != null) {
                            currentProfile = updatedProfile
                            Toast.makeText(
                                this@ProfileActivity,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    } else {
                        showError("Failed to update profile: ${response.body()?.message}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Network error: ${e.message}")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        scrollView.visibility = if (show) View.GONE else View.VISIBLE
        btnSave.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun navigateToLogin() {
        val tokenManager = TokenManager.getInstance(this)
        tokenManager.clearTokens()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

