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
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import com.todo.list.R
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivitySignInBinding
import com.todo.list.databinding.RecoverPasswordDialogLayoutBinding
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                            prefs.isUserSignInOrSignOutValue = true
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
                rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                dismissDialogIV.setColorFilter(lightBlueColor)
                resetPasswordTitleTV.setTextColor(lightBlueColor)
                resetPasswordButton.setBackgroundColor(lightBlueColor)
                resetPasswordButton.setTextColor(blackColor)

                securityQuestionAnswerTextInputLayout.boxStrokeColor = whiteColor
                securityQuestionAnswerTextInputLayout.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                securityQuestionAnswerTextInputLayout.hintTextColor = whiteColorStateList
                securityQuestionAnswerTextInputEditText.setTextColor(whiteColor)
                securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                securityQuestionAnswerTextInputLayout.setErrorTextColor(whiteColorStateList)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    securityQuestionAnswerTextInputLayout.cursorColor = whiteColorStateList
                }
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        dismissDialogIV.setColorFilter(defaultColor)
                        resetPasswordTitleTV.setTextColor(defaultColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(defaultColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(defaultColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(defaultColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(defaultColor))
                        resetPasswordButton.setBackgroundColor(defaultColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(defaultColor)
                        }
                    }

                    1 -> {
                        dismissDialogIV.setColorFilter(darkYellowColor)
                        resetPasswordTitleTV.setTextColor(darkYellowColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(darkYellowColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(darkYellowColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(darkYellowColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(darkYellowColor))
                        resetPasswordButton.setBackgroundColor(darkYellowColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(darkYellowColor)
                        }
                    }

                    2 -> {
                        dismissDialogIV.setColorFilter(orangeColor)
                        resetPasswordTitleTV.setTextColor(orangeColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(orangeColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(orangeColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(orangeColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(orangeColor))
                        resetPasswordButton.setBackgroundColor(orangeColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(orangeColor)
                        }
                    }

                    3 -> {
                        dismissDialogIV.setColorFilter(lightGreenColor)
                        resetPasswordTitleTV.setTextColor(lightGreenColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(lightGreenColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(lightGreenColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(lightGreenColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(lightGreenColor))
                        resetPasswordButton.setBackgroundColor(lightGreenColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(lightGreenColor)
                        }
                    }

                    4 -> {
                        dismissDialogIV.setColorFilter(blueColor)
                        resetPasswordTitleTV.setTextColor(blueColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(blueColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(blueColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(blueColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(blueColor))
                        resetPasswordButton.setBackgroundColor(blueColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(blueColor)
                        }
                    }

                    5 -> {
                        dismissDialogIV.setColorFilter(cyanColor)
                        resetPasswordTitleTV.setTextColor(cyanColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(cyanColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(cyanColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(cyanColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(cyanColor))
                        resetPasswordButton.setBackgroundColor(cyanColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(cyanColor)
                        }
                    }

                    6 -> {
                        dismissDialogIV.setColorFilter(pinkColor)
                        resetPasswordTitleTV.setTextColor(pinkColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(pinkColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(pinkColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(pinkColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(pinkColor))
                        resetPasswordButton.setBackgroundColor(pinkColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(pinkColor)
                        }
                    }

                    7 -> {
                        dismissDialogIV.setColorFilter(darkBlueColor)
                        resetPasswordTitleTV.setTextColor(darkBlueColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(darkBlueColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(darkBlueColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(darkBlueColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(darkBlueColor))
                        resetPasswordButton.setBackgroundColor(darkBlueColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(darkBlueColor)
                        }
                    }

                    8 -> {
                        dismissDialogIV.setColorFilter(redColor)
                        resetPasswordTitleTV.setTextColor(redColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(redColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(redColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(redColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(redColor))
                        resetPasswordButton.setBackgroundColor(redColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(redColor)
                        }
                    }

                    9 -> {
                        dismissDialogIV.setColorFilter(lightPurpleColor)
                        resetPasswordTitleTV.setTextColor(lightPurpleColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        securityQuestionAnswerTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = ColorStateList.valueOf(lightPurpleColor)
                        securityQuestionAnswerTextInputLayout.setErrorTextColor(ColorStateList.valueOf(lightPurpleColor))
                        resetPasswordButton.setBackgroundColor(lightPurpleColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTextInputLayout.cursorColor = ColorStateList.valueOf(lightPurpleColor)
                        }
                    }
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
                dontHaveAnAccountAndSignUpLayout.visibility = GONE
            } else {
                dontHaveAnAccountAndSignUpLayout.visibility = VISIBLE
            }
        }
    }

    private fun applyLightAndDarkMode() {
        with(binding) {
            if (prefs.isDarkModeEnable) {
                changeStatusBarColor(activityContext, screensNightModeColor)
                rootLayout.setBackgroundColor(screensNightModeColor)
                firstIB.background.colorFilter = PorterDuffColorFilter(cardsNightModeColor, PorterDuff.Mode.SRC_IN)
                secondIB.background.colorFilter = PorterDuffColorFilter(cardsNightModeColor, PorterDuff.Mode.SRC_IN)
                thirdIB.background.colorFilter = PorterDuffColorFilter(cardsNightModeColor, PorterDuff.Mode.SRC_IN)
                fourthIB.background.colorFilter = PorterDuffColorFilter(cardsNightModeColor, PorterDuff.Mode.SRC_IN)
                signInTV.setTextColor(lightBlueColor)
                signInCV.setCardBackgroundColor(cardsNightModeColor)

                userNameTIL.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                userNameTIL.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                userNameTIL.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                userNameTIL.boxStrokeColor = whiteColor
                userNameTIL.hintTextColor = whiteColorStateList
                userNameTIL.boxStrokeErrorColor = whiteColorStateList
                userNameTIET.setTextColor(whiteColor)
                userNameTIL.setErrorTextColor(whiteColorStateList)

                passwordTIL.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                passwordTIL.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                passwordTIL.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                passwordTIL.setEndIconTintList(ColorStateList.valueOf(lightBlueColor))
                passwordTIL.boxStrokeColor = whiteColor
                passwordTIL.hintTextColor = whiteColorStateList
                passwordTIL.boxStrokeErrorColor = whiteColorStateList
                passwordTIL.counterTextColor = ColorStateList.valueOf(lightBlueColor)
                passwordTIET.setTextColor(whiteColor)
                passwordTIL.setErrorTextColor(whiteColorStateList)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    userNameTIL.cursorColor = whiteColorStateList
                    passwordTIL.cursorColor = whiteColorStateList
                }

                rememberMeCB.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                rememberMeCB.setTextColor(darkModeTextColor)
                signInButton.setBackgroundColor(lightBlueColor)
                signInButton.setTextColor(blackColor)
                signUpTV.setTextColor(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        changeStatusBarColor(activityContext, defaultColor)
                        val defaultColorStateList = ColorStateList.valueOf(defaultColor)
                        errorColorStateList = defaultColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(defaultColor)
                        signInTV.setTextColor(defaultColor)
                        signUpTV.setTextColor(defaultColor)
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

                    1 -> {
                        changeStatusBarColor(activityContext, darkYellowColor)
                        val darkYellowColorStateList = ColorStateList.valueOf(darkYellowColor)
                        errorColorStateList = darkYellowColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(darkYellowColor)
                        signInTV.setTextColor(darkYellowColor)
                        rememberMeCB.buttonTintList = darkYellowColorStateList
                        signUpTV.setTextColor(darkYellowColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userNameTIL.cursorColor = darkYellowColorStateList
                            passwordTIL.cursorColor = darkYellowColorStateList
                        }

                        userNameTIL.setStartIconTintList(darkYellowColorStateList)
                        userNameTIL.boxStrokeErrorColor = darkYellowColorStateList
                        userNameTIL.setErrorIconTintList(darkYellowColorStateList)
                        userNameTIL.setErrorTextColor(darkYellowColorStateList)

                        passwordTIL.setStartIconTintList(darkYellowColorStateList)
                        passwordTIL.setEndIconTintList(darkYellowColorStateList)
                        passwordTIL.counterTextColor = darkYellowColorStateList
                        passwordTIL.boxStrokeErrorColor = darkYellowColorStateList
                        passwordTIL.setErrorIconTintList(darkYellowColorStateList)
                        passwordTIL.setErrorTextColor(darkYellowColorStateList)
                    }

                    2 -> {
                        changeStatusBarColor(activityContext, orangeColor)
                        val orangeColorStateList = ColorStateList.valueOf(orangeColor)
                        errorColorStateList = orangeColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(orangeColor)
                        signInTV.setTextColor(orangeColor)
                        rememberMeCB.buttonTintList = orangeColorStateList
                        signUpTV.setTextColor(orangeColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userNameTIL.cursorColor = orangeColorStateList
                            passwordTIL.cursorColor = orangeColorStateList
                        }

                        userNameTIL.setStartIconTintList(orangeColorStateList)
                        userNameTIL.boxStrokeErrorColor = orangeColorStateList
                        userNameTIL.setErrorIconTintList(orangeColorStateList)
                        userNameTIL.setErrorTextColor(orangeColorStateList)

                        passwordTIL.setStartIconTintList(orangeColorStateList)
                        passwordTIL.setEndIconTintList(orangeColorStateList)
                        passwordTIL.counterTextColor = orangeColorStateList
                        passwordTIL.boxStrokeErrorColor = orangeColorStateList
                        passwordTIL.setErrorIconTintList(orangeColorStateList)
                        passwordTIL.setErrorTextColor(orangeColorStateList)
                    }

                    3 -> {
                        changeStatusBarColor(activityContext, lightGreenColor)
                        val lightGreenColorStateList = ColorStateList.valueOf(lightGreenColor)
                        errorColorStateList = lightGreenColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(lightGreenColor)
                        signInTV.setTextColor(lightGreenColor)
                        rememberMeCB.buttonTintList = lightGreenColorStateList
                        signUpTV.setTextColor(lightGreenColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userNameTIL.cursorColor = lightGreenColorStateList
                            passwordTIL.cursorColor = lightGreenColorStateList
                        }

                        userNameTIL.setStartIconTintList(lightGreenColorStateList)
                        userNameTIL.boxStrokeErrorColor = lightGreenColorStateList
                        userNameTIL.setErrorIconTintList(lightGreenColorStateList)
                        userNameTIL.setErrorTextColor(lightGreenColorStateList)

                        passwordTIL.setStartIconTintList(lightGreenColorStateList)
                        passwordTIL.setEndIconTintList(lightGreenColorStateList)
                        passwordTIL.counterTextColor = lightGreenColorStateList
                        passwordTIL.boxStrokeErrorColor = lightGreenColorStateList
                        passwordTIL.setErrorIconTintList(lightGreenColorStateList)
                        passwordTIL.setErrorTextColor(lightGreenColorStateList)
                    }

                    4 -> {
                        changeStatusBarColor(activityContext, blueColor)
                        val blueColorStateList = ColorStateList.valueOf(blueColor)
                        errorColorStateList = blueColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(blueColor)
                        signInTV.setTextColor(blueColor)
                        rememberMeCB.buttonTintList = blueColorStateList
                        signUpTV.setTextColor(blueColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userNameTIL.cursorColor = blueColorStateList
                            passwordTIL.cursorColor = blueColorStateList
                        }

                        userNameTIL.setStartIconTintList(blueColorStateList)
                        userNameTIL.boxStrokeErrorColor = blueColorStateList
                        userNameTIL.setErrorIconTintList(blueColorStateList)
                        userNameTIL.setErrorTextColor(blueColorStateList)

                        passwordTIL.setStartIconTintList(blueColorStateList)
                        passwordTIL.setEndIconTintList(blueColorStateList)
                        passwordTIL.counterTextColor = blueColorStateList
                        passwordTIL.boxStrokeErrorColor = blueColorStateList
                        passwordTIL.setErrorIconTintList(blueColorStateList)
                        passwordTIL.setErrorTextColor(blueColorStateList)
                    }

                    5 -> {
                        changeStatusBarColor(activityContext, cyanColor)
                        val cyanColorStateList = ColorStateList.valueOf(cyanColor)
                        errorColorStateList = cyanColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(cyanColor)
                        signInTV.setTextColor(cyanColor)
                        rememberMeCB.buttonTintList = cyanColorStateList
                        signUpTV.setTextColor(cyanColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userNameTIL.cursorColor = cyanColorStateList
                            passwordTIL.cursorColor = cyanColorStateList
                        }

                        userNameTIL.setStartIconTintList(cyanColorStateList)
                        userNameTIL.boxStrokeErrorColor = cyanColorStateList
                        userNameTIL.setErrorIconTintList(cyanColorStateList)
                        userNameTIL.setErrorTextColor(cyanColorStateList)

                        passwordTIL.setStartIconTintList(cyanColorStateList)
                        passwordTIL.setEndIconTintList(cyanColorStateList)
                        passwordTIL.counterTextColor = cyanColorStateList
                        passwordTIL.boxStrokeErrorColor = cyanColorStateList
                        passwordTIL.setErrorIconTintList(cyanColorStateList)
                        passwordTIL.setErrorTextColor(cyanColorStateList)
                    }

                    6 -> {
                        changeStatusBarColor(activityContext, pinkColor)
                        val pinkColorStateList = ColorStateList.valueOf(pinkColor)
                        errorColorStateList = pinkColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(pinkColor)
                        signInTV.setTextColor(pinkColor)
                        rememberMeCB.buttonTintList = pinkColorStateList
                        signUpTV.setTextColor(pinkColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userNameTIL.cursorColor = pinkColorStateList
                            passwordTIL.cursorColor = pinkColorStateList
                        }

                        userNameTIL.setStartIconTintList(pinkColorStateList)
                        userNameTIL.boxStrokeErrorColor = pinkColorStateList
                        userNameTIL.setErrorIconTintList(pinkColorStateList)
                        userNameTIL.setErrorTextColor(pinkColorStateList)

                        passwordTIL.setStartIconTintList(pinkColorStateList)
                        passwordTIL.setEndIconTintList(pinkColorStateList)
                        passwordTIL.counterTextColor = pinkColorStateList
                        passwordTIL.boxStrokeErrorColor = pinkColorStateList
                        passwordTIL.setErrorIconTintList(pinkColorStateList)
                        passwordTIL.setErrorTextColor(pinkColorStateList)
                    }

                    7 -> {
                        changeStatusBarColor(activityContext, darkBlueColor)
                        val darkBlueColorStateList = ColorStateList.valueOf(darkBlueColor)
                        errorColorStateList = darkBlueColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(darkBlueColor)
                        signInTV.setTextColor(darkBlueColor)
                        rememberMeCB.buttonTintList = darkBlueColorStateList
                        signUpTV.setTextColor(darkBlueColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userNameTIL.cursorColor = darkBlueColorStateList
                            passwordTIL.cursorColor = darkBlueColorStateList
                        }

                        userNameTIL.setStartIconTintList(darkBlueColorStateList)
                        userNameTIL.boxStrokeErrorColor = darkBlueColorStateList
                        userNameTIL.setErrorIconTintList(darkBlueColorStateList)
                        userNameTIL.setErrorTextColor(darkBlueColorStateList)

                        passwordTIL.setStartIconTintList(darkBlueColorStateList)
                        passwordTIL.setEndIconTintList(darkBlueColorStateList)
                        passwordTIL.counterTextColor = darkBlueColorStateList
                        passwordTIL.boxStrokeErrorColor = darkBlueColorStateList
                        passwordTIL.setErrorIconTintList(darkBlueColorStateList)
                        passwordTIL.setErrorTextColor(darkBlueColorStateList)
                    }

                    8 -> {
                        changeStatusBarColor(activityContext, redColor)
                        val redColorStateList = ColorStateList.valueOf(redColor)
                        errorColorStateList = redColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(redColor)
                        signInTV.setTextColor(redColor)
                        rememberMeCB.buttonTintList = redColorStateList
                        signUpTV.setTextColor(redColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userNameTIL.cursorColor = redColorStateList
                            passwordTIL.cursorColor = redColorStateList
                        }

                        userNameTIL.setStartIconTintList(redColorStateList)
                        userNameTIL.boxStrokeErrorColor = redColorStateList
                        userNameTIL.setErrorIconTintList(redColorStateList)
                        userNameTIL.setErrorTextColor(redColorStateList)

                        passwordTIL.setStartIconTintList(redColorStateList)
                        passwordTIL.setEndIconTintList(redColorStateList)
                        passwordTIL.counterTextColor = redColorStateList
                        passwordTIL.boxStrokeErrorColor = redColorStateList
                        passwordTIL.setErrorIconTintList(redColorStateList)
                        passwordTIL.setErrorTextColor(redColorStateList)
                    }

                    9 -> {
                        changeStatusBarColor(activityContext, lightPurpleColor)
                        val lightPurpleColorStateList = ColorStateList.valueOf(lightPurpleColor)
                        errorColorStateList = lightPurpleColorStateList
                        firstIB.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        secondIB.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        thirdIB.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        fourthIB.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(lightPurpleColor)
                        signInTV.setTextColor(lightPurpleColor)
                        rememberMeCB.buttonTintList = lightPurpleColorStateList
                        signUpTV.setTextColor(lightPurpleColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userNameTIL.cursorColor = lightPurpleColorStateList
                            passwordTIL.cursorColor = lightPurpleColorStateList
                        }

                        userNameTIL.setStartIconTintList(lightPurpleColorStateList)
                        userNameTIL.boxStrokeErrorColor = lightPurpleColorStateList
                        userNameTIL.setErrorIconTintList(lightPurpleColorStateList)
                        userNameTIL.setErrorTextColor(lightPurpleColorStateList)

                        passwordTIL.setStartIconTintList(lightPurpleColorStateList)
                        passwordTIL.setEndIconTintList(lightPurpleColorStateList)
                        passwordTIL.counterTextColor = lightPurpleColorStateList
                        passwordTIL.boxStrokeErrorColor = lightPurpleColorStateList
                        passwordTIL.setErrorIconTintList(lightPurpleColorStateList)
                        passwordTIL.setErrorTextColor(lightPurpleColorStateList)
                    }
                }
            }
        }
    }
}