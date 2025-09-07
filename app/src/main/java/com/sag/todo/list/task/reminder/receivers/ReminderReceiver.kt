package com.sag.todo.list.task.reminder.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.activities.ToDoTaskDetailActivity
import com.sag.todo.list.task.reminder.db.ToDoTask
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.TASK_REMINDER_NOTIFICATION_CHANNEL_ID
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
        val notificationTapIntent = Intent(context, ToDoTaskDetailActivity::class.java)
            .putExtra("taskDetail", toDoTask)
        val notificationTapPI = PendingIntent.getActivity(
            context, 0, notificationTapIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notificationCompatBuilder = NotificationCompat.Builder(context, TASK_REMINDER_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(toDoTask?.title)
            .setContentText(toDoTask?.description)
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(notificationTapPI)
            .setOnlyAlertOnce(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound("android.resource://${context.packageName}/${R.raw.reminder_notification_default_sound}".toUri())
            .build()
        notificationManager.notify(toDoTask?.id ?: 0, notificationCompatBuilder)
    }
}