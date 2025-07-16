package com.sag.todo.list.task.reminder.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.widget.CompoundButton
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivitySignInBinding
import com.sag.todo.list.task.reminder.databinding.RecoverPasswordDialogLayoutBinding
import com.sag.todo.list.task.reminder.enums.SecurityQuestions
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeVisibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.keepActivityOn
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class SignInActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }

    @Inject
    @Named(value = "SignInAndSignUpCardViewsAnimation")
    lateinit var animation: Animation
    private lateinit var emailOrUserName: String
    private lateinit var password: String
    private var selectedSecurityQuestion = 0
    private lateinit var securityAnswer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultColor = ContextCompat.getColor(activityContext, R.color.defaultColor)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(defaultColor),
            navigationBarStyle = SystemBarStyle.dark(defaultColor)
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchUser()
        keepActivityOn(activityContext)

        with(binding) {
            signInCV.startAnimation(animation)

            if (prefs.isDarkModeEnable) {
                userNameTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
                passwordTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
            }
            signInButton.setOnClickListener(this@SignInActivity)
            forgotPasswordTV.setOnClickListener(this@SignInActivity)
            signUpTV.setOnClickListener(this@SignInActivity)
            rememberMeCB.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                prefs.rememberMe = isChecked
            }
        }
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.signInButton -> {
                    val signInEmailOrUserName = userNameTIL.editText?.text.toString().trim()
                    val signInPassword = passwordTIL.editText?.text.toString().trim()
                    if (signInEmailOrUserName.isEmpty()) {
                        passwordTIL.error = null
                        userNameTIL.error = getString(R.string.fill_this_field_text)
                    } else if (signInPassword.isEmpty()) {
                        userNameTIL.error = null
                        passwordTIL.error = getString(R.string.fill_this_field_text)
                    } else if (signInEmailOrUserName.equals(other = emailOrUserName, ignoreCase = true)) {
                        if (signInPassword.equals(password, ignoreCase = true)) {
                            userNameTIL.error = null
                            passwordTIL.error = null
                            prefs.isUserSignIn = true
                            toastController.showToast(activityContext, getString(R.string.sign_in_successfully_toast_message_text), true)
                            startActivity(Intent(activityContext, DashBoardActivity::class.java))
                            finish()
                        } else {
                            userNameTIL.error = null
                            passwordTIL.error = null
                            passwordTIL.error = getString(R.string.password_is_wrong_text)
                        }
                    } else {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        userNameTIL.error = getString(R.string.username_email_is_wrong_text)
                    }
                }

                R.id.forgotPasswordTV -> {
                    if (selectedSecurityQuestion != 0) {
                        showRecoverPasswordDialog()
                    }
                }

                R.id.signUpTV -> openSignUpActivity()
            }
        }
    }

    private fun openSignUpActivity() {
        startActivity(Intent(activityContext, SignUpActivity::class.java))
        finish()
    }

    private fun showRecoverPasswordDialog() {
        val recoverPasswordDialogLayoutBinding = RecoverPasswordDialogLayoutBinding.inflate(layoutInflater)

        val recoverPasswordDialogBuilder = AlertDialog.Builder(activityContext)
        with(recoverPasswordDialogBuilder) {
            setView(recoverPasswordDialogLayoutBinding.root)
            setCancelable(true)
        }
        val recoverPasswordAlertDialog = recoverPasswordDialogBuilder.create()
        if (!activityContext.isFinishing && !activityContext.isDestroyed && !recoverPasswordAlertDialog.isShowing) {
            with(recoverPasswordAlertDialog) {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                window?.setWindowAnimations(R.style.dialogBoxesAnimation)
                show()
            }
        }

        with(recoverPasswordDialogLayoutBinding) {
            securityQuestionAnswerTIL.hint = when (selectedSecurityQuestion) {
                SecurityQuestions.QUESTION_1.ordinal -> getString(R.string.what_is_your_favourite_book_question)
                SecurityQuestions.QUESTION_2.ordinal -> getString(R.string.what_is_your_favourite_teacher_name_question)
                SecurityQuestions.QUESTION_3.ordinal -> getString(R.string.what_is_your_school_name_question)
                SecurityQuestions.QUESTION_4.ordinal -> getString(R.string.what_is_your_favourite_game_question)
                else -> ""
            }
            softKeyboardVisibilityController.showSoftKeyboard()
            securityQuestionAnswerTIET.requestFocus()

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
                    securityQuestionAnswerTIL.error = getString(R.string.please_enter_answer_here_error_text)
                } else if (answer == securityAnswer) {
                    softKeyboardVisibilityController.hideSoftKeyboard(securityQuestionAnswerTIET)
                    securityQuestionAnswerTIL.setErrorTextColor(null)
                    securityQuestionAnswerTIL.error = null
                    if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                        recoverPasswordAlertDialog.dismiss()
                    }
                    openSignUpActivity()
                } else {
                    securityQuestionAnswerTIL.error = getString(R.string.enter_right_answer_error_text)
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
        val check = java.lang.Boolean.parseBoolean(userCredentialArray[5])
        with(binding) {
            if (prefs.rememberMe) {
                userNameTIL.editText?.setText(emailOrUserName)
                passwordTIL.editText?.setText(password)
                userNameTIL.editText?.setSelection(emailOrUserName.length)
                passwordTIL.editText?.setSelection(password.length)
                rememberMeCB.isChecked = true
            } else {
                rememberMeCB.isChecked = false
            }

            dontHaveAnAccountSignUpAccountGroup.changeVisibility(
                if (check) Visibility.GONE.ordinal else Visibility.VISIBLE.ordinal
            )
        }
    }
}