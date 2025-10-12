package com.example.myapplication.gemini

/**
 * Template manager for different AI prompt scenarios
 * Easily extensible for various use cases
 */
object PromptTemplate {
    
    /**
     * Generate exercise advice based on weather conditions
     * @param temperature Temperature in Celsius
     * @param weatherCondition Weather condition description (e.g., "sunny", "rainy")
     * @param windSpeed Wind speed in km/h
     * @param humidity Humidity percentage
     */
    fun getWeatherBasedAdvice(
        temperature: Double,
        weatherCondition: String,
        windSpeed: Double,
        humidity: Int
    ): String {
        return """
            You are a professional fitness coach. Based on the following weather conditions, provide brief exercise advice in exactly 2 sentences.
            
            Weather Information:
            - Temperature: ${temperature}°C
            - Condition: $weatherCondition
            - Wind Speed: ${windSpeed} km/h
            - Humidity: ${humidity}%
            
            Requirements:
            1. First sentence: Recommend the best type of outdoor exercise for these conditions
            2. Second sentence: Provide one practical preparation tip
            
            Keep it concise, friendly, and actionable. Do not use bullet points or numbering in the response.
        """.trimIndent()
    }
    
    /**
     * Generate analysis and recommendations based on workout history
     * This template is prepared for future use
     * @param workoutData Summary of recent workout statistics
     */
    fun getWorkoutHistoryAdvice(workoutData: WorkoutHistorySummary): String {
        return """
            You are a professional fitness coach analyzing workout history. Based on the following data, provide personalized advice in 2-3 concise sentences.
            
            Workout History Summary:
            - Total Distance (Last 7 Days): ${workoutData.totalDistance} km
            - Average Pace: ${workoutData.averagePace} min/km
            - Total Workouts: ${workoutData.totalWorkouts}
            - Average Duration: ${workoutData.averageDuration} minutes
            - Workout Frequency: ${workoutData.frequency} times per week
            
            Requirements:
            1. Identify the user's current fitness level and progress
            2. Provide specific, actionable recommendations for improvement
            3. Be encouraging and motivating
            
            Keep the tone friendly and supportive.
        """.trimIndent()
    }
    
    /**
     * Generate motivational message based on recent achievement
     * This template is prepared for future use
     */
    fun getMotivationalMessage(achievementType: String, value: String): String {
        return """
            You are a supportive fitness coach. The user just achieved: $achievementType with value: $value.
            
            Generate a brief, enthusiastic congratulatory message (1-2 sentences) that:
            1. Celebrates their achievement
            2. Encourages them to keep going
            
            Be genuine, specific, and motivating.
        """.trimIndent()
    }
}

/**
 * Data class for workout history summary
 * Used for future workout analysis feature
 */
data class WorkoutHistorySummary(
    val totalDistance: Double,
    val averagePace: Double,
    val totalWorkouts: Int,
    val averageDuration: Int,
    val frequency: Int
)

