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
import com.todo.list.enums.Gender
import com.todo.list.enums.SecurityQuestions
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

class SignUpActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignUpBinding
    private var gender = ""
    private lateinit var securityQuestion: String
    private var securityAnswer = ""
    private lateinit var errorColorStateList: ColorStateList
    private lateinit var popupWindow: PopupWindow
    private lateinit var securityQuestionDialogLayoutBinding: SecurityQuestionDialogLayoutBinding
    private lateinit var selectedColors: SelectedColors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedColors = getSelectedColor(context = activityContext, prefs = prefs)

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
            customPopupMenuLayoutBinding.root.setCardBackgroundColor(getContextCompatColor(activityContext, screensNightModeColor))
        } else {
            if (fromWhereInvoked == 1) {
                customPopupMenuLayoutBinding.root.setCardBackgroundColor(getContextCompatColor(activityContext, whiteColor))
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
                add(Gender.NONE.ordinal)
                add(Gender.MALE.ordinal)
                add(Gender.FEMALE.ordinal)
                add(Gender.TRANSGENDER.ordinal)
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
                add(SecurityQuestions.SELECT_SECURITY_QUESTION.ordinal)
                add(SecurityQuestions.QUESTION_1.ordinal)
                add(SecurityQuestions.QUESTION_2.ordinal)
                add(SecurityQuestions.QUESTION_3.ordinal)
                add(SecurityQuestions.QUESTION_4.ordinal)
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
                rootLayout.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, screensNightModeColor), PorterDuff.Mode.SRC_IN)
                dismissDialogIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                securityQuestionTitleTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                securityQuestionLayout.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, darkModeTextColor), PorterDuff.Mode.SRC_IN)
                selectSecurityQuestionTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                dropDownIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                }

                securityQuestionAnswerTIL.boxStrokeColor = getContextCompatColor(activityContext, whiteColor)
                securityQuestionAnswerTIL.hintTextColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                securityQuestionAnswerTIL.editText?.setTextColor(getContextCompatColor(activityContext, whiteColor))
                securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor)))
                securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor)))
                securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor)))
                saveButton.setBackgroundColor(getContextCompatColor(activityContext, lightBlueColor))
                saveButton.setTextColor(getContextCompatColor(activityContext, blackColor))
            } else {
                dismissDialogIV.setColorFilter(selectedColors.originalColor)
                securityQuestionTitleTV.setTextColor(selectedColors.originalColor)
                dropDownIV.setColorFilter(selectedColors.originalColor)
                securityQuestionAnswerTIL.setStartIconTintList(ColorStateList.valueOf(selectedColors.originalColor))
                securityQuestionAnswerTIL.boxStrokeErrorColor = ColorStateList.valueOf(selectedColors.originalColor)
                securityQuestionAnswerTIL.setErrorIconTintList(ColorStateList.valueOf(selectedColors.originalColor))
                securityQuestionAnswerTIL.setErrorTextColor(ColorStateList.valueOf(selectedColors.originalColor))
                saveButton.setBackgroundColor(selectedColors.originalColor)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    securityQuestionAnswerTIL.cursorColor = ColorStateList.valueOf(selectedColors.originalColor)
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
                changeStatusBarColor(activityContext, getContextCompatColor(activityContext, screensNightModeColor))
                rootLayout.setBackgroundColor(getContextCompatColor(activityContext, screensNightModeColor))
                firstIB.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, cardsNightModeColor), PorterDuff.Mode.SRC_IN)
                secondIB.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, cardsNightModeColor), PorterDuff.Mode.SRC_IN)
                thirdIB.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, cardsNightModeColor), PorterDuff.Mode.SRC_IN)
                fourthIB.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, cardsNightModeColor), PorterDuff.Mode.SRC_IN)
                signUpTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                signUpCV.setCardBackgroundColor(getContextCompatColor(activityContext, cardsNightModeColor))

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

                genderSelectionLayout.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, darkModeTextColor), PorterDuff.Mode.SRC_IN)
                selectGenderTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                dropDownIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    userNameTIL.cursorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                    passwordTIL.cursorColor = ColorStateList.valueOf(getContextCompatColor(activityContext, whiteColor))
                }

                securityQuestionsTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                asterikTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                signUpButton.setBackgroundColor(getContextCompatColor(activityContext, lightBlueColor))
                signUpButton.setTextColor(getContextCompatColor(activityContext, blackColor))
                alreadyHaveAnAccountTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                signInTV.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
            } else {
                changeStatusBarColor(activityContext, selectedColors.originalColor)
                val defaultColorStateList = ColorStateList.valueOf(selectedColors.originalColor)
                errorColorStateList = defaultColorStateList
                firstIB.background.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                secondIB.background.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                thirdIB.background.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                fourthIB.background.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                signUpTV.setTextColor(selectedColors.originalColor)
                dropDownIV.setColorFilter(selectedColors.originalColor)
                signInTV.setTextColor(selectedColors.originalColor)
                asterikTV.setTextColor(selectedColors.originalColor)
                signUpButton.setBackgroundColor(selectedColors.originalColor)

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

    private fun checkGenderAndSecurityQuestion(forWhichInvoked: String?, category: Int) {
        if (forWhichInvoked.equals(other = "Gender", ignoreCase = true)) {
            gender = ""
            when (category) {
                Gender.MALE.ordinal -> {
                    gender = getString(R.string.male_text)
                }

                Gender.FEMALE.ordinal -> {
                    gender = getString(R.string.fe_male_text)
                }

                Gender.TRANSGENDER.ordinal -> {
                    gender = getString(R.string.transgender_text)
                }
            }

            with(binding) {
                if (prefs.isDarkModeEnable) {
                    selectGenderTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                } else {
                    selectGenderTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                }
                when (category) {
                    Gender.NONE.ordinal -> {
                        selectGenderTV.text =
                            activityContext.getString(R.string.select_gender_text)
                        selectGenderTV.setTextColor(Color.parseColor("#9E9E9E"))
                    }

                    Gender.MALE.ordinal -> {
                        selectGenderTV.text = getString(R.string.male_text)
                    }

                    Gender.FEMALE.ordinal -> {
                        selectGenderTV.text = getString(R.string.fe_male_text)
                    }

                    Gender.TRANSGENDER.ordinal -> {
                        selectGenderTV.text = getString(R.string.transgender_text)
                    }
                }
            }
        } else if (forWhichInvoked.equals(other = "Security Questions", ignoreCase = true)) {
            if (category != 0) {
                with(securityQuestionDialogLayoutBinding) {
                    securityQuestionLayout.changeVisibility(Visibility.GONE.ordinal)
                    group1.changeVisibility(Visibility.VISIBLE.ordinal)
                    when (category) {
                        SecurityQuestions.QUESTION_1.ordinal -> {
                            securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_book_question)
                        }

                        SecurityQuestions.QUESTION_2.ordinal -> {
                            securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_teacher_name_question)
                        }

                        SecurityQuestions.QUESTION_3.ordinal -> {
                            securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_school_name_question)
                        }

                        SecurityQuestions.QUESTION_4.ordinal -> {
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