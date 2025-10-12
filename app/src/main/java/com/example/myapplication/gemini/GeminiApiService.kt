package com.example.myapplication.gemini

import android.util.Log
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiApiService(private val apiKey: String) {

    companion object {
        private const val TAG = "GeminiApiService"
        private const val MODEL_NAME = "gemini-2.5-flash-lite"
    }

    // 旧：GenerativeModel ——> 新：Client
    private val client: Client by lazy {
        Client.builder()
            .apiKey(apiKey)   // 也可不传，改用环境变量 GOOGLE_API_KEY
            .build()
    }

    suspend fun generateContent(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sending request to Gemini model: $MODEL_NAME")
            Log.d(TAG, "Prompt: ${prompt.take(100)}...")

            // 配置可为 null；需要的话再用 builder 传入可选项
            val config = GenerateContentConfig.builder()
                .maxOutputTokens(500)
                .build()

            val response = client.models.generateContent(MODEL_NAME, prompt, config)
            val text = response.text()

            if (text.isNullOrBlank()) {
                Result.failure(Exception("Empty response from AI"))
            } else {
                Log.d(TAG, "Response: ${text.take(50)}...")
                Result.success(text.trim())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API error", e)
            Result.failure(e)
        }
    }

    suspend fun getWeatherBasedAdvice(
        temperature: Double,
        weatherCondition: String,
        windSpeed: Double,
        humidity: Int
    ): Result<String> {
        val prompt = PromptTemplate.getWeatherBasedAdvice(
            temperature = temperature,
            weatherCondition = weatherCondition,
            windSpeed = windSpeed,
            humidity = humidity
        )
        return generateContent(prompt)
    }

    suspend fun getWorkoutHistoryAdvice(
        workoutData: WorkoutHistorySummary
    ): Result<String> {
        val prompt = PromptTemplate.getWorkoutHistoryAdvice(workoutData)
        return generateContent(prompt)
    }
    
    suspend fun getPeriodBasedAdvice(
        periodData: PeriodWorkoutData
    ): Result<String> {
        val prompt = PromptTemplate.getPeriodBasedAdvice(periodData)
        return generateContent(prompt)
    }
}
