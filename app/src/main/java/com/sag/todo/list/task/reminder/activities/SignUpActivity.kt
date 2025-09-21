package com.sag.todo.list.task.reminder.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.adapters.CategoryAdapter
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivitySignUpBinding
import com.sag.todo.list.task.reminder.databinding.CustomPopupMenuLayoutBinding
import com.sag.todo.list.task.reminder.databinding.SecurityQuestionDialogLayoutBinding
import com.sag.todo.list.task.reminder.enums.Gender
import com.sag.todo.list.task.reminder.enums.SecurityQuestions
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeVisibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.keepActivityOn
import com.sag.todo.list.task.reminder.utils.SignInAndSignUpCardViewsAnimation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    @Inject
    @SignInAndSignUpCardViewsAnimation
    lateinit var animation: Animation
    private var selectedGender = 0
    private var selectedSecurityQuestion = 0
    private var securityAnswer = ""
    private lateinit var popupWindow: PopupWindow
    private lateinit var securityQuestionDialogLayoutBinding: SecurityQuestionDialogLayoutBinding

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

        keepActivityOn(activityContext)

        with(binding) {
            signUpCV.startAnimation(animation)

            if (prefs.isDarkModeEnable) {
                userNameTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
                passwordTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
            }

            userNameTIET.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    genderSelectionBtn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(activityContext, R.color.subColor))
                }
            }

            passwordTIET.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    genderSelectionBtn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(activityContext, R.color.subColor))
                }
            }

            genderSelectionBtn.setOnClickListener(this@SignUpActivity)
            securityQuestionsTV.setOnClickListener(this@SignUpActivity)
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

    private fun switchToSignInActivity() = startActivity(Intent(activityContext, SignInActivity::class.java))

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.genderSelectionBtn -> {
                    userNameTIET.clearFocus()
                    passwordTIET.clearFocus()
                    genderSelectionBtn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(activityContext, R.color.defaultColor))
                    showCustomPopup(view, 1)
                }

                R.id.signUpButton -> {
                    val emailOrUserName = userNameTIL.editText?.text.toString().trim()
                    val password = passwordTIL.editText?.text.toString().trim()
                    if (emailOrUserName.isEmpty()) {
                        passwordTIL.error = null
                        userNameTIL.error = getString(R.string.fill_this_field_text)
                    } else if (password.isEmpty()) {
                        userNameTIL.error = null
                        passwordTIL.error = getString(R.string.fill_this_field_text)
                    } else if (password.length > 10) {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        passwordTIL.error = getString(R.string.password_is_too_long_text)
                    } else if (selectedGender == 0) {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        toastController.showToast(activityContext, getString(R.string.select_gender_text), false)
                    } else if (securityAnswer.isEmpty()) {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        toastController.showToast(activityContext, getString(R.string.select_security_question_text), false)
                    } else {
                        userNameTIL.error = null
                        passwordTIL.error = null
                        userNameTIL.editText!!.text = null
                        passwordTIL.editText!!.text = null
                        prefs.saveUserCredentials(
                            emailOrUserName, password, selectedGender, selectedSecurityQuestion, securityAnswer, true
                        )
                        prefs.rememberMe = false
                        toastController.showToast(activityContext, getString(R.string.sign_up_successfully_toast_message_text), true)
                        switchToSignInActivity()
                        finish()
                    }
                }
                R.id.signInTV -> switchToSignInActivity()
                R.id.securityQuestionsTV -> showSecurityQuestionsDialog()
            }
        }
    }

    private fun showCustomPopup(view: View, fromWhereInvoked: Int) {
        val customPopupMenuLayoutBinding = CustomPopupMenuLayoutBinding.inflate(layoutInflater)
        popupWindow = PopupWindow(
            customPopupMenuLayoutBinding.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 5F
        if (fromWhereInvoked == 1) {
            val genderArrayList = ArrayList<Int>()
            with(genderArrayList) {
                add(Gender.NONE.ordinal)
                add(Gender.MALE.ordinal)
                add(Gender.FEMALE.ordinal)
                add(Gender.TRANSGENDER.ordinal)
            }
            val categoryAdapter = CategoryAdapter("Gender", prefs) { category, forWhichInvoked ->
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
            val categoryAdapter = CategoryAdapter("Security Questions", prefs) { category, forWhichInvoked ->
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
            with(securityQuestionAlertDialog) {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                window?.setWindowAnimations(R.style.dialogBoxesAnimation)
                show()
            }
        }

        with(securityQuestionDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                securityQuestionAnswerTIL.setBoxStrokeColorStateList(
                    textInputLayoutDarkModeStrokeColor
                )
            }

            securityQuestionBtn.setOnClickListener { view: View ->
                securityQuestionBtn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(activityContext, R.color.defaultColor))
                showCustomPopup(view, 2)
            }

            dismissDialogIV.setOnClickListener { _: View? ->
                softKeyboardVisibilityController.hideSoftKeyboard(securityQuestionAnswerTIET)
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    securityQuestionAlertDialog.dismiss()
                }
            }

            saveButton.setOnClickListener { _: View? ->
                val answer = securityQuestionAnswerTIL.editText?.text.toString().trim()
                if (answer.isNotEmpty()) {
                    securityAnswer = answer
                    softKeyboardVisibilityController.hideSoftKeyboard(securityQuestionAnswerTIET)
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
            selectedGender = category
            with(binding) {
                when (category) {
                    Gender.NONE.ordinal -> {
                        genderSelectionBtn.text = getString(R.string.select_gender_text)
                        genderSelectionBtn.setTextColor(ContextCompat.getColor(activityContext, R.color.subColor))
                    }

                    Gender.MALE.ordinal -> {
                        genderSelectionBtn.setTextColor(
                            ContextCompat.getColor(
                                activityContext, R.color.blackAndWhiteViewsColor
                            )
                        )
                        genderSelectionBtn.text = getString(R.string.male_text)
                    }

                    Gender.FEMALE.ordinal -> {
                        genderSelectionBtn.setTextColor(
                            ContextCompat.getColor(
                                activityContext, R.color.blackAndWhiteViewsColor
                            )
                        )
                        genderSelectionBtn.text = getString(R.string.fe_male_text)
                    }

                    Gender.TRANSGENDER.ordinal -> {
                        genderSelectionBtn.setTextColor(
                            ContextCompat.getColor(
                                activityContext, R.color.blackAndWhiteViewsColor
                            )
                        )
                        genderSelectionBtn.text = getString(R.string.transgender_text)
                    }
                }
            }
        } else if (forWhichInvoked.equals(other = "Security Questions", ignoreCase = true)) {
            with(securityQuestionDialogLayoutBinding) {
                if (category != 0) {
                    securityQuestionBtn.changeVisibility(Visibility.GONE.ordinal)
                    group1.changeVisibility(Visibility.VISIBLE.ordinal)
                    selectedSecurityQuestion = category
                    when (category) {
                        SecurityQuestions.QUESTION_1.ordinal -> securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_book_question)
                        SecurityQuestions.QUESTION_2.ordinal -> securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_teacher_name_question)
                        SecurityQuestions.QUESTION_3.ordinal -> securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_school_name_question)
                        SecurityQuestions.QUESTION_4.ordinal -> securityQuestionAnswerTIL.hint = getString(R.string.what_is_your_favourite_game_question)
                    }
                    softKeyboardVisibilityController.showSoftKeyboard()
                    securityQuestionAnswerTIET.requestFocus()
                } else {
                }
            }
        }
        if (popupWindow.isShowing) {
            popupWindow.dismiss()
        }
    }
}