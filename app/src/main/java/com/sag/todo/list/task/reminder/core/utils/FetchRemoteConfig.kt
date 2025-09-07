package com.sag.todo.list.task.reminder.core.utils

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.core.utils.RemoteConfigValues.IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN

class FetchRemoteConfig {
    companion object {
        fun fetchRemoteConfigValues(isFetchedCallback: () -> Unit) {
            val remoteConfig = Firebase.remoteConfig
            val remoteConfigSettings = remoteConfigSettings {
                FirebaseRemoteConfigSettings.Builder.setMinimumFetchIntervalInSeconds = 20
            }
            remoteConfig.setConfigSettingsAsync(remoteConfigSettings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN = remoteConfig.getBoolean("IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN")
                    }
                    isFetchedCallback.invoke()
                }
        }
    }
}