package com.todo.list.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.list.R
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.databinding.ToDosRecyclerViewSingleItemLayoutBinding
import com.todo.list.db.ToDoTask
import com.todo.list.enums.TabsEnum
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.isSomethingChanged

class TasksRecyclerViewAdapter(
    private val lifecycleOwner: LifecycleOwner,
        private val colorsSchemeArray: IntArray,
        private val fromWhereInvoked: Int,
        private val commonCallback: (ToDoTask) -> Unit,
        private var taskUpdateAndDeleteCallback: (ToDoTask, View, Int) -> Unit
) : ListAdapter<ToDoTask, TasksRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var color = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                ToDosRecyclerViewSingleItemLayoutBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val toDoTask = getItem(position)

        with(holder.binding) {
            if (fromWhereInvoked == TabsEnum.TASKS_TAB.ordinal) {
                deleteFromCompletedFragmentIV.changeVisibility(0)
                updateAndDeleteOptionsIV.changeVisibility(1)
                updateAndDeleteOptionsIV.isEnabled = true
            } else if (fromWhereInvoked == TabsEnum.COMPLETED_TAB.ordinal) {
                updateAndDeleteOptionsIV.changeVisibility(2)
                updateAndDeleteOptionsIV.isEnabled = false
                deleteFromCompletedFragmentIV.changeVisibility(1)
            }

            if (toDoTask.day.length >= 3) {
                dayTV.text = toDoTask.day.substring(0, 3)
            } else {
                dayTV.text = toDoTask.day
            }
            dateTV.text = toDoTask.date
            monthTV.text = toDoTask.month
            toDoTaskTitleTV.text = toDoTask.title
            toDoTaskDescriptionTV.text = toDoTask.description
            toDoTaskTimeTV.text = toDoTask.time

            updateAndDeleteOptionsIV.setOnClickListener { v: View ->
                taskUpdateAndDeleteCallback.invoke(toDoTask, v, color)
            }

            deleteFromCompletedFragmentIV.setOnClickListener {
                commonCallback.invoke(toDoTask)
            }
        }

        holder.itemView.setOnClickListener { _: View? ->
            commonCallback.invoke(toDoTask)
        }

        isSomethingChanged.observe(lifecycleOwner) {
            holder.binding.toDoTaskTitleTV.textSize = prefs.textSizeValue.toFloat()
            applyColorSchemeOnNormalRVItems(holder)
        }
    }

    class ViewHolder(val binding: ToDosRecyclerViewSingleItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        val whiteColor: Int = ContextCompat.getColor(itemView.context, R.color.whiteColor)
        val lightBlueColor: Int = ContextCompat.getColor(itemView.context, R.color.lightBlueColor)
        val darkModeTextColor: Int = ContextCompat.getColor(itemView.context, R.color.purple_500)
        val blackColor: Int = ContextCompat.getColor(itemView.context, R.color.blackColor)
        val cardDarkModeColor: Int = ContextCompat.getColor(itemView.context, R.color.cardsNightModeColor)
        val lightBlackColor: Int = ContextCompat.getColor(itemView.context, R.color.lightBlackColor)
        val recyclerViewsDividerColor: Int = ContextCompat.getColor(itemView.context, R.color.recyclerViewsDividerColor)

        init {
            with(binding) {
                dayTV.typeface = typeface
                dateTV.typeface = typeface
                monthTV.typeface = typeface
                toDoTaskTitleTV.typeface = typeface
                toDoTaskDescriptionTV.typeface = typeface
                toDoTaskTimeTV.typeface = typeface
            }
        }
    }

    private fun applyColorSchemeOnNormalRVItems(holder: ViewHolder) {
        with(holder.binding) {
            if (prefs.isDarkModeEnable) {
                rootCV.setCardBackgroundColor(holder.cardDarkModeColor)
                dayTV.setTextColor(holder.darkModeTextColor)
                dateTV.setTextColor(holder.lightBlueColor)
                monthTV.setTextColor(holder.darkModeTextColor)
                view.setBackgroundColor(holder.darkModeTextColor)
                toDoTaskTitleTV.setTextColor(holder.whiteColor)
                toDoTaskDescriptionTV.setTextColor(holder.whiteColor)
                toDoTaskTimeTV.setTextColor(holder.darkModeTextColor)
                updateAndDeleteOptionsIV.setColorFilter(holder.lightBlueColor)
                deleteFromCompletedFragmentIV.setColorFilter(holder.whiteColor)
            } else {
                rootCV.setCardBackgroundColor(holder.whiteColor)
                dayTV.setTextColor(holder.lightBlackColor)
                monthTV.setTextColor(holder.lightBlackColor)
                view.setBackgroundColor(holder.recyclerViewsDividerColor)
                toDoTaskTitleTV.setTextColor(holder.blackColor)
                toDoTaskDescriptionTV.setTextColor(holder.lightBlackColor)
                toDoTaskTimeTV.setTextColor(holder.lightBlackColor)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        color = colorsSchemeArray[0]
                        dateTV.setTextColor(colorsSchemeArray[0])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[0])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[0])
                    }

                    1 -> {
                        color = colorsSchemeArray[1]
                        dateTV.setTextColor(colorsSchemeArray[1])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[1])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[1])
                    }

                    2 -> {
                        color = colorsSchemeArray[2]
                        dateTV.setTextColor(colorsSchemeArray[2])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[2])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[2])
                    }

                    3 -> {
                        color = colorsSchemeArray[3]
                        dateTV.setTextColor(colorsSchemeArray[3])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[3])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[3])
                    }

                    4 -> {
                        color = colorsSchemeArray[4]
                        dateTV.setTextColor(colorsSchemeArray[4])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[4])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[4])
                    }

                    5 -> {
                        color = colorsSchemeArray[5]
                        dateTV.setTextColor(colorsSchemeArray[5])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[5])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[5])
                    }

                    6 -> {
                        color = colorsSchemeArray[6]
                        dateTV.setTextColor(colorsSchemeArray[6])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[6])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[6])
                    }

                    7 -> {
                        color = colorsSchemeArray[7]
                        dateTV.setTextColor(colorsSchemeArray[7])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[7])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[7])
                    }

                    8 -> {
                        color = colorsSchemeArray[8]
                        dateTV.setTextColor(colorsSchemeArray[8])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[8])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[8])
                    }

                    9 -> {
                        color = colorsSchemeArray[9]
                        dateTV.setTextColor(colorsSchemeArray[9])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[9])
                        deleteFromCompletedFragmentIV.setColorFilter(colorsSchemeArray[9])
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