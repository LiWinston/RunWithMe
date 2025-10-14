package com.example.myapplication.record

import com.example.myapplication.landr.RetrofitClient

object RetrofitClient {
    // 复用带鉴权的 Retrofit 实例，确保所有记录/历史接口携带 Bearer token
    val api: RecordApi by lazy { RetrofitClient.create(RecordApi::class.java) }
}
