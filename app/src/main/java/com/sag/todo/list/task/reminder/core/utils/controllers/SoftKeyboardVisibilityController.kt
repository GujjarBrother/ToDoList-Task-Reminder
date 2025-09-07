package com.sag.todo.list.task.reminder.core.utils.controllers

import android.view.View
import android.view.inputmethod.InputMethodManager
import javax.inject.Inject

class SoftKeyboardVisibilityController @Inject constructor(private val inputMethodManager: InputMethodManager) {

    fun showSoftKeyboard() = inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    fun hideSoftKeyboard(view: View) = inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}