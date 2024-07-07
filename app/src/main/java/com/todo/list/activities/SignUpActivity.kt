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
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.todo.list.R
import com.todo.list.adapters.CategoryAdapter
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivitySignUpBinding
import com.todo.list.databinding.CustomPopupMenuLayoutBinding
import com.todo.list.databinding.SecurityQuestionDialogLayoutBinding
import com.todo.list.enums.GenderEnum
import com.todo.list.enums.SecurityQuestionsEnum
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity
import es.dmoral.toasty.Toasty

class SignUpActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignUpBinding
    private var gender = ""
    private lateinit var securityQuestion: String
    private var securityAnswer = ""
    private lateinit var errorColorStateList: ColorStateList
    private lateinit var popupWindow: PopupWindow
    private lateinit var securityQuestionDialogLayoutBinding: SecurityQuestionDialogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyLightAndDarkMode()
        signUpCardViewAnimation()
        makeFullScreenActivity(activityContext)
        keepActivityOn(activityContext)
        applyCustomFont()

//        Here, We Handle Click Listener's...
        with(binding) {
            genderSelectionLayout.setOnClickListener(this@SignUpActivity)
            securityQuestionsLayout.setOnClickListener(this@SignUpActivity)
            signUpButton.setOnClickListener(this@SignUpActivity)
            signInTV.setOnClickListener(this@SignUpActivity)
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                switchToSignInActivity()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun applyCustomFont() {
        with(binding) {
            signUpTV.typeface = typeface
            userNameTIL.typeface = typeface
            userNameTIET.typeface = typeface
            passwordTIL.typeface = typeface
            passwordTIET.typeface = typeface
            selectGenderTV.typeface = typeface
            securityQuestionsTV.typeface = typeface
            signUpButton.typeface = typeface
            alreadyHaveAnAccountTV.typeface = typeface
            signInTV.typeface = typeface
        }
    }

    private fun signUpCardViewAnimation() =
        binding.signUpCV.startAnimation(AnimationUtils.loadAnimation(activityContext, R.anim.sign_in_and_sign_up_card_views_animation))

    private fun switchToSignInActivity() = startActivity(Intent(activityContext, SignInActivity::class.java))

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.genderSelectionLayout -> {
                    showCustomPopup(view, 1)
                }

                R.id.signUpButton -> {
                    val emailOrUserName = userNameTIL.editText?.text.toString().trim()
                    val password = passwordTIL.editText?.text.toString().trim()
                    if (TextUtils.isEmpty(emailOrUserName)) {
                        passwordTIL.error = null
                        userNameTIL.error = getString(R.string.fill_this_field_text)
                    } else if (TextUtils.isEmpty(password)) {
                        userNameTIL.error = null
                        passwordTIL.error = getString(R.string.fill_this_field_text)
                    } else if (password.length > 10) {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        passwordTIL.error = getString(R.string.password_is_too_long_text)
                    } else if (TextUtils.isEmpty(gender)) {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        Toasty.error(activityContext, getString(R.string.select_gender_text), Toasty.LENGTH_LONG).show()
                    } else if (TextUtils.isEmpty(securityAnswer)) {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        Toasty.error(activityContext, getString(R.string.select_security_question_text), Toasty.LENGTH_LONG).show()
                    } else {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        userNameTIL.editText!!.text = null
                        passwordTIL.editText!!.text = null
                        prefs.saveUserCredentials(emailOrUserName, password, gender, securityQuestion, securityAnswer, true)
                        prefs.rememberMe = false
                        Toasty.success(activityContext, getString(R.string.sign_up_successfully_toast_message_text), Toasty.LENGTH_LONG).show()
                        switchToSignInActivity()
                        finish()
                    }
                }

                R.id.signInTV -> {
                    switchToSignInActivity()
                }

                R.id.securityQuestionsLayout -> {
                    showSecurityQuestionsDialog()
                }
            }
        }
    }

    private fun showCustomPopup(view: View, fromWhereInvoked: Int) {
        val customPopupMenuLayoutBinding = CustomPopupMenuLayoutBinding.inflate(layoutInflater)

        if (prefs.isDarkModeEnable) {
            customPopupMenuLayoutBinding.root.setCardBackgroundColor(screensNightModeColor)
        } else {
            if (fromWhereInvoked == 1) {
                customPopupMenuLayoutBinding.root.setCardBackgroundColor(whiteColor)
            }
        }
        popupWindow = PopupWindow(
            customPopupMenuLayoutBinding.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 5f
        if (fromWhereInvoked == 1) {
            val genderArrayList = ArrayList<Int>()
            with(genderArrayList) {
                add(GenderEnum.NONE.ordinal)
                add(GenderEnum.MALE.ordinal)
                add(GenderEnum.FEMALE.ordinal)
                add(GenderEnum.TRANSGENDER.ordinal)
            }
            val categoryAdapter = CategoryAdapter("Gender") { category, forWhichInvoked ->
                checkGenderAndSecurityQuestion(forWhichInvoked, category)
            }
            with(customPopupMenuLayoutBinding) {
                customPopUpMenuRV.adapter = categoryAdapter
            }
            categoryAdapter.submitList(genderArrayList)
        } else if (fromWhereInvoked == 2) {
            val securityQuestionsArrayList = ArrayList<Int>()
            with(securityQuestionsArrayList) {
                add(SecurityQuestionsEnum.SELECT_SECURITY_QUESTION.ordinal)
                add(SecurityQuestionsEnum.QUESTION_1.ordinal)
                add(SecurityQuestionsEnum.QUESTION_2.ordinal)
                add(SecurityQuestionsEnum.QUESTION_3.ordinal)
                add(SecurityQuestionsEnum.QUESTION_4.ordinal)
            }
            val categoryAdapter = CategoryAdapter("Security Questions") { category, forWhichInvoked ->
                checkGenderAndSecurityQuestion(forWhichInvoked, category)
            }
            with(customPopupMenuLayoutBinding) {
                customPopUpMenuRV.adapter = categoryAdapter
            }
            categoryAdapter.submitList(securityQuestionsArrayList)
        }
        popupWindow.showAsDropDown(view)
    }

    private fun showSecurityQuestionsDialog() {
        securityQuestionDialogLayoutBinding = SecurityQuestionDialogLayoutBinding.inflate(layoutInflater)

        val securityQuestionDialogBuilder = AlertDialog.Builder(activityContext)
        with(securityQuestionDialogBuilder) {
            setView(securityQuestionDialogLayoutBinding.root)
            setCancelable(true)
        }
        val securityQuestionAlertDialog = securityQuestionDialogBuilder.create()
        if (!activityContext.isFinishing && !activityContext.isDestroyed && !securityQuestionAlertDialog.isShowing) {
            val window = securityQuestionAlertDialog.window
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            securityQuestionAlertDialog.show()
        }

        with(securityQuestionDialogLayoutBinding) {
            applyCustomFontOnSecurityQuestionDialogViews(this)
            applyLightAndDarkModeOnSecurityQuestionsDialogViews(this)

            securityQuestionLayout.setOnClickListener { view: View ->
                showCustomPopup(view, 2)
            }

            dismissDialogIV.setOnClickListener { _: View? ->
                hideSoftKeyboard(securityQuestionAnswerTIET)
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    securityQuestionAlertDialog.dismiss()
                }
            }

            saveButton.setOnClickListener { _: View? ->
                val question = securityQuestionAnswerTIL.editText?.hint.toString().trim()
                val answer = securityQuestionAnswerTIL.editText?.text.toString().trim()
                if (answer.isNotEmpty()) {
                    securityQuestion = question
                    securityAnswer = answer
                    hideSoftKeyboard(securityQuestionAnswerTIET)
                    if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                        securityQuestionAlertDialog.dismiss()
                    }
                } else {
                    securityQuestionAnswerTIL.error = getString(R.string.please_enter_answer_here_error_text)
                }
            }
        }
    }

    private fun applyLightAndDarkModeOnSecurityQuestionsDialogViews(
            securityQuestionDialogLayoutBinding: SecurityQuestionDialogLayoutBinding
    ) {
        with(securityQuestionDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                dismissDialogIV.setColorFilter(lightBlueColor)
                securityQuestionTitleTV.setTextColor(lightBlueColor)
                securityQuestionLayout.background.colorFilter = PorterDuffColorFilter(darkModeTextColor, PorterDuff.Mode.SRC_IN)
                selectSecurityQuestionTV.setTextColor(darkModeTextColor)
                dropDownIV.setColorFilter(lightBlueColor)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    securityQuestionAnswerTIL.cursorColor = whiteColorStateList
                }

                securityQuestionAnswerTIL.boxStrokeColor = whiteColor
                securityQuestionAnswerTIL.hintTextColor = whiteColorStateList
                securityQuestionAnswerTIL.editText?.setTextColor(whiteColor)
                securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                securityQuestionAnswerTIL.boxStrokeErrorColor = whiteColorStateList
                securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                securityQuestionAnswerTIL.setErrorTextColor(whiteColorStateList)
                saveButton.setBackgroundColor(lightBlueColor)
                saveButton.setTextColor(blackColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        dismissDialogIV.setColorFilter(defaultColor)
                        securityQuestionTitleTV.setTextColor(defaultColor)
                        dropDownIV.setColorFilter(defaultColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(defaultColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(defaultColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(defaultColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(defaultColor))
                        saveButton.setBackgroundColor(defaultColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(defaultColor)
                        }
                    }

                    1 -> {
                        dismissDialogIV.setColorFilter(darkYellowColor)
                        securityQuestionTitleTV.setTextColor(darkYellowColor)
                        dropDownIV.setColorFilter(darkYellowColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(darkYellowColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkYellowColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(darkYellowColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(darkYellowColor))
                        saveButton.setBackgroundColor(darkYellowColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(darkYellowColor)
                        }
                    }

                    2 -> {
                        dismissDialogIV.setColorFilter(orangeColor)
                        securityQuestionTitleTV.setTextColor(orangeColor)
                        dropDownIV.setColorFilter(orangeColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(orangeColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(orangeColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(orangeColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(orangeColor))
                        saveButton.setBackgroundColor(orangeColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(orangeColor)
                        }
                    }

                    3 -> {
                        dismissDialogIV.setColorFilter(lightGreenColor)
                        securityQuestionTitleTV.setTextColor(lightGreenColor)
                        dropDownIV.setColorFilter(lightGreenColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(lightGreenColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightGreenColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(lightGreenColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(lightGreenColor))
                        saveButton.setBackgroundColor(lightGreenColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(lightGreenColor)
                        }
                    }

                    4 -> {
                        dismissDialogIV.setColorFilter(blueColor)
                        securityQuestionTitleTV.setTextColor(blueColor)
                        dropDownIV.setColorFilter(blueColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(blueColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(blueColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(blueColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(blueColor))
                        saveButton.setBackgroundColor(blueColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(blueColor)
                        }
                    }

                    5 -> {
                        dismissDialogIV.setColorFilter(cyanColor)
                        securityQuestionTitleTV.setTextColor(cyanColor)
                        dropDownIV.setColorFilter(cyanColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(cyanColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(cyanColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(cyanColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(cyanColor))
                        saveButton.setBackgroundColor(cyanColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(cyanColor)
                        }
                    }

                    6 -> {
                        dismissDialogIV.setColorFilter(pinkColor)
                        securityQuestionTitleTV.setTextColor(pinkColor)
                        dropDownIV.setColorFilter(pinkColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(pinkColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(pinkColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(pinkColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(pinkColor))
                        saveButton.setBackgroundColor(pinkColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(pinkColor)
                        }
                    }

                    7 -> {
                        dismissDialogIV.setColorFilter(darkBlueColor)
                        securityQuestionTitleTV.setTextColor(darkBlueColor)
                        dropDownIV.setColorFilter(darkBlueColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(darkBlueColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkBlueColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(darkBlueColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(darkBlueColor))
                        saveButton.setBackgroundColor(darkBlueColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(darkBlueColor)
                        }
                    }

                    8 -> {
                        dismissDialogIV.setColorFilter(redColor)
                        securityQuestionTitleTV.setTextColor(redColor)
                        dropDownIV.setColorFilter(redColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(redColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(redColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(redColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(redColor))
                        saveButton.setBackgroundColor(redColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(redColor)
                        }
                    }

                    9 -> {
                        dismissDialogIV.setColorFilter(lightPurpleColor)
                        securityQuestionTitleTV.setTextColor(lightPurpleColor)
                        dropDownIV.setColorFilter(lightPurpleColor)
                        securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightPurpleColor)
                        securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(lightPurpleColor))
                        saveButton.setBackgroundColor(lightPurpleColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(lightPurpleColor)
                        }
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnSecurityQuestionDialogViews(
            securityQuestionDialogLayoutBinding: SecurityQuestionDialogLayoutBinding
    ) {
        with(securityQuestionDialogLayoutBinding) {
            securityQuestionTitleTV.typeface = typeface
            selectSecurityQuestionTV.typeface = typeface
            securityQuestionAnswerTIL.typeface = typeface
            securityQuestionAnswerTIET.typeface = typeface
            saveButton.typeface = typeface
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
                signUpTV.setTextColor(lightBlueColor)
                signUpCV.setCardBackgroundColor(cardsNightModeColor)

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

                genderSelectionLayout.background.colorFilter = PorterDuffColorFilter(darkModeTextColor, PorterDuff.Mode.SRC_IN)
                selectGenderTV.setTextColor(darkModeTextColor)
                dropDownIV.setColorFilter(lightBlueColor)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    userNameTIL.cursorColor = whiteColorStateList
                    passwordTIL.cursorColor = whiteColorStateList
                }

                securityQuestionsTV.setTextColor(darkModeTextColor)
                asterikTV.setTextColor(lightBlueColor)
                signUpButton.setBackgroundColor(lightBlueColor)
                signUpButton.setTextColor(blackColor)
                alreadyHaveAnAccountTV.setTextColor(darkModeTextColor)
                signInTV.setTextColor(lightBlueColor)
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
                        signUpTV.setTextColor(defaultColor)
                        dropDownIV.setColorFilter(defaultColor)
                        signInTV.setTextColor(defaultColor)
                        asterikTV.setTextColor(defaultColor)
                        signUpButton.setBackgroundColor(defaultColor)

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
                        signUpTV.setTextColor(darkYellowColor)
                        signUpButton.setBackgroundColor(darkYellowColor)
                        dropDownIV.setColorFilter(darkYellowColor)
                        signInTV.setTextColor(darkYellowColor)
                        asterikTV.setTextColor(darkYellowColor)

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
                        signUpTV.setTextColor(orangeColor)
                        signUpButton.setBackgroundColor(orangeColor)
                        dropDownIV.setColorFilter(orangeColor)
                        signInTV.setTextColor(orangeColor)
                        asterikTV.setTextColor(orangeColor)

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
                        signUpTV.setTextColor(lightGreenColor)
                        signUpButton.setBackgroundColor(lightGreenColor)
                        dropDownIV.setColorFilter(lightGreenColor)
                        signInTV.setTextColor(lightGreenColor)
                        asterikTV.setTextColor(lightGreenColor)

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
                        signUpTV.setTextColor(blueColor)
                        signUpButton.setBackgroundColor(blueColor)
                        dropDownIV.setColorFilter(blueColor)
                        signInTV.setTextColor(blueColor)
                        asterikTV.setTextColor(blueColor)

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
                        signUpTV.setTextColor(cyanColor)
                        signUpButton.setBackgroundColor(cyanColor)
                        dropDownIV.setColorFilter(cyanColor)
                        signInTV.setTextColor(cyanColor)
                        asterikTV.setTextColor(cyanColor)

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
                        signUpTV.setTextColor(pinkColor)
                        signUpButton.setBackgroundColor(pinkColor)
                        dropDownIV.setColorFilter(pinkColor)
                        signInTV.setTextColor(pinkColor)
                        asterikTV.setTextColor(pinkColor)

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
                        signUpTV.setTextColor(darkBlueColor)
                        signUpButton.setBackgroundColor(darkBlueColor)
                        dropDownIV.setColorFilter(darkBlueColor)
                        signInTV.setTextColor(darkBlueColor)
                        asterikTV.setTextColor(darkBlueColor)

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
                        signUpTV.setTextColor(redColor)
                        signUpButton.setBackgroundColor(redColor)
                        dropDownIV.setColorFilter(redColor)
                        signInTV.setTextColor(redColor)
                        asterikTV.setTextColor(redColor)

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
                        signUpTV.setTextColor(lightPurpleColor)
                        signUpButton.setBackgroundColor(lightPurpleColor)
                        dropDownIV.setColorFilter(lightPurpleColor)
                        signInTV.setTextColor(lightPurpleColor)
                        asterikTV.setTextColor(lightPurpleColor)

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

    private fun checkGenderAndSecurityQuestion(forWhichInvoked: String?, category: Int) {
        if (forWhichInvoked.equals(other = "Gender", ignoreCase = true)) {
            gender = ""
            when (category) {
                GenderEnum.MALE.ordinal -> {
                    gender = getString(R.string.male_text)
                }

                GenderEnum.FEMALE.ordinal -> {
                    gender = getString(R.string.fe_male_text)
                }

                GenderEnum.TRANSGENDER.ordinal -> {
                    gender = getString(R.string.transgender_text)
                }
            }

            with(binding) {
                if (prefs.isDarkModeEnable) {
                    selectGenderTV.setTextColor(whiteColor)
                } else {
                    selectGenderTV.setTextColor(blackColor)
                }
                when (category) {
                    GenderEnum.NONE.ordinal -> {
                        selectGenderTV.text =
                            activityContext.getString(R.string.select_gender_text)
                        selectGenderTV.setTextColor(Color.parseColor("#9E9E9E"))
                    }

                    GenderEnum.MALE.ordinal -> {
                        selectGenderTV.text = getString(R.string.male_text)
                    }

                    GenderEnum.FEMALE.ordinal -> {
                        selectGenderTV.text = getString(R.string.fe_male_text)
                    }

                    GenderEnum.TRANSGENDER.ordinal -> {
                        selectGenderTV.text = getString(R.string.transgender_text)
                    }
                }
            }
        } else if (forWhichInvoked.equals(other = "Security Questions", ignoreCase = true)) {
            if (category != 0) {
                with(securityQuestionDialogLayoutBinding) {
                    securityQuestionLayout.visibility = View.GONE
                    group1.visibility = View.VISIBLE
                    when (category) {
                        SecurityQuestionsEnum.QUESTION_1.ordinal -> {
                            securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_book_question)
                        }

                        SecurityQuestionsEnum.QUESTION_2.ordinal -> {
                            securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_teacher_name_question)
                        }

                        SecurityQuestionsEnum.QUESTION_3.ordinal -> {
                            securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_school_name_question)
                        }

                        SecurityQuestionsEnum.QUESTION_4.ordinal -> {
                            securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_game_question)
                        }
                    }
                    showSoftKeyboard()
                    securityQuestionAnswerTIET.requestFocus()
                }
            }
        }
        if (popupWindow.isShowing) {
            popupWindow.dismiss()
        }
    }
}