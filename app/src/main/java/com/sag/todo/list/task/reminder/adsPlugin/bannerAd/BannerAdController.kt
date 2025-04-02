package com.sag.todo.list.task.reminder.adsPlugin.bannerAd

import android.app.Activity
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeVisibility

class BannerAdController {
    companion object {
        fun loadAndShowBannerAd(
            activity: Activity,
            containerLayout: LinearLayout,
            loadingLayout: ConstraintLayout,
            isLargeBanner: Boolean = false,
            isInternetConnected: Boolean,
            adID: String
        ) {
            MobileAds.initialize(activity) {
            }
            val adView = AdView(activity)
            adView.setAdSize(if (isLargeBanner) {
                AdSize.LARGE_BANNER
            } else {
                AdSize.BANNER
            })
            adView.adUnitId = adID
            containerLayout.addView(adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    if (isInternetConnected) {
                        adView.loadAd(adRequest)
                    } else {
                        containerLayout.changeVisibility(0)
                    }
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    loadingLayout.changeVisibility(0)
                }
            }
        }
    }
}