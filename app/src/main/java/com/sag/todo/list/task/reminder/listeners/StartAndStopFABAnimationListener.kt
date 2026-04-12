package com.sag.todo.list.task.reminder.listeners

import com.sag.todo.list.task.reminder.enums.StartStopFAB

interface StartAndStopFABAnimationListener {
    fun startAndStopFABAnimation(startStopFAB: StartStopFAB)
    fun search(query: String)
}