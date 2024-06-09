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
import androidx.core.content.ContextCompat
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

        applyColorSchemeORLightAndDarkMode()
        fetchUser()
        signInCardViewAnimation()
        makeFullScreenActivity(activityContext)
        keepActivityOn(activityContext)
        applyCustomFont()

//        Here, We Handle Click Listener's...
        with(binding) {
            signInButton.setOnClickListener(this@SignInActivity)
            forgotPasswordTextView.setOnClickListener(this@SignInActivity)
            signUpTextView.setOnClickListener(this@SignInActivity)
            rememberMeCheckBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                prefs.rememberMe = isChecked
            }
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            signInTextView.typeface = typeface
            userNameTextInputLayout.typeface = typeface
            userNameTextInputEditText.typeface = typeface
            passwordTextInputLayout.typeface = typeface
            passwordTextInputEditText.typeface = typeface
            rememberMeCheckBox.typeface = typeface
            signInButton.typeface = typeface
            forgotPasswordTextView.typeface = typeface
            dontHaveAnAccountTextView.typeface = typeface
            signUpTextView.typeface = typeface
        }
    }

    private fun signInCardViewAnimation() =
            binding.signInCardView.startAnimation(AnimationUtils.loadAnimation(activityContext, R.anim.sign_in_and_sign_up_card_views_animation))

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.signInButton -> {
                    val signInEmailOrUserName = userNameTextInputLayout.editText?.text.toString().trim()
                    val signInPassword = passwordTextInputLayout.editText?.text.toString().trim()
                    if (TextUtils.isEmpty(signInEmailOrUserName)) {
                        passwordTextInputLayout.error = null
                        userNameOREmailTextInputLayoutClickColors()
                        userNameTextInputLayout.error = getString(R.string.fill_this_field_text)
                    } else if (TextUtils.isEmpty(signInPassword)) {
                        userNameTextInputLayout.error = null
                        passwordTextInputLayoutClickColors()
                        passwordTextInputLayout.error = getString(R.string.fill_this_field_text)
                    } else if (signInEmailOrUserName.equals(emailOrUserName, ignoreCase = true)) {
                        if (signInPassword.equals(password, ignoreCase = true)) {
                            userNameTextInputLayout.error = null
                            passwordTextInputLayout.error = null

                            prefs.isUserSignInOrSignOutValue = true
                            Toasty.success(activityContext, R.string.sign_in_successfully_toast_message_text, Toasty.LENGTH_LONG).show()
                            openDashBoardActivity()
                        } else {
                            userNameTextInputLayout.error = null
                            passwordTextInputLayout.error = null
                            passwordTextInputLayoutClickColors()
                            passwordTextInputLayout.error = getString(R.string.password_is_wrong_text)
                        }
                    } else {
                        userNameTextInputLayout.error = null
                        passwordTextInputLayout.error = null
                        userNameOREmailTextInputLayoutClickColors()
                        userNameTextInputLayout.error = getString(R.string.username_email_is_wrong_text)
                    }
                }

                R.id.forgotPasswordTextView -> {
                    if (securityQuestion != "") {
                        showRecoverPasswordDialog()
                    }
                }

                R.id.signUpTextView -> {
                    openSignUpActivity()
                }
            }
        }
    }

    private fun userNameOREmailTextInputLayoutClickColors() {
        with(binding) {
            if (prefs.dayAndNightModeSwitchValue) {
                userNameTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                userNameTextInputLayout.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                userNameTextInputLayout.setErrorTextColor(whiteColorStateList)
            } else {
                userNameTextInputLayout.boxStrokeErrorColor = errorColorStateList
                userNameTextInputLayout.setErrorIconTintList(errorColorStateList)
                userNameTextInputLayout.setErrorTextColor(errorColorStateList)
            }
        }
    }

    private fun passwordTextInputLayoutClickColors() {
        with(binding) {
            if (prefs.dayAndNightModeSwitchValue) {
                passwordTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                passwordTextInputLayout.setErrorIconTintList(whiteColorStateList)
                passwordTextInputLayout.setErrorTextColor(whiteColorStateList)
            } else {
                passwordTextInputLayout.boxStrokeErrorColor = errorColorStateList
                passwordTextInputLayout.setErrorIconTintList(errorColorStateList)
                passwordTextInputLayout.setErrorTextColor(errorColorStateList)
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
            applyColorSchemeORLightAndDarkModeOnRecoverPasswordDialogViews(this)
            securityQuestionAnswerTextInputLayout.hint = securityQuestion
            showSoftKeyboard()
            securityQuestionAnswerTextInputEditText.requestFocus()
            dismissDialogIV.setOnClickListener { _: View? ->
                hideSoftKeyboard(recoverPasswordDialogLayoutBinding.securityQuestionAnswerTextInputEditText)
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    recoverPasswordAlertDialog.dismiss()
                }
            }

            recoverPasswordButton.setOnClickListener { _: View? ->
                val answer = securityQuestionAnswerTextInputLayout.editText?.text.toString().trim()
                if (TextUtils.isEmpty(answer)) {
                    recoverPasswordTextInputLayoutClickColor(this)
                    securityQuestionAnswerTextInputLayout.error = getString(R.string.please_enter_answer_here_error_text)
                } else if (answer == securityAnswer) {
                    hideSoftKeyboard(securityQuestionAnswerTextInputEditText)
                    securityQuestionAnswerTextInputLayout.setErrorTextColor(null)
                    securityQuestionAnswerTextInputLayout.error = null
                    group1.visibility = View.GONE
                    passwordTextView.visibility = View.VISIBLE
                    passwordTextView.text = String.format("%s %s", getString(R.string.your_password_is_text), password)
                } else {
                    recoverPasswordTextInputLayoutClickColor(this)
                    securityQuestionAnswerTextInputLayout.error = getString(R.string.enter_right_answer_error_text)
                }
            }
        }
    }

    private fun recoverPasswordTextInputLayoutClickColor(
            recoverPasswordDialogLayoutBinding: RecoverPasswordDialogLayoutBinding
    ) {
        with(recoverPasswordDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                securityQuestionAnswerTextInputLayout.setErrorIconTintList(whiteColorStateList)
                securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                securityQuestionAnswerTextInputLayout.setErrorTextColor(whiteColorStateList)
            } else {
                securityQuestionAnswerTextInputLayout.setErrorIconTintList(errorColorStateList)
                securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = errorColorStateList
                securityQuestionAnswerTextInputLayout.setErrorTextColor(errorColorStateList)
            }
        }
    }

    private fun applyColorSchemeORLightAndDarkModeOnRecoverPasswordDialogViews(
            recoverPasswordDialogLayoutBinding: RecoverPasswordDialogLayoutBinding
    ) {
        with(recoverPasswordDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                dismissDialogIV.setColorFilter(lightBlueColor)
                recoverPasswordTitleTV.setTextColor(whiteColor)
                recoverPasswordButton.setBackgroundColor(lightBlueColor)
                recoverPasswordButton.setTextColor(blackColor)
                securityQuestionAnswerTextInputLayout.boxStrokeColor = whiteColor
                securityQuestionAnswerTextInputLayout.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                securityQuestionAnswerTextInputLayout.hintTextColor = whiteColorStateList
                securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                securityQuestionAnswerTextInputEditText.setTextColor(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        dismissDialogIV.setColorFilter(defaultColor)
                        recoverPasswordTitleTV.setTextColor(defaultColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(defaultColor))
                        recoverPasswordButton.setBackgroundColor(defaultColor)
                    }

                    1 -> {
                        dismissDialogIV.setColorFilter(darkYellowColor)
                        recoverPasswordTitleTV.setTextColor(darkYellowColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(darkYellowColor))
                        recoverPasswordButton.setBackgroundColor(darkYellowColor)
                    }

                    2 -> {
                        dismissDialogIV.setColorFilter(orangeColor)
                        recoverPasswordTitleTV.setTextColor(orangeColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(orangeColor))
                        recoverPasswordButton.setBackgroundColor(orangeColor)
                    }

                    3 -> {
                        dismissDialogIV.setColorFilter(lightGreenColor)
                        recoverPasswordTitleTV.setTextColor(lightGreenColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(lightGreenColor))
                        recoverPasswordButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        recoverPasswordButton.setBackgroundColor(lightGreenColor)
                    }

                    4 -> {
                        dismissDialogIV.setColorFilter(blueColor)
                        recoverPasswordTitleTV.setTextColor(blueColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(blueColor))
                        recoverPasswordButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        recoverPasswordButton.setBackgroundColor(blueColor)
                    }

                    5 -> {
                        dismissDialogIV.setColorFilter(cyanColor)
                        recoverPasswordTitleTV.setTextColor(cyanColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(cyanColor))
                        recoverPasswordButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        recoverPasswordButton.setBackgroundColor(cyanColor)
                    }

                    6 -> {
                        dismissDialogIV.setColorFilter(pinkColor)
                        recoverPasswordTitleTV.setTextColor(pinkColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(pinkColor))
                        recoverPasswordButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        recoverPasswordButton.setBackgroundColor(pinkColor)
                    }

                    7 -> {
                        dismissDialogIV.setColorFilter(darkBlueColor)
                        recoverPasswordTitleTV.setTextColor(darkBlueColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(darkBlueColor))
                        recoverPasswordButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        recoverPasswordButton.setBackgroundColor(darkBlueColor)
                    }

                    8 -> {
                        dismissDialogIV.setColorFilter(redColor)
                        recoverPasswordTitleTV.setTextColor(redColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(redColor))
                        recoverPasswordButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        recoverPasswordButton.setBackgroundColor(redColor)
                    }

                    9 -> {
                        dismissDialogIV.setColorFilter(lightPurpleColor)
                        recoverPasswordTitleTV.setTextColor(lightPurpleColor)
                        securityQuestionAnswerTextInputLayout.setStartIconTintList(
                            ColorStateList.valueOf(lightPurpleColor))
                        recoverPasswordButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        recoverPasswordButton.setBackgroundColor(lightPurpleColor)
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnRecoverPasswordDialogViews(recoverPasswordDialogLayoutBinding: RecoverPasswordDialogLayoutBinding) {
        with(recoverPasswordDialogLayoutBinding) {
            recoverPasswordTitleTV.typeface = typeface
            securityQuestionAnswerTextInputLayout.typeface = typeface
            securityQuestionAnswerTextInputEditText.typeface = typeface
            recoverPasswordButton.typeface = typeface
            passwordTextView.typeface = typeface
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
                userNameTextInputLayout.editText?.setText(emailOrUserName)
                passwordTextInputLayout.editText?.setText(password)
                rememberMeCheckBox.isChecked = true
            } else {
                rememberMeCheckBox.isChecked = false
            }

            if (check) {
                dontHaveAnAccountAndSignUpLayout.visibility = View.GONE
            } else {
                dontHaveAnAccountAndSignUpLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun applyColorSchemeORLightAndDarkMode() {
        with(binding) {
            if (prefs.dayAndNightModeSwitchValue) {
                changeStatusBarColor(activityContext, screensNightModeColor)
                rootLayout.setBackgroundColor(screensNightModeColor)
                firstImageButton.background.colorFilter = PorterDuffColorFilter(cardsNightModeColor, PorterDuff.Mode.SRC_IN)
                secondImageButton.background.colorFilter = PorterDuffColorFilter(cardsNightModeColor, PorterDuff.Mode.SRC_IN)
                thirdImageButton.background.colorFilter = PorterDuffColorFilter(cardsNightModeColor, PorterDuff.Mode.SRC_IN)
                fourthImageButton.background.colorFilter = PorterDuffColorFilter(cardsNightModeColor, PorterDuff.Mode.SRC_IN)
                signInTextView.setTextColor(darkModeTextColor)
                signInCardView.setCardBackgroundColor(cardsNightModeColor)

//            Here, We Change The Box Stroke Color Of TextInputLayout When That is Un-Focused...
                userNameTextInputLayout.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                passwordTextInputLayout.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                userNameTextInputLayout.boxStrokeColor = whiteColor
                userNameTextInputLayout.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                userNameTextInputLayout.setEndIconTintList(ColorStateList.valueOf(lightBlueColor))
                userNameTextInputLayout.hintTextColor = whiteColorStateList
                userNameTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                userNameTextInputEditText.setTextColor(whiteColor)
                passwordTextInputLayout.boxStrokeColor = whiteColor
                passwordTextInputLayout.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                passwordTextInputLayout.setEndIconTintList(ColorStateList.valueOf(lightBlueColor))
                passwordTextInputLayout.hintTextColor = whiteColorStateList
                passwordTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                passwordTextInputLayout.counterTextColor = ColorStateList.valueOf(lightBlueColor)
                passwordTextInputEditText.setTextColor(whiteColor)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    userNameTextInputEditText.textCursorDrawable = editTextsCursorDarkModeColor
                    passwordTextInputEditText.textCursorDrawable = editTextsCursorDarkModeColor
                }
                rememberMeCheckBox.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                rememberMeCheckBox.setTextColor(darkModeTextColor)
                signInButton.setBackgroundColor(lightBlueColor)
                signInButton.setTextColor(blackColor)
                signUpTextView.setTextColor(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        changeStatusBarColor(activityContext, defaultColor)
                        val defaultColorStateList = ColorStateList.valueOf(defaultColor)
                        errorColorStateList = defaultColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(defaultColor)
                        signInTextView.setTextColor(defaultColor)
                        userNameTextInputLayout.setStartIconTintList(defaultColorStateList)
                        passwordTextInputLayout.setStartIconTintList(defaultColorStateList)
                        passwordTextInputLayout.setEndIconTintList(defaultColorStateList)
                        passwordTextInputLayout.counterTextColor = defaultColorStateList
                        rememberMeCheckBox.buttonTintList = defaultColorStateList
                        signUpTextView.setTextColor(defaultColor)
                    }

                    1 -> {
                        changeStatusBarColor(activityContext, darkYellowColor)
                        val darkYellowColorStateList = ColorStateList.valueOf(darkYellowColor)
                        errorColorStateList = darkYellowColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(darkYellowColor)
                        signInTextView.setTextColor(darkYellowColor)
                        userNameTextInputLayout.setStartIconTintList(darkYellowColorStateList)
                        passwordTextInputLayout.setStartIconTintList(darkYellowColorStateList)
                        passwordTextInputLayout.setEndIconTintList(darkYellowColorStateList)
                        passwordTextInputLayout.counterTextColor = darkYellowColorStateList
                        rememberMeCheckBox.buttonTintList = darkYellowColorStateList
                        signUpTextView.setTextColor(darkYellowColor)
                    }

                    2 -> {
                        changeStatusBarColor(activityContext, orangeColor)
                        val orangeColorStateList = ColorStateList.valueOf(orangeColor)
                        errorColorStateList = orangeColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(orangeColor)
                        signInTextView.setTextColor(orangeColor)
                        userNameTextInputLayout.setStartIconTintList(orangeColorStateList)
                        passwordTextInputLayout.setStartIconTintList(orangeColorStateList)
                        passwordTextInputLayout.setEndIconTintList(orangeColorStateList)
                        passwordTextInputLayout.counterTextColor = orangeColorStateList
                        rememberMeCheckBox.buttonTintList = orangeColorStateList
                        signUpTextView.setTextColor(orangeColor)
                    }

                    3 -> {
                        changeStatusBarColor(activityContext, lightGreenColor)
                        val lightGreenColorStateList = ColorStateList.valueOf(lightGreenColor)
                        errorColorStateList = lightGreenColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(lightGreenColor)
                        signInTextView.setTextColor(lightGreenColor)
                        userNameTextInputLayout.setStartIconTintList(lightGreenColorStateList)
                        passwordTextInputLayout.setStartIconTintList(lightGreenColorStateList)
                        passwordTextInputLayout.setEndIconTintList(lightGreenColorStateList)
                        passwordTextInputLayout.counterTextColor = lightGreenColorStateList
                        rememberMeCheckBox.buttonTintList = lightGreenColorStateList
                        signUpTextView.setTextColor(lightGreenColor)
                    }

                    4 -> {
                        changeStatusBarColor(activityContext, blueColor)
                        val blueColorStateList = ColorStateList.valueOf(blueColor)
                        errorColorStateList = blueColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(blueColor)
                        signInTextView.setTextColor(blueColor)
                        userNameTextInputLayout.setStartIconTintList(blueColorStateList)
                        passwordTextInputLayout.setStartIconTintList(blueColorStateList)
                        passwordTextInputLayout.setEndIconTintList(blueColorStateList)
                        passwordTextInputLayout.counterTextColor = blueColorStateList
                        rememberMeCheckBox.buttonTintList = blueColorStateList
                        signUpTextView.setTextColor(blueColor)
                    }

                    5 -> {
                        changeStatusBarColor(activityContext, cyanColor)
                        val cyanColorStateList = ColorStateList.valueOf(cyanColor)
                        errorColorStateList = cyanColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(cyanColor)
                        signInTextView.setTextColor(cyanColor)
                        userNameTextInputLayout.setStartIconTintList(cyanColorStateList)
                        passwordTextInputLayout.setStartIconTintList(cyanColorStateList)
                        passwordTextInputLayout.setEndIconTintList(cyanColorStateList)
                        passwordTextInputLayout.counterTextColor = cyanColorStateList
                        rememberMeCheckBox.buttonTintList = cyanColorStateList
                        signUpTextView.setTextColor(cyanColor)
                    }

                    6 -> {
                        changeStatusBarColor(activityContext, pinkColor)
                        val pinkColorStateList = ColorStateList.valueOf(pinkColor)
                        errorColorStateList = pinkColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(pinkColor)
                        signInTextView.setTextColor(pinkColor)
                        userNameTextInputLayout.setStartIconTintList(pinkColorStateList)
                        passwordTextInputLayout.setStartIconTintList(pinkColorStateList)
                        passwordTextInputLayout.setEndIconTintList(pinkColorStateList)
                        passwordTextInputLayout.counterTextColor = pinkColorStateList
                        rememberMeCheckBox.buttonTintList = pinkColorStateList
                        signUpTextView.setTextColor(pinkColor)
                    }

                    7 -> {
                        changeStatusBarColor(activityContext, darkBlueColor)
                        val darkBlueColorStateList = ColorStateList.valueOf(darkBlueColor)
                        errorColorStateList = darkBlueColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(darkBlueColor)
                        signInTextView.setTextColor(darkBlueColor)
                        userNameTextInputLayout.setStartIconTintList(darkBlueColorStateList)
                        passwordTextInputLayout.setStartIconTintList(darkBlueColorStateList)
                        passwordTextInputLayout.setEndIconTintList(darkBlueColorStateList)
                        passwordTextInputLayout.counterTextColor = darkBlueColorStateList
                        rememberMeCheckBox.buttonTintList = darkBlueColorStateList
                        signUpTextView.setTextColor(darkBlueColor)
                    }

                    8 -> {
                        changeStatusBarColor(activityContext, redColor)
                        val redColorStateList = ColorStateList.valueOf(redColor)
                        errorColorStateList = redColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(redColor)
                        signInTextView.setTextColor(redColor)
                        userNameTextInputLayout.setStartIconTintList(redColorStateList)
                        passwordTextInputLayout.setStartIconTintList(redColorStateList)
                        passwordTextInputLayout.setEndIconTintList(redColorStateList)
                        passwordTextInputLayout.counterTextColor = redColorStateList
                        rememberMeCheckBox.buttonTintList = redColorStateList
                        signUpTextView.setTextColor(redColor)
                    }

                    9 -> {
                        changeStatusBarColor(activityContext, lightPurpleColor)
                        val lightPurpleColorStateList = ColorStateList.valueOf(lightPurpleColor)
                        errorColorStateList = lightPurpleColorStateList
                        firstImageButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        secondImageButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        thirdImageButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        fourthImageButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        signInButton.setBackgroundColor(lightPurpleColor)
                        signInTextView.setTextColor(lightPurpleColor)
                        userNameTextInputLayout.setStartIconTintList(lightPurpleColorStateList)
                        passwordTextInputLayout.setStartIconTintList(lightPurpleColorStateList)
                        passwordTextInputLayout.setEndIconTintList(lightPurpleColorStateList)
                        passwordTextInputLayout.counterTextColor = lightPurpleColorStateList
                        rememberMeCheckBox.buttonTintList = lightPurpleColorStateList
                        signUpTextView.setTextColor(lightPurpleColor)
                    }
                }
            }
        }
    }
}