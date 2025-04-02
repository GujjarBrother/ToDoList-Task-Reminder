package com.sag.todo.list.task.reminder.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Prefs @Inject constructor(@ApplicationContext applicationContext: Context) {

    private val toDosListSharedPreferences = applicationContext.getSharedPreferences(
        "TO_DO_LIST_APP_PREFS", MODE_PRIVATE
    )

    var category: Int
        get() = toDosListSharedPreferences.getInt("category", -1)
        set(value) = toDosListSharedPreferences.edit().putInt("category", value).apply()

    var rememberMe: Boolean
        get() = toDosListSharedPreferences.getBoolean("rememberMe", false)
        set(value) = toDosListSharedPreferences.edit().putBoolean("rememberMe", value).apply()

    var isUserSignIn: Boolean
        get() = toDosListSharedPreferences.getBoolean("isUserSignIn", false)
        set(value) = toDosListSharedPreferences.edit().putBoolean("isUserSignIn", value).apply()

    var allTasksStyleValue: Boolean
        get() = toDosListSharedPreferences.getBoolean("allTasksStyle", false)
        set(value) = toDosListSharedPreferences.edit().putBoolean("allTasksStyle", value).apply()

    var completedTasksStyleValue: Boolean
        get() = toDosListSharedPreferences.getBoolean("completedTasksStyle", false)
        set(value) = toDosListSharedPreferences.edit().putBoolean("completedTasksStyle", value)
            .apply()

    var textSizeValue: Int
        get() = toDosListSharedPreferences.getInt("textSize", 14)
        set(value) = toDosListSharedPreferences.edit().putInt("textSize", value).apply()

    var isDarkModeEnable: Boolean
        get() = toDosListSharedPreferences.getBoolean("lightORDarkMode", false)
        set(value) = toDosListSharedPreferences.edit().putBoolean("lightORDarkMode", value).apply()

    fun saveUserCredentials(
        emailOrUserName: String, password: String, gender: String, securityQuestion: String,
        securityAnswer: String, check: Boolean
    ) {
        val userCredentialsEditor = toDosListSharedPreferences.edit()
        userCredentialsEditor.putString("emailOrUserName", emailOrUserName)
        userCredentialsEditor.putString("password", password)
        userCredentialsEditor.putString("gender", gender)
        userCredentialsEditor.putString("securityQuestion", securityQuestion)
        userCredentialsEditor.putString("securityAnswer", securityAnswer)
        userCredentialsEditor.putBoolean("check", check)
        userCredentialsEditor.apply()
    }

    val userCredentials: Array<String>
        get() {
            val emailOrUserName =
                toDosListSharedPreferences.getString("emailOrUserName", null).toString()
            val password = toDosListSharedPreferences.getString("password", null).toString()
            val gender = toDosListSharedPreferences.getString("gender", null).toString()
            val securityQuestion =
                toDosListSharedPreferences.getString("securityQuestion", "").toString()
            val securityAnswer =
                toDosListSharedPreferences.getString("securityAnswer", null).toString()
            val check = toDosListSharedPreferences.getBoolean("check", false).toString()
            return arrayOf(emailOrUserName, password, gender, securityQuestion, securityAnswer, check)
        }

    fun saveAllTasksSortingValues(aboveSortedValue: Int, belowSortedValue: Int) {
        val toDosSortingEditor = toDosListSharedPreferences.edit()
        toDosSortingEditor.putInt("allTasksAboveSorting", aboveSortedValue)
        toDosSortingEditor.putInt("allTasksBelowSorting", belowSortedValue)
        toDosSortingEditor.apply()
    }

    val allTasksSortingValues: IntArray
        get() {
            val aboveSortedValue = toDosListSharedPreferences.getInt("allTasksAboveSorting", 1)
            val belowSortedValue = toDosListSharedPreferences.getInt("allTasksBelowSorting", 7)
            return intArrayOf(aboveSortedValue, belowSortedValue)
        }

    fun saveCompletedTasksSortingValues(aboveSortedValue: Int, belowSortedValue: Int) {
        val toDosSortingEditor = toDosListSharedPreferences.edit()
        toDosSortingEditor.putInt("completedTasksAboveSorting", aboveSortedValue)
        toDosSortingEditor.putInt("completedTasksBelowSorting", belowSortedValue)
        toDosSortingEditor.apply()
    }

    val completedTasksSortingValues: IntArray
        get() {
            val aboveSortedValue =
                toDosListSharedPreferences.getInt("completedTasksAboveSorting", 1)
            val belowSortedValue =
                toDosListSharedPreferences.getInt("completedTasksBelowSorting", 7)
            return intArrayOf(aboveSortedValue, belowSortedValue)
        }
}