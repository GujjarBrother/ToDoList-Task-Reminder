package com.todo.list.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import com.todo.list.BuildConfig
import com.todo.list.R
import com.todo.list.adsPlugin.bannerAd.BannerAdController
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivityFeedbackBinding
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.keepActivityOn
import es.dmoral.toasty.Toasty

class FeedbackActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFeedbackBinding
    private val feedbackArrayList = ArrayList<String>()
    private var otherIssuesCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyColorSchemeORLightAndDarkMode()
        keepActivityOn(activityContext)
        applyCustomFont()

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isLargeBanner = true,
                isInternetConnected = isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager))
            )
            backArrowImageView.setOnClickListener(this@FeedbackActivity)
            submitButton.setOnClickListener(this@FeedbackActivity)
            cancelButton.setOnClickListener(this@FeedbackActivity)
            signUpIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    feedbackArrayList.add(signUpIssuesCB.text.toString().trim())
                } else {
                    feedbackArrayList.remove(signUpIssuesCB.text.toString().trim())
                }
            }

            signInIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    feedbackArrayList.add(signInIssuesCB.text.toString().trim())
                } else {
                    feedbackArrayList.remove(signInIssuesCB.text.toString().trim())
                }
            }

            tasksSaveIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    feedbackArrayList.add(tasksSaveIssuesCB.text.toString().trim())
                } else {
                    feedbackArrayList.remove(tasksSaveIssuesCB.text.toString().trim())
                }
            }

            tasksUpdateIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    feedbackArrayList.add(tasksUpdateIssuesCB.text.toString().trim())
                } else {
                    feedbackArrayList.remove(tasksUpdateIssuesCB.text.toString().trim())
                }
            }

            tasksDeleteIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    feedbackArrayList.add(tasksDeleteIssuesCB.text.toString().trim())
                } else {
                    feedbackArrayList.remove(tasksDeleteIssuesCB.text.toString().trim())
                }
            }

            signOutIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    feedbackArrayList.add(signOutIssuesCB.text.toString().trim())
                } else {
                    feedbackArrayList.remove(signOutIssuesCB.text.toString().trim())
                }
            }

            otherIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    feedbackArrayList.add(otherIssuesCB.text.toString().trim())
                    feedbackEditTextCardView.visibility = View.VISIBLE
                    showSoftKeyboard()
                    feedbackEditText.requestFocus()
                    otherIssuesCheck = true
                } else {
                    feedbackEditText.text = null
                    feedbackArrayList.remove(otherIssuesCB.text.toString().trim())
                    feedbackEditTextCardView.visibility = View.GONE
                    hideSoftKeyboard(feedbackEditText)
                    otherIssuesCheck = false
                }
            }
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackToSettingsActivity()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun goBackToSettingsActivity() = finish()

    private fun applyCustomFont() {
        with(binding) {
            toolbarTextView.typeface = typeface
            selectFeedbackTextView.typeface = typeface
            signUpIssuesCB.typeface = typeface
            signInIssuesCB.typeface = typeface
            tasksSaveIssuesCB.typeface = typeface
            tasksUpdateIssuesCB.typeface = typeface
            tasksDeleteIssuesCB.typeface = typeface
            signOutIssuesCB.typeface = typeface
            otherIssuesCB.typeface = typeface
            feedbackEditText.typeface = typeface
            submitButton.typeface = typeface
            cancelButton.typeface = typeface
            adLoadingInclude.adIsLoadingTextView.typeface = typeface
        }
    }

    private fun applyColorSchemeORLightAndDarkMode() {
        with(binding) {
            if (prefs.dayAndNightModeSwitchValue) {
                changeStatusBarColor(activityContext, screensNightModeColor)
                toolbar.setBackgroundColor(screensNightModeColor)
                include.root.visibility = View.VISIBLE
                feedbackActivityRootLayout.setBackgroundColor(screensNightModeColor)
                selectFeedbackTextView.setTextColor(whiteColor)
                signUpIssuesCB.buttonTintList = ColorStateList.valueOf(whiteColor)
                signUpIssuesCB.setTextColor(whiteColor)
                signInIssuesCB.buttonTintList = ColorStateList.valueOf(whiteColor)
                signInIssuesCB.setTextColor(whiteColor)
                tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(whiteColor)
                tasksSaveIssuesCB.setTextColor(whiteColor)
                tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(whiteColor)
                tasksUpdateIssuesCB.setTextColor(whiteColor)
                tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(whiteColor)
                tasksDeleteIssuesCB.setTextColor(whiteColor)
                signOutIssuesCB.buttonTintList = ColorStateList.valueOf(whiteColor)
                signOutIssuesCB.setTextColor(whiteColor)
                otherIssuesCB.buttonTintList = ColorStateList.valueOf(whiteColor)
                otherIssuesCB.setTextColor(whiteColor)
                feedbackEditTextCardView.setCardBackgroundColor(cardsNightModeColor)
                feedbackEditText.setTextColor(whiteColor)
                cancelButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                cancelButton.setTextColor(whiteColor)
                submitButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                submitButton.setTextColor(whiteColor)
                adLoadingInclude.adIsLoadingTextView.setTextColor(whiteColor)
                adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        changeStatusBarColor(activityContext, defaultColor)
                        toolbar.setBackgroundColor(defaultColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(defaultColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(defaultColor)
                    }

                    1 -> {
                        changeStatusBarColor(activityContext, darkYellowColor)
                        toolbar.setBackgroundColor(darkYellowColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkYellowColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkYellowColor)
                    }

                    2 -> {
                        changeStatusBarColor(activityContext, orangeColor)
                        toolbar.setBackgroundColor(orangeColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(orangeColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(orangeColor)
                    }

                    3 -> {
                        changeStatusBarColor(activityContext, lightGreenColor)
                        toolbar.setBackgroundColor(lightGreenColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightGreenColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightGreenColor)
                    }

                    4 -> {
                        changeStatusBarColor(activityContext, blueColor)
                        toolbar.setBackgroundColor(blueColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(blueColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(blueColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(blueColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(blueColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(blueColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(blueColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(blueColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(blueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(blueColor)
                    }

                    5 -> {
                        changeStatusBarColor(activityContext, cyanColor)
                        toolbar.setBackgroundColor(cyanColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(cyanColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(cyanColor)
                    }

                    6 -> {
                        changeStatusBarColor(activityContext, pinkColor)
                        toolbar.setBackgroundColor(pinkColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(pinkColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(pinkColor)
                    }

                    7 -> {
                        changeStatusBarColor(activityContext, darkBlueColor)
                        toolbar.setBackgroundColor(darkBlueColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(darkBlueColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(darkBlueColor)
                    }

                    8 -> {
                        changeStatusBarColor(activityContext, redColor)
                        toolbar.setBackgroundColor(redColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(redColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(redColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(redColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(redColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(redColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(redColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(redColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(redColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(redColor)
                    }

                    9 -> {
                        changeStatusBarColor(activityContext, lightPurpleColor)
                        toolbar.setBackgroundColor(lightPurpleColor)
                        signUpIssuesCB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        signInIssuesCB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        tasksSaveIssuesCB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        tasksUpdateIssuesCB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        tasksDeleteIssuesCB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        signOutIssuesCB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        otherIssuesCB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        submitButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        adLoadingInclude.adIsLoadingTextView.setTextColor(lightPurpleColor)
                        adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(lightPurpleColor)
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back_arrow_image_view -> {
                goBackToSettingsActivity()
            }

            R.id.cancel_button -> {
                goBackToSettingsActivity()
            }

            R.id.submit_button -> {
                if (feedbackArrayList.isEmpty()) {
                    Toasty.error(activityContext, R.string.please_select_at_least_one_issue_to_submit_toast_text, Toasty.LENGTH_LONG)
                            .show()
                } else {
                    if (otherIssuesCheck) {
                        val otherIssue = binding.feedbackEditText.text.toString().trim()
                        if (TextUtils.isEmpty(otherIssue)) {
                            binding.feedbackEditText.error = getString(R.string.write_your_issues_here_text)
                        } else {
                            sendFeedback(feedbackArrayList, otherIssue)
                        }
                    } else {
                        sendFeedback(feedbackArrayList)
                    }
                }
            }
        }
    }

    private fun sendFeedback(feedbackArrayList: ArrayList<String>, otherIssue: String) {
        val feedbackBody = StringBuilder("Issues :- \n  $otherIssue\n")
        var mailTo: String? = null
        for (i in feedbackArrayList.indices) {
            val issue = feedbackArrayList[i]
            if (!issue.equals(binding.otherIssuesCB.text.toString().trim(), ignoreCase = true)) {
                feedbackBody.append("  ").append(issue).append("\n")
            }
            val receiverEmail = "sa10181922@gmail.com"
            val feedbackSubject = """Feedback-${getString(R.string.app_name)}
                App Version : ${BuildConfig.VERSION_CODE}
                OS Version : ${Build.VERSION.SDK_INT}
                Device Model-${Build.MANUFACTURER} ${Build.MODEL}"""

            mailTo = "mailto:" + receiverEmail + "?&subject=" + Uri.encode(feedbackSubject) +
                    "&body=" + Uri.encode(feedbackBody.toString())
        }
        send(mailTo)
    }

    private fun sendFeedback(feedbackArrayList: ArrayList<String>) {
        val feedbackBody = StringBuilder("Issues :- \n")
        var mailTo: String? = null
        for (i in feedbackArrayList.indices) {
            val issue = feedbackArrayList[i]
            feedbackBody.append("  ").append(issue).append("\n")
            val receiverEmail = "sa10181922@gmail.com"
            val feedbackSubject = """Feedback-${getString(R.string.app_name)}
                App Version : ${BuildConfig.VERSION_CODE}
                OS Version : ${Build.VERSION.SDK_INT}
                Device Model-${Build.MANUFACTURER} ${Build.MODEL}"""
            mailTo = "mailto:" + receiverEmail + "?&subject=" + Uri.encode(feedbackSubject) +
                    "&body=" + Uri.encode(feedbackBody.toString())
        }
        send(mailTo)
    }

    private fun send(mailTo: String?) {
        val sendFeedbackIntent = Intent()
        with(sendFeedbackIntent) {
            action = Intent.ACTION_SENDTO
            data = Uri.parse(mailTo)
            val chooserIntent = Intent.createChooser(this, getString(R.string.share_via_text))
            if (resolveActivity(packageManager) != null) {
                startActivity(chooserIntent)
            } else {
                Toasty.error(activityContext, R.string.there_is_no_activity_available_to_handle_this_action_toast_text, Toasty.LENGTH_LONG).show()
            }
        }
    }
}