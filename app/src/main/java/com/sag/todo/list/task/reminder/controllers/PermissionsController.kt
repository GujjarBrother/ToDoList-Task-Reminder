package com.sag.todo.list.task.reminder.controllers

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.databinding.ExplainingWhyPermissionIsRequiredLayoutBinding

class PermissionsController(
    private val context: Activity,
    private val postNotificationPermissionLauncher: ActivityResultLauncher<String>,
    private val alarmManager: AlarmManager
) : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkScheduleExactAlarmPermission()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                checkPostNotificationPermission()
        }
    }

    private fun checkPostNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                }

                shouldShowRequestPermissionRationale(context, Manifest.permission.POST_NOTIFICATIONS) ->
                    showExplainingWhyNotificationPermissionRequiredDialog {
                        postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                else -> postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val scheduleExactAlarmIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                scheduleExactAlarmIntent.data = "package:${context.packageName}".toUri()
                context.startActivity(scheduleExactAlarmIntent)
            }
        }
    }

    private fun showExplainingWhyNotificationPermissionRequiredDialog(allowCallback: () -> Unit) {
        val whyPermissionIsRequiredLayoutBinding = ExplainingWhyPermissionIsRequiredLayoutBinding.inflate(context.layoutInflater)

        val alertDialogBuilder = AlertDialog.Builder(context)
        with(alertDialogBuilder) {
            setView(whyPermissionIsRequiredLayoutBinding.root)
            setCancelable(true)
        }
        val alertDialog = alertDialogBuilder.create()
        if (!context.isFinishing && !context.isDestroyed && !alertDialog.isShowing) {
            with(alertDialog) {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                window?.setWindowAnimations(R.style.dialogBoxesAnimation)
                show()
            }
        }

        with(whyPermissionIsRequiredLayoutBinding) {
            denyBtn.setOnClickListener {
                if (!context.isFinishing && !context.isDestroyed) {
                    alertDialog.dismiss()
                }
            }

            allowBtn.setOnClickListener {
                if (!context.isFinishing && !context.isDestroyed) {
                    alertDialog.dismiss()
                }
                allowCallback.invoke()
            }
        }
    }
}