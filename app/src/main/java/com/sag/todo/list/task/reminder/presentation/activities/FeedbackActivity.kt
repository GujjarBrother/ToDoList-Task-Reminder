package com.sag.todo.list.task.reminder.presentation.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sag.todo.list.task.reminder.BuildConfig
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivityFeedbackBinding
import com.sag.todo.list.task.reminder.domain.enums.Visibility
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.changeVisibility
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.keepActivityOn

class FeedbackActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityFeedbackBinding.inflate(layoutInflater)
    }
    private val feedbackArrayList = ArrayList<String>()
    private var otherIssuesCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultColor = ContextCompat.getColor(activityContext, R.color.defaultColor)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(defaultColor),
            navigationBarStyle = SystemBarStyle.dark(defaultColor)
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomInset = maxOf(systemBars.bottom, ime.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset)
            insets
        }

        keepActivityOn(activityContext)

        with(binding) {
            backArrowIV.setOnClickListener(this@FeedbackActivity)
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
                    feedbackEditTextCV.changeVisibility(Visibility.VISIBLE.ordinal)
                    softKeyboardVisibilityController.showSoftKeyboard()
                    feedbackET.requestFocus()
                    otherIssuesCheck = true
                } else {
                    feedbackET.text = null
                    feedbackArrayList.remove(otherIssuesCB.text.toString().trim())
                    feedbackEditTextCV.changeVisibility(Visibility.GONE.ordinal)
                    softKeyboardVisibilityController.hideSoftKeyboard(feedbackET)
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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> goBackToSettingsActivity()
            R.id.cancelButton -> goBackToSettingsActivity()
            R.id.submitButton -> {
                with(binding) {
                    if (feedbackArrayList.isEmpty()) {
                        toastController.showToast(activityContext, getString(R.string.please_select_at_least_one_issue_to_submit_toast_text), false)
                    } else {
                        if (otherIssuesCheck) {
                            val otherIssue = feedbackET.text.toString().trim()
                            if (TextUtils.isEmpty(otherIssue)) {
                                feedbackET.error = getString(R.string.write_your_issues_here_text)
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
            data = mailTo?.toUri()
            val chooserIntent = Intent.createChooser(this, getString(R.string.share_via_text))
            if (resolveActivity(packageManager) != null) {
                startActivity(chooserIntent)
            } else {
                toastController.showToast(activityContext, getString(R.string.there_is_no_activity_available_to_handle_this_action_toast_text), false)
            }
        }
    }
}