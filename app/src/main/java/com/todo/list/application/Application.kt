package com.todo.list.application

import android.app.Application
import android.graphics.Typeface
import androidx.room.Room.databaseBuilder
import com.google.android.gms.ads.MobileAds
import com.todo.list.db.ToDosDatabase
import com.todo.list.utils.Prefs

class Application : Application() {

//    Here, We Declare All Reference Variable's...
    companion object {
        lateinit var typeface: Typeface
        lateinit var prefs: Prefs
        lateinit var toDosDatabase: ToDosDatabase
    }

    override fun onCreate() {
        super.onCreate()

//        Here, We Create An Instance Of 'Typeface' Class...
        typeface = Typeface.createFromAsset(assets, "font/Cabin Medium.ttf")

//        Here, We Create An Instance Of 'Prefs' Class...
        prefs = Prefs(getSharedPreferences("TO_DO_LIST_APP_PREFS", MODE_PRIVATE))

//        Here, We Create An Instance Of 'Database'...
        toDosDatabase = databaseBuilder(this, ToDosDatabase::class.java, "ToDos_Tasks_Database")
                .allowMainThreadQueries()
                .build()
    }
}
