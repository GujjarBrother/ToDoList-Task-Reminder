package com.todo.list.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.todo.list.R
import com.todo.list.activities.PrivacyPolicyActivity
import com.todo.list.db.ToDosDatabase
import com.todo.list.repositories.TasksRepo
import com.todo.list.viewModels.TasksViewModel
import com.todo.list.viewModels.TasksViewModelFactory
import es.dmoral.toasty.Toasty

object CommonFunctions {

    var isSomethingChanged = MutableLiveData(false)

    fun getViewModel(context: FragmentActivity) =
        ViewModelProvider(context, TasksViewModelFactory(TasksRepo(ToDosDatabase.getDatabase(context).dao())))[TasksViewModel::class.java]

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

    fun View.changeVisibility(visibilityStatus: Int) {
        when(visibilityStatus) {
            0 -> this.visibility = GONE
            1 -> this.visibility = VISIBLE
            2 -> this.visibility = INVISIBLE
        }
    }

    fun changeAppMode(isDark: Boolean = false) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}