package com.todo.list.activities

import android.content.Intent
import android.graphics.Color
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
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivitySignUpBinding
import com.todo.list.databinding.CustomPopupMenuLayoutBinding
import com.todo.list.databinding.SecurityQuestionDialogLayoutBinding
import com.todo.list.enums.Gender
import com.todo.list.enums.SecurityQuestions
import com.todo.list.enums.Visibility
import com.todo.list.utils.ColorsUtils.blackColor
import com.todo.list.utils.ColorsUtils.getContextCompatColor
import com.todo.list.utils.ColorsUtils.whiteColor
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity
import es.dmoral.toasty.Toasty

class SignUpActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignUpBinding
    private var gender = ""
    private lateinit var securityQuestion: String
    private var securityAnswer = ""
    private lateinit var popupWindow: PopupWindow
    private lateinit var securityQuestionDialogLayoutBinding: SecurityQuestionDialogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeFullScreenActivity(activityContext)
        keepActivityOn(activityContext)

        with(binding) {
            signUpCardViewAnimation()
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

    private fun ActivitySignUpBinding.signUpCardViewAnimation() =
        signUpCV.startAnimation(AnimationUtils.loadAnimation(activityContext, R.anim.sign_in_and_sign_up_card_views_animation))

    private fun switchToSignInActivity() = startActivity(Intent(activityContext, SignInActivity::class.java))

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.genderSelectionLayout -> showCustomPopup(view, 1)
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
                R.id.signInTV -> switchToSignInActivity()
                R.id.securityQuestionsLayout -> showSecurityQuestionsDialog()
            }
        }
    }

    private fun showCustomPopup(view: View, fromWhereInvoked: Int) {
        val customPopupMenuLayoutBinding = CustomPopupMenuLayoutBinding.inflate(layoutInflater)
        /*if (prefs.isDarkModeEnable) {
            customPopupMenuLayoutBinding.root.setCardBackgroundColor(getContextCompatColor(activityContext, screensNightModeColor))
        } else {
            if (fromWhereInvoked == 1) {
                customPopupMenuLayoutBinding.root.setCardBackgroundColor(getContextCompatColor(activityContext, whiteColor))
            }
        }*/
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

    private fun checkGenderAndSecurityQuestion(forWhichInvoked: String?, category: Int) {
        if (forWhichInvoked.equals(other = "Gender", ignoreCase = true)) {
            gender = ""
            when (category) {
                Gender.MALE.ordinal -> gender = getString(R.string.male_text)
                Gender.FEMALE.ordinal -> gender = getString(R.string.fe_male_text)
                Gender.TRANSGENDER.ordinal -> gender = getString(R.string.transgender_text)
            }

            with(binding) {
                if (prefs.isDarkModeEnable) {
                    selectGenderTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                } else {
                    selectGenderTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                }
                when (category) {
                    Gender.NONE.ordinal -> {
                        selectGenderTV.text = activityContext.getString(R.string.select_gender_text)
                        selectGenderTV.setTextColor(Color.parseColor("#9E9E9E"))
                    }
                    Gender.MALE.ordinal -> selectGenderTV.text = getString(R.string.male_text)
                    Gender.FEMALE.ordinal -> selectGenderTV.text = getString(R.string.fe_male_text)
                    Gender.TRANSGENDER.ordinal -> selectGenderTV.text = getString(R.string.transgender_text)
                }
            }
        } else if (forWhichInvoked.equals(other = "Security Questions", ignoreCase = true)) {
            if (category != 0) {
                with(securityQuestionDialogLayoutBinding) {
                    securityQuestionLayout.changeVisibility(Visibility.GONE.ordinal)
                    group1.changeVisibility(Visibility.VISIBLE.ordinal)
                    when (category) {
                        SecurityQuestions.QUESTION_1.ordinal -> securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_book_question)
                        SecurityQuestions.QUESTION_2.ordinal -> securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_teacher_name_question)
                        SecurityQuestions.QUESTION_3.ordinal -> securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_school_name_question)
                        SecurityQuestions.QUESTION_4.ordinal -> securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_game_question)
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