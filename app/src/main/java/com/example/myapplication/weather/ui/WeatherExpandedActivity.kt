package com.example.myapplication.weather.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.weather.api.WeatherApiService
import com.example.myapplication.weather.data.CurrentWeather
import com.example.myapplication.weather.data.HourlyForecast
import com.example.myapplication.weather.repository.WeatherRepository
import com.example.myapplication.weather.utils.WeatherIconUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherExpandedActivity : AppCompatActivity() {

    private lateinit var weatherRepository: WeatherRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    // UI组件
    private lateinit var btnBack: ImageButton
    private lateinit var weatherIcon: ImageView
    private lateinit var locationText: TextView
    private lateinit var weatherDescription: TextView
    private lateinit var temperatureText: TextView
    private lateinit var humidityValue: TextView
    private lateinit var feelsLikeValue: TextView
    private lateinit var windValue: TextView
    private lateinit var windGustValue: TextView
    private lateinit var pressureValue: TextView
    private lateinit var visibilityValue: TextView
    private lateinit var uvIndexValue: TextView
    private lateinit var dewPointValue: TextView
    private lateinit var windChillValue: TextView
    private lateinit var heatIndexValue: TextView
    private lateinit var hourlyForecastRecycler: RecyclerView
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2001
        private const val TAG = "WeatherExpandedActivity"
        private const val DEFAULT_LATITUDE = -33.768796
        private const val DEFAULT_LONGITUDE = 151.015735
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_card_expanded)

        initializeComponents()
        setupClickListeners()
        setupLocationServices()
        loadWeatherData()
    }

    private fun initializeComponents() {
        // 初始化UI组件
        btnBack = findViewById(R.id.btn_back)
        weatherIcon = findViewById(R.id.weather_icon_expanded)
        locationText = findViewById(R.id.location_text_expanded)
        weatherDescription = findViewById(R.id.weather_description_expanded)
        temperatureText = findViewById(R.id.temperature_text_expanded)
        humidityValue = findViewById(R.id.humidity_value_expanded)
        feelsLikeValue = findViewById(R.id.feels_like_value_expanded)
        windValue = findViewById(R.id.wind_value_expanded)
        windGustValue = findViewById(R.id.wind_gust_value_expanded)
        pressureValue = findViewById(R.id.pressure_value_expanded)
        visibilityValue = findViewById(R.id.visibility_value_expanded)
        uvIndexValue = findViewById(R.id.uv_index_value_expanded)
        dewPointValue = findViewById(R.id.dew_point_value_expanded)
        windChillValue = findViewById(R.id.wind_chill_value_expanded)
        heatIndexValue = findViewById(R.id.heat_index_value_expanded)
        hourlyForecastRecycler = findViewById(R.id.hourly_forecast_recycler)

        // 初始化Repository：复用带鉴权的统一 Retrofit
        val apiService = com.example.myapplication.landr.RetrofitClient.create(WeatherApiService::class.java)
        weatherRepository = WeatherRepository(apiService)

        // 设置每小时预报RecyclerView
        hourlyForecastAdapter = HourlyForecastAdapter()
        hourlyForecastRecycler.apply {
            layoutManager = LinearLayoutManager(this@WeatherExpandedActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyForecastAdapter
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun getCurrentLocationAndLoadWeather() {
        if (!checkLocationPermission()) {
            Log.w(TAG, "位置权限未授权，使用默认位置")
            useDefaultLocation()
            return
        }

        Log.d(TAG, "开始获取当前位置...")

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null && isLocationValid(location)) {
                    Log.i(TAG, "成功获取最后已知位置: 纬度=${location.latitude}, 经度=${location.longitude}")
                    fetchWeatherData(location.latitude, location.longitude)
                } else {
                    Log.w(TAG, "最后已知位置无效或为null，尝试请求新位置...")
                    requestNewLocation()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "获取最后已知位置失败: ${exception.message}", exception)
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

                val location = locationResult.lastLocation
                if (location != null) {
                    Log.i(TAG, "成功获取新位置: 纬度=${location.latitude}, 经度=${location.longitude}")
                    fusedLocationClient.removeLocationUpdates(this)
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
                locationCallback?.let {
                    Log.w(TAG, "位置请求超时，使用默认位置")
                    fusedLocationClient.removeLocationUpdates(it)
                    useDefaultLocation()
                }
            }, 5000)

        } catch (e: SecurityException) {
            Log.e(TAG, "位置权限被拒绝", e)
            useDefaultLocation()
        }
    }

    private fun useDefaultLocation() {
        fetchWeatherData(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        Log.i(TAG, "正在获取天气数据 - 纬度: $latitude, 经度: $longitude")
        lifecycleScope.launch {
            try {
                val (currentWeatherResult, hourlyForecastResult) = weatherRepository.getWeatherData(latitude, longitude)

                when {
                    currentWeatherResult.isSuccess && hourlyForecastResult.isSuccess -> {
                        val currentWeather = currentWeatherResult.getOrNull()!!
                        val hourlyForecast = hourlyForecastResult.getOrNull()
                        Log.d(TAG, "成功获取两项数据 - 当前天气: ${currentWeather.temperature.degrees}°")
                        updateWeatherUI(currentWeather, hourlyForecast)
                    }
                    currentWeatherResult.isSuccess -> {
                        val currentWeather = currentWeatherResult.getOrNull()!!
                        Log.d(TAG, "只获取到当前天气数据 - 温度: ${currentWeather.temperature.degrees}°")
                        updateWeatherUI(currentWeather, null)
                        Log.w(TAG, "每小时预报获取失败", hourlyForecastResult.exceptionOrNull())
                    }
                    else -> {
                        showError("获取天气数据失败")
                        Log.e(TAG, "获取天气数据失败", currentWeatherResult.exceptionOrNull())
                    }
                }
            } catch (e: Exception) {
                showError("网络连接失败")
                Log.e(TAG, "获取天气数据异常", e)
            }
        }
    }

    private fun updateWeatherUI(currentWeather: CurrentWeather, hourlyForecast: HourlyForecast?) {
        // 加载天气图标
        val iconUrl = WeatherIconUtils.getIconUrl(currentWeather.condition.iconBaseUri, false)
        Glide.with(this)
            .load(iconUrl)
            .into(weatherIcon)

        // 更新主要信息
        locationText.text = "墨尔本, 卡尔顿"
        weatherDescription.text = "${WeatherIconUtils.getWeatherDescription(currentWeather.condition.type)}，适合户外运动"
        temperatureText.text = "${currentWeather.temperature.degrees.toInt()}°"

        // 更新详细信息
        humidityValue.text = "${currentWeather.humidity}%"
        feelsLikeValue.text = "${currentWeather.feelsLikeTemperature.degrees.toInt()}°C"
        windValue.text = "${currentWeather.wind.speed.value.toInt()} ${currentWeather.wind.speed.unit} ${currentWeather.wind.direction.cardinal}"
        windGustValue.text = currentWeather.wind.gust?.let { "${it.value.toInt()} ${it.unit}" } ?: "无"
        pressureValue.text = "${currentWeather.pressure.meanSeaLevelMillibars.toInt()} hPa"
        visibilityValue.text = "${currentWeather.visibility.distance.toInt()} ${currentWeather.visibility.unit}"
        uvIndexValue.text = "${currentWeather.uvIndex} ${getUvDescription(currentWeather.uvIndex)}"
        dewPointValue.text = "${currentWeather.dewPoint.degrees.toInt()}°C"
        windChillValue.text = "${currentWeather.windChill.degrees.toInt()}°C"
        heatIndexValue.text = "${currentWeather.heatIndex.degrees.toInt()}°C"

        // 更新每小时预报
        hourlyForecast?.let {
            if (it.forecasts.isNotEmpty()) {
                hourlyForecastAdapter.updateData(it.forecasts)
            }
        }
    }

    private fun getUvDescription(uvIndex: Int): String {
        return when {
            uvIndex <= 2 -> "低"
            uvIndex <= 5 -> "中等"
            uvIndex <= 7 -> "高"
            uvIndex <= 10 -> "很高"
            else -> "极高"
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "使用默认位置显示天气信息", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}
