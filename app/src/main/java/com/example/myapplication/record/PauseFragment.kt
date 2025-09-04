package com.example.myapplication.record

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.record.FinishActivity
import com.example.myapplication.R

class PauseFragment : Fragment() {

    private val workoutViewModel: WorkoutViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pause, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvSpeed = view.findViewById<TextView>(R.id.tvSpeed)
        val tvDistance = view.findViewById<TextView>(R.id.tvDistance)
        val tvCalories = view.findViewById<TextView>(R.id.tvCalories)
        val btnResume = view.findViewById<Button>(R.id.btnResume)
        val btnStop = view.findViewById<Button>(R.id.btnStop)

        workoutViewModel.time.observe(viewLifecycleOwner) { tvTime.text = it }
        workoutViewModel.speed.observe(viewLifecycleOwner) { tvSpeed.text = it }
        workoutViewModel.distance.observe(viewLifecycleOwner) { tvDistance.text = it }
        workoutViewModel.calories.observe(viewLifecycleOwner) { tvCalories.text = it }

        btnResume.setOnClickListener {
            workoutViewModel.resumeWorkout()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RecordingFragment())
                .commit()
        }

        btnStop.setOnClickListener {
            workoutViewModel.pauseWorkout() // 停止计时

            val intent = Intent(requireContext(), FinishActivity::class.java).apply {
                putExtra("distance", workoutViewModel.distance.value ?: "0.00 miles")
                putExtra("duration", workoutViewModel.time.value ?: "00:00:00")
                putExtra("calories", workoutViewModel.calories.value ?: "0 kcal")
                putExtra("speed", workoutViewModel.speed.value ?: "0.00 mph")
            }

            startActivity(intent)
            activity?.finish() // 关闭 RecordingActivity
        }

    }
}
