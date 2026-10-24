package com.sag.todo.task.reminder.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.slider.Slider
import com.sag.todo.task.reminder.BuildConfig
import com.sag.todo.task.reminder.R
import com.sag.todo.task.reminder.core.R as CoreR
import com.sag.todo.task.reminder.base.BaseActivity
import com.sag.todo.task.reminder.databinding.ActivitySettingsBinding
import com.sag.todo.task.reminder.enums.Visibility
import com.sag.todo.task.reminder.utils.AppConstants.changeVisibility
import com.sag.todo.task.reminder.utils.AppConstants.isSomethingChanged
import com.sag.todo.task.reminder.utils.AppConstants.keepActivityOn
import com.sag.todo.task.reminder.utils.AppConstants.openAppInPlayStore
import com.sag.todo.task.reminder.utils.AppConstants.openGoogleAppStore
import com.sag.todo.task.reminder.utils.AppConstants.openPrivacyPolicyActivity
import com.sag.todo.task.reminder.utils.FabRateUsAndApplyAnimation
import com.sag.todo.task.reminder.databinding.RateUsDialogLayoutBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }
    @Inject
    @FabRateUsAndApplyAnimation
    lateinit var animation: Animation
    private val appLanguageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startActivity(Intent(activityContext, DashBoardActivity::class.java))
                finishAffinity()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            keepActivityOn(activityContext)

            setSelectedLanguageFlag()

            versionNumberTV.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)
            textSizeValueTV.text = String.format(Locale.getDefault(), "%d", prefs.textSizeValue)
            textSizeSlider.value = prefs.textSizeValue.toFloat()

            backArrowIV.setOnClickListener(this@SettingsActivity)
            moreAppsChildLayout.setOnClickListener(this@SettingsActivity)
            rateUsLayout.setOnClickListener(this@SettingsActivity)
            feedbackLayout.setOnClickListener(this@SettingsActivity)
            shareAppLayout.setOnClickListener(this@SettingsActivity)
            privacyPolicyLayout.setOnClickListener(this@SettingsActivity)
            checkUpdateLayout.setOnClickListener(this@SettingsActivity)
            selectLanguageChildLayout.setOnClickListener(this@SettingsActivity)
            textSizeSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    prefs.textSizeValue = slider.value.toInt()
                    isSomethingChanged.value = true
                }
            })

            textSizeSlider.addOnChangeListener { _, value, _ ->
                if (value.toInt() < 14) {
                    textSizeValueTV.text = String.format(Locale.getDefault(), "%d", 14)
                    textSizeSlider.value = 14F
                } else
                    textSizeValueTV.text = String.format(Locale.getDefault(), "%d", value.toInt())
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> callBackPressed()
            R.id.selectLanguageChildLayout -> appLanguageLauncher.launch(Intent(activityContext, AppLanguageActivity::class.java))
            R.id.moreAppsChildLayout -> openGoogleAppStore(activityContext)
            R.id.rateUsLayout -> showRateUsDialog()
            R.id.feedbackLayout -> openFeedbackActivity()
            R.id.shareAppLayout -> shareApp()
            R.id.privacyPolicyLayout -> openPrivacyPolicyActivity(activityContext, internetController.isInternetConnected, toastController)
            R.id.checkUpdateLayout -> openAppInPlayStore(activityContext, BuildConfig.APPLICATION_ID)
        }
    }

    private fun openFeedbackActivity() = startActivity(Intent(activityContext, FeedbackActivity::class.java))

    private fun showRateUsDialog() {
        val rateUsDialogLayoutBinding = RateUsDialogLayoutBinding.inflate(layoutInflater)

        val rateUsDialogBuilder = AlertDialog.Builder(activityContext)
        rateUsDialogBuilder.apply {
            setView(rateUsDialogLayoutBinding.root)
            setCancelable(true)
        }
        val rateUsAlertDialog = rateUsDialogBuilder.create()
        if (!activityContext.isFinishing && !activityContext.isDestroyed && !rateUsAlertDialog.isShowing) {
            rateUsAlertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            rateUsAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            rateUsAlertDialog.show()
        }

        rateUsDialogLayoutBinding.apply {
            rateUsDialogIV.startAnimation(animation)

            dismissRateUsDialogIV.setOnClickListener { _: View? ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed)
                    rateUsAlertDialog.dismiss()
            }

            rateUsButton.setOnClickListener { _: View? ->
                val rating = rateUsDialogLayoutBinding.ratingBar.rating
                if (rating in 1.0..3.0) {
                    rateUsDialogLayoutBinding.rateUsButton.changeVisibility(Visibility.INVISIBLE)
                    rateUsDialogLayoutBinding.group.changeVisibility(Visibility.VISIBLE)
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                            rateUsAlertDialog.dismiss()
                        } }, 2000)
                } else if (rating >= 4.0) {
                    openAppInPlayStore(activityContext, BuildConfig.APPLICATION_ID)
                    if (!activityContext.isFinishing && !activityContext.isDestroyed)
                        rateUsAlertDialog.dismiss()
                } else
                    toastController.showToast(activityContext, getString(CoreR.string.please_rate_our_app_toast_text), false)
            }
        }
    }

    private fun shareApp() {
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            if (this.resolveActivity(packageManager) != null) {
                val chooserIntent = Intent.createChooser(this, getString(CoreR.string.share_via_text))
                startActivity(chooserIntent)
            } else
                toastController.showToast(activityContext, getString(CoreR.string.there_is_no_activity_available_to_handle_this_action_toast_text), false)
        }
    }

    private fun ActivitySettingsBinding.setSelectedLanguageFlag() =
        selectedLanguageFlagIV.setImageResource(when (prefs.selectedLanguageCode) {
            "ur" -> CoreR.drawable.urdu_flag
            "ar" -> CoreR.drawable.arabic_flag
            "en" -> CoreR.drawable.english_flag
            "af" -> CoreR.drawable.african_flag
            "bn" -> CoreR.drawable.bengali_flag
            "bho" -> CoreR.drawable.bhojpuri_flag
            "bg" -> CoreR.drawable.bulgarian_flag
            "my" -> CoreR.drawable.myanmar_burmese_flag
            "zh-CN" -> CoreR.drawable.chinese_simplified_flag
            "zh-TW" -> CoreR.drawable.chinese_traditional_flag
            "cs" -> CoreR.drawable.czech_flag
            "da" -> CoreR.drawable.danish_flag
            "nl" -> CoreR.drawable.dutch_flag
            "tl" -> CoreR.drawable.filipino_flag
            "fi" -> CoreR.drawable.finnish_flag
            "fr" -> CoreR.drawable.french_flag
            "de" -> CoreR.drawable.german_flag
            "el" -> CoreR.drawable.greek_flag
            "gn" -> CoreR.drawable.guarani_flag
            "ha" -> CoreR.drawable.hausa_flag
            "iw" -> CoreR.drawable.hebrew_flag
            "hi" -> CoreR.drawable.hindi_flag
            "hu" -> CoreR.drawable.hungarian_flag
            "id" -> CoreR.drawable.indonesian_flag
            "it" -> CoreR.drawable.italian_flag
            "ja" -> CoreR.drawable.japanese_flag
            "ko" -> CoreR.drawable.korean_flag
            "ms" -> CoreR.drawable.malay_flag
            "mr" -> CoreR.drawable.marathi_flag
            "om" -> CoreR.drawable.oromo_flag
            "fa" -> CoreR.drawable.persian_flag
            "pl" -> CoreR.drawable.polish_flag
            "pt" -> CoreR.drawable.portuguese_portugal
            "qu" -> CoreR.drawable.quechua_flag
            "ro" -> CoreR.drawable.romanian_flag
            "ru" -> CoreR.drawable.russain_flag
            "sr" -> CoreR.drawable.serbian_flag
            "es" -> CoreR.drawable.spanish_flag
            "sw" -> CoreR.drawable.swahili_flag
            "sv" -> CoreR.drawable.swedish_flag
            "ta" -> CoreR.drawable.tamil_flag
            "te" -> CoreR.drawable.telugu_flag
            "th" -> CoreR.drawable.thai_flag
            "tr" -> CoreR.drawable.turkish_flag
            "uk" -> CoreR.drawable.ukrainian_flag
            "vi" -> CoreR.drawable.vietnamese_flag
            "yo" -> CoreR.drawable.yoruba_flag
            "zu" -> CoreR.drawable.zulu_flag
            else -> 0
        })

    /*private fun appIsInstalledOrNot(appPackageName: String): Boolean {
        try {
            packageManager.getPackageInfo(appPackageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (_: PackageManager.NameNotFoundException) {
        }
        return false
    }*/

    override fun handleActivitiesBackPressed() = finish()
}