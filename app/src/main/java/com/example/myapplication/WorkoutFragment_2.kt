package com.example.myapplication

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.FrameLayout

class WorkoutFragment_2 : Fragment(R.layout.fragment_workout_2) {

    private lateinit var countdownText: TextView
    private var hasNavigated = false // 避免重复跳转

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countdownText = view.findViewById(R.id.countdown_text)

        // 3 秒倒计时，每秒更新一次
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000) + 1
                countdownText.text = secondsLeft.toString()
            }

            override fun onFinish() {
                if (!hasNavigated) {
                    hasNavigated = true
                    // 获取 MainActivity 的 FrameLayout 容器
                    val container = (requireActivity() as MainActivity)
                        .findViewById<FrameLayout>(R.id.main)
                    container?.let {
                        parentFragmentManager.beginTransaction()
                            .replace(it.id, WorkoutFragment_3())
                            .addToBackStack(null) // 可选：允许返回 WorkoutFragment_2
                            .commit()
                    }
                }
            }
        }.start()
    }
}