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
    private lateinit var tvWeekSteps: TextView

    // 运动记录列表
    private lateinit var rvWeekWorkouts: RecyclerView
    
    // AI建议相关UI
    private lateinit var btnGenerateWeekAdvice: Button
    private lateinit var tvWeekAdvice: TextView
    private lateinit var pbAdviceLoading: ProgressBar

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
        try {
            tvWeekDistance = view.findViewById(R.id.tvWeekDistance) ?: throw NullPointerException("tvWeekDistance not found")
            tvWeekDuration = view.findViewById(R.id.tvWeekDuration) ?: throw NullPointerException("tvWeekDuration not found")
            tvWeekPace = view.findViewById(R.id.tvWeekPace) ?: throw NullPointerException("tvWeekPace not found")
            tvWeekCalories = view.findViewById(R.id.tvWeekCalories) ?: throw NullPointerException("tvWeekCalories not found")
            tvWeekSteps = view.findViewById(R.id.tvWeekSteps) ?: throw NullPointerException("tvWeekSteps not found")
            rvWeekWorkouts = view.findViewById(R.id.rvWeekWorkouts) ?: throw NullPointerException("rvWeekWorkouts not found")
            
            // AI建议相关
            btnGenerateWeekAdvice = view.findViewById(R.id.btnGenerateWeekAdvice) ?: throw NullPointerException("btnGenerateWeekAdvice not found")
            tvWeekAdvice = view.findViewById(R.id.tvWeekAdvice) ?: throw NullPointerException("tvWeekAdvice not found")
            pbAdviceLoading = view.findViewById(R.id.pbAdviceLoading) ?: throw NullPointerException("pbAdviceLoading not found")
            
            // 设置按钮点击事件
            btnGenerateWeekAdvice.setOnClickListener {
                historyViewModel.generateWeekAdvice()
            }
        } catch (e: NullPointerException) {
            Log.e("HistoryWeekFragment", "Failed to initialize views: ${e.message}", e)
            throw e
        }
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
        
        // 观察AI建议
        historyViewModel.weekAdvice.observe(viewLifecycleOwner) { advice ->
            advice?.let {
                tvWeekAdvice.text = it
            }
        }
        
        // 观察AI加载状态
        historyViewModel.isLoadingAdvice.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                pbAdviceLoading.visibility = View.VISIBLE
                tvWeekAdvice.visibility = View.GONE
                btnGenerateWeekAdvice.isEnabled = false
            } else {
                pbAdviceLoading.visibility = View.GONE
                tvWeekAdvice.visibility = View.VISIBLE
                btnGenerateWeekAdvice.isEnabled = true
            }
        }
    }

    private fun loadWeekData() {
        lifecycleScope.launch {
            val userId = TokenManager.getInstance(requireContext()).getUserId()
            Log.d("HistoryWeekFragment", "Loading week data for user: $userId")
            
            if (userId > 0) {
                // Load user profile first
                historyViewModel.loadUserProfile()
                // Then load week data
                historyViewModel.loadWeekData(userId)
            } else {
                Log.e("HistoryWeekFragment", "Invalid user ID: $userId")
            }
        }
    }

    private fun updateStatsUI(stats: Map<String, Any>) {
        val distance = stats["totalDistance"] as? Double ?: 0.0
        val duration = (stats["totalDuration"] as? Number)?.toInt() ?: 0 // 兼容Int和Double
        val calories = stats["totalCalories"] as? Double ?: 0.0
        val steps = (stats["totalSteps"] as? Number)?.toInt() ?: 0

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

        tvWeekSteps.text = steps.toString()
    }
}
