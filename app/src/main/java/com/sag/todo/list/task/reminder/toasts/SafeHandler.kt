package com.sag.todo.list.task.reminder.toasts

import android.os.Handler
import android.os.Looper
import android.os.Message

class SafeHandler(private val originalHandler: Handler): Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
        try {
            originalHandler.handleMessage(msg)
        } catch (_: Exception) {
        }
    }
}