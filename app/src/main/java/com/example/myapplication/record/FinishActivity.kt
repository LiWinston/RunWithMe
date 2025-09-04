package com.example.myapplication.record

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            // 构造 Workout 数据对象
            val workout = Workout(
                distance = distance,
                duration = duration,
                speed = speed,
                calories = calories,
                userId = 1L // TODO: 这里改成你实际登录用户的 ID
            )

            // 调用 Retrofit 保存
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.api.saveWorkout(workout)
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@FinishActivity, "保存成功！", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@FinishActivity, "保存失败: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@FinishActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

// 前端用来传输数据的实体
data class Workout(
    val distance: String,
    val duration: String,
    val speed: String,
    val calories: String,
    val userId: Long
)
