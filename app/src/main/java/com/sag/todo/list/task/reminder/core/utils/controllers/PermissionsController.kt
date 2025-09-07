package com.sag.todo.list.task.reminder.core.utils.controllers

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.showExplainingWhyNotificationPermissionIsRequiredDialog

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
                    postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                shouldShowRequestPermissionRationale(context, Manifest.permission.POST_NOTIFICATIONS) ->
                    showExplainingWhyNotificationPermissionIsRequiredDialog(context) {
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
}