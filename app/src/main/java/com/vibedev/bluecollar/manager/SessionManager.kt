package com.vibedev.bluecollar.manager

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val PREF_NAME = "UserSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_AUTH_TOKEN = "authToken"
        private const val IS_PROFILE_COMPLETED = "isProfileCompleted"
    }


    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun setProfileCompleted(completed: Boolean) {
        editor.putBoolean(IS_PROFILE_COMPLETED, completed)
    }

    fun isProfileCompleted(): Boolean {
        return prefs.getBoolean(IS_PROFILE_COMPLETED, false)
    }


    fun deleteAuthToken(){
        editor.remove(KEY_AUTH_TOKEN)
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }


    val isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

}