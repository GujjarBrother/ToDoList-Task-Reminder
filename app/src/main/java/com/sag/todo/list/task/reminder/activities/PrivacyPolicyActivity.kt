package com.sag.todo.list.task.reminder.activities

import android.graphics.Color
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.databinding.ActivityPrivacyPolicyBinding
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeStatusBarColor
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeVisibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.keepActivityOn
import com.sag.todo.list.task.reminder.utils.CommonFunctions.makeFullScreenActivity

class PrivacyPolicyActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPrivacyPolicyBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        changeStatusBarColor(this, ContextCompat.getColor(this, R.color.defaultColor))
        keepActivityOn(this)
        makeFullScreenActivity(this)

        with(binding) {
            webView.loadUrl("https://sites.google.com/view/saginc-todolisttaskreminderapp/home")
            webView.setBackgroundColor(Color.TRANSPARENT)
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = WebViewClient()
            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    if (newProgress > 90) {
                        progressBar.changeVisibility(Visibility.GONE.ordinal)
                    }
                }
            }
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }
}