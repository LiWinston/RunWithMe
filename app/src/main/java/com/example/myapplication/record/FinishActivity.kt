package com.example.myapplication.record

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class FinishActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)

        val tvDistance = findViewById<TextView>(R.id.tvDistance)
        val tvDuration = findViewById<TextView>(R.id.tvDuration)
        val tvCalories = findViewById<TextView>(R.id.tvCalories)
        val tvSpeed = findViewById<TextView>(R.id.tvPace)  // 用 speed 填 pace 的格子
        val btnDone = findViewById<Button>(R.id.btnDone)

        // 取出传递过来的数据
        val distance = intent.getStringExtra("distance") ?: "0.00 miles"
        val duration = intent.getStringExtra("duration") ?: "00:00:00"
        val calories = intent.getStringExtra("calories") ?: "0 kcal"
        val speed = intent.getStringExtra("speed") ?: "0.00 mph"

        tvDistance.text = distance
        tvDuration.text = duration
        tvCalories.text = calories
        tvSpeed.text = speed  // 直接显示速度

        btnDone.setOnClickListener {
            finish() // 结束页面，回到主界面
        }
    }
}
