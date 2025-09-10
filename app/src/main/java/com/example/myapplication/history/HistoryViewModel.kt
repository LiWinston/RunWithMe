package com.example.myapplication.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.record.RetrofitClient
import kotlinx.coroutines.launch

/**
 * History界面的ViewModel
 * 管理今日/本周/本月的统计数据和运动记录
 */
class HistoryViewModel : ViewModel() {

    // 加载状态
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 今日数据
    private val _todayStats = MutableLiveData<Map<String, Any>>()
    val todayStats: LiveData<Map<String, Any>> = _todayStats

    private val _todayWorkouts = MutableLiveData<List<Workout>>()
    val todayWorkouts: LiveData<List<Workout>> = _todayWorkouts

    // 本周数据
    private val _weekStats = MutableLiveData<Map<String, Any>>()
    val weekStats: LiveData<Map<String, Any>> = _weekStats

    private val _weekWorkouts = MutableLiveData<List<Workout>>()
    val weekWorkouts: LiveData<List<Workout>> = _weekWorkouts

    private val _weekChart = MutableLiveData<Map<String, Any>>()
    val weekChart: LiveData<Map<String, Any>> = _weekChart

    // 本月数据
    private val _monthStats = MutableLiveData<Map<String, Any>>()
    val monthStats: LiveData<Map<String, Any>> = _monthStats

    private val _monthWorkouts = MutableLiveData<List<Workout>>()
    val monthWorkouts: LiveData<List<Workout>> = _monthWorkouts

    private val _monthChart = MutableLiveData<Map<String, Any>>()
    val monthChart: LiveData<Map<String, Any>> = _monthChart

    /**
     * 加载今日数据
     */
    fun loadTodayData(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 并行请求统计数据和运动记录
                val statsResponse = RetrofitClient.api.getTodayStats(userId)
                val workoutsResponse = RetrofitClient.api.getTodayWorkouts(userId)

                if (statsResponse.isSuccessful && statsResponse.body()?.code == 0) {
                    _todayStats.value = statsResponse.body()?.data ?: emptyMap()
                } else {
                    _error.value = "获取今日统计失败"
                }

                if (workoutsResponse.isSuccessful && workoutsResponse.body()?.code == 0) {
                    _todayWorkouts.value = workoutsResponse.body()?.data ?: emptyList()
                } else {
                    _error.value = "获取今日运动记录失败"
                }

            } catch (e: Exception) {
                _error.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载本周数据
     */
    fun loadWeekData(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 并行请求统计数据、运动记录和图表数据
                val statsResponse = RetrofitClient.api.getWeekStats(userId)
                val workoutsResponse = RetrofitClient.api.getWeekWorkouts(userId)
                val chartResponse = RetrofitClient.api.getWeekChart(userId)

                if (statsResponse.isSuccessful && statsResponse.body()?.code == 0) {
                    _weekStats.value = statsResponse.body()?.data ?: emptyMap()
                }

                if (workoutsResponse.isSuccessful && workoutsResponse.body()?.code == 0) {
                    _weekWorkouts.value = workoutsResponse.body()?.data ?: emptyList()
                }

                if (chartResponse.isSuccessful && chartResponse.body()?.code == 0) {
                    _weekChart.value = chartResponse.body()?.data ?: emptyMap()
                }

            } catch (e: Exception) {
                _error.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载本月数据
     */
    fun loadMonthData(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 并行请求统计数据、运动记录和图表数据
                val statsResponse = RetrofitClient.api.getMonthStats(userId)
                val workoutsResponse = RetrofitClient.api.getMonthWorkouts(userId)
                val chartResponse = RetrofitClient.api.getMonthChart(userId)

                if (statsResponse.isSuccessful && statsResponse.body()?.code == 0) {
                    _monthStats.value = statsResponse.body()?.data ?: emptyMap()
                }

                if (workoutsResponse.isSuccessful && workoutsResponse.body()?.code == 0) {
                    _monthWorkouts.value = workoutsResponse.body()?.data ?: emptyList()
                }

                if (chartResponse.isSuccessful && chartResponse.body()?.code == 0) {
                    _monthChart.value = chartResponse.body()?.data ?: emptyMap()
                }

            } catch (e: Exception) {
                _error.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }
}
