package com.sag.todo.list.task.reminder.activities

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.adapters.AppLanguageRVAdapter
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.controllers.localization.AppLanguage
import com.sag.todo.list.task.reminder.databinding.ActivityAppLanguageBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class AppLanguageActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityAppLanguageBinding.inflate(layoutInflater)
    }

    @Inject
    @Named(value = "FabRateUsAndApplyAnimation")
    lateinit var animation: Animation
    private var selectedAppLanguage: AppLanguage? = null
    private val appLanguageRVAdapter: AppLanguageRVAdapter by lazy {
        AppLanguageRVAdapter(appLanguageClickCallback = { appLanguage, currentPosition, oldPosition ->
            selectedAppLanguage = appLanguage
            languagesList[oldPosition].isSelected = false
            appLanguageRVAdapter.notifyItemChanged(oldPosition)

            languagesList[currentPosition].isSelected = true
            appLanguageRVAdapter.notifyItemChanged(currentPosition)
        })
    }
    private val languagesList by lazy {
        mutableListOf<AppLanguage>(
            AppLanguage(R.drawable.urdu_flag, "Urdu", "اردو", "ur"),
            AppLanguage(R.drawable.arabic_flag, "Arabic", "عربي", "ar"),
            AppLanguage(R.drawable.english_flag, "English", "English", "en"),
            AppLanguage(R.drawable.african_flag, "African", "Afrikaanse", "af"),
            AppLanguage(R.drawable.bengali_flag, "Bengali", "বাংলা", "bn"),
            AppLanguage(R.drawable.bhojpuri_flag, "Bhojpuri", "भोजपुरी", "bho"),
            AppLanguage(R.drawable.bulgarian_flag, "Bulgarian", "български", "bg"),
            AppLanguage(R.drawable.myanmar_burmese_flag, "Myanmar (Burmese)", "မြန်မာ (ဗမာ)၊", "my"),
            AppLanguage(R.drawable.chinese_simplified_flag, "Chinese (Simplified)", "简体中文", "zh-CN"),
            AppLanguage(R.drawable.chinese_traditional_flag, "Chinese (Traditional)", "繁體中文", "zh-TW"),
            AppLanguage(R.drawable.czech_flag, "Czech", "čeština", "cs"),
            AppLanguage(R.drawable.danish_flag, "Danish", "Dansk", "da"),
            AppLanguage(R.drawable.dutch_flag, "Dutch", "Nederlands", "nl"),
            AppLanguage(R.drawable.filipino_flag, "Filipino", "Filipino", "tl"),
            AppLanguage(R.drawable.finnish_flag, "Finnish", "suomi", "fi"),
            AppLanguage(R.drawable.french_flag, "French", "Français", "fr"),
            AppLanguage(R.drawable.german_flag, "German", "Deutsch", "de"),
            AppLanguage(R.drawable.greek_flag, "Greek", "ελληνικά", "el"),
            AppLanguage(R.drawable.guarani_flag, "Guarani", "guarani", "gn"),
            AppLanguage(R.drawable.hausa_flag, "Hausa", "Hausa", "ha"),
            AppLanguage(R.drawable.hebrew_flag, "Hebrew", "עִברִית", "iw"),
            AppLanguage(R.drawable.hindi_flag, "Hindi", "हिन्दी", "hi"),
            AppLanguage(R.drawable.hungarian_flag, "Hungarian", "magyar", "hu"),
            AppLanguage(R.drawable.indonesian_flag, "Indonesian", "Indonesia", "id"),
            AppLanguage(R.drawable.italian_flag, "Italian", "Italiana", "it"),
            AppLanguage(R.drawable.japanese_flag, "Japanese", "日本語", "ja"),
            AppLanguage(R.drawable.korean_flag, "Korean", "한국인", "ko"),
            AppLanguage(R.drawable.malay_flag, "Malay", "Melayu", "ms"),
            AppLanguage(R.drawable.marathi_flag, "Marathi", "मराठी", "mr"),
            AppLanguage(R.drawable.oromo_flag, "Oromo", "Afaan Oromoo", "om"),
            AppLanguage(R.drawable.persian_flag, "Persian", "فارسی", "fa"),
            AppLanguage(R.drawable.polish_flag, "Polish", "Polski", "pl"),
            AppLanguage(R.drawable.portuguese_portugal, "Portuguese", "Português", "pt"),
            AppLanguage(R.drawable.quechua_flag, "Quechua", "Runasimi", "qu"),
            AppLanguage(R.drawable.romanian_flag, "Romanian", "Română", "ro"),
            AppLanguage(R.drawable.russain_flag, "Russian", "Русский", "ru"),
            AppLanguage(R.drawable.serbian_flag, "Serbian", "Српски", "sr"),
            AppLanguage(R.drawable.spanish_flag, "Spanish", "Español", "es"),
            AppLanguage(R.drawable.swahili_flag, "Swahili", "Kihispania", "sw"),
            AppLanguage(R.drawable.swedish_flag, "Swedish", "Svenska", "sv"),
            AppLanguage(R.drawable.tamil_flag, "Tamil", "தமிழ்", "ta"),
            AppLanguage(R.drawable.telugu_flag, "Telugu", "తెలుగు", "te"),
            AppLanguage(R.drawable.thai_flag, "Thai", "แบบไทย", "th"),
            AppLanguage(R.drawable.turkish_flag, "Turkish", "Türkçe", "tr"),
            AppLanguage(R.drawable.ukrainian_flag, "Ukrainian", "українська", "uk"),
            AppLanguage(R.drawable.vietnamese_flag, "Vietnamese", "Tiếng Việt", "vi"),
            AppLanguage(R.drawable.yoruba_flag, "Yoruba", "Yoruba", "yo"),
            AppLanguage(R.drawable.zulu_flag, "Zulu", "Zulu", "zu")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultColor = ContextCompat.getColor(activityContext, R.color.defaultColor)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(defaultColor),
            navigationBarStyle = SystemBarStyle.dark(defaultColor)
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        with(binding) {
            applyBtn.startAnimation(animation)
            backArrowIV.setOnClickListener(this@AppLanguageActivity)
            applyBtn.setOnClickListener(this@AppLanguageActivity)

            languageRV.layoutManager = LinearLayoutManager(activityContext, VERTICAL, false)
            languagesList.forEach {
                if (it.languageCode == prefs.selectedLanguageCode) {
                    it.isSelected = true
                    selectedAppLanguage = it
                }
            }
            appLanguageRVAdapter.submitList(languagesList)
            languageRV.adapter = appLanguageRVAdapter
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onClick(p0: View?) {
        p0?.let {
            when (it.id) {
                R.id.backArrowIV -> finish()
                R.id.applyBtn -> {
                    prefs.selectedLanguageCode = selectedAppLanguage?.languageCode ?: ""
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }
}