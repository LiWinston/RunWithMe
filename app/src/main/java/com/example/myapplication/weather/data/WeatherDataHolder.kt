package com.example.myapplication.weather.data

/**
 * 天气数据持有者（单例）
 * 用于在不同界面之间共享天气数据，避免重复请求导致数据不一致
 */
object WeatherDataHolder {
    private var currentWeather: CurrentWeather? = null
    private var hourlyForecast: HourlyForecast? = null

    /**
     * 保存天气数据
     */
    fun setWeatherData(weather: CurrentWeather, forecast: HourlyForecast?) {
        currentWeather = weather
        hourlyForecast = forecast
    }

    /**
     * 获取当前天气数据
     */
    fun getCurrentWeather(): CurrentWeather? = currentWeather

    /**
     * 获取每小时预报数据
     */
    fun getHourlyForecast(): HourlyForecast? = hourlyForecast

    /**
     * 清除所有数据
     */
    fun clear() {
        currentWeather = null
        hourlyForecast = null
    }
}
