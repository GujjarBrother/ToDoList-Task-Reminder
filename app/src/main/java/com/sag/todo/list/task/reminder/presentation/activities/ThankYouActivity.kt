package com.sag.todo.list.task.reminder.presentation.activities

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.databinding.ActivityThankYouBinding
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.keepActivityOn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ThankYouActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityThankYouBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)
        val defaultColor = ContextCompat.getColor(this, R.color.defaultColor)
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

        keepActivityOn(this)

        lifecycleScope.launch {
            delay(2000)
            finishAffinity()
        }
    }
}