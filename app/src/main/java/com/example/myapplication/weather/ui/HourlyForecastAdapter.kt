package com.example.myapplication.weather.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.weather.data.HourlyForecastItem
import com.example.myapplication.weather.utils.WeatherIconUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 每小时天气预报适配器
 */
class HourlyForecastAdapter : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {
    
    private var forecastData: List<HourlyForecastItem> = emptyList()
    
    fun updateData(newData: List<HourlyForecastItem>) {
        forecastData = newData
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hourly_forecast_item, parent, false)
        return HourlyViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(forecastData[position])
    }
    
    override fun getItemCount(): Int = forecastData.size
    
    class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hourTime: TextView = itemView.findViewById(R.id.hour_time)
        private val hourWeatherIcon: ImageView = itemView.findViewById(R.id.hour_weather_icon)
        private val hourTemperature: TextView = itemView.findViewById(R.id.hour_temperature)
        private val hourFeelsLike: TextView = itemView.findViewById(R.id.hour_feels_like)
        private val hourPrecipitation: TextView = itemView.findViewById(R.id.hour_precipitation)
        private val hourHumidity: TextView = itemView.findViewById(R.id.hour_humidity)
        
        fun bind(item: HourlyForecastItem) {
            // 格式化时间
            hourTime.text = item.time
            
            // 加载天气图标
            val iconUrl = WeatherIconUtils.getIconUrl(item.condition.iconBaseUri, false)
            Glide.with(itemView.context)
                .load(iconUrl)
                .into(hourWeatherIcon)
            
            // 设置温度
            hourTemperature.text = "${item.temperature.degrees.toInt()}°"
            
            // 设置体感温度 (使用当前温度作为占位符)
            hourFeelsLike.text = "Feels ${item.temperature.degrees.toInt()}°"

            // 设置降水概率
            hourPrecipitation.text = "${item.precipitationProbability}%"

            // 设置湿度
            hourHumidity.text = "Humidity ${item.humidity}%"
        }
        
        private fun formatTime(timeString: String): String {
            return try {
                // 这里需要根据实际的时间格式进行解析
                // 假设是ISO格式: "2024-01-01T14:00:00Z"
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = inputFormat.parse(timeString)
                date?.let { outputFormat.format(it) } ?: timeString
            } catch (e: Exception) {
                // 如果解析失败，尝试提取小时部分
                if (timeString.contains("T") && timeString.length >= 16) {
                    timeString.substring(11, 16) // 提取 "14:00" 部分
                } else {
                    timeString
                }
            }
        }
    }
}
