package com.example.myapplication.record

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class StartRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_record)

        val btnRecord = findViewById<Button>(R.id.btn_record)

        btnRecord.setOnClickListener {
            // 点击录制按钮 → 跳转到倒计时界面
            val intent = Intent(this, PreRecordActivity::class.java)
            startActivity(intent)
        }
    }
}