package com.todo.list.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.todo.list.BuildConfig
import com.todo.list.R
import com.todo.list.adapters.ViewPagerAdapter
import com.todo.list.adsPlugin.bannerAd.BannerAdController
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivityDashBoardBinding
import com.todo.list.databinding.ExitFromAnAppDialogLayoutBinding
import com.todo.list.databinding.SignOutDialogLayoutBinding
import com.todo.list.enums.TabsEnum
import com.todo.list.listeners.StartAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener
import com.todo.list.utils.CommonFunctions.applyAnimation
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.isSomethingChanged
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.openAppInPlayStore
import com.todo.list.utils.CommonFunctions.openGoogleAppStore
import com.todo.list.utils.CommonFunctions.openPrivacyPolicyActivity

class DashBoardActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDashBoardBinding
    private lateinit var startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener: StartAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager))
            )

            keepActivityOn(activityContext)
            applyCustomFont()

            val viewPagerAdapter = ViewPagerAdapter(activityContext)

            val actionBarDrawerToggle = ActionBarDrawerToggle(activityContext, dashBoardActivityDrawerLayout,
                R.string.navigation_drawer_open_text, R.string.navigation_drawer_close_text)
            dashBoardActivityDrawerLayout.addDrawerListener(actionBarDrawerToggle)
            actionBarDrawerToggle.syncState()

            navigationDrawerInclude.versionNumberTV.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)

            dashBoardViewPager.adapter = viewPagerAdapter
            TabLayoutMediator(tabLayout, dashBoardViewPager) {
                tab: TabLayout.Tab, position: Int -> tab.setText(
                if (position == TabsEnum.TASKS_TAB.ordinal) {
                    getString(R.string.tasks_text)
                } else {
                    getString(R.string.completed_text)
                })
            }.attach()

            dashBoardViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    manageTabsScrolling(position)
                }
            })
            signOutIV.setOnClickListener(this@DashBoardActivity)
            settingsIV.setOnClickListener(this@DashBoardActivity)
            openAndCloseDrawerIV.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.settingsOuterLayout.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.visitOurAppStoreOuterLayout.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.privacyPolicyOuterLayout.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.checkUpdateOuterLayout.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.lightAndDarkModeSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                prefs.isDarkModeEnable = isChecked
                applyLightAndDarkModeOnDashboardActivity()
                isSomethingChanged.value = true
                dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
            }

            val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (dashBoardActivityDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        if (dashBoardViewPager.currentItem != 0) {
                            dashBoardViewPager.currentItem = 0
                        } else {
                            showExitDialog()
                        }
                    }
                }
            }
            onBackPressedDispatcher.addCallback(onBackPressedCallback)
            dashBoardActivityDrawerLayout.setScrimColor(ContextCompat.getColor(activityContext, R.color.navigationDrawerScrimColor))
        }
    }

    private fun ActivityDashBoardBinding.manageTabsScrolling(position: Int) {
        if (position == 0) {
            toolbarTV.text = getString(R.string.tasks_text)
            startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.goAhead(1)
        } else if (position == 1) {
            toolbarTV.text = getString(R.string.completed_text)
        }
        dashBoardViewPager.setCurrentItem(position, true)
    }

    override fun onResume() {
        super.onResume()
        applyLightAndDarkModeOnDashboardActivity()
    }

    private fun applyLightAndDarkModeOnDashboardActivity() {
        with(binding) {
            val trackDrawable = navigationDrawerInclude.lightAndDarkModeSwitch.trackDrawable
            if (prefs.isDarkModeEnable) {
                changeStatusBarColor(activityContext, screensNightModeColor)
                toolbar.setBackgroundColor(screensNightModeColor)
                rootLayout.setBackgroundColor(screensNightModeColor)
                tabLayout.setBackgroundColor(screensNightModeColor)
                tabLayout.setSelectedTabIndicatorColor(cardsNightModeColor)
                tabLayout.setTabTextColors(tabLayoutUnSelectedTabTextColor, whiteColor)
                adLoadingInclude.adIsLoadingTextView.setTextColor(whiteColor)
                adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(whiteColor)

                openAndCloseDrawerIV.setColorFilter(lightBlueColor)
                signOutIV.setColorFilter(lightBlueColor)
                settingsIV.setColorFilter(lightBlueColor)

                navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.appNameTV.setTextColor(whiteColor)
                navigationDrawerInclude.featuresTV.setBackgroundColor(cardsNightModeColor)
                navigationDrawerInclude.lightAndDarkIV.setImageResource(R.drawable.sun_image)
                navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(lightBlueColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeTV.text = getString(R.string.light_mode_text)
                navigationDrawerInclude.lightAndDarkModeTV.setTextColor(whiteColor)
                navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(darkModeTextColor)
                navigationDrawerInclude.lightAndDarkModeSwitch.isChecked = true
                trackDrawable.colorFilter = PorterDuffColorFilter(snowWhiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeSwitch.thumbDrawable =
                    ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb_night_mode)
                navigationDrawerInclude.generalSettingsTV.setBackgroundColor(cardsNightModeColor)
                navigationDrawerInclude.generalSettingsTV.setTextColor(whiteColor)
                navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                    lightBlueColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.settingsTV.setTextColor(whiteColor)
                navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(darkModeTextColor)
                navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                    lightBlueColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                    lightBlueColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.visitOurAppStoreTV.setTextColor(whiteColor)
                navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(darkModeTextColor)
                navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                    lightBlueColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                    lightBlueColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.privacyPolicyTV.setTextColor(whiteColor)
                navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(darkModeTextColor)
                navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                    lightBlueColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                    lightBlueColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.checkUpdateTV.setTextColor(whiteColor)
                navigationDrawerInclude.versionNumberTV.setTextColor(darkModeTextColor)
                navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                    lightBlueColor, PorterDuff.Mode.SRC_IN)
            } else {
                navigationDrawerInclude.lightAndDarkIV.setImageResource(R.drawable.moon_image)
                navigationDrawerInclude.lightAndDarkModeTV.text = getString(R.string.dark_mode_text)
                navigationDrawerInclude.lightAndDarkModeSwitch.isChecked = false
                trackDrawable.colorFilter = PorterDuffColorFilter(switchTrackOffColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeSwitch.thumbDrawable =
                    ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb)
                rootLayout.setBackgroundColor(snowWhiteColor)
                tabLayout.setSelectedTabIndicatorColor(snowWhiteColor)
                tabLayout.setTabTextColors(tabLayoutUnSelectedTabTextColor, blackColor)
                openAndCloseDrawerIV.setColorFilter(whiteColor)
                signOutIV.setColorFilter(whiteColor)
                settingsIV.setColorFilter(whiteColor)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        changeStatusBarColor(activityContext, defaultColor)
                        toolbar.setBackgroundColor(defaultColor)
                        tabLayout.setBackgroundColor(defaultColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(defaultColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(defaultColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(defaultTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(defaultTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        changeStatusBarColor(activityContext, darkYellowColor)
                        toolbar.setBackgroundColor(darkYellowColor)
                        tabLayout.setBackgroundColor(darkYellowColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkYellowColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkYellowColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(darkYellowTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(darkYellowTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        changeStatusBarColor(activityContext, orangeColor)
                        toolbar.setBackgroundColor(orangeColor)
                        tabLayout.setBackgroundColor(orangeColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(orangeColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(orangeColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(orangeTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(orangeTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        changeStatusBarColor(activityContext, lightGreenColor)
                        toolbar.setBackgroundColor(lightGreenColor)
                        tabLayout.setBackgroundColor(lightGreenColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightGreenColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightGreenColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(lightGreenTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(lightGreenTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        changeStatusBarColor(activityContext, blueColor)
                        toolbar.setBackgroundColor(blueColor)
                        tabLayout.setBackgroundColor(blueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(blueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(blueColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(blueTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(blueTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        changeStatusBarColor(activityContext, cyanColor)
                        toolbar.setBackgroundColor(cyanColor)
                        tabLayout.setBackgroundColor(cyanColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(cyanColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(cyanColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(cyanTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(cyanTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        changeStatusBarColor(activityContext, pinkColor)
                        toolbar.setBackgroundColor(pinkColor)
                        tabLayout.setBackgroundColor(pinkColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(pinkColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(pinkColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(pinkTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(pinkTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        changeStatusBarColor(activityContext, darkBlueColor)
                        toolbar.setBackgroundColor(darkBlueColor)
                        tabLayout.setBackgroundColor(darkBlueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkBlueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkBlueColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(darkBlueTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(darkBlueTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        changeStatusBarColor(activityContext, redColor)
                        toolbar.setBackgroundColor(redColor)
                        tabLayout.setBackgroundColor(redColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(redColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(redColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(redTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(redTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        changeStatusBarColor(activityContext, lightPurpleColor)
                        toolbar.setBackgroundColor(lightPurpleColor)
                        tabLayout.setBackgroundColor(lightPurpleColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightPurpleColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightPurpleColor)

                        navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTV.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTV.setBackgroundColor(lightPurpleTransparentColor)
                        navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTV.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTV.setBackgroundColor(lightPurpleTransparentColor)
                        navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTV.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTV.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTV.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTV.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTV.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            toolbarTV.typeface = typeface
            adLoadingInclude.adIsLoadingTextView.typeface = typeface
            navigationDrawerInclude.appNameTV.typeface = typeface
            navigationDrawerInclude.featuresTV.typeface = typeface
            navigationDrawerInclude.lightAndDarkModeTV.typeface = typeface
            navigationDrawerInclude.switchBetweenLightAndDarkModeTV.typeface = typeface
            navigationDrawerInclude.generalSettingsTV.typeface = typeface
            navigationDrawerInclude.settingsTV.typeface = typeface
            navigationDrawerInclude.seeTheRequiredSettingsTV.typeface = typeface
            navigationDrawerInclude.visitOurAppStoreTV.typeface = typeface
            navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.typeface = typeface
            navigationDrawerInclude.privacyPolicyTV.typeface = typeface
            navigationDrawerInclude.readOurPrivacyPolicyTV.typeface = typeface
            navigationDrawerInclude.checkUpdateTV.typeface = typeface
            navigationDrawerInclude.versionNumberTV.typeface = typeface

//        Here We Applying Our Custom Font On Tab Item's Text In TabLayout In Tabbed Activity...........
            val viewGroup = tabLayout.getChildAt(0) as ViewGroup
            val tabsCount = viewGroup.childCount
            for (j in 0 until tabsCount) {
                val vgTab = viewGroup.getChildAt(j) as ViewGroup
                val tabChildCount = vgTab.childCount
                for (i in 0 until tabChildCount) {
                    val tabViewChild = vgTab.getChildAt(i)
                    if (tabViewChild is TextView) {
                        tabViewChild.typeface = typeface
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.signOutIV -> {
                    showSignOutDialog()
                }

                R.id.settingsIV -> {
                    openSettingsActivity()
                }

                R.id.openAndCloseDrawerIV -> {
                    dashBoardActivityDrawerLayout.openDrawer(GravityCompat.START)
                }

                R.id.settingsOuterLayout -> {
                    openSettingsActivity()
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.visitOurAppStoreOuterLayout -> {
                    openGoogleAppStore(activityContext)
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.privacyPolicyOuterLayout -> {
                    openPrivacyPolicyActivity(
                        activityContext,
                        isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager))
                    )
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.checkUpdateOuterLayout -> {
                    openAppInPlayStore(activityContext, BuildConfig.APPLICATION_ID)
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }
            }
        }
    }

    private fun showSignOutDialog() {
        val signOutDialogLayoutBinding = SignOutDialogLayoutBinding.inflate(layoutInflater)

        val signOutDialogBuilder = AlertDialog.Builder(activityContext)
        with(signOutDialogBuilder) {
            setView(signOutDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.goAhead(1)
                }
            }
        }
        val signOutAlertDialog = signOutDialogBuilder.create()
        if (!activityContext.isFinishing && !activityContext.isDestroyed && !signOutAlertDialog.isShowing) {
            signOutAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            signOutAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            signOutAlertDialog.show()
        }

        if (binding.dashBoardViewPager.currentItem == 0) {
            startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.goAhead(0)
        }

        with(signOutDialogLayoutBinding) {
            signOutIV.startAnimation(applyAnimation(activityContext))
            applyCustomFontOnSignOutDialogViews(this)
            applyLightAndDarkModeOnSignOutDialogViews(this)

            noButton.setOnClickListener { _: View ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    signOutAlertDialog.dismiss()
                }
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.goAhead(1)
                }
            }

            yesButton.setOnClickListener { _: View ->
                prefs.isUserSignIn = false
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    signOutAlertDialog.dismiss()
                }
                openSignInActivity()
            }
        }
    }

    private fun openSignInActivity() {
        startActivity(Intent(activityContext, SignInActivity::class.java))
        finish()
    }

    //    Here, We Initialize Stop FAB Animation From ToDosFragment Listener...
    fun initializeStopFABAnimationFromToDosFragmentListener(
        startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener: StartAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener
    ) {
        this.startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener = startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener
    }

    private fun openSettingsActivity() = startActivity(Intent(activityContext, SettingsActivity::class.java))

    private fun applyLightAndDarkModeOnSignOutDialogViews(
            signOutDialogLayoutBinding: SignOutDialogLayoutBinding
    ) {
        with(signOutDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                signOutIV.setColorFilter(lightBlueColor)
                signOutMessageTV.setTextColor(whiteColor)
                noButton.strokeColor = ColorStateList.valueOf(lightBlueColor)
                noButton.setTextColor(lightBlueColor)
                yesButton.setBackgroundColor(lightBlueColor)
                yesButton.setTextColor(blackColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        signOutIV.setColorFilter(defaultColor)
                        noButton.strokeColor = ColorStateList.valueOf(defaultColor)
                        noButton.setTextColor(defaultColor)
                        yesButton.setBackgroundColor(defaultColor)
                    }

                    1 -> {
                        signOutIV.setColorFilter(darkYellowColor)
                        noButton.strokeColor = ColorStateList.valueOf(darkYellowColor)
                        noButton.setTextColor(darkYellowColor)
                        yesButton.setBackgroundColor(darkYellowColor)
                    }

                    2 -> {
                        signOutIV.setColorFilter(orangeColor)
                        noButton.strokeColor = ColorStateList.valueOf(orangeColor)
                        noButton.setTextColor(orangeColor)
                        yesButton.setBackgroundColor(orangeColor)
                    }

                    3 -> {
                        signOutIV.setColorFilter(lightGreenColor)
                        noButton.strokeColor = ColorStateList.valueOf(lightGreenColor)
                        noButton.setTextColor(lightGreenColor)
                        yesButton.setBackgroundColor(lightGreenColor)
                    }

                    4 -> {
                        signOutIV.setColorFilter(blueColor)
                        noButton.strokeColor = ColorStateList.valueOf(blueColor)
                        noButton.setTextColor(blueColor)
                        yesButton.setBackgroundColor(blueColor)
                    }

                    5 -> {
                        signOutIV.setColorFilter(blueColor)
                        noButton.strokeColor = ColorStateList.valueOf(blueColor)
                        noButton.setTextColor(blueColor)
                        yesButton.setBackgroundColor(blueColor)
                    }

                    6 -> {
                        signOutIV.setColorFilter(pinkColor)
                        noButton.strokeColor = ColorStateList.valueOf(pinkColor)
                        noButton.setTextColor(pinkColor)
                        yesButton.setBackgroundColor(pinkColor)
                    }

                    7 -> {
                        signOutIV.setColorFilter(darkBlueColor)
                        noButton.strokeColor = ColorStateList.valueOf(darkBlueColor)
                        noButton.setTextColor(darkBlueColor)
                        yesButton.setBackgroundColor(darkBlueColor)
                    }

                    8 -> {
                        signOutIV.setColorFilter(redColor)
                        noButton.strokeColor = ColorStateList.valueOf(redColor)
                        noButton.setTextColor(redColor)
                        yesButton.setBackgroundColor(redColor)
                    }

                    9 -> {
                        signOutIV.setColorFilter(lightPurpleColor)
                        noButton.strokeColor = ColorStateList.valueOf(lightPurpleColor)
                        noButton.setTextColor(lightPurpleColor)
                        yesButton.setBackgroundColor(lightPurpleColor)
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnSignOutDialogViews(signOutDialogLayoutBinding: SignOutDialogLayoutBinding) {
        with(signOutDialogLayoutBinding) {
            signOutMessageTV.typeface = typeface
            yesButton.typeface = typeface
            noButton.typeface = typeface
        }
    }

    private fun showExitDialog() {
        val exitFromAnAppDialogLayoutBinding = ExitFromAnAppDialogLayoutBinding.inflate(layoutInflater)

        val exitFromAnAppDialogBuilder = AlertDialog.Builder(activityContext)
        with(exitFromAnAppDialogBuilder) {
            setView(exitFromAnAppDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.goAhead(1)
                }
            }
        }
        val exitFromAnAppAlertDialog = exitFromAnAppDialogBuilder.create()
        if (!activityContext.isFinishing && !activityContext.isDestroyed && !exitFromAnAppAlertDialog.isShowing) {
            exitFromAnAppAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            exitFromAnAppAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            exitFromAnAppAlertDialog.show()
        }

        if (binding.dashBoardViewPager.currentItem == 0) {
            startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.goAhead(0)
        }

        with(exitFromAnAppDialogLayoutBinding) {
            exitFromAnAppIV.startAnimation(applyAnimation(activityContext))
            applyCustomFontOnExitFromAnAppDialogViews(this)
            applyLightAndDarkModeOnExitDialogViews(this)

            noButton.setOnClickListener { _: View ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    exitFromAnAppAlertDialog.dismiss()
                }
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.goAhead(1)
                }
            }

            yesButton.setOnClickListener { _: View ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    exitFromAnAppAlertDialog.dismiss()
                }
                openThankYouActivity()
            }
        }
    }

    private fun openThankYouActivity() {
        startActivity(Intent(activityContext, ThankYouActivity::class.java))
        finish()
    }

    private fun applyLightAndDarkModeOnExitDialogViews(
            exitFromAnAppDialogLayoutBinding: ExitFromAnAppDialogLayoutBinding
    ) {
        with(exitFromAnAppDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                exitFromAnAppIV.setColorFilter(whiteColor)
                exitFromAnAppMessageTV.setTextColor(whiteColor)
                noButton.strokeColor = ColorStateList.valueOf(lightBlueColor)
                noButton.setTextColor(lightBlueColor)
                yesButton.setBackgroundColor(lightBlueColor)
                yesButton.setTextColor(blackColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        exitFromAnAppIV.setColorFilter(defaultColor)
                        noButton.strokeColor = ColorStateList.valueOf(defaultColor)
                        noButton.setTextColor(defaultColor)
                        yesButton.setBackgroundColor(defaultColor)
                    }

                    1 -> {
                        exitFromAnAppIV.setColorFilter(darkYellowColor)
                        noButton.strokeColor = ColorStateList.valueOf(darkYellowColor)
                        noButton.setTextColor(darkYellowColor)
                        yesButton.setBackgroundColor(darkYellowColor)
                    }

                    2 -> {
                        exitFromAnAppIV.setColorFilter(orangeColor)
                        noButton.strokeColor = ColorStateList.valueOf(orangeColor)
                        noButton.setTextColor(orangeColor)
                        yesButton.setBackgroundColor(orangeColor)
                    }

                    3 -> {
                        exitFromAnAppIV.setColorFilter(lightGreenColor)
                        noButton.strokeColor = ColorStateList.valueOf(lightGreenColor)
                        noButton.setTextColor(lightGreenColor)
                        yesButton.setBackgroundColor(lightGreenColor)
                    }

                    4 -> {
                        exitFromAnAppIV.setColorFilter(blueColor)
                        noButton.strokeColor = ColorStateList.valueOf(blueColor)
                        noButton.setTextColor(blueColor)
                        yesButton.setBackgroundColor(blueColor)
                    }

                    5 -> {
                        exitFromAnAppIV.setColorFilter(cyanColor)
                        noButton.strokeColor = ColorStateList.valueOf(cyanColor)
                        noButton.setTextColor(cyanColor)
                        yesButton.setBackgroundColor(cyanColor)
                    }

                    6 -> {
                        exitFromAnAppIV.setColorFilter(pinkColor)
                        noButton.strokeColor = ColorStateList.valueOf(pinkColor)
                        noButton.setTextColor(pinkColor)
                        yesButton.setBackgroundColor(pinkColor)
                    }

                    7 -> {
                        exitFromAnAppIV.setColorFilter(darkBlueColor)
                        noButton.strokeColor = ColorStateList.valueOf(darkBlueColor)
                        noButton.setTextColor(darkBlueColor)
                        yesButton.setBackgroundColor(darkBlueColor)
                    }

                    8 -> {
                        exitFromAnAppIV.setColorFilter(redColor)
                        noButton.strokeColor = ColorStateList.valueOf(redColor)
                        noButton.setTextColor(redColor)
                        yesButton.setBackgroundColor(redColor)
                    }

                    9 -> {
                        exitFromAnAppIV.setColorFilter(lightPurpleColor)
                        noButton.strokeColor = ColorStateList.valueOf(lightPurpleColor)
                        noButton.setTextColor(lightPurpleColor)
                        yesButton.setBackgroundColor(lightPurpleColor)
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnExitFromAnAppDialogViews(
            exitFromAnAppDialogLayoutBinding: ExitFromAnAppDialogLayoutBinding
    ) {
        with(exitFromAnAppDialogLayoutBinding) {
            exitFromAnAppMessageTV.typeface = typeface
            yesButton.typeface = typeface
            noButton.typeface = typeface
        }
    }

    //    Override 'onConfigurationChanged' Method, Which Is Used To Prevent An Activity To 'Re-create' When
    //    Changing The Screen Orientation.i.e., Switching Between 'PORTRAIT MODE' TO 'LANDSCAPE MODE' & Vice Versa.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}