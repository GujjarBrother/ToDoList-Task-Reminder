package com.todo.list.activities

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.todo.list.R
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivitySplashBinding
import com.todo.list.utils.CommonFunctions
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var selectedColor = 0
    private var selectedProgressBarBackground: Drawable? = null
    private lateinit var valueAnimator: ValueAnimator
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { _ ->
        /*if (result.resultCode != RESULT_OK) {
            Toast.makeText(activityContext, "Update not available...!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(activityContext, "Update available...!", Toast.LENGTH_LONG).show()
        }*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkForAnAppUpdate()

        makeFullScreenActivity(activityContext)

        lifecycleScope.launch(Dispatchers.IO) {
            CommonFunctions.getViewModel(activityContext).updateCompletedAndTimeUpTasks(true, Date(System.currentTimeMillis()))
        }

        when(prefs.colorSchemeValue) {
            0 -> {
                selectedColor = defaultColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.default_progress_bar_background)
            }

            1 -> {
                selectedColor = darkYellowColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.dark_yellow_progress_bar_background)
            }

            2 -> {
                selectedColor = orangeColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.orange_progress_bar_background)
            }

            3 -> {
                selectedColor = lightGreenColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.light_green_progress_bar_background)
            }

            4 -> {
                selectedColor = blueColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.blue_progress_bar_background)
            }

            5 -> {
                selectedColor = cyanColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.cyan_progress_bar_background)
            }

            6 -> {
                selectedColor = pinkColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.pink_progress_bar_background)
            }

            7 -> {
                selectedColor = darkBlueColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.dark_blue_progress_bar_background)
            }

            8 -> {
                selectedColor = redColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.red_progress_bar_background)
            }

            9 -> {
                selectedColor = lightPurpleColor
                selectedProgressBarBackground = ContextCompat.getDrawable(activityContext, R.drawable.light_purple_progress_bar_background)
            }
        }

        with(binding) {
            applyCustomFont()
            applyLightAndDarkMode()
            applySplashAnimation()
            applyAnimationOnProgressBar()
        }
    }

    private fun checkForAnAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(activityContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, activityResultLauncher, AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    private fun ActivitySplashBinding.applyAnimationOnProgressBar() {
        valueAnimator = ValueAnimator.ofInt(0, splashLoadingProgressBar.max)
        valueAnimator.setDuration(5000)
        valueAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue
            if (animatedValue is Int) {
                splashLoadingProgressBar.progress = animatedValue
                loadingPercentageTV.text = String.format(Locale.getDefault(), "%d%s", animatedValue, "%")
                if (animatedValue == 100) {
                    checkUserSignInOrSignOutStatus()
                }
            }
        }
        valueAnimator.start()
    }

    private fun checkUserSignInOrSignOutStatus() {
        if (prefs.isUserSignIn) {
            startActivity(Intent(activityContext, DashBoardActivity::class.java))
        } else {
            startActivity(Intent(activityContext, SignInActivity::class.java))
        }
        finish()
    }

    private fun ActivitySplashBinding.applySplashAnimation() =
        splashIV.startAnimation(AnimationUtils.loadAnimation(activityContext, R.anim.splash_image_animation))

    private fun ActivitySplashBinding.applyLightAndDarkMode() {
        if (prefs.isDarkModeEnable) {
            changeStatusBarColor(activityContext, screensNightModeColor)
            rootLayout.setBackgroundColor(screensNightModeColor)
            taskTV.setTextColor(lightBlueColor)
            reminderTV.setTextColor(lightBlueColor)
            loadingTV.setTextColor(lightBlueColor)
            loadingPercentageTV.setTextColor(lightBlueColor)
            splashLoadingProgressBar.progressDrawable = ContextCompat.getDrawable(activityContext, R.drawable.dark_mode_progress_bar_background)
        } else {
            changeStatusBarColor(activityContext, defaultColor)
            taskTV.setTextColor(selectedColor)
            loadingTV.setTextColor(selectedColor)
            loadingPercentageTV.setTextColor(selectedColor)
            splashLoadingProgressBar.progressDrawable = selectedProgressBarBackground
        }
    }

    private fun ActivitySplashBinding.applyCustomFont() {
        taskTV.typeface = typeface
        reminderTV.typeface = typeface
        loadingTV.typeface = typeface
        loadingPercentageTV.typeface = typeface
    }
}