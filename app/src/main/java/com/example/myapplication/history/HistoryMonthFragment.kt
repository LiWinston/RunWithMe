package com.example.myapplication.history

import android.os.Bundle
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
import com.example.myapplication.record.Workout
import kotlinx.coroutines.launch

/**
 * 本月历史记录Fragment
 * 显示本月统计数据、趋势图和运动记录列表
 */
class HistoryMonthFragment : Fragment() {

    private val historyViewModel: HistoryViewModel by activityViewModels()
    private lateinit var workoutAdapter: WorkoutRecordAdapter

    // 统计数据UI组件
    private lateinit var tvMonthDistance: TextView
    private lateinit var tvMonthDuration: TextView
    private lateinit var tvMonthPace: TextView
    private lateinit var tvMonthCalories: TextView
    private lateinit var tvMonthSteps: TextView

    // 运动记录列表
    private lateinit var rvMonthWorkouts: RecyclerView
    
    // AI建议相关UI
    private lateinit var btnGenerateMonthAdvice: Button
    private lateinit var tvMonthAdvice: TextView
    private lateinit var pbAdviceLoading: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_month, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        observeData()
        loadMonthData()
    }

    private fun initViews(view: View) {
        tvMonthDistance = view.findViewById(R.id.tvMonthDistance)
        tvMonthDuration = view.findViewById(R.id.tvMonthDuration)
        tvMonthPace = view.findViewById(R.id.tvMonthPace)
        tvMonthCalories = view.findViewById(R.id.tvMonthCalories)
        tvMonthSteps = view.findViewById(R.id.tvMonthSteps)
        rvMonthWorkouts = view.findViewById(R.id.rvMonthWorkouts)
        
        // AI建议相关
        btnGenerateMonthAdvice = view.findViewById(R.id.btnGenerateMonthAdvice)
        tvMonthAdvice = view.findViewById(R.id.tvMonthAdvice)
        pbAdviceLoading = view.findViewById(R.id.pbAdviceLoading)
        
        // 设置按钮点击事件
        btnGenerateMonthAdvice.setOnClickListener {
            historyViewModel.generateMonthAdvice()
        }
    }

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutRecordAdapter { workout ->
            // TODO: 显示运动详情
        }
        
        rvMonthWorkouts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
        }
    }

    private fun observeData() {
        historyViewModel.monthStats.observe(viewLifecycleOwner) { stats ->
            updateStatsUI(stats)
        }

        historyViewModel.monthWorkouts.observe(viewLifecycleOwner) { workouts ->
            workoutAdapter.submitList(workouts)
        }

        historyViewModel.monthChart.observe(viewLifecycleOwner) { chartData ->
            // TODO: 显示月趋势图（折线图）
        }
        
        // 观察AI建议
        historyViewModel.monthAdvice.observe(viewLifecycleOwner) { advice ->
            advice?.let {
                tvMonthAdvice.text = it
            }
        }
        
        // 观察AI加载状态
        historyViewModel.isLoadingAdvice.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                pbAdviceLoading.visibility = View.VISIBLE
                tvMonthAdvice.visibility = View.GONE
                btnGenerateMonthAdvice.isEnabled = false
            } else {
                pbAdviceLoading.visibility = View.GONE
                tvMonthAdvice.visibility = View.VISIBLE
                btnGenerateMonthAdvice.isEnabled = true
            }
        }
    }

    private fun loadMonthData() {
        lifecycleScope.launch {
            // Load user profile first
            historyViewModel.loadUserProfile()
            // Then load month data
            historyViewModel.loadMonthData(1L)
        }
    }

    private fun updateStatsUI(stats: Map<String, Any>) {
        val distance = stats["totalDistance"] as? Double ?: 0.0
        val duration = (stats["totalDuration"] as? Number)?.toInt() ?: 0 // 兼容Int和Double
        val calories = stats["totalCalories"] as? Double ?: 0.0
        val steps = (stats["totalSteps"] as? Number)?.toInt() ?: 0

        tvMonthDistance.text = String.format("%.1f km", distance)

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
        tvMonthDuration.text = timeText

        val avgPace = if (distance > 0 && duration > 0) {
            val paceSeconds = (duration / distance).toInt()
            val paceMinutes = paceSeconds / 60
            val paceSecondsRemainder = paceSeconds % 60
            "${paceMinutes}'${String.format("%02d", paceSecondsRemainder)}\""
        } else {
            "0'00\""
        }
        tvMonthPace.text = "$avgPace /km"

        tvMonthCalories.text = "${calories.toInt()} kcal"

        tvMonthSteps.text = steps.toString()
    }
}
