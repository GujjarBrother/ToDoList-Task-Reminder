package com.sag.todo.list.task.reminder.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.drawable.toDrawable
import com.sag.todo.list.task.reminder.BuildConfig
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.adsPlugin.bannerAd.BannerAdController
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivitySettingsBinding
import com.sag.todo.list.task.reminder.databinding.RateUsDialogLayoutBinding
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.applyAnimation
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeVisibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.keepActivityOn
import com.sag.todo.list.task.reminder.utils.CommonFunctions.openAppInPlayStore
import com.sag.todo.list.task.reminder.utils.CommonFunctions.openGoogleAppStore
import com.sag.todo.list.task.reminder.utils.CommonFunctions.openPrivacyPolicyActivity
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
                isInternetConnected = internetController.isInternetConnected,
                adID = getString(R.string.settingsScreenBannerAdId)
            )

            keepActivityOn(activityContext)

            versionNumberTV.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)
            textSizeValueTV.text = String.format(Locale.getDefault(), "%d", prefs.textSizeValue)
//            textSizeSeekBar.progress = prefs.textSizeValue

            backArrowIV.setOnClickListener(this@SettingsActivity)
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
            R.id.visitOurAppStoreLayout -> openGoogleAppStore(activityContext)
            R.id.rateUsLayout -> showRateUsDialog()
            R.id.feedbackLayout -> openFeedbackActivity()
            R.id.shareAppLayout -> shareApp()
            R.id.privacyPolicyLayout -> openPrivacyPolicyActivity(
                activityContext,
                internetController.isInternetConnected
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
            rateUsAlertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            rateUsAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            rateUsAlertDialog.show()
        }

        with(rateUsDialogLayoutBinding) {
            rateUsDialogIV.startAnimation(applyAnimation(activityContext))

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
                    toastController.showToast(activityContext, getString(R.string.please_rate_our_app_toast_text), false)
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
                toastController.showToast(activityContext, getString(R.string.there_is_no_activity_available_to_handle_this_action_toast_text), false)
            }
        }
    }

    /*private fun appIsInstalledOrNot(appPackageName: String): Boolean {
        try {
            packageManager.getPackageInfo(appPackageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (_: PackageManager.NameNotFoundException) {
        }
        return false
    }*/
}