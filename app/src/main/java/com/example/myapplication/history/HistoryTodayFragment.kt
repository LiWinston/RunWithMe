package com.example.myapplication.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.landr.TokenManager
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
    private lateinit var tvTotalSteps: TextView

    // 运动记录列表
    private lateinit var rvWorkoutRecords: RecyclerView
    
    // AI建议相关UI
    private lateinit var btnGenerateTodayAdvice: Button
    private lateinit var tvTodayAdvice: TextView
    private lateinit var pbAdviceLoading: ProgressBar

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

    override fun onResume() {
        super.onResume()
        // Refresh data when user returns to this fragment
        Log.d("HistoryTodayFragment", "onResume - refreshing today data")
        loadTodayData()
    }

    private fun initViews(view: View) {
        try {
            // 统计卡片
            tvTotalDistance = view.findViewById(R.id.tvTotalDistance) ?: throw NullPointerException("tvTotalDistance not found")
            tvTotalDuration = view.findViewById(R.id.tvTotalDuration) ?: throw NullPointerException("tvTotalDuration not found")
            tvTotalPace = view.findViewById(R.id.tvTotalPace) ?: throw NullPointerException("tvTotalPace not found")
            tvTotalCalories = view.findViewById(R.id.tvTotalCalories) ?: throw NullPointerException("tvTotalCalories not found")
            tvTotalSteps = view.findViewById(R.id.tvTotalSteps) ?: throw NullPointerException("tvTotalSteps not found")

            // 运动记录列表
            rvWorkoutRecords = view.findViewById(R.id.rvWorkoutRecords) ?: throw NullPointerException("rvWorkoutRecords not found")
            
            // AI建议相关
            btnGenerateTodayAdvice = view.findViewById(R.id.btnGenerateTodayAdvice) ?: throw NullPointerException("btnGenerateTodayAdvice not found")
            tvTodayAdvice = view.findViewById(R.id.tvTodayAdvice) ?: throw NullPointerException("tvTodayAdvice not found")
            pbAdviceLoading = view.findViewById(R.id.pbAdviceLoading) ?: throw NullPointerException("pbAdviceLoading not found")
            
            // 设置按钮点击事件
            btnGenerateTodayAdvice.setOnClickListener {
                historyViewModel.generateTodayAdvice()
            }
        } catch (e: NullPointerException) {
            Log.e("HistoryTodayFragment", "Failed to initialize views: ${e.message}", e)
            throw e
        }
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
            android.util.Log.d("HistoryTodayFragment", "接收到${workouts.size}条运动记录")
            workouts.forEach { workout ->
                android.util.Log.d("HistoryTodayFragment", "Workout ID: ${workout.id}, 类型: ${workout.workoutType}, 开始时间: ${workout.startTime}")
            }
            workoutAdapter.submitList(workouts)
        }

        // 观察加载状态
        historyViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: 显示/隐藏加载指示器
        }
        
        // 观察AI建议
        historyViewModel.todayAdvice.observe(viewLifecycleOwner) { advice ->
            advice?.let {
                tvTodayAdvice.text = it
            }
        }
        
        // 观察AI加载状态
        historyViewModel.isLoadingAdvice.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                pbAdviceLoading.visibility = View.VISIBLE
                tvTodayAdvice.visibility = View.GONE
                btnGenerateTodayAdvice.isEnabled = false
            } else {
                pbAdviceLoading.visibility = View.GONE
                tvTodayAdvice.visibility = View.VISIBLE
                btnGenerateTodayAdvice.isEnabled = true
            }
        }
    }

    private fun loadTodayData() {
        lifecycleScope.launch {
            val userId = TokenManager.getInstance(requireContext()).getUserId()
            Log.d("HistoryTodayFragment", "Loading today data for user: $userId")
            
            if (userId > 0) {
                // Load user profile first
                historyViewModel.loadUserProfile()
                // Then load today data
                historyViewModel.loadTodayData(userId)
            } else {
                Log.e("HistoryTodayFragment", "Invalid user ID: $userId")
            }
        }
    }

    private fun updateStatsUI(stats: Map<String, Any>) {
        val distance = stats["totalDistance"] as? Double ?: 0.0
        val duration = (stats["totalDuration"] as? Number)?.toInt() ?: 0 // 秒，兼容Int和Double
        val calories = stats["totalCalories"] as? Double ?: 0.0
        val steps = (stats["totalSteps"] as? Number)?.toInt() ?: 0
        val workoutCount = (stats["workoutCount"] as? Number)?.toInt() ?: 0
        
        // 调试信息
        android.util.Log.d("HistoryTodayFragment", "Stats received: $stats")
        android.util.Log.d("HistoryTodayFragment", "Distance: $distance, Duration: $duration, Calories: $calories, WorkoutCount: $workoutCount")

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

        // 更新步数
        tvTotalSteps.text = steps.toString()
    }

    private fun showWorkoutDetail(workout: Workout) {
        // TODO: 实现运动详情展示
        // 可以弹出底部弹窗显示彩虹图和详细数据
    }
}
