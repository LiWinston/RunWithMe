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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
    
    // 图表
    private lateinit var monthLineChart: LineChart
    
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

    override fun onResume() {
        super.onResume()
        // Refresh data when user returns to this fragment
        Log.d("HistoryMonthFragment", "onResume - refreshing month data")
        loadMonthData()
    }

    private fun initViews(view: View) {
        try {
            tvMonthDistance = view.findViewById(R.id.tvMonthDistance) ?: throw NullPointerException("tvMonthDistance not found")
            tvMonthDuration = view.findViewById(R.id.tvMonthDuration) ?: throw NullPointerException("tvMonthDuration not found")
            tvMonthPace = view.findViewById(R.id.tvMonthPace) ?: throw NullPointerException("tvMonthPace not found")
            tvMonthCalories = view.findViewById(R.id.tvMonthCalories) ?: throw NullPointerException("tvMonthCalories not found")
            tvMonthSteps = view.findViewById(R.id.tvMonthSteps) ?: throw NullPointerException("tvMonthSteps not found")
            rvMonthWorkouts = view.findViewById(R.id.rvMonthWorkouts) ?: throw NullPointerException("rvMonthWorkouts not found")
            
            // 图表
            monthLineChart = view.findViewById(R.id.monthLineChart) ?: throw NullPointerException("monthLineChart not found")
            setupLineChart()
            
            // AI建议相关
            btnGenerateMonthAdvice = view.findViewById(R.id.btnGenerateMonthAdvice) ?: throw NullPointerException("btnGenerateMonthAdvice not found")
            tvMonthAdvice = view.findViewById(R.id.tvMonthAdvice) ?: throw NullPointerException("tvMonthAdvice not found")
            pbAdviceLoading = view.findViewById(R.id.pbAdviceLoading) ?: throw NullPointerException("pbAdviceLoading not found")
            
            // 设置按钮点击事件
            btnGenerateMonthAdvice.setOnClickListener {
                historyViewModel.generateMonthAdvice()
            }
        } catch (e: NullPointerException) {
            Log.e("HistoryMonthFragment", "Failed to initialize views: ${e.message}", e)
            throw e
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
            updateMonthLineChart(chartData)
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
            val userId = TokenManager.getInstance(requireContext()).getUserId()
            Log.d("HistoryMonthFragment", "Loading month data for user: $userId")
            
            if (userId > 0) {
                // Load user profile first
                historyViewModel.loadUserProfile()
                // Then load month data
                historyViewModel.loadMonthData(userId)
            } else {
                Log.e("HistoryMonthFragment", "Invalid user ID: $userId")
            }
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
    
    private fun setupLineChart() {
        monthLineChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setPinchZoom(false)
            setScaleEnabled(false)
            isDragEnabled = true
            
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
            animateX(800)
        }
    }
    
    private fun updateMonthLineChart(chartData: Map<String, Any>?) {
        if (chartData == null) {
            monthLineChart.clear()
            return
        }
        
        try {
            // 从chartData中提取每日数据
            @Suppress("UNCHECKED_CAST")
            val dailyData = chartData["daily"] as? List<Map<String, Any>> ?: emptyList()
            
            val entries = mutableListOf<Entry>()
            val labels = mutableListOf<String>()
            
            // 遍历每日数据
            dailyData.forEachIndexed { index, dayData ->
                val day = (dayData["day"] as? Number)?.toInt() ?: (index + 1)
                val distance = (dayData["distance"] as? Number)?.toFloat() ?: 0f
                
                labels.add(day.toString())
                entries.add(Entry(index.toFloat(), distance))
            }
            
            // 如果没有数据，显示空图表
            if (entries.isEmpty()) {
                monthLineChart.clear()
                return
            }
            
            // 创建数据集
            val dataSet = LineDataSet(entries, "Distance (km)").apply {
                color = Color.parseColor("#2196F3")
                lineWidth = 2.5f
                circleRadius = 4f
                setCircleColor(Color.parseColor("#2196F3"))
                circleHoleRadius = 2f
                circleHoleColor = Color.WHITE
                valueTextColor = Color.BLACK
                valueTextSize = 11f  // 增大字体从9f到11f
                setDrawValues(true)
                mode = LineDataSet.Mode.CUBIC_BEZIER // 平滑曲线
                cubicIntensity = 0.2f
                setDrawFilled(true)
                fillColor = Color.parseColor("#2196F3")
                fillAlpha = 50
            }
            
            // 设置数据
            val lineData = LineData(dataSet)
            monthLineChart.data = lineData
            monthLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            monthLineChart.xAxis.labelCount = minOf(7, labels.size) // 最多显示7个标签
            monthLineChart.invalidate()
            
        } catch (e: Exception) {
            Log.e("HistoryMonthFragment", "Error updating line chart", e)
            monthLineChart.clear()
        }
    }
}
