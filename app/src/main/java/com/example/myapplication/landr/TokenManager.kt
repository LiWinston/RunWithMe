package com.example.myapplication.landr

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "token_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_ID_KEY = "user_id"
        private const val USERNAME_KEY = "username"
        
        @Volatile
        private var INSTANCE: TokenManager? = null
        
        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    fun saveUserInfo(userId: Long, username: String) {
        sharedPreferences.edit()
            .putLong(USER_ID_KEY, userId)
            .putString(USERNAME_KEY, username)
            .apply()
    }

    fun getUserId(): Long {
        return sharedPreferences.getLong(USER_ID_KEY, -1)
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(USERNAME_KEY, null)
    }

    fun clearTokens() {
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .remove(USER_ID_KEY)
            .remove(USERNAME_KEY)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
