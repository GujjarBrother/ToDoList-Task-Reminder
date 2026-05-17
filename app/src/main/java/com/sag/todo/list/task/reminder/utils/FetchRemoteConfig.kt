package com.sag.todo.list.task.reminder.utils

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.utils.RemoteConfigValues.IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN
import com.sag.todo.list.task.reminder.utils.RemoteConfigValues.LANGUAGE_ACTIVITY_APPEARANCE_AFTER_SPLASH
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchRemoteConfig @Inject constructor() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun fetchRemoteConfigValues() {
        remoteConfig = Firebase.remoteConfig
        val remoteConfigSettings = remoteConfigSettings {
            setMinimumFetchIntervalInSeconds(3600)
        }
        remoteConfig.apply {
            setConfigSettingsAsync(remoteConfigSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate()
                .addOnCompleteListener {
                    if (it.isSuccessful) assignRemoteValues()
                }
        }
    }

    private fun assignRemoteValues() {
        remoteConfig.apply {
            IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN = getBoolean("IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN")
            LANGUAGE_ACTIVITY_APPEARANCE_AFTER_SPLASH = getLong("LANGUAGE_ACTIVITY_APPEARANCE_AFTER_SPLASH").toInt()
        }
    }
}