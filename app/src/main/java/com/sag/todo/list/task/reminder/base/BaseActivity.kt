package com.sag.todo.list.task.reminder.base

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.core.utils.Prefs
import com.example.localization.LocalizationHelper
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.activities.AppLanguageActivity
import com.sag.todo.list.task.reminder.activities.FeedbackActivity
import com.sag.todo.list.task.reminder.utils.AppConstants.getColorResource
import com.sag.todo.list.task.reminder.utils.controllers.InternetController
import com.sag.todo.list.task.reminder.utils.controllers.SoftKeyboardVisibilityController
import com.sag.todo.list.task.reminder.utils.toasts.ToastController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

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
    protected open val isApplyEdgeToEdgeForDashBoardActivity = true

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        if (isApplyEdgeToEdgeForDashBoardActivity) {
            val defaultColor = this.getColorResource(R.color.defaultColor)
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(defaultColor),
                navigationBarStyle = SystemBarStyle.dark(defaultColor)
            )
        }
        super.onCreate(savedInstanceState)

        activityContext = this

        if (activityContext is AppLanguageActivity || activityContext is FeedbackActivity) {
            findViewById<View>(android.R.id.content).enableEdgeToEdge(true)
        } else {
            findViewById<View>(android.R.id.content).enableEdgeToEdge()
        }

        textInputLayoutDarkModeStrokeColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(
                // Color when focused
                activityContext.getColorResource(R.color.defaultColor),
                // Color when not focused
                activityContext.getColorResource(R.color.subColor)
        ))

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleActivitiesBackPressed()
            }
        })
    }

    protected fun View.enableEdgeToEdge(isForIme: Boolean = false) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottomInset = if (isForIme) {
                val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
                maxOf(systemBars.bottom, ime.bottom)
            } else {
                systemBars.bottom
            }
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset)
            insets
        }
    }

    protected fun callBackPressed() = handleActivitiesBackPressed()

    abstract fun handleActivitiesBackPressed()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalizationHelper.applyLanguage(newBase))
    }
}