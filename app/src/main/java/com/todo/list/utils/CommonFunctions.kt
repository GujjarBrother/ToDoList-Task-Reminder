package com.todo.list.utils

import android.app.Activity
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.todo.list.R

object CommonFunctions {

    var isSomethingChanged = false
    const val DEFAULT_CATEGORY = 0
    const val PERSONAL_CATEGORY = 1
    const val WORK_CATEGORY = 2

    const val TASKS_TAB = 0
    const val COMPLETED_TAB = 1

    fun changeStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    fun applyAnimation(activity: Activity): Animation =
        AnimationUtils.loadAnimation(activity, R.anim.fab_and_rate_us_image_view_animation)

    fun keepActivityOn(activity: Activity) =
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

    fun makeFullScreenActivity(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}