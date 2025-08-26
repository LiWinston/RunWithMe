package com.example.myapplication.weather.ui

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.weather.data.CurrentWeather
import com.example.myapplication.weather.data.HourlyForecast
import com.example.myapplication.weather.utils.WeatherIconUtils
import kotlinx.coroutines.launch

/**
 * 可扩展的天气组件
 * 默认显示简化信息，点击后展开显示详细信息
 */
class ExpandableWeatherWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var isExpanded = false
    private var currentWeather: CurrentWeather? = null
    private var hourlyForecast: HourlyForecast? = null
    
    // 简化视图组件
    private lateinit var compactCard: View
    private lateinit var compactWeatherIcon: ImageView
    private lateinit var compactLocationText: TextView
    private lateinit var compactWeatherDescription: TextView
    private lateinit var compactTemperatureText: TextView
    private lateinit var compactHumidityText: TextView
    private lateinit var compactWindText: TextView
    
    // 展开视图组件
    private lateinit var expandedCard: View
    private lateinit var expandedWeatherIcon: ImageView
    private lateinit var expandedLocationText: TextView
    private lateinit var expandedWeatherDescription: TextView
    private lateinit var expandedTemperatureText: TextView
    private lateinit var expandedHumidityValue: TextView
    private lateinit var expandedWindValue: TextView
    private lateinit var expandedPressureValue: TextView
    private lateinit var expandedVisibilityValue: TextView
    private lateinit var expandedUvIndexValue: TextView
    private lateinit var expandedDewPointValue: TextView
    private lateinit var hourlyForecastRecycler: RecyclerView
    
    // 每小时预报适配器
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    
    init {
        setupViews()
        setupClickListeners()
    }
    
    private fun setupViews() {
        // 添加简化视图
        compactCard = LayoutInflater.from(context).inflate(R.layout.weather_card_compact, this, false)
        addView(compactCard)
        
        // 添加展开视图（初始隐藏）
        expandedCard = LayoutInflater.from(context).inflate(R.layout.weather_card_expanded, this, false)
        expandedCard.visibility = View.GONE
        addView(expandedCard)
        
        // 初始化简化视图组件
        initCompactViews()
        
        // 初始化展开视图组件
        initExpandedViews()
        
        // 设置每小时预报RecyclerView
        setupHourlyForecastRecycler()
    }
    
    private fun initCompactViews() {
        compactWeatherIcon = compactCard.findViewById(R.id.weather_icon)
        compactLocationText = compactCard.findViewById(R.id.location_text)
        compactWeatherDescription = compactCard.findViewById(R.id.weather_description)
        compactTemperatureText = compactCard.findViewById(R.id.temperature_text)
        compactHumidityText = compactCard.findViewById(R.id.humidity_text)
        compactWindText = compactCard.findViewById(R.id.wind_text)
    }
    
    private fun initExpandedViews() {
        expandedWeatherIcon = expandedCard.findViewById(R.id.weather_icon_expanded)
        expandedLocationText = expandedCard.findViewById(R.id.location_text_expanded)
        expandedWeatherDescription = expandedCard.findViewById(R.id.weather_description_expanded)
        expandedTemperatureText = expandedCard.findViewById(R.id.temperature_text_expanded)
        expandedHumidityValue = expandedCard.findViewById(R.id.humidity_value_expanded)
        expandedWindValue = expandedCard.findViewById(R.id.wind_value_expanded)
        expandedPressureValue = expandedCard.findViewById(R.id.pressure_value_expanded)
        expandedVisibilityValue = expandedCard.findViewById(R.id.visibility_value_expanded)
        expandedUvIndexValue = expandedCard.findViewById(R.id.uv_index_value_expanded)
        expandedDewPointValue = expandedCard.findViewById(R.id.dew_point_value_expanded)
        hourlyForecastRecycler = expandedCard.findViewById(R.id.hourly_forecast_recycler)
    }
    
    private fun setupHourlyForecastRecycler() {
        hourlyForecastAdapter = HourlyForecastAdapter()
        hourlyForecastRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyForecastAdapter
        }
    }
    
    private fun setupClickListeners() {
        compactCard.setOnClickListener {
            expandCard()
        }
        
        expandedCard.setOnClickListener {
            collapseCard()
        }
    }
    
    /**
     * 展开卡片
     */
    private fun expandCard() {
        if (isExpanded) return
        
        isExpanded = true
        
        // 淡入展开视图
        expandedCard.alpha = 0f
        expandedCard.visibility = View.VISIBLE
        expandedCard.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
        
        // 淡出简化视图
        compactCard.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                compactCard.visibility = View.GONE
            }
            .start()
        
        // 更新展开视图数据
        updateExpandedView()
    }
    
    /**
     * 收起卡片
     */
    private fun collapseCard() {
        if (!isExpanded) return
        
        isExpanded = false
        
        // 淡入简化视图
        compactCard.alpha = 0f
        compactCard.visibility = View.VISIBLE
        compactCard.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
        
        // 淡出展开视图
        expandedCard.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                expandedCard.visibility = View.GONE
            }
            .start()
    }
    
    /**
     * 更新天气数据
     */
    fun updateWeatherData(currentWeather: CurrentWeather, hourlyForecast: HourlyForecast? = null) {
        this.currentWeather = currentWeather
        this.hourlyForecast = hourlyForecast
        
        updateCompactView()
        if (isExpanded) {
            updateExpandedView()
        }
    }
    
    private fun updateCompactView() {
        currentWeather?.let { weather ->
            // 加载天气图标
            val iconUrl = WeatherIconUtils.getIconUrl(weather.condition.iconBaseUri, false)
            Glide.with(context)
                .load(iconUrl)
                .into(compactWeatherIcon)
            
            // 更新文本信息
            compactLocationText.text = "墨尔本, 卡尔顿" // 可以根据实际位置获取
            compactWeatherDescription.text = WeatherIconUtils.getWeatherDescription(weather.condition.type)
            compactTemperatureText.text = "${weather.temperature.degrees.toInt()}°"
            compactHumidityText.text = "湿度 ${weather.humidity}%"
            compactWindText.text = "风速 ${weather.wind.speed.value.toInt()}${weather.wind.speed.unit}"
        }
    }
    
    private fun updateExpandedView() {
        currentWeather?.let { weather ->
            // 加载天气图标
            val iconUrl = WeatherIconUtils.getIconUrl(weather.condition.iconBaseUri, false)
            Glide.with(context)
                .load(iconUrl)
                .into(expandedWeatherIcon)
            
            // 更新主要信息
            expandedLocationText.text = "墨尔本, 卡尔顿"
            expandedWeatherDescription.text = "${WeatherIconUtils.getWeatherDescription(weather.condition.type)}，适合户外运动"
            expandedTemperatureText.text = "${weather.temperature.degrees.toInt()}°"
            
            // 更新详细信息
            expandedHumidityValue.text = "${weather.humidity}%"
            expandedWindValue.text = "${weather.wind.speed.value.toInt()} ${weather.wind.speed.unit} ${weather.wind.direction.cardinal}"
            expandedPressureValue.text = "${weather.pressure.meanSeaLevelMillibars.toInt()} hPa"
            expandedVisibilityValue.text = "${weather.visibility.distance.toInt()} ${weather.visibility.unit}"
            expandedUvIndexValue.text = "${weather.uvIndex} ${getUvDescription(weather.uvIndex)}"
            expandedDewPointValue.text = "${weather.dewPoint.degrees.toInt()}°C"
        }
        
        // 更新每小时预报
        hourlyForecast?.let { forecast ->
            android.util.Log.d("ExpandableWeatherWidget", "更新每小时预报数据，预报条目数: ${forecast.forecasts.size}")
            forecast.forecasts.forEachIndexed { index, item ->
                android.util.Log.d("ExpandableWeatherWidget", "预报[$index]: 时间=${item.time}, 温度=${item.temperature.value}°, 降水=${item.precipitationProbability}%")
            }
            hourlyForecastAdapter.updateData(forecast.forecasts)
        } ?: run {
            android.util.Log.w("ExpandableWeatherWidget", "每小时预报数据为空")
        }
    }
    
    private fun getUvDescription(uvIndex: Int): String {
        return when {
            uvIndex <= 2 -> "低"
            uvIndex <= 5 -> "中等"
            uvIndex <= 7 -> "高"
            uvIndex <= 10 -> "很高"
            else -> "极高"
        }
    }
    
    /**
     * 强制展开（用于外部调用）
     */
    fun expand() {
        expandCard()
    }
    
    /**
     * 强制收起（用于外部调用）
     */
    fun collapse() {
        collapseCard()
    }
    
    /**
     * 获取当前展开状态
     */
    fun isExpanded(): Boolean = isExpanded
}
