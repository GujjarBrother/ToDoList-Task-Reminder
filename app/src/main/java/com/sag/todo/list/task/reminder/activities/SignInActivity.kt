package com.sag.todo.list.task.reminder.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.addTextChangedListener
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivitySignInBinding
import com.sag.todo.list.task.reminder.databinding.RecoverPasswordDialogLayoutBinding
import com.sag.todo.list.task.reminder.enums.SecurityQuestions
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.AppConstants.changeVisibility
import com.sag.todo.list.task.reminder.utils.AppConstants.keepActivityOn
import com.sag.todo.list.task.reminder.utils.SignInAndSignUpCardViewsAnimation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignInActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }

    @Inject
    @SignInAndSignUpCardViewsAnimation
    lateinit var animation: Animation
    private var emailOrUserName: String? = null
    private var password: String? = null
    private var selectedSecurityQuestion = 0
    private var securityAnswer: String? = null
    private var isForgotPasswordDialogShown = false
    private var resetPasswordAnswer: String? = null
    private var isScreenRotate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        keepActivityOn(activityContext)

        binding.apply {
            if (prefs.isDarkModeEnable) {
                userNameTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
                passwordTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
            }

            signInButton.setOnClickListener(this@SignInActivity)
            forgotPasswordTV.setOnClickListener(this@SignInActivity)
            signUpTV.setOnClickListener(this@SignInActivity)
        }
    }

    override fun onRestart() {
        super.onRestart()
        isForgotPasswordDialogShown = false
    }

    override fun onResume() {
        super.onResume()
        if (!isScreenRotate) fetchUser()
        if (selectedSecurityQuestion != 0 && isForgotPasswordDialogShown) showRecoverPasswordDialog()
        binding.apply {
            if (!isScreenRotate) signInCV?.startAnimation(animation)
            forgotPasswordTV.changeVisibility(if (selectedSecurityQuestion != 0) Visibility.VISIBLE else Visibility.GONE)
            dontHaveAnAccountSignUpAccountGroup.changeVisibility(if (selectedSecurityQuestion != 0) Visibility.GONE else Visibility.VISIBLE)
        }
        isScreenRotate = false
    }

    override fun onClick(view: View?) {
        binding.apply {
            when (view?.id) {
                R.id.signInButton -> {
                    val signInEmailOrUserName = userNameTIL.editText?.text.toString().trim()
                    val signInPassword = passwordTIL.editText?.text.toString().trim()
                    if (signInEmailOrUserName.isEmpty()) {
                        passwordTIL.error = null
                        userNameTIL.error = getString(com.example.core.R.string.fill_this_field_text)
                    } else if (signInPassword.isEmpty()) {
                        userNameTIL.error = null
                        passwordTIL.error = getString(com.example.core.R.string.fill_this_field_text)
                    } else if (signInEmailOrUserName.equals(emailOrUserName, true)) {
                        if (signInPassword.equals(password, true)) {
                            userNameTIL.error = null
                            passwordTIL.error = null
                            prefs.isUserSignIn = true
                            prefs.rememberMe = rememberMeCB.isChecked
                            toastController.showToast(activityContext, getString(com.example.core.R.string.sign_in_successfully_toast_message_text), true)
                            startActivity(Intent(activityContext, DashBoardActivity::class.java))
                            finish()
                        } else {
                            userNameTIL.error = null
                            passwordTIL.error = null
                            passwordTIL.error = getString(com.example.core.R.string.password_is_wrong_text)
                        }
                    } else {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        userNameTIL.error = getString(com.example.core.R.string.username_email_is_wrong_text)
                    }
                }
                R.id.forgotPasswordTV -> showRecoverPasswordDialog()
                R.id.signUpTV -> openSignUpActivity()
            }
        }
    }

    private fun openSignUpActivity() =
        startActivity(Intent(activityContext, SignUpActivity::class.java))

    private fun showRecoverPasswordDialog() {
        val recoverPasswordDialogLayoutBinding = RecoverPasswordDialogLayoutBinding.inflate(layoutInflater)

        val recoverPasswordDialogBuilder = AlertDialog.Builder(activityContext)
        recoverPasswordDialogBuilder.apply {
            setView(recoverPasswordDialogLayoutBinding.root)
            setCancelable(true)
        }
        val recoverPasswordAlertDialog = recoverPasswordDialogBuilder.create()
        recoverPasswordAlertDialog.apply {
            setOnShowListener {
                isForgotPasswordDialogShown = true
            }
            setOnDismissListener {
                isForgotPasswordDialogShown = false
            }
        }
        if (!activityContext.isFinishing && !activityContext.isDestroyed && !recoverPasswordAlertDialog.isShowing) {
            recoverPasswordAlertDialog.apply {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                window?.setWindowAnimations(R.style.dialogBoxesAnimation)
                show()
            }
        }

        recoverPasswordDialogLayoutBinding.apply {
            securityQuestionAnswerTIL.hint = when (selectedSecurityQuestion) {
                SecurityQuestions.QUESTION_1.ordinal -> getString(com.example.core.R.string.what_is_your_favourite_book_question)
                SecurityQuestions.QUESTION_2.ordinal -> getString(com.example.core.R.string.what_is_your_favourite_teacher_name_question)
                SecurityQuestions.QUESTION_3.ordinal -> getString(com.example.core.R.string.what_is_your_school_name_question)
                SecurityQuestions.QUESTION_4.ordinal -> getString(com.example.core.R.string.what_is_your_favourite_game_question)
                else -> ""
            }
            securityQuestionAnswerTIL.editText?.setText(resetPasswordAnswer ?: "")
            softKeyboardVisibilityController.showSoftKeyboard()
            securityQuestionAnswerTIET.requestFocus()

            securityQuestionAnswerTIET.addTextChangedListener {
                resetPasswordAnswer = it.toString().trim()
            }

            if (prefs.isDarkModeEnable) {
                securityQuestionAnswerTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
            }

            dismissDialogIV.setOnClickListener { _: View? ->
                softKeyboardVisibilityController.hideSoftKeyboard(securityQuestionAnswerTIET)
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    recoverPasswordAlertDialog.dismiss()
                }
            }

            resetPasswordButton.setOnClickListener { _: View? ->
                val answer = securityQuestionAnswerTIL.editText?.text.toString().trim()
                if (answer.isEmpty()) {
                    securityQuestionAnswerTIL.error = getString(com.example.core.R.string.please_enter_answer_here_error_text)
                } else if (answer == securityAnswer) {
                    softKeyboardVisibilityController.hideSoftKeyboard(securityQuestionAnswerTIET)
                    securityQuestionAnswerTIL.setErrorTextColor(null)
                    securityQuestionAnswerTIL.error = null
                    if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                        recoverPasswordAlertDialog.dismiss()
                    }
                    openSignUpActivity()
                } else {
                    securityQuestionAnswerTIL.error = getString(com.example.core.R.string.enter_right_answer_error_text)
                }
            }
        }
    }

    private fun fetchUser() {
        val userCredentialArray = prefs.userCredentials
        emailOrUserName = userCredentialArray[0]
        password = userCredentialArray[1]
        selectedSecurityQuestion = userCredentialArray[3].toInt()
        securityAnswer = userCredentialArray[4]
        binding.apply {
            if (prefs.rememberMe) {
                userNameTIL.editText?.setText(emailOrUserName)
                passwordTIL.editText?.setText(password)
                userNameTIL.editText?.setSelection(emailOrUserName?.length ?: 0)
                passwordTIL.editText?.setSelection(password?.length ?: 0)
                rememberMeCB.isChecked = true
            } else rememberMeCB.isChecked = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.apply {
            outState.also {
                it.putString("userNameOrEmail", userNameTIL.editText?.text.toString().trim())
                it.putString("password", passwordTIL.editText?.text.toString().trim())
                it.putBoolean("rememberMe", rememberMeCB.isChecked)
                it.putBoolean("isForgotPasswordDialogShown", isForgotPasswordDialogShown)
                it.putBoolean("isScreenRotate", !isScreenRotate)
                it.putInt("isAnySecurityQuestionSelected", selectedSecurityQuestion)
                it.putString("resetPasswordAnswer", resetPasswordAnswer)
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.apply {
            savedInstanceState.also {
                userNameTIL.editText?.setText(it.getString("userNameOrEmail"))
                passwordTIL.editText?.setText(it.getString("password"))
                rememberMeCB.isChecked = it.getBoolean("rememberMe")
                isForgotPasswordDialogShown = it.getBoolean("isForgotPasswordDialogShown")
                isScreenRotate = it.getBoolean("isScreenRotate")
                resetPasswordAnswer = it.getString("resetPasswordAnswer")
                selectedSecurityQuestion = it.getInt("isAnySecurityQuestionSelected")
            }
        }
    }

    override fun handleActivitiesBackPressed() = finish()
}