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
import com.todo.list.enums.Visibility
import com.todo.list.models.ColorSchemeModel
import com.todo.list.models.SelectedColors
import com.todo.list.utils.ColorsUtils.blackColor
import com.todo.list.utils.ColorsUtils.blueColor
import com.todo.list.utils.ColorsUtils.cardsNightModeColor
import com.todo.list.utils.ColorsUtils.cyanColor
import com.todo.list.utils.ColorsUtils.darkBlueColor
import com.todo.list.utils.ColorsUtils.darkModeTextColor
import com.todo.list.utils.ColorsUtils.darkYellowColor
import com.todo.list.utils.ColorsUtils.defaultColor
import com.todo.list.utils.ColorsUtils.getContextCompatColor
import com.todo.list.utils.ColorsUtils.getSelectedColor
import com.todo.list.utils.ColorsUtils.lightBlueColor
import com.todo.list.utils.ColorsUtils.lightGreenColor
import com.todo.list.utils.ColorsUtils.lightPurpleColor
import com.todo.list.utils.ColorsUtils.orangeColor
import com.todo.list.utils.ColorsUtils.pinkColor
import com.todo.list.utils.ColorsUtils.redColor
import com.todo.list.utils.ColorsUtils.screensNightModeColor
import com.todo.list.utils.ColorsUtils.snowWhiteColor
import com.todo.list.utils.ColorsUtils.switchTrackOffColor
import com.todo.list.utils.ColorsUtils.whiteColor
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
    private lateinit var selectedColors: SelectedColors

    private val sa10PhotoEditorAppPackage = "com.editor.sa10photoeditor"
    private val dailyExpenseManagerAppPackage = "com.daily.manager"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedColors = getSelectedColor(context = activityContext, prefs = prefs)

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = isInternetConnectedORNot(
                    (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)
                ), adID = getString(R.string.settingsScreenBannerAdId)
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
                    colorSchemeCV.changeVisibility(Visibility.GONE.ordinal)
                } else {
                    colorSchemeCV.changeVisibility(Visibility.VISIBLE.ordinal)
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
                include.root.changeVisibility(Visibility.VISIBLE.ordinal)
                lightAndDarkModeIV.setImageResource(R.drawable.sun_image)
                lightAndDarkModeTV.setText(R.string.light_mode_text)
                switchTrackDrawable.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, snowWhiteColor), PorterDuff.Mode.SRC_IN
                )
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
                include.root.changeVisibility(Visibility.GONE.ordinal)
                lightAndDarkModeIV.setImageResource(R.drawable.moon_image)
                lightAndDarkModeTV.setText(R.string.dark_mode_text)
                switchTrackDrawable.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(
                        activityContext,
                        switchTrackOffColor
                    ), PorterDuff.Mode.SRC_IN
                )
                lightAndDarkModeSwitch.thumbDrawable = ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb)
            }
        }
    }

    private fun goBackToDashBoardActivity() = finish()

    private fun applyLightAndDarkMode() {
        with(binding) {
            if (prefs.isDarkModeEnable) {
                changeStatusBarColor(
                    activityContext,
                    getContextCompatColor(activityContext, screensNightModeColor)
                )
                colorSchemeCV.changeVisibility(Visibility.GONE.ordinal)
                toolbar.setBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        screensNightModeColor
                    )
                )
                rootLayout.setBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        screensNightModeColor
                    )
                )
                appearanceCV.setCardBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        cardsNightModeColor
                    )
                )
                appearanceTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                appearanceIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                textSizeTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                textSizeValueTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                textSizeSeekBar.progressTintList =
                    ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor))
                textSizeSeekBar.thumbTintList =
                    ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor))
                smallAIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                capitalAIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                lightAndDarkModeIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                lightAndDarkModeTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                moreAppsCV.setCardBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        cardsNightModeColor
                    )
                )
                moreAppsTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                photoEditorTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                photoEditorAppArrowIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                dailyExpenseManagerAppTV.setTextColor(
                    getContextCompatColor(
                        activityContext,
                        whiteColor
                    )
                )
                dailyExpenseManagerAppArrowIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                visitOurAppStoreShapeableIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                visitOurAppStoreTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                visitOurAppStoreArrowIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                aboutCV.setCardBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        cardsNightModeColor
                    )
                )
                aboutTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                rateUsIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                rateUsTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                rateUsArrowIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                feedbackIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                feedbackTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                feedbackArrowIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                shareAppIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                shareAppTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                shareAppArrowIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                privacyPolicyIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                privacyPolicyTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                privacyPolicyArrowIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                checkUpdateIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                checkUpdateTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                checkUpdateArrowIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                adLoadingInclude.adIsLoadingTextView.setTextColor(
                    getContextCompatColor(
                        activityContext,
                        whiteColor
                    )
                )
                adLoadingInclude.progressBar.indeterminateTintList =
                    ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
            } else {
                colorSchemeCV.changeVisibility(Visibility.VISIBLE.ordinal)
                rootLayout.setBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        snowWhiteColor
                    )
                )
                appearanceCV.setCardBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        whiteColor
                    )
                )
                textSizeTV.setTextColor(getContextCompatColor(activityContext, defaultColor))
                lightAndDarkModeTV.setTextColor(
                    getContextCompatColor(
                        activityContext,
                        defaultColor
                    )
                )
                colorSchemeCV.setCardBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        whiteColor
                    )
                )
                moreAppsCV.setCardBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        whiteColor
                    )
                )
                photoEditorTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                dailyExpenseManagerAppTV.setTextColor(
                    getContextCompatColor(
                        activityContext,
                        blackColor
                    )
                )
                visitOurAppStoreTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                aboutCV.setCardBackgroundColor(getContextCompatColor(activityContext, whiteColor))
                rateUsTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                feedbackTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                shareAppTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                privacyPolicyTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                checkUpdateTV.setTextColor(getContextCompatColor(activityContext, blackColor))

                changeStatusBarColor(activityContext, selectedColors.originalColor)
                toolbar.setBackgroundColor(selectedColors.originalColor)
                appearanceTV.setTextColor(selectedColors.originalColor)
                appearanceIV.setColorFilter(selectedColors.originalColor)
                textSizeValueTV.setTextColor(selectedColors.originalColor)
                textSizeSeekBar.progressTintList =
                    ColorStateList.valueOf(selectedColors.originalColor)
                textSizeSeekBar.thumbTintList = ColorStateList.valueOf(selectedColors.originalColor)
                smallAIV.setColorFilter(selectedColors.originalColor)
                capitalAIV.setColorFilter(selectedColors.originalColor)
                lightAndDarkModeIV.setColorFilter(selectedColors.originalColor)
                colorSchemeTV.setTextColor(selectedColors.originalColor)
                colorSchemeIV.setColorFilter(selectedColors.originalColor)
                moreAppsTV.setTextColor(selectedColors.originalColor)
                photoEditorAppArrowIV.setColorFilter(selectedColors.originalColor)
                dailyExpenseManagerAppArrowIV.setColorFilter(selectedColors.originalColor)
                visitOurAppStoreShapeableIV.setColorFilter(selectedColors.originalColor)
                visitOurAppStoreArrowIV.setColorFilter(selectedColors.originalColor)
                aboutTV.setTextColor(selectedColors.originalColor)
                rateUsIV.setColorFilter(selectedColors.originalColor)
                rateUsArrowIV.setColorFilter(selectedColors.originalColor)
                feedbackIV.setColorFilter(selectedColors.originalColor)
                feedbackArrowIV.setColorFilter(selectedColors.originalColor)
                shareAppIV.setColorFilter(selectedColors.originalColor)
                shareAppArrowIV.setColorFilter(selectedColors.originalColor)
                privacyPolicyIV.setColorFilter(selectedColors.originalColor)
                privacyPolicyArrowIV.setColorFilter(selectedColors.originalColor)
                checkUpdateIV.setColorFilter(selectedColors.originalColor)
                checkUpdateArrowIV.setColorFilter(selectedColors.originalColor)
                adLoadingInclude.adIsLoadingTextView.setTextColor(selectedColors.originalColor)
                adLoadingInclude.progressBar.indeterminateTintList =
                    ColorStateList.valueOf(selectedColors.originalColor)
            }
        }
    }

    private fun showColorsForColorScheme() {
        with(colorSchemeArrayList) {
            add(ColorSchemeModel(0, getContextCompatColor(activityContext, defaultColor), false))
            add(ColorSchemeModel(1, getContextCompatColor(activityContext, darkYellowColor), false))
            add(ColorSchemeModel(2, getContextCompatColor(activityContext, orangeColor), false))
            add(ColorSchemeModel(3, getContextCompatColor(activityContext, lightGreenColor), false))
            add(ColorSchemeModel(4, getContextCompatColor(activityContext, blueColor), false))
            add(ColorSchemeModel(5, getContextCompatColor(activityContext, cyanColor), false))
            add(ColorSchemeModel(6, getContextCompatColor(activityContext, pinkColor), false))
            add(ColorSchemeModel(7, getContextCompatColor(activityContext, darkBlueColor), false))
            add(ColorSchemeModel(8, getContextCompatColor(activityContext, redColor), false))
            add(
                ColorSchemeModel(
                    9,
                    getContextCompatColor(activityContext, lightPurpleColor),
                    false
                )
            )
        }
        colorSchemeArrayList[prefs.colorSchemeValue].isSelected = true
        colorSchemeAdapter = ColorSchemeAdapter(colorSchemeArrayList) { colorSchemeModel, newSelectedColorPosition ->
            isSomethingChanged.value = true
            !colorSchemeArrayList[prefs.lastTimeSelectedColorValue].isSelected
            for (i in colorSchemeArrayList.indices) {
                val csm = colorSchemeArrayList[i]
                csm.isSelected = colorSchemeModel.id == csm.id
            }
            colorSchemeAdapter.notifyItemChanged(newSelectedColorPosition)
            colorSchemeAdapter.notifyItemChanged(prefs.lastTimeSelectedColorValue)
            prefs.colorSchemeValue = colorSchemeModel.id
            prefs.lastTimeSelectedColorValue = newSelectedColorPosition
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
                    rateUsDialogLayoutBinding.rateUsButton.changeVisibility(Visibility.INVISIBLE.ordinal)
                    rateUsDialogLayoutBinding.group.changeVisibility(Visibility.VISIBLE.ordinal)
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
                rootLayout.background.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(
                        activityContext,
                        screensNightModeColor
                    ), PorterDuff.Mode.SRC_IN
                )
                dismissRateUsDialogIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                rateOurAppTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                messageTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                ratingBar.progressTintList =
                    ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor))
                rateUsButton.setBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        lightBlueColor
                    )
                )
                rateUsButton.setTextColor(getContextCompatColor(activityContext, blackColor))
                thanksForYourFeedbackTV.setTextColor(
                    getContextCompatColor(
                        activityContext,
                        darkModeTextColor
                    )
                )
            } else {
                rootLayout.setBackgroundResource(R.drawable.dialog_boxes_light_mode_background)
                dismissRateUsDialogIV.setColorFilter(
                    getContextCompatColor(
                        activityContext,
                        selectedColors.originalColor
                    )
                )
                ratingBar.progressTintList = ColorStateList.valueOf(
                    getContextCompatColor(
                        activityContext,
                        selectedColors.originalColor
                    )
                )
                rateUsButton.setBackgroundColor(
                    getContextCompatColor(
                        activityContext,
                        selectedColors.originalColor
                    )
                )
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
                photoEditorAppLayout.changeVisibility(Visibility.GONE.ordinal)
            } else {
                photoEditorAppLayout.changeVisibility(Visibility.VISIBLE.ordinal)
            }

            if (isDailyExpenseManagerAppInstalledOrNot) {
                dailyExpenseManagerAppLayout.changeVisibility(Visibility.GONE.ordinal)
            } else {
                dailyExpenseManagerAppLayout.changeVisibility(Visibility.VISIBLE.ordinal)
            }
        }
    }
}