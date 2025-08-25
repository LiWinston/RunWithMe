package com.example.myapplication.landr.registerapp

import android.content.Intent
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.util.Log
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.loginapp.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.landr.registerapp.models.RegisterRequest
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
    private lateinit var genderRg: RadioGroup
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
        setupClickListeners()
    }

    private fun initViews() {
        usernameEt = findViewById(R.id.usernameEt)
        passwordEt = findViewById(R.id.passwordEt)
        emailEt = findViewById(R.id.emailEt)
        firstnameEt = findViewById(R.id.firstnameEt)
        lastnameEt = findViewById(R.id.lastnameEt)
        genderRg = findViewById(R.id.genderRg)
        ageEt = findViewById(R.id.ageEt)
        phoneEt = findViewById(R.id.phoneEt)
        heightEt = findViewById(R.id.heightEt)
        weightEt = findViewById(R.id.weightEt)
        fitnessGoalEt = findViewById(R.id.fitnessGoalEt)
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
        val fitnessGoal = fitnessGoalEt.text.toString().trim()

        // 验证必填字段
        if (username.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show()
            return
        }

        if (firstName.isEmpty()) {
            Toast.makeText(this, "请输入名字", Toast.LENGTH_SHORT).show()
            return
        }

        if (lastName.isEmpty()) {
            Toast.makeText(this, "请输入姓氏", Toast.LENGTH_SHORT).show()
            return
        }

        // 获取性别
        val selectedGenderId = genderRg.checkedRadioButtonId
        var gender: String? = null
        if (selectedGenderId != -1) {
            val selectedGenderRb = findViewById<RadioButton>(selectedGenderId)
            gender = when (selectedGenderRb.text.toString()) {
                "Male" -> "MALE"
                "Female" -> "FEMALE"
                "Other" -> "OTHER"
                else -> "OTHER"
            }
        }

        // 转换数值类型
        val age = if (ageText.isNotEmpty()) ageText.toIntOrNull() else null
        val height = if (heightText.isNotEmpty()) heightText.toDoubleOrNull() else null
        val weight = if (weightText.isNotEmpty()) weightText.toDoubleOrNull() else null

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
            fitnessGoal = if (fitnessGoal.isNotEmpty()) fitnessGoal else null,
            fitnessLevel = fitnessLevel,
            weeklyAvailability = null // 暂时不包含此字段
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.register(request)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        if (registerResponse?.status == 1) {
                            Toast.makeText(this@RegisterActivity, "注册成功！请登录", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                registerResponse?.message ?: "注册失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "网络错误: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}