package com.sag.todo.list.task.reminder.base

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.controllers.InternetController
import com.sag.todo.list.task.reminder.controllers.SoftKeyboardVisibilityController
import com.sag.todo.list.task.reminder.controllers.localization.Localization
import com.sag.todo.list.task.reminder.toasts.ToastController
import com.sag.todo.list.task.reminder.core.utils.Prefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {

    protected lateinit var activityContext: AppCompatActivity

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var internetController: InternetController

    @Inject
    lateinit var softKeyboardVisibilityController: SoftKeyboardVisibilityController

    @Inject
    lateinit var toastController: ToastController

    protected lateinit var textInputLayoutDarkModeStrokeColor: ColorStateList

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)

        activityContext = this

//        changeStatusBarColor(activityContext, ContextCompat.getColor(activityContext, R.color.defaultColor))

        textInputLayoutDarkModeStrokeColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(
                // Color when focused
                ContextCompat.getColor(activityContext, R.color.defaultColor),
                // Color when not focused
                ContextCompat.getColor(activityContext, R.color.subColor))
        )
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Localization.onAttach(newBase))
    }
}
