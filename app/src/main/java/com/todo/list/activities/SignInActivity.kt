package com.todo.list.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import com.todo.list.R
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivitySignInBinding
import com.todo.list.databinding.RecoverPasswordDialogLayoutBinding
import com.todo.list.enums.Visibility
import com.todo.list.models.SelectedColors
import com.todo.list.utils.ColorsUtils.blackColor
import com.todo.list.utils.ColorsUtils.cardsNightModeColor
import com.todo.list.utils.ColorsUtils.darkModeTextColor
import com.todo.list.utils.ColorsUtils.getContextCompatColor
import com.todo.list.utils.ColorsUtils.getSelectedColor
import com.todo.list.utils.ColorsUtils.lightBlueColor
import com.todo.list.utils.ColorsUtils.screensNightModeColor
import com.todo.list.utils.ColorsUtils.whiteColor
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity
import es.dmoral.toasty.Toasty

class SignInActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var emailOrUserName: String
    private lateinit var password: String
    private var securityQuestion = ""
    private lateinit var securityAnswer: String
    private lateinit var errorColorStateList: ColorStateList
    private lateinit var selectedColors: SelectedColors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedColors = getSelectedColor(context = activityContext, prefs = prefs)

        applyLightAndDarkMode()
        fetchUser()
        signInCardViewAnimation()
        makeFullScreenActivity(activityContext)
        keepActivityOn(activityContext)
        applyCustomFont()

//        Here, We Handle Click Listener's...
        with(binding) {
            signInButton.setOnClickListener(this@SignInActivity)
            forgotPasswordTV.setOnClickListener(this@SignInActivity)
            signUpTV.setOnClickListener(this@SignInActivity)
            rememberMeCB.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                prefs.rememberMe = isChecked
            }
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            signInTV.typeface = typeface
            userNameTIL.typeface = typeface
            userNameTIET.typeface = typeface
            passwordTIL.typeface = typeface
            passwordTIET.typeface = typeface
            rememberMeCB.typeface = typeface
            signInButton.typeface = typeface
            forgotPasswordTV.typeface = typeface
            dontHaveAnAccountTV.typeface = typeface
            signUpTV.typeface = typeface
        }
    }

    private fun signInCardViewAnimation() =
            binding.signInCV.startAnimation(AnimationUtils.loadAnimation(activityContext, R.anim.sign_in_and_sign_up_card_views_animation))

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
                            Toasty.success(activityContext, R.string.sign_in_successfully_toast_message_text, Toasty.LENGTH_LONG).show()
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

                R.id.signUpTV -> {
                    openSignUpActivity()
                }
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
            recoverPasswordAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            recoverPasswordAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            recoverPasswordAlertDialog.show()
        }

        with(recoverPasswordDialogLayoutBinding) {
            applyCustomFontOnRecoverPasswordDialogViews(this)
            applyLightAndDarkModeOnRecoverPasswordDialogViews(this)
            securityQuestionAnswerTextInputLayout.hint = securityQuestion
            showSoftKeyboard()
            securityQuestionAnswerTextInputEditText.requestFocus()

            dismissDialogIV.setOnClickListener { _: View? ->
                hideSoftKeyboard(recoverPasswordDialogLayoutBinding.securityQuestionAnswerTextInputEditText)
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    recoverPasswordAlertDialog.dismiss()
                }
            }

            resetPasswordButton.setOnClickListener { _: View? ->
                val answer = securityQuestionAnswerTextInputLayout.editText?.text.toString().trim()
                if (TextUtils.isEmpty(answer)) {
                    securityQuestionAnswerTextInputLayout.error = getString(R.string.please_enter_answer_here_error_text)
                } else if (answer == securityAnswer) {
                    hideSoftKeyboard(securityQuestionAnswerTextInputEditText)
                    securityQuestionAnswerTextInputLayout.setErrorTextColor(null)
                    securityQuestionAnswerTextInputLayout.error = null
                    if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                        recoverPasswordAlertDialog.dismiss()
                    }
                    openSignUpActivity()
                } else {
                    securityQuestionAnswerTextInputLayout.error = getString(R.string.enter_right_answer_error_text)
                }
            }
        }
    }

    private fun applyLightAndDarkModeOnRecoverPasswordDialogViews(
            recoverPasswordDialogLayoutBinding: RecoverPasswordDialogLayoutBinding
    ) {
        with(recoverPasswordDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, screensNightModeColor), PorterDuff.Mode.SRC_IN)
                dismissDialogIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                resetPasswordTitleTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                resetPasswordButton.setBackgroundColor(getContextCompatColor(activityContext, lightBlueColor))
                resetPasswordButton.setTextColor(getContextCompatColor(activityContext, blackColor))

                securityQuestionAnswerTextInputLayout.boxStrokeColor = getContextCompatColor(activityContext, whiteColor)
                securityQuestionAnswerTextInputLayout.setStartIconTintList(ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor)))
                securityQuestionAnswerTextInputLayout.hintTextColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                securityQuestionAnswerTextInputEditText.setTextColor(getContextCompatColor(activityContext, whiteColor))
                securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor)))
                securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor)))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                }
            } else {
                dismissDialogIV.setColorFilter(selectedColors.originalColor)
                resetPasswordTitleTV.setTextColor(selectedColors.originalColor)
                securityQuestionAnswerTextInputLayout.setStartIconTintList(
                    ColorStateList.valueOf(selectedColors.originalColor))
                securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(selectedColors.originalColor))
                securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(selectedColors.originalColor)
                securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(selectedColors.originalColor))
                resetPasswordButton.setBackgroundColor(selectedColors.originalColor)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(selectedColors.originalColor)
                }
            }
        }
    }

    private fun applyCustomFontOnRecoverPasswordDialogViews(
        recoverPasswordDialogLayoutBinding: RecoverPasswordDialogLayoutBinding
    ) {
        with(recoverPasswordDialogLayoutBinding) {
            resetPasswordTitleTV.typeface = typeface
            securityQuestionAnswerTextInputLayout.typeface = typeface
            securityQuestionAnswerTextInputEditText.typeface = typeface
            resetPasswordButton.typeface = typeface
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
                dontHaveAnAccountAndSignUpLayout.changeVisibility(Visibility.GONE.ordinal)
            } else {
                dontHaveAnAccountAndSignUpLayout.changeVisibility(Visibility.VISIBLE.ordinal)
            }
        }
    }

    private fun applyLightAndDarkMode() {
        with(binding) {
            if (prefs.isDarkModeEnable) {
                changeStatusBarColor(activityContext, screensNightModeColor)
                rootLayout.setBackgroundColor(screensNightModeColor)
                firstIB.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, cardsNightModeColor), PorterDuff.Mode.SRC_IN)
                secondIB.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, cardsNightModeColor), PorterDuff.Mode.SRC_IN)
                thirdIB.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, cardsNightModeColor), PorterDuff.Mode.SRC_IN)
                fourthIB.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, cardsNightModeColor), PorterDuff.Mode.SRC_IN)
                signInTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                signInCV.setCardBackgroundColor(getContextCompatColor(activityContext, cardsNightModeColor))

                userNameTIL.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                userNameTIL.setStartIconTintList(ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor)))
                userNameTIL.setErrorIconTintList(ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor)))
                userNameTIL.boxStrokeColor = getContextCompatColor(activityContext, whiteColor)
                userNameTIL.hintTextColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                userNameTIL.boxStrokeErrorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                userNameTIET.setTextColor(getContextCompatColor(activityContext, whiteColor))
                userNameTIL.setErrorTextColor(ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor)))

                passwordTIL.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                passwordTIL.setStartIconTintList(ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor)))
                passwordTIL.setErrorIconTintList(ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor)))
                passwordTIL.setEndIconTintList(ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor)))
                passwordTIL.boxStrokeColor = getContextCompatColor(activityContext, whiteColor)
                passwordTIL.hintTextColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                passwordTIL.boxStrokeErrorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                passwordTIL.counterTextColor = ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor))
                passwordTIET.setTextColor(getContextCompatColor(activityContext, whiteColor))
                passwordTIL.setErrorTextColor(ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor)))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    userNameTIL.cursorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                    passwordTIL.cursorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                }

                rememberMeCB.buttonTintList = ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor))
                rememberMeCB.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                signInButton.setBackgroundColor(getContextCompatColor(activityContext, lightBlueColor))
                signInButton.setTextColor(getContextCompatColor(activityContext, blackColor))
                signUpTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
            } else {
                changeStatusBarColor(activityContext, selectedColors.originalColor)
                val defaultColorStateList = ColorStateList.valueOf(selectedColors.originalColor)
                errorColorStateList = defaultColorStateList
                firstIB.background.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                secondIB.background.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                thirdIB.background.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                fourthIB.background.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                signInButton.setBackgroundColor(selectedColors.originalColor)
                signInTV.setTextColor(selectedColors.originalColor)
                signUpTV.setTextColor(selectedColors.originalColor)
                rememberMeCB.buttonTintList = defaultColorStateList

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    userNameTIL.cursorColor = defaultColorStateList
                    passwordTIL.cursorColor = defaultColorStateList
                }

                userNameTIL.setStartIconTintList(defaultColorStateList)
                userNameTIL.boxStrokeErrorColor = defaultColorStateList
                userNameTIL.setErrorIconTintList(defaultColorStateList)
                userNameTIL.setErrorTextColor(defaultColorStateList)

                passwordTIL.setStartIconTintList(defaultColorStateList)
                passwordTIL.setEndIconTintList(defaultColorStateList)
                passwordTIL.counterTextColor = defaultColorStateList
                passwordTIL.boxStrokeErrorColor = defaultColorStateList
                passwordTIL.setErrorIconTintList(defaultColorStateList)
                passwordTIL.setErrorTextColor(defaultColorStateList)
            }
        }
    }
}