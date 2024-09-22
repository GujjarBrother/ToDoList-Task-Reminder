package com.todo.list.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.databinding.ToDosRecyclerViewSingleItemLayoutBinding
import com.todo.list.db.ToDoTask
import com.todo.list.enums.Tabs
import com.todo.list.enums.Visibility
import com.todo.list.models.SelectedColors
import com.todo.list.utils.ColorsUtils.blackColor
import com.todo.list.utils.ColorsUtils.cardsNightModeColor
import com.todo.list.utils.ColorsUtils.darkModeTextColor
import com.todo.list.utils.ColorsUtils.getContextCompatColor
import com.todo.list.utils.ColorsUtils.lightBlackColor
import com.todo.list.utils.ColorsUtils.lightBlueColor
import com.todo.list.utils.ColorsUtils.recyclerViewsDividerColor
import com.todo.list.utils.ColorsUtils.whiteColor
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.isSomethingChanged

class TasksRecyclerViewAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val selectedColors: SelectedColors,
    private val fromWhereInvoked: Int,
    private val commonCallback: (ToDoTask) -> Unit,
    private var taskUpdateAndDeleteCallback: (ToDoTask, View, Int) -> Unit
) : ListAdapter<ToDoTask, TasksRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK), Filterable {

    private var color = 0
    private var fullList = listOf<ToDoTask>()
    private var filteredList = listOf<ToDoTask>()

    init {
        filteredList = currentList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                ToDosRecyclerViewSingleItemLayoutBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val toDoTask = getItem(position)

        with(holder.binding) {
            if (fromWhereInvoked == Tabs.TASKS_TAB.ordinal) {
                deleteFromCompletedFragmentIV.changeVisibility(Visibility.GONE.ordinal)
                updateAndDeleteOptionsIV.changeVisibility(Visibility.VISIBLE.ordinal)
                updateAndDeleteOptionsIV.isEnabled = true
            } else if (fromWhereInvoked == Tabs.COMPLETED_TAB.ordinal) {
                updateAndDeleteOptionsIV.changeVisibility(Visibility.INVISIBLE.ordinal)
                updateAndDeleteOptionsIV.isEnabled = false
                deleteFromCompletedFragmentIV.changeVisibility(Visibility.VISIBLE.ordinal)
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
                rootCV.setCardBackgroundColor(getContextCompatColor(context, cardsNightModeColor))
                dayTV.setTextColor(getContextCompatColor(context, darkModeTextColor))
                dateTV.setTextColor(getContextCompatColor(context, lightBlueColor))
                monthTV.setTextColor(getContextCompatColor(context, darkModeTextColor))
                view.setBackgroundColor(getContextCompatColor(context, darkModeTextColor))
                toDoTaskTitleTV.setTextColor(getContextCompatColor(context, whiteColor))
                toDoTaskDescriptionTV.setTextColor(getContextCompatColor(context, whiteColor))
                toDoTaskTimeTV.setTextColor(getContextCompatColor(context, darkModeTextColor))
                updateAndDeleteOptionsIV.setColorFilter(getContextCompatColor(context, lightBlueColor))
                deleteFromCompletedFragmentIV.setColorFilter(getContextCompatColor(context, whiteColor))
            } else {
                rootCV.setCardBackgroundColor(getContextCompatColor(context, whiteColor))
                dayTV.setTextColor(getContextCompatColor(context, lightBlackColor))
                monthTV.setTextColor(getContextCompatColor(context, lightBlackColor))
                view.setBackgroundColor(getContextCompatColor(context, recyclerViewsDividerColor))
                toDoTaskTitleTV.setTextColor(getContextCompatColor(context, blackColor))
                toDoTaskDescriptionTV.setTextColor(getContextCompatColor(context, lightBlackColor))
                toDoTaskTimeTV.setTextColor(getContextCompatColor(context, lightBlackColor))
                color = selectedColors.originalColor
                dateTV.setTextColor(selectedColors.originalColor)
                updateAndDeleteOptionsIV.setColorFilter(selectedColors.originalColor)
                deleteFromCompletedFragmentIV.setColorFilter(selectedColors.originalColor)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<ToDoTask> = object : DiffUtil.ItemCallback<ToDoTask>() {
            override fun areItemsTheSame(oldItem: ToDoTask, newItem: ToDoTask): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ToDoTask, newItem: ToDoTask): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun setFullList(list: List<ToDoTask>) {
        fullList = list
        submitList(fullList)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString()
                filteredList = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.title.contains(query, true)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<ToDoTask>
                submitList(filteredList)
            }
        }
    }
}