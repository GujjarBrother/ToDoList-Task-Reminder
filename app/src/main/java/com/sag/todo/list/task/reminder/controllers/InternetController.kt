package com.sag.todo.list.task.reminder.controllers

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject

class InternetController @Inject constructor(private val connectivityManager: ConnectivityManager) {
    val isInternetConnected: Boolean
        get() {
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (networkCapabilities != null && networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return true
                }
            }
            return false
        }
}