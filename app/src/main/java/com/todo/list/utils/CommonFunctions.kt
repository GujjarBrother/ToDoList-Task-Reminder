package com.todo.list.utils

import android.app.Activity
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.todo.list.R
import com.todo.list.activities.PrivacyPolicyActivity
import es.dmoral.toasty.Toasty

object CommonFunctions {

    var isSomethingChanged = false
    const val DEFAULT_CATEGORY = 0
    const val PERSONAL_CATEGORY = 1
    const val WORK_CATEGORY = 2

    const val TASKS_TAB = 0
    const val COMPLETED_TAB = 1

    fun openGoogleAppStore(activity: Activity) {
        val openGoogleAppStoreIntent = Intent()
        with(openGoogleAppStoreIntent) {
            action = Intent.ACTION_VIEW
            data = Uri.parse("https://play.google.com/store/apps/developer?id=SAG+Inc.")
            activity.startActivity(this)
        }
    }

    fun openPrivacyPolicyActivity(activity: Activity, isInternetConnectedORNot: Boolean) {
        if (isInternetConnectedORNot) {
            activity.startActivity(Intent(activity, PrivacyPolicyActivity::class.java))
        } else {
            Toasty.error(activity, activity.getString(R.string.check_your_internet_connection_toast_text),
                Toasty.LENGTH_LONG).show()
        }
    }

    fun openAppInPlayStore(activity: Activity, appPackageName: String) {
        val openAppInPlayStoreIntent = Intent()
        with(openAppInPlayStoreIntent) {
            action = Intent.ACTION_VIEW
            data = Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            activity.startActivity(this)
        }
    }

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