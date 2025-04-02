package com.sag.todo.list.task.reminder.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.activities.PrivacyPolicyActivity
import com.sag.todo.list.task.reminder.customFonts.PopUpMenuItemsTypefaceAndColor
import com.sag.todo.list.task.reminder.db.ToDosDatabase
import com.sag.todo.list.task.reminder.repositories.TasksRepo
import com.sag.todo.list.task.reminder.viewModels.TasksViewModel
import com.sag.todo.list.task.reminder.viewModels.TasksViewModelFactory

object CommonFunctions {

    var isSomethingChanged = MutableLiveData(false)

    fun getViewModel(context: FragmentActivity) =
        ViewModelProvider(context, TasksViewModelFactory(TasksRepo(ToDosDatabase.getDatabase(context).dao())))[TasksViewModel::class.java]

    fun openGoogleAppStore(activity: Activity) {
        val openGoogleAppStoreIntent = Intent()
        with(openGoogleAppStoreIntent) {
            action = Intent.ACTION_VIEW
            data = "https://play.google.com/store/apps/developer?id=SAG+Inc.".toUri()
            activity.startActivity(this)
        }
    }

    fun openPrivacyPolicyActivity(activity: Activity, isInternetConnectedORNot: Boolean) {
        if (isInternetConnectedORNot) {
            activity.startActivity(Intent(activity, PrivacyPolicyActivity::class.java))
        } else {
            Toast.makeText(activity, activity.getString(R.string.check_your_internet_connection_toast_text), Toast.LENGTH_LONG).show()
        }
    }

    fun openAppInPlayStore(activity: Activity, appPackageName: String) {
        val openAppInPlayStoreIntent = Intent()
        with(openAppInPlayStoreIntent) {
            action = Intent.ACTION_VIEW
            data = "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
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

    fun applyCustomFontAndColorToPopupMenuItemsText(
        context: Activity,
        menuItem: MenuItem,
        @ColorInt customColor: Int
    ) {
        val customFont = Typeface.createFromAsset(context.assets, "fonts/Cabin Medium.ttf")
        val spannableString = SpannableString(menuItem.title)
        spannableString.setSpan(
            PopUpMenuItemsTypefaceAndColor("", customFont, customColor),
            0,
            spannableString.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        menuItem.title = spannableString
    }
}