package com.example.safelink.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        sharedPref.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPref.getString("auth_token", null)
    }

    fun saveUserInfo(userId: String, userName: String) {
        sharedPref.edit().apply {
            putString("user_id", userId)
            putString("user_name", userName)
            apply()
        }
    }

    fun getUserInfo(): Pair<String?, String?> {
        return Pair(
            sharedPref.getString("user_id", null),
            sharedPref.getString("user_name", null)
        )
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    fun clearAuthData() {
        sharedPref.edit().clear().apply()
    }
}