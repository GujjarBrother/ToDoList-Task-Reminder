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
import com.todo.list.enums.TabsEnum

class TasksRecyclerViewAdapter(
        private val colorsSchemeArray: IntArray,
        private val isAppColorChanged: Boolean,
        private val fromWhereInvoked: Int,
        private val taskDetailCallback: ((ToDoTask) -> Unit)? = null,
        private val taskUpdateAndDeleteCallback: ((ToDoTask, View, Int) -> Unit)? = null
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
        /*val signInCardAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_view_single_item_animation)
        holder.itemView.startAnimation(signInCardAnimation)*/

        val toDoTask = getItem(position)

        with(holder.binding) {
            if (fromWhereInvoked == TabsEnum.TASKS_TAB.ordinal) {
                deleteFromCompletedFragmentImageView.visibility = GONE
                updateAndDeleteOptionsIV.visibility = VISIBLE
                updateAndDeleteOptionsIV.isEnabled = true
            } else if (fromWhereInvoked == TabsEnum.COMPLETED_TAB.ordinal) {
                updateAndDeleteOptionsIV.visibility = INVISIBLE
                updateAndDeleteOptionsIV.isEnabled = false
                deleteFromCompletedFragmentImageView.visibility = VISIBLE
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

            if (isTextSizeChanged) {
                holder.binding.toDoTaskTitleTV.textSize = prefs.textSizeValue.toFloat()
            }

            updateAndDeleteOptionsIV.setOnClickListener { v: View ->
                taskUpdateAndDeleteCallback?.invoke(toDoTask, v, color)
            }
        }

        if (isAppColorChanged) {
            applyColorSchemeOnNormalRVItems(holder)
        }

        holder.itemView.setOnClickListener { _: View? ->
            taskDetailCallback?.invoke(toDoTask)
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
                deleteFromCompletedFragmentImageView.setColorFilter(holder.whiteColor)
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
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[0])
                    }

                    1 -> {
                        color = colorsSchemeArray[1]
                        dateTV.setTextColor(colorsSchemeArray[1])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[1])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[1])
                    }

                    2 -> {
                        color = colorsSchemeArray[2]
                        dateTV.setTextColor(colorsSchemeArray[2])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[2])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[2])
                    }

                    3 -> {
                        color = colorsSchemeArray[3]
                        dateTV.setTextColor(colorsSchemeArray[3])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[3])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[3])
                    }

                    4 -> {
                        color = colorsSchemeArray[4]
                        dateTV.setTextColor(colorsSchemeArray[4])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[4])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[4])
                    }

                    5 -> {
                        color = colorsSchemeArray[5]
                        dateTV.setTextColor(colorsSchemeArray[5])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[5])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[5])
                    }

                    6 -> {
                        color = colorsSchemeArray[6]
                        dateTV.setTextColor(colorsSchemeArray[6])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[6])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[6])
                    }

                    7 -> {
                        color = colorsSchemeArray[7]
                        dateTV.setTextColor(colorsSchemeArray[7])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[7])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[7])
                    }

                    8 -> {
                        color = colorsSchemeArray[8]
                        dateTV.setTextColor(colorsSchemeArray[8])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[8])
                        deleteFromCompletedFragmentImageView.setColorFilter(colorsSchemeArray[8])
                    }

                    9 -> {
                        color = colorsSchemeArray[9]
                        dateTV.setTextColor(colorsSchemeArray[9])
                        updateAndDeleteOptionsIV.setColorFilter(colorsSchemeArray[9])
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