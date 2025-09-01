package com.example.myapplication.weather.repository

import com.example.myapplication.weather.api.WeatherApiService
import com.example.myapplication.weather.data.CurrentWeather
import com.example.myapplication.weather.data.HourlyForecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 天气数据仓库
 * 负责从API获取天气数据并提供给UI层
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) {
    
    /**
     * 获取当前天气状况
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<CurrentWeather> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getCurrentWeather(latitude, longitude)
                if (response.isSuccessful) {
                    val apiResult = response.body()
                    if (apiResult?.code == 0 && apiResult.data != null) {
                        Result.success(apiResult.data)
                    } else {
                        Result.failure(Exception(apiResult?.message ?: "获取天气数据失败"))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取每小时天气预报
     */
    suspend fun getHourlyForecast(
        latitude: Double, 
        longitude: Double, 
        hours: Int = 24
    ): Result<HourlyForecast> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getHourlyForecast(latitude, longitude, hours = hours)
                if (response.isSuccessful) {
                    val apiResult = response.body()
                    if (apiResult?.code == 0 && apiResult.data != null) {
                        android.util.Log.d("WeatherRepository", "每小时预报API返回成功，数据: ${apiResult.data}")
                        Result.success(apiResult.data)
                    } else {
                        android.util.Log.e("WeatherRepository", "每小时预报API返回错误: code=${apiResult?.code}, message=${apiResult?.message}")
                        Result.failure(Exception(apiResult?.message ?: "获取每小时预报失败"))
                    }
                } else {
                    android.util.Log.e("WeatherRepository", "每小时预报网络请求失败: ${response.code()}")
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("WeatherRepository", "每小时预报请求异常", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * 同时获取当前天气和每小时预报
     */
    suspend fun getWeatherData(
        latitude: Double, 
        longitude: Double
    ): Pair<Result<CurrentWeather>, Result<HourlyForecast>> {
        return coroutineScope {
            val currentWeatherDeferred = async { getCurrentWeather(latitude, longitude) }
            val hourlyForecastDeferred = async { getHourlyForecast(latitude, longitude, 8) }
            
            Pair(
                currentWeatherDeferred.await(),
                hourlyForecastDeferred.await()
            )
        }
    }
}
