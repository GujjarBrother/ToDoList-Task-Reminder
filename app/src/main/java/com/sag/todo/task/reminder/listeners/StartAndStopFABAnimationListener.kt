package com.sag.todo.task.reminder.listeners

import com.sag.todo.task.reminder.enums.StartStopFAB

interface StartAndStopFABAnimationListener {
    fun startAndStopFABAnimation(startStopFAB: StartStopFAB)
    fun search(query: String)
}