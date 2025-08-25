package com.example.myapplication.weather.api

import com.example.myapplication.weather.data.CurrentWeather
import com.example.myapplication.weather.data.HourlyForecast
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 天气API接口
 */
interface WeatherApiService {
    
    /**
     * 获取当前天气状况
     */
    @GET("api/weather/current")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("units") units: String = "METRIC"
    ): Response<ApiResult<CurrentWeather>>
    
    /**
     * 获取每小时天气预报
     */
    @GET("api/weather/hourly")
    suspend fun getHourlyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("units") units: String = "METRIC",
        @Query("hours") hours: Int = 24
    ): Response<ApiResult<HourlyForecast>>
}

/**
 * API返回结果包装类
 */
data class ApiResult<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    val isSuccess: Boolean get() = code == 0
}
