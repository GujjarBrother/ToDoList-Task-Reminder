package com.todo.list.activities

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.todo.list.R
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivitySplashBinding
import com.todo.list.utils.CommonFunctions
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
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
        setContentView(binding.root)

        checkForAnAppUpdate()
        makeFullScreenActivity(activityContext)

        lifecycleScope.launch(Dispatchers.IO) {
            CommonFunctions.getViewModel(activityContext).updateCompletedAndTimeUpTasks(true, Date(System.currentTimeMillis()))
        }

        with(binding) {
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
        with(valueAnimator) {
            setDuration(5000)
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue
                if (animatedValue is Int) {
                    splashLoadingProgressBar.progress = animatedValue
                    loadingPercentageTV.text = String.format(Locale.getDefault(), "%d%s", animatedValue, "%")
                    if (animatedValue == 100) {
                        checkUserSignInOrSignOutStatus()
                    }
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
}