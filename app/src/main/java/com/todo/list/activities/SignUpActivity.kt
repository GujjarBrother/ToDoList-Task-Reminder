package com.todo.list.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
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
import com.todo.list.listeners.SignUpActivityCategorySelectionListener
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity
import es.dmoral.toasty.Toasty

class SignUpActivity : BaseActivity(), View.OnClickListener, SignUpActivityCategorySelectionListener {

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

        applyColorScheme(prefs.colorSchemeValue)
        signUpCardViewAnimation()
        makeFullScreenActivity(activityContext)
        keepActivityOn(activityContext)
        applyCustomFont()

//        Here, We Handle Click Listener's...
        with(binding) {
            genderSelectionLayout.setOnClickListener(this@SignUpActivity)
            securityQuestionsLayout.setOnClickListener(this@SignUpActivity)
            signUpButton.setOnClickListener(this@SignUpActivity)
            signInTextView.setOnClickListener(this@SignUpActivity)
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
            signUpTextView.typeface = typeface
            userNameTextInputLayout.typeface = typeface
            userNameTextInputEditText.typeface = typeface
            passwordTextInputLayout.typeface = typeface
            passwordTextInputEditText.typeface = typeface
            selectGenderTextView.typeface = typeface
            securityQuestionsTextView.typeface = typeface
            signUpButton.typeface = typeface
            alreadyHaveAnAccountTextView.typeface = typeface
            signInTextView.typeface = typeface
        }
    }

    private fun signUpCardViewAnimation() =
        binding.signUpCardView.startAnimation(AnimationUtils.loadAnimation(activityContext, R.anim.sign_in_and_sign_up_card_views_animation))

    private fun switchToSignInActivity() = startActivity(Intent(activityContext, SignInActivity::class.java))

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.genderSelectionLayout -> {
                    showCustomPopup(view, 1)
                }

                R.id.sign_up_button -> {
                    val emailOrUserName = userNameTextInputLayout.editText?.text.toString().trim()
                    val password = passwordTextInputLayout.editText?.text.toString().trim()
                    if (TextUtils.isEmpty(emailOrUserName)) {
                        passwordTextInputLayout.error = null
                        userNameTextInputLayout.boxStrokeErrorColor = errorColorStateList
                        userNameTextInputLayout.setErrorIconTintList(errorColorStateList)
                        userNameTextInputLayout.setErrorTextColor(errorColorStateList)
                        userNameTextInputLayout.error = getString(R.string.fill_this_field_text)
                    } else if (TextUtils.isEmpty(password)) {
                        userNameTextInputLayout.error = null
                        passwordTextInputLayout.boxStrokeErrorColor = errorColorStateList
                        passwordTextInputLayout.setErrorIconTintList(errorColorStateList)
                        passwordTextInputLayout.setErrorTextColor(errorColorStateList)
                        passwordTextInputLayout.error = getString(R.string.fill_this_field_text)
                    } else if (password.length > 10) {
                        userNameTextInputLayout.error = null
                        passwordTextInputLayout.error = null
                        passwordTextInputLayout.boxStrokeErrorColor = errorColorStateList
                        passwordTextInputLayout.setErrorIconTintList(errorColorStateList)
                        passwordTextInputLayout.setErrorTextColor(errorColorStateList)
                        passwordTextInputLayout.error = getString(R.string.password_is_too_long_text)
                    } else if (TextUtils.isEmpty(gender)) {
                        userNameTextInputLayout.error = null
                        passwordTextInputLayout.error = null
                        Toasty.error(activityContext, getString(R.string.select_gender_text), Toasty.LENGTH_LONG).show()
                    } else if (TextUtils.isEmpty(securityAnswer)) {
                        userNameTextInputLayout.error = null
                        passwordTextInputLayout.error = null
                        Toasty.error(activityContext, getString(R.string.select_security_question_text), Toasty.LENGTH_LONG).show()
                    } else {
                        userNameTextInputLayout.error = null
                        passwordTextInputLayout.error = null
                        userNameTextInputLayout.editText!!.text = null
                        passwordTextInputLayout.editText!!.text = null
                        prefs.saveUserCredentials(emailOrUserName, password, gender, securityQuestion, securityAnswer, true)
                        Toasty.success(activityContext, getString(R.string.sign_up_successfully_toast_message_text), Toasty.LENGTH_LONG).show()
                        switchToSignInActivity()
                        finish()
                    }
                }

                R.id.sign_in_text_view -> {
                    switchToSignInActivity()
                }

                R.id.security_questions_layout -> {
                    showSecurityQuestionsDialog()
                }
            }
        }
    }

    private fun showCustomPopup(view: View, fromWhereInvoked: Int) {
        val customPopupMenuLayoutBinding = CustomPopupMenuLayoutBinding.inflate(layoutInflater)

        if (prefs.dayAndNightModeSwitchValue) {
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
                add(0)
                add(1)
                add(2)
                add(3)
            }
            val categoryAdapter = CategoryAdapter(this, "Gender")
            with(customPopupMenuLayoutBinding) {
                customPopUpMenuRecyclerView.adapter = categoryAdapter
            }
            categoryAdapter.submitList(genderArrayList)
        } else if (fromWhereInvoked == 2) {
            val securityQuestionsArrayList = ArrayList<Int>()
            with(securityQuestionsArrayList) {
                add(0)
                add(1)
                add(2)
                add(3)
                add(4)
            }
            val categoryAdapter = CategoryAdapter(this, "Security Questions")
            with(customPopupMenuLayoutBinding) {
                customPopUpMenuRecyclerView.adapter = categoryAdapter
            }
            categoryAdapter.submitList(securityQuestionsArrayList)
        }
        popupWindow.showAsDropDown(view)
    }

    private fun showSecurityQuestionsDialog() {
        securityQuestionDialogLayoutBinding = SecurityQuestionDialogLayoutBinding.inflate(layoutInflater)

        val securityQuestionDialogBuilder = AlertDialog.Builder(activityContext)
        securityQuestionDialogBuilder.setView(securityQuestionDialogLayoutBinding.root)
        securityQuestionDialogBuilder.setCancelable(false)
        val securityQuestionAlertDialog = securityQuestionDialogBuilder.create()

        if (!activityContext.isFinishing && !activityContext.isDestroyed && !securityQuestionAlertDialog.isShowing) {
            val window = securityQuestionAlertDialog.window
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            securityQuestionAlertDialog.show()
        }

        with(securityQuestionDialogLayoutBinding) {
            applyCustomFontOnSecurityQuestionDialogViews(this)
            applyColorSchemeOnSecurityQuestionsDialogViews(this, prefs.colorSchemeValue)

            securityQuestionLayout.setOnClickListener { view: View ->
                showCustomPopup(view, 2)
            }

            dismissDialogImageView.setOnClickListener { _: View? ->
                hideSoftKeyboard(securityQuestionAnswerTextInputEditText)
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    securityQuestionAlertDialog.dismiss()
                }
            }

            saveSecurityQuestionButton.setOnClickListener { _: View? ->
                val question = securityQuestionAnswerTextInputLayout.editText?.hint.toString().trim()
                val answer = securityQuestionAnswerTextInputLayout.editText?.text.toString().trim()
                if (TextUtils.isEmpty(answer)) {
                    securityQuestionAnswerTextInputLayout.boxStrokeErrorColor = errorColorStateList
                    securityQuestionAnswerTextInputLayout.setErrorIconTintList(errorColorStateList)
                    securityQuestionAnswerTextInputLayout.setErrorTextColor(errorColorStateList)
                    securityQuestionAnswerTextInputLayout.error = getString(R.string.please_enter_answer_here_error_text)
                } else {
                    securityQuestion = question
                    securityAnswer = answer
                    hideSoftKeyboard(securityQuestionAnswerTextInputEditText)
                    if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                        securityQuestionAlertDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun applyColorSchemeOnSecurityQuestionsDialogViews(
            securityQuestionDialogLayoutBinding: SecurityQuestionDialogLayoutBinding, color: Int
    ) {
        with(securityQuestionDialogLayoutBinding) {
            when (color) {
                0 -> {
                    dismissDialogImageView.setColorFilter(defaultColor)
                    securityQuestionTitleTextView.setTextColor(defaultColor)
                    dropDownImageView.setColorFilter(defaultColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(defaultColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                }

                1 -> {
                    dismissDialogImageView.setColorFilter(darkYellowColor)
                    securityQuestionTitleTextView.setTextColor(darkYellowColor)
                    dropDownImageView.setColorFilter(darkYellowColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(darkYellowColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                }

                2 -> {
                    dismissDialogImageView.setColorFilter(orangeColor)
                    securityQuestionTitleTextView.setTextColor(orangeColor)
                    dropDownImageView.setColorFilter(orangeColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(orangeColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                }

                3 -> {
                    dismissDialogImageView.setColorFilter(lightGreenColor)
                    securityQuestionTitleTextView.setTextColor(lightGreenColor)
                    dropDownImageView.setColorFilter(lightGreenColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(lightGreenColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                }

                4 -> {
                    dismissDialogImageView.setColorFilter(blueColor)
                    securityQuestionTitleTextView.setTextColor(blueColor)
                    dropDownImageView.setColorFilter(blueColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(blueColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                }

                5 -> {
                    dismissDialogImageView.setColorFilter(cyanColor)
                    securityQuestionTitleTextView.setTextColor(cyanColor)
                    dropDownImageView.setColorFilter(cyanColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(cyanColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                }

                6 -> {
                    dismissDialogImageView.setColorFilter(pinkColor)
                    securityQuestionTitleTextView.setTextColor(pinkColor)
                    dropDownImageView.setColorFilter(pinkColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(pinkColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                }

                7 -> {
                    dismissDialogImageView.setColorFilter(darkBlueColor)
                    securityQuestionTitleTextView.setTextColor(darkBlueColor)
                    dropDownImageView.setColorFilter(darkBlueColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(darkBlueColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                }

                8 -> {
                    dismissDialogImageView.setColorFilter(redColor)
                    securityQuestionTitleTextView.setTextColor(redColor)
                    dropDownImageView.setColorFilter(redColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(redColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                }

                9 -> {
                    dismissDialogImageView.setColorFilter(lightPurpleColor)
                    securityQuestionTitleTextView.setTextColor(lightPurpleColor)
                    dropDownImageView.setColorFilter(lightPurpleColor)
                    securityQuestionAnswerTextInputLayout.setStartIconTintList(
                        ColorStateList.valueOf(lightPurpleColor))
                    saveSecurityQuestionButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                }
            }
        }
    }

    private fun applyCustomFontOnSecurityQuestionDialogViews(
            securityQuestionDialogLayoutBinding: SecurityQuestionDialogLayoutBinding
    ) {
        with(securityQuestionDialogLayoutBinding) {
            securityQuestionTitleTextView.typeface = typeface
            selectSecurityQuestionTextView.typeface = typeface
            securityQuestionAnswerTextInputLayout.typeface = typeface
            securityQuestionAnswerTextInputEditText.typeface = typeface
            saveSecurityQuestionButton.typeface = typeface
        }
    }

    private fun applyColorScheme(color: Int) {
        with(binding) {
            when (color) {
                0 -> {
                    changeStatusBarColor(activityContext, defaultColor)
                    val defaultColorStateList = ColorStateList.valueOf(defaultColor)
                    errorColorStateList = defaultColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(defaultColor)
                    userNameTextInputLayout.setStartIconTintList(defaultColorStateList)
                    passwordTextInputLayout.setStartIconTintList(defaultColorStateList)
                    passwordTextInputLayout.setEndIconTintList(defaultColorStateList)
                    passwordTextInputLayout.counterTextColor = defaultColorStateList
                    dropDownImageView.setColorFilter(defaultColor)
                    signInTextView.setTextColor(defaultColor)
                    asterikTextView.setTextColor(defaultColor)
                }

                1 -> {
                    changeStatusBarColor(activityContext, darkYellowColor)
                    val darkYellowColorStateList = ColorStateList.valueOf(darkYellowColor)
                    errorColorStateList = darkYellowColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(darkYellowColor)
                    userNameTextInputLayout.setStartIconTintList(darkYellowColorStateList)
                    passwordTextInputLayout.setStartIconTintList(darkYellowColorStateList)
                    passwordTextInputLayout.setEndIconTintList(darkYellowColorStateList)
                    passwordTextInputLayout.counterTextColor = darkYellowColorStateList
                    dropDownImageView.setColorFilter(darkYellowColor)
                    signInTextView.setTextColor(darkYellowColor)
                    asterikTextView.setTextColor(darkYellowColor)
                }

                2 -> {
                    changeStatusBarColor(activityContext, orangeColor)
                    val orangeColorStateList = ColorStateList.valueOf(orangeColor)
                    errorColorStateList = orangeColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(orangeColor)
                    userNameTextInputLayout.setStartIconTintList(orangeColorStateList)
                    passwordTextInputLayout.setStartIconTintList(orangeColorStateList)
                    passwordTextInputLayout.setEndIconTintList(orangeColorStateList)
                    passwordTextInputLayout.counterTextColor = orangeColorStateList
                    dropDownImageView.setColorFilter(orangeColor)
                    signInTextView.setTextColor(orangeColor)
                    asterikTextView.setTextColor(orangeColor)
                }

                3 -> {
                    changeStatusBarColor(activityContext, lightGreenColor)
                    val lightGreenColorStateList = ColorStateList.valueOf(lightGreenColor)
                    errorColorStateList = lightGreenColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(lightGreenColor)
                    userNameTextInputLayout.setStartIconTintList(lightGreenColorStateList)
                    passwordTextInputLayout.setStartIconTintList(lightGreenColorStateList)
                    passwordTextInputLayout.setEndIconTintList(lightGreenColorStateList)
                    passwordTextInputLayout.counterTextColor = lightGreenColorStateList
                    dropDownImageView.setColorFilter(lightGreenColor)
                    signInTextView.setTextColor(lightGreenColor)
                    asterikTextView.setTextColor(lightGreenColor)
                }

                4 -> {
                    changeStatusBarColor(activityContext, blueColor)
                    val blueColorStateList = ColorStateList.valueOf(blueColor)
                    errorColorStateList = blueColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(blueColor)
                    userNameTextInputLayout.setStartIconTintList(blueColorStateList)
                    passwordTextInputLayout.setStartIconTintList(blueColorStateList)
                    passwordTextInputLayout.setEndIconTintList(blueColorStateList)
                    passwordTextInputLayout.counterTextColor = blueColorStateList
                    dropDownImageView.setColorFilter(blueColor)
                    signInTextView.setTextColor(blueColor)
                    asterikTextView.setTextColor(blueColor)
                }

                5 -> {
                    changeStatusBarColor(activityContext, cyanColor)
                    val cyanColorStateList = ColorStateList.valueOf(cyanColor)
                    errorColorStateList = cyanColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(cyanColor)
                    userNameTextInputLayout.setStartIconTintList(cyanColorStateList)
                    passwordTextInputLayout.setStartIconTintList(cyanColorStateList)
                    passwordTextInputLayout.setEndIconTintList(cyanColorStateList)
                    passwordTextInputLayout.counterTextColor = cyanColorStateList
                    dropDownImageView.setColorFilter(cyanColor)
                    signInTextView.setTextColor(cyanColor)
                    asterikTextView.setTextColor(cyanColor)
                }

                6 -> {
                    changeStatusBarColor(activityContext, pinkColor)
                    val pinkColorStateList = ColorStateList.valueOf(pinkColor)
                    errorColorStateList = pinkColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(pinkColor)
                    userNameTextInputLayout.setStartIconTintList(pinkColorStateList)
                    passwordTextInputLayout.setStartIconTintList(pinkColorStateList)
                    passwordTextInputLayout.setEndIconTintList(pinkColorStateList)
                    passwordTextInputLayout.counterTextColor = pinkColorStateList
                    dropDownImageView.setColorFilter(pinkColor)
                    signInTextView.setTextColor(pinkColor)
                    asterikTextView.setTextColor(pinkColor)
                }

                7 -> {
                    changeStatusBarColor(activityContext, darkBlueColor)
                    val darkBlueColorStateList = ColorStateList.valueOf(darkBlueColor)
                    errorColorStateList = darkBlueColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(darkBlueColor)
                    userNameTextInputLayout.setStartIconTintList(darkBlueColorStateList)
                    passwordTextInputLayout.setStartIconTintList(darkBlueColorStateList)
                    passwordTextInputLayout.setEndIconTintList(darkBlueColorStateList)
                    passwordTextInputLayout.counterTextColor = darkBlueColorStateList
                    dropDownImageView.setColorFilter(darkBlueColor)
                    signInTextView.setTextColor(darkBlueColor)
                    asterikTextView.setTextColor(darkBlueColor)
                }

                8 -> {
                    changeStatusBarColor(activityContext, redColor)
                    val redColorStateList = ColorStateList.valueOf(redColor)
                    errorColorStateList = redColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(redColor)
                    userNameTextInputLayout.setStartIconTintList(redColorStateList)
                    passwordTextInputLayout.setStartIconTintList(redColorStateList)
                    passwordTextInputLayout.setEndIconTintList(redColorStateList)
                    passwordTextInputLayout.counterTextColor = redColorStateList
                    dropDownImageView.setColorFilter(redColor)
                    signInTextView.setTextColor(redColor)
                    asterikTextView.setTextColor(redColor)
                }

                9 -> {
                    changeStatusBarColor(activityContext, lightPurpleColor)
                    val lightPurpleColorStateList = ColorStateList.valueOf(lightPurpleColor)
                    errorColorStateList = lightPurpleColorStateList
                    firstImageButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    secondImageButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    thirdImageButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    fourthImageButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    signUpButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    signUpTextView.setTextColor(lightPurpleColor)
                    userNameTextInputLayout.setStartIconTintList(lightPurpleColorStateList)
                    passwordTextInputLayout.setStartIconTintList(lightPurpleColorStateList)
                    passwordTextInputLayout.setEndIconTintList(lightPurpleColorStateList)
                    passwordTextInputLayout.counterTextColor = lightPurpleColorStateList
                    dropDownImageView.setColorFilter(lightPurpleColor)
                    signInTextView.setTextColor(lightPurpleColor)
                    asterikTextView.setTextColor(lightPurpleColor)
                }
            }
        }
    }

    override fun selectCategory(category: Int, forWhichInvoked: String) {
        if (forWhichInvoked.equals("Gender", ignoreCase = true)) {
            gender = ""
            when (category) {
                1 -> {
                    gender = getString(R.string.male_text)
                }

                2 -> {
                    gender = getString(R.string.fe_male_text)
                }

                3 -> {
                    gender = getString(R.string.transgender_text)
                }
            }

            with(binding) {
                when (category) {
                    0 -> {
                        selectGenderTextView.text = activityContext.getString(R.string.select_gender_text)
                        selectGenderTextView.setTextColor(Color.parseColor("#9E9E9E"))
                    }

                    1 -> {
                        selectGenderTextView.text = activityContext.getString(R.string.male_text)
                        selectGenderTextView.setTextColor(blackColor)
                    }

                    2 -> {
                        selectGenderTextView.text = activityContext.getString(R.string.fe_male_text)
                        selectGenderTextView.setTextColor(blackColor)
                    }

                    3 -> {
                        selectGenderTextView.text = activityContext.getString(R.string.transgender_text)
                        selectGenderTextView.setTextColor(blackColor)
                    }
                }
            }
        } else if (forWhichInvoked.equals("Security Questions", ignoreCase = true)) {
            if (category != 0) {
                with(securityQuestionDialogLayoutBinding) {
                    securityQuestionLayout.visibility = View.GONE
                    group1.visibility = View.VISIBLE
                    when (category) {
                        1 -> {
                            securityQuestionAnswerTextInputLayout.hint =
                                getString(R.string.what_is_your_favourite_book_question)
                        }

                        2 -> {
                            securityQuestionAnswerTextInputLayout.hint =
                                getString(R.string.what_is_your_favourite_teacher_name_question)
                        }

                        3 -> {
                            securityQuestionAnswerTextInputLayout.hint =
                                getString(R.string.what_is_your_school_name_question)
                        }

                        4 -> {
                            securityQuestionAnswerTextInputLayout.hint =
                                getString(R.string.what_is_your_favourite_game_question)
                        }
                    }
                    showSoftKeyboard()
                    securityQuestionAnswerTextInputEditText.requestFocus()
                }
            }
        }
        if (popupWindow.isShowing) {
            popupWindow.dismiss()
        }
    }
}