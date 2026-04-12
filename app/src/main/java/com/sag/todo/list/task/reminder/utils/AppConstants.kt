package com.sag.todo.list.task.reminder.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.sag.todo.list.task.reminder.BuildConfig
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.activities.PrivacyPolicyActivity
import com.sag.todo.list.task.reminder.databinding.ExplainingWhyPermissionIsRequiredLayoutBinding
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.customFonts.PopUpMenuItemsTypefaceAndColor
import com.sag.todo.list.task.reminder.utils.toasts.ToastController

object AppConstants {

    const val TASK_REMINDER_NOTIFICATION_CHANNEL_ID = "TASK_REMINDER_NOTIFICATION_CHANNEL_ID"
    const val TASK_REMINDER_NOTIFICATION_CHANNEL_NAME = "Task Reminder"

    var isSomethingChanged = MutableLiveData(false)

    val isDebug: Boolean
        get() = BuildConfig.DEBUG

    fun openGoogleAppStore(activity: Activity) {
        val openGoogleAppStoreIntent = Intent()
        openGoogleAppStoreIntent.apply {
            action = Intent.ACTION_VIEW
            data = "https://play.google.com/store/apps/developer?id=SAG+Inc.".toUri()
            activity.startActivity(this)
        }
    }

    fun openPrivacyPolicyActivity(
        activity: Activity,
        isInternetConnectedORNot: Boolean,
        toastController: ToastController
    ) {
        if (isInternetConnectedORNot)
            activity.startActivity(Intent(activity, PrivacyPolicyActivity::class.java))
        else
            toastController.showToast(activity, activity.getString(com.example.core.R.string.check_your_internet_connection_toast_text), false)
    }

    fun openAppInPlayStore(activity: Activity, appPackageName: String) {
        val openAppInPlayStoreIntent = Intent()
        openAppInPlayStoreIntent.apply {
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

    fun keepActivityOn(activity: Activity) =
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

    fun View.changeVisibility(visibilityStatus: Visibility) {
        when (visibilityStatus) {
            Visibility.GONE -> this.visibility = GONE
            Visibility.VISIBLE -> this.visibility = VISIBLE
            Visibility.INVISIBLE -> this.visibility = INVISIBLE
        }
    }

    fun Context.getColorResource(color: Int) = ContextCompat.getColor(this, color)

    fun Context.getDrawableResource(drawable: Int) = ContextCompat.getDrawable(this, drawable)

    fun String.logIt(tag: String = "SAG") {
        if (isDebug) Log.d(tag, this)
    }

    fun Context.showToast(message: String, isLengthShort: Boolean = true) {
        Toast.makeText(this, message, if (isLengthShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
    }

    fun changeAppMode(isDark: Boolean = false) =
        AppCompatDelegate.setDefaultNightMode(if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

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

    fun showExplainingWhyNotificationPermissionIsRequiredDialog(context: Activity, isForOpenSettingsScreen: Boolean = false, allowOrOpenAppSettingsCallback: () -> Unit) {
        val whyPermissionIsRequiredLayoutBinding = ExplainingWhyPermissionIsRequiredLayoutBinding.inflate(context.layoutInflater)

        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.apply {
            setView(whyPermissionIsRequiredLayoutBinding.root)
            setCancelable(true)
        }
        val alertDialog = alertDialogBuilder.create()
        if (!context.isFinishing && !context.isDestroyed && !alertDialog.isShowing) {
            alertDialog.apply {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                window?.setWindowAnimations(R.style.dialogBoxesAnimation)
                show()
            }
        }

        whyPermissionIsRequiredLayoutBinding.apply {
            if (isForOpenSettingsScreen) {
                titleTV.text = context.getString(com.example.core.R.string.open_settings_text)
                descriptionTV.text = context.getString(com.example.core.R.string.allow_notifications_permission_from_settings_text)
                denyAndCancelBtn.text = context.getString(com.example.core.R.string.cancel_text)
                allowAndOpenSettingsBtn.text = context.getString(com.example.core.R.string.open_settings_text)
            }

            denyAndCancelBtn.setOnClickListener {
                if (!context.isFinishing && !context.isDestroyed) {
                    alertDialog.dismiss()
                }
            }

            allowAndOpenSettingsBtn.setOnClickListener {
                if (!context.isFinishing && !context.isDestroyed) {
                    alertDialog.dismiss()
                }
                allowOrOpenAppSettingsCallback.invoke()
            }
        }
    }
}