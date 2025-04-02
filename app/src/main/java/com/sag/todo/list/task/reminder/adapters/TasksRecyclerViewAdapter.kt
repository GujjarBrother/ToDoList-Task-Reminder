package com.sag.todo.list.task.reminder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sag.todo.list.task.reminder.databinding.ToDosRecyclerViewSingleItemLayoutBinding
import com.sag.todo.list.task.reminder.db.ToDoTask
import com.sag.todo.list.task.reminder.enums.Tabs
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeVisibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.isSomethingChanged
import com.sag.todo.list.task.reminder.utils.Prefs

class TasksRecyclerViewAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val fromWhereInvoked: Int,
    private val prefs: Prefs,
    private val commonCallback: (ToDoTask) -> Unit,
    private var taskUpdateAndDeleteCallback: (ToDoTask, View) -> Unit
) : ListAdapter<ToDoTask, TasksRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK), Filterable {

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
                taskUpdateAndDeleteCallback.invoke(toDoTask, v)
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
        }
    }

    class ViewHolder(val binding: ToDosRecyclerViewSingleItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

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