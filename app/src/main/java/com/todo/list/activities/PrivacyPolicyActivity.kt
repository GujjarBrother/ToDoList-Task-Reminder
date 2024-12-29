package com.todo.list.activities

import android.graphics.Color
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.todo.list.databinding.ActivityPrivacyPolicyBinding
import com.todo.list.enums.Visibility
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

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