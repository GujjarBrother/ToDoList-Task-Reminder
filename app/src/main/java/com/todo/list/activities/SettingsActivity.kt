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
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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
import com.todo.list.listeners.ColorSchemeListener
import com.todo.list.models.ColorSchemeModel
import com.todo.list.utils.CommonFunctions
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.isSomethingChanged
import com.todo.list.utils.CommonFunctions.keepActivityOn
import es.dmoral.toasty.Toasty

class SettingsActivity : BaseActivity(), View.OnClickListener, ColorSchemeListener {

    private lateinit var binding: ActivitySettingsBinding
    private val colorSchemeArrayList = ArrayList<ColorSchemeModel>()
    private lateinit var colorSchemeAdapter: ColorSchemeAdapter

    private val sa10PhotoEditorAppPackage = "com.editor.sa10photoeditor"
    private val ramadanAppPackage = "com.shafqatali.ramadanapp"
    private val notePadAppPackage = "com.shafqatali.noteapp"
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

            applyColorSchemeORDayAndNightMode()
            applyColorSchemeORDayAndNightModeOnSwitch()
            keepActivityOn(activityContext)
            applyCustomFont()
            showColorsForColorScheme()

            versionNumberTextView.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)
            textSizeValueTextView.text = prefs.textSizeValue.toString()
            textSizeSeekBar.progress = prefs.textSizeValue
            dayAndNightModeSwitch.isChecked = prefs.dayAndNightModeSwitchValue

            backArrowImageView.setOnClickListener(this@SettingsActivity)
            photoEditorAppLayout.setOnClickListener(this@SettingsActivity)
            ramadanAppLayout.setOnClickListener(this@SettingsActivity)
            notePadAppLayout.setOnClickListener(this@SettingsActivity)
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
                        textSizeValueTextView.text = 14.toString()
                        textSizeSeekBar.progress = 14
                    } else {
                        textSizeValueTextView.text = i.toString()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    prefs.textSizeValue = seekBar.progress
                    isSomethingChanged = true
                }
            })

            dayAndNightModeSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                if (isChecked) {
                    colorSchemeCardView.visibility = GONE
                } else {
                    colorSchemeCardView.visibility = VISIBLE
                }
                isSomethingChanged = true
                prefs.dayAndNightModeSwitchValue = isChecked
                applyColorSchemeORDayAndNightMode()
                applyColorSchemeORDayAndNightModeOnSwitch()
            }
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackToDashBoardActivity()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun applyColorSchemeORDayAndNightModeOnSwitch() {
        with(binding) {
            val switchTrackDrawable = dayAndNightModeSwitch.trackDrawable
            if (prefs.dayAndNightModeSwitchValue) {
                include.root.visibility = VISIBLE
                dayAndNightModeImageView.setImageResource(R.drawable.sun_image)
                dayAndNightModeTextView.setText(R.string.light_mode_text)
                switchTrackDrawable.colorFilter = PorterDuffColorFilter(snowWhiteColor, PorterDuff.Mode.SRC_IN)
                dayAndNightModeSwitch.thumbDrawable =
                    ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb_night_mode)
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
                include.root.visibility = GONE
                dayAndNightModeImageView.setImageResource(R.drawable.moon_image)
                dayAndNightModeTextView.setText(R.string.dark_mode_text)
                switchTrackDrawable.colorFilter = PorterDuffColorFilter(switchTrackOffColor, PorterDuff.Mode.SRC_IN)
                dayAndNightModeSwitch.thumbDrawable =
                    ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb)
            }
        }
    }

    private fun goBackToDashBoardActivity() = finish()

    private fun applyColorSchemeORDayAndNightMode() {
        with(binding) {
            if (prefs.dayAndNightModeSwitchValue) {
                colorSchemeCardView.visibility = GONE
                changeStatusBarColor(activityContext, screensNightModeColor)
                toolbar.setBackgroundColor(screensNightModeColor)
                settingsActivityRootLayout.setBackgroundColor(screensNightModeColor)
                appearanceCardView.setCardBackgroundColor(cardsNightModeColor)
                appearanceTextView.setTextColor(whiteColor)
                appearanceImageView.setColorFilter(whiteColor)
                textSizeTextView.setTextColor(whiteColor)
                textSizeValueTextView.setTextColor(whiteColor)
                textSizeSeekBar.progressTintList = ColorStateList.valueOf(whiteColor)
                textSizeSeekBar.thumbTintList = ColorStateList.valueOf(whiteColor)
                smallAImageView.setColorFilter(whiteColor)
                capitalAImageView.setColorFilter(whiteColor)
                dayAndNightModeImageView.setColorFilter(whiteColor)
                dayAndNightModeTextView.setTextColor(whiteColor)
                moreAppsCardView.setCardBackgroundColor(cardsNightModeColor)
                moreAppsTextView.setTextColor(whiteColor)
                photoEditorTextView.setTextColor(whiteColor)
                photoEditorAppArrowImageView.setColorFilter(whiteColor)
                ramadanAppTextView.setTextColor(whiteColor)
                ramadanAppArrowImageView.setColorFilter(whiteColor)
                notePadAppTextView.setTextColor(whiteColor)
                notePadAppArrowImageView.setColorFilter(whiteColor)
                dailyExpenseManagerAppTextView.setTextColor(whiteColor)
                dailyExpenseManagerAppArrowImageView.setColorFilter(whiteColor)
                visitOurAppStoreShapeableImageView.setColorFilter(whiteColor)
                visitOurAppStoreTextView.setTextColor(whiteColor)
                visitOurAppStoreArrowImageView.setColorFilter(whiteColor)
                aboutCardView.setCardBackgroundColor(cardsNightModeColor)
                aboutTextView.setTextColor(whiteColor)
                rateUsImageView.setColorFilter(whiteColor)
                rateUsTextView.setTextColor(whiteColor)
                rateUsArrowImageView.setColorFilter(whiteColor)
                feedbackImageView.setColorFilter(whiteColor)
                feedbackTextView.setTextColor(whiteColor)
                feedbackArrowImageView.setColorFilter(whiteColor)
                shareAppImageView.setColorFilter(whiteColor)
                shareAppTextView.setTextColor(whiteColor)
                shareAppArrowImageView.setColorFilter(whiteColor)
                privacyPolicyImageView.setColorFilter(whiteColor)
                privacyPolicyTextView.setTextColor(whiteColor)
                privacyPolicyArrowImageView.setColorFilter(whiteColor)
                checkUpdateImageView.setColorFilter(whiteColor)
                checkUpdateTextView.setTextColor(whiteColor)
                checkUpdateArrowImageView.setColorFilter(whiteColor)
                adLoadingInclude.adIsLoadingTextView.setTextColor(whiteColor)
                adLoadingInclude.progressBar.indeterminateTintList =
                    ColorStateList.valueOf(whiteColor)
            } else {
                colorSchemeCardView.visibility = VISIBLE
                settingsActivityRootLayout.setBackgroundColor(snowWhiteColor)
                appearanceCardView.setCardBackgroundColor(whiteColor)
                textSizeTextView.setTextColor(defaultColor)
                dayAndNightModeTextView.setTextColor(defaultColor)
                colorSchemeCardView.setCardBackgroundColor(whiteColor)
                moreAppsCardView.setCardBackgroundColor(whiteColor)
                photoEditorTextView.setTextColor(blackColor)
                ramadanAppTextView.setTextColor(blackColor)
                dailyExpenseManagerAppTextView.setTextColor(blackColor)
                visitOurAppStoreTextView.setTextColor(blackColor)
                aboutCardView.setCardBackgroundColor(whiteColor)
                rateUsTextView.setTextColor(blackColor)
                feedbackTextView.setTextColor(blackColor)
                shareAppTextView.setTextColor(blackColor)
                privacyPolicyTextView.setTextColor(blackColor)
                checkUpdateTextView.setTextColor(blackColor)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        changeStatusBarColor(activityContext, defaultColor)
                        toolbar.setBackgroundColor(defaultColor)
                        appearanceTextView.setTextColor(defaultColor)
                        appearanceImageView.setColorFilter(defaultColor)
                        textSizeValueTextView.setTextColor(defaultColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(defaultColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(defaultColor)
                        smallAImageView.setColorFilter(defaultColor)
                        capitalAImageView.setColorFilter(defaultColor)
                        dayAndNightModeImageView.setColorFilter(defaultColor)
                        colorSchemeTextView.setTextColor(defaultColor)
                        colorSchemeImageView.setColorFilter(defaultColor)
                        moreAppsTextView.setTextColor(defaultColor)
                        photoEditorAppArrowImageView.setColorFilter(defaultColor)
                        ramadanAppArrowImageView.setColorFilter(defaultColor)
                        notePadAppArrowImageView.setColorFilter(defaultColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(defaultColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(defaultColor)
                        visitOurAppStoreArrowImageView.setColorFilter(defaultColor)
                        aboutTextView.setTextColor(defaultColor)
                        rateUsImageView.setColorFilter(defaultColor)
                        rateUsArrowImageView.setColorFilter(defaultColor)
                        feedbackImageView.setColorFilter(defaultColor)
                        feedbackArrowImageView.setColorFilter(defaultColor)
                        shareAppImageView.setColorFilter(defaultColor)
                        shareAppArrowImageView.setColorFilter(defaultColor)
                        privacyPolicyImageView.setColorFilter(defaultColor)
                        privacyPolicyArrowImageView.setColorFilter(defaultColor)
                        checkUpdateImageView.setColorFilter(defaultColor)
                        checkUpdateArrowImageView.setColorFilter(defaultColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(defaultColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(defaultColor)
                    }

                    1 -> {
                        changeStatusBarColor(activityContext, darkYellowColor)
                        toolbar.setBackgroundColor(darkYellowColor)
                        appearanceTextView.setTextColor(darkYellowColor)
                        appearanceImageView.setColorFilter(darkYellowColor)
                        textSizeValueTextView.setTextColor(darkYellowColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(darkYellowColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(darkYellowColor)
                        smallAImageView.setColorFilter(darkYellowColor)
                        capitalAImageView.setColorFilter(darkYellowColor)
                        dayAndNightModeImageView.setColorFilter(darkYellowColor)
                        colorSchemeTextView.setTextColor(darkYellowColor)
                        colorSchemeImageView.setColorFilter(darkYellowColor)
                        moreAppsTextView.setTextColor(darkYellowColor)
                        photoEditorAppArrowImageView.setColorFilter(darkYellowColor)
                        ramadanAppArrowImageView.setColorFilter(darkYellowColor)
                        notePadAppArrowImageView.setColorFilter(darkYellowColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(darkYellowColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(darkYellowColor)
                        visitOurAppStoreArrowImageView.setColorFilter(darkYellowColor)
                        aboutTextView.setTextColor(darkYellowColor)
                        rateUsImageView.setColorFilter(darkYellowColor)
                        rateUsArrowImageView.setColorFilter(darkYellowColor)
                        feedbackImageView.setColorFilter(darkYellowColor)
                        feedbackArrowImageView.setColorFilter(darkYellowColor)
                        shareAppImageView.setColorFilter(darkYellowColor)
                        shareAppArrowImageView.setColorFilter(darkYellowColor)
                        privacyPolicyImageView.setColorFilter(darkYellowColor)
                        privacyPolicyArrowImageView.setColorFilter(darkYellowColor)
                        checkUpdateImageView.setColorFilter(darkYellowColor)
                        checkUpdateArrowImageView.setColorFilter(darkYellowColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkYellowColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(darkYellowColor)
                    }

                    2 -> {
                        changeStatusBarColor(activityContext, orangeColor)
                        toolbar.setBackgroundColor(orangeColor)
                        appearanceTextView.setTextColor(orangeColor)
                        appearanceImageView.setColorFilter(orangeColor)
                        textSizeValueTextView.setTextColor(orangeColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(orangeColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(orangeColor)
                        smallAImageView.setColorFilter(orangeColor)
                        capitalAImageView.setColorFilter(orangeColor)
                        dayAndNightModeImageView.setColorFilter(orangeColor)
                        colorSchemeTextView.setTextColor(orangeColor)
                        colorSchemeImageView.setColorFilter(orangeColor)
                        moreAppsTextView.setTextColor(orangeColor)
                        photoEditorAppArrowImageView.setColorFilter(orangeColor)
                        ramadanAppArrowImageView.setColorFilter(orangeColor)
                        notePadAppArrowImageView.setColorFilter(orangeColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(orangeColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(orangeColor)
                        visitOurAppStoreArrowImageView.setColorFilter(orangeColor)
                        aboutTextView.setTextColor(orangeColor)
                        rateUsImageView.setColorFilter(orangeColor)
                        rateUsArrowImageView.setColorFilter(orangeColor)
                        feedbackImageView.setColorFilter(orangeColor)
                        feedbackArrowImageView.setColorFilter(orangeColor)
                        shareAppImageView.setColorFilter(orangeColor)
                        shareAppArrowImageView.setColorFilter(orangeColor)
                        privacyPolicyImageView.setColorFilter(orangeColor)
                        privacyPolicyArrowImageView.setColorFilter(orangeColor)
                        checkUpdateImageView.setColorFilter(orangeColor)
                        checkUpdateArrowImageView.setColorFilter(orangeColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(orangeColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(orangeColor)
                    }

                    3 -> {
                        changeStatusBarColor(activityContext, lightGreenColor)
                        toolbar.setBackgroundColor(lightGreenColor)
                        appearanceTextView.setTextColor(lightGreenColor)
                        appearanceImageView.setColorFilter(lightGreenColor)
                        textSizeValueTextView.setTextColor(lightGreenColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(lightGreenColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(lightGreenColor)
                        smallAImageView.setColorFilter(lightGreenColor)
                        capitalAImageView.setColorFilter(lightGreenColor)
                        dayAndNightModeImageView.setColorFilter(lightGreenColor)
                        colorSchemeTextView.setTextColor(lightGreenColor)
                        colorSchemeImageView.setColorFilter(lightGreenColor)
                        moreAppsTextView.setTextColor(lightGreenColor)
                        photoEditorAppArrowImageView.setColorFilter(lightGreenColor)
                        ramadanAppArrowImageView.setColorFilter(lightGreenColor)
                        notePadAppArrowImageView.setColorFilter(lightGreenColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(lightGreenColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(lightGreenColor)
                        visitOurAppStoreArrowImageView.setColorFilter(lightGreenColor)
                        aboutTextView.setTextColor(lightGreenColor)
                        rateUsImageView.setColorFilter(lightGreenColor)
                        rateUsArrowImageView.setColorFilter(lightGreenColor)
                        feedbackImageView.setColorFilter(lightGreenColor)
                        feedbackArrowImageView.setColorFilter(lightGreenColor)
                        shareAppImageView.setColorFilter(lightGreenColor)
                        shareAppArrowImageView.setColorFilter(lightGreenColor)
                        privacyPolicyImageView.setColorFilter(lightGreenColor)
                        privacyPolicyArrowImageView.setColorFilter(lightGreenColor)
                        checkUpdateImageView.setColorFilter(lightGreenColor)
                        checkUpdateArrowImageView.setColorFilter(lightGreenColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightGreenColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(lightGreenColor)
                    }

                    4 -> {
                        changeStatusBarColor(activityContext, blueColor)
                        toolbar.setBackgroundColor(blueColor)
                        appearanceTextView.setTextColor(blueColor)
                        appearanceImageView.setColorFilter(blueColor)
                        textSizeValueTextView.setTextColor(blueColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(blueColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(blueColor)
                        smallAImageView.setColorFilter(blueColor)
                        capitalAImageView.setColorFilter(blueColor)
                        dayAndNightModeImageView.setColorFilter(blueColor)
                        colorSchemeTextView.setTextColor(blueColor)
                        colorSchemeImageView.setColorFilter(blueColor)
                        moreAppsTextView.setTextColor(blueColor)
                        photoEditorAppArrowImageView.setColorFilter(blueColor)
                        ramadanAppArrowImageView.setColorFilter(blueColor)
                        notePadAppArrowImageView.setColorFilter(blueColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(blueColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(blueColor)
                        visitOurAppStoreArrowImageView.setColorFilter(blueColor)
                        aboutTextView.setTextColor(blueColor)
                        rateUsImageView.setColorFilter(blueColor)
                        rateUsArrowImageView.setColorFilter(blueColor)
                        feedbackImageView.setColorFilter(blueColor)
                        feedbackArrowImageView.setColorFilter(blueColor)
                        shareAppImageView.setColorFilter(blueColor)
                        shareAppArrowImageView.setColorFilter(blueColor)
                        privacyPolicyImageView.setColorFilter(blueColor)
                        privacyPolicyArrowImageView.setColorFilter(blueColor)
                        checkUpdateImageView.setColorFilter(blueColor)
                        checkUpdateArrowImageView.setColorFilter(blueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(blueColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(blueColor)
                    }

                    5 -> {
                        changeStatusBarColor(activityContext, cyanColor)
                        toolbar.setBackgroundColor(cyanColor)
                        appearanceTextView.setTextColor(cyanColor)
                        appearanceImageView.setColorFilter(cyanColor)
                        textSizeValueTextView.setTextColor(cyanColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(cyanColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(cyanColor)
                        smallAImageView.setColorFilter(cyanColor)
                        capitalAImageView.setColorFilter(cyanColor)
                        dayAndNightModeImageView.setColorFilter(cyanColor)
                        colorSchemeTextView.setTextColor(cyanColor)
                        colorSchemeImageView.setColorFilter(cyanColor)
                        moreAppsTextView.setTextColor(cyanColor)
                        photoEditorAppArrowImageView.setColorFilter(cyanColor)
                        ramadanAppArrowImageView.setColorFilter(cyanColor)
                        notePadAppArrowImageView.setColorFilter(cyanColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(cyanColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(cyanColor)
                        visitOurAppStoreArrowImageView.setColorFilter(cyanColor)
                        aboutTextView.setTextColor(cyanColor)
                        rateUsImageView.setColorFilter(cyanColor)
                        rateUsArrowImageView.setColorFilter(cyanColor)
                        feedbackImageView.setColorFilter(cyanColor)
                        feedbackArrowImageView.setColorFilter(cyanColor)
                        shareAppImageView.setColorFilter(cyanColor)
                        shareAppArrowImageView.setColorFilter(cyanColor)
                        privacyPolicyImageView.setColorFilter(cyanColor)
                        privacyPolicyArrowImageView.setColorFilter(cyanColor)
                        checkUpdateImageView.setColorFilter(cyanColor)
                        checkUpdateArrowImageView.setColorFilter(cyanColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(cyanColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(cyanColor)
                    }

                    6 -> {
                        changeStatusBarColor(activityContext, pinkColor)
                        toolbar.setBackgroundColor(pinkColor)
                        appearanceTextView.setTextColor(pinkColor)
                        appearanceImageView.setColorFilter(pinkColor)
                        textSizeValueTextView.setTextColor(pinkColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(pinkColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(pinkColor)
                        smallAImageView.setColorFilter(pinkColor)
                        capitalAImageView.setColorFilter(pinkColor)
                        dayAndNightModeImageView.setColorFilter(pinkColor)
                        colorSchemeTextView.setTextColor(pinkColor)
                        colorSchemeImageView.setColorFilter(pinkColor)
                        moreAppsTextView.setTextColor(pinkColor)
                        photoEditorAppArrowImageView.setColorFilter(pinkColor)
                        ramadanAppArrowImageView.setColorFilter(pinkColor)
                        notePadAppArrowImageView.setColorFilter(pinkColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(pinkColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(pinkColor)
                        visitOurAppStoreArrowImageView.setColorFilter(pinkColor)
                        aboutTextView.setTextColor(pinkColor)
                        rateUsImageView.setColorFilter(pinkColor)
                        rateUsArrowImageView.setColorFilter(pinkColor)
                        feedbackImageView.setColorFilter(pinkColor)
                        feedbackArrowImageView.setColorFilter(pinkColor)
                        shareAppImageView.setColorFilter(pinkColor)
                        shareAppArrowImageView.setColorFilter(pinkColor)
                        privacyPolicyImageView.setColorFilter(pinkColor)
                        privacyPolicyArrowImageView.setColorFilter(pinkColor)
                        checkUpdateImageView.setColorFilter(pinkColor)
                        checkUpdateArrowImageView.setColorFilter(pinkColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(pinkColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(pinkColor)
                    }

                    7 -> {
                        changeStatusBarColor(activityContext, darkBlueColor)
                        toolbar.setBackgroundColor(darkBlueColor)
                        appearanceTextView.setTextColor(darkBlueColor)
                        appearanceImageView.setColorFilter(darkBlueColor)
                        textSizeValueTextView.setTextColor(darkBlueColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(darkBlueColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(darkBlueColor)
                        smallAImageView.setColorFilter(darkBlueColor)
                        capitalAImageView.setColorFilter(darkBlueColor)
                        dayAndNightModeImageView.setColorFilter(darkBlueColor)
                        colorSchemeTextView.setTextColor(darkBlueColor)
                        colorSchemeImageView.setColorFilter(darkBlueColor)
                        moreAppsTextView.setTextColor(darkBlueColor)
                        photoEditorAppArrowImageView.setColorFilter(darkBlueColor)
                        ramadanAppArrowImageView.setColorFilter(darkBlueColor)
                        notePadAppArrowImageView.setColorFilter(darkBlueColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(darkBlueColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(darkBlueColor)
                        visitOurAppStoreArrowImageView.setColorFilter(darkBlueColor)
                        aboutTextView.setTextColor(darkBlueColor)
                        rateUsImageView.setColorFilter(darkBlueColor)
                        rateUsArrowImageView.setColorFilter(darkBlueColor)
                        feedbackImageView.setColorFilter(darkBlueColor)
                        feedbackArrowImageView.setColorFilter(darkBlueColor)
                        shareAppImageView.setColorFilter(darkBlueColor)
                        shareAppArrowImageView.setColorFilter(darkBlueColor)
                        privacyPolicyImageView.setColorFilter(darkBlueColor)
                        privacyPolicyArrowImageView.setColorFilter(darkBlueColor)
                        checkUpdateImageView.setColorFilter(darkBlueColor)
                        checkUpdateArrowImageView.setColorFilter(darkBlueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkBlueColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(darkBlueColor)
                    }

                    8 -> {
                        changeStatusBarColor(activityContext, redColor)
                        toolbar.setBackgroundColor(redColor)
                        appearanceTextView.setTextColor(redColor)
                        appearanceImageView.setColorFilter(redColor)
                        textSizeValueTextView.setTextColor(redColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(redColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(redColor)
                        smallAImageView.setColorFilter(redColor)
                        capitalAImageView.setColorFilter(redColor)
                        dayAndNightModeImageView.setColorFilter(redColor)
                        colorSchemeTextView.setTextColor(redColor)
                        colorSchemeImageView.setColorFilter(redColor)
                        moreAppsTextView.setTextColor(redColor)
                        photoEditorAppArrowImageView.setColorFilter(redColor)
                        ramadanAppArrowImageView.setColorFilter(redColor)
                        notePadAppArrowImageView.setColorFilter(redColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(redColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(redColor)
                        visitOurAppStoreArrowImageView.setColorFilter(redColor)
                        aboutTextView.setTextColor(redColor)
                        rateUsImageView.setColorFilter(redColor)
                        rateUsArrowImageView.setColorFilter(redColor)
                        feedbackImageView.setColorFilter(redColor)
                        feedbackArrowImageView.setColorFilter(redColor)
                        shareAppImageView.setColorFilter(redColor)
                        shareAppArrowImageView.setColorFilter(redColor)
                        privacyPolicyImageView.setColorFilter(redColor)
                        privacyPolicyArrowImageView.setColorFilter(redColor)
                        checkUpdateImageView.setColorFilter(redColor)
                        checkUpdateArrowImageView.setColorFilter(redColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(redColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(redColor)
                    }

                    9 -> {
                        changeStatusBarColor(activityContext, lightPurpleColor)
                        toolbar.setBackgroundColor(lightPurpleColor)
                        appearanceTextView.setTextColor(lightPurpleColor)
                        appearanceImageView.setColorFilter(lightPurpleColor)
                        textSizeValueTextView.setTextColor(lightPurpleColor)
                        textSizeSeekBar.thumbTintList = ColorStateList.valueOf(lightPurpleColor)
                        textSizeSeekBar.progressTintList = ColorStateList.valueOf(lightPurpleColor)
                        smallAImageView.setColorFilter(lightPurpleColor)
                        capitalAImageView.setColorFilter(lightPurpleColor)
                        dayAndNightModeImageView.setColorFilter(lightPurpleColor)
                        colorSchemeTextView.setTextColor(lightPurpleColor)
                        colorSchemeImageView.setColorFilter(lightPurpleColor)
                        moreAppsTextView.setTextColor(lightPurpleColor)
                        photoEditorAppArrowImageView.setColorFilter(lightPurpleColor)
                        ramadanAppArrowImageView.setColorFilter(lightPurpleColor)
                        notePadAppArrowImageView.setColorFilter(lightPurpleColor)
                        dailyExpenseManagerAppArrowImageView.setColorFilter(lightPurpleColor)
                        visitOurAppStoreShapeableImageView.setColorFilter(lightPurpleColor)
                        visitOurAppStoreArrowImageView.setColorFilter(lightPurpleColor)
                        aboutTextView.setTextColor(lightPurpleColor)
                        rateUsImageView.setColorFilter(lightPurpleColor)
                        rateUsArrowImageView.setColorFilter(lightPurpleColor)
                        feedbackImageView.setColorFilter(lightPurpleColor)
                        feedbackArrowImageView.setColorFilter(lightPurpleColor)
                        shareAppImageView.setColorFilter(lightPurpleColor)
                        shareAppArrowImageView.setColorFilter(lightPurpleColor)
                        privacyPolicyImageView.setColorFilter(lightPurpleColor)
                        privacyPolicyArrowImageView.setColorFilter(lightPurpleColor)
                        checkUpdateImageView.setColorFilter(lightPurpleColor)
                        checkUpdateArrowImageView.setColorFilter(lightPurpleColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightPurpleColor)
                        adLoadingInclude.progressBar.indeterminateTintList =
                            ColorStateList.valueOf(lightPurpleColor)
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
        colorSchemeAdapter = ColorSchemeAdapter(colorSchemeArrayList, this)
        val gridLayoutManager = GridLayoutManager(activityContext, 5, RecyclerView.VERTICAL, false)
        with(binding) {
            colorSchemeRecyclerView.layoutManager = gridLayoutManager
            colorSchemeRecyclerView.adapter = colorSchemeAdapter
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            toolbarTextView.typeface = typeface
            appearanceTextView.typeface = typeface
            textSizeTextView.typeface = typeface
            textSizeValueTextView.typeface = typeface
            dayAndNightModeTextView.typeface = typeface
            colorSchemeTextView.typeface = typeface
            enjoyMultipleColorsTextView.typeface = typeface
            moreAppsTextView.typeface = typeface
            photoEditorTextView.typeface = typeface
            ramadanAppTextView.typeface = typeface
            notePadAppTextView.typeface = typeface
            dailyExpenseManagerAppTextView.typeface = typeface
            visitOurAppStoreTextView.typeface = typeface
            aboutTextView.typeface = typeface
            rateUsTextView.typeface = typeface
            pleaseTellYourExperienceTextView.typeface = typeface
            feedbackTextView.typeface = typeface
            giveYourSuggestionsAndFeedbackTextView.typeface = typeface
            shareAppTextView.typeface = typeface
            shareOurAppToYourFriendsAndFamilyTextView.typeface = typeface
            privacyPolicyTextView.typeface = typeface
            readOurPrivacyPolicyTextView.typeface = typeface
            checkUpdateTextView.typeface = typeface
            versionNumberTextView.typeface = typeface
            adLoadingInclude.adIsLoadingTextView.typeface = typeface
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back_arrow_image_view -> {
                goBackToDashBoardActivity()
            }

            R.id.photo_editor_app_layout -> {
                openAppInPlayStore(sa10PhotoEditorAppPackage)
            }

            R.id.ramadan_app_layout -> {
                openAppInPlayStore(ramadanAppPackage)
            }

            R.id.note_pad_app_layout -> {
                openAppInPlayStore(notePadAppPackage)
            }

            R.id.daily_expense_manager_app_layout -> {
                openAppInPlayStore(dailyExpenseManagerAppPackage)
            }

            R.id.visit_our_app_store_layout -> {
                openGoogleAppStore()
            }

            R.id.rate_us_layout -> {
                showRateUsDialog()
            }

            R.id.feedback_layout -> {
                openFeedbackActivity()
            }

            R.id.share_app_layout -> {
                shareApp()
            }

            R.id.privacy_policy_layout -> {
                if (isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager))) {
                    openPrivacyPolicyActivity()
                } else {
                    Toasty.error(
                        activityContext,
                        R.string.check_your_internet_connection_toast_text,
                        Toasty.LENGTH_LONG
                    ).show()
                }
            }

            R.id.checkUpdateLayout -> {
                openAppInPlayStore(BuildConfig.APPLICATION_ID)
            }
        }
    }

    private fun openFeedbackActivity() =
        startActivity(Intent(activityContext, FeedbackActivity::class.java))

    private fun openPrivacyPolicyActivity() =
        startActivity(Intent(activityContext, PrivacyPolicyActivity::class.java))

    private fun showRateUsDialog() {
        val rateUsDialogLayoutBinding = RateUsDialogLayoutBinding.inflate(layoutInflater)

        val rateUsDialogBuilder = AlertDialog.Builder(activityContext)
        rateUsDialogBuilder.setView(rateUsDialogLayoutBinding.root)
        rateUsDialogBuilder.setCancelable(false)
        val rateUsAlertDialog = rateUsDialogBuilder.create()

        if (!activityContext.isFinishing && !activityContext.isDestroyed && !rateUsAlertDialog.isShowing) {
            rateUsAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            rateUsAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            rateUsAlertDialog.show()
        }

        with(rateUsDialogLayoutBinding) {
            rateUsDialogImageView.startAnimation(CommonFunctions.applyAnimation(activityContext))
            applyCustomFontOnRateUsDialogViews(this)
            applyColorSchemeORDayAndNightModeOnRateUsDialogViews(this)

            dismissRateUsDialogImageView.setOnClickListener { _: View? ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    rateUsAlertDialog.dismiss()
                }
            }

            rateUsButton.setOnClickListener { _: View? ->
                val rating = rateUsDialogLayoutBinding.ratingBar.rating
                if (rating in 1.0..3.0) {
                    rateUsDialogLayoutBinding.rateUsButton.visibility = INVISIBLE
                    rateUsDialogLayoutBinding.group.visibility = VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed(
                        { rateUsAlertDialog.dismiss() },
                        2000
                    )
                } else if (rating >= 4.0) {
                    openAppInPlayStore(BuildConfig.APPLICATION_ID)
                    if (!activityContext.isFinishing && !activityContext.isDestroyed) rateUsAlertDialog.dismiss()
                } else {
                    Toasty.error(
                        activityContext,
                        getString(R.string.please_rate_our_app_toast_text),
                        Toasty.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun applyCustomFontOnRateUsDialogViews(rateUsDialogLayoutBinding: RateUsDialogLayoutBinding) {
        with(rateUsDialogLayoutBinding) {
            rateOurAppTextView.typeface = typeface
            messageTextView.typeface = typeface
            rateUsButton.typeface = typeface
            thanksForYourFeedbackTextView.typeface = typeface
        }
    }

    private fun applyColorSchemeORDayAndNightModeOnRateUsDialogViews(
        rateUsDialogLayoutBinding: RateUsDialogLayoutBinding
    ) {
        with(rateUsDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                rateUsDialogRootLayout.setBackgroundResource(dialogBoxesDarkModeBackground)
                dismissRateUsDialogImageView.setColorFilter(whiteColor)
                rateOurAppTextView.setTextColor(whiteColor)
                messageTextView.setTextColor(whiteColor)
                rateUsButton.background.colorFilter =
                    PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                ratingBar.progressTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        activityContext,
                        R.color.ratingBarStarsCheckedStateDarkModeColor
                    )
                )
                rateUsButton.setTextColor(blackColor)
            } else {
                rateUsDialogRootLayout.setBackgroundResource(dialogBoxesLightModeBackground)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        dismissRateUsDialogImageView.setColorFilter(defaultColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(defaultColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        dismissRateUsDialogImageView.setColorFilter(darkYellowColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(darkYellowColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        dismissRateUsDialogImageView.setColorFilter(orangeColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(orangeColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        dismissRateUsDialogImageView.setColorFilter(lightGreenColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(lightGreenColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        dismissRateUsDialogImageView.setColorFilter(blueColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(blueColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        dismissRateUsDialogImageView.setColorFilter(cyanColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(cyanColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        dismissRateUsDialogImageView.setColorFilter(pinkColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(pinkColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        dismissRateUsDialogImageView.setColorFilter(darkBlueColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(darkBlueColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        dismissRateUsDialogImageView.setColorFilter(redColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(redColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        dismissRateUsDialogImageView.setColorFilter(lightPurpleColor)
                        ratingBar.progressTintList = ColorStateList.valueOf(lightPurpleColor)
                        rateUsButton.background.colorFilter =
                            PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
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
            putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            )
            val chooserIntent = Intent.createChooser(this, getString(R.string.share_via_text))
            if (this.resolveActivity(packageManager) != null) {
                startActivity(chooserIntent)
            } else {
                Toasty.error(
                    activityContext,
                    R.string.there_is_no_activity_available_to_handle_this_action_toast_text,
                    Toasty.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openAppInPlayStore(appPackageName: String) {
        val openAppInPlayStoreIntent = Intent()
        with(openAppInPlayStoreIntent) {
            action = Intent.ACTION_VIEW
            data = Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            startActivity(this)
        }
    }

    private fun openGoogleAppStore() {
        val openGoogleAppStoreIntent = Intent()
        with(openGoogleAppStoreIntent) {
            action = Intent.ACTION_VIEW
            data = Uri.parse("https://play.google.com/store/apps/developer?id=SAG+Inc.")
            startActivity(this)
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
            val isRamadanAppInstalledOrNot = appIsInstalledOrNot(ramadanAppPackage)
            val isNotePadAppInstalledOrNot = appIsInstalledOrNot(notePadAppPackage)
            val isDailyExpenseManagerAppInstalledOrNot =
                appIsInstalledOrNot(dailyExpenseManagerAppPackage)

            if (isPhotoEditorAppInstalledOrNot) {
                photoEditorAppLayout.visibility = GONE
            } else {
                photoEditorAppLayout.visibility = VISIBLE
            }

            if (isRamadanAppInstalledOrNot) {
                ramadanAppLayout.visibility = GONE
            } else {
                ramadanAppLayout.visibility = VISIBLE
            }

            if (isNotePadAppInstalledOrNot) {
                notePadAppLayout.visibility = GONE
            } else {
                notePadAppLayout.visibility = VISIBLE
            }

            if (isDailyExpenseManagerAppInstalledOrNot) {
                dailyExpenseManagerAppLayout.visibility = GONE
            } else {
                dailyExpenseManagerAppLayout.visibility = VISIBLE
            }
        }
    }

    override fun changeColorScheme(position: Int) {
        isSomethingChanged = true
        for (i in colorSchemeArrayList.indices) {
            val colorSchemeModel = colorSchemeArrayList[i]
            colorSchemeModel.isSelected = position == colorSchemeModel.id
            prefs.colorSchemeValue = position
            colorSchemeAdapter.notifyDataSetChanged()
        }
        applyColorSchemeORDayAndNightMode()
        applyColorSchemeORDayAndNightModeOnSwitch()
    }

}