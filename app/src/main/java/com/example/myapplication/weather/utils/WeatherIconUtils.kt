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
        format: IconFormat = IconFormat.SVG
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
     * 获取天气状况的中文描述
     */
    fun getWeatherDescription(conditionType: String): String {
        return when (conditionType) {
            "CLEAR" -> "晴朗"
            "MOSTLY_CLEAR" -> "大部晴朗"
            "PARTLY_CLOUDY" -> "局部多云"
            "MOSTLY_CLOUDY" -> "大部多云"
            "CLOUDY" -> "阴天"
            "WINDY" -> "大风"
            "WIND_AND_RAIN" -> "风雨交加"
            "LIGHT_RAIN_SHOWERS" -> "小阵雨"
            "CHANCE_OF_SHOWERS" -> "可能有阵雨"
            "SCATTERED_SHOWERS" -> "零星阵雨"
            "RAIN_SHOWERS" -> "阵雨"
            "HEAVY_RAIN_SHOWERS" -> "大阵雨"
            "LIGHT_TO_MODERATE_RAIN" -> "小到中雨"
            "MODERATE_TO_HEAVY_RAIN" -> "中到大雨"
            "RAIN" -> "雨"
            "LIGHT_RAIN" -> "小雨"
            "HEAVY_RAIN" -> "大雨"
            "RAIN_PERIODICALLY_HEAVY" -> "雨，偶有大雨"
            "LIGHT_SNOW_SHOWERS" -> "小阵雪"
            "CHANCE_OF_SNOW_SHOWERS" -> "可能有阵雪"
            "SCATTERED_SNOW_SHOWERS" -> "零星阵雪"
            "SNOW_SHOWERS" -> "阵雪"
            "HEAVY_SNOW_SHOWERS" -> "大阵雪"
            "LIGHT_TO_MODERATE_SNOW" -> "小到中雪"
            "MODERATE_TO_HEAVY_SNOW" -> "中到大雪"
            "SNOW" -> "雪"
            "LIGHT_SNOW" -> "小雪"
            "HEAVY_SNOW" -> "大雪"
            "SNOWSTORM" -> "暴雪"
            "SNOW_PERIODICALLY_HEAVY" -> "雪，偶有大雪"
            "HEAVY_SNOW_STORM" -> "强暴雪"
            "BLOWING_SNOW" -> "风雪"
            "RAIN_AND_SNOW" -> "雨夹雪"
            "HAIL" -> "冰雹"
            "HAIL_SHOWERS" -> "阵性冰雹"
            "THUNDERSTORM" -> "雷暴"
            "THUNDERSHOWER" -> "雷阵雨"
            "LIGHT_THUNDERSTORM_RAIN" -> "轻雷阵雨"
            "SCATTERED_THUNDERSTORMS" -> "零星雷暴"
            "HEAVY_THUNDERSTORM" -> "强雷暴"
            else -> "未知"
        }
    }
}
