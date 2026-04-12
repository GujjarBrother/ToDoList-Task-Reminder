package com.sag.todo.list.task.reminder.activities

import android.os.Bundle
import android.view.View
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivityToDoTaskDetailBinding
import com.sag.todo.list.task.reminder.models.ToDoTask
import com.sag.todo.list.task.reminder.utils.AppConstants.keepActivityOn
import java.util.Locale

class ToDoTaskDetailActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityToDoTaskDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        keepActivityOn(activityContext)
        val toDoTask = intent.getSerializableExtra("taskDetail") as ToDoTask?

        binding.apply {
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
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> callBackPressed()
        }
    }

    /*Override 'onConfigurationChanged' Method, Which Is Used To Prevent An Activity To 'Re-create' When
    Changing The Screen Orientation.i.e., Switching Between 'PORTRAIT MODE' TO 'LANDSCAPE MODE' & Vice Versa.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }*/

    override fun handleActivitiesBackPressed() = finish()
}