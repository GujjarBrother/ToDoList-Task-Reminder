package com.sag.todo.list.task.reminder.models

data class NavigationDrawer(
    val heading: String? = null,
    var image: Int? = null,
    var title: String? = null,
    val subTitle: String? = null,
    val isSwitch: Boolean = false,
    var isSwitchChecked: Boolean = false
)