package com.todo.list.adsPlugin.bannerAd

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.todo.list.R

class BannerAdController {
    companion object {
        fun loadAndShowBannerAd(
            activity: Activity,
            containerLayout: LinearLayout,
            loadingLayout: LinearLayout,
            isLargeBanner: Boolean = false,
            isInternetConnected: Boolean
        ) {
            MobileAds.initialize(activity) {
            }
            val adView = AdView(activity)
            adView.setAdSize(if (isLargeBanner) {
                AdSize.LARGE_BANNER
            } else {
                AdSize.BANNER
            })
            adView.adUnitId = activity.getString(R.string.dashboard_banner_id)
            containerLayout.addView(adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    if (isInternetConnected) {
                        adView.loadAd(adRequest)
                    } else {
                        containerLayout.visibility = View.GONE
                    }
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    loadingLayout.visibility = View.GONE
                }
            }
        }
    }
}