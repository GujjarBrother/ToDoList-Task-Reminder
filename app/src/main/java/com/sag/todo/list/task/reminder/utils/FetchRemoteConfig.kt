package com.sag.todo.list.task.reminder.utils

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.sag.todo.list.task.reminder.R

class FetchRemoteConfig {
    companion object {
        fun fetchRemoteConfigValues(isFetchedCallback: () -> Unit) {
            val remoteConfig = Firebase.remoteConfig
            val remoteConfigSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 30
            }
            remoteConfig.setConfigSettingsAsync(remoteConfigSettings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                    }
                    isFetchedCallback.invoke()
                }
        }
    }
}