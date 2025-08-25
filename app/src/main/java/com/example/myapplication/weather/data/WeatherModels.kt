package com.example.myapplication.weather.data

/**
 * 天气位置信息
 */
data class Location(
    val latitude: Double,
    val longitude: Double
)

/**
 * 天气状况
 */
data class WeatherCondition(
    val type: String,
    val description: String,
    val iconBaseUri: String
)

/**
 * 温度信息
 */
data class Temperature(
    val value: Double,
    val unit: String = "°C"
)

/**
 * 湿度信息
 */
data class Humidity(
    val value: Int,
    val unit: String = "%"
)

/**
 * 风力信息
 */
data class Wind(
    val speed: Double,
    val direction: String,
    val unit: String = "km/h"
)

/**
 * 当前天气状况
 */
data class CurrentWeather(
    val location: Location,
    val condition: WeatherCondition,
    val temperature: Temperature,
    val humidity: Humidity,
    val wind: Wind,
    val pressure: Double,
    val visibility: Double,
    val uvIndex: Int,
    val dewPoint: Double
)

/**
 * 每小时天气预报项
 */
data class HourlyForecastItem(
    val time: String,
    val condition: WeatherCondition,
    val temperature: Temperature,
    val humidity: Humidity,
    val precipitationProbability: Int
)

/**
 * 每小时天气预报
 */
data class HourlyForecast(
    val location: Location,
    val forecasts: List<HourlyForecastItem>
)
