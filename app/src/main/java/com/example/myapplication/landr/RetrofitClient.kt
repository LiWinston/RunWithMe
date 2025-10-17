package com.example.myapplication.landr

import android.content.Context
import kotlinx.coroutines.runBlocking
import com.google.gson.Gson
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
        if (
            accessToken == null ||
            originalRequest.url.encodedPath.contains("/auth/login") ||
            originalRequest.url.encodedPath.contains("/auth/register") ||
            originalRequest.url.encodedPath.contains("/auth/refresh")
        ) {
            return@Interceptor chain.proceed(originalRequest)
        }

        // 添加 Authorization header（若原请求未携带）
        val hasAuthHeader = originalRequest.header("Authorization")?.isNotEmpty() == true
        val authenticatedRequest = if (hasAuthHeader) originalRequest else originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        val response = chain.proceed(authenticatedRequest)

        // 如果 token 过期：
        // 1) HTTP 401
        // 2) HTTP 200 但业务码 code==401（后端统一200+业务码）
        val shouldAttemptBusinessRefresh: Boolean = try {
            if (response.code != 200) false else {
                val peek = response.peekBody(1024 * 1024).string()
                val basic = Gson().fromJson(peek, BasicResult::class.java)
                basic?.code == 401
            }
        } catch (e: Exception) { false }

        if (response.code == 401 || shouldAttemptBusinessRefresh) {
            response.close()
            
            // 防止重复刷新重试造成循环
            if (originalRequest.header("X-Retry-After-Refresh") == "true") {
                return@Interceptor chain.proceed(authenticatedRequest)
            }

            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                try {
                    // 尝试刷新 token
                    val refreshResponse = runBlocking {
                        api.refreshToken("Bearer $refreshToken")
                    }
                    
                    // 后端约定：code == 0 表示成功
                    if (refreshResponse.isSuccessful && refreshResponse.body()?.status == 0) {
                        val loginData = refreshResponse.body()?.data
                        if (loginData != null) {
                            // 保存新的 tokens
                            tokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                            
                            // 使用新 token 重新发送请求
                            val newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer ${loginData.accessToken}")
                                .header("X-Retry-After-Refresh", "true")
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

    // 基础Result解析（仅取code与message用于拦截器判断）
    private data class BasicResult(
        val code: Int?,
        val message: String?
    )

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