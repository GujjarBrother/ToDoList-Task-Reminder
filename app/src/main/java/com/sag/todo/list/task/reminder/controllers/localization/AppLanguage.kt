package com.sag.todo.list.task.reminder.controllers.localization

data class AppLanguage(
    val flag: Int,
    val languageName: String,
    val languageNameInNative: String,
    val languageCode: String,
    var isSelected: Boolean = false
)
