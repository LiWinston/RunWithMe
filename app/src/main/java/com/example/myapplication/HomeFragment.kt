package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.weather.api.WeatherApiService
import com.example.myapplication.weather.repository.WeatherRepository
import com.example.myapplication.weather.ui.ExpandableWeatherWidget
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment(R.layout.fragment_home) {
    
    private lateinit var weatherWidget: ExpandableWeatherWidget
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherRepository: WeatherRepository
    private var locationCallback: LocationCallback? = null
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "HomeFragment"
        private const val DEFAULT_LATITUDE = -33.768796
        private const val DEFAULT_LONGITUDE = 151.015735
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        setupLocationServices()
        loadWeatherData()
    }
    
    private fun initializeComponents() {
        weatherWidget = requireView().findViewById(R.id.weather_card)
        
        // 初始化Retrofit和API服务
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // 开发环境使用模拟器默认IP
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        val apiService = retrofit.create(WeatherApiService::class.java)
        weatherRepository = WeatherRepository(apiService)
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
        
        // 首先尝试获取最后已知位置
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
        // 检查位置是否有效（不是模拟器的默认位置，且时间不太旧）
        val currentTime = System.currentTimeMillis()
        val locationAge = currentTime - location.time
        val maxAge = 5 * 60 * 1000 // 5分钟
        
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
                    Log.i(TAG, "位置精度: ${location.accuracy}米")
                    
                    // 停止位置更新
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
            
            // 5秒后如果还没有获取到位置，就使用默认位置
            requireView().postDelayed({
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
        fetchWeatherData(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }
    
    private fun fetchWeatherData(latitude: Double, longitude: Double) {
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
                    }
                    currentWeatherResult.isSuccess -> {
                        val currentWeather = currentWeatherResult.getOrNull()!!
                        Log.d(TAG, "只获取到当前天气数据 - 温度: ${currentWeather.temperature.degrees}°")
                        weatherWidget.updateWeatherData(currentWeather)
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
    
    private fun showError(message: String) {
        // 检查Fragment是否还附着到Activity，避免crash
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
                    // 权限被拒绝，使用默认位置
                    Log.w(TAG, "位置权限被拒绝，使用默认位置")
                    useDefaultLocation()
                    Toast.makeText(requireContext(), "使用默认位置显示天气信息", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理位置回调
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}