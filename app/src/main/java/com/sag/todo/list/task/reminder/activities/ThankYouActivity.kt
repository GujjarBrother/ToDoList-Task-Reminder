package com.sag.todo.list.task.reminder.activities

import android.animation.Animator
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivityThankYouBinding
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.AppConstants.changeVisibility
import com.sag.todo.list.task.reminder.utils.AppConstants.keepActivityOn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ThankYouActivity : BaseActivity() {

    private val binding by lazy {
        ActivityThankYouBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        keepActivityOn(this)

        binding.apply {
            thankYouLottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(p0: Animator) {
                }

                override fun onAnimationEnd(p0: Animator) {
                    forUsingAnAppAndComeBackAgainGroup.changeVisibility(Visibility.VISIBLE)
                    lifecycleScope.launch {
                        delay(2000)
                        finishAffinity()
                    }
                }

                override fun onAnimationRepeat(p0: Animator) {
                }

                override fun onAnimationStart(p0: Animator) {
                }
            })
        }
    }

    override fun handleActivitiesBackPressed() {
    }
}