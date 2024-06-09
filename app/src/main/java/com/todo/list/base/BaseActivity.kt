package com.todo.list.base

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.todo.list.R

open class BaseActivity : AppCompatActivity() {

    protected lateinit var activityContext: AppCompatActivity
    private lateinit var inputMethodManager: InputMethodManager

    protected var defaultColor = 0
    protected var darkYellowColor = 0
    protected var orangeColor = 0
    protected var lightGreenColor = 0
    protected var blueColor = 0
    protected var cyanColor = 0
    protected var pinkColor = 0
    protected var darkBlueColor = 0
    protected var redColor = 0
    protected var lightPurpleColor = 0

    protected var defaultTransparentColor = 0
    protected var darkYellowTransparentColor = 0
    protected var orangeTransparentColor = 0
    protected var lightGreenTransparentColor = 0
    protected var blueTransparentColor = 0
    protected var cyanTransparentColor = 0
    protected var pinkTransparentColor = 0
    protected var darkBlueTransparentColor = 0
    protected var redTransparentColor = 0
    protected var lightPurpleTransparentColor = 0

    protected var subTitlesTextColor = 0

    protected var snowWhiteColor = 0
    protected var switchTrackOffColor = 0
    protected var screensNightModeColor = 0
    protected var cardsNightModeColor = 0
    protected var whiteColor = 0
    protected var lightBlueColor = 0
    protected var darkModeTextColor = 0
    protected var blackColor = 0
    protected var tabLayoutUnSelectedTabTextColor = 0

    protected var dialogBoxesLightModeBackground = 0

    protected lateinit var editTextsCursorDarkModeColor: Drawable

    protected lateinit var whiteColorStateList: ColorStateList
    protected lateinit var textInputLayoutBoxStrokeDarkModeColor: ColorStateList

    private var feedbackEditTextCardViewLightModeColor = 0

    protected lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityContext = this

        handler = Handler(Looper.getMainLooper())

        defaultColor = ContextCompat.getColor(activityContext, R.color.defaultColor)
        darkYellowColor = ContextCompat.getColor(activityContext, R.color.darkYellowColor)
        orangeColor = ContextCompat.getColor(activityContext, R.color.orangeColor)
        lightGreenColor = ContextCompat.getColor(activityContext, R.color.lightGreenColor)
        blueColor = ContextCompat.getColor(activityContext, R.color.blueColor)
        cyanColor = ContextCompat.getColor(activityContext, R.color.cyanColor)
        pinkColor = ContextCompat.getColor(activityContext, R.color.pinkColor)
        darkBlueColor = ContextCompat.getColor(activityContext, R.color.darkBlueColor)
        redColor = ContextCompat.getColor(activityContext, R.color.redColor)
        lightPurpleColor = ContextCompat.getColor(activityContext, R.color.lightPurpleColor)

        defaultTransparentColor = ContextCompat.getColor(activityContext, R.color.defaultTransparentColor)
        darkYellowTransparentColor = ContextCompat.getColor(activityContext, R.color.darkYellowTransparentColor)
        orangeTransparentColor = ContextCompat.getColor(activityContext, R.color.orangeTransparentColor)
        lightGreenTransparentColor = ContextCompat.getColor(activityContext, R.color.lightGreenTransparentColor)
        blueTransparentColor = ContextCompat.getColor(activityContext, R.color.blueTransparentColor)
        cyanTransparentColor = ContextCompat.getColor(activityContext, R.color.cyanTransparentColor)
        pinkTransparentColor = ContextCompat.getColor(activityContext, R.color.pinkTransparentColor)
        darkBlueTransparentColor = ContextCompat.getColor(activityContext, R.color.darkBlueTransparentColor)
        redTransparentColor = ContextCompat.getColor(activityContext, R.color.redTransparentColor)
        lightPurpleTransparentColor = ContextCompat.getColor(activityContext, R.color.lightPurpleTransparentColor)

        subTitlesTextColor = ContextCompat.getColor(activityContext, R.color.subTitlesTextColor)

        snowWhiteColor = ContextCompat.getColor(activityContext, R.color.snowWhiteColor)
        switchTrackOffColor = ContextCompat.getColor(activityContext, R.color.switchTrackOffColor)
        screensNightModeColor = ContextCompat.getColor(activityContext, R.color.screensNightModeColor)
        cardsNightModeColor = ContextCompat.getColor(activityContext, R.color.cardsNightModeColor)
        whiteColor = ContextCompat.getColor(activityContext, R.color.whiteColor)
        blackColor = ContextCompat.getColor(activityContext, R.color.blackColor)
        lightBlueColor = ContextCompat.getColor(activityContext, R.color.lightBlueColor)
        darkModeTextColor = ContextCompat.getColor(activityContext, R.color.purple_500)
        feedbackEditTextCardViewLightModeColor = ContextCompat.getColor(activityContext, R.color.feedbackEditTextCardViewLightModeColor)
        tabLayoutUnSelectedTabTextColor = ContextCompat.getColor(activityContext, R.color.tabLayoutUnSelectedTabTextColor)

        editTextsCursorDarkModeColor = ContextCompat.getDrawable(activityContext, R.drawable.edittexts_cursor_dark_mode_color) as Drawable
        dialogBoxesLightModeBackground = R.drawable.dialog_boxes_light_mode_background

        whiteColorStateList = ColorStateList.valueOf(whiteColor)

        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        textInputLayoutBoxStrokeDarkModeColor = ColorStateList(
                arrayOf(
                        intArrayOf(android.R.attr.state_focused), intArrayOf(-android.R.attr.state_focused)),
                intArrayOf(
                        whiteColor      // Color when focused
                 , whiteColor // Color when not focused
                )
        )
    }

    protected fun showSoftKeyboard() = inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    protected fun hideSoftKeyboard(view: View) = inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

    protected fun isInternetConnectedORNot(connectivityManager: ConnectivityManager): Boolean {
        val isWiFiConnected: Boolean
        val isMobileDataConnected: Boolean
        var isConnectedORNot = false
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            isWiFiConnected = activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
            isMobileDataConnected = activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
            if (isWiFiConnected) isConnectedORNot = true else if (isMobileDataConnected) isConnectedORNot = true
        }
        return isConnectedORNot
    }
}
