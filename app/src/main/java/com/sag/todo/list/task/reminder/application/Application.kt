package com.sag.todo.list.task.reminder.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import com.google.firebase.FirebaseApp
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.controllers.localization.Localization
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.TASK_REMINDER_NOTIFICATION_CHANNEL_ID
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.TASK_REMINDER_NOTIFICATION_CHANNEL_NAME
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.changeAppMode
import com.sag.todo.list.task.reminder.core.utils.Prefs
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.core.net.toUri

@HiltAndroidApp
class Application : Application() {

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Exception) {
        }
        changeAppMode(prefs.isDarkModeEnable)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = "android.resource://$packageName/${R.raw.reminder_notification_default_sound}".toUri()
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val taskReminderNotificationChannel = NotificationChannel(
                TASK_REMINDER_NOTIFICATION_CHANNEL_ID,
                TASK_REMINDER_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            taskReminderNotificationChannel.apply {
                name = TASK_REMINDER_NOTIFICATION_CHANNEL_NAME
                description = "This channel is for remind about your task's."
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(taskReminderNotificationChannel)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(Localization.onAttach(base))
    }
}
