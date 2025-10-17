package com.example.myapplication.weather.utils

/**
 * 天气图标工具类
 * 根据Google Maps Weather API文档处理图标URL
 */
object WeatherIconUtils {
    
    /**
     * 获取天气图标URL
     * @param iconBaseUri API返回的图标基础URI
     * @param isDarkMode 是否为深色模式
     * @param format 图标格式 (.svg 或 .png)
     * @return 完整的图标URL
     */
    fun getIconUrl(
        iconBaseUri: String,
        isDarkMode: Boolean = false,
        format: IconFormat = IconFormat.PNG // 默认使用PNG格式
    ): String {
        val suffix = if (isDarkMode) "_dark${format.extension}" else format.extension
        return "$iconBaseUri$suffix"
    }
    
    /**
     * 图标格式枚举
     */
    enum class IconFormat(val extension: String) {
        SVG(".svg"),
        PNG(".png")
    }
    
    /**
     * Get weather condition description in English
     */
    fun getWeatherDescription(conditionType: String): String {
        return when (conditionType) {
            "CLEAR" -> "Clear"
            "MOSTLY_CLEAR" -> "Mostly Clear"
            "PARTLY_CLOUDY" -> "Partly Cloudy"
            "MOSTLY_CLOUDY" -> "Mostly Cloudy"
            "CLOUDY" -> "Cloudy"
            "WINDY" -> "Windy"
            "WIND_AND_RAIN" -> "Wind and Rain"
            "LIGHT_RAIN_SHOWERS" -> "Light Rain Showers"
            "CHANCE_OF_SHOWERS" -> "Chance of Showers"
            "SCATTERED_SHOWERS" -> "Scattered Showers"
            "RAIN_SHOWERS" -> "Rain Showers"
            "HEAVY_RAIN_SHOWERS" -> "Heavy Rain Showers"
            "LIGHT_TO_MODERATE_RAIN" -> "Light to Moderate Rain"
            "MODERATE_TO_HEAVY_RAIN" -> "Moderate to Heavy Rain"
            "RAIN" -> "Rain"
            "LIGHT_RAIN" -> "Light Rain"
            "HEAVY_RAIN" -> "Heavy Rain"
            "RAIN_PERIODICALLY_HEAVY" -> "Rain, Periodically Heavy"
            "LIGHT_SNOW_SHOWERS" -> "Light Snow Showers"
            "CHANCE_OF_SNOW_SHOWERS" -> "Chance of Snow Showers"
            "SCATTERED_SNOW_SHOWERS" -> "Scattered Snow Showers"
            "SNOW_SHOWERS" -> "Snow Showers"
            "HEAVY_SNOW_SHOWERS" -> "Heavy Snow Showers"
            "LIGHT_TO_MODERATE_SNOW" -> "Light to Moderate Snow"
            "MODERATE_TO_HEAVY_SNOW" -> "Moderate to Heavy Snow"
            "SNOW" -> "Snow"
            "LIGHT_SNOW" -> "Light Snow"
            "HEAVY_SNOW" -> "Heavy Snow"
            "SNOWSTORM" -> "Snowstorm"
            "SNOW_PERIODICALLY_HEAVY" -> "Snow, Periodically Heavy"
            "HEAVY_SNOW_STORM" -> "Heavy Snow Storm"
            "BLOWING_SNOW" -> "Blowing Snow"
            "RAIN_AND_SNOW" -> "Rain and Snow"
            "HAIL" -> "Hail"
            "HAIL_SHOWERS" -> "Hail Showers"
            "THUNDERSTORM" -> "Thunderstorm"
            "THUNDERSHOWER" -> "Thundershower"
            "LIGHT_THUNDERSTORM_RAIN" -> "Light Thunderstorm Rain"
            "SCATTERED_THUNDERSTORMS" -> "Scattered Thunderstorms"
            "HEAVY_THUNDERSTORM" -> "Heavy Thunderstorm"
            else -> "Unknown"
        }
    }
}
