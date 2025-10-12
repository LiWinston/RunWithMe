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
import com.example.myapplication.weather.ui.WeatherExpandedActivity  // 新增导入
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.card.MaterialCardView  // 新增导入
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

        // 新增：添加天气卡片点击事件
        setupWeatherCardClick(view)
    }

    // 新增这个方法
    private fun setupWeatherCardClick(view: View) {
        val weatherCard = view.findViewById<MaterialCardView>(R.id.weather_card)
        weatherCard?.setOnClickListener {
            val intent = Intent(requireContext(), WeatherExpandedActivity::class.java)
            startActivity(intent)

        }
    }

    private fun initializeComponents() {
        weatherWidget = requireView().findViewById(R.id.weather_card)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(WeatherApiService::class.java)
        weatherRepository = WeatherRepository(apiService)
    }
    
    private fun initializeGeminiService() {
        // Check if API key is configured
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

                    fetchWeatherData(location.latitude, location.longitude)
                } else {
                    Log.w(TAG, "获取新位置失败，使用默认位置")
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
                    useDefaultLocation()
                }
            }, 5000)

        } catch (e: SecurityException) {
            Log.e(TAG, "位置权限被拒绝", e)
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
                val (currentWeatherResult, hourlyForecastResult) = weatherRepository.getWeatherData(latitude, longitude)

                when {
                    currentWeatherResult.isSuccess && hourlyForecastResult.isSuccess -> {
                        val currentWeather = currentWeatherResult.getOrNull()!!
                        val hourlyForecast = hourlyForecastResult.getOrNull()
                        Log.d(TAG, "成功获取两项数据 - 当前天气: ${currentWeather.temperature.degrees}°, 每小时预报数量: ${hourlyForecast?.forecasts?.size ?: 0}")
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
                        showError("获取天气数据失败")
                        Log.e(TAG, "获取天气数据失败", currentWeatherResult.exceptionOrNull())
                        
                        // Generate AI advice with default weather data
                        generateExerciseAdviceWithDefaults()
                    }
                }
            } catch (e: Exception) {
                showError("网络连接失败")
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
        // Check if Gemini API is configured
        if (!GeminiConfig.isConfigured()) {
            aiAdviceText.text = "⚠️ AI advice unavailable. Please configure Gemini API key in GeminiConfig.kt to enable personalized exercise recommendations."
            return
        }
        
        // Check if service is initialized
        if (!::geminiApiService.isInitialized) {
            aiAdviceText.text = "AI service not available"
            return
        }
        
        // Show loading state
        showAdviceLoading()
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "Generating AI advice for weather: ${weather.temperature.degrees}°C, ${weather.condition.description.text}")
                
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
    
    /**
     * Show loading state for AI advice
     */
    private fun showAdviceLoading() {
        if (!isAdded || view == null) return
        
        adviceLoadingProgress.visibility = View.VISIBLE
        aiAdviceText.text = "Generating personalized exercise advice..."
    }
    
    /**
     * Display the generated advice
     */
    private fun showAdvice(advice: String) {
        if (!isAdded || view == null) return
        
        adviceLoadingProgress.visibility = View.GONE
        aiAdviceText.text = advice
    }
    
    /**
     * Show error state for AI advice
     */
    private fun showAdviceError() {
        if (!isAdded || view == null) return
        
        adviceLoadingProgress.visibility = View.GONE
        aiAdviceText.text = "Unable to generate advice at this time. Please check your internet connection and try again."
    }
    
    /**
     * Generate exercise advice using default weather data when weather API fails
     */
    private fun generateExerciseAdviceWithDefaults() {
        // Check if Gemini API is configured
        if (!GeminiConfig.isConfigured()) {
            aiAdviceText.text = "⚠️ AI advice unavailable. Please configure Gemini API key in GeminiConfig.kt to enable personalized exercise recommendations."
            return
        }
        
        // Check if service is initialized
        if (!::geminiApiService.isInitialized) {
            aiAdviceText.text = "AI service not available"
            return
        }
        
        Log.i(TAG, "Weather data unavailable, using default weather conditions for AI advice")
        
        // Show loading state
        showAdviceLoading()
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "Generating AI advice with default weather: ${DEFAULT_TEMPERATURE}°C, $DEFAULT_WEATHER_CONDITION")
                
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