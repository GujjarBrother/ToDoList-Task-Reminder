package com.sag.todo.list.task.reminder.activities

import android.graphics.Color
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivityPrivacyPolicyBinding
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.AppConstants.changeStatusBarColor
import com.sag.todo.list.task.reminder.utils.AppConstants.changeVisibility
import com.sag.todo.list.task.reminder.utils.AppConstants.getColorResource
import com.sag.todo.list.task.reminder.utils.AppConstants.keepActivityOn

class PrivacyPolicyActivity : BaseActivity() {

    private val binding by lazy {
        ActivityPrivacyPolicyBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        changeStatusBarColor(this, this.getColorResource(R.color.defaultColor))
        keepActivityOn(this)

        binding.apply {
            webView.loadUrl("https://sites.google.com/view/saginc-todolisttaskreminderapp/home")
            webView.setBackgroundColor(Color.TRANSPARENT)
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = WebViewClient()
            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    if (newProgress > 90) {
                        progressBar.changeVisibility(Visibility.GONE)
                    }
                }
            }
        }
    }

    override fun handleActivitiesBackPressed() = finish()
}