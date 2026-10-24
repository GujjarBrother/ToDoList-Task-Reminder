package com.sag.todo.task.reminder.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.sag.todo.task.reminder.R
import com.sag.todo.task.reminder.core.R as CoreR
import com.sag.todo.task.reminder.adapters.AppLanguageRVAdapter
import com.sag.todo.task.reminder.base.BaseActivity
import com.sag.todo.task.reminder.databinding.ActivityAppLanguageBinding
import com.sag.todo.task.reminder.models.AppLanguage
import com.sag.todo.task.reminder.utils.FabRateUsAndApplyAnimation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppLanguageActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityAppLanguageBinding.inflate(layoutInflater)
    }

    @Inject
    @FabRateUsAndApplyAnimation
    lateinit var animation: Animation
    private var selectedAppLanguage: AppLanguage? = null
    private var isFromSplash = false
    private val appLanguageRVAdapter: AppLanguageRVAdapter by lazy {
        AppLanguageRVAdapter(
            clickCallback = { appLanguage ->
                selectedAppLanguage = appLanguage
                appLanguageRVAdapter.apply {
                    languagesList.forEach {
                        it.isSelected = it.languageCode == appLanguage.languageCode
                    }
                    submitList(
                        currentList.map {
                            it.copy(isSelected = it.languageCode == appLanguage.languageCode)
                        }
                    )
                }
            }
        )
    }
    private val languagesList by lazy {
        mutableListOf(
            AppLanguage(CoreR.drawable.urdu_flag, "Urdu", "اردو", "ur"),
            AppLanguage(CoreR.drawable.arabic_flag, "Arabic", "عربي", "ar"),
            AppLanguage(CoreR.drawable.english_flag, "English", "English", "en"),
            AppLanguage(CoreR.drawable.african_flag, "African", "Afrikaanse", "af"),
            AppLanguage(CoreR.drawable.bengali_flag, "Bengali", "বাংলা", "bn"),
            AppLanguage(CoreR.drawable.bhojpuri_flag, "Bhojpuri", "भोजपुरी", "bho"),
            AppLanguage(CoreR.drawable.bulgarian_flag, "Bulgarian", "български", "bg"),
            AppLanguage(CoreR.drawable.myanmar_burmese_flag, "Myanmar (Burmese)", "မြန်မာ (ဗမာ)၊", "my"),
            AppLanguage(CoreR.drawable.chinese_simplified_flag, "Chinese (Simplified)", "简体中文", "zh-CN"),
            AppLanguage(CoreR.drawable.chinese_traditional_flag, "Chinese (Traditional)", "繁體中文", "zh-TW"),
            AppLanguage(CoreR.drawable.czech_flag, "Czech", "čeština", "cs"),
            AppLanguage(CoreR.drawable.danish_flag, "Danish", "Dansk", "da"),
            AppLanguage(CoreR.drawable.dutch_flag, "Dutch", "Nederlands", "nl"),
            AppLanguage(CoreR.drawable.filipino_flag, "Filipino", "Filipino", "tl"),
            AppLanguage(CoreR.drawable.finnish_flag, "Finnish", "suomi", "fi"),
            AppLanguage(CoreR.drawable.french_flag, "French", "Français", "fr"),
            AppLanguage(CoreR.drawable.german_flag, "German", "Deutsch", "de"),
            AppLanguage(CoreR.drawable.greek_flag, "Greek", "ελληνικά", "el"),
            AppLanguage(CoreR.drawable.guarani_flag, "Guarani", "guarani", "gn"),
            AppLanguage(CoreR.drawable.hausa_flag, "Hausa", "Hausa", "ha"),
            AppLanguage(CoreR.drawable.hebrew_flag, "Hebrew", "עִברִית", "iw"),
            AppLanguage(CoreR.drawable.hindi_flag, "Hindi", "हिन्दी", "hi"),
            AppLanguage(CoreR.drawable.hungarian_flag, "Hungarian", "magyar", "hu"),
            AppLanguage(CoreR.drawable.indonesian_flag, "Indonesian", "Indonesia", "id"),
            AppLanguage(CoreR.drawable.italian_flag, "Italian", "Italiana", "it"),
            AppLanguage(CoreR.drawable.japanese_flag, "Japanese", "日本語", "ja"),
            AppLanguage(CoreR.drawable.korean_flag, "Korean", "한국인", "ko"),
            AppLanguage(CoreR.drawable.malay_flag, "Malay", "Melayu", "ms"),
            AppLanguage(CoreR.drawable.marathi_flag, "Marathi", "मराठी", "mr"),
            AppLanguage(CoreR.drawable.oromo_flag, "Oromo", "Afaan Oromoo", "om"),
            AppLanguage(CoreR.drawable.persian_flag, "Persian", "فارسی", "fa"),
            AppLanguage(CoreR.drawable.polish_flag, "Polish", "Polski", "pl"),
            AppLanguage(CoreR.drawable.portuguese_portugal, "Portuguese", "Português", "pt"),
            AppLanguage(CoreR.drawable.quechua_flag, "Quechua", "Runasimi", "qu"),
            AppLanguage(CoreR.drawable.romanian_flag, "Romanian", "Română", "ro"),
            AppLanguage(CoreR.drawable.russain_flag, "Russian", "Русский", "ru"),
            AppLanguage(CoreR.drawable.serbian_flag, "Serbian", "Српски", "sr"),
            AppLanguage(CoreR.drawable.spanish_flag, "Spanish", "Español", "es"),
            AppLanguage(CoreR.drawable.swahili_flag, "Swahili", "Kihispania", "sw"),
            AppLanguage(CoreR.drawable.swedish_flag, "Swedish", "Svenska", "sv"),
            AppLanguage(CoreR.drawable.tamil_flag, "Tamil", "தமிழ்", "ta"),
            AppLanguage(CoreR.drawable.telugu_flag, "Telugu", "తెలుగు", "te"),
            AppLanguage(CoreR.drawable.thai_flag, "Thai", "แบบไทย", "th"),
            AppLanguage(CoreR.drawable.turkish_flag, "Turkish", "Türkçe", "tr"),
            AppLanguage(CoreR.drawable.ukrainian_flag, "Ukrainian", "українська", "uk"),
            AppLanguage(CoreR.drawable.vietnamese_flag, "Vietnamese", "Tiếng Việt", "vi"),
            AppLanguage(CoreR.drawable.yoruba_flag, "Yoruba", "Yoruba", "yo"),
            AppLanguage(CoreR.drawable.zulu_flag, "Zulu", "Zulu", "zu")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        isFromSplash = intent.extras?.getBoolean("isFromSplash", false) ?: false

        binding.apply {
            backArrowIV.isVisible = !isFromSplash
            applyBtn.startAnimation(animation)
            backArrowIV.setOnClickListener(this@AppLanguageActivity)
            applyBtn.setOnClickListener(this@AppLanguageActivity)
            crossIV.setOnClickListener(this@AppLanguageActivity)

            languageSearchET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    p0?.let {
                        crossIV.isVisible = it.isNotEmpty()
                        val filteredLanguagesListToSubmit = languagesList.filter { appLanguage ->
                            appLanguage.languageName.contains(it.toString().trim(), true)
                        }.map { appLanguage ->
                            appLanguage.copy()
                        }
                        appLanguageRVAdapter.submitList(filteredLanguagesListToSubmit) {
                            languageRV.smoothScrollToPosition(0)
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            if (savedInstanceState == null) {
                languagesList.forEach {
                    it.isSelected = it.languageCode == prefs.selectedLanguageCode
                }
                val listToSubmit = languagesList.map {
                    if (it.languageCode == prefs.selectedLanguageCode) selectedAppLanguage = it
                    it.copy(isSelected = it.languageCode == prefs.selectedLanguageCode)
                }
                showLanguagesOnRV(listToSubmit)
            }
        }
    }

    private fun ActivityAppLanguageBinding.showLanguagesOnRV(listToSubmit: List<AppLanguage>? = null) {
        languageRV.layoutManager = LinearLayoutManager(activityContext, VERTICAL, false)
        appLanguageRVAdapter.submitList(listToSubmit)
        languageRV.adapter = appLanguageRVAdapter
    }

    override fun onClick(p0: View?) {
        p0?.let {
            binding.apply {
                when (it.id) {
                    R.id.backArrowIV -> callBackPressed()
                    R.id.crossIV -> languageSearchET.text = null
                    R.id.applyBtn -> {
                        if (prefs.selectedLanguageCode != (selectedAppLanguage?.languageCode ?: "en")) {
                            prefs.selectedLanguageCode = selectedAppLanguage?.languageCode ?: "en"
                            if (!isFromSplash) setResult(RESULT_OK)
                        }
                        if (isFromSplash) {
                            openDashBoardActivity()
//                            openSignUpOrSignInOrDashBoardActivities()
                        }
                        finish()
                    }
                }
            }
        }
    }

    /*private fun openSignUpOrSignInOrDashBoardActivities() {
        val goNextIntent = if (IS_SHOW_SIGN_IN_SIGN_OUT_SCREEN)
            Intent(activityContext, if (prefs.isUserSignIn) DashBoardActivity::class.java else SignInActivity::class.java)
        else
            Intent(activityContext, DashBoardActivity::class.java)
        goNextIntent.apply {
            startActivity(this)
        }
    }*/

    private fun openDashBoardActivity() {
        Intent(activityContext, DashBoardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    }

    override fun handleActivitiesBackPressed() {
        if (isFromSplash) {
            openDashBoardActivity()
//            openSignUpOrSignInOrDashBoardActivities()
        } else finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putParcelable("appLanguage", selectedAppLanguage)
            putParcelableArrayList("languagesList", ArrayList(appLanguageRVAdapter.currentList))
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.apply {
            val (appLanguage, list) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Pair(
                    getParcelable("appLanguage", AppLanguage::class.java),
                    getParcelableArrayList("languagesList", AppLanguage::class.java)
                )
            } else {
                @Suppress("DEPRECATION")
                Pair(
                    getParcelable("appLanguage"),
                    getParcelableArrayList("languagesList")
                )
            }
            languagesList.forEach {
                it.isSelected = it.languageCode == (appLanguage?.languageCode ?: "en")
            }
            binding.apply {
                showLanguagesOnRV(list)
            }
        }
    }
}