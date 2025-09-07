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
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.sag.todo.list.task.reminder.R
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
        /*if (it.resultCode != RESULT_OK) {
            toastController.showToast(activityContext, "Update not available...!", false)
        } else {
            toastController.showToast(activityContext, "Update available...!", true)
        }*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultColor = ContextCompat.getColor(activityContext, R.color.defaultColor)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(defaultColor),
            navigationBarStyle = SystemBarStyle.dark(defaultColor)
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FetchRemoteConfig.fetchRemoteConfigValues {
        }

        checkForAnAppUpdate()
        makeFullScreenActivity(activityContext)

        lifecycleScope.launch(Dispatchers.IO) {
            tasksViewModel.updateCompletedAndTimeUpTasks(true, Date(System.currentTimeMillis()))
        }

        with(binding) {
            splashIV.startAnimation(animation)
            applyAnimationOnProgressBar()
        }
    }

    private fun makeFullScreenActivity(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
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
            duration = 5000
            addUpdateListener {
                val animatedValue = it.animatedValue
                if (animatedValue is Int) {
                    splashLoadingProgressBar.progress = animatedValue
                    loadingPercentageTV.text = String.format(Locale.getDefault(), "%d%s", animatedValue, "%")
                    if (animatedValue == 100) {
                        if (IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN) {
                            startActivity(Intent(activityContext, if (prefs.isUserSignIn) DashBoardActivity::class.java else SignInActivity::class.java))
                        } else {
                            startActivity(Intent(activityContext, DashBoardActivity::class.java))
                        }
                        finish()
                    }
                }
            }
        }
        valueAnimator.start()
    }
}