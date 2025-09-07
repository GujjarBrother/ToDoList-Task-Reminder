package com.sag.todo.list.task.reminder.activities

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivityToDoTaskDetailBinding
import com.sag.todo.list.task.reminder.db.ToDoTask
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.keepActivityOn
import java.util.Locale

class ToDoTaskDetailActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityToDoTaskDetailBinding.inflate(layoutInflater)
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

        keepActivityOn(activityContext)
        val toDoTask = intent.getSerializableExtra("taskDetail") as ToDoTask?

        with(binding) {
            if (toDoTask != null) {
                toolbarTV.text = toDoTask.title
                titleTV.text = toDoTask.title
                descriptionTV.text = toDoTask.description
                dateAndDayTV.text = String.format(Locale.getDefault(), "%s, %s %s, %s",
                    toDoTask.day, toDoTask.month, toDoTask.date, toDoTask.year)
                timeTV.text = toDoTask.time
            }
            titleTV.textSize = prefs.textSizeValue.toFloat()
            descriptionTV.textSize = prefs.textSizeValue.toFloat()
            dateAndDayTV.textSize = prefs.textSizeValue.toFloat()
            timeTV.textSize = prefs.textSizeValue.toFloat()
            backArrowIV.setOnClickListener(this@ToDoTaskDetailActivity)
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackToDashBoardActivity()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> goBackToDashBoardActivity()
        }
    }

    private fun goBackToDashBoardActivity() = finish()

    //    Override 'onConfigurationChanged' Method, Which Is Used To Prevent An Activity To 'Re-create' When
    //    Changing The Screen Orientation.i.e., Switching Between 'PORTRAIT MODE' TO 'LANDSCAPE MODE' & Vice Versa.
    /*override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }*/
}