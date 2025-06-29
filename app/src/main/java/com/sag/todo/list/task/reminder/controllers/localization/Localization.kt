package com.sag.todo.list.task.reminder.controllers.localization

import android.content.Context
import com.sag.todo.list.task.reminder.utils.Prefs
import java.util.Locale

object Localization  {
    fun onAttach(context: Context): Context {
        val prefs = Prefs(context)
        prefs.selectedLanguageCode?.let {
            return applyLanguage(context, prefs)
        } ?: return applyLanguage(context, prefs)
    }

    private fun applyLanguage(context: Context, prefs: Prefs): Context {
        val locale = Locale(prefs.selectedLanguageCode ?: "en")
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}