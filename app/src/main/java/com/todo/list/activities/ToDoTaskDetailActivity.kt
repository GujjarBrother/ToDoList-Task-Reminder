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
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.keepActivityOn
import java.util.Locale

class ToDoTaskDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityToDoTaskDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDoTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyLightAndDarkMode()
        keepActivityOn(activityContext)
        applyCustomFont()

        val toDoTask = intent.getSerializableExtra("taskDetail") as ToDoTask?

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager))
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
                changeStatusBarColor(activityContext, screensNightModeColor)
                include.root.visibility = View.VISIBLE
                rootLayout.setBackgroundColor(screensNightModeColor)
                toolbar.setBackgroundColor(screensNightModeColor)
                backArrowIV.setColorFilter(whiteColor)
                toolbarTV.setTextColor(whiteColor)
                titleTV.setTextColor(whiteColor)
                descriptionTV.setTextColor(whiteColor)
                dateAndDayTV.setTextColor(whiteColor)
                timeTV.setTextColor(whiteColor)
                adLoadingInclude.adIsLoadingTextView.setTextColor(whiteColor)
                adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(whiteColor)
            } else {
                include.root.visibility = View.GONE
                when (prefs.colorSchemeValue) {
                    0 -> {
                        changeStatusBarColor(activityContext, defaultColor)
                        toolbar.setBackgroundColor(defaultColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(defaultColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(defaultColor)
                    }

                    1 -> {
                        changeStatusBarColor(activityContext, darkYellowColor)
                        toolbar.setBackgroundColor(darkYellowColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkYellowColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkYellowColor)
                    }

                    2 -> {
                        changeStatusBarColor(activityContext, orangeColor)
                        toolbar.setBackgroundColor(orangeColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(orangeColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(orangeColor)
                    }

                    3 -> {
                        changeStatusBarColor(activityContext, lightGreenColor)
                        toolbar.setBackgroundColor(lightGreenColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightGreenColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightGreenColor)
                    }

                    4 -> {
                        changeStatusBarColor(activityContext, blueColor)
                        toolbar.setBackgroundColor(blueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(blueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(blueColor)
                    }

                    5 -> {
                        changeStatusBarColor(activityContext, cyanColor)
                        toolbar.setBackgroundColor(cyanColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(cyanColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(cyanColor)
                    }

                    6 -> {
                        changeStatusBarColor(activityContext, pinkColor)
                        toolbar.setBackgroundColor(pinkColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(pinkColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(pinkColor)
                    }

                    7 -> {
                        changeStatusBarColor(activityContext, darkBlueColor)
                        toolbar.setBackgroundColor(darkBlueColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkBlueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkBlueColor)
                    }

                    8 -> {
                        changeStatusBarColor(activityContext, redColor)
                        toolbar.setBackgroundColor(redColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(redColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(redColor)
                    }

                    9 -> {
                        changeStatusBarColor(activityContext, lightPurpleColor)
                        toolbar.setBackgroundColor(lightPurpleColor)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightPurpleColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightPurpleColor)
                    }
                }
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