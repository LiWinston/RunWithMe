package com.example.myapplication.record

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.record.RecordingActivity

class PreRecordActivity : AppCompatActivity() {
    private var tvCountdown: TextView? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_record)

        tvCountdown = findViewById<TextView?>(R.id.tv_countdown)

        // 倒计时 3 -> 1
        countDownTimer = object : CountDownTimer(3000, 1000) {
            var count: Int = 3

            override fun onTick(millisUntilFinished: Long) {
                tvCountdown!!.setText(count.toString())
                count--
            }

            override fun onFinish() {
                val intent = Intent(this@PreRecordActivity, RecordingActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
    }
}