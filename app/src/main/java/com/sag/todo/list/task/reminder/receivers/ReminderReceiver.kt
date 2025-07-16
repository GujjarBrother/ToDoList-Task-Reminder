package com.sag.todo.list.task.reminder.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.activities.ToDoTaskDetailActivity
import com.sag.todo.list.task.reminder.db.ToDoTask
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent?) {
        var toDoTask: ToDoTask? = null
        intent?.let {
            toDoTask = it.getSerializableExtra("TASK") as ToDoTask
        }
        showReminderNotification(context, toDoTask)
    }

    private fun showReminderNotification(context: Context, toDoTask: ToDoTask?) {
        val channelID = "Reminder_Channel"
        val channelName = "Channel For Reminder Notification."
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = "android.resource://${context.packageName}/${R.raw.reminder_notification_default_sound}".toUri()
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminder Tasks Channel"
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationCompatBuilder = NotificationCompat.Builder(context, channelID)
            .setContentTitle(toDoTask?.title)
            .setContentText(toDoTask?.description)
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(
                context, 0,
                Intent(context, ToDoTaskDetailActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("taskDetail", toDoTask)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        notificationManager.notify(1, notificationCompatBuilder)
    }
}