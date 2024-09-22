package com.todo.list.activities

import android.os.Bundle
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivityThankYouBinding
import com.todo.list.utils.ColorsUtils.getContextCompatColor
import com.todo.list.utils.ColorsUtils.screensNightModeColor
import com.todo.list.utils.ColorsUtils.whiteColor
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity

class ThankYouActivity : BaseActivity() {

    private lateinit var binding: ActivityThankYouBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThankYouBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyLightAndDarkMode()
        keepActivityOn(activityContext)
        makeFullScreenActivity(activityContext)
        applyCustomFont()
        handler.postDelayed({ finishAffinity() }, 2000)
    }

    private fun applyCustomFont() {
        with(binding) {
            forUsingAnAppTextView.typeface = typeface
            descriptionTextView.typeface = typeface
        }
    }

    private fun applyLightAndDarkMode() {
        with(binding) {
            if (prefs.isDarkModeEnable) {
                thankYouActivityRootLayout.setBackgroundColor(getContextCompatColor(activityContext, screensNightModeColor))
                forUsingAnAppTextView.setTextColor(getContextCompatColor(activityContext, whiteColor))
                descriptionTextView.setTextColor(getContextCompatColor(activityContext, whiteColor))
            }
        }
    }
}