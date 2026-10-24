package com.sag.todo.task.reminder.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppLanguage(
    val flag: Int,
    val languageName: String,
    val languageNameInNative: String,
    val languageCode: String,
    var isSelected: Boolean = false
) : Parcelable