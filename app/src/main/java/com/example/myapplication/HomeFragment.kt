package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.gemini.GeminiApiService
import com.example.myapplication.gemini.GeminiConfig
import com.example.myapplication.weather.api.WeatherApiService
import com.example.myapplication.weather.data.CurrentWeather
import com.example.myapplication.weather.repository.WeatherRepository
import com.example.myapplication.weather.ui.ExpandableWeatherWidget
import com.example.myapplication.weather.ui.WeatherExpandedActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var weatherWidget: ExpandableWeatherWidget
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var geminiApiService: GeminiApiService
    private var locationCallback: LocationCallback? = null

    // UI elements for AI advice
    private lateinit var aiAdviceText: TextView
    private lateinit var adviceLoadingProgress: ProgressBar

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "HomeFragment"
        private const val DEFAULT_LATITUDE = -33.768796
        private const val DEFAULT_LONGITUDE = 151.015735

        // Default weather data for fallback when API fails
        private const val DEFAULT_TEMPERATURE = 20.0
        private const val DEFAULT_WEATHER_CONDITION = "Partly Cloudy"
        private const val DEFAULT_WIND_SPEED = 15.0
        private const val DEFAULT_HUMIDITY = 65
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        initializeGeminiService()
        initializeUIComponents(view)
        setupLocationServices()
        loadWeatherData()

        // 天气卡片点击事件：进入扩展页
        setupWeatherCardClick(view)
    }

    private fun setupWeatherCardClick(view: View) {
        val weatherCard = view.findViewById<MaterialCardView>(R.id.weather_card)
        weatherCard?.setOnClickListener {
            val intent = Intent(requireContext(), WeatherExpandedActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeComponents() {
        weatherWidget = requireView().findViewById(R.id.weather_card)
        val apiService = com.example.myapplication.landr.RetrofitClient.create(WeatherApiService::class.java)
        weatherRepository = WeatherRepository(apiService)
    }

    private fun initializeGeminiService() {
        if (!GeminiConfig.isConfigured()) {
            Log.w(TAG, "Gemini API key not configured. Please set your API key in GeminiConfig.kt")
            return
        }
        geminiApiService = GeminiApiService(GeminiConfig.API_KEY)
        Log.d(TAG, "Gemini API service initialized")
    }

    private fun initializeUIComponents(view: View) {
        aiAdviceText = view.findViewById(R.id.aiAdviceText)
        adviceLoadingProgress = view.findViewById(R.id.adviceLoadingProgress)

        // 加载组动态审计 Feed
        loadGroupFeed(view)

        // 点击动态卡片打开 BottomSheet 展示完整 Feed
        view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.dynamic_card)
            ?.setOnClickListener {
                val sheet = com.example.myapplication.feed.FeedBottomSheet()
                sheet.show(parentFragmentManager, "feedBottomSheet")
            }
    }

    private fun loadGroupFeed(view: View) {
        val todayDate = view.findViewById<TextView>(R.id.todayDate)
        val todayDeed = view.findViewById<TextView>(R.id.todayDeed)
        val yesterdayDate = view.findViewById<TextView>(R.id.yesterdayDate)
        val yesterdayDeed = view.findViewById<TextView>(R.id.yesterdayDeed)
        val thirdDate = view.findViewById<TextView>(R.id.thirdDate)
        val thirdDeed = view.findViewById<TextView>(R.id.thirdDeed)

        Log.d(TAG, "开始加载组动态Feed")
        val api = com.example.myapplication.landr.RetrofitClient.create(
            com.example.myapplication.group.GroupApi::class.java
        )
        api.feed(20).enqueue(object :
            retrofit2.Callback<com.example.myapplication.group.Result<com.example.myapplication.group.FeedResponse>> {
            override fun onResponse(
                call: retrofit2.Call<com.example.myapplication.group.Result<com.example.myapplication.group.FeedResponse>>,
                response: retrofit2.Response<com.example.myapplication.group.Result<com.example.myapplication.group.FeedResponse>>
            ) {
                Log.d(TAG, "Feed API响应 - 成功: ${response.isSuccessful}, code: ${response.code()}")
                val res = response.body()
                Log.d(TAG, "响应body - code: ${res?.code}, message: ${res?.message}, data: ${res?.data}")

                if (response.isSuccessful && res != null) {
                    Log.d(TAG, "API返回 - code: ${res.code}, message: ${res.message}")

                    if (res.code == 0 && res.data != null) {
                        val feed = res.data
                        Log.d(
                            TAG,
                            "Feed数据 - workouts数量: ${feed.workouts?.size ?: 0}, interactions数量: ${feed.interactions?.size ?: 0}"
                        )

                        // 组装一个简单的三行：优先展示 workout，再展示互动
                        val items = mutableListOf<Pair<String, String>>()

                        // —— 合并后的统一实现（兼容 distanceKm / distance、summary / fallback）——
                        feed.workouts?.take(3)?.forEach { w ->
                            val dateStr = w.startTime ?: ""

                            // 取 summary 字段（如果存在且非空）
                            val summaryFromField =
                                (w::class.members.firstOrNull { it.name == "summary" }?.call(w) as? String)
                                    ?.takeIf { it.isNotBlank() }

                            // 距离字段：优先 distanceKm，否则 distance；都没有则 0.0
                            val distanceKm = when {
                                try {
                                    w.javaClass.getDeclaredField("distanceKm"); true
                                } catch (_: Throwable) {
                                    false
                                } -> (w::class.members.firstOrNull { it.name == "distanceKm" }
                                    ?.call(w) as? Double) ?: 0.0

                                try {
                                    w.javaClass.getDeclaredField("distance"); true
                                } catch (_: Throwable) {
                                    false
                                } -> (w::class.members.firstOrNull { it.name == "distance" }
                                    ?.call(w) as? Double) ?: 0.0

                                else -> 0.0
                            }

                            val workoutType = w.workoutType
                            val fallbackSummary = buildString {
                                append("🏃 ")
                                append(String.format("%.1f km", distanceKm))
                                if (!workoutType.isNullOrBlank()) append(" · ").append(workoutType)
                            }

                            val summary = summaryFromField ?: fallbackSummary

                            items += dateStr to summary
                            Log.d(TAG, "添加workout: $dateStr - $summary")
                        }

                        val remaining = 3 - items.size
                        if (remaining > 0) {
                            feed.interactions?.take(remaining)?.forEach { n ->
                                val dateStr = n.createdAt ?: ""

                                val summaryFromField =
                                    (n::class.members.firstOrNull { it.name == "summary" }?.call(n) as? String)
                                        ?.takeIf { it.isNotBlank() }

                                val typeField =
                                    (n::class.members.firstOrNull { it.name == "type" }?.call(n) as? String)

                                val summary = summaryFromField ?: when (typeField) {
                                    "LIKE" -> "👍 Like"
                                    "REMIND" -> "⏰ Remind"
                                    else -> typeField ?: ""
                                }

                                items += dateStr to summary
                                Log.d(TAG, "添加interaction: $dateStr - $summary")
                            }
                        }

                        Log.d(TAG, "总共收集到 ${items.size} 条动态")

                        fun fmt(src: String): String {
                            return try {
                                if (src.length >= 16) src.substring(5, 16).replace('T', ' ') else src
                            } catch (_: Exception) {
                                src
                            }
                        }

                        // 写入三个槽位
                        val line1 = items.getOrNull(0)
                        val line2 = items.getOrNull(1)
                        val line3 = items.getOrNull(2)

                        todayDate.text = line1?.first?.let { fmt(it) } ?: "dd/mm/yy--"
                        todayDeed.text = line1?.second ?: "--"

                        yesterdayDate.text = line2?.first?.let { fmt(it) } ?: "dd/yy/mm--"
                        yesterdayDeed.text = line2?.second ?: "--"

                        thirdDate.text = line3?.first?.let { fmt(it) } ?: "dd/yy/mm--"
                        thirdDeed.text = line3?.second ?: "--"

                        Log.d(TAG, "Feed UI更新完成")
                    } else {
                        Log.w(TAG, "Feed数据为空或API返回错误 - code: ${res.code}, message: ${res.message}")
                    }
                } else {
                    Log.e(TAG, "Feed API请求失败 - HTTP code: ${response.code()}, message: ${response.message()}")
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.example.myapplication.group.Result<com.example.myapplication.group.FeedResponse>>,
                t: Throwable
            ) {
                Log.e(TAG, "Feed API请求失败", t)
            }
        })
    }

    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun loadWeatherData() {
        if (checkLocationPermission()) {
            getCurrentLocationAndLoadWeather()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun getCurrentLocationAndLoadWeather() {
        if (!checkLocationPermission()) {
            Log.w(TAG, "位置权限未授权，使用默认位置")
            return
        }

        Log.d(TAG, "开始获取当前位置...")

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null && isLocationValid(location)) {
                    Log.i(TAG, "成功获取最后已知位置: 纬度=${location.latitude}, 经度=${location.longitude}")
                    Log.i(TAG, "位置精度: ${location.accuracy}米, 时间: ${location.time}")
                    fetchWeatherData(location.latitude, location.longitude)
                } else {
                    Log.w(TAG, "最后已知位置无效或为null，尝试请求新位置...")
                    requestNewLocation()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "获取最后已知位置失败: ${exception.message}", exception)
                Log.w(TAG, "尝试请求新位置...")
                requestNewLocation()
            }
    }

    private fun isLocationValid(location: Location): Boolean {
        val currentTime = System.currentTimeMillis()
        val locationAge = currentTime - location.time
        val maxAge = 5 * 60 * 1000
        return locationAge <= maxAge && location.accuracy <= 100
    }

    private fun requestNewLocation() {
        if (!isAdded || view == null) {
            useDefaultLocation()
            return
        }

        if (!checkLocationPermission()) {
            useDefaultLocation()
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMaxUpdateDelayMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                if (!isAdded || view == null) {
                    return
                }

                val location = locationResult.lastLocation
                if (location != null) {
                    Log.i(TAG, "成功获取新位置: 纬度=${location.latitude}, 经度=${location.longitude}")
                    Log.i(TAG, "位置精度: ${location.accuracy}米")

                    fusedLocationClient.removeLocationUpdates(locationCallback!!)
                    locationCallback = null

                    fetchWeatherData(location.latitude, location.longitude)
                } else {
                    Log.w(TAG, "获取新位置失败，使用默认位置")
                    locationCallback = null
                    useDefaultLocation()
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )

            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (!isAdded || view == null) {
                    return@postDelayed
                }

                if (locationCallback != null) {
                    Log.w(TAG, "位置请求超时，使用默认位置")
                    fusedLocationClient.removeLocationUpdates(locationCallback!!)
                    locationCallback = null
                    useDefaultLocation()
                }
            }, 5000)

        } catch (e: SecurityException) {
            Log.e(TAG, "位置权限被拒绝", e)
            locationCallback = null
            useDefaultLocation()
        }
    }

    private fun useDefaultLocation() {
        if (!isAdded || view == null) {
            return
        }
        fetchWeatherData(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        if (!isAdded || view == null) {
            return
        }

        Log.i(TAG, "正在获取天气数据 - 纬度: $latitude, 经度: $longitude")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val (currentWeatherResult, hourlyForecastResult) =
                    weatherRepository.getWeatherData(latitude, longitude)

                when {
                    currentWeatherResult.isSuccess && hourlyForecastResult.isSuccess -> {
                        val currentWeather = currentWeatherResult.getOrNull()!!
                        val hourlyForecast = hourlyForecastResult.getOrNull()
                        Log.d(
                            TAG,
                            "成功获取两项数据 - 当前天气: ${currentWeather.temperature.degrees}°, 每小时预报数量: ${hourlyForecast?.forecasts?.size ?: 0}"
                        )
                        weatherWidget.updateWeatherData(currentWeather, hourlyForecast)

                        // Generate AI advice based on weather
                        generateExerciseAdvice(currentWeather)
                    }

                    currentWeatherResult.isSuccess -> {
                        val currentWeather = currentWeatherResult.getOrNull()!!
                        Log.d(TAG, "只获取到当前天气数据 - 温度: ${currentWeather.temperature.degrees}°")
                        weatherWidget.updateWeatherData(currentWeather)
                        Log.w(TAG, "每小时预报获取失败", hourlyForecastResult.exceptionOrNull())

                        // Generate AI advice based on weather
                        generateExerciseAdvice(currentWeather)
                    }

                    else -> {
                        showError("Fail to get weather data")
                        Log.e(TAG, "获取天气数据失败", currentWeatherResult.exceptionOrNull())

                        // Generate AI advice with default weather data
                        generateExerciseAdviceWithDefaults()
                    }
                }
            } catch (e: Exception) {
                showError("Internet Error")
                Log.e(TAG, "获取天气数据异常", e)

                // Generate AI advice with default weather data
                generateExerciseAdviceWithDefaults()
            }
        }
    }

    private fun showError(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generate exercise advice using Gemini AI based on current weather
     */
    private fun generateExerciseAdvice(weather: CurrentWeather) {
        if (!GeminiConfig.isConfigured()) {
            aiAdviceText.text =
                "⚠️ AI advice unavailable. Please configure Gemini API key in GeminiConfig.kt to enable personalized exercise recommendations."
            return
        }

        if (!::geminiApiService.isInitialized) {
            aiAdviceText.text = "AI service not available"
            return
        }

        showAdviceLoading()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(
                    TAG,
                    "Generating AI advice for weather: ${weather.temperature.degrees}°C, ${weather.condition.description.text}"
                )

                val result = geminiApiService.getWeatherBasedAdvice(
                    temperature = weather.temperature.degrees,
                    weatherCondition = weather.condition.description.text,
                    windSpeed = weather.wind.speed.value,
                    humidity = weather.humidity
                )

                if (!isAdded || view == null) {
                    return@launch
                }

                result.fold(
                    onSuccess = { advice ->
                        Log.d(TAG, "Successfully generated AI advice")
                        showAdvice(advice)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to generate AI advice", error)
                        showAdviceError()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error generating exercise advice", e)
                if (isAdded && view != null) {
                    showAdviceError()
                }
            }
        }
    }

    private fun showAdviceLoading() {
        if (!isAdded || view == null) return
        adviceLoadingProgress.visibility = View.VISIBLE
        aiAdviceText.text = "Generating personalized exercise advice..."
    }

    private fun showAdvice(advice: String) {
        if (!isAdded || view == null) return
        adviceLoadingProgress.visibility = View.GONE
        aiAdviceText.text = advice
    }

    private fun showAdviceError() {
        if (!isAdded || view == null) return
        adviceLoadingProgress.visibility = View.GONE
        aiAdviceText.text =
            "Unable to generate advice at this time. Please check your internet connection and try again."
    }

    private fun generateExerciseAdviceWithDefaults() {
        if (!GeminiConfig.isConfigured()) {
            aiAdviceText.text =
                "⚠️ AI advice unavailable. Please configure Gemini API key in GeminiConfig.kt to enable personalized exercise recommendations."
            return
        }

        if (!::geminiApiService.isInitialized) {
            aiAdviceText.text = "AI service not available"
            return
        }

        Log.i(TAG, "Weather data unavailable, using default weather conditions for AI advice")

        showAdviceLoading()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(
                    TAG,
                    "Generating AI advice with default weather: ${DEFAULT_TEMPERATURE}°C, $DEFAULT_WEATHER_CONDITION"
                )

                val result = geminiApiService.getWeatherBasedAdvice(
                    temperature = DEFAULT_TEMPERATURE,
                    weatherCondition = DEFAULT_WEATHER_CONDITION,
                    windSpeed = DEFAULT_WIND_SPEED,
                    humidity = DEFAULT_HUMIDITY
                )

                if (!isAdded || view == null) {
                    return@launch
                }

                result.fold(
                    onSuccess = { advice ->
                        Log.d(TAG, "Successfully generated AI advice with default weather")
                        showAdvice(advice)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to generate AI advice with default weather", error)
                        showAdviceError()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error generating exercise advice with defaults", e)
                if (isAdded && view != null) {
                    showAdviceError()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "位置权限已授权，重新尝试获取位置")
                    getCurrentLocationAndLoadWeather()
                } else {
                    Log.w(TAG, "位置权限被拒绝，使用默认位置")
                    useDefaultLocation()
                    Toast.makeText(requireContext(), "使用默认位置显示天气信息", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}
