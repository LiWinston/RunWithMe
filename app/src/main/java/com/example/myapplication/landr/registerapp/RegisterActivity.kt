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
    private lateinit var weeklyDistanceSpinner: Spinner
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
        setupWeeklyDistanceSpinner()
        setupGenderSelection()
        setupClickListeners()
    }

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
        weeklyDistanceSpinner = findViewById(R.id.weeklyDistanceSpinner)
        fitnessLevelSpinner = findViewById(R.id.fitnessLevelSpinner)
        registerBtn = findViewById(R.id.registerBtn)
        backToLoginBtn = findViewById(R.id.backToLoginBtn)
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

    private fun setupWeeklyDistanceSpinner() {
        // Weekly distance options in km
        val distanceOptions = arrayOf(
            "Select Weekly Goal",
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
        weeklyDistanceSpinner.adapter = adapter
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
        val username = usernameEt.text.toString().trim()
        val password = passwordEt.text.toString().trim()
        val email = emailEt.text.toString().trim()
        val firstName = firstnameEt.text.toString().trim()
        val lastName = lastnameEt.text.toString().trim()
        val ageText = ageEt.text.toString().trim()
        val phoneNumber = phoneEt.text.toString().trim()
        val heightText = heightEt.text.toString().trim()
        val weightText = weightEt.text.toString().trim()

        // 验证必填字段
        if (username.isEmpty()) {
            Toast.makeText(this, "Username can not be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password can not be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (firstName.isEmpty()) {
            Toast.makeText(this, "FirstName can not be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (lastName.isEmpty()) {
            Toast.makeText(this, "Lastname can not be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // 验证weekly distance goal必须选择
        if (weeklyDistanceSpinner.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select a weekly distance goal", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = when(genderDropdown.text.toString().trim().lowercase()) {
            "male" -> "MALE"
            "female" -> "FEMALE"
            "other" -> "OTHER"
            else -> null
        }

        // 转换数值类型
        val age = if (ageText.isNotEmpty()) ageText.toIntOrNull() else null
        val height = if (heightText.isNotEmpty()) heightText.toDoubleOrNull() else null
        val weight = if (weightText.isNotEmpty()) weightText.toDoubleOrNull() else null

        // 获取健身水平
        val fitnessLevel = if (fitnessLevelSpinner.selectedItemPosition > 0) {
            fitnessLevelSpinner.selectedItem.toString().uppercase()
        } else null

        // 获取每周距离目标 (从下拉菜单选项中提取数字，例如 "10 km" -> 10.0)
        val weeklyDistanceKm = when (weeklyDistanceSpinner.selectedItemPosition) {
            1 -> 5.0
            2 -> 10.0
            3 -> 15.0
            4 -> 20.0
            5 -> 25.0
            6 -> 30.0
            7 -> 40.0
            8 -> 50.0
            else -> null
        }

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
            fitnessGoal = weeklyDistanceKm?.let { FitnessGoal(weeklyDistanceKm = it) },
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