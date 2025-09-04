package com.example.myapplication.record

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R
import com.example.myapplication.record.WorkoutViewModel

class RecordingFragment : Fragment() {

    private val workoutViewModel: WorkoutViewModel by activityViewModels()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recording, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvSpeed = view.findViewById<TextView>(R.id.tvSpeed)
        val tvDistance = view.findViewById<TextView>(R.id.tvDistance)
        val tvCalories = view.findViewById<TextView>(R.id.tvCalories)
        val btnPause = view.findViewById<Button>(R.id.btnPause)

        // 绑定 LiveData
        workoutViewModel.time.observe(viewLifecycleOwner) { tvTime.text = it }
        workoutViewModel.speed.observe(viewLifecycleOwner) { tvSpeed.text = it }
        workoutViewModel.distance.observe(viewLifecycleOwner) { tvDistance.text = it }
        workoutViewModel.calories.observe(viewLifecycleOwner) { tvCalories.text = it }

        // 启动计时器（1s 对应 1s）
        startTimer()

        // 启动定位
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            workoutViewModel.startLocationTracking()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }

        // 暂停按钮：进入 PauseFragment
        btnPause.setOnClickListener {
            workoutViewModel.pauseWorkout()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, PauseFragment())
                .commit()
        }
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                workoutViewModel.tick() // 这里 tick() 用时间戳计算，保证 1s 对应 1s
                handler.postDelayed(this, 1000)
            }
        })
    }
}