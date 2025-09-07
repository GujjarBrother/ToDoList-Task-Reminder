package com.sag.todo.list.task.reminder.adsPlugin.bannerAd

import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdView
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.controllers.InternetController
import com.sag.todo.list.task.reminder.core.utils.Prefs

class BannerAdController(
    private val prefs: Prefs,
    private val internetController: InternetController
) : DefaultLifecycleObserver {
    private var mBannerAdView: AdView? = null
    private var adFrame: LinearLayout? = null
    private var fromScreen: String = ""
    private var isAdLoadCalled: Boolean = false
    private var isRequesting: Boolean = false
    private var lifecycle: Lifecycle? = null
    private var adID: Int = R.string.dashboardScreenBannerAdId
    private lateinit var mContext: AppCompatActivity
}