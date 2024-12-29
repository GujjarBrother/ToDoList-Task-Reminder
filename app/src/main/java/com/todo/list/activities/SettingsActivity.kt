package com.todo.list.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.todo.list.BuildConfig
import com.todo.list.R
import com.todo.list.adsPlugin.bannerAd.BannerAdController
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivitySettingsBinding
import com.todo.list.databinding.RateUsDialogLayoutBinding
import com.todo.list.enums.Visibility
import com.todo.list.utils.CommonFunctions
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.openAppInPlayStore
import com.todo.list.utils.CommonFunctions.openGoogleAppStore
import com.todo.list.utils.CommonFunctions.openPrivacyPolicyActivity
import es.dmoral.toasty.Toasty
import java.util.Locale

class SettingsActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = isInternetConnectedORNot(
                    (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)
                ), adID = getString(R.string.settingsScreenBannerAdId)
            )

            keepActivityOn(activityContext)

            versionNumberTV.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)
            textSizeValueTV.text = String.format(Locale.getDefault(), "%d", prefs.textSizeValue)
//            textSizeSeekBar.progress = prefs.textSizeValue

            backArrowIV.setOnClickListener(this@SettingsActivity)
            photoEditorAppLayout.setOnClickListener(this@SettingsActivity)
            dailyExpenseManagerAppLayout.setOnClickListener(this@SettingsActivity)
            visitOurAppStoreLayout.setOnClickListener(this@SettingsActivity)
            rateUsLayout.setOnClickListener(this@SettingsActivity)
            feedbackLayout.setOnClickListener(this@SettingsActivity)
            shareAppLayout.setOnClickListener(this@SettingsActivity)
            privacyPolicyLayout.setOnClickListener(this@SettingsActivity)
            checkUpdateLayout.setOnClickListener(this@SettingsActivity)
            /*textSizeSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    if (i < 14) {
                        textSizeValueTV.text = String.format(Locale.getDefault(), "%d", 14)
                        textSizeSeekBar.progress = 14
                    } else {
                        textSizeValueTV.text = String.format(Locale.getDefault(), "%d", i)
                    }
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    prefs.textSizeValue = slider.value.toInt()
                    isSomethingChanged.value = true
                }
            })*/
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackToDashBoardActivity()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun goBackToDashBoardActivity() = finish()

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> goBackToDashBoardActivity()
            R.id.photoEditorAppLayout -> openAppInPlayStore(
                activityContext,
                "com.editor.sa10photoeditor"
            )

            R.id.dailyExpenseManagerAppLayout -> openAppInPlayStore(
                activityContext,
                "com.daily.manager"
            )

            R.id.visitOurAppStoreLayout -> openGoogleAppStore(activityContext)
            R.id.rateUsLayout -> showRateUsDialog()
            R.id.feedbackLayout -> openFeedbackActivity()
            R.id.shareAppLayout -> shareApp()
            R.id.privacyPolicyLayout -> openPrivacyPolicyActivity(
                activityContext,
                isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager))
            )

            R.id.checkUpdateLayout -> openAppInPlayStore(
                activityContext,
                BuildConfig.APPLICATION_ID
            )
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
            if (appIsInstalledOrNot("com.editor.sa10photoeditor")) {
                photoEditorAppLayout.changeVisibility(Visibility.GONE.ordinal)
            } else {
                photoEditorAppLayout.changeVisibility(Visibility.VISIBLE.ordinal)
            }

            if (appIsInstalledOrNot("com.daily.manager")) {
                dailyExpenseManagerAppLayout.changeVisibility(Visibility.GONE.ordinal)
            } else {
                dailyExpenseManagerAppLayout.changeVisibility(Visibility.VISIBLE.ordinal)
            }
        }
    }
}