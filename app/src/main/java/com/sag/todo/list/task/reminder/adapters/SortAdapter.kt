package com.sag.todo.list.task.reminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sag.todo.list.task.reminder.databinding.SortByRvSingleItemBinding
import com.sag.todo.list.task.reminder.models.Sort

class SortAdapter(
    private val sortClickCallback: ((Sort, Int) -> Unit)? = null
) : ListAdapter<Sort, SortAdapter.SortViewHolder>(SORT_DIFFUTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SortViewHolder(SortByRvSingleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SortViewHolder, position: Int) {
        val sort = getItem(position)
        holder.sortByRvSingleItemBinding.sortNameRB.apply {
            sort.let {
                text = it.sortName
                isChecked = it.isSelected
            }
        }
    }

    inner class SortViewHolder(val sortByRvSingleItemBinding: SortByRvSingleItemBinding) : RecyclerView.ViewHolder(sortByRvSingleItemBinding.root) {
        init {
            sortByRvSingleItemBinding.apply {
                root.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) sortClickCallback?.invoke(getItem(position), position)
                }
            }
        }
    }

    companion object {
        val SORT_DIFFUTIL = object : DiffUtil.ItemCallback<Sort>() {
            override fun areItemsTheSame(oldItem: Sort, newItem: Sort) = oldItem.sortName == newItem.sortName
            override fun areContentsTheSame(oldItem: Sort, newItem: Sort) = oldItem == newItem
        }
    }
}