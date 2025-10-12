package com.example.myapplication.gemini

/**
 * Usage examples for Gemini API integration
 * 
 * This file contains code examples showing how to use the Gemini API service
 * in different scenarios. Copy and adapt these examples for your use cases.
 */

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch

/**
 * Example 1: Get weather-based exercise advice
 * 
 * This is already implemented in HomeFragment.kt
 * Location: HomeFragment.generateExerciseAdvice()
 */
fun example1_weatherAdvice(
    lifecycleScope: LifecycleCoroutineScope,
    geminiService: GeminiApiService
) {
    lifecycleScope.launch {
        // Weather data from your weather service
        val temperature = 22.0
        val condition = "Partly Cloudy"
        val windSpeed = 15.0
        val humidity = 65
        
        val result = geminiService.getWeatherBasedAdvice(
            temperature = temperature,
            weatherCondition = condition,
            windSpeed = windSpeed,
            humidity = humidity
        )
        
        result.fold(
            onSuccess = { advice ->
                // Display the advice in your UI
                Log.d("GeminiExample", "Advice: $advice")
                // textView.text = advice
            },
            onFailure = { error ->
                Log.e("GeminiExample", "Error getting advice", error)
                // Show error message to user
            }
        )
    }
}

/**
 * Example 2: Get workout history analysis
 * 
 * Use this in your History fragment or activity
 */
fun example2_workoutHistoryAdvice(
    lifecycleScope: LifecycleCoroutineScope,
    geminiService: GeminiApiService
) {
    lifecycleScope.launch {
        // Prepare workout data summary
        val workoutData = WorkoutHistorySummary(
            totalDistance = 25.5,  // km in last 7 days
            averagePace = 5.5,     // min/km
            totalWorkouts = 4,
            averageDuration = 45,   // minutes
            frequency = 4           // times per week
        )
        
        val result = geminiService.getWorkoutHistoryAdvice(workoutData)
        
        result.fold(
            onSuccess = { advice ->
                Log.d("GeminiExample", "Workout Analysis: $advice")
                // Display in your history view
            },
            onFailure = { error ->
                Log.e("GeminiExample", "Error analyzing workout history", error)
            }
        )
    }
}

/**
 * Example 3: Get motivational message after achievement
 * 
 * Use this when user completes a significant milestone
 */
fun example3_motivationalMessage(
    lifecycleScope: LifecycleCoroutineScope,
    geminiService: GeminiApiService
) {
    lifecycleScope.launch {
        val achievementType = "New Distance Record"
        val value = "10 km in 50 minutes"
        
        val prompt = PromptTemplate.getMotivationalMessage(
            achievementType = achievementType,
            value = value
        )
        
        val result = geminiService.generateContent(prompt)
        
        result.fold(
            onSuccess = { message ->
                Log.d("GeminiExample", "Motivation: $message")
                // Show in a dialog or toast
            },
            onFailure = { error ->
                Log.e("GeminiExample", "Error generating message", error)
            }
        )
    }
}

/**
 * Example 4: Custom prompt for new feature
 * 
 * Create your own prompt for a completely new feature
 */
fun example4_customPrompt(
    lifecycleScope: LifecycleCoroutineScope,
    geminiService: GeminiApiService
) {
    lifecycleScope.launch {
        // Create a custom prompt
        val customPrompt = """
            You are a nutrition expert for athletes. Based on the following workout data,
            suggest a post-workout meal.
            
            Workout Details:
            - Duration: 60 minutes
            - Distance: 10 km
            - Intensity: Moderate
            - Weather: Hot and humid
            
            Provide a brief meal suggestion (2-3 sentences) that focuses on:
            1. Recovery and rehydration
            2. Protein and carbohydrate balance
            
            Keep it practical and easy to prepare.
        """.trimIndent()
        
        val result = geminiService.generateContent(customPrompt)
        
        result.fold(
            onSuccess = { suggestion ->
                Log.d("GeminiExample", "Meal Suggestion: $suggestion")
                // Display to user
            },
            onFailure = { error ->
                Log.e("GeminiExample", "Error getting suggestion", error)
            }
        )
    }
}

/**
 * Example 5: Adding a new template to PromptTemplate.kt
 * 
 * When you need to reuse a prompt, add it to PromptTemplate object:
 * 
 * In PromptTemplate.kt, add:
 * 
 * fun getInjuryPreventionAdvice(
 *     recentPain: String,
 *     workoutFrequency: Int,
 *     averageDistance: Double
 * ): String {
 *     return """
 *         You are a sports medicine expert. Based on the following information,
 *         provide injury prevention advice.
 *         
 *         User Information:
 *         - Recent Pain/Discomfort: $recentPain
 *         - Weekly Workout Frequency: $workoutFrequency times
 *         - Average Distance: $averageDistance km
 *         
 *         Provide brief advice (2-3 sentences) on:
 *         1. Whether they should rest or continue
 *         2. Preventive measures to take
 *         
 *         Be cautious and prioritize health.
 *     """.trimIndent()
 * }
 * 
 * Then use it like:
 */
fun example5_injuryPreventionAdvice(
    lifecycleScope: LifecycleCoroutineScope,
    geminiService: GeminiApiService
) {
    lifecycleScope.launch {
        // This is a hypothetical example - you'd need to add the template first
        val prompt = """
            You are a sports medicine expert. Based on the following information,
            provide injury prevention advice.
            
            User Information:
            - Recent Pain/Discomfort: Knee discomfort after long runs
            - Weekly Workout Frequency: 5 times
            - Average Distance: 8.5 km
            
            Provide brief advice (2-3 sentences) on:
            1. Whether they should rest or continue
            2. Preventive measures to take
            
            Be cautious and prioritize health.
        """.trimIndent()
        
        val result = geminiService.generateContent(prompt)
        
        result.fold(
            onSuccess = { advice ->
                Log.d("GeminiExample", "Injury Prevention: $advice")
            },
            onFailure = { error ->
                Log.e("GeminiExample", "Error getting advice", error)
            }
        )
    }
}

/**
 * Best Practices:
 * 
 * 1. Always check if API is configured:
 *    if (!GeminiConfig.isConfigured()) { 
 *        // Show error message
 *        return 
 *    }
 * 
 * 2. Show loading state before API call:
 *    progressBar.visibility = View.VISIBLE
 *    
 * 3. Hide loading state after result:
 *    progressBar.visibility = View.GONE
 *    
 * 4. Handle both success and failure:
 *    result.fold(onSuccess = {...}, onFailure = {...})
 *    
 * 5. Check fragment/activity lifecycle:
 *    if (!isAdded || view == null) return@launch
 *    
 * 6. Use meaningful log tags:
 *    Log.d("YourFeature", "message")
 *    
 * 7. Keep prompts concise to reduce latency and cost
 * 
 * 8. Consider caching responses for repeated queries
 * 
 * 9. Add error messages that help users understand what went wrong
 * 
 * 10. Test with different scenarios and edge cases
 */

