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
import com.todo.list.listeners.StartAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener
import com.todo.list.utils.CommonFunctions.TASKS_TAB
import com.todo.list.utils.CommonFunctions.applyAnimation
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.keepActivityOn

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

            checkSignInOrSignOutStatus()
            keepActivityOn(activityContext)
            applyCustomFont()

            val viewPagerAdapter = ViewPagerAdapter(activityContext)

            val actionBarDrawerToggle = ActionBarDrawerToggle(activityContext, dashBoardActivityDrawerLayout,
                R.string.navigation_drawer_open_text, R.string.navigation_drawer_close_text)
            dashBoardActivityDrawerLayout.addDrawerListener(actionBarDrawerToggle)
            actionBarDrawerToggle.syncState()

            navigationDrawerInclude.versionNumberTextView.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)

            dashBoardViewPager.adapter = viewPagerAdapter
            TabLayoutMediator(
                tabLayout, dashBoardViewPager
            ) {
                tab: TabLayout.Tab, position: Int -> tab.setText(
                if (position == TASKS_TAB) {
                    getString(R.string.tasks_text)
                } else {
                    getString(R.string.completed_text)
                })
            }.attach()

            dashBoardViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position == 0) {
                        toolbarTextView.text = getString(R.string.tasks_text)
                    } else if (position == 1) {
                        toolbarTextView.text = getString(R.string.completed_text)
                    }
                }
            })
            signOutImageView.setOnClickListener(this@DashBoardActivity)
            settingsImageView.setOnClickListener(this@DashBoardActivity)
            openAndCloseDrawerImageView.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.settingsOuterLayout.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.lightAndDarkModeSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                prefs.dayAndNightModeSwitchValue = isChecked
                applyColorSchemeORLightAndDarkModeOnDashboardActivity()
                dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                if (::startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.isInitialized) {
                    startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.goAhead(
                        1, isChecked, true
                    )
                }
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

    override fun onResume() {
        super.onResume()
        applyColorSchemeORLightAndDarkModeOnDashboardActivity()
    }

    private fun applyColorSchemeORLightAndDarkModeOnDashboardActivity() {
        with(binding) {
            val trackDrawable = navigationDrawerInclude.lightAndDarkModeSwitch.trackDrawable
            if (prefs.dayAndNightModeSwitchValue) {
                changeStatusBarColor(activityContext, screensNightModeColor)
                toolbar.setBackgroundColor(screensNightModeColor)
                dashBoardActivityRootLayout.setBackgroundColor(screensNightModeColor)
                tabLayout.setBackgroundColor(screensNightModeColor)
                tabLayout.setSelectedTabIndicatorColor(cardsNightModeColor)
                tabLayout.setTabTextColors(tabLayoutUnSelectedTabTextColor, whiteColor)
                adLoadingInclude.adIsLoadingTextView.setTextColor(whiteColor)
                adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(whiteColor)

                navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                    screensNightModeColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.appNameTextView.setTextColor(whiteColor)
                navigationDrawerInclude.featuresTextView.setBackgroundColor(cardsNightModeColor)
                navigationDrawerInclude.featuresTextView.setTextColor(whiteColor)
                navigationDrawerInclude.lightAndDarkImageView.setImageResource(R.drawable.sun_image)
                navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                    whiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeTextView.text = getString(R.string.light_mode_text)
                navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(whiteColor)
                navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(whiteColor)
                navigationDrawerInclude.lightAndDarkModeSwitch.isChecked = true
                trackDrawable.colorFilter = PorterDuffColorFilter(snowWhiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeSwitch.thumbDrawable =
                    ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb_night_mode)
                navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(cardsNightModeColor)
                navigationDrawerInclude.generalSettingsTextView.setTextColor(whiteColor)
                navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                    whiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.settingsTextView.setTextColor(whiteColor)
                navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(whiteColor)
                navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                    whiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                    whiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(whiteColor)
                navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(whiteColor)
                navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                    whiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                    whiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.privacyPolicyTextView.setTextColor(whiteColor)
                navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(whiteColor)
                navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                    whiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                    whiteColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.checkUpdateTextView.setTextColor(whiteColor)
                navigationDrawerInclude.versionNumberTextView.setTextColor(whiteColor)
                navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                    whiteColor, PorterDuff.Mode.SRC_IN)
            } else {
                navigationDrawerInclude.lightAndDarkImageView.setImageResource(R.drawable.moon_image)
                navigationDrawerInclude.lightAndDarkModeTextView.text = getString(R.string.dark_mode_text)
                navigationDrawerInclude.lightAndDarkModeSwitch.isChecked = false
                trackDrawable.colorFilter = PorterDuffColorFilter(switchTrackOffColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeSwitch.thumbDrawable =
                    ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb)
                dashBoardActivityRootLayout.setBackgroundColor(snowWhiteColor)
                tabLayout.setSelectedTabIndicatorColor(snowWhiteColor)
                tabLayout.setTabTextColors(tabLayoutUnSelectedTabTextColor, blackColor)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        changeStatusBarColor(activityContext, defaultColor)
                        toolbar.setBackgroundColor(defaultColor)
                        tabLayout.setBackgroundColor(defaultColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(defaultColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(defaultColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(defaultTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(defaultColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(defaultTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(defaultColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        changeStatusBarColor(activityContext, darkYellowColor)
                        toolbar.setBackgroundColor(darkYellowColor)
                        tabLayout.setBackgroundColor(darkYellowColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkYellowColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkYellowColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(darkYellowTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(darkYellowColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(darkYellowTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(darkYellowColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        changeStatusBarColor(activityContext, orangeColor)
                        toolbar.setBackgroundColor(orangeColor)
                        tabLayout.setBackgroundColor(orangeColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(orangeColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(orangeColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(orangeTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(orangeColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(orangeTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(orangeColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        changeStatusBarColor(activityContext, lightGreenColor)
                        toolbar.setBackgroundColor(lightGreenColor)
                        tabLayout.setBackgroundColor(lightGreenColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightGreenColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightGreenColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(lightGreenTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(lightGreenColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(lightGreenTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(lightGreenColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        changeStatusBarColor(activityContext, blueColor)
                        toolbar.setBackgroundColor(blueColor)
                        tabLayout.setBackgroundColor(blueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(blueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(blueColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(blueTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(blueColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(blueTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(blueColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        changeStatusBarColor(activityContext, cyanColor)
                        toolbar.setBackgroundColor(cyanColor)
                        tabLayout.setBackgroundColor(cyanColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(cyanColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(cyanColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(cyanTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(cyanColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(cyanTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(cyanColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        changeStatusBarColor(activityContext, pinkColor)
                        toolbar.setBackgroundColor(pinkColor)
                        tabLayout.setBackgroundColor(pinkColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(pinkColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(pinkColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(pinkTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(pinkColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(pinkTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(pinkColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        changeStatusBarColor(activityContext, darkBlueColor)
                        toolbar.setBackgroundColor(darkBlueColor)
                        tabLayout.setBackgroundColor(darkBlueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkBlueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkBlueColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(darkBlueTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(darkBlueColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(darkBlueTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(darkBlueColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        changeStatusBarColor(activityContext, redColor)
                        toolbar.setBackgroundColor(redColor)
                        tabLayout.setBackgroundColor(redColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(redColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(redColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(redTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(redColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(redTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(redColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        changeStatusBarColor(activityContext, lightPurpleColor)
                        toolbar.setBackgroundColor(lightPurpleColor)
                        tabLayout.setBackgroundColor(lightPurpleColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightPurpleColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightPurpleColor)

                        navigationDrawerInclude.navigationDrawerRootLayout.background.colorFilter = PorterDuffColorFilter(
                            snowWhiteColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.appNameTextView.setTextColor(blackColor)
                        navigationDrawerInclude.featuresTextView.setBackgroundColor(lightPurpleTransparentColor)
                        navigationDrawerInclude.featuresTextView.setTextColor(lightPurpleColor)
                        navigationDrawerInclude.lightAndDarkImageView.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.lightAndDarkModeTextView.setTextColor(blackColor)
                        navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.generalSettingsTextView.setBackgroundColor(lightPurpleTransparentColor)
                        navigationDrawerInclude.generalSettingsTextView.setTextColor(lightPurpleColor)
                        navigationDrawerInclude.settingsImageView.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.settingsTextView.setTextColor(blackColor)
                        navigationDrawerInclude.seeTheRequiredSettingsTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.settingsArrowImageView.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreImageView.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.visitOurAppStoreTextView.setTextColor(blackColor)
                        navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.visitOurAppStoreArrowImageView.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyImageView.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.privacyPolicyTextView.setTextColor(blackColor)
                        navigationDrawerInclude.readOurPrivacyPolicyTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.privacyPolicyArrowImageView.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateImageView.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        navigationDrawerInclude.checkUpdateTextView.setTextColor(blackColor)
                        navigationDrawerInclude.versionNumberTextView.setTextColor(subTitlesTextColor)
                        navigationDrawerInclude.checkUpdateArrowImageView.colorFilter = PorterDuffColorFilter(
                            lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        }
    }

    private fun checkSignInOrSignOutStatus() {
        if (!prefs.isUserSignInOrSignOutValue) {
            startActivity(Intent(activityContext, SignInActivity::class.java))
            finish()
        }
    }

    //    Here, We Set 'Custom Font' On An Activity View's...
    private fun applyCustomFont() {
        with(binding) {
            toolbarTextView.typeface = typeface
            adLoadingInclude.adIsLoadingTextView.typeface = typeface
            navigationDrawerInclude.appNameTextView.typeface = typeface
            navigationDrawerInclude.featuresTextView.typeface = typeface
            navigationDrawerInclude.lightAndDarkModeTextView.typeface = typeface
            navigationDrawerInclude.switchBetweenLightAndDarkModeTextView.typeface = typeface
            navigationDrawerInclude.generalSettingsTextView.typeface = typeface
            navigationDrawerInclude.settingsTextView.typeface = typeface
            navigationDrawerInclude.seeTheRequiredSettingsTextView.typeface = typeface
            navigationDrawerInclude.visitOurAppStoreTextView.typeface = typeface
            navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTextView.typeface = typeface
            navigationDrawerInclude.privacyPolicyTextView.typeface = typeface
            navigationDrawerInclude.readOurPrivacyPolicyTextView.typeface = typeface
            navigationDrawerInclude.checkUpdateTextView.typeface = typeface
            navigationDrawerInclude.versionNumberTextView.typeface = typeface

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
                R.id.sign_out_image_view -> {
                    showSignOutDialog()
                }

                R.id.settingsImageView -> {
                    openSettingsActivity()
                }

                R.id.openAndCloseDrawerImageView -> {
                    dashBoardActivityDrawerLayout.openDrawer(GravityCompat.START)
                }

                R.id.settingsOuterLayout -> {
                    openSettingsActivity()
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }
            }
        }
    }

    private fun showSignOutDialog() {
        val signOutDialogLayoutBinding = SignOutDialogLayoutBinding.inflate(layoutInflater)

        val signOutDialogBuilder = AlertDialog.Builder(activityContext)
        signOutDialogBuilder.setView(signOutDialogLayoutBinding.root)
        signOutDialogBuilder.setCancelable(false)
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
            signOutImageView.startAnimation(applyAnimation(activityContext))
            applyCustomFontOnSignOutDialogViews(this)
            applyColorSchemeORLightAndDarkModeOnSignOutDialogViews(this)

            noButton.setOnClickListener { _: View ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    signOutAlertDialog.dismiss()
                }
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener.goAhead(1)
                }
            }

            yesButton.setOnClickListener { _: View ->
                prefs.isUserSignInOrSignOutValue = false
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

    private fun applyColorSchemeORLightAndDarkModeOnSignOutDialogViews(
            signOutDialogLayoutBinding: SignOutDialogLayoutBinding
    ) {
        with(signOutDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                signOutDialogRootLayout.setBackgroundResource(dialogBoxesDarkModeBackground)
                signOutImageView.setColorFilter(whiteColor)
                signOutMessageTextView.setTextColor(whiteColor)
                noButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                noButton.setTextColor(whiteColor)
                yesButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                yesButton.setTextColor(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        signOutImageView.setColorFilter(defaultColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        signOutImageView.setColorFilter(darkYellowColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        signOutImageView.setColorFilter(orangeColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        signOutImageView.setColorFilter(lightGreenColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        signOutImageView.setColorFilter(blueColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        signOutImageView.setColorFilter(blueColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        signOutImageView.setColorFilter(pinkColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        signOutImageView.setColorFilter(darkBlueColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        signOutImageView.setColorFilter(redColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        signOutImageView.setColorFilter(lightPurpleColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnSignOutDialogViews(signOutDialogLayoutBinding: SignOutDialogLayoutBinding) {
        with(signOutDialogLayoutBinding) {
            signOutMessageTextView.typeface = typeface
            yesButton.typeface = typeface
            noButton.typeface = typeface
        }
    }

    private fun showExitDialog() {
        val exitFromAnAppDialogLayoutBinding = ExitFromAnAppDialogLayoutBinding.inflate(layoutInflater)

        val exitFromAnAppDialogBuilder = AlertDialog.Builder(activityContext)
        exitFromAnAppDialogBuilder.setView(exitFromAnAppDialogLayoutBinding.root)
        exitFromAnAppDialogBuilder.setCancelable(false)
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
            exitFromAnAppImageView.startAnimation(applyAnimation(activityContext))
            applyCustomFontOnExitFromAnAppDialogViews(this)
            applyColorSchemeAndLightDarkModeOnExitDialogViews(this)

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

    private fun applyColorSchemeAndLightDarkModeOnExitDialogViews(
            exitFromAnAppDialogLayoutBinding: ExitFromAnAppDialogLayoutBinding
    ) {
        with(exitFromAnAppDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                exitDialogRootLayout.setBackgroundResource(dialogBoxesDarkModeBackground)
                exitFromAnAppImageView.setColorFilter(whiteColor)
                exitFromAnAppMessageTextView.setTextColor(whiteColor)
                yesButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                yesButton.setTextColor(whiteColor)
                noButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                noButton.setTextColor(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        exitFromAnAppImageView.setColorFilter(defaultColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        exitFromAnAppImageView.setColorFilter(darkYellowColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        exitFromAnAppImageView.setColorFilter(orangeColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        exitFromAnAppImageView.setColorFilter(lightGreenColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        exitFromAnAppImageView.setColorFilter(blueColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        exitFromAnAppImageView.setColorFilter(cyanColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        exitFromAnAppImageView.setColorFilter(pinkColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        exitFromAnAppImageView.setColorFilter(darkBlueColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        exitFromAnAppImageView.setColorFilter(redColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        exitFromAnAppImageView.setColorFilter(lightPurpleColor)
                        yesButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        noButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnExitFromAnAppDialogViews(
            exitFromAnAppDialogLayoutBinding: ExitFromAnAppDialogLayoutBinding
    ) {
        with(exitFromAnAppDialogLayoutBinding) {
            exitFromAnAppMessageTextView.typeface = typeface
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