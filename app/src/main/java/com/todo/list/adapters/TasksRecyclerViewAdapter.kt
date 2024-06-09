package com.todo.list.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.list.R
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.databinding.ToDosRecyclerViewSingleItemLayoutBinding
import com.todo.list.db.ToDoTask
import com.todo.list.listeners.TaskUpdateAndDeleteListener
import com.todo.list.listeners.ToDoTaskDetailListener
import com.todo.list.utils.CommonFunctions.COMPLETED_TAB
import com.todo.list.utils.CommonFunctions.TASKS_TAB

class TasksRecyclerViewAdapter(
        private val toDoTaskDetailListener: ToDoTaskDetailListener,
        private val taskUpdateAndDeleteListener: TaskUpdateAndDeleteListener? = null,
        private val colorsSchemeArray: IntArray,
        private val isAppColorChanged: Boolean,
        private val fromWhereInvoked: Int
) : ListAdapter<ToDoTask, TasksRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK) {

    var isTextSizeChanged = false
    private var color = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                ToDosRecyclerViewSingleItemLayoutBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val signInCardAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_view_single_item_animation)
        holder.itemView.startAnimation(signInCardAnimation)

        val toDoTask = getItem(position)

        with(holder.binding) {
            if (fromWhereInvoked == TASKS_TAB) {
                deleteFromCompletedFragmentImageView.visibility = GONE
                updateAndDeleteOptionsImageView.visibility = VISIBLE
                updateAndDeleteOptionsImageView.isEnabled = true
            } else if (fromWhereInvoked == COMPLETED_TAB) {
                updateAndDeleteOptionsImageView.visibility = INVISIBLE
                updateAndDeleteOptionsImageView.isEnabled = false
                deleteFromCompletedFragmentImageView.visibility = VISIBLE
            }

            if (toDoTask.day.length >= 3) {
                dayTextView.text = toDoTask.day.substring(0, 3)
            } else {
                dayTextView.text = toDoTask.day
            }
            dateTextView.text = toDoTask.date
            monthTextView.text = toDoTask.month
            toDoTaskTitleTextView.text = toDoTask.title
            toDoTaskDescriptionTextView.text = toDoTask.description
            toDoTaskTimeTextView.text = toDoTask.time

            if (isTextSizeChanged) {
                holder.binding.toDoTaskTitleTextView.textSize = prefs.textSizeValue.toFloat()
            }

            updateAndDeleteOptionsImageView.setOnClickListener { v: View ->
                taskUpdateAndDeleteListener?.taskUpdateAndDelete(toDoTask, v, color, position)
            }
        }

        if (isAppColorChanged) {
            applyColorSchemeOnNormalRVItems(holder)
        }

        holder.itemView.setOnClickListener { _: View? ->
            toDoTaskDetailListener.taskDetail(toDoTask)
        }
    }

    class ViewHolder(val binding: ToDosRecyclerViewSingleItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        val whiteColor: Int = ContextCompat.getColor(itemView.context, R.color.whiteColor)
        val blackColor: Int = ContextCompat.getColor(itemView.context, R.color.blackColor)
        val cardDarkModeColor: Int = ContextCompat.getColor(itemView.context, R.color.cardsNightModeColor)
        val lightBlackColor: Int = ContextCompat.getColor(itemView.context, R.color.lightBlackColor)
        val recyclerViewsDividerColor: Int = ContextCompat.getColor(itemView.context, R.color.recyclerViewsDividerColor)

        init {
            with(binding) {
                dayTextView.typeface = typeface
                dateTextView.typeface = typeface
                monthTextView.typeface = typeface
                toDoTaskTitleTextView.typeface = typeface
                toDoTaskDescriptionTextView.typeface = typeface
                toDoTaskTimeTextView.typeface = typeface
            }
        }
    }

    private fun applyColorSchemeOnNormalRVItems(holder: ViewHolder) {
        with(holder.binding) {
            if (prefs.dayAndNightModeSwitchValue) {
                cardView.setCardBackgroundColor(holder.cardDarkModeColor)
                dayTextView.setTextColor(holder.whiteColor)
                dateTextView.setTextColor(holder.whiteColor)
                monthTextView.setTextColor(holder.whiteColor)
                view.setBackgroundColor(holder.whiteColor)
                toDoTaskTitleTextView.setTextColor(holder.whiteColor)
                toDoTaskDescriptionTextView.setTextColor(holder.whiteColor)
                toDoTaskTimeTextView.setTextColor(holder.whiteColor)
                updateAndDeleteOptionsImageView.setColorFilter(holder.whiteColor)
                deleteFromCompletedFragmentImageView.setColorFilter(holder.whiteColor)
            } else {
                cardView.setCardBackgroundColor(holder.whiteColor)
                dayTextView.setTextColor(holder.lightBlackColor)
                monthTextView.setTextColor(holder.lightBlackColor)
                view.setBackgroundColor(holder.recyclerViewsDividerColor)
                toDoTaskTitleTextView.setTextColor(holder.blackColor)
                toDoTaskDescriptionTextView.setTextColor(holder.lightBlackColor)
                toDoTaskTimeTextView.setTextColor(holder.lightBlackColor)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        color = colorsSchemeArray[0]
                        dateTextView.setTextColor(colorsSchemeArray[0])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[0])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[0])
                    }

                    1 -> {
                        color = colorsSchemeArray[1]
                        dateTextView.setTextColor(colorsSchemeArray[1])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[1])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[1])
                    }

                    2 -> {
                        color = colorsSchemeArray[2]
                        dateTextView.setTextColor(colorsSchemeArray[2])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[2])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[2])
                    }

                    3 -> {
                        color = colorsSchemeArray[3]
                        dateTextView.setTextColor(colorsSchemeArray[3])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[3])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[3])
                    }

                    4 -> {
                        color = colorsSchemeArray[4]
                        dateTextView.setTextColor(colorsSchemeArray[4])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[4])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[4])
                    }

                    5 -> {
                        color = colorsSchemeArray[5]
                        dateTextView.setTextColor(colorsSchemeArray[5])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[5])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[5])
                    }

                    6 -> {
                        color = colorsSchemeArray[6]
                        dateTextView.setTextColor(colorsSchemeArray[6])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[6])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[6])
                    }

                    7 -> {
                        color = colorsSchemeArray[7]
                        dateTextView.setTextColor(colorsSchemeArray[7])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[7])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[7])
                    }

                    8 -> {
                        color = colorsSchemeArray[8]
                        dateTextView.setTextColor(colorsSchemeArray[8])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[8])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[8])
                    }

                    9 -> {
                        color = colorsSchemeArray[9]
                        dateTextView.setTextColor(colorsSchemeArray[9])
                        updateAndDeleteOptionsImageView.setColorFilter(colorsSchemeArray[9])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[9])
                    }
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<ToDoTask> = object : DiffUtil.ItemCallback<ToDoTask>() {
            override fun areItemsTheSame(oldItem: ToDoTask, newItem: ToDoTask): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: ToDoTask, newItem: ToDoTask): Boolean {
                return oldItem === newItem
            }
        }
    }
}