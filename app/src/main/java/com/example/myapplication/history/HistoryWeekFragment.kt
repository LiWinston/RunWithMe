package com.example.myapplication.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.record.Workout
import kotlinx.coroutines.launch

/**
 * 本周历史记录Fragment
 * 显示本周统计数据、图表和运动记录列表
 */
class HistoryWeekFragment : Fragment() {

    private val historyViewModel: HistoryViewModel by activityViewModels()
    private lateinit var workoutAdapter: WorkoutRecordAdapter

    // 统计数据UI组件
    private lateinit var tvWeekDistance: TextView
    private lateinit var tvWeekDuration: TextView
    private lateinit var tvWeekPace: TextView
    private lateinit var tvWeekCalories: TextView

    // 运动记录列表
    private lateinit var rvWeekWorkouts: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_week, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        observeData()
        loadWeekData()
    }

    private fun initViews(view: View) {
        tvWeekDistance = view.findViewById(R.id.tvWeekDistance)
        tvWeekDuration = view.findViewById(R.id.tvWeekDuration)
        tvWeekPace = view.findViewById(R.id.tvWeekPace)
        tvWeekCalories = view.findViewById(R.id.tvWeekCalories)
        rvWeekWorkouts = view.findViewById(R.id.rvWeekWorkouts)
    }

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutRecordAdapter { workout ->
            // TODO: 显示运动详情
        }
        
        rvWeekWorkouts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
        }
    }

    private fun observeData() {
        historyViewModel.weekStats.observe(viewLifecycleOwner) { stats ->
            updateStatsUI(stats)
        }

        historyViewModel.weekWorkouts.observe(viewLifecycleOwner) { workouts ->
            workoutAdapter.submitList(workouts)
        }

        historyViewModel.weekChart.observe(viewLifecycleOwner) { chartData ->
            // TODO: 显示周图表（柱状图）
        }
    }

    private fun loadWeekData() {
        lifecycleScope.launch {
            historyViewModel.loadWeekData(1L)
        }
    }

    private fun updateStatsUI(stats: Map<String, Any>) {
        val distance = stats["totalDistance"] as? Double ?: 0.0
        val duration = stats["totalDuration"] as? Int ?: 0
        val calories = stats["totalCalories"] as? Double ?: 0.0

        tvWeekDistance.text = String.format("%.2f km", distance)

        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        val timeText = if (hours > 0) {
            "${hours}hrs ${minutes}mins"
        } else if (minutes > 0) {
            "${minutes}mins"
        } else {
            "${seconds}s"
        }
        tvWeekDuration.text = timeText

        val avgPace = if (distance > 0 && duration > 0) {
            val paceSeconds = (duration / distance).toInt()
            val paceMinutes = paceSeconds / 60
            val paceSecondsRemainder = paceSeconds % 60
            "${paceMinutes}'${String.format("%02d", paceSecondsRemainder)}\""
        } else {
            "0'00\""
        }
        tvWeekPace.text = "$avgPace /km"

        tvWeekCalories.text = "${calories.toInt()} kcal"
    }
}
