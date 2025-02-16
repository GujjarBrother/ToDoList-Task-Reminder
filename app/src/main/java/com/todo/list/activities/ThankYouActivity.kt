package com.todo.list.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.todo.list.R
import com.todo.list.databinding.ActivityThankYouBinding
import com.todo.list.utils.ColorsUtils.getContextCompatColor
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity
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

        changeStatusBarColor(this, getContextCompatColor(this, R.color.defaultColor))
        keepActivityOn(this)
        makeFullScreenActivity(this)
        lifecycleScope.launch {
            delay(2000)
            finishAffinity()
        }
    }
}