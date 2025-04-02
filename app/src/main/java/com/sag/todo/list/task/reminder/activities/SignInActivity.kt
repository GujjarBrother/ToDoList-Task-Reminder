package com.sag.todo.list.task.reminder.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivitySignInBinding
import com.sag.todo.list.task.reminder.databinding.RecoverPasswordDialogLayoutBinding
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeVisibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.keepActivityOn
import com.sag.todo.list.task.reminder.utils.CommonFunctions.makeFullScreenActivity

class SignInActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }
    private lateinit var emailOrUserName: String
    private lateinit var password: String
    private var securityQuestion = ""
    private lateinit var securityAnswer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fetchUser()
        makeFullScreenActivity(activityContext)
        keepActivityOn(activityContext)

        with(binding) {
            signInCardViewAnimation()
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

    private fun ActivitySignInBinding.signInCardViewAnimation() =
            signInCV.startAnimation(AnimationUtils.loadAnimation(activityContext, R.anim.sign_in_and_sign_up_card_views_animation))

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.signInButton -> {
                    val signInEmailOrUserName = userNameTIL.editText?.text.toString().trim()
                    val signInPassword = passwordTIL.editText?.text.toString().trim()
                    if (TextUtils.isEmpty(signInEmailOrUserName)) {
                        passwordTIL.error = null
                        userNameTIL.error = getString(R.string.fill_this_field_text)
                    } else if (TextUtils.isEmpty(signInPassword)) {
                        userNameTIL.error = null
                        passwordTIL.error = getString(R.string.fill_this_field_text)
                    } else if (signInEmailOrUserName.equals(emailOrUserName, ignoreCase = true)) {
                        if (signInPassword.equals(password, ignoreCase = true)) {
                            userNameTIL.error = null
                            passwordTIL.error = null
                            prefs.isUserSignIn = true
                            toastController.showToast(activityContext, getString(R.string.sign_in_successfully_toast_message_text), true)
                            openDashBoardActivity()
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
                    if (securityQuestion != "") {
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

    private fun openDashBoardActivity() {
        startActivity(Intent(activityContext, DashBoardActivity::class.java))
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
            recoverPasswordAlertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            recoverPasswordAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            recoverPasswordAlertDialog.show()
        }

        with(recoverPasswordDialogLayoutBinding) {
            securityQuestionAnswerTIL.hint = securityQuestion
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
                if (TextUtils.isEmpty(answer)) {
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
        securityQuestion = userCredentialArray[3]
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

            if (check) {
                dontHaveAnAccountSignUpAccountGroup.changeVisibility(Visibility.GONE.ordinal)
            } else {
                dontHaveAnAccountSignUpAccountGroup.changeVisibility(Visibility.VISIBLE.ordinal)
            }
        }
    }
}