package com.todo.list.activities

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.todo.list.R
import com.todo.list.adsPlugin.bannerAd.BannerAdController
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivityToDoTaskDetailBinding
import com.todo.list.db.ToDoTask
import com.todo.list.enums.Visibility
import com.todo.list.models.SelectedColors
import com.todo.list.utils.ColorsUtils.getContextCompatColor
import com.todo.list.utils.ColorsUtils.getSelectedColor
import com.todo.list.utils.ColorsUtils.screensNightModeColor
import com.todo.list.utils.ColorsUtils.whiteColor
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.keepActivityOn
import java.util.Locale

class ToDoTaskDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityToDoTaskDetailBinding
    private lateinit var selectedColors: SelectedColors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDoTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedColors = getSelectedColor(context = activityContext, prefs = prefs)

        applyLightAndDarkMode()
        keepActivityOn(activityContext)
        applyCustomFont()

        val toDoTask = intent.getSerializableExtra("taskDetail") as ToDoTask?

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)),
                adID = getString(R.string.detailScreenBannerAdId)
            )
            if (toDoTask != null) {
                toolbarTV.text = toDoTask.title
                titleTV.text = toDoTask.title
                descriptionTV.text = toDoTask.description
                dateAndDayTV.text = String.format(Locale.getDefault(), "%s, %s %s, %s",
                    toDoTask.day, toDoTask.month, toDoTask.date, toDoTask.year)
                timeTV.text = toDoTask.time
            }
            titleTV.textSize = prefs.textSizeValue.toFloat()
            descriptionTV.textSize = prefs.textSizeValue.toFloat()
            dateAndDayTV.textSize = prefs.textSizeValue.toFloat()
            timeTV.textSize = prefs.textSizeValue.toFloat()
            backArrowIV.setOnClickListener(this@ToDoTaskDetailActivity)
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackToDashBoardActivity()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun applyCustomFont() {
        with(binding) {
            toolbarTV.typeface = typeface
            titleTV.typeface = typeface
            descriptionTV.typeface = typeface
            dateAndDayTV.typeface = typeface
            timeTV.typeface = typeface
            adLoadingInclude.adIsLoadingTextView.typeface = typeface
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> {
                goBackToDashBoardActivity()
            }
        }
    }

    private fun applyLightAndDarkMode() {
        with(binding) {
            if (prefs.isDarkModeEnable) {
                changeStatusBarColor(activityContext, getContextCompatColor(activityContext, screensNightModeColor))
                include.root.changeVisibility(Visibility.VISIBLE.ordinal)
                rootLayout.setBackgroundColor(getContextCompatColor(activityContext, screensNightModeColor))
                toolbar.setBackgroundColor(getContextCompatColor(activityContext, screensNightModeColor))
                backArrowIV.setColorFilter(getContextCompatColor(activityContext, whiteColor))
                toolbarTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                titleTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                descriptionTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                dateAndDayTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                timeTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                adLoadingInclude.adIsLoadingTextView.setTextColor(getContextCompatColor(activityContext, whiteColor))
                adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
            } else {
                include.root.changeVisibility(Visibility.GONE.ordinal)
                changeStatusBarColor(activityContext, selectedColors.originalColor)
                toolbar.setBackgroundColor(selectedColors.originalColor)
                adLoadingInclude.adIsLoadingTextView.setTextColor(selectedColors.originalColor)
                adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(selectedColors.originalColor)
            }
        }
    }

    private fun goBackToDashBoardActivity() = finish()

    //    Override 'onConfigurationChanged' Method, Which Is Used To Prevent An Activity To 'Re-create' When
    //    Changing The Screen Orientation.i.e., Switching Between 'PORTRAIT MODE' TO 'LANDSCAPE MODE' & Vice Versa.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}