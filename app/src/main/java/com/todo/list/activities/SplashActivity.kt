package com.todo.list.activities

import android.annotation.SuppressLint
import android.os.Bundle
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivitySplashBinding
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var selectedColor= 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedColor = when(prefs.colorSchemeValue) {
            0 -> defaultColor
            1 -> darkYellowColor
            2 -> orangeColor
            3 -> lightGreenColor
            4 -> blueColor
            5 -> cyanColor
            6 -> pinkColor
            7 -> darkBlueColor
            8 -> redColor
            else -> lightPurpleColor
        }

        makeFullScreenActivity(activityContext)
        applyCustomFont()
        applyLightAndDarkMode()
    }

    private fun applyLightAndDarkMode() {
        with(binding) {
            if (prefs.isDarkModeEnable) {
                changeStatusBarColor(activityContext, screensNightModeColor)
                rootLayout.setBackgroundColor(screensNightModeColor)
                taskTV.setTextColor(lightBlueColor)
                reminderTV.setTextColor(lightBlueColor)
                loadingTV.setTextColor(lightBlueColor)
                loadingPercentageTV.setTextColor(lightBlueColor)
            } else {
                changeStatusBarColor(activityContext, defaultColor)
                taskTV.setTextColor(selectedColor)
                loadingTV.setTextColor(selectedColor)
                loadingPercentageTV.setTextColor(selectedColor)
            }
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            taskTV.typeface = typeface
            reminderTV.typeface = typeface
            loadingTV.typeface = typeface
            loadingPercentageTV.typeface = typeface
        }
    }
}