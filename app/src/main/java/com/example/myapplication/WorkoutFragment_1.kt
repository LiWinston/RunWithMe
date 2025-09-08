package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.FrameLayout
import android.widget.ImageView

class WorkoutFragment_1 : Fragment(R.layout.fragment_workout_1) {

    private var hasNavigated = false // 防止重复点击

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button = view.findViewById<ImageView>(R.id.start_button)
        button.setOnClickListener {
            if (!hasNavigated) {
                hasNavigated = true

                // 获取 MainActivity 的 FrameLayout 容器
                val containerId = (requireActivity() as MainActivity)
                    .findViewById<FrameLayout>(R.id.main)?.id

                // 使用安全调用，防止容器不存在时报错
                containerId?.let { id ->
                    parentFragmentManager.beginTransaction()
                        .replace(id, WorkoutFragment_2())
                        .addToBackStack(null) // 可选：允许返回 WorkoutFragment_1
                        .commit()
                }
            }
        }
    }
}