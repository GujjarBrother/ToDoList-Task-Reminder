package com.todo.list.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.todo.list.BuildConfig
import com.todo.list.R
import com.todo.list.adapters.ColorSchemeAdapter
import com.todo.list.adsPlugin.bannerAd.BannerAdController
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivitySettingsBinding
import com.todo.list.databinding.RateUsDialogLayoutBinding
import com.todo.list.models.ColorSchemeModel
import com.todo.list.utils.CommonFunctions
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.isSomethingChanged
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.openAppInPlayStore
import com.todo.list.utils.CommonFunctions.openGoogleAppStore
import com.todo.list.utils.CommonFunctions.openPrivacyPolicyActivity
import es.dmoral.toasty.Toasty

class SettingsActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySettingsBinding
    private val colorSchemeArrayList = ArrayList<ColorSchemeModel>()
    private lateinit var colorSchemeAdapter: ColorSchemeAdapter

    private val sa10PhotoEditorAppPackage = "com.editor.sa10photoeditor"
    private val dailyExpenseManagerAppPackage = "com.daily.manager"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = isInternetConnectedORNot(
                    (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)
                )
            )

            applyLightAndDarkMode()
            applyLightAndDarkModeOnSwitch()
            keepActivityOn(activityContext)
            applyCustomFont()
            showColorsForColorScheme()

            versionNumberTV.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)
            textSizeValueTV.text = prefs.textSizeValue.toString()
            textSizeSeekBar.progress = prefs.textSizeValue
            lightAndDarkModeSwitch.isChecked = prefs.isDarkModeEnable

            backArrowIV.setOnClickListener(this@SettingsActivity)
            photoEditorAppLayout.setOnClickListener(this@SettingsActivity)
            dailyExpenseManagerAppLayout.setOnClickListener(this@SettingsActivity)
            visitOurAppStoreLayout.setOnClickListener(this@SettingsActivity)
            rateUsLayout.setOnClickListener(this@SettingsActivity)
            feedbackLayout.setOnClickListener(this@SettingsActivity)
            shareAppLayout.setOnClickListener(this@SettingsActivity)
            privacyPolicyLayout.setOnClickListener(this@SettingsActivity)
            checkUpdateLayout.setOnClickListener(this@SettingsActivity)
            textSizeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    if (i < 14) {
                        textSizeValueTV.text = 14.toString()
                        textSizeSeekBar.progress = 14
                    } else {
                        textSizeValueTV.text = i.toString()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    prefs.textSizeValue = seekBar.progress
                    isSomethingChanged.value = true
                }
            })

            lightAndDarkModeSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                if (isChecked) {
                    colorSchemeCV.changeVisibility(0)
                } else {
                    colorSchemeCV.changeVisibility(1)
                }
                isSomethingChanged.value = true
                prefs.isDarkModeEnable = isChecked
                applyLightAndDarkMode()
                applyLightAndDarkModeOnSwitch()
            }
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackToDashBoardActivity()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun applyLightAndDarkModeOnSwitch() {
        with(binding) {
            val switchTrackDrawable = lightAndDarkModeSwitch.trackDrawable
            if (prefs.isDarkModeEnable) {
                include.root.changeVisibility(1)
                lightAndDarkModeIV.setImageResource(R.drawable.sun_image)
                lightAndDarkModeTV.setText(R.string.light_mode_text)
                switchTrackDrawable.colorFilter = PorterDuffColorFilter(snowWhiteColor, PorterDuff.Mode.SRC_IN)
                lightAndDarkModeSwitch.thumbDrawable = ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb_night_mode)
                /*if (prefs.getColorSchemeValue() == 0) {
                    switchTrackDrawable.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.default_switch_thumb));
                } else if (prefs.getColorSchemeValue() == 1) {
                    switchTrackDrawable.setColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.dark_yellow_switch_thumb));
                } else if (prefs.getColorSchemeValue() == 2) {
                    switchTrackDrawable.setColorFilter(orangeColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.orange_switch_thumb));
                } else if (prefs.getColorSchemeValue() == 3) {
                    switchTrackDrawable.setColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.light_green_switch_thumb));
                } else if (prefs.getColorSchemeValue() == 4) {
                    switchTrackDrawable.setColorFilter(blueColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.blue_switch_thumb));
                } else if (prefs.getColorSchemeValue() == 5) {
                    switchTrackDrawable.setColorFilter(cyanColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.cyan_switch_thumb));
                } else if (prefs.getColorSchemeValue() == 6) {
                    switchTrackDrawable.setColorFilter(pinkColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.pink_switch_thumb));
                } else if (prefs.getColorSchemeValue() == 7) {
                    switchTrackDrawable.setColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.dark_blue_switch_thumb));
                } else if (prefs.getColorSchemeValue() == 8) {
                    switchTrackDrawable.setColorFilter(redColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.red_switch_thumb));
                } else if (prefs.getColorSchemeValue() == 9) {
                    switchTrackDrawable.setColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN);
                    dayAndNightModeSwitch.setThumbDrawable(ContextCompat.getDrawable(activityContext,
                            R.drawable.light_purple_switch_thumb));
                }*/
            } else {
                include.root.changeVisibility(0)
                lightAndDarkModeIV.setImageResource(R.drawable.moon_image)
                lightAndDarkModeTV.setText(R.string.dark_mode_text)
                switchTrackDrawable.colorFilter = PorterDuffColorFilter(switchTrackOffColor, PorterDuff.Mode.SRC_IN)
                lightAndDarkModeSwitch.thumbDrawable = ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb)
            }
        }
    }

    private fun goBackToDashBoardActivity() = finish()

    private fun applyLightAndDarkMode() {
        with(binding) {
            if (prefs.isDarkModeEnable) {
                changeStatusBarColor(activityContext, screensNightModeColor)
                colorSchemeCV.changeVisibility(0)
                toolbar.setBackgroundColor(screensNightModeColor)
                rootLayout.setBackgroundColor(screensNightModeColor)
                appearanceCV.setCardBackgroundColor(cardsNightModeColor)
                appearanceTV.setTextColor(lightBlueColor)
                appearanceIV.setColorFilter(lightBlueColor)
                textSizeTV.setTextColor(whiteColor)
                textSizeValueTV.setTextColor(lightBlueColor)
                textSizeSeekBar.progressTintList = ColorStateList.valueOf(lightBlueColor)
                textSizeSeekBar.thumbTintList = ColorStateList.valueOf(lightBlueColor)
                smallAIV.setColorFilter(lightBlueColor)
                capitalAIV.setColorFilter(lightBlueColor)
                lightAndDarkModeIV.setColorFilter(lightBlueColor)
                lightAndDarkModeTV.setTextColor(whiteColor)
                moreAppsCV.setCardBackgroundColor(cardsNightModeColor)
                moreAppsTV.setTextColor(lightBlueColor)
                photoEditorTV.setTextColor(whiteColor)
                photoEditorAppArrowIV.setColorFilter(lightBlueColor)
                dailyExpenseManagerAppTV.setTextColor(whiteColor)
                dailyExpenseManagerAppArrowIV.setColorFilter(lightBlueColor)
                visitOurAppStoreShapeableIV.setColorFilter(lightBlueColor)
                visitOurAppStoreTV.setTextColor(whiteColor)
                visitOurAppStoreArrowIV.setColorFilter(lightBlueColor)
                aboutCV.setCardBackgroundColor(cardsNightModeColor)
                aboutTV.setTextColor(lightBlueColor)
                rateUsIV.setColorFilter(lightBlueColor)
                rateUsTV.setTextColor(whiteColor)
                rateUsArrowIV.setColorFilter(lightBlueColor)
                feedbackIV.setColorFilter(lightBlueColor)
                feedbackTV.setTextColor(whiteColor)
                feedbackArrowIV.setColorFilter(lightBlueColor)
                shareAppIV.setColorFilter(lightBlueColor)
                shareAppTV.setTextColor(whiteColor)
                shareAppArrowIV.setColorFilter(lightBlueColor)
                privacyPolicyIV.setColorFilter(lightBlueColor)
                privacyPolicyTV.setTextColor(whiteColor)
                privacyPolicyArrowIV.setColorFilter(lightBlueColor)
                checkUpdateIV.setColorFilter(lightBlueColor)
                checkUpdateTV.setTextColor(whiteColor)
                checkUpdateArrowIV.setColorFilter(lightBlueColor)
                adLoadingInclude.adIsLoadingTextView.setTextColor(whiteColor)
                adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(whiteColor)
            } else {
                colorSchemeCV.changeVisibility(1)
                rootLayout.setBackgroundColor(snowWhiteColor)
                appearanceCV.setCardBackgroundColor(whiteColor)
                textSizeTV.setTextColor(defaultColor)
                lightAndDarkModeTV.setTextColor(defaultColor)
                colorSchemeCV.setCardBackgroundColor(whiteColor)
                moreAppsCV.setCardBackgroundColor(whiteColor)
                photoEditorTV.setTextColor(blackColor)
                dailyExpenseManagerAppTV.setTextColor(blackColor)
                visitOurAppStoreTV.setTextColor(blackColor)
                aboutCV.setCardBackgroundColor(whiteColor)
                rateUsTV.setTextColor(blackColor)
                feedbackTV.setTextColor(blackColor)
                shareAppTV.setTextColor(blackColor)
                privacyPolicyTV.setTextColor(blackColor)
                checkUpdateTV.setTextColor(blackColor)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        changeStatusBarColor(activityContext, defaultColor)
                        toolbar.setBackgroundColor(defaultColor)
                        appearanceTV.setTextColor(defaultColor)
                        appearanceIV.setColorFilter(defaultColor)
                        textSizeValueTV.setTextColor(defaultColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(defaultColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(defaultColor)
                        smallAIV.setColorFilter(defaultColor)
                        capitalAIV.setColorFilter(defaultColor)
                        lightAndDarkModeIV.setColorFilter(defaultColor)
                        colorSchemeTV.setTextColor(defaultColor)
                        colorSchemeIV.setColorFilter(defaultColor)
                        moreAppsTV.setTextColor(defaultColor)
                        photoEditorAppArrowIV.setColorFilter(defaultColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(defaultColor)
                        visitOurAppStoreShapeableIV.setColorFilter(defaultColor)
                        visitOurAppStoreArrowIV.setColorFilter(defaultColor)
                        aboutTV.setTextColor(defaultColor)
                        rateUsIV.setColorFilter(defaultColor)
                        rateUsArrowIV.setColorFilter(defaultColor)
                        feedbackIV.setColorFilter(defaultColor)
                        feedbackArrowIV.setColorFilter(defaultColor)
                        shareAppIV.setColorFilter(defaultColor)
                        shareAppArrowIV.setColorFilter(defaultColor)
                        privacyPolicyIV.setColorFilter(defaultColor)
                        privacyPolicyArrowIV.setColorFilter(defaultColor)
                        checkUpdateIV.setColorFilter(defaultColor)
                        checkUpdateArrowIV.setColorFilter(defaultColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(defaultColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(defaultColor)
                    }

                    1 -> {
                        changeStatusBarColor(activityContext, darkYellowColor)
                        toolbar.setBackgroundColor(darkYellowColor)
                        appearanceTV.setTextColor(darkYellowColor)
                        appearanceIV.setColorFilter(darkYellowColor)
                        textSizeValueTV.setTextColor(darkYellowColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(darkYellowColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(darkYellowColor)
                        smallAIV.setColorFilter(darkYellowColor)
                        capitalAIV.setColorFilter(darkYellowColor)
                        lightAndDarkModeIV.setColorFilter(darkYellowColor)
                        colorSchemeTV.setTextColor(darkYellowColor)
                        colorSchemeIV.setColorFilter(darkYellowColor)
                        moreAppsTV.setTextColor(darkYellowColor)
                        photoEditorAppArrowIV.setColorFilter(darkYellowColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(darkYellowColor)
                        visitOurAppStoreShapeableIV.setColorFilter(darkYellowColor)
                        visitOurAppStoreArrowIV.setColorFilter(darkYellowColor)
                        aboutTV.setTextColor(darkYellowColor)
                        rateUsIV.setColorFilter(darkYellowColor)
                        rateUsArrowIV.setColorFilter(darkYellowColor)
                        feedbackIV.setColorFilter(darkYellowColor)
                        feedbackArrowIV.setColorFilter(darkYellowColor)
                        shareAppIV.setColorFilter(darkYellowColor)
                        shareAppArrowIV.setColorFilter(darkYellowColor)
                        privacyPolicyIV.setColorFilter(darkYellowColor)
                        privacyPolicyArrowIV.setColorFilter(darkYellowColor)
                        checkUpdateIV.setColorFilter(darkYellowColor)
                        checkUpdateArrowIV.setColorFilter(darkYellowColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkYellowColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkYellowColor)
                    }

                    2 -> {
                        changeStatusBarColor(activityContext, orangeColor)
                        toolbar.setBackgroundColor(orangeColor)
                        appearanceTV.setTextColor(orangeColor)
                        appearanceIV.setColorFilter(orangeColor)
                        textSizeValueTV.setTextColor(orangeColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(orangeColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(orangeColor)
                        smallAIV.setColorFilter(orangeColor)
                        capitalAIV.setColorFilter(orangeColor)
                        lightAndDarkModeIV.setColorFilter(orangeColor)
                        colorSchemeTV.setTextColor(orangeColor)
                        colorSchemeIV.setColorFilter(orangeColor)
                        moreAppsTV.setTextColor(orangeColor)
                        photoEditorAppArrowIV.setColorFilter(orangeColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(orangeColor)
                        visitOurAppStoreShapeableIV.setColorFilter(orangeColor)
                        visitOurAppStoreArrowIV.setColorFilter(orangeColor)
                        aboutTV.setTextColor(orangeColor)
                        rateUsIV.setColorFilter(orangeColor)
                        rateUsArrowIV.setColorFilter(orangeColor)
                        feedbackIV.setColorFilter(orangeColor)
                        feedbackArrowIV.setColorFilter(orangeColor)
                        shareAppIV.setColorFilter(orangeColor)
                        shareAppArrowIV.setColorFilter(orangeColor)
                        privacyPolicyIV.setColorFilter(orangeColor)
                        privacyPolicyArrowIV.setColorFilter(orangeColor)
                        checkUpdateIV.setColorFilter(orangeColor)
                        checkUpdateArrowIV.setColorFilter(orangeColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(orangeColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(orangeColor)
                    }

                    3 -> {
                        changeStatusBarColor(activityContext, lightGreenColor)
                        toolbar.setBackgroundColor(lightGreenColor)
                        appearanceTV.setTextColor(lightGreenColor)
                        appearanceIV.setColorFilter(lightGreenColor)
                        textSizeValueTV.setTextColor(lightGreenColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(lightGreenColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(lightGreenColor)
                        smallAIV.setColorFilter(lightGreenColor)
                        capitalAIV.setColorFilter(lightGreenColor)
                        lightAndDarkModeIV.setColorFilter(lightGreenColor)
                        colorSchemeTV.setTextColor(lightGreenColor)
                        colorSchemeIV.setColorFilter(lightGreenColor)
                        moreAppsTV.setTextColor(lightGreenColor)
                        photoEditorAppArrowIV.setColorFilter(lightGreenColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(lightGreenColor)
                        visitOurAppStoreShapeableIV.setColorFilter(lightGreenColor)
                        visitOurAppStoreArrowIV.setColorFilter(lightGreenColor)
                        aboutTV.setTextColor(lightGreenColor)
                        rateUsIV.setColorFilter(lightGreenColor)
                        rateUsArrowIV.setColorFilter(lightGreenColor)
                        feedbackIV.setColorFilter(lightGreenColor)
                        feedbackArrowIV.setColorFilter(lightGreenColor)
                        shareAppIV.setColorFilter(lightGreenColor)
                        shareAppArrowIV.setColorFilter(lightGreenColor)
                        privacyPolicyIV.setColorFilter(lightGreenColor)
                        privacyPolicyArrowIV.setColorFilter(lightGreenColor)
                        checkUpdateIV.setColorFilter(lightGreenColor)
                        checkUpdateArrowIV.setColorFilter(lightGreenColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightGreenColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightGreenColor)
                    }

                    4 -> {
                        changeStatusBarColor(activityContext, blueColor)
                        toolbar.setBackgroundColor(blueColor)
                        appearanceTV.setTextColor(blueColor)
                        appearanceIV.setColorFilter(blueColor)
                        textSizeValueTV.setTextColor(blueColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(blueColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(blueColor)
                        smallAIV.setColorFilter(blueColor)
                        capitalAIV.setColorFilter(blueColor)
                        lightAndDarkModeIV.setColorFilter(blueColor)
                        colorSchemeTV.setTextColor(blueColor)
                        colorSchemeIV.setColorFilter(blueColor)
                        moreAppsTV.setTextColor(blueColor)
                        photoEditorAppArrowIV.setColorFilter(blueColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(blueColor)
                        visitOurAppStoreShapeableIV.setColorFilter(blueColor)
                        visitOurAppStoreArrowIV.setColorFilter(blueColor)
                        aboutTV.setTextColor(blueColor)
                        rateUsIV.setColorFilter(blueColor)
                        rateUsArrowIV.setColorFilter(blueColor)
                        feedbackIV.setColorFilter(blueColor)
                        feedbackArrowIV.setColorFilter(blueColor)
                        shareAppIV.setColorFilter(blueColor)
                        shareAppArrowIV.setColorFilter(blueColor)
                        privacyPolicyIV.setColorFilter(blueColor)
                        privacyPolicyArrowIV.setColorFilter(blueColor)
                        checkUpdateIV.setColorFilter(blueColor)
                        checkUpdateArrowIV.setColorFilter(blueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(blueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(blueColor)
                    }

                    5 -> {
                        changeStatusBarColor(activityContext, cyanColor)
                        toolbar.setBackgroundColor(cyanColor)
                        appearanceTV.setTextColor(cyanColor)
                        appearanceIV.setColorFilter(cyanColor)
                        textSizeValueTV.setTextColor(cyanColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(cyanColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(cyanColor)
                        smallAIV.setColorFilter(cyanColor)
                        capitalAIV.setColorFilter(cyanColor)
                        lightAndDarkModeIV.setColorFilter(cyanColor)
                        colorSchemeTV.setTextColor(cyanColor)
                        colorSchemeIV.setColorFilter(cyanColor)
                        moreAppsTV.setTextColor(cyanColor)
                        photoEditorAppArrowIV.setColorFilter(cyanColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(cyanColor)
                        visitOurAppStoreShapeableIV.setColorFilter(cyanColor)
                        visitOurAppStoreArrowIV.setColorFilter(cyanColor)
                        aboutTV.setTextColor(cyanColor)
                        rateUsIV.setColorFilter(cyanColor)
                        rateUsArrowIV.setColorFilter(cyanColor)
                        feedbackIV.setColorFilter(cyanColor)
                        feedbackArrowIV.setColorFilter(cyanColor)
                        shareAppIV.setColorFilter(cyanColor)
                        shareAppArrowIV.setColorFilter(cyanColor)
                        privacyPolicyIV.setColorFilter(cyanColor)
                        privacyPolicyArrowIV.setColorFilter(cyanColor)
                        checkUpdateIV.setColorFilter(cyanColor)
                        checkUpdateArrowIV.setColorFilter(cyanColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(cyanColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(cyanColor)
                    }

                    6 -> {
                        changeStatusBarColor(activityContext, pinkColor)
                        toolbar.setBackgroundColor(pinkColor)
                        appearanceTV.setTextColor(pinkColor)
                        appearanceIV.setColorFilter(pinkColor)
                        textSizeValueTV.setTextColor(pinkColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(pinkColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(pinkColor)
                        smallAIV.setColorFilter(pinkColor)
                        capitalAIV.setColorFilter(pinkColor)
                        lightAndDarkModeIV.setColorFilter(pinkColor)
                        colorSchemeTV.setTextColor(pinkColor)
                        colorSchemeIV.setColorFilter(pinkColor)
                        moreAppsTV.setTextColor(pinkColor)
                        photoEditorAppArrowIV.setColorFilter(pinkColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(pinkColor)
                        visitOurAppStoreShapeableIV.setColorFilter(pinkColor)
                        visitOurAppStoreArrowIV.setColorFilter(pinkColor)
                        aboutTV.setTextColor(pinkColor)
                        rateUsIV.setColorFilter(pinkColor)
                        rateUsArrowIV.setColorFilter(pinkColor)
                        feedbackIV.setColorFilter(pinkColor)
                        feedbackArrowIV.setColorFilter(pinkColor)
                        shareAppIV.setColorFilter(pinkColor)
                        shareAppArrowIV.setColorFilter(pinkColor)
                        privacyPolicyIV.setColorFilter(pinkColor)
                        privacyPolicyArrowIV.setColorFilter(pinkColor)
                        checkUpdateIV.setColorFilter(pinkColor)
                        checkUpdateArrowIV.setColorFilter(pinkColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(pinkColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(pinkColor)
                    }

                    7 -> {
                        changeStatusBarColor(activityContext, darkBlueColor)
                        toolbar.setBackgroundColor(darkBlueColor)
                        appearanceTV.setTextColor(darkBlueColor)
                        appearanceIV.setColorFilter(darkBlueColor)
                        textSizeValueTV.setTextColor(darkBlueColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(darkBlueColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(darkBlueColor)
                        smallAIV.setColorFilter(darkBlueColor)
                        capitalAIV.setColorFilter(darkBlueColor)
                        lightAndDarkModeIV.setColorFilter(darkBlueColor)
                        colorSchemeTV.setTextColor(darkBlueColor)
                        colorSchemeIV.setColorFilter(darkBlueColor)
                        moreAppsTV.setTextColor(darkBlueColor)
                        photoEditorAppArrowIV.setColorFilter(darkBlueColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(darkBlueColor)
                        visitOurAppStoreShapeableIV.setColorFilter(darkBlueColor)
                        visitOurAppStoreArrowIV.setColorFilter(darkBlueColor)
                        aboutTV.setTextColor(darkBlueColor)
                        rateUsIV.setColorFilter(darkBlueColor)
                        rateUsArrowIV.setColorFilter(darkBlueColor)
                        feedbackIV.setColorFilter(darkBlueColor)
                        feedbackArrowIV.setColorFilter(darkBlueColor)
                        shareAppIV.setColorFilter(darkBlueColor)
                        shareAppArrowIV.setColorFilter(darkBlueColor)
                        privacyPolicyIV.setColorFilter(darkBlueColor)
                        privacyPolicyArrowIV.setColorFilter(darkBlueColor)
                        checkUpdateIV.setColorFilter(darkBlueColor)
                        checkUpdateArrowIV.setColorFilter(darkBlueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkBlueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkBlueColor)
                    }

                    8 -> {
                        changeStatusBarColor(activityContext, redColor)
                        toolbar.setBackgroundColor(redColor)
                        appearanceTV.setTextColor(redColor)
                        appearanceIV.setColorFilter(redColor)
                        textSizeValueTV.setTextColor(redColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(redColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(redColor)
                        smallAIV.setColorFilter(redColor)
                        capitalAIV.setColorFilter(redColor)
                        lightAndDarkModeIV.setColorFilter(redColor)
                        colorSchemeTV.setTextColor(redColor)
                        colorSchemeIV.setColorFilter(redColor)
                        moreAppsTV.setTextColor(redColor)
                        photoEditorAppArrowIV.setColorFilter(redColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(redColor)
                        visitOurAppStoreShapeableIV.setColorFilter(redColor)
                        visitOurAppStoreArrowIV.setColorFilter(redColor)
                        aboutTV.setTextColor(redColor)
                        rateUsIV.setColorFilter(redColor)
                        rateUsArrowIV.setColorFilter(redColor)
                        feedbackIV.setColorFilter(redColor)
                        feedbackArrowIV.setColorFilter(redColor)
                        shareAppIV.setColorFilter(redColor)
                        shareAppArrowIV.setColorFilter(redColor)
                        privacyPolicyIV.setColorFilter(redColor)
                        privacyPolicyArrowIV.setColorFilter(redColor)
                        checkUpdateIV.setColorFilter(redColor)
                        checkUpdateArrowIV.setColorFilter(redColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(redColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(redColor)
                    }

                    9 -> {
                        changeStatusBarColor(activityContext, lightPurpleColor)
                        toolbar.setBackgroundColor(lightPurpleColor)
                        appearanceTV.setTextColor(lightPurpleColor)
                        appearanceIV.setColorFilter(lightPurpleColor)
                        textSizeValueTV.setTextColor(lightPurpleColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(lightPurpleColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(lightPurpleColor)
                        smallAIV.setColorFilter(lightPurpleColor)
                        capitalAIV.setColorFilter(lightPurpleColor)
                        lightAndDarkModeIV.setColorFilter(lightPurpleColor)
                        colorSchemeTV.setTextColor(lightPurpleColor)
                        colorSchemeIV.setColorFilter(lightPurpleColor)
                        moreAppsTV.setTextColor(lightPurpleColor)
                        photoEditorAppArrowIV.setColorFilter(lightPurpleColor)
                        dailyExpenseManagerAppArrowIV.setColorFilter(lightPurpleColor)
                        visitOurAppStoreShapeableIV.setColorFilter(lightPurpleColor)
                        visitOurAppStoreArrowIV.setColorFilter(lightPurpleColor)
                        aboutTV.setTextColor(lightPurpleColor)
                        rateUsIV.setColorFilter(lightPurpleColor)
                        rateUsArrowIV.setColorFilter(lightPurpleColor)
                        feedbackIV.setColorFilter(lightPurpleColor)
                        feedbackArrowIV.setColorFilter(lightPurpleColor)
                        shareAppIV.setColorFilter(lightPurpleColor)
                        shareAppArrowIV.setColorFilter(lightPurpleColor)
                        privacyPolicyIV.setColorFilter(lightPurpleColor)
                        privacyPolicyArrowIV.setColorFilter(lightPurpleColor)
                        checkUpdateIV.setColorFilter(lightPurpleColor)
                        checkUpdateArrowIV.setColorFilter(lightPurpleColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightPurpleColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightPurpleColor)
                    }
                }
            }
        }
    }

    private fun showColorsForColorScheme() {
        with(colorSchemeArrayList) {
            add(ColorSchemeModel(0, defaultColor, false))
            add(ColorSchemeModel(1, darkYellowColor, false))
            add(ColorSchemeModel(2, orangeColor, false))
            add(ColorSchemeModel(3, lightGreenColor, false))
            add(ColorSchemeModel(4, blueColor, false))
            add(ColorSchemeModel(5, cyanColor, false))
            add(ColorSchemeModel(6, pinkColor, false))
            add(ColorSchemeModel(7, darkBlueColor, false))
            add(ColorSchemeModel(8, redColor, false))
            add(ColorSchemeModel(9, lightPurpleColor, false))
        }
        colorSchemeArrayList[prefs.colorSchemeValue].isSelected = true
        colorSchemeAdapter = ColorSchemeAdapter(colorSchemeArrayList) { id ->
            isSomethingChanged.value = true
            for (i in colorSchemeArrayList.indices) {
                val colorSchemeModel = colorSchemeArrayList[i]
                colorSchemeModel.isSelected = id == colorSchemeModel.id
                prefs.colorSchemeValue = id
                colorSchemeAdapter.notifyDataSetChanged()
            }
            applyLightAndDarkMode()
            applyLightAndDarkModeOnSwitch()
        }
        val gridLayoutManager = GridLayoutManager(activityContext, 5, RecyclerView.VERTICAL, false)
        with(binding) {
            colorSchemeRV.layoutManager = gridLayoutManager
            colorSchemeRV.adapter = colorSchemeAdapter
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            toolbarTV.typeface = typeface
            appearanceTV.typeface = typeface
            textSizeTV.typeface = typeface
            textSizeValueTV.typeface = typeface
            lightAndDarkModeTV.typeface = typeface
            colorSchemeTV.typeface = typeface
            enjoyMultipleColorsTV.typeface = typeface
            moreAppsTV.typeface = typeface
            photoEditorTV.typeface = typeface
            dailyExpenseManagerAppTV.typeface = typeface
            visitOurAppStoreTV.typeface = typeface
            aboutTV.typeface = typeface
            rateUsTV.typeface = typeface
            pleaseTellYourExperienceTV.typeface = typeface
            feedbackTV.typeface = typeface
            giveYourSuggestionsAndFeedbackTV.typeface = typeface
            shareAppTV.typeface = typeface
            shareOurAppToYourFriendsAndFamilyTV.typeface = typeface
            privacyPolicyTV.typeface = typeface
            readOurPrivacyPolicyTV.typeface = typeface
            checkUpdateTV.typeface = typeface
            versionNumberTV.typeface = typeface
            adLoadingInclude.adIsLoadingTextView.typeface = typeface
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> {
                goBackToDashBoardActivity()
            }

            R.id.photoEditorAppLayout -> {
                openAppInPlayStore(activityContext, sa10PhotoEditorAppPackage)
            }

            R.id.dailyExpenseManagerAppLayout -> {
                openAppInPlayStore(activityContext, dailyExpenseManagerAppPackage)
            }

            R.id.visitOurAppStoreLayout -> {
                openGoogleAppStore(activityContext)
            }

            R.id.rateUsLayout -> {
                showRateUsDialog()
            }

            R.id.feedbackLayout -> {
                openFeedbackActivity()
            }

            R.id.shareAppLayout -> {
                shareApp()
            }

            R.id.privacyPolicyLayout -> {
                openPrivacyPolicyActivity(activityContext, isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)))
            }

            R.id.checkUpdateLayout -> {
                openAppInPlayStore(activityContext, BuildConfig.APPLICATION_ID)
            }
        }
    }

    private fun openFeedbackActivity() = startActivity(Intent(activityContext, FeedbackActivity::class.java))

    private fun showRateUsDialog() {
        val rateUsDialogLayoutBinding = RateUsDialogLayoutBinding.inflate(layoutInflater)

        val rateUsDialogBuilder = AlertDialog.Builder(activityContext)
        with(rateUsDialogBuilder) {
            setView(rateUsDialogLayoutBinding.root)
            setCancelable(true)
        }
        val rateUsAlertDialog = rateUsDialogBuilder.create()
        if (!activityContext.isFinishing && !activityContext.isDestroyed && !rateUsAlertDialog.isShowing) {
            rateUsAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            rateUsAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            rateUsAlertDialog.show()
        }

        with(rateUsDialogLayoutBinding) {
            rateUsDialogIV.startAnimation(CommonFunctions.applyAnimation(activityContext))
            applyCustomFontOnRateUsDialogViews(this)
            applyColorSchemeORDayAndNightModeOnRateUsDialogViews(this)

            dismissRateUsDialogIV.setOnClickListener { _: View? ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    rateUsAlertDialog.dismiss()
                }
            }

            rateUsButton.setOnClickListener { _: View? ->
                val rating = rateUsDialogLayoutBinding.ratingBar.rating
                if (rating in 1.0..3.0) {
                    rateUsDialogLayoutBinding.rateUsButton.changeVisibility(2)
                    rateUsDialogLayoutBinding.group.changeVisibility(1)
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                            rateUsAlertDialog.dismiss()
                        } }, 2000)
                } else if (rating >= 4.0) {
                    openAppInPlayStore(activityContext, BuildConfig.APPLICATION_ID)
                    if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                        rateUsAlertDialog.dismiss()
                    }
                } else {
                    Toasty.error(activityContext, getString(R.string.please_rate_our_app_toast_text), Toasty.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun applyCustomFontOnRateUsDialogViews(rateUsDialogLayoutBinding: RateUsDialogLayoutBinding) {
        with(rateUsDialogLayoutBinding) {
            rateOurAppTV.typeface = typeface
            messageTV.typeface = typeface
            rateUsButton.typeface = typeface
            thanksForYourFeedbackTV.typeface = typeface
        }
    }

    private fun applyColorSchemeORDayAndNightModeOnRateUsDialogViews(
        rateUsDialogLayoutBinding: RateUsDialogLayoutBinding
    ) {
        with(rateUsDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                dismissRateUsDialogIV.setColorFilter(lightBlueColor)
                rateOurAppTV.setTextColor(lightBlueColor)
                messageTV.setTextColor(darkModeTextColor)
                ratingBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(activityContext, R.color.lightBlueColor))
                rateUsButton.setBackgroundColor(lightBlueColor)
                rateUsButton.setTextColor(blackColor)
                thanksForYourFeedbackTV.setTextColor(darkModeTextColor)
            } else {
                rootLayout.setBackgroundResource(dialogBoxesLightModeBackground)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        dismissRateUsDialogIV.setColorFilter(defaultColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(defaultColor)
                        rateUsButton.setBackgroundColor(defaultColor)
                    }

                    1 -> {
                        dismissRateUsDialogIV.setColorFilter(darkYellowColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(darkYellowColor)
                        rateUsButton.setBackgroundColor(darkYellowColor)
                    }

                    2 -> {
                        dismissRateUsDialogIV.setColorFilter(orangeColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(orangeColor)
                        rateUsButton.setBackgroundColor(orangeColor)
                    }

                    3 -> {
                        dismissRateUsDialogIV.setColorFilter(lightGreenColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(lightGreenColor)
                        rateUsButton.setBackgroundColor(lightGreenColor)
                    }

                    4 -> {
                        dismissRateUsDialogIV.setColorFilter(blueColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(blueColor)
                        rateUsButton.setBackgroundColor(blueColor)
                    }

                    5 -> {
                        dismissRateUsDialogIV.setColorFilter(cyanColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(cyanColor)
                        rateUsButton.setBackgroundColor(cyanColor)
                    }

                    6 -> {
                        dismissRateUsDialogIV.setColorFilter(pinkColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(pinkColor)
                        rateUsButton.setBackgroundColor(pinkColor)
                    }

                    7 -> {
                        dismissRateUsDialogIV.setColorFilter(darkBlueColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(darkBlueColor)
                        rateUsButton.setBackgroundColor(darkBlueColor)
                    }

                    8 -> {
                        dismissRateUsDialogIV.setColorFilter(redColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(redColor)
                        rateUsButton.setBackgroundColor(redColor)
                    }

                    9 -> {
                        dismissRateUsDialogIV.setColorFilter(lightPurpleColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(lightPurpleColor)
                        rateUsButton.setBackgroundColor(lightPurpleColor)
                    }
                }
            }
        }
    }

    private fun shareApp() {
        val shareAppIntent = Intent()
        with(shareAppIntent) {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            if (this.resolveActivity(packageManager) != null) {
                val chooserIntent = Intent.createChooser(this, getString(R.string.share_via_text))
                startActivity(chooserIntent)
            } else {
                Toasty.error(activityContext, R.string.there_is_no_activity_available_to_handle_this_action_toast_text, Toasty.LENGTH_LONG).show()
            }
        }
    }

    private fun appIsInstalledOrNot(appPackageName: String): Boolean {
        try {
            packageManager.getPackageInfo(appPackageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (_: PackageManager.NameNotFoundException) {
        }
        return false
    }

    override fun onResume() {
        super.onResume()

        with(binding) {
            val isPhotoEditorAppInstalledOrNot = appIsInstalledOrNot(sa10PhotoEditorAppPackage)
            val isDailyExpenseManagerAppInstalledOrNot = appIsInstalledOrNot(dailyExpenseManagerAppPackage)

            if (isPhotoEditorAppInstalledOrNot) {
                photoEditorAppLayout.changeVisibility(0)
            } else {
                photoEditorAppLayout.changeVisibility(1)
            }

            if (isDailyExpenseManagerAppInstalledOrNot) {
                dailyExpenseManagerAppLayout.changeVisibility(0)
            } else {
                dailyExpenseManagerAppLayout.changeVisibility(1)
            }
        }
    }
}