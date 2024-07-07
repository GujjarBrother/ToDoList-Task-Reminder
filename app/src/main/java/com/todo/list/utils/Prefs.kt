package com.todo.list.utils

import android.content.SharedPreferences

class Prefs(private val toDoListSharedPreferences: SharedPreferences) {

    var category: Int
        get() = toDoListSharedPreferences.getInt("category", -1)
        set(value) = toDoListSharedPreferences.edit().putInt("category", value).apply()

    var rememberMe: Boolean
        get() = toDoListSharedPreferences.getBoolean("rememberMe", false)
        set(value) = toDoListSharedPreferences.edit().putBoolean("rememberMe", value).apply()

    var isUserSignInOrSignOutValue: Boolean
        get() = toDoListSharedPreferences.getBoolean("signInSignOut", false)
        set(value) = toDoListSharedPreferences.edit().putBoolean("signInSignOut", value).apply()

    var colorSchemeValue: Int
        get() = toDoListSharedPreferences.getInt("colorScheme", 0)
        set(value) = toDoListSharedPreferences.edit().putInt("colorScheme", value).apply()

    var allTasksStyleValue: Boolean
        get() = toDoListSharedPreferences.getBoolean("allTasksStyle", false)
        set(value) = toDoListSharedPreferences.edit().putBoolean("allTasksStyle", value).apply()

    var completedTasksStyleValue: Boolean
        get() = toDoListSharedPreferences.getBoolean("completedTasksStyle", false)
        set(value) = toDoListSharedPreferences.edit().putBoolean("completedTasksStyle", value).apply()

    var textSizeValue: Int
        get() = toDoListSharedPreferences.getInt("textSize", 14)
        set(value) = toDoListSharedPreferences.edit().putInt("textSize", value).apply()

    var isDarkModeEnable: Boolean
        get() = toDoListSharedPreferences.getBoolean("lightORDarkMode", false)
        set(value) = toDoListSharedPreferences.edit().putBoolean("lightORDarkMode", value).apply()

    fun saveUserCredentials(
        emailOrUserName: String, password: String, gender: String, securityQuestion: String,
        securityAnswer: String, check: Boolean
    ) {
        val userCredentialsEditor = toDoListSharedPreferences.edit()
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
            val emailOrUserName = toDoListSharedPreferences.getString("emailOrUserName", null).toString()
            val password = toDoListSharedPreferences.getString("password", null).toString()
            val gender = toDoListSharedPreferences.getString("gender", null).toString()
            val securityQuestion = toDoListSharedPreferences.getString("securityQuestion", "").toString()
            val securityAnswer = toDoListSharedPreferences.getString("securityAnswer", null).toString()
            val check = toDoListSharedPreferences.getBoolean("check", false).toString()
            return arrayOf(emailOrUserName, password, gender, securityQuestion, securityAnswer, check)
        }

    fun saveAllTasksSortingValues(aboveSortedValue: Int, belowSortedValue: Int) {
        val toDosSortingEditor = toDoListSharedPreferences.edit()
        toDosSortingEditor.putInt("allTasksAboveSorting", aboveSortedValue)
        toDosSortingEditor.putInt("allTasksBelowSorting", belowSortedValue)
        toDosSortingEditor.apply()
    }

    val allTasksSortingValues: IntArray
        get() {
            val aboveSortedValue = toDoListSharedPreferences.getInt("allTasksAboveSorting", 1)
            val belowSortedValue = toDoListSharedPreferences.getInt("allTasksBelowSorting", 7)
            return intArrayOf(aboveSortedValue, belowSortedValue)
        }

    fun saveCompletedTasksSortingValues(aboveSortedValue: Int, belowSortedValue: Int) {
        val toDosSortingEditor = toDoListSharedPreferences.edit()
        toDosSortingEditor.putInt("completedTasksAboveSorting", aboveSortedValue)
        toDosSortingEditor.putInt("completedTasksBelowSorting", belowSortedValue)
        toDosSortingEditor.apply()
    }

    val completedTasksSortingValues: IntArray
        get() {
            val aboveSortedValue = toDoListSharedPreferences.getInt("completedTasksAboveSorting", 1)
            val belowSortedValue = toDoListSharedPreferences.getInt("completedTasksBelowSorting", 7)
            return intArrayOf(aboveSortedValue, belowSortedValue)
        }
}