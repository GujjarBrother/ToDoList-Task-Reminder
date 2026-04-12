package com.sag.todo.list.task.reminder.activities

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivitySplashBinding
import com.sag.todo.list.task.reminder.utils.FetchRemoteConfig
import com.sag.todo.list.task.reminder.utils.RemoteConfigValues.IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN
import com.sag.todo.list.task.reminder.utils.SplashImageAnimation
import com.sag.todo.list.task.reminder.viewModels.TasksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    @Inject
    @SplashImageAnimation
    lateinit var animation: Animation
    private lateinit var valueAnimator: ValueAnimator
    private val tasksViewModel: TasksViewModel by viewModels()
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        when (it.resultCode) {
            RESULT_CANCELED, RESULT_IN_APP_UPDATE_FAILED -> finishAffinity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FetchRemoteConfig.fetchRemoteConfigValues {
        }

        makeFullScreenActivity(activityContext)
        checkForAnAppUpdate()
    }

    override fun onResume() {
        super.onResume()
        checkAppUpdateAfterMinimize()
    }

    private fun checkAppUpdateAfterMinimize() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }

    private fun checkForAnAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(activityContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(it, activityResultLauncher, AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            } else {
                startSplashAnimation()
            }
        }
    }

    private fun startSplashAnimation() {
        binding.apply {
            lifecycleScope.launch(Dispatchers.IO) {
                tasksViewModel.updateCompletedAndTimeUpTasks(true, Date(System.currentTimeMillis()))
            }
            splashIV.startAnimation(animation)
            applyAnimationOnProgressBar()
        }
    }

    private fun ActivitySplashBinding.applyAnimationOnProgressBar() {
        valueAnimator = ValueAnimator.ofInt(0, splashLoadingProgressBar.max)
        valueAnimator.apply {
            valueAnimator.duration = 5000
            addUpdateListener {
                val animatedValue = it.animatedValue
                if (animatedValue is Int) {
                    splashLoadingProgressBar.progress = animatedValue
                    loadingPercentageTV.text = String.format(Locale.getDefault(), "%d%s", animatedValue, "%")
                    if (animatedValue == 100) {
                        val goNextIntent = if (IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN)
                            Intent(activityContext, if (prefs.isUserSignIn) DashBoardActivity::class.java else SignInActivity::class.java)
                        else
                            Intent(activityContext, DashBoardActivity::class.java)
                        goNextIntent.apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(this)
                        }
                    }
                }
            }
        }
        valueAnimator.start()
    }

    private fun makeFullScreenActivity(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        else
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun handleActivitiesBackPressed() {
    }
}