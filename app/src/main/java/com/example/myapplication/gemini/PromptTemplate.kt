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
    
    /**
     * Generate personalized advice based on workout data in a specific period
     * @param periodData Workout data summary for today/week/month
     */
    fun getPeriodBasedAdvice(periodData: PeriodWorkoutData): String {
        return """
            You are a professional fitness coach. Analyze the user's workout performance and provide personalized advice.
            
            User Profile:
            - Fitness Goal: ${periodData.userFitnessGoal ?: "Not specified"}
            - Fitness Level: ${periodData.userFitnessLevel ?: "Not specified"}
            - Age: ${periodData.userAge ?: "Not specified"}
            - Gender: ${periodData.userGender ?: "Not specified"}
            - Height: ${periodData.userHeight?.let { "%.1f cm".format(it) } ?: "Not specified"}
            - Weight: ${periodData.userWeight?.let { "%.1f kg".format(it) } ?: "Not specified"}
            
            ${periodData.periodName} Performance:
            - Total Distance: ${periodData.totalDistance} km
            - Total Duration: ${periodData.totalDuration} minutes
            - Total Workouts: ${periodData.totalWorkouts}
            - Calories Burned: ${periodData.totalCalories} kcal
            - Average Pace: ${if (periodData.avgPace > 0) "%.2f".format(periodData.avgPace) + " min/km" else "N/A"}
            
            Requirements:
            1. Consider their fitness goal and current fitness level
            2. Analyze if their ${periodData.periodName.lowercase()} performance aligns with their goals
            3. Provide 2-3 specific, actionable recommendations for improvement
            4. Be encouraging and motivating
            
            Keep the response concise (3-4 sentences maximum) and supportive.
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

/**
 * Data class for period-based workout data (today/week/month)
 * Used for generating personalized advice
 */
data class PeriodWorkoutData(
    val periodName: String, // "Today", "This Week", "This Month"
    val totalDistance: Double, // km
    val totalDuration: Int, // minutes
    val totalWorkouts: Int,
    val totalCalories: Double, // kcal
    val avgPace: Double, // min/km
    // User personal information
    val userFitnessGoal: String?,
    val userFitnessLevel: String?,
    val userAge: Int?,
    val userGender: String?,
    val userHeight: Double?,
    val userWeight: Double?
)

