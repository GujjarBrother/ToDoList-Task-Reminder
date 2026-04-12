package com.example.localization

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.example.core.utils.Prefs
import java.util.Locale

object LocalizationHelper  {
    fun applyLanguage(context: Context): Context {
        val prefs = Prefs(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags(prefs.selectedLanguageCode ?: "en")
            context
        } else {
            val locale = Locale.forLanguageTag(prefs.selectedLanguageCode ?: "en")
            Locale.setDefault(locale)
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            context.createConfigurationContext(configuration)
        }
    }
}