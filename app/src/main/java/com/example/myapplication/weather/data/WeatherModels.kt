package com.example.myapplication.weather.data

import com.google.gson.annotations.SerializedName

/**
 * 天气位置信息
 */
data class Location(
    val latitude: Double,
    val longitude: Double
)

/**
 * 天气描述信息 - 对应后端WeatherDescription.java
 */
data class WeatherDescription(
    val text: String,
    val languageCode: String
)

/**
 * 天气状况 - 对应后端WeatherCondition.java
 */
data class WeatherCondition(
    val type: String,
    val description: WeatherDescription,
    val iconBaseUri: String
)

/**
 * 温度信息 - 对应后端Temperature.java
 */
data class Temperature(
    val degrees: Double,
    val unit: String = "CELSIUS"
) {
    // 兼容性属性，保持原有API不变
    val value: Double get() = degrees
}

/**
 * 湿度信息
 */
data class Humidity(
    val value: Int,
    val unit: String = "%"
)

/**
 * 风向信息 - 对应后端WindDirection.java
 */
data class WindDirection(
    val degrees: Int,
    val cardinal: String
)

/**
 * 风速信息 - 对应后端WindSpeed.java
 */
data class WindSpeed(
    val value: Double,
    val unit: String
)

/**
 * 风力信息 - 对应后端Wind.java
 */
data class Wind(
    val direction: WindDirection,
    val speed: WindSpeed,
    val gust: WindSpeed? = null
)

/**
 * 当前天气状况 - 对应后端CurrentConditionsResponse.java
 */
data class CurrentWeather(
    @SerializedName("weatherCondition")
    val condition: WeatherCondition,
    val temperature: Temperature,
    @SerializedName("relativeHumidity")
    val humidity: Int, // 后端返回的是Integer，不是Humidity对象
    val wind: Wind,
    @SerializedName("airPressure")
    val pressure: AirPressure, // 后端返回的是AirPressure对象，不是Double
    val visibility: Visibility, // 后端返回的是Visibility对象，不是Double
    val uvIndex: Int,
    val dewPoint: Temperature
) {
    // 为了保持向后兼容，提供计算属性
    val location: Location get() = Location(0.0, 0.0) // 临时占位符
}

/**
 * 气压信息 - 对应后端AirPressure.java
 */
data class AirPressure(
    val meanSeaLevelMillibars: Double
)

/**
 * 能见度信息 - 对应后端Visibility.java
 */
data class Visibility(
    val distance: Double,
    val unit: String
)

/**
 * 显示时间信息 - 对应后端DisplayDateTime.java
 */
data class DisplayDateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hours: Int,
    val utcOffset: String
)

/**
 * 降水概率 - 对应后端PrecipitationProbability.java
 */
data class PrecipitationProbability(
    val percent: Int,
    val type: String
)

/**
 * 降水信息 - 对应后端Precipitation.java
 */
data class PrecipitationInfo(
    val probability: PrecipitationProbability
)

/**
 * 每小时天气预报项 - 对应后端HourlyForecast.java
 */
data class HourlyForecastItem(
    val displayDateTime: DisplayDateTime,
    @SerializedName("weatherCondition")
    val condition: WeatherCondition,
    val temperature: Temperature,
    @SerializedName("relativeHumidity")
    val humidity: Int,
    val precipitation: PrecipitationInfo
) {
    // 计算属性：格式化的时间字符串
    val time: String
        get() = String.format("%02d:%02d", displayDateTime.hours, 0)
    
    // 计算属性：降水概率
    val precipitationProbability: Int
        get() = precipitation.probability.percent
}

/**
 * 每小时天气预报 - 对应后端HourlyForecastResponse.java
 */
data class HourlyForecast(
    @SerializedName("forecastHours")
    val forecasts: List<HourlyForecastItem>,
    val timeZone: TimeZoneInfo? = null,
    val nextPageToken: String? = null
)

/**
 * 时区信息
 */
data class TimeZoneInfo(
    val id: String
)
