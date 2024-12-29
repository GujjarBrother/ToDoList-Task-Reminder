package com.todo.list.application

import android.app.Application
import com.todo.list.utils.CommonFunctions.changeAppMode
import com.todo.list.utils.Prefs

class Application : Application() {

//    Here, We Declare All Reference Variable's...
    companion object {
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(getSharedPreferences("TO_DO_LIST_APP_PREFS", MODE_PRIVATE))
        changeAppMode(prefs.isDarkModeEnable)
    }
}
