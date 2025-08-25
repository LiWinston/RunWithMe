package com.example.myapplication.landr.loginapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.TokenManager
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.landr.loginapp.models.LoginRequest
import com.example.myapplication.landr.registerapp.RegisterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化 RetrofitClient 和 TokenManager
        RetrofitClient.init(this)
        tokenManager = TokenManager.getInstance(this)
        
        // 检查是否已登录
        if (tokenManager.isLoggedIn()) {
            navigateToMain()
            return
        }
        
        setContentView(R.layout.activity_login)

        usernameEt = findViewById(R.id.usernameEt)
        passwordEt = findViewById(R.id.passwordEt)
        loginBtn = findViewById(R.id.loginBtn)
        registerBtn = findViewById(R.id.registerBtn)

        loginBtn.setOnClickListener {
            val username = usernameEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.api.login(
                        LoginRequest(username, password)
                    )
                    
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse?.status == 1 && loginResponse.data != null) {
                                // 保存 tokens 和用户信息
                                tokenManager.saveTokens(
                                    loginResponse.data.accessToken,
                                    loginResponse.data.refreshToken
                                )
                                tokenManager.saveUserInfo(
                                    loginResponse.data.user.id,
                                    loginResponse.data.user.username
                                )
                                
                                Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                                navigateToMain()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    loginResponse?.message ?: "登录失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "网络错误: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        registerBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}