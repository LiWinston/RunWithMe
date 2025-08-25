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
        private val hourPrecipitation: TextView = itemView.findViewById(R.id.hour_precipitation)
        
        fun bind(item: HourlyForecastItem) {
            // 格式化时间（假设time是ISO格式字符串）
            hourTime.text = formatTime(item.time)
            
            // 加载天气图标
            val iconUrl = WeatherIconUtils.getIconUrl(item.condition.iconBaseUri, false)
            Glide.with(itemView.context)
                .load(iconUrl)
                .into(hourWeatherIcon)
            
            // 设置温度
            hourTemperature.text = "${item.temperature.value.toInt()}°"
            
            // 设置降水概率
            hourPrecipitation.text = "${item.precipitationProbability}%"
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
