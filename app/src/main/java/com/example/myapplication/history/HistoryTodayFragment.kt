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
 * 今日历史记录Fragment
 * 显示今日统计数据和运动记录列表
 */
class HistoryTodayFragment : Fragment() {

    private val historyViewModel: HistoryViewModel by activityViewModels()
    private lateinit var workoutAdapter: WorkoutRecordAdapter

    // 统计数据UI组件
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var tvTotalPace: TextView
    private lateinit var tvTotalCalories: TextView

    // 运动记录列表
    private lateinit var rvWorkoutRecords: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_today, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        observeData()
        loadTodayData()
    }

    private fun initViews(view: View) {
        // 统计卡片
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance)
        tvTotalDuration = view.findViewById(R.id.tvTotalDuration)
        tvTotalPace = view.findViewById(R.id.tvTotalPace)
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories)

        // 运动记录列表
        rvWorkoutRecords = view.findViewById(R.id.rvWorkoutRecords)
    }

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutRecordAdapter { workout ->
            // 点击记录项，展开详情或跳转到详情页
            showWorkoutDetail(workout)
        }
        
        rvWorkoutRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
        }
    }

    private fun observeData() {
        // 观察统计数据
        historyViewModel.todayStats.observe(viewLifecycleOwner) { stats ->
            updateStatsUI(stats)
        }

        // 观察运动记录列表
        historyViewModel.todayWorkouts.observe(viewLifecycleOwner) { workouts ->
            workoutAdapter.submitList(workouts)
        }

        // 观察加载状态
        historyViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: 显示/隐藏加载指示器
        }
    }

    private fun loadTodayData() {
        lifecycleScope.launch {
            historyViewModel.loadTodayData(1L) // TODO: 使用真实用户ID
        }
    }

    private fun updateStatsUI(stats: Map<String, Any>) {
        val distance = stats["totalDistance"] as? Double ?: 0.0
        val duration = stats["totalDuration"] as? Int ?: 0 // 秒
        val calories = stats["totalCalories"] as? Double ?: 0.0
        val workoutCount = stats["workoutCount"] as? Int ?: 0

        // 更新距离
        tvTotalDistance.text = String.format("%.2f km", distance)

        // 更新时长
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
        tvTotalDuration.text = timeText

        // 更新平均配速（如果有距离和时间）
        val avgPace = if (distance > 0 && duration > 0) {
            val paceSeconds = (duration / distance).toInt()
            val paceMinutes = paceSeconds / 60
            val paceSecondsRemainder = paceSeconds % 60
            "${paceMinutes}'${String.format("%02d", paceSecondsRemainder)}\""
        } else {
            "0'00\""
        }
        tvTotalPace.text = "$avgPace /km"

        // 更新卡路里
        tvTotalCalories.text = "${calories.toInt()} kcal"
    }

    private fun showWorkoutDetail(workout: Workout) {
        // TODO: 实现运动详情展示
        // 可以弹出底部弹窗显示彩虹图和详细数据
    }
}
