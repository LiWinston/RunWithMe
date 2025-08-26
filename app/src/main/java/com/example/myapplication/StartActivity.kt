package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.myapplication.landr.loginapp.LoginActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start) // 绑定 XML

        // 找到按钮
        val startButton: Button = findViewById(R.id.button)

        // 点击事件：跳转到 MainActivity
        startButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // 可选：关闭 StartActivity，不让用户回退回来
        }
    }
}