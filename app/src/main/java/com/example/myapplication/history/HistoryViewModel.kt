package com.example.myapplication.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.gemini.GeminiApiService
import com.example.myapplication.gemini.GeminiConfig
import com.example.myapplication.gemini.PeriodWorkoutData
import com.example.myapplication.landr.loginapp.models.User
import com.example.myapplication.record.RetrofitClient
import com.example.myapplication.record.Workout
import kotlinx.coroutines.launch

/**
 * History界面的ViewModel
 * 管理今日/本周/本月的统计数据和运动记录
 */
class HistoryViewModel : ViewModel() {

    companion object {
        private const val TAG = "HistoryViewModel"
    }

    // Gemini AI service
    private val geminiService: GeminiApiService by lazy {
        GeminiApiService(GeminiConfig.API_KEY)
    }

    // 加载状态
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // 用户信息
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

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
    
    // AI建议
    private val _todayAdvice = MutableLiveData<String?>()
    val todayAdvice: LiveData<String?> = _todayAdvice
    
    private val _weekAdvice = MutableLiveData<String?>()
    val weekAdvice: LiveData<String?> = _weekAdvice
    
    private val _monthAdvice = MutableLiveData<String?>()
    val monthAdvice: LiveData<String?> = _monthAdvice
    
    // AI加载状态
    private val _isLoadingAdvice = MutableLiveData(false)
    val isLoadingAdvice: LiveData<Boolean> = _isLoadingAdvice

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
                    _todayWorkouts.value = workoutsResponse.body()?.data ?: emptyList<Workout>()
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
            Log.d(TAG, "Starting to load week data for userId: $userId")
            _isLoading.value = true
            _error.value = null

            try {
                // 并行请求统计数据、运动记录和图表数据
                val statsResponse = RetrofitClient.api.getWeekStats(userId)
                val workoutsResponse = RetrofitClient.api.getWeekWorkouts(userId)
                val chartResponse = RetrofitClient.api.getWeekChart(userId)

                if (statsResponse.isSuccessful && statsResponse.body()?.code == 0) {
                    val data = statsResponse.body()?.data ?: emptyMap()
                    Log.d(TAG, "Week stats loaded: $data")
                    _weekStats.value = data
                } else {
                    Log.e(TAG, "Failed to load week stats: ${statsResponse.code()} - ${statsResponse.body()?.message}")
                }

                if (workoutsResponse.isSuccessful && workoutsResponse.body()?.code == 0) {
                    val workouts = workoutsResponse.body()?.data ?: emptyList<Workout>()
                    Log.d(TAG, "Week workouts loaded: ${workouts.size} workouts")
                    _weekWorkouts.value = workouts
                } else {
                    Log.e(TAG, "Failed to load week workouts: ${workoutsResponse.code()} - ${workoutsResponse.body()?.message}")
                }

                if (chartResponse.isSuccessful && chartResponse.body()?.code == 0) {
                    val chart = chartResponse.body()?.data ?: emptyMap()
                    Log.d(TAG, "Week chart loaded: $chart")
                    _weekChart.value = chart
                } else {
                    Log.e(TAG, "Failed to load week chart: ${chartResponse.code()} - ${chartResponse.body()?.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading week data", e)
                _error.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Finished loading week data")
            }
        }
    }

    /**
     * 加载本月数据
     */
    fun loadMonthData(userId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "Starting to load month data for userId: $userId")
            _isLoading.value = true
            _error.value = null

            try {
                // 并行请求统计数据、运动记录和图表数据
                val statsResponse = RetrofitClient.api.getMonthStats(userId)
                val workoutsResponse = RetrofitClient.api.getMonthWorkouts(userId)
                val chartResponse = RetrofitClient.api.getMonthChart(userId)

                if (statsResponse.isSuccessful && statsResponse.body()?.code == 0) {
                    val data = statsResponse.body()?.data ?: emptyMap()
                    Log.d(TAG, "Month stats loaded: $data")
                    _monthStats.value = data
                } else {
                    Log.e(TAG, "Failed to load month stats: ${statsResponse.code()} - ${statsResponse.body()?.message}")
                }

                if (workoutsResponse.isSuccessful && workoutsResponse.body()?.code == 0) {
                    val workouts = workoutsResponse.body()?.data ?: emptyList<Workout>()
                    Log.d(TAG, "Month workouts loaded: ${workouts.size} workouts")
                    _monthWorkouts.value = workouts
                } else {
                    Log.e(TAG, "Failed to load month workouts: ${workoutsResponse.code()} - ${workoutsResponse.body()?.message}")
                }

                if (chartResponse.isSuccessful && chartResponse.body()?.code == 0) {
                    val chart = chartResponse.body()?.data ?: emptyMap()
                    Log.d(TAG, "Month chart loaded: $chart")
                    _monthChart.value = chart
                } else {
                    Log.e(TAG, "Failed to load month chart: ${chartResponse.code()} - ${chartResponse.body()?.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading month data", e)
                _error.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Finished loading month data")
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 加载用户信息
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getUserProfile()
                if (response.isSuccessful && response.body()?.code == 0) {
                    _userProfile.value = response.body()?.data
                    Log.d(TAG, "User profile loaded successfully")
                } else {
                    Log.e(TAG, "Failed to load user profile: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user profile", e)
            }
        }
    }
    
    /**
     * 生成今日运动建议
     */
    fun generateTodayAdvice() {
        viewModelScope.launch {
            val stats = _todayStats.value
            val workouts = _todayWorkouts.value ?: emptyList()
            val user = _userProfile.value
            
            // 如果没有数据，直接显示鼓励信息
            if (workouts.isEmpty() || stats == null) {
                _todayAdvice.value = "Looks like you haven’t been working out today, don’t forget your goal! Getting started is always the hardest part, you’ve got this! 💪"
                return@launch
            }
            
            _isLoadingAdvice.value = true
            
            try {
                val totalDistance = stats["totalDistance"] as? Double ?: 0.0
                val totalDuration = (stats["totalDuration"] as? Number)?.toInt() ?: 0
                val totalCalories = stats["totalCalories"] as? Double ?: 0.0
                val workoutCount = (stats["workoutCount"] as? Number)?.toInt() ?: 0
                
                // 计算平均配速 (min/km)
                val avgPace = if (totalDistance > 0 && totalDuration > 0) {
                    (totalDuration / 60.0) / totalDistance
                } else {
                    0.0
                }
                
                val periodData = PeriodWorkoutData(
                    periodName = "Today",
                    totalDistance = totalDistance,
                    totalDuration = totalDuration / 60, // convert to minutes
                    totalWorkouts = workoutCount,
                    totalCalories = totalCalories,
                    avgPace = avgPace,
                    userFitnessGoal = user?.fitnessGoal,
                    userFitnessLevel = user?.fitnessLevel,
                    userAge = user?.age,
                    userGender = user?.gender,
                    userHeight = user?.height,
                    userWeight = user?.weight
                )
                
                val result = geminiService.getPeriodBasedAdvice(periodData)
                _todayAdvice.value = result.getOrElse { 
                    "Failed to generate advice: ${it.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating today advice", e)
                _todayAdvice.value = "Failed to generate advice"
            } finally {
                _isLoadingAdvice.value = false
            }
        }
    }
    
    /**
     * 生成本周运动建议
     */
    fun generateWeekAdvice() {
        viewModelScope.launch {
            val stats = _weekStats.value
            val workouts = _weekWorkouts.value ?: emptyList()
            val user = _userProfile.value
            
            // 如果没有数据，直接显示鼓励信息
            if (workouts.isEmpty() || stats == null) {
                _weekAdvice.value = "Looks like you haven’t been working out this week, don’t forget your goal! Getting started is always the hardest part, you’ve got this! 💪"
                return@launch
            }
            
            _isLoadingAdvice.value = true
            
            try {
                val totalDistance = stats["totalDistance"] as? Double ?: 0.0
                val totalDuration = (stats["totalDuration"] as? Number)?.toInt() ?: 0
                val totalCalories = stats["totalCalories"] as? Double ?: 0.0
                val workoutCount = (stats["workoutCount"] as? Number)?.toInt() ?: 0
                
                val avgPace = if (totalDistance > 0 && totalDuration > 0) {
                    (totalDuration / 60.0) / totalDistance
                } else {
                    0.0
                }
                
                val periodData = PeriodWorkoutData(
                    periodName = "This Week",
                    totalDistance = totalDistance,
                    totalDuration = totalDuration / 60,
                    totalWorkouts = workoutCount,
                    totalCalories = totalCalories,
                    avgPace = avgPace,
                    userFitnessGoal = user?.fitnessGoal,
                    userFitnessLevel = user?.fitnessLevel,
                    userAge = user?.age,
                    userGender = user?.gender,
                    userHeight = user?.height,
                    userWeight = user?.weight
                )
                
                val result = geminiService.getPeriodBasedAdvice(periodData)
                _weekAdvice.value = result.getOrElse { 
                    "Failed to generate advice: ${it.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating week advice", e)
                _weekAdvice.value = "Failed to generate advice"
            } finally {
                _isLoadingAdvice.value = false
            }
        }
    }
    
    /**
     * 生成本月运动建议
     */
    fun generateMonthAdvice() {
        viewModelScope.launch {
            val stats = _monthStats.value
            val workouts = _monthWorkouts.value ?: emptyList()
            val user = _userProfile.value
            
            // 如果没有数据，直接显示鼓励信息
            if (workouts.isEmpty() || stats == null) {
                _monthAdvice.value = "Looks like you haven’t been working out this month, don’t forget your goal! Getting started is always the hardest part, you’ve got this! 💪"
                return@launch
            }
            
            _isLoadingAdvice.value = true
            
            try {
                val totalDistance = stats["totalDistance"] as? Double ?: 0.0
                val totalDuration = (stats["totalDuration"] as? Number)?.toInt() ?: 0
                val totalCalories = stats["totalCalories"] as? Double ?: 0.0
                val workoutCount = (stats["workoutCount"] as? Number)?.toInt() ?: 0
                
                val avgPace = if (totalDistance > 0 && totalDuration > 0) {
                    (totalDuration / 60.0) / totalDistance
                } else {
                    0.0
                }
                
                val periodData = PeriodWorkoutData(
                    periodName = "This Month",
                    totalDistance = totalDistance,
                    totalDuration = totalDuration / 60,
                    totalWorkouts = workoutCount,
                    totalCalories = totalCalories,
                    avgPace = avgPace,
                    userFitnessGoal = user?.fitnessGoal,
                    userFitnessLevel = user?.fitnessLevel,
                    userAge = user?.age,
                    userGender = user?.gender,
                    userHeight = user?.height,
                    userWeight = user?.weight
                )
                
                val result = geminiService.getPeriodBasedAdvice(periodData)
                _monthAdvice.value = result.getOrElse { 
                    "Failed to generate advice: ${it.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating month advice", e)
                _monthAdvice.value = "Failed to generate advice"
            } finally {
                _isLoadingAdvice.value = false
            }
        }
    }
}
