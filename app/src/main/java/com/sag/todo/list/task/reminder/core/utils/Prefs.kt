package com.sag.todo.list.task.reminder.core.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefs @Inject constructor(@ApplicationContext applicationContext: Context) {

    private val toDosListSharedPreferences = applicationContext.getSharedPreferences(
        "TO_DO_LIST_APP_PREFS", MODE_PRIVATE
    )

    var category: Int
        get() = toDosListSharedPreferences.getInt("category", -1)
        set(value) = toDosListSharedPreferences.edit {
            putInt("category", value)
        }

    var rememberMe: Boolean
        get() = toDosListSharedPreferences.getBoolean("rememberMe", false)
        set(value) = toDosListSharedPreferences.edit {
            putBoolean("rememberMe", value)
        }

    var isUserSignIn: Boolean
        get() = toDosListSharedPreferences.getBoolean("isUserSignIn", false)
        set(value) = toDosListSharedPreferences.edit {
            putBoolean("isUserSignIn", value)
        }

    var allTasksStyleValue: Boolean
        get() = toDosListSharedPreferences.getBoolean("allTasksStyle", false)
        set(value) = toDosListSharedPreferences.edit {
            putBoolean("allTasksStyle", value)
        }

    var completedTasksStyleValue: Boolean
        get() = toDosListSharedPreferences.getBoolean("completedTasksStyle", false)
        set(value) = toDosListSharedPreferences.edit {
            putBoolean("completedTasksStyle", value)
        }

    var textSizeValue: Int
        get() = toDosListSharedPreferences.getInt("textSize", 14)
        set(value) = toDosListSharedPreferences.edit {
            putInt("textSize", value)
        }

    var isDarkModeEnable: Boolean
        get() = toDosListSharedPreferences.getBoolean("lightORDarkMode", false)
        set(value) = toDosListSharedPreferences.edit {
            putBoolean("lightORDarkMode", value)
        }

    var selectedLanguageCode: String?
        get() = toDosListSharedPreferences.getString("selectedLanguageCode", "en")
        set(value) = toDosListSharedPreferences.edit {
            putString("selectedLanguageCode", value)
        }

    fun saveUserCredentials(
        emailOrUserName: String,
        password: String,
        gender: Int,
        selectedSecurityQuestion: Int,
        securityAnswer: String,
        check: Boolean
    ) {
        toDosListSharedPreferences.edit {
            putString("emailOrUserName", emailOrUserName)
            putString("password", password)
            putInt("gender", gender)
            putInt("selectedSecurityQuestion", selectedSecurityQuestion)
            putString("securityAnswer", securityAnswer)
            putBoolean("check", check)
        }
    }

    val userCredentials: Array<String>
        get() {
            val emailOrUserName = toDosListSharedPreferences.getString("emailOrUserName", null).toString()
            val password = toDosListSharedPreferences.getString("password", null).toString()
            val gender = toDosListSharedPreferences.getInt("gender", 0).toString()
            val securityQuestion = toDosListSharedPreferences.getInt("selectedSecurityQuestion", 0).toString()
            val securityAnswer = toDosListSharedPreferences.getString("securityAnswer", null).toString()
            val check = toDosListSharedPreferences.getBoolean("check", false).toString()
            return arrayOf(emailOrUserName, password, gender, securityQuestion, securityAnswer, check)
        }

    fun saveAllTasksSortingValues(aboveSortedValue: Int, belowSortedValue: Int) {
        toDosListSharedPreferences.edit {
            putInt("allTasksAboveSorting", aboveSortedValue)
            putInt("allTasksBelowSorting", belowSortedValue)
        }
    }

    val allTasksSortingValues: IntArray
        get() {
            val aboveSortedValue = toDosListSharedPreferences.getInt("allTasksAboveSorting", 1)
            val belowSortedValue = toDosListSharedPreferences.getInt("allTasksBelowSorting", 7)
            return intArrayOf(aboveSortedValue, belowSortedValue)
        }

    fun saveCompletedTasksSortingValues(aboveSortedValue: Int, belowSortedValue: Int) {
        toDosListSharedPreferences.edit {
            putInt("completedTasksAboveSorting", aboveSortedValue)
            putInt("completedTasksBelowSorting", belowSortedValue)
        }
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