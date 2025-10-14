package com.example.myapplication.landr

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private lateinit var tokenManager: TokenManager
    private var retrofitInstance: Retrofit? = null

    fun init(context: Context) {
        tokenManager = TokenManager.getInstance(context)
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val accessToken = tokenManager.getAccessToken()

        // 如果没有 token 或者是登录/注册请求，直接发送原始请求
        if (accessToken == null || originalRequest.url.encodedPath.contains("/auth/login") 
            || originalRequest.url.encodedPath.contains("/auth/register")) {
            return@Interceptor chain.proceed(originalRequest)
        }

        // 添加 Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        val response = chain.proceed(authenticatedRequest)

        // 如果 token 过期 (401)，尝试刷新
        if (response.code == 401) {
            response.close()
            
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                try {
                    // 尝试刷新 token
                    val refreshResponse = runBlocking {
                        api.refreshToken("Bearer $refreshToken")
                    }
                    
                    if (refreshResponse.isSuccessful && refreshResponse.body()?.status == 1) {
                        val loginData = refreshResponse.body()?.data
                        if (loginData != null) {
                            // 保存新的 tokens
                            tokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                            
                            // 使用新 token 重新发送请求
                            val newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer ${loginData.accessToken}")
                                .build()
                            return@Interceptor chain.proceed(newRequest)
                        }
                    }
                } catch (e: Exception) {
                    // 刷新失败，清除 tokens
                    tokenManager.clearTokens()
                }
            }
            
            // 刷新失败，返回原始的 401 响应
            return@Interceptor chain.proceed(authenticatedRequest)
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private fun retrofit(): Retrofit {
        val cached = retrofitInstance
        if (cached != null) return cached
        val created = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitInstance = created
        return created
    }

    val api: ApiService by lazy { retrofit().create(ApiService::class.java) }

    fun <T> create(service: Class<T>): T = retrofit().create(service)
}