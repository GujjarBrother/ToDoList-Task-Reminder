package com.todo.list.activities

import android.os.Bundle
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivityThankYouBinding
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.makeFullScreenActivity

class ThankYouActivity : BaseActivity() {

    private lateinit var binding: ActivityThankYouBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThankYouBinding.inflate(layoutInflater)
        setContentView(binding.root)

        keepActivityOn(activityContext)
        makeFullScreenActivity(activityContext)
        handler.postDelayed({ finishAffinity() }, 2000)
    }
}