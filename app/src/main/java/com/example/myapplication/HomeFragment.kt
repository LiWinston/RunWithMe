package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
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
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment(R.layout.fragment_home) {
    
    private lateinit var weatherWidget: ExpandableWeatherWidget
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherRepository: WeatherRepository
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "HomeFragment"
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        setupLocationServices()
        loadWeatherData()
    }
    
    private fun initializeComponents() {
        weatherWidget = requireView().findViewById(R.id.weather_widget)
        
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
            return
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    fetchWeatherData(location.latitude, location.longitude)
                } else {
                    // 使用默认位置（墨尔本）
                    fetchWeatherData(-37.79798624199073, 144.94433507262613)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "获取位置失败", exception)
                // 使用默认位置（墨尔本）
                fetchWeatherData(-37.79798624199073, 144.94433507262613)
            }
    }
    
    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
                    getCurrentLocationAndLoadWeather()
                } else {
                    // 权限被拒绝，使用默认位置
                    fetchWeatherData(-37.79798624199073, 144.94433507262613)
                    Toast.makeText(requireContext(), "使用默认位置显示天气信息", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}