package com.sag.todo.list.task.reminder.application

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.sag.todo.list.task.reminder.controllers.localization.Localization
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeAppMode
import com.sag.todo.list.task.reminder.utils.Prefs
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    @Inject
    lateinit var prefs: Prefs

    override fun onCreate() {
        super.onCreate()

        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Exception) {
        }
        changeAppMode(prefs.isDarkModeEnable)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(Localization.onAttach(base))
    }
}
