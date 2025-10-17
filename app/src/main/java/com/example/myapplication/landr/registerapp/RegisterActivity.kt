package com.example.myapplication.landr.registerapp

import android.content.Intent
import android.widget.LinearLayout
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.util.Log
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.loginapp.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.landr.registerapp.models.RegisterRequest
import com.example.myapplication.landr.registerapp.models.FitnessGoal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var firstnameEt: EditText
    private lateinit var lastnameEt: EditText
    private lateinit var genderDropdown: AutoCompleteTextView
    private lateinit var ageEt: EditText
    private lateinit var phoneEt: EditText
    private lateinit var heightEt: EditText
    private lateinit var weightEt: EditText
    private lateinit var fitnessGoalEt: EditText
    private lateinit var fitnessLevelSpinner: Spinner
    private lateinit var registerBtn: Button
    private lateinit var backToLoginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 初始化 RetrofitClient
        RetrofitClient.init(this)

        initViews()
        setupFitnessLevelSpinner()
        setupGenderSelection()
        setupClickListeners()
    }

    private lateinit var tilUsername: com.google.android.material.textfield.TextInputLayout
    private lateinit var tilPassword: com.google.android.material.textfield.TextInputLayout
    private lateinit var tilAge: com.google.android.material.textfield.TextInputLayout
    private lateinit var tilHeight: com.google.android.material.textfield.TextInputLayout
    private lateinit var tilWeight: com.google.android.material.textfield.TextInputLayout

    private fun initViews() {
        usernameEt = findViewById(R.id.usernameEt)
        passwordEt = findViewById(R.id.passwordEt)
        emailEt = findViewById(R.id.emailEt)
        firstnameEt = findViewById(R.id.firstnameEt)
        lastnameEt = findViewById(R.id.lastnameEt)
        genderDropdown = findViewById(R.id.genderDropdown)
        ageEt = findViewById(R.id.ageEt)
        phoneEt = findViewById(R.id.phoneEt)
        heightEt = findViewById(R.id.heightEt)
        weightEt = findViewById(R.id.weightEt)
        fitnessGoalEt = findViewById(R.id.fitnessGoalEt)
        fitnessLevelSpinner = findViewById(R.id.fitnessLevelSpinner)
        registerBtn = findViewById(R.id.registerBtn)
        backToLoginBtn = findViewById(R.id.backToLoginBtn)
        
        // Get TextInputLayout references for error display
        tilUsername = findViewById(R.id.til_username)
        tilPassword = findViewById(R.id.til_password)
        tilAge = findViewById(R.id.til_age)
        tilHeight = findViewById(R.id.til_height)
        tilWeight = findViewById(R.id.til_weight)
    }

    private fun setupFitnessLevelSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.fitness_level_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fitnessLevelSpinner.adapter = adapter
    }

    private fun setupGenderSelection() {
        val items = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        genderDropdown.setAdapter(adapter)

        // 确保下拉菜单可以点击
        genderDropdown.setOnClickListener {
            genderDropdown.showDropDown()
        }

        // 处理选择事件
        genderDropdown.setOnItemClickListener { _, _, position, _ ->
            // 选择后自动填入文本
            genderDropdown.clearFocus()
        }
    }

    private fun setupClickListeners() {
        registerBtn.setOnClickListener {
            performRegister()
        }

        backToLoginBtn.setOnClickListener {
            finish()
        }
    }

    private fun performRegister() {
        // Clear previous errors
        tilUsername.error = null
        tilPassword.error = null
        tilAge.error = null
        tilHeight.error = null
        tilWeight.error = null
        
        val username = usernameEt.text.toString().trim()
        val password = passwordEt.text.toString().trim()
        val email = emailEt.text.toString().trim()
        val firstName = firstnameEt.text.toString().trim()
        val lastName = lastnameEt.text.toString().trim()
        val ageText = ageEt.text.toString().trim()
        val phoneNumber = phoneEt.text.toString().trim()
        val heightText = heightEt.text.toString().trim()
        val weightText = weightEt.text.toString().trim()
        val fitnessGoalText = fitnessGoalEt.text.toString().trim()

        // Validate username (3-30 characters)
        if (username.isEmpty()) {
            tilUsername.error = "Username cannot be empty"
            usernameEt.requestFocus()
            return
        }
        if (username.length < 3) {
            tilUsername.error = "Username must be at least 3 characters"
            usernameEt.requestFocus()
            return
        }
        if (username.length > 30) {
            tilUsername.error = "Username cannot exceed 30 characters"
            usernameEt.requestFocus()
            return
        }

        // Validate password (6-100 characters)
        if (password.isEmpty()) {
            tilPassword.error = "Password cannot be empty"
            passwordEt.requestFocus()
            return
        }
        if (password.length < 6) {
            tilPassword.error = "Password must be at least 6 characters"
            passwordEt.requestFocus()
            return
        }

        // Validate first name
        if (firstName.isEmpty()) {
            Toast.makeText(this, "First name cannot be empty", Toast.LENGTH_SHORT).show()
            firstnameEt.requestFocus()
            return
        }

        // Validate last name
        if (lastName.isEmpty()) {
            Toast.makeText(this, "Last name cannot be empty", Toast.LENGTH_SHORT).show()
            lastnameEt.requestFocus()
            return
        }

        val gender = when(genderDropdown.text.toString().trim().lowercase()) {
            "male" -> "MALE"
            "female" -> "FEMALE"
            "other" -> "OTHER"
            else -> null
        }

        // Validate and convert age (13-120)
        val age = if (ageText.isNotEmpty()) {
            val ageValue = ageText.toIntOrNull()
            if (ageValue == null) {
                tilAge.error = "Please enter a valid age"
                ageEt.requestFocus()
                return
            }
            if (ageValue < 13) {
                tilAge.error = "Age must be at least 13"
                ageEt.requestFocus()
                return
            }
            if (ageValue > 120) {
                tilAge.error = "Age cannot exceed 120"
                ageEt.requestFocus()
                return
            }
            ageValue
        } else null

        // Validate and convert height (50-300 cm)
        val height = if (heightText.isNotEmpty()) {
            val heightValue = heightText.toDoubleOrNull()
            if (heightValue == null) {
                tilHeight.error = "Please enter a valid height"
                heightEt.requestFocus()
                return
            }
            if (heightValue < 50.0) {
                tilHeight.error = "Height must be at least 50 cm"
                heightEt.requestFocus()
                return
            }
            if (heightValue > 300.0) {
                tilHeight.error = "Height cannot exceed 300 cm"
                heightEt.requestFocus()
                return
            }
            heightValue
        } else null

        // Validate and convert weight (20-500 kg)
        val weight = if (weightText.isNotEmpty()) {
            val weightValue = weightText.toDoubleOrNull()
            if (weightValue == null) {
                tilWeight.error = "Please enter a valid weight"
                weightEt.requestFocus()
                return
            }
            if (weightValue < 20.0) {
                tilWeight.error = "Weight must be at least 20 kg"
                weightEt.requestFocus()
                return
            }
            if (weightValue > 500.0) {
                tilWeight.error = "Weight cannot exceed 500 kg"
                weightEt.requestFocus()
                return
            }
            weightValue
        } else null

        // 获取健身水平
        val fitnessLevel = if (fitnessLevelSpinner.selectedItemPosition > 0) {
            fitnessLevelSpinner.selectedItem.toString().uppercase()
        } else null

        val request = RegisterRequest(
            username = username,
            password = password,
            email = if (email.isNotEmpty()) email else null,
            firstName = firstName,
            lastName = lastName,
            gender = gender,
            age = age,
            phoneNumber = if (phoneNumber.isNotEmpty()) phoneNumber else null,
            height = height,
            weight = weight,
            // Interpret the single field as weeklyDistanceKm for now
            fitnessGoal = fitnessGoalText.toDoubleOrNull()?.let { FitnessGoal(weeklyDistanceKm = it) },
            fitnessLevel = fitnessLevel,
            weeklyAvailability = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.register(request)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        if (registerResponse?.status == 1) {
                            Toast.makeText(this@RegisterActivity, "Register Successfully! Please Log in.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                registerResponse?.message ?: "Register Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Network Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}