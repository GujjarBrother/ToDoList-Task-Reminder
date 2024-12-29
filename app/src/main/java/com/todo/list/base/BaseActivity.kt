package com.todo.list.base

import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.todo.list.R
import com.todo.list.utils.ColorsUtils.getContextCompatColor
import com.todo.list.utils.CommonFunctions.changeStatusBarColor

open class BaseActivity : AppCompatActivity() {

    protected lateinit var activityContext: AppCompatActivity
    private lateinit var inputMethodManager: InputMethodManager
    protected lateinit var handler: Handler
    protected lateinit var textInputLayoutBoxStrokeDarkModeColor: ColorStateList

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)

        activityContext = this

        changeStatusBarColor(activityContext, getContextCompatColor(activityContext, R.color.defaultColor))
        handler = Handler(Looper.getMainLooper())
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        /*textInputLayoutBoxStrokeDarkModeColor = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf(-android.R.attr.state_focused)),
                intArrayOf(
                    // Color when focused
                    ContextCompat.getColor(activityContext, R.color.purple_500)
                    // Color when not focused
                 ,ContextCompat.getColor(activityContext, R.color.purple_500)
                )
        )*/
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
