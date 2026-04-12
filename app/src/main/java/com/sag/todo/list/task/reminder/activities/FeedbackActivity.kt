package com.sag.todo.list.task.reminder.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.core.net.toUri
import com.sag.todo.list.task.reminder.BuildConfig
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivityFeedbackBinding
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.AppConstants.changeVisibility
import com.sag.todo.list.task.reminder.utils.AppConstants.keepActivityOn

class FeedbackActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityFeedbackBinding.inflate(layoutInflater)
    }
    private val feedbackArrayList = emptyList<String>().toMutableList()
    private var otherIssuesCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        keepActivityOn(activityContext)

        binding.apply {
            backArrowIV.setOnClickListener(this@FeedbackActivity)
            submitButton.setOnClickListener(this@FeedbackActivity)
            cancelButton.setOnClickListener(this@FeedbackActivity)
            signUpIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked)
                    feedbackArrayList.add(signUpIssuesCB.text.toString().trim())
                else
                    feedbackArrayList.remove(signUpIssuesCB.text.toString().trim())
            }

            signInIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked)
                    feedbackArrayList.add(signInIssuesCB.text.toString().trim())
                else
                    feedbackArrayList.remove(signInIssuesCB.text.toString().trim())
            }

            tasksSaveIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked)
                    feedbackArrayList.add(tasksSaveIssuesCB.text.toString().trim())
                else
                    feedbackArrayList.remove(tasksSaveIssuesCB.text.toString().trim())
            }

            tasksUpdateIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked)
                    feedbackArrayList.add(tasksUpdateIssuesCB.text.toString().trim())
                else
                    feedbackArrayList.remove(tasksUpdateIssuesCB.text.toString().trim())
            }

            tasksDeleteIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked)
                    feedbackArrayList.add(tasksDeleteIssuesCB.text.toString().trim())
                else
                    feedbackArrayList.remove(tasksDeleteIssuesCB.text.toString().trim())
            }

            signOutIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) feedbackArrayList.add(signOutIssuesCB.text.toString().trim())
                else
                    feedbackArrayList.remove(signOutIssuesCB.text.toString().trim())
            }

            otherIssuesCB.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    feedbackArrayList.add(otherIssuesCB.text.toString().trim())
                    feedbackEditTextCV.changeVisibility(Visibility.VISIBLE)
                    softKeyboardVisibilityController.showSoftKeyboard()
                    feedbackET.requestFocus()
                    otherIssuesCheck = true
                } else {
                    feedbackET.text = null
                    feedbackArrayList.remove(otherIssuesCB.text.toString().trim())
                    feedbackEditTextCV.changeVisibility(Visibility.GONE)
                    softKeyboardVisibilityController.hideSoftKeyboard(feedbackET)
                    otherIssuesCheck = false
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> callBackPressed()
            R.id.cancelButton -> callBackPressed()
            R.id.submitButton -> {
                binding.apply {
                    if (feedbackArrayList.isEmpty()) toastController.showToast(
                        activityContext,
                        getString(com.example.core.R.string.please_select_at_least_one_issue_to_submit_toast_text),
                        false
                    )
                    else {
                        if (otherIssuesCheck) {
                            val otherIssue = feedbackET.text.toString().trim()
                            if (otherIssue.isNotEmpty())
                                sendFeedback(feedbackArrayList, otherIssue)
                            else
                                feedbackET.error = getString(com.example.core.R.string.write_your_issues_here_text)
                        } else sendFeedback(feedbackArrayList)
                    }
                }
            }
        }
    }

    private fun sendFeedback(feedbackArrayList: MutableList<String>, otherIssue: String) {
        val feedbackBody = StringBuilder("Issues :- \n  $otherIssue\n")
        var mailTo: String? = null
        for (i in feedbackArrayList.indices) {
            val issue = feedbackArrayList[i]
            if (!issue.equals(binding.otherIssuesCB.text.toString().trim(), ignoreCase = true)) {
                feedbackBody.append("  ").append(issue).append("\n")
            }
            val receiverEmail = "sa10181922@gmail.com"
            val feedbackSubject = """Feedback-${getString(com.example.core.R.string.app_name)}
                App Version : ${BuildConfig.VERSION_CODE}
                OS Version : ${Build.VERSION.SDK_INT}
                Device Model-${Build.MANUFACTURER} ${Build.MODEL}"""

            mailTo = "mailto:" + receiverEmail + "?&subject=" + Uri.encode(feedbackSubject) +
                    "&body=" + Uri.encode(feedbackBody.toString())
        }
        send(mailTo)
    }

    private fun sendFeedback(feedbackArrayList: MutableList<String>) {
        val feedbackBody = StringBuilder("Issues :- \n")
        var mailTo: String? = null
        for (i in feedbackArrayList.indices) {
            val issue = feedbackArrayList[i]
            feedbackBody.append("  ").append(issue).append("\n")
            val receiverEmail = "sa10181922@gmail.com"
            val feedbackSubject = """Feedback-${getString(com.example.core.R.string.app_name)}
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
        sendFeedbackIntent.apply {
            action = Intent.ACTION_SENDTO
            data = mailTo?.toUri()
            val chooserIntent = Intent.createChooser(this, getString(com.example.core.R.string.share_via_text))
            if (resolveActivity(packageManager) != null)
                startActivity(chooserIntent)
            else
                toastController.showToast(activityContext, getString(com.example.core.R.string.there_is_no_activity_available_to_handle_this_action_toast_text), false)
        }
    }

    override fun handleActivitiesBackPressed() = finish()
}