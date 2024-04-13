package com.todo.list.listeners

interface StartAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener {
    fun goAhead(startAndStopFABAnimation: Int = 0, isLightAndDarkMode: Boolean = false,
                isFromNavigationDrawer: Boolean = false)
}