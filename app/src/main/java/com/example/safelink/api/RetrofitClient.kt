package com.example.safelink.api

import android.util.Log
import com.example.safelink.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://safe-link-indol.vercel.app/api/"

    private var sessionManager: SessionManager? = null

    fun initialize(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
        Log.d("RetrofitClient", "✅ SessionManager initialisé")
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")

            sessionManager?.getToken()?.let { token ->
                if (token.isNotEmpty()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                    Log.d("AuthInterceptor", "🔑 Token ajouté: ${token.take(20)}...")
                }
            }

            val request = requestBuilder.build()
            Log.d("HTTP_OUT", "➡️ ${request.method} ${request.url}")

            val response = chain.proceed(request)
            Log.d("HTTP_IN", "⬅️ ${response.code} ${response.message}")

            response
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}