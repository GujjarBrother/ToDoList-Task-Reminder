package com.todo.list.application

import android.app.Application
import com.todo.list.utils.CommonFunctions.changeAppMode
import com.todo.list.utils.Prefs
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    @Inject
    lateinit var prefs: Prefs

    override fun onCreate() {
        super.onCreate()

        changeAppMode(prefs.isDarkModeEnable)
    }
}
