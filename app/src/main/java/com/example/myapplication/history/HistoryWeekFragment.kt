package com.example.myapplication.history

import android.graphics.Color
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
    
    // 图表
    private lateinit var weekBarChart: BarChart
    
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

    override fun onResume() {
        super.onResume()
        // Refresh data when user returns to this fragment
        Log.d("HistoryWeekFragment", "onResume - refreshing week data")
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
            
            // 图表
            weekBarChart = view.findViewById(R.id.weekBarChart) ?: throw NullPointerException("weekBarChart not found")
            setupBarChart()
            
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
            updateWeekBarChart(chartData)
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
    
    private fun setupBarChart() {
        weekBarChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            
            // X轴设置
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.GRAY
                textSize = 10f
            }
            
            // 左Y轴设置
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textColor = Color.GRAY
                textSize = 10f
                axisMinimum = 0f
            }
            
            // 右Y轴禁用
            axisRight.isEnabled = false
            
            // 图例设置
            legend.isEnabled = false
            
            // 动画
            animateY(800)
        }
    }
    
    private fun updateWeekBarChart(chartData: Map<String, Any>?) {
        if (chartData == null) {
            weekBarChart.clear()
            return
        }
        
        try {
            // 从chartData中提取每日数据
            @Suppress("UNCHECKED_CAST")
            val dailyData = chartData["daily"] as? List<Map<String, Any>> ?: emptyList()
            
            val entries = mutableListOf<BarEntry>()
            val labels = mutableListOf<String>()
            
            // 准备数据 - 一周7天
            val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            
            for (i in 0..6) {
                labels.add(daysOfWeek[i])
                
                // 查找当天的数据
                val dayData = dailyData.getOrNull(i)
                val distance = (dayData?.get("distance") as? Number)?.toFloat() ?: 0f
                
                entries.add(BarEntry(i.toFloat(), distance))
            }
            
            // 创建数据集
            val dataSet = BarDataSet(entries, "Distance (km)").apply {
                color = Color.parseColor("#4CAF50")
                valueTextColor = Color.BLACK
                valueTextSize = 9f
                setDrawValues(true)
            }
            
            // 设置数据
            val barData = BarData(dataSet)
            barData.barWidth = 0.6f
            
            weekBarChart.data = barData
            weekBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            weekBarChart.xAxis.labelCount = labels.size
            weekBarChart.invalidate()
            
        } catch (e: Exception) {
            Log.e("HistoryWeekFragment", "Error updating bar chart", e)
            weekBarChart.clear()
        }
    }
}
