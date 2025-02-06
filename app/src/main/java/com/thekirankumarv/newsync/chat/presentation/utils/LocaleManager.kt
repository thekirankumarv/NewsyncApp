package com.thekirankumarv.newsync.chat.presentation.utils

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LocaleManager {

    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList(locale))
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

