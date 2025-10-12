package com.example.myapplication.gemini

/**
 * Configuration for Gemini API
 * Store your API key here or load from a secure source
 */
object GeminiConfig {
    /**
     * Your Gemini API Key
     * IMPORTANT: In production, use a more secure method to store the API key
     * (e.g., Android Keystore, environment variables, or a backend service)
     * 
     * Get your API key from: https://makersuite.google.com/app/apikey
     */
    const val API_KEY = "AIzaSyAfQUF5gSOUTVUjC1lNI_0zYFFKyfRfoBc"
    
    /**
     * Check if API key is configured
     */
    fun isConfigured(): Boolean {
        return API_KEY != "YOUR_GEMINI_API_KEY_HERE" && API_KEY.isNotBlank()
    }
}

