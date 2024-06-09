package com.todo.list.application

import android.app.Application
import android.graphics.Typeface
import com.todo.list.utils.Prefs

class Application : Application() {

//    Here, We Declare All Reference Variable's...
    companion object {
        lateinit var typeface: Typeface
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        super.onCreate()

//        Here, We Create An Instance Of 'Typeface' Class...
        typeface = Typeface.createFromAsset(assets, "font/Cabin Medium.ttf")

//        Here, We Create An Instance Of 'Prefs' Class...
        prefs = Prefs(getSharedPreferences("TO_DO_LIST_APP_PREFS", MODE_PRIVATE))
    }
}
