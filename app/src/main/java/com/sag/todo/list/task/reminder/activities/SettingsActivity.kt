package com.sag.todo.list.task.reminder.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.Slider
import com.sag.todo.list.task.reminder.BuildConfig
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.adsPlugin.bannerAd.BannerAdController
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivitySettingsBinding
import com.sag.todo.list.task.reminder.databinding.RateUsDialogLayoutBinding
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeVisibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.isSomethingChanged
import com.sag.todo.list.task.reminder.utils.CommonFunctions.keepActivityOn
import com.sag.todo.list.task.reminder.utils.CommonFunctions.openAppInPlayStore
import com.sag.todo.list.task.reminder.utils.CommonFunctions.openGoogleAppStore
import com.sag.todo.list.task.reminder.utils.CommonFunctions.openPrivacyPolicyActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class SettingsActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    @Inject
    @Named(value = "FabRateUsAndApplyAnimation")
    lateinit var animation: Animation

    private val appLanguageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startActivity(Intent(activityContext, DashBoardActivity::class.java))
                finish()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultColor = ContextCompat.getColor(activityContext, R.color.defaultColor)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(defaultColor),
            navigationBarStyle = SystemBarStyle.dark(defaultColor)
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.root,
                isInternetConnected = internetController.isInternetConnected,
                adID = getString(R.string.settingsScreenBannerAdId)
            )

            keepActivityOn(activityContext)

            setSelectedLanguageFlag()

            versionNumberTV.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)
            textSizeValueTV.text = String.format(Locale.getDefault(), "%d", prefs.textSizeValue)
            textSizeSlider.value = prefs.textSizeValue.toFloat()

            backArrowIV.setOnClickListener(this@SettingsActivity)
            visitOurAppStoreLayout.setOnClickListener(this@SettingsActivity)
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

            textSizeSlider.addOnChangeListener(object : Slider.OnChangeListener {
                override fun onValueChange(
                    slider: Slider,
                    value: Float,
                    fromUser: Boolean
                ) {
                    if (value.toInt() < 14) {
                        textSizeValueTV.text = String.format(Locale.getDefault(), "%d", 14)
                        textSizeSlider.value = 14F
                    } else {
                        textSizeValueTV.text = String.format(Locale.getDefault(), "%d", value.toInt())
                    }
                }
            })
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> finish()
            R.id.selectLanguageChildLayout -> appLanguageLauncher.launch(Intent(activityContext,
                AppLanguageActivity::class.java))
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
            rateUsDialogIV.startAnimation(animation)

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

    private fun ActivitySettingsBinding.setSelectedLanguageFlag() =
        selectedLanguageFlagIV.setImageResource(when (prefs.selectedLanguageCode) {
            "ur" -> R.drawable.urdu_flag
            "ar" -> R.drawable.arabic_flag
            "en" -> R.drawable.english_flag
            "af" -> R.drawable.african_flag
            "bn" -> R.drawable.bengali_flag
            "bho" -> R.drawable.bhojpuri_flag
            "bg" -> R.drawable.bulgarian_flag
            "my" -> R.drawable.myanmar_burmese_flag
            "zh-CN" -> R.drawable.chinese_simplified_flag
            "zh-TW" -> R.drawable.chinese_traditional_flag
            "cs" -> R.drawable.czech_flag
            "da" -> R.drawable.danish_flag
            "nl" -> R.drawable.dutch_flag
            "tl" -> R.drawable.filipino_flag
            "fi" -> R.drawable.finnish_flag
            "fr" -> R.drawable.french_flag
            "de" -> R.drawable.german_flag
            "el" -> R.drawable.greek_flag
            "gn" -> R.drawable.guarani_flag
            "ha" -> R.drawable.hausa_flag
            "iw" -> R.drawable.hebrew_flag
            "hi" -> R.drawable.hindi_flag
            "hu" -> R.drawable.hungarian_flag
            "id" -> R.drawable.indonesian_flag
            "it" -> R.drawable.italian_flag
            "ja" -> R.drawable.japanese_flag
            "ko" -> R.drawable.korean_flag
            "ms" -> R.drawable.malay_flag
            "mr" -> R.drawable.marathi_flag
            "om" -> R.drawable.oromo_flag
            "fa" -> R.drawable.persian_flag
            "pl" -> R.drawable.polish_flag
            "pt" -> R.drawable.portuguese_portugal
            "qu" -> R.drawable.quechua_flag
            "ro" -> R.drawable.romanian_flag
            "ru" -> R.drawable.russain_flag
            "sr" -> R.drawable.serbian_flag
            "es" -> R.drawable.spanish_flag
            "sw" -> R.drawable.swahili_flag
            "sv" -> R.drawable.swedish_flag
            "ta" -> R.drawable.tamil_flag
            "te" -> R.drawable.telugu_flag
            "th" -> R.drawable.thai_flag
            "tr" -> R.drawable.turkish_flag
            "uk" -> R.drawable.ukrainian_flag
            "vi" -> R.drawable.vietnamese_flag
            "yo" -> R.drawable.yoruba_flag
            "zu" -> R.drawable.zulu_flag
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
}