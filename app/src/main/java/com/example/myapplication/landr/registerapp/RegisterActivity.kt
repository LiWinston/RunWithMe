package com.example.myapplication.landr.registerapp

import android.content.Intent
import android.widget.RadioGroup
import android.widget.RadioButton
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

    private lateinit var firstnameEt: EditText
    private lateinit var lastnameEt: EditText
    private lateinit var usernameEt: EditText
    private lateinit var genderRg: RadioGroup
    private lateinit var ageEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 初始化 RetrofitClient
        RetrofitClient.init(this)

        firstnameEt = findViewById(R.id.firstnameEt)
        lastnameEt = findViewById(R.id.lastnameEt)
        usernameEt = findViewById(R.id.usernameEt)
        genderRg = findViewById<RadioGroup>(R.id.genderRg)
        ageEt = findViewById(R.id.ageEt)
        passwordEt = findViewById(R.id.passwordEt)
        registerBtn = findViewById(R.id.registerBtn)

        registerBtn.setOnClickListener {
            val firstName = firstnameEt.text.toString().trim()
            val lastName = lastnameEt.text.toString().trim()
            val username = usernameEt.text.toString().trim()
            val selectedGenderId = genderRg.checkedRadioButtonId
            val ageText = ageEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            // 验证输入
            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请填写所有必填字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var gender: String? = null
            if (selectedGenderId != -1) {
                val selectedGenderRb = findViewById<RadioButton>(selectedGenderId)
                gender = when (selectedGenderRb.text.toString()) {
                    "Male" -> "MALE"
                    "Female" -> "FEMALE"
                    else -> "OTHER"
                }
                Log.d("Register", "Selected gender: $gender")
            } else {
                Toast.makeText(this, "请选择性别", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = if (ageText.isNotEmpty()) ageText.toIntOrNull() else null

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.api.register(
                        RegisterRequest(
                            username = username,
                            password = password,
                            firstName = firstName,
                            lastName = lastName,
                            gender = gender,
                            age = age
                        )
                    )
                    
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
}