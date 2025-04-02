package com.sag.todo.list.task.reminder.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.databinding.ActivityThankYouBinding
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeStatusBarColor
import com.sag.todo.list.task.reminder.utils.CommonFunctions.keepActivityOn
import com.sag.todo.list.task.reminder.utils.CommonFunctions.makeFullScreenActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ThankYouActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityThankYouBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        changeStatusBarColor(this, ContextCompat.getColor(this, R.color.defaultColor))
        keepActivityOn(this)
        makeFullScreenActivity(this)
        lifecycleScope.launch {
            delay(2000)
            finishAffinity()
        }
    }
}