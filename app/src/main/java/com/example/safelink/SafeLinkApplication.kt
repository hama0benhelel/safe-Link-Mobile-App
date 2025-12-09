package com.example.safelink

import android.app.Application
import com.example.safelink.api.RetrofitClient
import com.example.safelink.utils.SessionManager

class SafeLinkApplication : Application() {

    companion object {
        private lateinit var instance: SafeLinkApplication

        val sessionManager: SessionManager by lazy {
            SessionManager(instance.applicationContext)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        RetrofitClient.initialize(sessionManager)
    }
}