package com.thekirankumarv.newsync.chat.presentation.utils

import android.content.Context

object PreferenceManager {
    private const val PREF_NAME = "newsync_prefs"
    private const val LANGUAGE_KEY = "language_key"

    fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, language).apply()
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, "en") ?: "en"
    }
}
